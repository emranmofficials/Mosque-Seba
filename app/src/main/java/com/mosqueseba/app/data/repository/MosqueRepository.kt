package com.mosqueseba.app.data.repository

import com.mosqueseba.app.data.local.MosqueDatabase
import com.mosqueseba.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class MosqueRepository(private val database: MosqueDatabase) {

    // Mosque
    val mosque: Flow<MosqueEntity?> = database.mosqueDao().getMosque()
    fun getMosqueById(id: String): Flow<MosqueEntity?> = database.mosqueDao().getMosqueById(id)
    suspend fun getMosqueByCommitteeId(committeeId: String): MosqueEntity? = database.mosqueDao().getMosqueByCommitteeId(committeeId)
    suspend fun updateMosque(mosque: MosqueEntity) = database.mosqueDao().insertOrUpdate(mosque)

    suspend fun updateMosqueProfile(mosque: MosqueEntity, actor: String) {
        database.mosqueDao().insertOrUpdate(mosque)
        logAudit("MOSQUE_PROFILE_UPDATED", "মসজিদের প্রোফাইল ও তথ্য হালনাগাদ: ${mosque.nameBn}", actor)
    }

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

    suspend fun getMemberByMobile(mobile: String): MemberEntity? = database.memberDao().getMemberByMobile(mobile)

    suspend fun getMemberByEmail(email: String): MemberEntity? = database.memberDao().getMemberByEmail(email)

    suspend fun getPresident(): MemberEntity? = database.memberDao().getPresident()

    suspend fun registerNewMosqueAndPresident(
        mosque: MosqueEntity,
        president: MemberEntity
    ): Boolean {
        database.mosqueDao().insertOrUpdate(mosque)
        database.memberDao().insertMember(president)
        logAudit("MOSQUE_REGISTERED", "নতুন মসজিদ ও সভাপতি নিবন্ধিত: ${mosque.nameBn} (${president.nameBn})")
        return true
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
            sourceType = "chanda_${payment.method}",
            sourceId = payment.id,
            createdBy = payment.receivedBy,
            approvedBy = payment.receivedBy,
            approvalStatus = "approved"
        )
        database.transactionDao().insertTransaction(txn)
        logAudit("CHANDA_RECORDED", "${payment.memberNameBn}-এর চাঁদা জমা: ৳${payment.amountPaid} (রসিদ: ${payment.receiptCode}) — স্বয়ংক্রিয়ভাবে মোট আয়ে যুক্ত", payment.receivedBy)
    }

    // Staff
    val activeStaff: Flow<List<StaffEntity>> = database.staffDao().getAllActiveStaff()
    suspend fun addStaff(staff: StaffEntity) {
        database.staffDao().insertStaff(staff)
        logAudit("STAFF_ADDED", "নতুন কর্মচারী নিয়োগ: ${staff.nameBn} (${staff.designationBn})")
    }

    suspend fun updateStaff(staff: StaffEntity) {
        database.staffDao().updateStaff(staff)
        logAudit("STAFF_UPDATED", "কর্মচারী তথ্য হালনাগাদ: ${staff.nameBn} (${staff.designationBn})")
    }

    // Notices
    val allNotices: Flow<List<NoticeEntity>> = database.noticeDao().getAllNotices()
    suspend fun addNotice(notice: NoticeEntity) {
        database.noticeDao().insertNotice(notice)
        logAudit("NOTICE_PUBLISHED", "নোটিশ প্রকাশ: ${notice.title}")
    }

    suspend fun updateNotice(
        notice: NoticeEntity,
        auditAction: String = "NOTICE_UPDATED",
        auditDetails: String = "নোটিশ হালনাগাদ: ${notice.title}"
    ) {
        database.noticeDao().updateNotice(notice)
        logAudit(auditAction, auditDetails)
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

    suspend fun updateMeeting(
        meeting: MeetingEntity,
        auditAction: String = "MEETING_UPDATED",
        auditDetails: String = "সভার তথ্য হালনাগাদ: ${meeting.title}"
    ) {
        database.meetingDao().updateMeeting(meeting)
        logAudit(auditAction, auditDetails)
    }

    suspend fun deleteMeeting(meeting: MeetingEntity) {
        database.meetingDao().deleteMeeting(meeting)
        logAudit("MEETING_DELETED", "সভা বাতিল/মুছে ফেলা হয়েছে: ${meeting.title}")
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

    suspend fun disburseStaffSalary(
        payroll: StaffPayrollEntity,
        paidBy: String = "কমিটি কোষাধ্যক্ষ",
        paymentMethod: String = "cash"
    ) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        database.staffPayrollDao().insertPayroll(
            payroll.copy(paymentStatus = "paid", paidDate = dateStr)
        )
        // Post linked Finance EXPENSE automatically (PRD §10)
        val bonusSuffix = if (payroll.bonusAmount > 0.0) {
            " (মূল বেতন: ৳${payroll.salaryAmount.toInt()}, বোনাস/ভাতা: ৳${payroll.bonusAmount.toInt()})"
        } else ""
        val txn = TransactionEntity(
            id = UUID.randomUUID().toString(),
            transactionCode = "TXN-PAY-${UUID.randomUUID().toString().take(6).uppercase()}",
            categoryNameBn = "ইমাম ও কর্মচারীদের বেতন",
            type = "EXPENSE",
            amount = payroll.netSalary,
            txnDate = dateStr,
            description = "${payroll.staffNameBn}-এর বেতন পরিশোধ (${payroll.month})$bonusSuffix",
            sourceType = "payroll_$paymentMethod",
            sourceId = payroll.id,
            createdBy = paidBy,
            approvedBy = paidBy,
            approvalStatus = "approved"
        )
        database.transactionDao().insertTransaction(txn)
        logAudit("SALARY_DISBURSED", "${payroll.staffNameBn}-কে বেতন পরিশোধ: ৳${payroll.netSalary} (${payroll.month}) — স্বয়ংক্রিয়ভাবে মোট ব্যয়ে যুক্ত", paidBy)
    }

    val allDutyRosters: Flow<List<DutyRosterEntity>> = database.dutyRosterDao().getAllRosters()
    suspend fun addDutyRoster(roster: DutyRosterEntity) {
        database.dutyRosterDao().insertRoster(roster)
        logAudit("ROSTER_UPDATED", "ডিউটি রোস্টার যুক্ত: ${roster.staffNameBn} (${roster.prayerName})")
    }

    val allFoodSchedules: Flow<List<FoodScheduleEntity>> = database.foodScheduleDao().getAllSchedules()
    suspend fun addFoodSchedule(schedule: FoodScheduleEntity) {
        database.foodScheduleDao().insertSchedule(schedule)
        logAudit("FOOD_SCHEDULED", "খাবার দায়িত্ব বণ্টন: ${schedule.dateStr} • দাতা: ${schedule.providerName} → ${schedule.receiverStaffName}")
    }

    suspend fun addFoodSchedulesBatch(
        schedules: List<FoodScheduleEntity>,
        actor: String,
        summaryNote: String
    ) {
        if (schedules.isEmpty()) return
        database.foodScheduleDao().insertSchedules(schedules)
        logAudit("FOOD_AUTO_SCHEDULED", summaryNote, actor)
    }

    suspend fun updateFoodSchedule(
        schedule: FoodScheduleEntity,
        actor: String,
        auditDetails: String
    ) {
        database.foodScheduleDao().updateSchedule(schedule)
        logAudit("FOOD_SCHEDULE_UPDATED", auditDetails, actor)
    }

    suspend fun deleteFoodSchedule(
        schedule: FoodScheduleEntity,
        actor: String
    ) {
        database.foodScheduleDao().deleteSchedule(schedule)
        logAudit(
            "FOOD_SCHEDULE_DELETED",
            "খাবার শিডিউল অপসারণ: ${schedule.dateStr} (${schedule.mealType}) - ${schedule.providerName}",
            actor
        )
    }

    // Audit
    val recentAuditLogs: Flow<List<AuditLogEntity>> = database.auditDao().getRecentLogs()

    // Welfare & Zakat (PRD §26)
    val allWelfareCases: Flow<List<WelfareCaseEntity>> = database.welfareDao().getAllCases()
    suspend fun addWelfareCase(case: WelfareCaseEntity) {
        database.welfareDao().insertCase(case)
        logAudit("WELFARE_APPLICATION_ADDED", "সমাজকল্যাণ আবেদন যুক্ত: ${case.beneficiaryName} (৳${case.amountRequested.toInt()})")
    }

    suspend fun markWelfareDisbursed(case: WelfareCaseEntity) {
        val updated = case.copy(status = "disbursed")
        database.welfareDao().updateCase(updated)
        logAudit("WELFARE_CASE_DISBURSED", "সমাজকল্যাণ সহায়তা বিতরণ সম্পন্ন: ${case.beneficiaryName} (৳${case.amountApproved.toInt()})")
    }

    // Events & Programs (PRD §25)
    val allEvents: Flow<List<MosqueEventEntity>> = database.eventDao().getAllEvents()
    suspend fun addEvent(event: MosqueEventEntity) {
        database.eventDao().insertEvent(event)
        logAudit("EVENT_PROGRAM_CREATED", "নতুন মসজিদ অনুষ্ঠান: ${event.title} (${event.dateStr})")
    }

    // --- দান বাক্স (ক্যাশবক্স) সাক্ষী ও ব্যাংক জমা ---
    val allCashboxRecords: Flow<List<CashboxRecordEntity>> = database.cashboxDao().getAllRecords()

    suspend fun recordCashboxOpening(record: CashboxRecordEntity, actor: String) {
        database.cashboxDao().insertRecord(record)
        logAudit(
            action = "CASHBOX_OPENED",
            details = "${record.boxType} গণনা: ৳${record.countedAmount.toInt()} (সাক্ষী: ${record.witnessNames})",
            actor = actor
        )
    }

    suspend fun verifyCashboxAsWitness(record: CashboxRecordEntity, witnessName: String) {
        val updatedWitnesses = if (record.witnessNames.contains(witnessName)) {
            record.witnessNames
        } else if (record.witnessNames.isBlank()) {
            witnessName
        } else {
            "${record.witnessNames}, $witnessName"
        }
        val newStatus = if (record.depositStatus == "bank_deposited") "bank_deposited" else "witness_verified"
        database.cashboxDao().updateRecord(
            record.copy(
                witnessNames = updatedWitnesses,
                witnessVerified = true,
                verifiedBy = witnessName,
                depositStatus = newStatus
            )
        )
        logAudit(
            action = "CASHBOX_WITNESS_VERIFIED",
            details = "৳${record.countedAmount.toInt()} দান বাক্স গণনায় সাক্ষ্য নিশ্চিত করেছেন $witnessName",
            actor = witnessName
        )
    }

    suspend fun markCashboxBankDeposited(
        record: CashboxRecordEntity,
        bankName: String,
        slipNo: String,
        actor: String
    ) {
        val alreadyDeposited = record.depositStatus == "bank_deposited"
        database.cashboxDao().updateRecord(
            record.copy(
                bankName = bankName,
                depositSlipNo = slipNo,
                depositStatus = "bank_deposited"
            )
        )
        if (!alreadyDeposited) {
            val txn = TransactionEntity(
                id = UUID.randomUUID().toString(),
                transactionCode = "TXN-BOX-${record.id.takeLast(4).uppercase()}",
                categoryNameBn = record.boxType,
                type = "INCOME",
                amount = record.countedAmount,
                txnDate = record.collectionDate,
                description = "${record.boxType} ব্যাংক জমা (ব্যাংক: $bankName, স্লিপ: $slipNo, সাক্ষী: ${record.witnessNames})",
                sourceType = "cashbox_deposit",
                sourceId = record.id,
                createdBy = actor,
                approvalStatus = "pending"
            )
            database.transactionDao().insertTransaction(txn)
        }
        logAudit(
            action = "CASHBOX_BANK_DEPOSITED",
            details = "৳${record.countedAmount.toInt()} ব্যাংকে জমা সম্পন্ন ($bankName, স্লিপ: $slipNo)",
            actor = actor
        )
    }

    // --- দপ্তর, দলিল-চুক্তি ও চিঠিপত্র ---
    val allOfficeDocuments: Flow<List<OfficeDocumentEntity>> = database.officeDocumentDao().getAllDocuments()

    suspend fun addOfficeDocument(doc: OfficeDocumentEntity, actor: String) {
        database.officeDocumentDao().insertDocument(doc)
        logAudit(
            action = "OFFICE_DOC_RECORDED",
            details = "দপ্তর নথি যুক্ত: ${doc.title} (${doc.category} - ${doc.docCode})",
            actor = actor
        )
    }

    suspend fun updateOfficeDocumentReply(
        doc: OfficeDocumentEntity,
        replyNote: String,
        newStatus: String,
        actor: String
    ) {
        database.officeDocumentDao().updateDocument(
            doc.copy(replyNote = replyNote, status = newStatus)
        )
        logAudit(
            action = "OFFICE_DOC_UPDATED",
            details = "${doc.docCode} নথির অবস্থা হালনাগাদ: $newStatus",
            actor = actor
        )
    }

    // --- শরিয়াহ পর্যালোচনা ও উপদেষ্টা পরামর্শ ---
    val allAdvisoryNotes: Flow<List<AdvisoryNoteEntity>> = database.advisoryNoteDao().getAllNotes()

    suspend fun submitAdvisoryNote(note: AdvisoryNoteEntity) {
        database.advisoryNoteDao().insertNote(note)
        logAudit(
            action = note.noteType,
            details = "${note.topic} — ${note.shariaCompliance}",
            actor = "${note.authorName} (${note.authorRoleTitle})"
        )
    }

    suspend fun updateAdvisoryNoteStatus(note: AdvisoryNoteEntity, newStatus: String, actor: String) {
        database.advisoryNoteDao().updateNote(note.copy(status = newStatus))
        logAudit(
            action = "ADVISORY_STATUS_UPDATED",
            details = "${note.topic} স্ট্যাটাস '$newStatus' হিসেবে চিহ্নিত",
            actor = actor
        )
    }

    // --- প্রচার ও প্রকাশনা / স্মার্ট বোর্ড উক্তি ---
    val allSmartBoardQuotes: Flow<List<SmartBoardQuoteEntity>> = database.smartBoardQuoteDao().getAllQuotes()

    suspend fun addSmartBoardQuote(quote: SmartBoardQuoteEntity) {
        database.smartBoardQuoteDao().insertQuote(quote)
        logAudit(
            action = "SMART_BOARD_UPDATED",
            details = "স্মার্ট বোর্ড বার্তা যুক্ত: ${quote.title} (${quote.category})",
            actor = quote.createdBy
        )
    }

    suspend fun togglePinSmartBoardQuote(quote: SmartBoardQuoteEntity, actor: String) {
        database.smartBoardQuoteDao().updateQuote(quote.copy(isPinned = !quote.isPinned))
        logAudit(
            action = "SMART_BOARD_PINNED",
            details = "${quote.title} পিন অবস্থা পরিবর্তিত",
            actor = actor
        )
    }

    suspend fun deleteSmartBoardQuote(quote: SmartBoardQuoteEntity, actor: String) {
        database.smartBoardQuoteDao().deleteQuote(quote)
        logAudit(
            action = "SMART_BOARD_DELETED",
            details = "স্মার্ট বোর্ড বার্তা অপসারিত: ${quote.title}",
            actor = actor
        )
    }

    // Election & Reconstruction
    val allElections: Flow<List<ElectionScheduleEntity>> = database.electionDao().getAllElections()
    val activeElection: Flow<ElectionScheduleEntity?> = database.electionDao().getActiveElection()
    val allCandidates: Flow<List<ElectionCandidateEntity>> = database.electionDao().getAllCandidates()
    fun getCandidatesForElection(electionId: String): Flow<List<ElectionCandidateEntity>> =
        database.electionDao().getCandidatesForElection(electionId)

    suspend fun createElection(election: ElectionScheduleEntity, actor: String) {
        database.electionDao().insertElection(election)
        logAudit("ELECTION_SCHEDULED", "নতুন নির্বাচনী তফসিল ঘোষণা: ${election.termTitle}", actor)
    }

    suspend fun updateElection(election: ElectionScheduleEntity, actor: String) {
        database.electionDao().updateElection(election)
        logAudit("ELECTION_UPDATED", "নির্বাচনী তথ্য/স্ট্যাটাস হালনাগাদ: ${election.termTitle} (${election.status})", actor)
    }

    suspend fun addCandidate(candidate: ElectionCandidateEntity, actor: String) {
        database.electionDao().insertCandidate(candidate)
        logAudit("CANDIDATE_NOMINATED", "প্রার্থী মনোনয়ন দাখিল: ${candidate.candidateNameBn} (${candidate.targetRoleTitleBn})", actor)
    }

    suspend fun updateCandidate(candidate: ElectionCandidateEntity, actor: String) {
        database.electionDao().updateCandidate(candidate)
        logAudit("CANDIDATE_UPDATED", "প্রার্থীর তথ্য হালনাগাদ: ${candidate.candidateNameBn} (${candidate.status})", actor)
    }

    suspend fun deleteCandidate(candidate: ElectionCandidateEntity, actor: String) {
        database.electionDao().deleteCandidate(candidate)
        logAudit("CANDIDATE_DELETED", "প্রার্থী মনোনয়ন প্রত্যাহার/বাতিল: ${candidate.candidateNameBn}", actor)
    }

    suspend fun activateNewCommittee(
        election: ElectionScheduleEntity,
        actor: String
    ) {
        database.electionDao().updateElection(election.copy(status = "completed"))
        logAudit("COMMITTEE_RECONSTRUCTED", "নতুন নির্বাচিত কমিটি কার্যকর করা হয়েছে: ${election.termTitle}", actor)
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
