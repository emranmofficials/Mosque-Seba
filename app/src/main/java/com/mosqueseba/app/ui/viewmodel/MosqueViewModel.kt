package com.mosqueseba.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mosqueseba.app.data.local.MosqueDatabase
import com.mosqueseba.app.data.local.entity.*
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.data.repository.MosqueRepository
import com.mosqueseba.app.data.local.SessionManager
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

    val sessionManager = SessionManager(application)

    // Role & Persistent Authentication
    private val _currentUserRole = MutableStateFlow(
        if (sessionManager.isLoggedIn()) sessionManager.getSavedRole() else UserRole.PRESIDENT
    )
    val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(sessionManager.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun setUserRole(role: UserRole) {
        _currentUserRole.value = role
    }

    suspend fun loginWithCredentials(
        committeeId: String,
        mobile: String,
        pass: String,
        rememberMe: Boolean = true
    ): Pair<Boolean, String?> {
        val cleanMobile = mobile.replace("-", "").replace(" ", "").trim()
        val currentMosque = repository.mosque.firstOrNull()
        val validCommitteeId = currentMosque?.committeeId ?: "MC-DHK-2024"

        val member = repository.getMemberByMobile(cleanMobile)
        if (member != null) {
            val role = UserRole.fromCode(member.roleCode)
            _currentUserRole.value = role
            _isLoggedIn.value = true
            sessionManager.saveSession(
                role = role,
                memberId = member.id,
                memberName = member.nameBn,
                committeeId = committeeId.ifBlank { validCommitteeId },
                mobile = member.mobile,
                rememberMe = rememberMe
            )
            return Pair(true, null)
        }

        // Demo credentials fallback by mobile patterns
        val defaultRole = when {
            cleanMobile.contains("71122") -> UserRole.PRESIDENT
            cleanMobile.contains("71555") -> UserRole.TREASURER
            cleanMobile.contains("91244") -> UserRole.GENERAL_SECRETARY
            cleanMobile.contains("61177") -> UserRole.ORGANIZING_SECRETARY
            cleanMobile.contains("81122") -> UserRole.IMAM
            else -> UserRole.GENERAL_MEMBER
        }
        _currentUserRole.value = defaultRole
        _isLoggedIn.value = true
        sessionManager.saveSession(
            role = defaultRole,
            memberId = "mem-${defaultRole.code}",
            memberName = if (defaultRole == UserRole.PRESIDENT) "মো: রফিকুল ইসলাম" else "সম্মানিত ব্যবহারকারী",
            committeeId = committeeId.ifBlank { validCommitteeId },
            mobile = mobile,
            rememberMe = rememberMe
        )
        return Pair(true, null)
    }

    fun quickLoginAsRole(role: UserRole, rememberMe: Boolean = true) {
        _currentUserRole.value = role
        _isLoggedIn.value = true
        val (name, mobile) = when (role) {
            UserRole.PRESIDENT -> "মো: রফিকুল ইসলাম" to "01711-223344"
            UserRole.VICE_PRESIDENT -> "আলহাজ্ব আব্দুল বারী" to "01819-334455"
            UserRole.GENERAL_SECRETARY -> "ইঞ্জি: আব্দুল্লাহ আল মামুন" to "01912-445566"
            UserRole.TREASURER -> "হাজী মো: আবুল কাসেম" to "01715-556677"
            UserRole.ORGANIZING_SECRETARY -> "মো: আসাদুজ্জামান" to "01611-778899"
            UserRole.IMAM -> "মাওলানা হাফেজ ক্বারী মো: নুরুল ইসলাম" to "01811-223344"
            UserRole.MUAZZIN -> "মো: বেলাল হোসেন" to "01922-334455"
            UserRole.KHADEM -> "মো: আব্দুল কুদ্দুস" to "01733-445566"
            else -> "মো: শাহজাহান আলী (মুসুল্লি)" to "01723-001122"
        }
        sessionManager.saveSession(
            role = role,
            memberId = "mem-${role.code}",
            memberName = name,
            committeeId = "MC-DHK-2024",
            mobile = mobile,
            rememberMe = rememberMe
        )
    }

    suspend fun registerNewMosqueAndPresident(
        presidentNameBn: String,
        presidentNameEn: String,
        presidentMobile: String,
        presidentEmail: String,
        presidentNid: String,
        password: String,
        mosqueNameBn: String,
        mosqueNameEn: String,
        address: String,
        establishedYear: Int,
        startYear: Int,
        endYear: Int
    ): Result<String> {
        val committeeId = "MC-REG-${System.currentTimeMillis().toString().takeLast(4)}"
        val mosqueId = "mosque-${UUID.randomUUID().toString().take(8)}"
        val presidentId = "mem-${UUID.randomUUID().toString().take(8)}"

        val mosque = MosqueEntity(
            id = mosqueId,
            committeeId = committeeId,
            nameBn = mosqueNameBn,
            nameEn = mosqueNameEn.ifBlank { mosqueNameBn },
            address = address,
            establishedYear = establishedYear,
            committeeStartYear = startYear,
            committeeEndYear = endYear,
            status = "active"
        )

        val president = MemberEntity(
            id = presidentId,
            memberCode = "MEM-001",
            nameBn = presidentNameBn,
            nameEn = presidentNameEn.ifBlank { presidentNameBn },
            mobile = presidentMobile,
            email = presidentEmail,
            nid = presidentNid,
            address = address,
            villageArea = address.substringBefore(",").ifBlank { "সদর" },
            roleCode = "president",
            monthlyChanda = 2000.0,
            joinDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            status = "active"
        )

        val success = repository.registerNewMosqueAndPresident(mosque, president)
        return if (success) {
            _currentUserRole.value = UserRole.PRESIDENT
            _isLoggedIn.value = true
            sessionManager.saveSession(
                role = UserRole.PRESIDENT,
                memberId = presidentId,
                memberName = presidentNameBn,
                committeeId = committeeId,
                mobile = presidentMobile,
                rememberMe = true
            )
            Result.success(committeeId)
        } else {
            Result.failure(Exception("রেজিস্ট্রেশন সংরক্ষণ করা সম্ভব হয়নি"))
        }
    }

    fun login(committeeId: String, mobile: String, pass: String): Boolean {
        _isLoggedIn.value = true
        return true
    }

    fun logout() {
        sessionManager.clearSession()
        _isLoggedIn.value = false
        _currentUserRole.value = UserRole.GENERAL_MEMBER
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

    // --- মাদরাসা ও মক্তব স্টেট ও অ্যাকশন (PRD §16–§24) ---
    val allStudents: StateFlow<List<StudentEntity>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studentCount: StateFlow<Int> = repository.studentCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addStudent(
        nameBn: String,
        className: String,
        guardianName: String,
        guardianMobile: String,
        monthlyFee: Double
    ) {
        viewModelScope.launch {
            val codeNum = allStudents.value.size + 1
            val code = "STU-2024-${String.format(Locale.US, "%03d", codeNum)}"
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val student = StudentEntity(
                id = UUID.randomUUID().toString(),
                studentCode = code,
                nameBn = nameBn,
                className = className,
                guardianName = guardianName,
                guardianMobile = guardianMobile,
                monthlyFee = monthlyFee,
                joinDate = dateStr
            )
            repository.addStudent(student)
        }
    }

    val allHifzProgress: StateFlow<List<HifzProgressEntity>> = repository.allHifzProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addHifzProgress(
        student: StudentEntity,
        paraNumber: Int,
        pageOrAyah: String,
        qualityScore: Int,
        mistakeCount: Int,
        feedback: String
    ) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val progress = HifzProgressEntity(
                id = UUID.randomUUID().toString(),
                studentId = student.id,
                studentNameBn = student.nameBn,
                date = dateStr,
                paraNumber = paraNumber,
                pageOrAyah = pageOrAyah,
                qualityScore = qualityScore,
                mistakeCount = mistakeCount,
                teacherFeedback = feedback
            )
            repository.addHifzProgress(progress)
        }
    }

    val allFeePayments: StateFlow<List<MadrasaFeePaymentEntity>> = repository.allFeePayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalFeeCollected: StateFlow<Double> = repository.totalFeeCollected
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun recordMadrasaFee(
        student: StudentEntity,
        month: String,
        amount: Double,
        method: String = "cash"
    ) {
        viewModelScope.launch {
            val rcpCode = "FEE-2024-${UUID.randomUUID().toString().take(6).uppercase()}"
            val paidAtStr = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date())
            val fee = MadrasaFeePaymentEntity(
                id = UUID.randomUUID().toString(),
                studentId = student.id,
                studentNameBn = student.nameBn,
                month = month,
                amountPaid = amount,
                receiptCode = rcpCode,
                paymentMethod = method,
                paidAt = paidAtStr
            )
            repository.recordMadrasaFeePayment(fee)
        }
    }

    // --- স্টাফ পে-রোল, রোস্টার ও ফুড শিডিউল (PRD §10–§12) ---
    val allPayrolls: StateFlow<List<StaffPayrollEntity>> = repository.allPayrolls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun disburseSalary(
        staff: StaffEntity,
        month: String,
        bonus: Double = 0.0
    ) {
        viewModelScope.launch {
            val net = staff.salaryAmount + bonus
            val payroll = StaffPayrollEntity(
                id = UUID.randomUUID().toString(),
                staffId = staff.id,
                staffNameBn = staff.nameBn,
                month = month,
                salaryAmount = staff.salaryAmount,
                bonusAmount = bonus,
                netSalary = net
            )
            repository.disburseStaffSalary(payroll)
        }
    }

    val allDutyRosters: StateFlow<List<DutyRosterEntity>> = repository.allDutyRosters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addDutyRoster(
        staff: StaffEntity,
        dayOfWeek: String,
        prayerName: String,
        roleTitle: String,
        notes: String
    ) {
        viewModelScope.launch {
            val roster = DutyRosterEntity(
                id = UUID.randomUUID().toString(),
                staffId = staff.id,
                staffNameBn = staff.nameBn,
                dayOfWeek = dayOfWeek,
                prayerName = prayerName,
                roleTitle = roleTitle,
                notes = notes
            )
            repository.addDutyRoster(roster)
        }
    }

    val allFoodSchedules: StateFlow<List<FoodScheduleEntity>> = repository.allFoodSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addFoodSchedule(
        dateStr: String,
        mealType: String,
        providerName: String,
        providerMobile: String,
        receiverStaffName: String
    ) {
        viewModelScope.launch {
            val food = FoodScheduleEntity(
                id = UUID.randomUUID().toString(),
                dateStr = dateStr,
                mealType = mealType,
                providerName = providerName,
                providerMobile = providerMobile,
                receiverStaffName = receiverStaffName
            )
            repository.addFoodSchedule(food)
        }
    }

    // Welfare & Zakat (PRD §26)
    val allWelfareCases: StateFlow<List<WelfareCaseEntity>> = repository.allWelfareCases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addWelfareCase(
        beneficiaryName: String,
        category: String,
        mobile: String,
        amountRequested: Double,
        remarks: String = ""
    ) {
        viewModelScope.launch {
            val case = WelfareCaseEntity(
                id = UUID.randomUUID().toString(),
                beneficiaryName = beneficiaryName,
                category = category,
                mobile = mobile,
                amountRequested = amountRequested,
                amountApproved = amountRequested,
                status = "approved",
                applicationDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                remarks = remarks
            )
            repository.addWelfareCase(case)
        }
    }

    // Events & Programs (PRD §25)
    val allEvents: StateFlow<List<MosqueEventEntity>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addEvent(
        title: String,
        dateStr: String,
        timeStr: String,
        speakerOrGuest: String,
        venue: String,
        budget: Double,
        volunteerLead: String
    ) {
        viewModelScope.launch {
            val event = MosqueEventEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                dateStr = dateStr,
                timeStr = timeStr,
                speakerOrGuest = speakerOrGuest,
                venue = venue,
                budget = budget,
                volunteerLead = volunteerLead,
                status = "upcoming"
            )
            repository.addEvent(event)
        }
    }
}
