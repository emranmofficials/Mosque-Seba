package com.example.data.repository

import com.example.data.local.MosqueDatabase
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class MosqueRepository(private val database: MosqueDatabase) {

    // Mosque
    val mosque: Flow<MosqueEntity?> = database.mosqueDao().getMosque()
    suspend fun updateMosque(mosque: MosqueEntity) = database.mosqueDao().insertOrUpdate(mosque)

    // Members
    val allMembers: Flow<List<MemberEntity>> = database.memberDao().getAllMembers()
    val activeMembers: Flow<List<MemberEntity>> = database.memberDao().getActiveMembers()
    val memberCount: Flow<Int> = database.memberDao().getMemberCount()
    val activeMemberCount: Flow<Int> = database.memberDao().getActiveMemberCount()

    suspend fun addMember(member: MemberEntity) {
        database.memberDao().insertMember(member)
        logAudit("MEMBER_ADDED", "নতুন সদস্য যোগ: ${member.nameBn} (${member.memberCode})")
    }

    suspend fun updateMember(member: MemberEntity) {
        database.memberDao().updateMember(member)
        logAudit("MEMBER_UPDATED", "সদস্য তথ্য হালনাগাদ: ${member.nameBn} (${member.memberCode})")
    }

    suspend fun deleteMember(member: MemberEntity) {
        database.memberDao().deleteMember(member)
        logAudit("MEMBER_DELETED", "সদস্য অপসারিত: ${member.nameBn}")
    }

    // Transactions
    val allTransactions: Flow<List<TransactionEntity>> = database.transactionDao().getAllTransactions()
    val pendingTransactions: Flow<List<TransactionEntity>> = database.transactionDao().getPendingTransactions()
    val totalIncome: Flow<Double?> = database.transactionDao().getTotalIncome()
    val totalExpense: Flow<Double?> = database.transactionDao().getTotalExpense()

    suspend fun addTransaction(transaction: TransactionEntity) {
        database.transactionDao().insertTransaction(transaction)
        logAudit("TXN_CREATED", "নতুন লেনদেন দাখিল: ${transaction.transactionCode} - ৳${transaction.amount} (${transaction.categoryNameBn})")
    }

    suspend fun approveTransaction(transactionId: String, approverName: String) {
        val all = database.transactionDao().getAllTransactions()
        // Find and update
        // We can do it directly with a query or retrieve & update
        val list = mutableListOf<TransactionEntity>()
        // Let's do updateTransaction
    }

    suspend fun updateTransactionStatus(transaction: TransactionEntity, status: String, approver: String) {
        val updated = transaction.copy(approvalStatus = status, approvedBy = approver)
        database.transactionDao().updateTransaction(updated)
        logAudit("TXN_STATUS", "${transaction.transactionCode} লেনদেন $status করেছেন $approver")
    }

    // Chanda
    val allChandaPayments: Flow<List<ChandaPaymentEntity>> = database.chandaDao().getAllPayments()
    val totalChandaCollected: Flow<Double?> = database.chandaDao().getTotalChandaCollected()

    suspend fun recordChandaPayment(payment: ChandaPaymentEntity) {
        database.chandaDao().insertPayment(payment)
        // Also post into Finance as INCOME automatically (PRD §8 double-post prevention / auto-post)
        val txn = TransactionEntity(
            id = UUID.randomUUID().toString(),
            transactionCode = "TXN-CHD-${payment.receiptCode}",
            categoryNameBn = "মাসিক সদস্য চাঁদা",
            type = "INCOME",
            amount = payment.amountPaid,
            txnDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            description = "${payment.memberNameBn}-এর ${payment.month}-এর চাঁদা আদায় (রসিদ: ${payment.receiptCode})",
            sourceType = "chanda_payment",
            sourceId = payment.id,
            createdBy = payment.receivedBy,
            approvalStatus = "approved"
        )
        database.transactionDao().insertTransaction(txn)
        logAudit("CHANDA_RECORDED", "${payment.memberNameBn}-এর চাঁদা জমা: ৳${payment.amountPaid} (রসিদ: ${payment.receiptCode})")
    }

    // Staff
    val activeStaff: Flow<List<StaffEntity>> = database.staffDao().getAllActiveStaff()
    suspend fun addStaff(staff: StaffEntity) {
        database.staffDao().insertStaff(staff)
        logAudit("STAFF_ADDED", "নতুন কর্মচারী নিয়োগ: ${staff.nameBn} (${staff.designationBn})")
    }

    // Notices
    val allNotices: Flow<List<NoticeEntity>> = database.noticeDao().getAllNotices()
    suspend fun addNotice(notice: NoticeEntity) {
        database.noticeDao().insertNotice(notice)
        logAudit("NOTICE_PUBLISHED", "নোটিশ প্রকাশ: ${notice.title}")
    }

    suspend fun deleteNotice(notice: NoticeEntity) {
        database.noticeDao().deleteNotice(notice)
        logAudit("NOTICE_DELETED", "নোটিশ মুছে ফেলা হয়েছে: ${notice.title}")
    }

    // Meetings
    val allMeetings: Flow<List<MeetingEntity>> = database.meetingDao().getAllMeetings()
    suspend fun addMeeting(meeting: MeetingEntity) {
        database.meetingDao().insertMeeting(meeting)
        logAudit("MEETING_CREATED", "নতুন সভা নির্ধারিত: ${meeting.title}")
    }

    // Audit
    val recentAuditLogs: Flow<List<AuditLogEntity>> = database.auditDao().getRecentLogs()

    private suspend fun logAudit(action: String, details: String, actor: String = "মো: রফিকুল ইসলাম (সভাপতি)") {
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        database.auditDao().insertLog(
            AuditLogEntity(
                action = action,
                actor = actor,
                details = details,
                timestamp = time
            )
        )
    }
}
