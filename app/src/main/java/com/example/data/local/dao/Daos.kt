package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
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
