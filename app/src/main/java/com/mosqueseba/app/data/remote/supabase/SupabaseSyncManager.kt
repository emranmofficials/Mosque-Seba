package com.mosqueseba.app.data.remote.supabase

import android.content.Context
import android.util.Log
import com.mosqueseba.app.data.local.MosqueDatabase
import com.mosqueseba.app.data.local.SessionManager
import com.mosqueseba.app.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
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
     * Enforces STRICT MULTI-TENANCY: All pulls and pushes are partition-locked by mosque_id.
     * One mosque will NEVER see or alter another mosque's data.
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

        val sessionManager = SessionManager(context)
        val activeMosqueId = sessionManager.getActiveMosqueId().ifBlank { null }
        val localMosque = if (!activeMosqueId.isNullOrBlank()) {
            database.mosqueDao().getMosqueById(activeMosqueId).firstOrNull()
        } else {
            database.mosqueDao().getMosque().firstOrNull()
        }

        // If no mosque is active or registered on this device, do NOT pull any external mosque's records.
        if (localMosque == null) {
            return@withContext SyncResult(
                isSuccess = true,
                messageBn = "কোনো সক্রিয় মসজিদ নির্বাচিত নেই। রেজিস্ট্রেশন বা লগইন সম্পন্ন করুন।"
            )
        }

        val currentMosqueId = localMosque.id

        var syncedMosque = 0
        var syncedMembers = 0
        var syncedTransactions = 0
        var syncedNotices = 0
        var syncedMeetings = 0
        var syncedChanda = 0

        try {
            // 1. Sync Mosque Entity (Strictly for currentMosqueId)
            val cloudMosqueRes = client.get("mosques", "id=eq.$currentMosqueId&limit=1")
            if (cloudMosqueRes.isSuccess && (cloudMosqueRes.getOrNull()?.length() ?: 0) > 0) {
                val cloudJson = cloudMosqueRes.getOrNull()!!.getJSONObject(0)
                val cloudLogo = if (cloudJson.has("logo_url") && !cloudJson.isNull("logo_url")) cloudJson.optString("logo_url") else null
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
            } else {
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

            // 2. Sync Members (Scoped to currentMosqueId)
            val cloudMembersRes = client.get("members", "mosque_id=eq.$currentMosqueId")
            if (cloudMembersRes.isSuccess) {
                val cArray = cloudMembersRes.getOrNull() ?: JSONArray()
                for (i in 0 until cArray.length()) {
                    val mJson = cArray.getJSONObject(i)
                    val mId = mJson.optString("id")
                    if (mId.isNotBlank()) {
                        val member = MemberEntity(
                            id = mId,
                            memberCode = mJson.optString("member_code", "MEM-001"),
                            nameBn = mJson.optString("name_bn", ""),
                            nameEn = mJson.optString("name_en", ""),
                            mobile = mJson.optString("mobile", ""),
                            email = mJson.optString("email", ""),
                            nid = mJson.optString("nid", ""),
                            address = mJson.optString("address", ""),
                            villageArea = mJson.optString("village_area", ""),
                            roleCode = mJson.optString("role_code", "general_member"),
                            monthlyChanda = mJson.optDouble("monthly_chanda", 0.0),
                            joinDate = mJson.optString("join_date", ""),
                            photoUrl = if (mJson.has("photo_url") && !mJson.isNull("photo_url")) mJson.optString("photo_url") else null,
                            status = mJson.optString("status", "active"),
                            willingToProvideStaffMeals = mJson.optBoolean("willing_to_provide_staff_meals", false)
                        )
                        database.memberDao().insertMember(member)
                    }
                }
            }

            val localMembers = database.memberDao().getAllMembers().first()
            if (localMembers.isNotEmpty()) {
                val membersArray = JSONArray()
                localMembers.forEach { m ->
                    membersArray.put(JSONObject().apply {
                        put("id", m.id)
                        put("mosque_id", currentMosqueId)
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

            // 3. Sync Transactions (Scoped to currentMosqueId)
            val cloudTxnsRes = client.get("transactions", "mosque_id=eq.$currentMosqueId")
            if (cloudTxnsRes.isSuccess) {
                val cTxns = cloudTxnsRes.getOrNull() ?: JSONArray()
                for (i in 0 until cTxns.length()) {
                    val tJson = cTxns.getJSONObject(i)
                    val tId = tJson.optString("id")
                    if (tId.isNotBlank()) {
                        val txn = TransactionEntity(
                            id = tId,
                            transactionCode = tJson.optString("transaction_code", "TX-001"),
                            categoryNameBn = tJson.optString("category_name_bn", "সাধারণ দান"),
                            type = tJson.optString("type", "INCOME"),
                            amount = tJson.optDouble("amount", 0.0),
                            txnDate = tJson.optString("txn_date", ""),
                            description = tJson.optString("description", ""),
                            sourceType = tJson.optString("source_type", "manual"),
                            createdBy = tJson.optString("created_by", "ক্যাশিয়ার"),
                            approvalStatus = tJson.optString("approval_status", "approved"),
                            isReversal = tJson.optBoolean("is_reversal", false)
                        )
                        database.transactionDao().insertTransaction(txn)
                    }
                }
            }

            val localTxns = database.transactionDao().getAllTransactions().first()
            if (localTxns.isNotEmpty()) {
                val txnsArray = JSONArray()
                localTxns.forEach { t ->
                    txnsArray.put(JSONObject().apply {
                        put("id", t.id)
                        put("mosque_id", currentMosqueId)
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

            // 4. Sync Notices & By-Laws (Scoped to currentMosqueId)
            val cloudNoticesRes = client.get("notices", "mosque_id=eq.$currentMosqueId")
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
                        put("mosque_id", currentMosqueId)
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

            // 5. Sync Meetings (Scoped to currentMosqueId)
            try {
                val cloudMeetingsRes = client.get("meetings", "mosque_id=eq.$currentMosqueId")
                if (cloudMeetingsRes.isSuccess) {
                    val mArr = cloudMeetingsRes.getOrNull() ?: JSONArray()
                    for (i in 0 until mArr.length()) {
                        val mtJson = mArr.getJSONObject(i)
                        val mId = mtJson.optString("id")
                        if (mId.isNotBlank()) {
                            val meeting = MeetingEntity(
                                id = mId,
                                title = mtJson.optString("title", ""),
                                meetingDate = mtJson.optString("meeting_date", ""),
                                location = mtJson.optString("location", ""),
                                agenda = mtJson.optString("agenda", ""),
                                status = mtJson.optString("status", "upcoming"),
                                resolution = mtJson.optString("resolution", "")
                            )
                            database.meetingDao().insertMeeting(meeting)
                        }
                    }
                }

                val localMeetings = database.meetingDao().getAllMeetings().first()
                if (localMeetings.isNotEmpty()) {
                    val meetingsArray = JSONArray()
                    localMeetings.forEach { mt ->
                        meetingsArray.put(JSONObject().apply {
                            put("id", mt.id)
                            put("mosque_id", currentMosqueId)
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
            } catch (e: Exception) {
                Log.w(TAG, "Meetings sync skipped", e)
            }

            // 6. Sync Chanda Payments (Scoped to currentMosqueId)
            try {
                val cloudChandaRes = client.get("chanda_payments", "mosque_id=eq.$currentMosqueId")
                if (cloudChandaRes.isSuccess) {
                    val chArr = cloudChandaRes.getOrNull() ?: JSONArray()
                    for (i in 0 until chArr.length()) {
                        val cJson = chArr.getJSONObject(i)
                        val cId = cJson.optString("id")
                        if (cId.isNotBlank()) {
                            val mId = cJson.optString("member_id", "")
                            val localMem = if (mId.isNotBlank()) database.memberDao().getMemberById(mId) else null
                            val payment = ChandaPaymentEntity(
                                id = cId,
                                memberId = mId,
                                memberNameBn = localMem?.nameBn ?: "সদস্য",
                                month = cJson.optString("month", ""),
                                amountDue = cJson.optDouble("amount_due", 0.0),
                                amountPaid = cJson.optDouble("amount_paid", 0.0),
                                method = cJson.optString("method", "cash"),
                                status = cJson.optString("status", "paid"),
                                receiptCode = cJson.optString("receipt_code", ""),
                                qrVerificationCode = cJson.optString("qr_verification_code", ""),
                                receivedBy = cJson.optString("received_by", ""),
                                paidAt = cJson.optString("paid_at", "")
                            )
                            database.chandaDao().insertPayment(payment)
                        }
                    }
                }

                val localChanda = database.chandaDao().getAllPayments().first()
                if (localChanda.isNotEmpty()) {
                    val chandaArray = JSONArray()
                    localChanda.forEach { c ->
                        chandaArray.put(JSONObject().apply {
                            put("id", c.id)
                            put("mosque_id", currentMosqueId)
                            put("member_id", c.memberId)
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
            } catch (e: Exception) {
                Log.w(TAG, "Chanda sync skipped", e)
            }

            // 7. Sync Madrasa Students (Scoped to currentMosqueId)
            try {
                val cloudStudentsRes = client.get("madrasa_students", "mosque_id=eq.$currentMosqueId")
                if (cloudStudentsRes.isSuccess) {
                    val sArr = cloudStudentsRes.getOrNull() ?: JSONArray()
                    for (i in 0 until sArr.length()) {
                        val sJson = sArr.getJSONObject(i)
                        val sId = sJson.optString("id")
                        if (sId.isNotBlank()) {
                            val student = StudentEntity(
                                id = sId,
                                studentCode = sJson.optString("student_code", "STU-001"),
                                nameBn = sJson.optString("name_bn", ""),
                                nameEn = sJson.optString("name_en", ""),
                                className = sJson.optString("class_name", ""),
                                guardianName = sJson.optString("guardian_name", ""),
                                guardianMobile = sJson.optString("guardian_mobile", ""),
                                monthlyFee = sJson.optDouble("monthly_fee", 0.0),
                                joinDate = sJson.optString("join_date", ""),
                                status = sJson.optString("status", "active")
                            )
                            database.studentDao().insertStudent(student)
                        }
                    }
                }

                val localStudents = database.studentDao().getAllActiveStudents().first()
                if (localStudents.isNotEmpty()) {
                    val sArray = JSONArray()
                    localStudents.forEach { s ->
                        sArray.put(JSONObject().apply {
                            put("id", s.id)
                            put("mosque_id", currentMosqueId)
                            put("student_code", s.studentCode)
                            put("name_bn", s.nameBn)
                            put("name_en", s.nameEn)
                            put("class_name", s.className)
                            put("guardian_name", s.guardianName)
                            put("guardian_mobile", s.guardianMobile)
                            put("monthly_fee", s.monthlyFee)
                            put("join_date", s.joinDate)
                            put("status", s.status)
                        })
                    }
                    client.upsert("madrasa_students", sArray.toString())
                }
            } catch (e: Exception) {
                Log.w(TAG, "Madrasa sync skipped", e)
            }

            // 8. Sync Cashbox Records (Scoped to currentMosqueId)
            try {
                val cloudCashboxRes = client.get("cashbox_records", "mosque_id=eq.$currentMosqueId")
                if (cloudCashboxRes.isSuccess) {
                    val cbArr = cloudCashboxRes.getOrNull() ?: JSONArray()
                    for (i in 0 until cbArr.length()) {
                        val cbJson = cbArr.getJSONObject(i)
                        val cbId = cbJson.optString("id")
                        if (cbId.isNotBlank()) {
                            val record = CashboxRecordEntity(
                                id = cbId,
                                collectionDate = cbJson.optString("collection_date", ""),
                                boxType = cbJson.optString("box_type", "জুমার বাক্স"),
                                countedAmount = cbJson.optDouble("counted_amount", 0.0),
                                countedBy = cbJson.optString("counted_by", ""),
                                witnessNames = cbJson.optString("witness_names", ""),
                                witnessVerified = cbJson.optBoolean("witness_verified", false),
                                verifiedBy = if (cbJson.has("verified_by") && !cbJson.isNull("verified_by")) cbJson.optString("verified_by") else null,
                                bankName = cbJson.optString("bank_name", ""),
                                depositSlipNo = cbJson.optString("deposit_slip_no", ""),
                                depositStatus = cbJson.optString("deposit_status", "counted"),
                                notes = cbJson.optString("notes", "")
                            )
                            database.cashboxDao().insertRecord(record)
                        }
                    }
                }

                val localCashbox = database.cashboxDao().getAllRecords().first()
                if (localCashbox.isNotEmpty()) {
                    val cbArray = JSONArray()
                    localCashbox.forEach { cb ->
                        cbArray.put(JSONObject().apply {
                            put("id", cb.id)
                            put("mosque_id", currentMosqueId)
                            put("collection_date", cb.collectionDate)
                            put("box_type", cb.boxType)
                            put("counted_amount", cb.countedAmount)
                            put("counted_by", cb.countedBy)
                            put("witness_names", cb.witnessNames)
                            put("witness_verified", cb.witnessVerified)
                            put("verified_by", cb.verifiedBy ?: JSONObject.NULL)
                            put("bank_name", cb.bankName)
                            put("deposit_slip_no", cb.depositSlipNo)
                            put("deposit_status", cb.depositStatus)
                            put("notes", cb.notes)
                        })
                    }
                    client.upsert("cashbox_records", cbArray.toString())
                }
            } catch (e: Exception) {
                Log.w(TAG, "Cashbox sync skipped", e)
            }

            // 9. Sync Welfare Cases (Scoped to currentMosqueId)
            try {
                val cloudWelfareRes = client.get("welfare_cases", "mosque_id=eq.$currentMosqueId")
                if (cloudWelfareRes.isSuccess) {
                    val wArr = cloudWelfareRes.getOrNull() ?: JSONArray()
                    for (i in 0 until wArr.length()) {
                        val wJson = wArr.getJSONObject(i)
                        val wId = wJson.optString("id")
                        if (wId.isNotBlank()) {
                            val wCase = WelfareCaseEntity(
                                id = wId,
                                beneficiaryName = wJson.optString("beneficiary_name", ""),
                                category = wJson.optString("category", ""),
                                mobile = wJson.optString("mobile", ""),
                                amountRequested = wJson.optDouble("amount_requested", 0.0),
                                amountApproved = wJson.optDouble("amount_approved", 0.0),
                                status = wJson.optString("status", "approved"),
                                applicationDate = wJson.optString("application_date", ""),
                                remarks = wJson.optString("remarks", "")
                            )
                            database.welfareDao().insertCase(wCase)
                        }
                    }
                }

                val localWelfare = database.welfareDao().getAllCases().first()
                if (localWelfare.isNotEmpty()) {
                    val wArray = JSONArray()
                    localWelfare.forEach { w ->
                        wArray.put(JSONObject().apply {
                            put("id", w.id)
                            put("mosque_id", currentMosqueId)
                            put("beneficiary_name", w.beneficiaryName)
                            put("category", w.category)
                            put("mobile", w.mobile)
                            put("amount_requested", w.amountRequested)
                            put("amount_approved", w.amountApproved)
                            put("status", w.status)
                            put("application_date", w.applicationDate)
                            put("remarks", w.remarks)
                        })
                    }
                    client.upsert("welfare_cases", wArray.toString())
                }
            } catch (e: Exception) {
                Log.w(TAG, "Welfare sync skipped", e)
            }

            // 10. Sync Election Schedules & Candidates (Scoped to currentMosqueId)
            try {
                val cloudElectionsRes = client.get("election_schedules", "mosque_id=eq.$currentMosqueId")
                if (cloudElectionsRes.isSuccess) {
                    val elArr = cloudElectionsRes.getOrNull() ?: JSONArray()
                    for (i in 0 until elArr.length()) {
                        val elJson = elArr.getJSONObject(i)
                        val elId = elJson.optString("id")
                        if (elId.isNotBlank()) {
                            val election = ElectionScheduleEntity(
                                id = elId,
                                termTitle = elJson.optString("term_title", ""),
                                termStartYear = elJson.optInt("term_start_year", 2026),
                                termEndYear = elJson.optInt("term_end_year", 2028),
                                chiefElectionCommissioner = elJson.optString("chief_election_commissioner", ""),
                                commissionerMobile = elJson.optString("commissioner_mobile", ""),
                                scheduleDate = elJson.optString("schedule_date", ""),
                                nominationDeadline = elJson.optString("nomination_deadline", ""),
                                votingDate = elJson.optString("voting_date", ""),
                                status = elJson.optString("status", "scheduled"),
                                electionType = elJson.optString("election_type", "regular"),
                                gazetteNotes = elJson.optString("gazette_notes", "")
                            )
                            database.electionDao().insertElection(election)
                        }
                    }
                }

                val localElections = database.electionDao().getAllElections().first()
                if (localElections.isNotEmpty()) {
                    val elArray = JSONArray()
                    localElections.forEach { el ->
                        elArray.put(JSONObject().apply {
                            put("id", el.id)
                            put("mosque_id", currentMosqueId)
                            put("term_title", el.termTitle)
                            put("term_start_year", el.termStartYear)
                            put("term_end_year", el.termEndYear)
                            put("chief_election_commissioner", el.chiefElectionCommissioner)
                            put("commissioner_mobile", el.commissionerMobile)
                            put("schedule_date", el.scheduleDate)
                            put("nomination_deadline", el.nominationDeadline)
                            put("voting_date", el.votingDate)
                            put("status", el.status)
                            put("election_type", el.electionType)
                            put("gazette_notes", el.gazetteNotes)
                        })
                    }
                    client.upsert("election_schedules", elArray.toString())
                }

                val cloudCandidatesRes = client.get("election_candidates", "mosque_id=eq.$currentMosqueId")
                if (cloudCandidatesRes.isSuccess) {
                    val candArr = cloudCandidatesRes.getOrNull() ?: JSONArray()
                    for (i in 0 until candArr.length()) {
                        val cJson = candArr.getJSONObject(i)
                        val cId = cJson.optString("id")
                        if (cId.isNotBlank()) {
                            val candidate = ElectionCandidateEntity(
                                id = cId,
                                electionId = cJson.optString("election_id", ""),
                                targetRoleCode = cJson.optString("target_role_code", ""),
                                targetRoleTitleBn = cJson.optString("target_role_title_bn", ""),
                                candidateNameBn = cJson.optString("candidate_name_bn", ""),
                                candidateMobile = cJson.optString("candidate_mobile", ""),
                                memberCode = cJson.optString("member_code", ""),
                                proposerNameBn = cJson.optString("proposer_name_bn", ""),
                                seconderNameBn = cJson.optString("seconder_name_bn", ""),
                                status = cJson.optString("status", "nominated"),
                                symbolOrPanel = cJson.optString("symbol_or_panel", ""),
                                votesReceived = cJson.optInt("votes_received", 0),
                                isElected = cJson.optBoolean("is_elected", false)
                            )
                            database.electionDao().insertCandidate(candidate)
                        }
                    }
                }

                val localCandidates = database.electionDao().getAllCandidates().first()
                if (localCandidates.isNotEmpty()) {
                    val candArray = JSONArray()
                    localCandidates.forEach { c ->
                        candArray.put(JSONObject().apply {
                            put("id", c.id)
                            put("mosque_id", currentMosqueId)
                            put("election_id", c.electionId)
                            put("target_role_code", c.targetRoleCode)
                            put("target_role_title_bn", c.targetRoleTitleBn)
                            put("candidate_name_bn", c.candidateNameBn)
                            put("candidate_mobile", c.candidateMobile)
                            put("member_code", c.memberCode)
                            put("proposer_name_bn", c.proposerNameBn)
                            put("seconder_name_bn", c.seconderNameBn)
                            put("status", c.status)
                            put("symbol_or_panel", c.symbolOrPanel)
                            put("votes_received", c.votesReceived)
                            put("is_elected", c.isElected)
                        })
                    }
                    client.upsert("election_candidates", candArray.toString())
                }
            } catch (e: Exception) {
                Log.w(TAG, "Election sync skipped", e)
            }

            // 11. Sync Mosque Properties (Scoped to currentMosqueId)
            try {
                val cloudPropsRes = client.get("mosque_properties", "mosque_id=eq.$currentMosqueId")
                if (cloudPropsRes.isSuccess) {
                    val pArr = cloudPropsRes.getOrNull() ?: JSONArray()
                    for (i in 0 until pArr.length()) {
                        val pJson = pArr.getJSONObject(i)
                        val pId = pJson.optString("id")
                        if (pId.isNotBlank()) {
                            val prop = MosquePropertyEntity(
                                id = pId,
                                title = pJson.optString("title", ""),
                                category = pJson.optString("category", ""),
                                areaOrQuantity = pJson.optString("area_or_quantity", ""),
                                locationOrDagNo = pJson.optString("location_or_dag_no", ""),
                                deedOrRegNo = pJson.optString("deed_or_reg_no", ""),
                                estimatedValueOrIncome = pJson.optString("estimated_value_or_income", ""),
                                photoUrl = if (pJson.has("photo_url") && !pJson.isNull("photo_url")) pJson.optString("photo_url") else null,
                                status = pJson.optString("status", "active"),
                                description = pJson.optString("description", ""),
                                recordedDate = pJson.optString("recorded_date", "")
                            )
                            database.mosquePropertyDao().insertProperty(prop)
                        }
                    }
                }

                val localProps = database.mosquePropertyDao().getAllProperties().first()
                if (localProps.isNotEmpty()) {
                    val pArray = JSONArray()
                    localProps.forEach { p ->
                        pArray.put(JSONObject().apply {
                            put("id", p.id)
                            put("mosque_id", currentMosqueId)
                            put("title", p.title)
                            put("category", p.category)
                            put("area_or_quantity", p.areaOrQuantity)
                            put("location_or_dag_no", p.locationOrDagNo)
                            put("deed_or_reg_no", p.deedOrRegNo)
                            put("estimated_value_or_income", p.estimatedValueOrIncome)
                            put("photo_url", p.photoUrl ?: JSONObject.NULL)
                            put("status", p.status)
                            put("description", p.description)
                            put("recorded_date", p.recordedDate)
                        })
                    }
                    client.upsert("mosque_properties", pArray.toString())
                }
            } catch (e: Exception) {
                Log.w(TAG, "Properties sync skipped", e)
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

    /**
     * Restores a mosque and its members when logging in on a new device using a unique Committee ID.
     * Strictly scopes all restored records to this specific mosque only.
     */
    suspend fun fetchAndRestoreMosqueByCommitteeId(
        context: Context,
        database: MosqueDatabase,
        committeeId: String
    ): MosqueEntity? = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured(context) || committeeId.isBlank()) return@withContext null
        val client = SupabaseHttpClient(context)
        val trimmedId = committeeId.trim()
        val res = client.get("mosques", "committee_id=eq.$trimmedId&limit=1")
        if (res.isSuccess && (res.getOrNull()?.length() ?: 0) > 0) {
            val cloudJson = res.getOrNull()!!.getJSONObject(0)
            val cloudLogo = if (cloudJson.has("logo_url") && !cloudJson.isNull("logo_url")) cloudJson.optString("logo_url") else null
            val restoredMosque = MosqueEntity(
                id = cloudJson.optString("id", UUID.randomUUID().toString()),
                committeeId = cloudJson.optString("committee_id", trimmedId),
                nameBn = cloudJson.optString("name_bn", "মসজিদ"),
                nameEn = cloudJson.optString("name_en", "Mosque"),
                address = cloudJson.optString("address", ""),
                establishedYear = cloudJson.optInt("established_year", 2026),
                committeeStartYear = cloudJson.optInt("committee_start_year", 2026),
                committeeEndYear = cloudJson.optInt("committee_end_year", 2028),
                status = cloudJson.optString("status", "active"),
                district = cloudJson.optString("district", ""),
                upazila = cloudJson.optString("upazila", ""),
                waqfRegNo = cloudJson.optString("waqf_reg_no", ""),
                capacity = if (cloudJson.has("capacity") && !cloudJson.isNull("capacity")) cloudJson.optInt("capacity") else null,
                bankAccountInfo = cloudJson.optString("bank_account_info", ""),
                logoUrl = cloudLogo
            )
            database.mosqueDao().insertOrUpdate(restoredMosque)
            val sessionManager = SessionManager(context)
            sessionManager.saveActiveMosqueId(restoredMosque.id)

            // Immediately pull ONLY members belonging to this specific mosque
            val membersRes = client.get("members", "mosque_id=eq.${restoredMosque.id}")
            if (membersRes.isSuccess) {
                val arr = membersRes.getOrNull() ?: JSONArray()
                for (i in 0 until arr.length()) {
                    val mJson = arr.getJSONObject(i)
                    val mId = mJson.optString("id")
                    if (mId.isNotBlank()) {
                        val member = MemberEntity(
                            id = mId,
                            memberCode = mJson.optString("member_code", "MEM-001"),
                            nameBn = mJson.optString("name_bn", ""),
                            nameEn = mJson.optString("name_en", ""),
                            mobile = mJson.optString("mobile", ""),
                            email = mJson.optString("email", ""),
                            nid = mJson.optString("nid", ""),
                            address = mJson.optString("address", ""),
                            villageArea = mJson.optString("village_area", ""),
                            roleCode = mJson.optString("role_code", "general_member"),
                            monthlyChanda = mJson.optDouble("monthly_chanda", 0.0),
                            joinDate = mJson.optString("join_date", ""),
                            photoUrl = if (mJson.has("photo_url") && !mJson.isNull("photo_url")) mJson.optString("photo_url") else null,
                            status = mJson.optString("status", "active"),
                            willingToProvideStaffMeals = mJson.optBoolean("willing_to_provide_staff_meals", false)
                        )
                        database.memberDao().insertMember(member)
                    }
                }
            }
            return@withContext restoredMosque
        }
        return@withContext null
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Securely syncs a president credential hash to cloud profiles so login works on new devices.
     */
    suspend fun syncPresidentCredential(
        context: Context,
        mosqueId: String,
        memberId: String,
        email: String,
        mobile: String,
        nameBn: String,
        plainPass: String
    ) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured(context) || email.isBlank() || plainPass.isBlank()) return@withContext
        try {
            val client = SupabaseHttpClient(context)
            val profileJson = JSONObject().apply {
                put("id", "prof-$memberId")
                put("mosque_id", mosqueId)
                put("member_id", memberId)
                put("name_bn", nameBn)
                put("email", email.trim().lowercase())
                put("mobile", mobile.trim())
                put("password_hash", hashPassword(plainPass.trim()))
                put("status", "active")
            }
            client.upsert("profiles", JSONArray().apply { put(profileJson) }.toString())
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync credential to profiles", e)
        }
    }

    /**
     * Verifies cloud password hash for a president logging in from a new device.
     */
    suspend fun verifyRemotePresidentPassword(
        context: Context,
        mosqueId: String,
        email: String,
        plainPass: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured(context) || email.isBlank() || plainPass.isBlank()) return@withContext false
        try {
            val client = SupabaseHttpClient(context)
            val cleanEmail = email.trim().lowercase()
            val res = client.get("profiles", "mosque_id=eq.$mosqueId&email=eq.$cleanEmail&limit=1")
            if (res.isSuccess && (res.getOrNull()?.length() ?: 0) > 0) {
                val prof = res.getOrNull()!!.getJSONObject(0)
                val cloudHash = prof.optString("password_hash")
                if (cloudHash.isNotBlank()) {
                    val inputHash = hashPassword(plainPass.trim())
                    return@withContext inputHash == cloudHash
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Remote password verification failed", e)
        }
        return@withContext false
    }
}
