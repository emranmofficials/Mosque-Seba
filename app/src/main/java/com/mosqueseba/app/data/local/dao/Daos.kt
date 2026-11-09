package com.mosqueseba.app.data.local.dao

import androidx.room.*
import com.mosqueseba.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MosqueDao {
    @Query("SELECT * FROM mosques LIMIT 1")
    fun getMosque(): Flow<MosqueEntity?>

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

    @Delete
    suspend fun deleteNotice(notice: NoticeEntity): Int
}

@Dao
interface MeetingDao {
    @Query("SELECT * FROM meetings ORDER BY meetingDate ASC")
    fun getAllMeetings(): Flow<List<MeetingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: MeetingEntity): Long
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

    @Update
    suspend fun updateSchedule(schedule: FoodScheduleEntity): Int
}

// --- সমাজকল্যাণ ও যাকাত তহবিল (PRD §26) DAO ---
@Dao
interface WelfareDao {
    @Query("SELECT * FROM welfare_cases ORDER BY id DESC")
    fun getAllCases(): Flow<List<WelfareCaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCase(welfareCase: WelfareCaseEntity): Long
}

// --- মাহফিল ও বিশেষ ইভেন্ট (PRD §25) DAO ---
@Dao
interface EventDao {
    @Query("SELECT * FROM mosque_events ORDER BY id DESC")
    fun getAllEvents(): Flow<List<MosqueEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: MosqueEventEntity): Long
}

