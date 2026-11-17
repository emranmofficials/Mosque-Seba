package com.mosqueseba.app.data.remote.supabase

import android.content.Context
import android.util.Log
import com.mosqueseba.app.data.local.MosqueDatabase
import com.mosqueseba.app.data.local.entity.*
import com.mosqueseba.app.ui.components.toBengaliDigits
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "SupabaseSync"

data class SyncResult(
    val isSuccess: Boolean,
    val messageBn: String,
    val mosquesCount: Int = 0,
    val membersCount: Int = 0,
    val transactionsCount: Int = 0,
    val noticesCount: Int = 0,
    val meetingsCount: Int = 0,
    val chandaCount: Int = 0,
    val syncedAt: String = ""
)

object SupabaseSyncManager {

    /**
     * Executes bidirectional synchronization between local Room DB and Supabase Cloud.
     */
    suspend fun performSync(context: Context, database: MosqueDatabase): SyncResult = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured(context)) {
            return@withContext SyncResult(
                isSuccess = false,
                messageBn = "Supabase প্রজেক্ট URL বা API Key কনফিগার করা নেই। অনুগ্রহ করে সেটিংস থেকে প্রদান করুন।"
            )
        }

        val client = SupabaseHttpClient(context)
        val testRes = client.testConnection()
        if (testRes.isFailure) {
            val err = testRes.exceptionOrNull()?.message ?: "সার্ভার সংযোগ পাওয়া যায়নি"
            return@withContext SyncResult(
                isSuccess = false,
                messageBn = "ক্লাউড কানেকশন ত্রুটি: $err"
            )
        }

        var syncedMosque = 0
        var syncedMembers = 0
        var syncedTransactions = 0
        var syncedNotices = 0
        var syncedMeetings = 0
        var syncedChanda = 0

        try {
            // 1. Sync Mosque Entity (Bidirectional: Pull cloud updates + Push)
            val cloudMosqueRes = client.get("mosques", "limit=1")
            if (cloudMosqueRes.isSuccess && (cloudMosqueRes.getOrNull()?.length() ?: 0) > 0) {
                val cloudJson = cloudMosqueRes.getOrNull()!!.getJSONObject(0)
                val cloudLogo = if (cloudJson.has("logo_url") && !cloudJson.isNull("logo_url")) cloudJson.optString("logo_url") else null
                val localMosque = database.mosqueDao().getMosque().firstOrNull()
                if (localMosque != null) {
                    val mergedLogo = cloudLogo ?: localMosque.logoUrl
                    val mergedMosque = localMosque.copy(
                        nameBn = cloudJson.optString("name_bn", localMosque.nameBn),
                        nameEn = cloudJson.optString("name_en", localMosque.nameEn),
                        address = cloudJson.optString("address", localMosque.address),
                        logoUrl = mergedLogo
                    )
                    database.mosqueDao().insertOrUpdate(mergedMosque)
                    syncedMosque = 1

                    val mosqueJson = JSONObject().apply {
                        put("id", mergedMosque.id)
                        put("committee_id", mergedMosque.committeeId)
                        put("name_bn", mergedMosque.nameBn)
                        put("name_en", mergedMosque.nameEn)
                        put("address", mergedMosque.address)
                        put("established_year", mergedMosque.establishedYear)
                        put("committee_start_year", mergedMosque.committeeStartYear)
                        put("committee_end_year", mergedMosque.committeeEndYear)
                        put("status", mergedMosque.status)
                        put("logo_url", mergedMosque.logoUrl ?: JSONObject.NULL)
                    }
                    client.upsert("mosques", JSONArray().apply { put(mosqueJson) }.toString())
                }
            } else {
                val localMosque = database.mosqueDao().getMosque().firstOrNull()
                if (localMosque != null) {
                    val mosqueJson = JSONObject().apply {
                        put("id", localMosque.id)
                        put("committee_id", localMosque.committeeId)
                        put("name_bn", localMosque.nameBn)
                        put("name_en", localMosque.nameEn)
                        put("address", localMosque.address)
                        put("established_year", localMosque.establishedYear)
                        put("committee_start_year", localMosque.committeeStartYear)
                        put("committee_end_year", localMosque.committeeEndYear)
                        put("status", localMosque.status)
                        put("logo_url", localMosque.logoUrl ?: JSONObject.NULL)
                    }
                    val upsertRes = client.upsert("mosques", JSONArray().apply { put(mosqueJson) }.toString())
                    if (upsertRes.isSuccess) syncedMosque = 1
                }
            }

            // 2. Sync Members
            val localMembers = database.memberDao().getAllMembers().first()
            if (localMembers.isNotEmpty()) {
                val membersArray = JSONArray()
                localMembers.forEach { m ->
                    membersArray.put(JSONObject().apply {
                        put("id", m.id)
                        put("member_code", m.memberCode)
                        put("name_bn", m.nameBn)
                        put("name_en", m.nameEn)
                        put("mobile", m.mobile)
                        put("email", m.email)
                        put("nid", m.nid)
                        put("address", m.address)
                        put("village_area", m.villageArea)
                        put("role_code", m.roleCode)
                        put("monthly_chanda", m.monthlyChanda)
                        put("join_date", m.joinDate)
                        put("status", m.status)
                        put("willing_to_provide_staff_meals", m.willingToProvideStaffMeals)
                    })
                }
                val mRes = client.upsert("members", membersArray.toString())
                if (mRes.isSuccess) syncedMembers = localMembers.size
            }

            // 3. Sync Transactions (Income & Expense)
            val localTxns = database.transactionDao().getAllTransactions().first()
            if (localTxns.isNotEmpty()) {
                val txnsArray = JSONArray()
                localTxns.forEach { t ->
                    txnsArray.put(JSONObject().apply {
                        put("id", t.id)
                        put("transaction_code", t.transactionCode)
                        put("category_name_bn", t.categoryNameBn)
                        put("type", t.type)
                        put("amount", t.amount)
                        put("txn_date", t.txnDate)
                        put("description", t.description)
                        put("source_type", t.sourceType)
                        put("created_by", t.createdBy)
                        put("approval_status", t.approvalStatus)
                        put("is_reversal", t.isReversal)
                    })
                }
                val tRes = client.upsert("transactions", txnsArray.toString())
                if (tRes.isSuccess) syncedTransactions = localTxns.size
            }

            // 4. Sync Notices & By-Laws (Bidirectional)
            val cloudNoticesRes = client.get("notices")
            if (cloudNoticesRes.isSuccess) {
                val cArray = cloudNoticesRes.getOrNull() ?: JSONArray()
                for (i in 0 until cArray.length()) {
                    val nJson = cArray.getJSONObject(i)
                    val id = nJson.optString("id")
                    if (id.isNotBlank()) {
                        val notice = NoticeEntity(
                            id = id,
                            title = nJson.optString("title", ""),
                            content = nJson.optString("content", ""),
                            priority = nJson.optString("priority", "normal"),
                            isPinned = nJson.optBoolean("is_pinned", false),
                            publishDate = nJson.optString("publish_date", ""),
                            status = nJson.optString("status", "published"),
                            createdBy = nJson.optString("created_by", "Admin")
                        )
                        database.noticeDao().insertNotice(notice)
                    }
                }
            }

            val localNotices = database.noticeDao().getAllNotices().first()
            if (localNotices.isNotEmpty()) {
                val noticesArray = JSONArray()
                localNotices.forEach { n ->
                    noticesArray.put(JSONObject().apply {
                        put("id", n.id)
                        put("title", n.title)
                        put("content", n.content)
                        put("priority", n.priority)
                        put("is_pinned", n.isPinned)
                        put("publish_date", n.publishDate)
                        put("status", n.status)
                        put("created_by", n.createdBy)
                    })
                }
                val nRes = client.upsert("notices", noticesArray.toString())
                if (nRes.isSuccess) syncedNotices = localNotices.size
            }

            // 5. Sync Meetings
            val localMeetings = database.meetingDao().getAllMeetings().first()
            if (localMeetings.isNotEmpty()) {
                val meetingsArray = JSONArray()
                localMeetings.forEach { mt ->
                    meetingsArray.put(JSONObject().apply {
                        put("id", mt.id)
                        put("title", mt.title)
                        put("meeting_date", mt.meetingDate)
                        put("location", mt.location)
                        put("agenda", mt.agenda)
                        put("status", mt.status)
                        put("resolution", mt.resolution)
                    })
                }
                val mtRes = client.upsert("meetings", meetingsArray.toString())
                if (mtRes.isSuccess) syncedMeetings = localMeetings.size
            }

            // 6. Sync Chanda Payments
            val localChanda = database.chandaDao().getAllPayments().first()
            if (localChanda.isNotEmpty()) {
                val chandaArray = JSONArray()
                localChanda.forEach { c ->
                    chandaArray.put(JSONObject().apply {
                        put("id", c.id)
                        put("member_id", c.memberId)
                        put("member_name_bn", c.memberNameBn)
                        put("month", c.month)
                        put("amount_due", c.amountDue)
                        put("amount_paid", c.amountPaid)
                        put("method", c.method)
                        put("status", c.status)
                        put("receipt_code", c.receiptCode)
                        put("qr_verification_code", c.qrVerificationCode)
                        put("received_by", c.receivedBy)
                        put("paid_at", c.paidAt)
                    })
                }
                val cRes = client.upsert("chanda_payments", chandaArray.toString())
                if (cRes.isSuccess) syncedChanda = localChanda.size
            }

            val nowFormatted = SimpleDateFormat("dd MMMM, hh:mm a", Locale("bn", "BD")).format(Date())
            SupabaseConfig.saveLastSyncTime(context, nowFormatted)

            val summaryMsg = "আলহামদুলিল্লাহ, ক্লাউড ডেটাবেজে সফলভাবে সিঙ্ক সম্পন্ন হয়েছে!"
            return@withContext SyncResult(
                isSuccess = true,
                messageBn = summaryMsg,
                mosquesCount = syncedMosque,
                membersCount = syncedMembers,
                transactionsCount = syncedTransactions,
                noticesCount = syncedNotices,
                meetingsCount = syncedMeetings,
                chandaCount = syncedChanda,
                syncedAt = nowFormatted
            )

        } catch (e: Exception) {
            Log.e(TAG, "Sync error", e)
            return@withContext SyncResult(
                isSuccess = false,
                messageBn = "সিঙ্ক করার সময় সমস্যা দেখা দিয়েছে: ${e.localizedMessage ?: e.message}"
            )
        }
    }
}
