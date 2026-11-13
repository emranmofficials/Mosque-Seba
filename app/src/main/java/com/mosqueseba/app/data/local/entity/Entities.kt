package com.mosqueseba.app.data.local.entity

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
    val status: String = "active",
    val district: String = "",
    val upazila: String = "",
    val waqfRegNo: String = "",
    val capacity: Int? = null,
    val bankAccountInfo: String = "",
    val logoUrl: String? = null
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
    val status: String = "active", // active, inactive, archived
    val willingToProvideStaffMeals: Boolean = false
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

// --- মাদরাসা ও মক্তব মডিউল (PRD §16–§24) ---
@Entity(tableName = "madrasa_students")
data class StudentEntity(
    @PrimaryKey val id: String,
    val studentCode: String,
    val nameBn: String,
    val nameEn: String = "",
    val className: String,
    val guardianName: String,
    val guardianMobile: String,
    val monthlyFee: Double,
    val joinDate: String,
    val status: String = "active" // active, inactive, archived
)

@Entity(tableName = "madrasa_attendance")
data class MadrasaAttendanceEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val studentNameBn: String,
    val date: String,
    val status: String = "present" // present, absent, late, excused
)

@Entity(tableName = "hifz_progress")
data class HifzProgressEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val studentNameBn: String,
    val date: String,
    val paraNumber: Int,
    val pageOrAyah: String,
    val qualityScore: Int = 5, // 1 to 5
    val mistakeCount: Int = 0,
    val teacherFeedback: String = ""
)

@Entity(tableName = "madrasa_fee_payments")
data class MadrasaFeePaymentEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val studentNameBn: String,
    val month: String,
    val amountPaid: Double,
    val receiptCode: String,
    val paymentMethod: String = "cash",
    val status: String = "verified",
    val paidAt: String
)

// --- স্টাফ পে-রোল ও পরিচালনা মডিউল (PRD §10–§12) ---
@Entity(tableName = "staff_payroll")
data class StaffPayrollEntity(
    @PrimaryKey val id: String,
    val staffId: String,
    val staffNameBn: String,
    val month: String,
    val salaryAmount: Double,
    val bonusAmount: Double = 0.0,
    val netSalary: Double,
    val paymentStatus: String = "pending", // pending, paid
    val paidDate: String? = null
)

@Entity(tableName = "duty_roster")
data class DutyRosterEntity(
    @PrimaryKey val id: String,
    val staffId: String,
    val staffNameBn: String,
    val dayOfWeek: String,
    val prayerName: String, // ফজর, যোহর, আসর, মাগরিব, ইশা
    val roleTitle: String, // ইমামতি, আযান, খুতবা
    val notes: String = ""
)

@Entity(tableName = "food_schedules")
data class FoodScheduleEntity(
    @PrimaryKey val id: String,
    val dateStr: String,
    val mealType: String, // দুপুর, রাত
    val providerName: String,
    val providerMobile: String,
    val receiverStaffName: String,
    val status: String = "scheduled" // scheduled, delivered, cancelled
)

// --- সমাজকল্যাণ ও যাকাত তহবিল (PRD §26) ---
@Entity(tableName = "welfare_cases")
data class WelfareCaseEntity(
    @PrimaryKey val id: String,
    val beneficiaryName: String,
    val category: String, // চিকিৎসা সহায়তা, শিক্ষা সহায়তা, খাদ্য সহায়তা, বিধবা ও এতিম ভাতা
    val mobile: String,
    val amountRequested: Double,
    val amountApproved: Double,
    val status: String = "approved", // pending, approved, disbursed
    val applicationDate: String,
    val remarks: String = ""
)

// --- মাহফিল ও বিশেষ ইভেন্ট (PRD §25) ---
@Entity(tableName = "mosque_events")
data class MosqueEventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val dateStr: String,
    val timeStr: String,
    val speakerOrGuest: String,
    val venue: String,
    val budget: Double,
    val volunteerLead: String,
    val status: String = "upcoming" // upcoming, ongoing, completed
)

// --- দান বাক্স (ক্যাশবক্স) খোলা, সাক্ষী ও ব্যাংক জমা ---
@Entity(tableName = "cashbox_records")
data class CashboxRecordEntity(
    @PrimaryKey val id: String,
    val collectionDate: String,
    val boxType: String, // সাপ্তাহিক জুমার দান বাক্স, মাসিক দান বাক্স
    val countedAmount: Double,
    val countedBy: String,
    val witnessNames: String,
    val witnessVerified: Boolean = false,
    val verifiedBy: String? = null,
    val bankName: String = "",
    val depositSlipNo: String = "",
    val depositStatus: String = "counted", // counted, witness_verified, bank_deposited
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

// --- দপ্তর, দলিল-চুক্তি ও চিঠিপত্র রেজিস্টার ---
@Entity(tableName = "office_documents")
data class OfficeDocumentEntity(
    @PrimaryKey val id: String,
    val docCode: String,
    val title: String,
    val category: String, // দলিল, চুক্তি, আগত চিঠি, প্রেরিত চিঠি, অফিস ফাইল
    val counterparty: String,
    val docDate: String,
    val summary: String,
    val replyNote: String = "",
    val status: String = "সংরক্ষিত", // সংরক্ষিত, জবাব অপেক্ষমান, জবাব প্রেরিত
    val recordedBy: String,
    val createdAt: Long = System.currentTimeMillis()
)

// --- শরিয়াহ পর্যালোচনা ও উপদেষ্টা পরামর্শ লগ ---
@Entity(tableName = "advisory_notes")
data class AdvisoryNoteEntity(
    @PrimaryKey val id: String,
    val noteType: String, // SHARIA_REVIEW, ADVISORY_GUIDANCE
    val topic: String,
    val recommendation: String,
    val authorName: String,
    val authorRoleTitle: String,
    val shariaCompliance: String = "শরিয়াহ সম্মত", // শরিয়াহ সম্মত, সংশোধনী প্রয়োজন, কৌশলগত পরামর্শ
    val status: String = "পর্যালোচনাধীন", // পর্যালোচনাধীন, গৃহীত, বাস্তবায়িত
    val createdDate: String,
    val createdAt: Long = System.currentTimeMillis()
)

// --- প্রচার ও প্রকাশনা / স্মার্ট বোর্ড দেয়াল উক্তি ও নির্দেশনা ---
@Entity(tableName = "smart_board_quotes")
data class SmartBoardQuoteEntity(
    @PrimaryKey val id: String,
    val category: String, // দেয়াল উক্তি, জুমার নির্দেশনা, বিশেষ ধর্মীয় দিবস
    val title: String,
    val message: String,
    val isPinned: Boolean = true,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis()
)


