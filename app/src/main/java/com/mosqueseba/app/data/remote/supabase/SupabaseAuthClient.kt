package com.mosqueseba.app.data.remote.supabase

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "SupabaseAuth"

/** A real Supabase Auth session, returned by signup / password login / refresh. */
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val uid: String,
    val email: String
)

/**
 * Thin client for Supabase Auth (GoTrue). Supabase Auth is email/password based;
 * the app keeps its existing UI (president = email, member = mobile) by deriving
 * a deterministic synthetic email for mobile-only members.
 */
class SupabaseAuthClient(private val context: Context) {

    private fun baseUrl(): String = SupabaseConfig.getCredentials(context).first.trimEnd('/')
    private fun anonKey(): String = SupabaseConfig.getCredentials(context).second.trim()

    suspend fun signUp(email: String, password: String): Result<AuthSession> = withContext(Dispatchers.IO) {
        request(
            path = "/auth/v1/signup",
            body = JSONObject().apply {
                put("email", email.trim().lowercase())
                put("password", password)
            }.toString(),
            bearer = anonKey()
        )
    }

    suspend fun signIn(email: String, password: String): Result<AuthSession> = withContext(Dispatchers.IO) {
        request(
            path = "/auth/v1/token?grant_type=password",
            body = JSONObject().apply {
                put("email", email.trim().lowercase())
                put("password", password)
            }.toString(),
            bearer = anonKey()
        )
    }

    suspend fun refresh(refreshToken: String): Result<AuthSession> = withContext(Dispatchers.IO) {
        request(
            path = "/auth/v1/token?grant_type=refresh_token",
            body = JSONObject().apply { put("refresh_token", refreshToken) }.toString(),
            bearer = anonKey()
        )
    }

    /** Changes the password of the currently authenticated Auth user. */
    suspend fun updatePassword(accessToken: String, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = URL("${baseUrl()}/auth/v1/user")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                connectTimeout = 15000
                readTimeout = 15000
                doOutput = true
                setRequestProperty("apikey", anonKey())
                setRequestProperty("Authorization", "Bearer $accessToken")
                setRequestProperty("Content-Type", "application/json")
            }
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(JSONObject().put("password", newPassword).toString())
                writer.flush()
            }
            val code = conn.responseCode
            if (code in 200..299) {
                Result.success(Unit)
            } else {
                val error = conn.errorStream?.let { BufferedReader(InputStreamReader(it)).readText() } ?: "HTTP $code"
                Result.failure(Exception(friendlyError(code, error)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "updatePassword failed", e)
            Result.failure(e)
        }
    }

    private fun request(path: String, body: String, bearer: String): Result<AuthSession> {
        val base = baseUrl()
        if (base.isBlank() || anonKey().isBlank()) {
            return Result.failure(Exception("Supabase কনফিগার করা নেই।"))
        }
        return try {
            val url = URL("$base$path")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 15000
                doOutput = true
                setRequestProperty("apikey", anonKey())
                setRequestProperty("Authorization", "Bearer $bearer")
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(body)
                writer.flush()
            }
            val code = conn.responseCode
            val raw = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.let { BufferedReader(InputStreamReader(it)).readText() }.orEmpty()
            if (code in 200..299) {
                val json = JSONObject(raw)
                val session = parseSession(json)
                if (session.accessToken.isBlank() || session.uid.isBlank()) {
                    Result.failure(Exception("ইমেইল ভেরিফিকেশন সম্পন্ন হয়নি। অনুগ্রহ করে আবার চেষ্টা করুন।"))
                } else {
                    Result.success(session)
                }
            } else {
                Result.failure(Exception(friendlyError(code, raw)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Auth request $path failed", e)
            Result.failure(e)
        }
    }

    private fun parseSession(json: JSONObject): AuthSession {
        val user = json.optJSONObject("user") ?: JSONObject()
        return AuthSession(
            accessToken = json.optString("access_token"),
            refreshToken = json.optString("refresh_token"),
            expiresIn = json.optLong("expires_in", 3600L),
            uid = user.optString("id")
                .ifBlank { user.optString("sub") }
                .ifBlank { json.optString("id") },
            email = user.optString("email").ifBlank { json.optString("email") }
        )
    }

    private fun friendlyError(code: Int, raw: String): String {
        val msg = try {
            val j = JSONObject(raw)
            j.optString("msg").ifBlank { j.optString("message") }.ifBlank { j.optString("error_description") }
        } catch (e: Exception) {
            raw.take(120)
        }
        return when {
            msg.contains("email_address_invalid", true) -> "ইমেইল ঠিকানাটি বৈধ নয়।"
            msg.contains("invalid_credentials", true) || code == 400 -> "ইমেইল বা পাসওয়ার্ড সঠিক নয়।"
            msg.contains("email_not_confirmed", true) -> "ইমেইল ভেরিফিকেশন সম্পন্ন হয়নি।"
            msg.contains("weak_password", true) -> "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে।"
            msg.contains("duplicate", true) || msg.contains("already registered", true) -> "এই ইমেইল দিয়ে ইতিমধ্যে অ্যাকাউন্ট আছে।"
            msg.isNotBlank() -> msg
            else -> "অথেনটিকেশন ব্যর্থ ($code)।"
        }
    }

    companion object {
        /**
         * Supabase Auth needs an email. Members log in with mobile only, so map
         * their immutable member ID to a deterministic, unique email. The same
         * function is used at signup and at login, so no extra lookup is needed.
         */
        fun syntheticEmail(memberId: String): String {
            val local = memberId.filter { it.isLetterOrDigit() }.lowercase().take(32)
            return "m-$local@mosqueseba.app"
        }
    }
}
