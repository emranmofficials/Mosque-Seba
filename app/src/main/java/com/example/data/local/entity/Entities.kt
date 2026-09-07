package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mosques")
data class MosqueEntity(
    @PrimaryKey val id: String,
    val committeeId: String,
    val nameBn: String,
    val nameEn: String,
    val address: String,
    val establishedYear: Int,
    val committeeStartYear: Int,
    val committeeEndYear: Int,
    val status: String = "active"
)

@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey val id: String,
    val memberCode: String,
    val nameBn: String,
    val nameEn: String,
    val mobile: String,
    val email: String,
    val nid: String,
    val address: String,
    val villageArea: String,
    val roleCode: String,
    val monthlyChanda: Double,
    val joinDate: String,
    val photoUrl: String? = null,
    val status: String = "active" // active, inactive, archived
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val transactionCode: String,
    val categoryNameBn: String,
    val type: String, // INCOME or EXPENSE
    val amount: Double,
    val txnDate: String,
    val description: String,
    val sourceType: String = "manual", // chanda_payment, fee_payment, manual, payroll
    val sourceId: String? = null,
    val createdBy: String,
    val approvedBy: String? = null,
    val approvalStatus: String = "approved", // pending, approved, rejected
    val isReversal: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chanda_payments")
data class ChandaPaymentEntity(
    @PrimaryKey val id: String,
    val memberId: String,
    val memberNameBn: String,
    val month: String, // e.g. "সেপ্টেম্বর ২০২৪"
    val amountDue: Double,
    val amountPaid: Double,
    val method: String, // cash, mobile_banking, online
    val status: String = "paid", // pending, paid, partial
    val receiptCode: String,
    val qrVerificationCode: String,
    val receivedBy: String,
    val paidAt: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "staff")
data class StaffEntity(
    @PrimaryKey val id: String,
    val nameBn: String,
    val roleCode: String, // imam, muazzin, khadem, other_staff
    val designationBn: String,
    val employmentType: String = "full_time",
    val joinDate: String,
    val salaryAmount: Double,
    val salaryFrequency: String = "monthly",
    val mealRequired: Boolean = true,
    val mobile: String,
    val isActive: Boolean = true
)

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val priority: String = "normal", // normal, important, urgent
    val isPinned: Boolean = false,
    val publishDate: String,
    val expiryDate: String? = null,
    val status: String = "published", // published, draft, archived
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "meetings")
data class MeetingEntity(
    @PrimaryKey val id: String,
    val title: String,
    val meetingDate: String,
    val location: String,
    val agenda: String,
    val status: String = "upcoming"
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val actor: String,
    val details: String,
    val timestamp: String
)
