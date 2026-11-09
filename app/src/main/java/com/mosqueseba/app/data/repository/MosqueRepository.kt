package com.mosqueseba.app.data.repository

import com.mosqueseba.app.data.local.MosqueDatabase
import com.mosqueseba.app.data.local.entity.*
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
        val txn = database.transactionDao().getTransactionById(transactionId) ?: return
        updateTransactionStatus(txn, "approved", approverName)
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

    // --- মাদরাসা মডিউল (PRD §16–§24) ---
    val allStudents: Flow<List<StudentEntity>> = database.studentDao().getAllActiveStudents()
    val studentCount: Flow<Int> = database.studentDao().getStudentCount()

    suspend fun addStudent(student: StudentEntity) {
        database.studentDao().insertStudent(student)
        logAudit("STUDENT_ENROLLED", "নতুন শিক্ষার্থী ভর্তি: ${student.nameBn} (${student.studentCode})")
    }

    fun getAttendanceByDate(date: String): Flow<List<MadrasaAttendanceEntity>> =
        database.madrasaAttendanceDao().getAttendanceByDate(date)

    suspend fun recordAttendanceBatch(list: List<MadrasaAttendanceEntity>) {
        database.madrasaAttendanceDao().recordAttendanceBatch(list)
        logAudit("ATTENDANCE_RECORDED", "${list.size} জন শিক্ষার্থীর হাজিরা গ্রহণ করা হয়েছে")
    }

    val allHifzProgress: Flow<List<HifzProgressEntity>> = database.hifzDao().getAllProgress()
    suspend fun addHifzProgress(progress: HifzProgressEntity) {
        database.hifzDao().insertProgress(progress)
        logAudit("HIFZ_EVALUATION", "${progress.studentNameBn}-এর হিফজ পাঠ মূল্যায়ন: পারা ${progress.paraNumber}")
    }

    val allFeePayments: Flow<List<MadrasaFeePaymentEntity>> = database.madrasaFeeDao().getAllFeePayments()
    val totalFeeCollected: Flow<Double?> = database.madrasaFeeDao().getTotalFeeCollected()

    suspend fun recordMadrasaFeePayment(payment: MadrasaFeePaymentEntity) {
        database.madrasaFeeDao().insertFeePayment(payment)
        // Automatically post to Finance as INCOME (PRD §20)
        val txn = TransactionEntity(
            id = UUID.randomUUID().toString(),
            transactionCode = "TXN-FEE-${payment.receiptCode}",
            categoryNameBn = "মাদরাসা মাসিক ফি",
            type = "INCOME",
            amount = payment.amountPaid,
            txnDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            description = "${payment.studentNameBn}-এর মাদরাসা ফি আদায় (${payment.month})",
            sourceType = "fee_payment",
            sourceId = payment.id,
            createdBy = "মাদরাসা হিসাব শাখা",
            approvalStatus = "approved"
        )
        database.transactionDao().insertTransaction(txn)
        logAudit("MADRASA_FEE_RECORDED", "${payment.studentNameBn}-এর মাদরাসা ফি জমা: ৳${payment.amountPaid}")
    }

    // --- স্টাফ পে-রোল, রোস্টার ও ফুড শিডিউল (PRD §10–§12) ---
    val allPayrolls: Flow<List<StaffPayrollEntity>> = database.staffPayrollDao().getAllPayrolls()

    suspend fun disburseStaffSalary(payroll: StaffPayrollEntity) {
        database.staffPayrollDao().insertPayroll(payroll.copy(paymentStatus = "paid", paidDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())))
        // Post linked Finance EXPENSE (PRD §10)
        val txn = TransactionEntity(
            id = UUID.randomUUID().toString(),
            transactionCode = "TXN-PAY-${UUID.randomUUID().toString().take(6).uppercase()}",
            categoryNameBn = "কর্মচারী বেতন ও ভাতা",
            type = "EXPENSE",
            amount = payroll.netSalary,
            txnDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            description = "${payroll.staffNameBn}-এর বেতন পরিশোধ (${payroll.month})",
            sourceType = "payroll",
            sourceId = payroll.id,
            createdBy = "কমিটি কোষাধ্যক্ষ",
            approvalStatus = "approved"
        )
        database.transactionDao().insertTransaction(txn)
        logAudit("SALARY_DISBURSED", "${payroll.staffNameBn}-কে বেতন পরিশোধ: ৳${payroll.netSalary}")
    }

    val allDutyRosters: Flow<List<DutyRosterEntity>> = database.dutyRosterDao().getAllRosters()
    suspend fun addDutyRoster(roster: DutyRosterEntity) {
        database.dutyRosterDao().insertRoster(roster)
        logAudit("ROSTER_UPDATED", "ডিউটি রোস্টার যুক্ত: ${roster.staffNameBn} (${roster.prayerName})")
    }

    val allFoodSchedules: Flow<List<FoodScheduleEntity>> = database.foodScheduleDao().getAllSchedules()
    suspend fun addFoodSchedule(schedule: FoodScheduleEntity) {
        database.foodScheduleDao().insertSchedule(schedule)
        logAudit("FOOD_SCHEDULED", "খাবার দায়িত্ব বণ্টন: ${schedule.providerName} (${schedule.mealType})")
    }

    // Audit
    val recentAuditLogs: Flow<List<AuditLogEntity>> = database.auditDao().getRecentLogs()

    // Welfare & Zakat (PRD §26)
    val allWelfareCases: Flow<List<WelfareCaseEntity>> = database.welfareDao().getAllCases()
    suspend fun addWelfareCase(case: WelfareCaseEntity) {
        database.welfareDao().insertCase(case)
        logAudit("WELFARE_APPLICATION_ADDED", "সমাজকল্যাণ আবেদন যুক্ত: ${case.beneficiaryName} (৳${case.amountRequested.toInt()})")
    }

    // Events & Programs (PRD §25)
    val allEvents: Flow<List<MosqueEventEntity>> = database.eventDao().getAllEvents()
    suspend fun addEvent(event: MosqueEventEntity) {
        database.eventDao().insertEvent(event)
        logAudit("EVENT_PROGRAM_CREATED", "নতুন মসজিদ অনুষ্ঠান: ${event.title} (${event.dateStr})")
    }

    private suspend fun logAudit(action: String, details: String, actor: String = "কমিটি প্রশাসন") {
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
