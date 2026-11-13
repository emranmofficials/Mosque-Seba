package com.mosqueseba.app.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import com.mosqueseba.app.data.local.MosqueDatabase
import com.mosqueseba.app.data.local.SessionManager
import com.mosqueseba.app.data.local.entity.*
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.data.repository.MosqueRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "MosqueViewModel"

sealed interface AiNoticeUiState {
    data object Idle : AiNoticeUiState
    data object Loading : AiNoticeUiState
    data class Success(val title: String, val content: String) : AiNoticeUiState
    data class Error(val message: String) : AiNoticeUiState
}

sealed interface AiSummaryUiState {
    data object Idle : AiSummaryUiState
    data object Loading : AiSummaryUiState
    data class Streaming(val text: String) : AiSummaryUiState
    data class Success(val text: String) : AiSummaryUiState
    data class Error(val message: String) : AiSummaryUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
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

    private val _activeMosqueId = MutableStateFlow(sessionManager.getActiveMosqueId())

    suspend fun loginPresidentWithEmail(
        committeeId: String,
        email: String,
        pass: String,
        rememberMe: Boolean = true
    ): Pair<Boolean, String?> {
        val cleanEmail = email.trim().lowercase()
        val trimmedCommitteeId = committeeId.trim()

        val matchedMosque = if (trimmedCommitteeId.isNotBlank()) {
            repository.getMosqueByCommitteeId(trimmedCommitteeId)
        } else {
            repository.getMosqueById(_activeMosqueId.value).firstOrNull() ?: repository.mosque.firstOrNull()
        }

        if (trimmedCommitteeId.isNotBlank() && matchedMosque == null) {
            return Pair(false, "প্রদত্ত কমিটি আইডি খুঁজে পাওয়া যায়নি।")
        }

        val member = repository.getMemberByEmail(cleanEmail)
            ?: return Pair(false, "এই ইমেইল ঠিকানায় কোনো সভাপতি অ্যাকাউন্ট পাওয়া যায়নি।")

        val role = UserRole.fromCode(member.roleCode)
        if (role != UserRole.PRESIDENT) {
            return Pair(false, "শুধুমাত্র সভাপতি ইমেইল ও পাসওয়ার্ড দিয়ে লগইন করতে পারবেন।")
        }

        val registeredPasswordCheck = sessionManager.verifyRegisteredPresidentPassword(cleanEmail, pass)
        val isPasswordValid = registeredPasswordCheck ?: (pass == "123456")
        if (!isPasswordValid) {
            return Pair(false, "গোপন পাসওয়ার্ড সঠিক নয়।")
        }

        val resolvedMosqueId = matchedMosque?.id
            ?: sessionManager.getRegisteredMosqueIdForEmail(cleanEmail)
            ?: _activeMosqueId.value
        _activeMosqueId.value = resolvedMosqueId
        _currentUserRole.value = role
        _isLoggedIn.value = true
        sessionManager.saveSession(
            role = role,
            memberId = member.id,
            memberName = member.nameBn,
            committeeId = matchedMosque?.committeeId ?: trimmedCommitteeId.ifBlank { "MC-DHK-2024" },
            mobile = member.mobile,
            rememberMe = rememberMe,
            mosqueId = resolvedMosqueId
        )
        return Pair(true, null)
    }

    suspend fun loginMemberWithMobile(
        committeeId: String,
        mobile: String,
        pass: String,
        rememberMe: Boolean = true
    ): Pair<Boolean, String?> {
        val cleanMobile = mobile.replace("-", "").replace(" ", "").trim()
        val trimmedCommitteeId = committeeId.trim()

        val matchedMosque = if (trimmedCommitteeId.isNotBlank()) {
            repository.getMosqueByCommitteeId(trimmedCommitteeId)
        } else {
            repository.getMosqueById(_activeMosqueId.value).firstOrNull() ?: repository.mosque.firstOrNull()
        }

        if (trimmedCommitteeId.isNotBlank() && matchedMosque == null) {
            return Pair(false, "প্রদত্ত কমিটি আইডি খুঁজে পাওয়া যায়নি।")
        }

        val member = repository.getMemberByMobile(cleanMobile)
            ?: return Pair(false, "এই মোবাইল নম্বরে কোনো নিবন্ধিত সদস্য পাওয়া যায়নি।")

        val role = UserRole.fromCode(member.roleCode)
        if (role == UserRole.PRESIDENT) {
            return Pair(false, "সভাপতি মোবাইল দিয়ে নয়, 'সভাপতি লগইন' ট্যাব থেকে ইমেইল ও পাসওয়ার্ড দিয়ে লগইন করুন।")
        }

        val digitsOnly = member.mobile.filter { it.isDigit() }
        val expectedLast6 = digitsOnly.takeLast(6)
        if (digitsOnly.length < 6 || pass.trim() != expectedLast6) {
            return Pair(false, "পাসওয়ার্ড সঠিক নয়। আপনার মোবাইল নম্বরের শেষ ৬টি সংখ্যা দিন।")
        }

        val resolvedMosqueId = matchedMosque?.id
            ?: sessionManager.getRegisteredMosqueIdForMobile(cleanMobile)
            ?: _activeMosqueId.value
        _activeMosqueId.value = resolvedMosqueId
        _currentUserRole.value = role
        _isLoggedIn.value = true
        sessionManager.saveSession(
            role = role,
            memberId = member.id,
            memberName = member.nameBn,
            committeeId = matchedMosque?.committeeId ?: trimmedCommitteeId.ifBlank { "MC-DHK-2024" },
            mobile = member.mobile,
            rememberMe = rememberMe,
            mosqueId = resolvedMosqueId
        )
        return Pair(true, null)
    }

    suspend fun loginWithCredentials(
        committeeId: String,
        mobile: String,
        pass: String,
        rememberMe: Boolean = true
    ): Pair<Boolean, String?> = loginMemberWithMobile(committeeId, mobile, pass, rememberMe)

    fun quickLoginAsRole(role: UserRole, rememberMe: Boolean = true) {
        _currentUserRole.value = role
        _isLoggedIn.value = true
        val matchedMember = allMembers.value.firstOrNull { it.roleCode.equals(role.code, ignoreCase = true) }
        val (fallbackName, fallbackMobile) = when (role) {
            UserRole.PRESIDENT -> "মো: রফিকুল ইসলাম" to "01711-223344"
            UserRole.VICE_PRESIDENT -> "আলহাজ্ব আব্দুল বারী" to "01819-334455"
            UserRole.GENERAL_SECRETARY -> "ইঞ্জি: আব্দুল্লাহ আল মামুন" to "01912-445566"
            UserRole.ASSISTANT_SECRETARY -> "মো: জাকির হোসেন" to "01716-334411"
            UserRole.TREASURER -> "হাজী মো: আবুল কাসেম" to "01715-556677"
            UserRole.ASSISTANT_TREASURER -> "মো: নজরুল ইসলাম" to "01817-445522"
            UserRole.ORGANIZING_SECRETARY -> "মো: আসাদুজ্জামান" to "01611-778899"
            UserRole.PUBLICITY_SECRETARY -> "হাফেজ মাওলানা সাইফুল ইসলাম" to "01918-556633"
            UserRole.OFFICE_SECRETARY -> "মো: তানভীর আহমেদ" to "01619-667744"
            UserRole.WELFARE_EDUCATION_SECRETARY -> "মাওলানা আব্দুর রাজ্জাক" to "01720-778855"
            UserRole.EXECUTIVE_MEMBER -> "ডা: তারিকুল ইসলাম" to "01552-889900"
            UserRole.IMAM -> "মাওলানা মুফতি মিজানুর রহমান" to "01712-998877"
            UserRole.ADVISORY_BOARD -> "আলহাজ্ব শামসুল হক চৌধুরী" to "01711-009988"
            UserRole.MUAZZIN -> "কারী মো: ইসমাইল হোসেন" to "01823-887766"
            UserRole.KHADEM -> "মো: মোস্তফা মিয়া" to "01934-776655"
            UserRole.GENERAL_MEMBER -> "মো: শাহজাহান আলী" to "01723-001122"
        }
        val name = matchedMember?.nameBn ?: fallbackName
        val mobile = matchedMember?.mobile ?: fallbackMobile
        val memberId = matchedMember?.id ?: "mem-${role.code}"
        val activeMosque = mosque.value
        sessionManager.saveSession(
            role = role,
            memberId = memberId,
            memberName = name,
            committeeId = activeMosque?.committeeId ?: "MC-DHK-2024",
            mobile = mobile,
            rememberMe = rememberMe,
            mosqueId = activeMosque?.id ?: _activeMosqueId.value
        )
    }

    suspend fun registerNewMosqueAndPresident(
        presidentNameBn: String,
        presidentNameEn: String,
        presidentMobile: String,
        presidentEmail: String,
        presidentNid: String,
        password: String,
        presidentMonthlyChanda: Double = 0.0,
        willingToProvideStaffMeals: Boolean = false,
        presidentPhotoUri: String? = null,
        mosqueNameBn: String,
        mosqueNameEn: String,
        address: String,
        establishedYear: Int,
        startYear: Int,
        endYear: Int,
        district: String = "",
        upazila: String = "",
        waqfRegNo: String = "",
        capacity: Int? = null,
        bankAccountInfo: String = "",
        mosqueLogoUri: String? = null
    ): Result<String> {
        val committeeId = "MC-REG-${System.currentTimeMillis().toString().takeLast(4)}"
        val mosqueId = "mosque-${UUID.randomUUID().toString().take(8)}"
        val presidentId = "mem-${UUID.randomUUID().toString().take(8)}"

        val mosqueEntity = MosqueEntity(
            id = mosqueId,
            committeeId = committeeId,
            nameBn = mosqueNameBn,
            nameEn = mosqueNameEn.ifBlank { mosqueNameBn },
            address = address,
            establishedYear = establishedYear,
            committeeStartYear = startYear,
            committeeEndYear = endYear,
            status = "active",
            district = district.trim(),
            upazila = upazila.trim(),
            waqfRegNo = waqfRegNo.trim(),
            capacity = capacity,
            bankAccountInfo = bankAccountInfo.trim(),
            logoUrl = mosqueLogoUri
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
            villageArea = upazila.trim().ifBlank { address.substringBefore(",").ifBlank { "সদর" } },
            roleCode = "president",
            monthlyChanda = presidentMonthlyChanda,
            joinDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            photoUrl = presidentPhotoUri,
            status = "active",
            willingToProvideStaffMeals = willingToProvideStaffMeals
        )

        val success = repository.registerNewMosqueAndPresident(mosqueEntity, president)
        return if (success) {
            sessionManager.saveRegisteredCredential(
                mobile = presidentMobile,
                committeeId = committeeId,
                mosqueId = mosqueId,
                password = password
            )
            sessionManager.saveRegisteredPresidentEmailCredential(
                email = presidentEmail,
                committeeId = committeeId,
                mosqueId = mosqueId,
                password = password
            )
            sessionManager.saveActiveMosqueId(mosqueId)
            _activeMosqueId.value = mosqueId
            _currentUserRole.value = UserRole.PRESIDENT
            _isLoggedIn.value = true
            sessionManager.saveSession(
                role = UserRole.PRESIDENT,
                memberId = presidentId,
                memberName = presidentNameBn,
                committeeId = committeeId,
                mobile = presidentMobile,
                rememberMe = true,
                mosqueId = mosqueId
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
    val mosque: StateFlow<MosqueEntity?> = _activeMosqueId
        .flatMapLatest { id ->
            repository.getMosqueById(id).flatMapLatest { byId ->
                if (byId != null) flowOf(byId) else repository.mosque
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Firebase AI Logic (Gemini 3.8 Flash) ---
    private val noticeSchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "A concise, formal Bengali mosque notice title"),
            "content" to Schema.string(description = "Professional Islamic-toned Bengali notice body starting with Bismillah and ending with Mosque Committee signature")
        )
    )

    private val noticeDraftModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = noticeSchema
            }
        )
    }

    private val summaryModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    private val _aiNoticeState = MutableStateFlow<AiNoticeUiState>(AiNoticeUiState.Idle)
    val aiNoticeState: StateFlow<AiNoticeUiState> = _aiNoticeState.asStateFlow()

    private val _aiSummaryState = MutableStateFlow<AiSummaryUiState>(AiSummaryUiState.Idle)
    val aiSummaryState: StateFlow<AiSummaryUiState> = _aiSummaryState.asStateFlow()

    fun clearAiNoticeState() {
        _aiNoticeState.value = AiNoticeUiState.Idle
    }

    fun clearAiSummaryState() {
        _aiSummaryState.value = AiSummaryUiState.Idle
    }

    fun draftNoticeWithAi(instruction: String) {
        val cleanInstruction = instruction.trim()
        if (cleanInstruction.isBlank()) {
            _aiNoticeState.value = AiNoticeUiState.Error("অনুগ্রহ করে নোটিশের সংক্ষিপ্ত নির্দেশনা লিখুন।")
            return
        }
        val activeMosqueName = mosque.value?.nameBn ?: "বায়তুল জান্নাত জামে মসজিদ"
        val activeMosqueAddress = mosque.value?.address ?: "ঢাকা"

        viewModelScope.launch {
            _aiNoticeState.value = AiNoticeUiState.Loading
            try {
                val prompt = """
                    আপনি "$activeMosqueName" ($activeMosqueAddress)-এর দাপ্তরিক নোটিশ সহকারী।
                    নিচের সংক্ষিপ্ত নির্দেশনা থেকে মুসল্লি ও কমিটির জন্য মার্জিত, ইসলামিক ও দাপ্তরিক বাংলায় একটি নোটিশের শিরোনাম (title) এবং বিস্তারিত নোটিশ (content) তৈরি করুন।
                    বিস্তারিত নোটিশ "বিসমিল্লাহির রাহমানির রাহিম" দিয়ে শুরু করে "— মসজিদ পরিচালনা কমিটি, $activeMosqueName" দিয়ে শেষ করবেন।
                    নির্দেশনা: $cleanInstruction
                """.trimIndent()

                val response = withTimeout(60_000L) {
                    noticeDraftModel.generateContent(prompt)
                }
                val jsonString = response.text
                if (jsonString.isNullOrBlank()) {
                    _aiNoticeState.value = AiNoticeUiState.Error("জেমিনি মডেল থেকে কোনো খসড়া পাওয়া যায়নি।")
                    return@launch
                }
                val json = JSONObject(jsonString)
                val draftedTitle = json.getString("title")
                val draftedContent = json.getString("content")
                _aiNoticeState.value = AiNoticeUiState.Success(
                    title = draftedTitle,
                    content = draftedContent
                )
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI notice drafting timed out", e)
                _aiNoticeState.value = AiNoticeUiState.Error("৬০ সেকেন্ডে কোনো উত্তর পাওয়া যায়নি। অনুগ্রহ করে আবার চেষ্টা করুন।")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Firebase AI notice drafting failed", e)
                _aiNoticeState.value = AiNoticeUiState.Error(
                    e.localizedMessage ?: "এআই নোটিশ খসড়া তৈরি ব্যর্থ হয়েছে।"
                )
            }
        }
    }

    fun generateFinanceSummaryWithAi() {
        val activeMosqueName = mosque.value?.nameBn ?: "বায়তুল জান্নাত জামে মসজিদ"
        val inc = totalIncome.value
        val exp = totalExpense.value
        val bal = netBalance.value
        val pendingCount = pendingTransactions.value.size
        val approvedCount = allTransactions.value.count { it.approvalStatus == "approved" }
        val chanda = totalChandaCollected.value

        viewModelScope.launch {
            _aiSummaryState.value = AiSummaryUiState.Loading
            val accumulated = StringBuilder()
            try {
                val prompt = """
                    আপনি "$activeMosqueName"-এর স্বচ্ছ আর্থিক প্রতিবেদনের সহকারী।
                    নিচের বাস্তব হিসাবের উপর ভিত্তি করে ৩-৪টি সংক্ষিপ্ত বুলেট পয়েন্টে বাংলায় একটি পরিচ্ছন্ন ও স্বচ্ছ আর্থিক সারসংক্ষেপ লিখুন (কোনো সংখ্যা বানাবেন না):
                    - মোট অনুমোদিত আয়: ৳${String.format(Locale.US, "%,.0f", inc)}
                    - মোট অনুমোদিত ব্যয়: ৳${String.format(Locale.US, "%,.0f", exp)}
                    - বর্তমান নিট তহবিল ব্যালেন্স: ৳${String.format(Locale.US, "%,.0f", bal)}
                    - মোট সদস্য চাঁদা আদায়: ৳${String.format(Locale.US, "%,.0f", chanda)}
                    - অনুমোদিত লেনদেন: $approvedCount টি
                    - অনুমোদনের অপেক্ষায়: $pendingCount টি
                """.trimIndent()

                withTimeout(60_000L) {
                    summaryModel.generateContentStream(prompt).collect { chunk ->
                        accumulated.append(chunk.text.orEmpty())
                        _aiSummaryState.value = AiSummaryUiState.Streaming(accumulated.toString())
                    }
                }
                if (accumulated.isNotBlank()) {
                    _aiSummaryState.value = AiSummaryUiState.Success(accumulated.toString())
                } else {
                    _aiSummaryState.value = AiSummaryUiState.Error("এআই সারসংক্ষেপ খালি এসেছে।")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI finance summary timed out", e)
                _aiSummaryState.value = AiSummaryUiState.Error("আর্থিক সারসংক্ষেপ তৈরিতে সময় শেষ হয়েছে।")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Firebase AI finance summary failed", e)
                _aiSummaryState.value = AiSummaryUiState.Error(
                    e.localizedMessage ?: "আর্থিক সারসংক্ষেপ তৈরি ব্যর্থ হয়েছে।"
                )
            }
        }
    }

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
        monthlyChanda: Double,
        photoUrl: String? = null,
        willingToProvideStaffMeals: Boolean = false,
        salaryAmount: Double = 0.0,
        staffMealRequired: Boolean = true
    ) {
        viewModelScope.launch {
            val count = allMembers.value.size + 101
            val newCode = "MEM-$count"
            val joinDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
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
                joinDate = joinDateStr,
                photoUrl = photoUrl,
                status = "active",
                willingToProvideStaffMeals = willingToProvideStaffMeals
            )
            repository.addMember(member)

            val isStaffRole = roleCode.equals("imam", ignoreCase = true) ||
                roleCode.equals("muazzin", ignoreCase = true) ||
                roleCode.equals("khadem", ignoreCase = true)
            if (isStaffRole) {
                val roleTitle = UserRole.fromCode(roleCode).titleBn
                val staff = StaffEntity(
                    id = "staff-${UUID.randomUUID().toString().take(8)}",
                    nameBn = nameBn,
                    roleCode = roleCode,
                    designationBn = roleTitle,
                    joinDate = joinDateStr,
                    salaryAmount = if (salaryAmount > 0) salaryAmount else 12000.0,
                    mealRequired = staffMealRequired,
                    mobile = mobile
                )
                repository.addStaff(staff)
            }
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

            // Approval policy: President / Vice President creates auto-approved up to 50k; others pending
            val status = if (role.canApproveFinance() && amount <= 50000) "approved" else "pending"
            val approvedBy = if (status == "approved") role.titleBn else null

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

    val dueChanda: StateFlow<Double> = combine(allMembers, totalChandaCollected) { members, collected ->
        val expectedMonthly = members.filter { it.status == "active" }.sumOf { it.monthlyChanda }
        (expectedMonthly - collected).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

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

    fun getAttendanceByDate(date: String): Flow<List<MadrasaAttendanceEntity>> =
        repository.getAttendanceByDate(date)

    fun recordAttendanceBatch(attendanceList: List<MadrasaAttendanceEntity>) {
        viewModelScope.launch {
            repository.recordAttendanceBatch(attendanceList)
        }
    }

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

    // --- Member Management Actions ---
    fun updateMemberRole(member: MemberEntity, newRole: UserRole) {
        viewModelScope.launch {
            repository.updateMember(member.copy(roleCode = newRole.code))
        }
    }

    fun updateMemberDetails(member: MemberEntity) {
        viewModelScope.launch {
            repository.updateMember(member)
        }
    }

    fun toggleMemberStatus(member: MemberEntity) {
        viewModelScope.launch {
            val nextStatus = if (member.status == "active") "inactive" else "active"
            repository.updateMember(member.copy(status = nextStatus))
        }
    }

    fun deleteMember(member: MemberEntity) {
        viewModelScope.launch {
            repository.deleteMember(member)
        }
    }

    fun updateStaffDetails(staff: StaffEntity) {
        viewModelScope.launch {
            repository.updateStaff(staff)
        }
    }

    // --- দান বাক্স (ক্যাশবক্স) সাক্ষী ও ব্যাংক জমা ---
    val allCashboxRecords: StateFlow<List<CashboxRecordEntity>> = repository.allCashboxRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun recordCashboxOpening(
        boxType: String,
        countedAmount: Double,
        witnessNames: String,
        bankName: String,
        depositSlipNo: String,
        notes: String
    ) {
        viewModelScope.launch {
            val role = _currentUserRole.value
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val hasBankDeposit = bankName.isNotBlank() && depositSlipNo.isNotBlank()
            val record = CashboxRecordEntity(
                id = "cb-${UUID.randomUUID().toString().take(8)}",
                collectionDate = dateStr,
                boxType = boxType,
                countedAmount = countedAmount,
                countedBy = role.titleBn,
                witnessNames = witnessNames.ifBlank { "উপস্থিত কার্যনির্বাহী সদস্যবৃন্দ" },
                witnessVerified = false,
                bankName = bankName,
                depositSlipNo = depositSlipNo,
                depositStatus = if (hasBankDeposit) "bank_deposited" else "counted",
                notes = notes
            )
            repository.recordCashboxOpening(record, role.titleBn)
            if (hasBankDeposit) {
                addTransaction(
                    categoryNameBn = boxType,
                    type = "INCOME",
                    amount = countedAmount,
                    description = "$boxType ব্যাংক জমা ($bankName, স্লিপ: $depositSlipNo, সাক্ষী: $witnessNames)",
                    sourceType = "cashbox_deposit"
                )
            }
        }
    }

    fun verifyCashboxAsWitness(record: CashboxRecordEntity) {
        viewModelScope.launch {
            val role = _currentUserRole.value
            val actorName = allMembers.value.firstOrNull {
                it.roleCode.equals(role.code, ignoreCase = true)
            }?.nameBn ?: role.titleBn
            repository.verifyCashboxAsWitness(record, "$actorName (${role.titleBn})")
        }
    }

    fun markCashboxBankDeposited(record: CashboxRecordEntity, bankName: String, slipNo: String) {
        viewModelScope.launch {
            repository.markCashboxBankDeposited(
                record = record,
                bankName = bankName,
                slipNo = slipNo,
                actor = _currentUserRole.value.titleBn
            )
        }
    }

    // --- দপ্তর, দলিল-চুক্তি ও চিঠিপত্র ---
    val allOfficeDocuments: StateFlow<List<OfficeDocumentEntity>> = repository.allOfficeDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addOfficeDocument(
        title: String,
        category: String,
        counterparty: String,
        summary: String,
        status: String
    ) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val num = allOfficeDocuments.value.size + 1
            val doc = OfficeDocumentEntity(
                id = "doc-${UUID.randomUUID().toString().take(8)}",
                docCode = "DOC-${String.format(Locale.US, "%03d", num)}",
                title = title,
                category = category,
                counterparty = counterparty,
                docDate = dateStr,
                summary = summary,
                status = status,
                recordedBy = _currentUserRole.value.titleBn
            )
            repository.addOfficeDocument(doc, _currentUserRole.value.titleBn)
        }
    }

    fun updateOfficeDocumentReply(doc: OfficeDocumentEntity, replyNote: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateOfficeDocumentReply(doc, replyNote, newStatus, _currentUserRole.value.titleBn)
        }
    }

    // --- শরিয়াহ পর্যালোচনা ও উপদেষ্টা পরামর্শ ---
    val allAdvisoryNotes: StateFlow<List<AdvisoryNoteEntity>> = repository.allAdvisoryNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun submitAdvisoryNote(
        noteType: String,
        topic: String,
        recommendation: String,
        shariaCompliance: String
    ) {
        viewModelScope.launch {
            val role = _currentUserRole.value
            val authorName = allMembers.value.firstOrNull {
                it.roleCode.equals(role.code, ignoreCase = true)
            }?.nameBn ?: role.titleBn
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val note = AdvisoryNoteEntity(
                id = "adv-${UUID.randomUUID().toString().take(8)}",
                noteType = noteType,
                topic = topic,
                recommendation = recommendation,
                authorName = authorName,
                authorRoleTitle = role.titleBn,
                shariaCompliance = shariaCompliance,
                status = "পর্যালোচনাধীন",
                createdDate = dateStr
            )
            repository.submitAdvisoryNote(note)
        }
    }

    fun updateAdvisoryNoteStatus(note: AdvisoryNoteEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateAdvisoryNoteStatus(note, newStatus, _currentUserRole.value.titleBn)
        }
    }

    // --- প্রচার ও প্রকাশনা / স্মার্ট বোর্ড উক্তি ---
    val allSmartBoardQuotes: StateFlow<List<SmartBoardQuoteEntity>> = repository.allSmartBoardQuotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSmartBoardQuote(category: String, title: String, message: String, isPinned: Boolean) {
        viewModelScope.launch {
            val quote = SmartBoardQuoteEntity(
                id = "sbq-${UUID.randomUUID().toString().take(8)}",
                category = category,
                title = title,
                message = message,
                isPinned = isPinned,
                createdBy = _currentUserRole.value.titleBn
            )
            repository.addSmartBoardQuote(quote)
        }
    }

    fun togglePinSmartBoardQuote(quote: SmartBoardQuoteEntity) {
        viewModelScope.launch {
            repository.togglePinSmartBoardQuote(quote, _currentUserRole.value.titleBn)
        }
    }

    fun deleteSmartBoardQuote(quote: SmartBoardQuoteEntity) {
        viewModelScope.launch {
            repository.deleteSmartBoardQuote(quote, _currentUserRole.value.titleBn)
        }
    }
}
