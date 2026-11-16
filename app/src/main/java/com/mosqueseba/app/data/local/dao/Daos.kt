package com.mosqueseba.app.data.local.dao

import androidx.room.*
import com.mosqueseba.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MosqueDao {
    @Query("SELECT * FROM mosques LIMIT 1")
    fun getMosque(): Flow<MosqueEntity?>

    @Query("SELECT * FROM mosques WHERE id = :id LIMIT 1")
    fun getMosqueById(id: String): Flow<MosqueEntity?>

    @Query("SELECT * FROM mosques WHERE committeeId = :committeeId LIMIT 1")
    suspend fun getMosqueByCommitteeId(committeeId: String): MosqueEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(mosque: MosqueEntity): Long
}

@Dao
interface MemberDao {
    @Query("SELECT * FROM members ORDER BY memberCode ASC")
    fun getAllMembers(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE id = :id")
    suspend fun getMemberById(id: String): MemberEntity?

    @Query("SELECT * FROM members WHERE REPLACE(REPLACE(mobile, '-', ''), ' ', '') = REPLACE(REPLACE(:mobile, '-', ''), ' ', '') LIMIT 1")
    suspend fun getMemberByMobile(mobile: String): MemberEntity?

    @Query("SELECT * FROM members WHERE LOWER(TRIM(email)) = LOWER(TRIM(:email)) LIMIT 1")
    suspend fun getMemberByEmail(email: String): MemberEntity?

    @Query("SELECT * FROM members WHERE roleCode = 'president' LIMIT 1")
    suspend fun getPresident(): MemberEntity?

    @Query("SELECT * FROM members WHERE status = 'active' ORDER BY memberCode ASC")
    fun getActiveMembers(): Flow<List<MemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity): Long

    @Update
    suspend fun updateMember(member: MemberEntity): Int

    @Delete
    suspend fun deleteMember(member: MemberEntity): Int

    @Query("SELECT COUNT(*) FROM members")
    fun getMemberCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM members WHERE status = 'active'")
    fun getActiveMemberCount(): Flow<Int>
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE approvalStatus = 'pending' ORDER BY createdAt DESC")
    fun getPendingTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity): Int

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND approvalStatus = 'approved' AND isReversal = 0")
    fun getTotalIncome(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND approvalStatus = 'approved' AND isReversal = 0")
    fun getTotalExpense(): Flow<Double?>
}

@Dao
interface ChandaDao {
    @Query("SELECT * FROM chanda_payments ORDER BY createdAt DESC")
    fun getAllPayments(): Flow<List<ChandaPaymentEntity>>

    @Query("SELECT * FROM chanda_payments WHERE memberId = :memberId ORDER BY createdAt DESC")
    fun getPaymentsForMember(memberId: String): Flow<List<ChandaPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: ChandaPaymentEntity): Long

    @Query("SELECT SUM(amountPaid) FROM chanda_payments")
    fun getTotalChandaCollected(): Flow<Double?>
}

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff WHERE isActive = 1")
    fun getAllActiveStaff(): Flow<List<StaffEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffEntity): Long

    @Update
    suspend fun updateStaff(staff: StaffEntity): Int
}

@Dao
interface NoticeDao {
    @Query("SELECT * FROM notices ORDER BY isPinned DESC, createdAt DESC")
    fun getAllNotices(): Flow<List<NoticeEntity>>

    @Query("SELECT * FROM notices WHERE status = 'published' ORDER BY isPinned DESC, createdAt DESC")
    fun getPublishedNotices(): Flow<List<NoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeEntity): Long

    @Update
    suspend fun updateNotice(notice: NoticeEntity): Int

    @Delete
    suspend fun deleteNotice(notice: NoticeEntity): Int
}

@Dao
interface MeetingDao {
    @Query("SELECT * FROM meetings ORDER BY meetingDate ASC")
    fun getAllMeetings(): Flow<List<MeetingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: MeetingEntity): Long

    @Update
    suspend fun updateMeeting(meeting: MeetingEntity): Int

    @Delete
    suspend fun deleteMeeting(meeting: MeetingEntity): Int
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_logs ORDER BY id DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity): Long
}

// --- মাদরাসা DAOs ---
@Dao
interface StudentDao {
    @Query("SELECT * FROM madrasa_students WHERE status = 'active' ORDER BY studentCode ASC")
    fun getAllActiveStudents(): Flow<List<StudentEntity>>

    @Query("SELECT COUNT(*) FROM madrasa_students WHERE status = 'active'")
    fun getStudentCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long

    @Update
    suspend fun updateStudent(student: StudentEntity): Int
}

@Dao
interface MadrasaAttendanceDao {
    @Query("SELECT * FROM madrasa_attendance WHERE date = :date ORDER BY studentNameBn ASC")
    fun getAttendanceByDate(date: String): Flow<List<MadrasaAttendanceEntity>>

    @Query("SELECT * FROM madrasa_attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: String): Flow<List<MadrasaAttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordAttendance(attendance: MadrasaAttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordAttendanceBatch(list: List<MadrasaAttendanceEntity>)
}

@Dao
interface HifzDao {
    @Query("SELECT * FROM hifz_progress ORDER BY date DESC")
    fun getAllProgress(): Flow<List<HifzProgressEntity>>

    @Query("SELECT * FROM hifz_progress WHERE studentId = :studentId ORDER BY date DESC")
    fun getProgressForStudent(studentId: String): Flow<List<HifzProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: HifzProgressEntity): Long
}

@Dao
interface MadrasaFeeDao {
    @Query("SELECT * FROM madrasa_fee_payments ORDER BY paidAt DESC")
    fun getAllFeePayments(): Flow<List<MadrasaFeePaymentEntity>>

    @Query("SELECT SUM(amountPaid) FROM madrasa_fee_payments")
    fun getTotalFeeCollected(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeePayment(payment: MadrasaFeePaymentEntity): Long
}

// --- স্টাফ পে-রোল, রোস্টার ও ফুড শিডিউল DAOs ---
@Dao
interface StaffPayrollDao {
    @Query("SELECT * FROM staff_payroll ORDER BY month DESC")
    fun getAllPayrolls(): Flow<List<StaffPayrollEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayroll(payroll: StaffPayrollEntity): Long

    @Update
    suspend fun updatePayroll(payroll: StaffPayrollEntity): Int
}

@Dao
interface DutyRosterDao {
    @Query("SELECT * FROM duty_roster ORDER BY dayOfWeek ASC, prayerName ASC")
    fun getAllRosters(): Flow<List<DutyRosterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoster(roster: DutyRosterEntity): Long

    @Delete
    suspend fun deleteRoster(roster: DutyRosterEntity): Int
}

@Dao
interface FoodScheduleDao {
    @Query("SELECT * FROM food_schedules ORDER BY dateStr DESC")
    fun getAllSchedules(): Flow<List<FoodScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: FoodScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<FoodScheduleEntity>)

    @Update
    suspend fun updateSchedule(schedule: FoodScheduleEntity): Int

    @Delete
    suspend fun deleteSchedule(schedule: FoodScheduleEntity): Int
}

// --- সমাজকল্যাণ ও যাকাত তহবিল (PRD §26) DAO ---
@Dao
interface WelfareDao {
    @Query("SELECT * FROM welfare_cases ORDER BY id DESC")
    fun getAllCases(): Flow<List<WelfareCaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCase(welfareCase: WelfareCaseEntity): Long

    @Update
    suspend fun updateCase(welfareCase: WelfareCaseEntity): Int
}

// --- মাহফিল ও বিশেষ ইভেন্ট (PRD §25) DAO ---
@Dao
interface EventDao {
    @Query("SELECT * FROM mosque_events ORDER BY id DESC")
    fun getAllEvents(): Flow<List<MosqueEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: MosqueEventEntity): Long
}

// --- দান বাক্স (ক্যাশবক্স), দপ্তর নথি, উপদেষ্টা/শরিয়াহ ও স্মার্ট বোর্ড DAOs ---
@Dao
interface CashboxDao {
    @Query("SELECT * FROM cashbox_records ORDER BY createdAt DESC")
    fun getAllRecords(): Flow<List<CashboxRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CashboxRecordEntity): Long

    @Update
    suspend fun updateRecord(record: CashboxRecordEntity): Int
}

@Dao
interface OfficeDocumentDao {
    @Query("SELECT * FROM office_documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<OfficeDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: OfficeDocumentEntity): Long

    @Update
    suspend fun updateDocument(doc: OfficeDocumentEntity): Int
}

@Dao
interface AdvisoryNoteDao {
    @Query("SELECT * FROM advisory_notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<AdvisoryNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: AdvisoryNoteEntity): Long

    @Update
    suspend fun updateNote(note: AdvisoryNoteEntity): Int
}

@Dao
interface SmartBoardQuoteDao {
    @Query("SELECT * FROM smart_board_quotes ORDER BY isPinned DESC, createdAt DESC")
    fun getAllQuotes(): Flow<List<SmartBoardQuoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: SmartBoardQuoteEntity): Long

    @Update
    suspend fun updateQuote(quote: SmartBoardQuoteEntity): Int

    @Delete
    suspend fun deleteQuote(quote: SmartBoardQuoteEntity): Int
}

@Dao
interface ElectionDao {
    @Query("SELECT * FROM election_schedules ORDER BY createdAt DESC")
    fun getAllElections(): Flow<List<ElectionScheduleEntity>>

    @Query("SELECT * FROM election_schedules WHERE status != 'completed' ORDER BY createdAt DESC LIMIT 1")
    fun getActiveElection(): Flow<ElectionScheduleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElection(election: ElectionScheduleEntity): Long

    @Update
    suspend fun updateElection(election: ElectionScheduleEntity): Int

    @Delete
    suspend fun deleteElection(election: ElectionScheduleEntity): Int

    @Query("SELECT * FROM election_candidates WHERE electionId = :electionId ORDER BY createdAt ASC")
    fun getCandidatesForElection(electionId: String): Flow<List<ElectionCandidateEntity>>

    @Query("SELECT * FROM election_candidates ORDER BY createdAt ASC")
    fun getAllCandidates(): Flow<List<ElectionCandidateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidate(candidate: ElectionCandidateEntity): Long

    @Update
    suspend fun updateCandidate(candidate: ElectionCandidateEntity): Int

    @Delete
    suspend fun deleteCandidate(candidate: ElectionCandidateEntity): Int
}

@Dao
interface MosquePropertyDao {
    @Query("SELECT * FROM mosque_properties ORDER BY createdAt DESC")
    fun getAllProperties(): Flow<List<MosquePropertyEntity>>

    @Query("SELECT * FROM mosque_properties WHERE id = :id LIMIT 1")
    suspend fun getPropertyById(id: String): MosquePropertyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: MosquePropertyEntity): Long

    @Update
    suspend fun updateProperty(property: MosquePropertyEntity): Int

    @Delete
    suspend fun deleteProperty(property: MosquePropertyEntity): Int
}



