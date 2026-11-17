package com.mosqueseba.app.data.remote.supabase

import android.content.Context
import android.util.Log
import com.mosqueseba.app.data.local.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "SupabaseClient"

class SupabaseHttpClient(private val context: Context) {

    private fun getBaseUrl(): String {
        return SupabaseConfig.getCredentials(context).first.trimEnd('/')
    }

    private fun getApiKey(): String {
        return SupabaseConfig.getCredentials(context).second.trim()
    }

    /**
     * Supabase Auth JWT for Row Level Security. Falls back to the anon key only
     * before sign-in (e.g. connection test or anonymous bootstrap RPCs).
     */
    private fun bearer(): String {
        return SessionManager(context.applicationContext).getAccessToken() ?: getApiKey()
    }

    /**
     * Test connection to Supabase Project
     */
    suspend fun testConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        val baseUrl = getBaseUrl()
        val apiKey = getApiKey()

        if (baseUrl.isBlank() || apiKey.isBlank()) {
            return@withContext Result.failure(Exception("Supabase Project URL বা API Key দেওয়া হয়নি।"))
        }

        try {
            // 1. Server reachability + read access to the mosque partition.
            val url = URL("$baseUrl/rest/v1/mosques?select=id&limit=1")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("apikey", apiKey)
                setRequestProperty("Authorization", "Bearer ${bearer()}")
                setRequestProperty("Accept", "application/json")
            }

            val code = conn.responseCode
            if (code !in 200..299) {
                val errorStream = conn.errorStream ?: conn.inputStream
                val errorMsg = BufferedReader(InputStreamReader(errorStream)).readText()
                return@withContext Result.failure(Exception("কানেকশন কোড $code: $errorMsg"))
            }

            // 2. Verify the columns used by the sync engine actually exist.
            val requiredColumns = listOf(
                "mosques" to "id,committee_id,name_bn,address,status",
                "members" to "id,mosque_id,name_bn,role_code,monthly_chanda",
                "transactions" to "id,mosque_id,transaction_code,category_id,type,amount,txn_date",
                "notices" to "id,mosque_id,title,content,publish_at,priority",
                "meetings" to "id,mosque_id,title,meeting_date,agenda",
                "chanda_payments" to "id,mosque_id,member_id,month,amount_paid",
                "madrasa_students" to "id,mosque_id,student_code,name_bn,class_name",
                "cashbox_records" to "id,mosque_id,collection_date,counted_amount,witness_names",
                "welfare_cases" to "id,mosque_id,beneficiary_name,category,status",
                "election_schedules" to "id,mosque_id,term_title,voting_date,status",
                "election_candidates" to "id,mosque_id,election_id,candidate_name_bn,votes_received",
                "mosque_properties" to "id,mosque_id,title,category,status",
                "profiles" to "id,mosque_id,member_id,email,password_hash",
                "staff" to "id,mosque_id,name_bn,mobile,role_code,salary_amount",
                "audit_logs" to "id,mosque_id,action,actor_id,details,timestamp",
                "madrasa_attendance" to "id,mosque_id,student_id,date,status",
                "hifz_progress" to "id,mosque_id,student_id,para_number,page_or_ayah",
                "madrasa_fee_payments" to "id,mosque_id,student_id,month,amount_paid,paid_at",
                "staff_payroll" to "id,mosque_id,staff_id,month,net_salary",
                "duty_roster" to "id,mosque_id,staff_id,day_of_week,prayer_name",
                "food_schedules" to "id,mosque_id,date_str,meal_type,status",
                "mosque_events" to "id,mosque_id,title,date_str,budget,status",
                "office_documents" to "id,mosque_id,doc_code,title,recorded_by",
                "advisory_notes" to "id,mosque_id,note_type,topic,created_date",
                "smart_board_quotes" to "id,mosque_id,category,title,message,created_by"
            )
            for ((table, columns) in requiredColumns) {
                val schemaUrl = URL("$baseUrl/rest/v1/$table?select=$columns&limit=0")
                val schemaConn = (schemaUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10000
                    readTimeout = 10000
                    setRequestProperty("apikey", apiKey)
                    setRequestProperty("Authorization", "Bearer ${bearer()}")
                    setRequestProperty("Accept", "application/json")
                }
                if (schemaConn.responseCode !in 200..299) {
                    val errorStream = schemaConn.errorStream ?: schemaConn.inputStream
                    val errorMsg = BufferedReader(InputStreamReader(errorStream)).readText()
                    return@withContext Result.failure(Exception("$table টেবিলের স্কিমা মিলছে না: $errorMsg"))
                }
            }

            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Connection test failed", e)
            Result.failure(Exception("কানেকশন ব্যর্থ: ${e.localizedMessage ?: e.message}"))
        }
    }

    /**
     * Query table records using PostgREST GET
     */
    suspend fun get(table: String, queryParams: String = "select=*"): Result<JSONArray> = withContext(Dispatchers.IO) {
        val baseUrl = getBaseUrl()
        val apiKey = getApiKey()

        if (baseUrl.isBlank() || apiKey.isBlank()) {
            return@withContext Result.failure(Exception("Supabase কনফিগার করা নেই।"))
        }

        try {
            val urlString = if (queryParams.isNotBlank()) "$baseUrl/rest/v1/$table?$queryParams" else "$baseUrl/rest/v1/$table"
            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12000
                readTimeout = 12000
                setRequestProperty("apikey", apiKey)
                setRequestProperty("Authorization", "Bearer ${bearer()}")
                setRequestProperty("Accept", "application/json")
            }

            val code = conn.responseCode
            if (code in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val responseStr = reader.readText().trim()
                if (responseStr.startsWith("[")) {
                    Result.success(JSONArray(responseStr))
                } else if (responseStr.startsWith("{")) {
                    val arr = JSONArray().apply { put(JSONObject(responseStr)) }
                    Result.success(arr)
                } else {
                    Result.success(JSONArray())
                }
            } else {
                val errorMsg = conn.errorStream?.let { BufferedReader(InputStreamReader(it)).readText() } ?: "HTTP $code"
                Result.failure(Exception("ডেটা আনা ব্যর্থ ($code): $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "GET $table failed", e)
            Result.failure(e)
        }
    }

    /**
     * Upsert records using PostgREST POST with merge-duplicates resolution
     */
    suspend fun upsert(table: String, jsonPayload: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val baseUrl = getBaseUrl()
        val apiKey = getApiKey()

        if (baseUrl.isBlank() || apiKey.isBlank()) {
            return@withContext Result.failure(Exception("Supabase কনফিগার করা নেই।"))
        }

        try {
            val url = URL("$baseUrl/rest/v1/$table")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 15000
                doOutput = true
                setRequestProperty("apikey", apiKey)
                setRequestProperty("Authorization", "Bearer ${bearer()}")
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Prefer", "resolution=merge-duplicates,return=minimal")
            }

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(jsonPayload)
                writer.flush()
            }

            val code = conn.responseCode
            if (code in 200..299) {
                Result.success(true)
            } else {
                val errorMsg = conn.errorStream?.let { BufferedReader(InputStreamReader(it)).readText() } ?: "HTTP $code"
                Result.failure(Exception("আপসার্ট ব্যর্থ ($code): $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Upsert $table failed", e)
            Result.failure(e)
        }
    }

    /**
     * Call PostgreSQL stored procedure via PostgREST RPC
     */
    suspend fun callRpc(
        functionName: String,
        jsonParams: JSONObject = JSONObject(),
        useAnon: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        val baseUrl = getBaseUrl()
        val apiKey = getApiKey()

        if (baseUrl.isBlank() || apiKey.isBlank()) {
            return@withContext Result.failure(Exception("Supabase কনফিগার করা নেই।"))
        }

        try {
            val url = URL("$baseUrl/rest/v1/rpc/$functionName")
            // Anonymous bootstrap lookups intentionally use the anon key; all
            // other RPCs carry the signed-in user's JWT.
            val rpcToken = if (useAnon) apiKey else bearer()
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 15000
                doOutput = true
                setRequestProperty("apikey", apiKey)
                setRequestProperty("Authorization", "Bearer $rpcToken")
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(jsonParams.toString())
                writer.flush()
            }

            val code = conn.responseCode
            if (code in 200..299) {
                val resp = BufferedReader(InputStreamReader(conn.inputStream)).readText()
                Result.success(resp)
            } else {
                val errorMsg = conn.errorStream?.let { BufferedReader(InputStreamReader(it)).readText() } ?: "HTTP $code"
                Result.failure(Exception("RPC কল ব্যর্থ ($code): $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "RPC $functionName failed", e)
            Result.failure(e)
        }
    }
}
