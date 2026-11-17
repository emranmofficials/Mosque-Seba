package com.mosqueseba.app.data.remote.supabase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.mosqueseba.app.data.local.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

private const val TAG = "SupabaseRealtime"

/**
 * Lightweight Supabase Realtime (Phoenix v1 protocol) client.
 *
 * Opens a single WSS connection, joins one channel with `postgres_changes`
 * subscriptions for every table used by [SupabaseSyncManager], and reports
 * every committed database change so the caller can sync immediately instead
 * of waiting for a poll. Includes heartbeats and reconnect-with-backoff.
 */
object SupabaseRealtimeClient {

    private const val TOPIC = "realtime:mosque-seba-sync"
    private const val HEARTBEAT_MS = 20_000L
    private const val MAX_BACKOFF_MS = 30_000L

    private val SYNC_TABLES = listOf(
        "mosques", "members", "transactions", "notices", "meetings",
        "chanda_payments", "madrasa_students", "cashbox_records",
        "welfare_cases", "election_schedules", "election_candidates",
        "mosque_properties", "profiles",
        // Remaining feature modules
        "staff", "audit_logs", "madrasa_attendance", "hifz_progress",
        "madrasa_fee_payments", "staff_payroll", "duty_roster",
        "food_schedules", "mosque_events", "office_documents",
        "advisory_notes", "smart_board_quotes"
    )

    private val refs = AtomicInteger(1)

    @Volatile private var appContext: Context? = null
    @Volatile private var baseUrl: String = ""
    @Volatile private var apiKey: String = ""
    @Volatile private var webSocket: WebSocket? = null
    @Volatile private var running = false
    @Volatile private var joined = false
    @Volatile private var joinedMosqueId: String? = null
    @Volatile private var onDatabaseChange: ((String) -> Unit)? = null

    private var httpClient: OkHttpClient? = null
    private var scope: CoroutineScope? = null
    private var heartbeatJob: Job? = null
    private var reconnectJob: Job? = null
    private var reconnectAttempt = 0

    fun isConnected(): Boolean = running && joined && webSocket != null

    fun start(context: Context, onDatabaseChange: (String) -> Unit) {
        // Always reset before (re)starting so no duplicate sockets survive.
        stop()
        appContext = context.applicationContext
        this.onDatabaseChange = onDatabaseChange

        val (url, key) = SupabaseConfig.getCredentials(appContext!!)
        if (url.isBlank() || key.isBlank() || !url.startsWith("http")) {
            Log.w(TAG, "Supabase not configured — realtime disabled")
            return
        }
        baseUrl = url.trim().trimEnd('/')
        apiKey = key.trim()
        joinedMosqueId = SessionManager(appContext!!).getActiveMosqueId()

        running = true
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        httpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .pingInterval(HEARTBEAT_MS, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .build()
        connect()
    }

    /**
     * Re-joins the channel when the active mosque changes so the server-side
     * `mosque_id` filter always matches the currently selected tenant.
     */
    fun refreshFilter(context: Context) {
        val ctx = context.applicationContext
        val current = SessionManager(ctx).getActiveMosqueId()
        if (current == joinedMosqueId) return
        joinedMosqueId = current
        val callback = onDatabaseChange
        if (callback != null) start(ctx, callback)
    }

    fun stop() {
        running = false
        joined = false
        heartbeatJob?.cancel()
        reconnectJob?.cancel()
        heartbeatJob = null
        reconnectJob = null
        webSocket?.cancel()
        webSocket = null
        httpClient = null
        scope?.cancel()
        scope = null
        onDatabaseChange = null
        appContext = null
        reconnectAttempt = 0
    }

    private fun connect() {
        if (!running) return
        val url = buildWebSocketUrl(baseUrl, apiKey)
        val request = Request.Builder().url(url).build()
        Log.d(TAG, "Connecting to Supabase Realtime")
        webSocket = httpClient?.newWebSocket(request, SocketListener())
    }

    private fun buildWebSocketUrl(baseUrl: String, apiKey: String): String {
        val wsBase = when {
            baseUrl.startsWith("https://") -> "wss://" + baseUrl.removePrefix("https://")
            baseUrl.startsWith("http://") -> "ws://" + baseUrl.removePrefix("http://")
            else -> "wss://$baseUrl"
        }
        return "$wsBase/realtime/v1/websocket?apikey=${Uri.encode(apiKey)}&vsn=1.0.0"
    }

    private fun nextRef(): String = refs.incrementAndGet().toString()

    private fun envelope(topic: String, event: String, payload: JSONObject, ref: String): String =
        JSONObject().apply {
            put("topic", topic)
            put("event", event)
            put("payload", payload)
            put("ref", ref)
            if (event == "phx_join") put("join_ref", ref)
        }.toString()

    private fun sendJoin(ws: WebSocket) {
        val mosqueId = joinedMosqueId.orEmpty()
        val changes = JSONArray()
        SYNC_TABLES.forEach { table ->
            changes.put(JSONObject().apply {
                put("event", "*")
                put("schema", "public")
                put("table", table)
                if (mosqueId.isNotBlank()) {
                    // `mosques` is partitioned by its own `id`, all others by `mosque_id`.
                    put("filter", if (table == "mosques") "id=eq.$mosqueId" else "mosque_id=eq.$mosqueId")
                }
            })
        }
        // Realtime applies the same RLS policies as REST, so the channel must
        // join with the signed-in user's Auth JWT (fresh on every reconnect).
        val authToken = appContext?.let { SessionManager(it).getAccessToken() } ?: apiKey
        val payload = JSONObject().apply {
            put("config", JSONObject().apply { put("postgres_changes", changes) })
            put("access_token", authToken)
        }
        ws.send(envelope(TOPIC, "phx_join", payload, "1"))
    }

    private fun startHeartbeat(ws: WebSocket) {
        heartbeatJob?.cancel()
        heartbeatJob = scope?.launch(Dispatchers.IO) {
            while (isActive && running) {
                delay(HEARTBEAT_MS)
                if (!isActive || !running) break
                ws.send(envelope("phoenix", "heartbeat", JSONObject(), nextRef()))
            }
        }
    }

    private fun scheduleReconnect() {
        if (!running) return
        heartbeatJob?.cancel()
        joined = false
        val backoff = minOf(MAX_BACKOFF_MS, 1_000L shl minOf(reconnectAttempt, 5))
        reconnectAttempt++
        Log.d(TAG, "Realtime disconnected — reconnecting in ${backoff}ms")
        reconnectJob?.cancel()
        reconnectJob = scope?.launch(Dispatchers.IO) {
            delay(backoff)
            if (running) connect()
        }
    }

    private fun handleMessage(text: String) {
        val msg = try {
            JSONObject(text)
        } catch (_: Exception) {
            return
        }
        when (msg.optString("event")) {
            "phx_reply" -> {
                val payload = msg.optJSONObject("payload") ?: return
                if (payload.optString("status") == "ok" && msg.optString("topic") == TOPIC) {
                    joined = true
                    reconnectAttempt = 0
                    Log.d(TAG, "Realtime channel joined — live updates active")
                }
            }
            "postgres_changes" -> {
                val data = msg.optJSONObject("payload")?.optJSONObject("data") ?: return
                val table = data.optString("table")
                if (table.isBlank()) return
                if (!belongsToActiveMosque(table, data)) return
                Log.d(TAG, "Live change: ${data.optString("type")} on $table")
                onDatabaseChange?.invoke(table)
            }
        }
    }

    private fun belongsToActiveMosque(table: String, data: JSONObject): Boolean {
        val ctx = appContext ?: return true
        val activeId = SessionManager(ctx).getActiveMosqueId()
        if (activeId.isBlank()) return true // performSync() handles the "no mosque" case.
        val record = data.optJSONObject("record")
            ?: data.optJSONObject("old_record")
            ?: return true
        val recordMosqueId = if (table == "mosques") record.optString("id") else record.optString("mosque_id")
        return recordMosqueId.isBlank() || recordMosqueId == activeId
    }

    private class SocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            if (!running) {
                webSocket.cancel()
                return
            }
            Log.d(TAG, "Realtime socket opened")
            sendJoin(webSocket)
            startHeartbeat(webSocket)
        }

        override fun onMessage(webSocket: WebSocket, text: String) = handleMessage(text)

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1000, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            scheduleReconnect()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.w(TAG, "Realtime socket failure: ${t.message}")
            scheduleReconnect()
        }
    }
}
