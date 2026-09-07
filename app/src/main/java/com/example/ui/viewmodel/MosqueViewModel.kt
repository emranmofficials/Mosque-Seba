package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MosqueDatabase
import com.example.data.local.entity.*
import com.example.data.model.UserRole
import com.example.data.repository.MosqueRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MosqueViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MosqueRepository

    init {
        val db = MosqueDatabase.getDatabase(application, viewModelScope)
        repository = MosqueRepository(db)
    }

    // Role simulation
    private val _currentUserRole = MutableStateFlow(UserRole.PRESIDENT)
    val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()

    fun setUserRole(role: UserRole) {
        _currentUserRole.value = role
    }

    // Mosque
    val mosque: StateFlow<MosqueEntity?> = repository.mosque
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Members & search
    val allMembers: StateFlow<List<MemberEntity>> = repository.allMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeMemberCount: StateFlow<Int> = repository.activeMemberCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val memberCount: StateFlow<Int> = repository.memberCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _memberSearchQuery = MutableStateFlow("")
    val memberSearchQuery: StateFlow<String> = _memberSearchQuery.asStateFlow()

    private val _selectedRoleFilter = MutableStateFlow<String?>(null)
    val selectedRoleFilter: StateFlow<String?> = _selectedRoleFilter.asStateFlow()

    val filteredMembers: StateFlow<List<MemberEntity>> = combine(
        allMembers,
        _memberSearchQuery,
        _selectedRoleFilter
    ) { list, query, roleFilter ->
        list.filter { member ->
            val matchesQuery = query.isBlank() ||
                    member.nameBn.contains(query, ignoreCase = true) ||
                    member.nameEn.contains(query, ignoreCase = true) ||
                    member.memberCode.contains(query, ignoreCase = true) ||
                    member.mobile.contains(query)
            val matchesRole = roleFilter == null || member.roleCode.equals(roleFilter, ignoreCase = true)
            matchesQuery && matchesRole
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setMemberSearchQuery(query: String) {
        _memberSearchQuery.value = query
    }

    fun setSelectedRoleFilter(role: String?) {
        _selectedRoleFilter.value = role
    }

    fun addMember(
        nameBn: String,
        nameEn: String,
        mobile: String,
        email: String,
        nid: String,
        address: String,
        villageArea: String,
        roleCode: String,
        monthlyChanda: Double
    ) {
        viewModelScope.launch {
            val count = allMembers.value.size + 101
            val newCode = "MEM-$count"
            val member = MemberEntity(
                id = UUID.randomUUID().toString(),
                memberCode = newCode,
                nameBn = nameBn,
                nameEn = nameEn,
                mobile = mobile,
                email = email,
                nid = nid,
                address = address,
                villageArea = villageArea,
                roleCode = roleCode,
                monthlyChanda = monthlyChanda,
                joinDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                status = "active"
            )
            repository.addMember(member)
        }
    }

    // Finance & Transactions
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTransactions: StateFlow<List<TransactionEntity>> = repository.pendingTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalIncome: StateFlow<Double> = repository.totalIncome
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = repository.totalExpense
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val netBalance: StateFlow<Double> = combine(totalIncome, totalExpense) { inc, exp ->
        inc - exp
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addTransaction(
        categoryNameBn: String,
        type: String,
        amount: Double,
        description: String,
        sourceType: String = "manual"
    ) {
        viewModelScope.launch {
            val nextNum = allTransactions.value.size + 1
            val code = "TXN-2024-${String.format(Locale.US, "%03d", nextNum)}"
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val role = _currentUserRole.value

            // Approval policy: President creates auto-approved; others or high amount pending
            val status = if (role == UserRole.PRESIDENT && amount <= 50000) "approved" else "pending"
            val approvedBy = if (status == "approved") "${role.titleBn}" else null

            val txn = TransactionEntity(
                id = UUID.randomUUID().toString(),
                transactionCode = code,
                categoryNameBn = categoryNameBn,
                type = type,
                amount = amount,
                txnDate = dateStr,
                description = description,
                sourceType = sourceType,
                createdBy = role.titleBn,
                approvedBy = approvedBy,
                approvalStatus = status
            )
            repository.addTransaction(txn)
        }
    }

    fun updateTransactionStatus(transaction: TransactionEntity, approved: Boolean) {
        viewModelScope.launch {
            val approver = "${_currentUserRole.value.titleBn} (মো: রফিকুল ইসলাম)"
            val newStatus = if (approved) "approved" else "rejected"
            repository.updateTransactionStatus(transaction, newStatus, approver)
        }
    }

    // Chanda payments
    val allChandaPayments: StateFlow<List<ChandaPaymentEntity>> = repository.allChandaPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalChandaCollected: StateFlow<Double> = repository.totalChandaCollected
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun recordChandaPayment(
        member: MemberEntity,
        month: String,
        amount: Double,
        method: String
    ) {
        viewModelScope.launch {
            val rcpNum = allChandaPayments.value.size + 101
            val rcpCode = "RCP-2024-$rcpNum"
            val qrCode = "VER-MC-DHK-2024-$rcpNum-${UUID.randomUUID().toString().take(6).uppercase()}"
            val paidAtStr = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date())
            val payment = ChandaPaymentEntity(
                id = UUID.randomUUID().toString(),
                memberId = member.id,
                memberNameBn = member.nameBn,
                month = month,
                amountDue = member.monthlyChanda,
                amountPaid = amount,
                method = method,
                status = if (amount >= member.monthlyChanda) "paid" else "partial",
                receiptCode = rcpCode,
                qrVerificationCode = qrCode,
                receivedBy = _currentUserRole.value.titleBn,
                paidAt = paidAtStr
            )
            repository.recordChandaPayment(payment)
        }
    }

    // Staff
    val activeStaff: StateFlow<List<StaffEntity>> = repository.activeStaff
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addStaff(
        nameBn: String,
        roleCode: String,
        designationBn: String,
        salaryAmount: Double,
        mobile: String
    ) {
        viewModelScope.launch {
            val staff = StaffEntity(
                id = UUID.randomUUID().toString(),
                nameBn = nameBn,
                roleCode = roleCode,
                designationBn = designationBn,
                joinDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                salaryAmount = salaryAmount,
                mobile = mobile
            )
            repository.addStaff(staff)
        }
    }

    // Notices
    val allNotices: StateFlow<List<NoticeEntity>> = repository.allNotices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun publishNotice(title: String, content: String, priority: String, isPinned: Boolean) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("bn-BD")).format(Date())
            val notice = NoticeEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                content = content,
                priority = priority,
                isPinned = isPinned,
                publishDate = dateStr,
                createdBy = "${_currentUserRole.value.titleBn}"
            )
            repository.addNotice(notice)
        }
    }

    fun deleteNotice(notice: NoticeEntity) {
        viewModelScope.launch {
            repository.deleteNotice(notice)
        }
    }

    // Meetings
    val allMeetings: StateFlow<List<MeetingEntity>> = repository.allMeetings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun scheduleMeeting(title: String, dateStr: String, location: String, agenda: String) {
        viewModelScope.launch {
            val meeting = MeetingEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                meetingDate = dateStr,
                location = location,
                agenda = agenda,
                status = "upcoming"
            )
            repository.addMeeting(meeting)
        }
    }

    // Audit logs
    val recentAuditLogs: StateFlow<List<AuditLogEntity>> = repository.recentAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
