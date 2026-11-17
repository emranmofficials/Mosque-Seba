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
import com.mosqueseba.app.data.updater.AppUpdateInfo
import com.mosqueseba.app.data.updater.AppUpdateManager
import com.mosqueseba.app.ui.components.toBengaliDigits
import com.mosqueseba.app.data.remote.supabase.SupabaseConfig
import com.mosqueseba.app.data.remote.supabase.SupabaseHttpClient
import com.mosqueseba.app.data.remote.supabase.SupabaseSyncManager
import com.mosqueseba.app.data.remote.supabase.SyncResult
import com.mosqueseba.app.util.MosquePushNotificationHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import com.mosqueseba.app.data.model.IslamicEvent
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

sealed interface AiEventGuidanceUiState {
    data object Idle : AiEventGuidanceUiState
    data object Loading : AiEventGuidanceUiState
    data class Success(
        val eventId: String,
        val fullGuidanceText: String
    ) : AiEventGuidanceUiState
    data class Error(val message: String) : AiEventGuidanceUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class MosqueViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MosqueRepository

    private val _availableUpdate = MutableStateFlow<AppUpdateInfo?>(null)
    val availableUpdate: StateFlow<AppUpdateInfo?> = _availableUpdate.asStateFlow()

    init {
        val db = MosqueDatabase.getDatabase(application, viewModelScope)
        repository = MosqueRepository(db)
        MosquePushNotificationHelper.initChannels(application)
        checkForAppUpdates(forceManual = false)
    }

    fun checkForAppUpdates(forceManual: Boolean = false, onResult: ((AppUpdateInfo?) -> Unit)? = null) {
        viewModelScope.launch {
            val update = AppUpdateManager.checkLatestRelease(getApplication(), forceCheck = forceManual)
            if (update != null && update.isUpdateAvailable) {
                val isDismissed = !forceManual && AppUpdateManager.isVersionDismissed(getApplication(), update.latestVersionName)
                if (!isDismissed) {
                    _availableUpdate.value = update
                }
            } else if (forceManual) {
                _availableUpdate.value = update
            }
            onResult?.invoke(update)
        }
    }

    fun dismissUpdateDialog(versionName: String) {
        AppUpdateManager.dismissVersion(getApplication(), versionName)
        _availableUpdate.value = null
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

    private val _notificationsEnabled = MutableStateFlow(sessionManager.isNotificationsEnabled())
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _showHijriBanner = MutableStateFlow(sessionManager.isHijriBannerVisible())
    val showHijriBanner: StateFlow<Boolean> = _showHijriBanner.asStateFlow()

    private val _hijriDayOffset = MutableStateFlow(sessionManager.getHijriDayOffset())
    val hijriDayOffset: StateFlow<Int> = _hijriDayOffset.asStateFlow()

    private val _rememberMeEnabled = MutableStateFlow(sessionManager.isRememberMeEnabled())
    val rememberMeEnabled: StateFlow<Boolean> = _rememberMeEnabled.asStateFlow()

    private val _darkModeEnabled = MutableStateFlow(sessionManager.isDarkModeEnabled())
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()

    private val _readNotificationIds = MutableStateFlow(sessionManager.getReadNotificationIds())
    val readNotificationIds: StateFlow<Set<String>> = _readNotificationIds.asStateFlow()

    private fun todayDateIso(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())

    private val _khademCheckedTasks = MutableStateFlow(
        sessionManager.getKhademCheckedTasksForDate(todayDateIso())
    )
    val khademCheckedTasks: StateFlow<Set<String>> = _khademCheckedTasks.asStateFlow()

    fun toggleKhademTask(taskId: String) {
        _khademCheckedTasks.value = sessionManager.toggleKhademTaskForDate(todayDateIso(), taskId)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        sessionManager.setNotificationsEnabled(enabled)
        _notificationsEnabled.value = enabled
    }

    // Supabase Cloud Backend & Sync
    fun performCloudSync(onResult: (SyncResult) -> Unit) {
        viewModelScope.launch {
            val db = MosqueDatabase.getDatabase(getApplication(), viewModelScope)
            val result = SupabaseSyncManager.performSync(getApplication(), db)
            onResult(result)
        }
    }

    suspend fun testCloudConnection(): Result<Boolean> {
        val client = SupabaseHttpClient(getApplication())
        return client.testConnection()
    }

    fun saveSupabaseCredentials(url: String, key: String) {
        SupabaseConfig.saveCredentials(getApplication(), url, key)
    }

    fun getSupabaseCredentials(): Pair<String, String> {
        return SupabaseConfig.getCredentials(getApplication())
    }

    fun isSupabaseConfigured(): Boolean {
        return SupabaseConfig.isConfigured(getApplication())
    }

    fun getLastCloudSyncTime(): String? {
        return SupabaseConfig.getLastSyncTime(getApplication())
    }

    fun setShowHijriBanner(visible: Boolean) {
        sessionManager.setHijriBannerVisible(visible)
        _showHijriBanner.value = visible
    }

    fun setHijriDayOffset(offset: Int) {
        sessionManager.setHijriDayOffset(offset)
        _hijriDayOffset.value = offset
    }

    fun setRememberMeEnabled(enabled: Boolean) {
        sessionManager.setRememberMeEnabled(enabled)
        _rememberMeEnabled.value = enabled
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        sessionManager.setDarkModeEnabled(enabled)
        _darkModeEnabled.value = enabled
    }

    fun markNotificationAsRead(notificationId: String) {
        sessionManager.markNotificationRead(notificationId)
        _readNotificationIds.value = sessionManager.getReadNotificationIds()
    }

    fun markAllNotificationsAsRead(notificationIds: Collection<String>) {
        sessionManager.markAllNotificationsRead(notificationIds)
        _readNotificationIds.value = sessionManager.getReadNotificationIds()
    }

    fun changeCurrentUserPassword(
        currentPassword: String,
        newPassword: String
    ): Pair<Boolean, String> {
        val trimmedCurrent = currentPassword.trim()
        val trimmedNew = newPassword.trim()
        if (trimmedNew.length < 6) {
            return Pair(false, "নতুন পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে।")
        }

        val role = _currentUserRole.value
        val activeMosque = mosque.value
        val committeeId = activeMosque?.committeeId ?: sessionManager.getSavedCommitteeId()
        val mosqueId = activeMosque?.id ?: _activeMosqueId.value

        val member = allMembers.value.firstOrNull {
            it.id == sessionManager.getSavedMemberId() && role == sessionManager.getSavedRole()
        } ?: allMembers.value.firstOrNull { it.roleCode.equals(role.code, ignoreCase = true) }

        val email = member?.email?.trim()?.lowercase().orEmpty()
        val mobile = member?.mobile?.ifBlank { sessionManager.getSavedMobile() } ?: sessionManager.getSavedMobile()

        return if (role == UserRole.PRESIDENT && email.isNotBlank()) {
            val savedCheck = sessionManager.verifyRegisteredPresidentPassword(email, trimmedCurrent)
            val isValid = savedCheck ?: (trimmedCurrent == "123456")
            if (!isValid) {
                Pair(false, "বর্তমান পাসওয়ার্ড সঠিক নয়।")
            } else {
                sessionManager.saveRegisteredPresidentEmailCredential(
                    email = email,
                    committeeId = committeeId,
                    mosqueId = mosqueId,
                    password = trimmedNew
                )
                Pair(true, "পাসওয়ার্ড সফলভাবে পরিবর্তন করা হয়েছে।")
            }
        } else {
            val cleanMobile = mobile.replace("-", "").replace(" ", "").trim()
            val digitsOnly = cleanMobile.filter { it.isDigit() }
            val defaultPass = digitsOnly.takeLast(6)
            val savedCheck = sessionManager.verifyRegisteredPassword(cleanMobile, trimmedCurrent)
            val isValid = savedCheck ?: (digitsOnly.length >= 6 && trimmedCurrent == defaultPass)
            if (!isValid) {
                Pair(false, "বর্তমান পাসওয়ার্ড সঠিক নয়।")
            } else {
                sessionManager.saveRegisteredCredential(
                    mobile = cleanMobile,
                    committeeId = committeeId,
                    mosqueId = mosqueId,
                    password = trimmedNew
                )
                Pair(true, "পাসওয়ার্ড সফলভাবে পরিবর্তন করা হয়েছে।")
            }
        }
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
            committeeId = matchedMosque?.committeeId ?: trimmedCommitteeId.ifBlank { "MC-REG" },
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
        val savedPasswordCheck = sessionManager.verifyRegisteredPassword(cleanMobile, pass.trim())
        val isPasswordValid = savedPasswordCheck ?: (digitsOnly.length >= 6 && pass.trim() == expectedLast6)
        if (!isPasswordValid) {
            return Pair(false, "পাসওয়ার্ড সঠিক নয়। আপনার নির্ধারিত পাসওয়ার্ড বা মোবাইল নম্বরের শেষ ৬টি সংখ্যা দিন।")
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
            committeeId = matchedMosque?.committeeId ?: trimmedCommitteeId.ifBlank { "MC-REG" },
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
        val name = matchedMember?.nameBn ?: role.titleBn
        val mobile = matchedMember?.mobile ?: ""
        val memberId = matchedMember?.id ?: "mem-${role.code}"
        val activeMosque = mosque.value
        sessionManager.saveSession(
            role = role,
            memberId = memberId,
            memberName = name,
            committeeId = activeMosque?.committeeId ?: "",
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
        _readNotificationIds.value = emptySet()
        _activeMosqueId.value = ""
        _isLoggedIn.value = false
        _currentUserRole.value = UserRole.GENERAL_MEMBER
    }

    fun purgeDemoData(onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = MosqueDatabase.getDatabase(getApplication(), viewModelScope)
            DatabaseInitializer.purgeDemoDataIfPresent(db, getApplication())
            withContext(Dispatchers.Main) {
                logout()
                onComplete()
            }
        }
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
        val activeMosqueName = mosque.value?.nameBn ?: "মসজিদ সেবা"
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
        val activeMosqueName = mosque.value?.nameBn ?: "মসজিদ সেবা"
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

    private val _aiEventGuidanceState = MutableStateFlow<AiEventGuidanceUiState>(AiEventGuidanceUiState.Idle)
    val aiEventGuidanceState: StateFlow<AiEventGuidanceUiState> = _aiEventGuidanceState.asStateFlow()

    fun clearEventAiGuidance() {
        _aiEventGuidanceState.value = AiEventGuidanceUiState.Idle
    }

    fun generateEventAiGuidance(event: IslamicEvent) {
        val activeMosqueName = mosque.value?.nameBn ?: "মসজিদ সেবা"
        val activeMosqueAddress = mosque.value?.address ?: "ঢাকা"

        viewModelScope.launch {
            _aiEventGuidanceState.value = AiEventGuidanceUiState.Loading
            try {
                val prompt = """
                    আপনি "$activeMosqueName" ($activeMosqueAddress)-এর খতিব ও ইসলামিক গবেষণা সহযোগী।
                    আসন্ন বা চলমান পবিত্র উপলক্ষ: "${event.titleBn} (${event.titleEn})"
                    আরবি মাস ও তারিখ: ${event.dateRangeBn}
                    মূল তাৎপর্য: ${event.defaultSignificanceBn}
                    
                    অনুগ্রহ করে খতিব সাহেব ও সাধারণ মুসল্লিদের জন্য অত্যন্ত মার্জিত, প্রামাণ্য (কুরআন ও সুন্নাহ ভিত্তিক) বাংলায় একটি পূর্ণাঙ্গ দিকনির্দেশনা তৈরি করুন। এতে অন্তর্ভুক্ত থাকবে:
                    ১. কুরআন ও সহিহ হাদিসের আলোকে এই দিবসের ফজিলত ও গুরুত্ব।
                    ২. সাধারণ মুসল্লিদের জন্য বিশেষ করণীয় নফল আমল, দোয়া ও জিকিরসমূহ (এবং বর্জনীয় বিদআত/কুসংস্কার)।
                    ৩. ইমাম ও খতিবদের জন্য জুমার বয়ান বা মাহফিলের ৩-৪টি শক্তিশালী পয়েন্ট ও পয়েন্টভিত্তিক মূল বক্তব্য।
                    ৪. সমাজের অভাবী মানুষ ও পাড়া-প্রতিবেশীর হক আদায়ে বিশেষ দিকনির্দেশনা।
                    
                    ভাষা হবে আবেগময়, প্রামাণ্য ও উৎসাহব্যঞ্জক। অপ্রয়োজনীয় ভূমিকা পরিহার করে সরাসরি বিষয়বস্তু তুলে ধরুন।
                """.trimIndent()

                val response = withTimeout(60_000L) {
                    summaryModel.generateContent(prompt)
                }
                val text = response.text
                if (text.isNullOrBlank()) {
                    _aiEventGuidanceState.value = AiEventGuidanceUiState.Success(
                        eventId = event.id,
                        fullGuidanceText = event.buildDefaultGuidanceSummary()
                    )
                } else {
                    _aiEventGuidanceState.value = AiEventGuidanceUiState.Success(
                        eventId = event.id,
                        fullGuidanceText = text.trim()
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI event guidance timed out, fallback to default", e)
                _aiEventGuidanceState.value = AiEventGuidanceUiState.Success(
                    eventId = event.id,
                    fullGuidanceText = event.buildDefaultGuidanceSummary()
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Firebase AI event guidance failed, fallback to default", e)
                _aiEventGuidanceState.value = AiEventGuidanceUiState.Success(
                    eventId = event.id,
                    fullGuidanceText = event.buildDefaultGuidanceSummary()
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
            val maxNum = allMembers.value.mapNotNull {
                it.memberCode.removePrefix("MEM-").toIntOrNull()
            }.maxOrNull() ?: 100
            val newCode = "MEM-${maxNum + 1}"
            val joinDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val memberId = UUID.randomUUID().toString()
            val member = MemberEntity(
                id = memberId,
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
            syncStaffForMember(
                member = member,
                salaryAmount = salaryAmount,
                staffMealRequired = staffMealRequired
            )
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
        sourceType: String = "manual",
        paymentMethod: String = "cash"
    ) {
        viewModelScope.launch {
            val nextNum = allTransactions.value.size + 1
            val code = "TXN-2024-${String.format(Locale.US, "%03d", nextNum)}"
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val role = _currentUserRole.value

            // Approval policy: President / Vice President creates auto-approved up to 50k; others pending
            val status = if (role.canApproveFinance() && amount <= 50000) "approved" else "pending"
            val approvedBy = if (status == "approved") role.titleBn else null
            val resolvedSourceType = if (sourceType == "manual") "manual_$paymentMethod" else sourceType

            val txn = TransactionEntity(
                id = UUID.randomUUID().toString(),
                transactionCode = code,
                categoryNameBn = categoryNameBn,
                type = type,
                amount = amount,
                txnDate = dateStr,
                description = description,
                sourceType = resolvedSourceType,
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
        method: String,
        selectedMonthCount: Int = 1,
        onRecorded: ((ChandaPaymentEntity) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val commId = mosque.value?.committeeId ?: "MS"
            val rcpNum = allChandaPayments.value.size + 101
            val rcpCode = "RCP-$currentYear-$rcpNum"
            val qrCode = "VER-$commId-$rcpNum-${UUID.randomUUID().toString().take(6).uppercase()}"
            val cal = Calendar.getInstance()
            val amPm = if (cal.get(Calendar.AM_PM) == Calendar.AM) "পূর্বাহ্ণ" else "অপরাহ্ণ"
            val paidAtStr = toBengaliDigits("${SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault()).format(cal.time)} $amPm")
            val monthCount = selectedMonthCount.coerceAtLeast(1)
            val expectedDue = (member.monthlyChanda * monthCount).coerceAtLeast(member.monthlyChanda)
            val payment = ChandaPaymentEntity(
                id = UUID.randomUUID().toString(),
                memberId = member.id,
                memberNameBn = member.nameBn,
                month = month,
                amountDue = expectedDue,
                amountPaid = amount,
                method = method,
                status = if (amount >= expectedDue) "paid" else "partial",
                receiptCode = rcpCode,
                qrVerificationCode = qrCode,
                receivedBy = _currentUserRole.value.titleBn,
                paidAt = paidAtStr
            )
            repository.recordChandaPayment(payment)
            if (sessionManager.isNotificationsEnabled()) {
                MosquePushNotificationHelper.postChandaReceiptNotification(getApplication(), payment)
            }
            onRecorded?.invoke(payment)
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
            if (sessionManager.isNotificationsEnabled()) {
                MosquePushNotificationHelper.postNoticeNotification(getApplication(), notice)
            }
        }
    }

    fun updateNotice(
        notice: NoticeEntity,
        title: String,
        content: String,
        priority: String,
        isPinned: Boolean
    ) {
        viewModelScope.launch {
            val updated = notice.copy(
                title = title,
                content = content,
                priority = priority,
                isPinned = isPinned
            )
            repository.updateNotice(updated)
        }
    }

    fun togglePinNotice(notice: NoticeEntity) {
        viewModelScope.launch {
            val updated = notice.copy(isPinned = !notice.isPinned)
            val action = if (updated.isPinned) "NOTICE_PINNED" else "NOTICE_UNPINNED"
            val details = if (updated.isPinned) "নোটিশ পিন করা হয়েছে: ${notice.title}" else "নোটিশ আনপিন করা হয়েছে: ${notice.title}"
            repository.updateNotice(updated, action, details)
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
            if (sessionManager.isNotificationsEnabled()) {
                MosquePushNotificationHelper.postMeetingNotification(getApplication(), meeting)
            }
        }
    }

    fun updateMeeting(
        meeting: MeetingEntity,
        title: String,
        dateStr: String,
        location: String,
        agenda: String
    ) {
        viewModelScope.launch {
            val updated = meeting.copy(
                title = title,
                meetingDate = dateStr,
                location = location,
                agenda = agenda
            )
            repository.updateMeeting(updated)
        }
    }

    fun toggleMeetingStatus(meeting: MeetingEntity) {
        viewModelScope.launch {
            val nextStatus = if (meeting.status == "completed") "upcoming" else "completed"
            val updated = meeting.copy(status = nextStatus)
            val action = if (nextStatus == "completed") "MEETING_COMPLETED" else "MEETING_REOPENED"
            val details = if (nextStatus == "completed") "সভা সম্পন্ন হিসেবে চিহ্নিত: ${meeting.title}" else "সভা পুনরায় আসন্ন হিসেবে চিহ্নিত: ${meeting.title}"
            repository.updateMeeting(updated, action, details)
        }
    }

    fun saveMeetingResolution(
        meeting: MeetingEntity,
        resolution: String,
        markCompleted: Boolean
    ) {
        viewModelScope.launch {
            val nextStatus = if (markCompleted) "completed" else meeting.status
            val updated = meeting.copy(
                resolution = resolution.trim(),
                status = nextStatus
            )
            repository.updateMeeting(
                updated,
                auditAction = "MEETING_RESOLUTION_SAVED",
                auditDetails = "সভার সিদ্ধান্ত/কার্যবিবরণী সংরক্ষিত: ${meeting.title}"
            )
        }
    }

    fun deleteMeeting(meeting: MeetingEntity) {
        viewModelScope.launch {
            repository.deleteMeeting(meeting)
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

    fun getAttendanceForStudent(studentId: String): Flow<List<MadrasaAttendanceEntity>> =
        repository.getAttendanceForStudent(studentId)

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

    fun updateStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.updateStudent(student)
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
            val cal = Calendar.getInstance()
            val amPm = if (cal.get(Calendar.AM_PM) == Calendar.AM) "পূর্বাহ্ণ" else "অপরাহ্ণ"
            val paidAtStr = toBengaliDigits("${SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault()).format(cal.time)} $amPm")
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
        bonus: Double = 0.0,
        paymentMethod: String = "cash",
        onDisbursed: ((StaffPayrollEntity) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val baseSalary = if (staff.salaryAmount > 0.0) staff.salaryAmount else {
                when (staff.roleCode.lowercase()) {
                    "imam" -> 18000.0
                    "muazzin" -> 12000.0
                    else -> 9000.0
                }
            }
            if (staff.salaryAmount <= 0.0) {
                repository.updateStaff(staff.copy(salaryAmount = baseSalary))
            }
            val existsInDb = activeStaff.value.any { it.id == staff.id }
            if (!existsInDb) {
                repository.addStaff(staff.copy(salaryAmount = baseSalary, isActive = true))
            }
            val net = baseSalary + bonus
            val payroll = StaffPayrollEntity(
                id = UUID.randomUUID().toString(),
                staffId = staff.id,
                staffNameBn = staff.nameBn,
                month = month,
                salaryAmount = baseSalary,
                bonusAmount = bonus,
                netSalary = net,
                paymentStatus = "paid",
                paidDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )
            repository.disburseStaffSalary(
                payroll = payroll,
                paidBy = _currentUserRole.value.titleBn,
                paymentMethod = paymentMethod
            )
            onDisbursed?.invoke(payroll)
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

    fun normalizeDigitsToAscii(input: String): String {
        val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val sb = StringBuilder(input.length)
        for (ch in input) {
            val idx = bnDigits.indexOf(ch)
            if (idx >= 0) sb.append(('0' + idx)) else sb.append(ch)
        }
        return sb.toString()
    }

    fun getOrderedWillingMealDonors(
        members: List<MemberEntity> = allMembers.value,
        schedules: List<FoodScheduleEntity> = allFoodSchedules.value
    ): List<MemberEntity> {
        val willing = members
            .filter { it.status == "active" && it.willingToProvideStaffMeals }
            .sortedBy { it.memberCode }
        if (willing.isEmpty()) return emptyList()

        val activeSchedules = schedules.filter { it.status != "cancelled" && it.status != "rejected" }
        return willing.sortedWith(
            compareBy<MemberEntity> { member ->
                val memberDigits = member.mobile.filter { it.isDigit() }
                val cleanName = member.nameBn.trim()
                activeSchedules.count { food ->
                    val foodDigits = normalizeDigitsToAscii(food.providerMobile).filter { it.isDigit() }
                    (memberDigits.length >= 6 && foodDigits == memberDigits) ||
                        food.providerName.trim().equals(cleanName, ignoreCase = true)
                }
            }.thenBy { it.memberCode }
        )
    }

    fun addFoodSchedule(
        dateStr: String,
        providerName: String,
        providerMobile: String,
        receiverStaffName: String
    ) {
        viewModelScope.launch {
            val food = FoodScheduleEntity(
                id = UUID.randomUUID().toString(),
                dateStr = normalizeDigitsToAscii(dateStr).trim(),
                mealType = "দৈনিক খাবার",
                providerName = providerName,
                providerMobile = providerMobile,
                receiverStaffName = receiverStaffName,
                status = "scheduled"
            )
            repository.addFoodSchedule(food)
        }
    }

    fun autoGenerateFoodSchedule(
        daysCount: Int,
        startDateIso: String,
        targetStaffLabels: List<String>,
        onComplete: (createdCount: Int, skippedCount: Int) -> Unit
    ) {
        viewModelScope.launch {
            val orderedDonors = getOrderedWillingMealDonors()
            if (orderedDonors.isEmpty() || targetStaffLabels.isEmpty()) {
                onComplete(0, 0)
                return@launch
            }

            val activeExisting = allFoodSchedules.value
                .filter { it.status != "cancelled" && it.status != "rejected" }

            // Track already occupied (date -> receiver) and (date -> set of donor keys) so 1 member doesn't feed 2 staff on same day
            val existingDateReceiverPairs = activeExisting
                .map { normalizeDigitsToAscii(it.dateStr).trim() to it.receiverStaffName.trim() }
                .toMutableSet()

            val donorKeysByDate = mutableMapOf<String, MutableSet<String>>()
            activeExisting.forEach { food ->
                val dateKey = normalizeDigitsToAscii(food.dateStr).trim()
                val donorKey = food.providerMobile.filter { it.isDigit() }.ifBlank { food.providerName.trim() }
                donorKeysByDate.getOrPut(dateKey) { mutableSetOf() }.add(donorKey)
            }

            val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val parsedStart = runCatching {
                isoFormat.parse(normalizeDigitsToAscii(startDateIso).trim())
            }.getOrNull() ?: Date()

            val calendar = Calendar.getInstance().apply { time = parsedStart }
            val newSchedules = mutableListOf<FoodScheduleEntity>()
            var skippedCount = 0
            var donorCursor = 0

            for (dayOffset in 0 until daysCount) {
                val dateIso = isoFormat.format(calendar.time)
                val usedToday = donorKeysByDate.getOrPut(dateIso) { mutableSetOf() }

                for (staffLabel in targetStaffLabels) {
                    val slotKey = dateIso to staffLabel.trim()
                    val alreadyAssigned = existingDateReceiverPairs.any { (d, r) ->
                        d == dateIso && (r.equals(staffLabel.trim(), ignoreCase = true) ||
                            r.contains(staffLabel.substringBefore(" (").trim(), ignoreCase = true))
                    }
                    if (alreadyAssigned) {
                        skippedCount++
                        continue
                    }

                    // Pick next donor in fair round-robin who isn't already assigned today (when enough donors exist)
                    var pickedDonor: MemberEntity? = null
                    for (attempt in orderedDonors.indices) {
                        val candidate = orderedDonors[(donorCursor + attempt) % orderedDonors.size]
                        val candidateKey = candidate.mobile.filter { it.isDigit() }.ifBlank { candidate.nameBn.trim() }
                        if (candidateKey !in usedToday) {
                            pickedDonor = candidate
                            donorCursor = (donorCursor + attempt + 1) % orderedDonors.size
                            usedToday.add(candidateKey)
                            break
                        }
                    }
                    if (pickedDonor == null) {
                        pickedDonor = orderedDonors[donorCursor % orderedDonors.size]
                        donorCursor = (donorCursor + 1) % orderedDonors.size
                    }

                    existingDateReceiverPairs.add(slotKey)
                    newSchedules.add(
                        FoodScheduleEntity(
                            id = "food-auto-${UUID.randomUUID()}",
                            dateStr = dateIso,
                            mealType = "দৈনিক খাবার",
                            providerName = pickedDonor.nameBn,
                            providerMobile = pickedDonor.mobile,
                            receiverStaffName = staffLabel.trim(),
                            status = "scheduled"
                        )
                    )
                }
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            if (newSchedules.isNotEmpty()) {
                repository.addFoodSchedulesBatch(
                    schedules = newSchedules,
                    actor = _currentUserRole.value.titleBn,
                    summaryNote = "স্বয়ংক্রিয় দৈনিক খাবার শিডিউল: ${newSchedules.size} টি পালা (${daysCount} দিন, ${targetStaffLabels.size} জন স্টাফ, ${orderedDonors.size} জন দাতা)"
                )
            }
            onComplete(newSchedules.size, skippedCount)
        }
    }

    fun approveFoodSchedule(schedule: FoodScheduleEntity) {
        viewModelScope.launch {
            val updated = schedule.copy(status = "approved")
            repository.updateFoodSchedule(
                schedule = updated,
                actor = schedule.providerName,
                auditDetails = "খাবার দায়িত্ব সম্মতি: ${schedule.dateStr} • দাতা: ${schedule.providerName} → ${schedule.receiverStaffName}"
            )
        }
    }

    fun rejectFoodSchedule(
        schedule: FoodScheduleEntity,
        onResult: (reassignedToName: String?) -> Unit = {}
    ) {
        viewModelScope.launch {
            val targetDate = normalizeDigitsToAscii(schedule.dateStr).trim()
            val currentDigits = normalizeDigitsToAscii(schedule.providerMobile).filter { it.isDigit() }
            val usedSameDayKeys = allFoodSchedules.value
                .filter {
                    normalizeDigitsToAscii(it.dateStr).trim() == targetDate &&
                        it.id != schedule.id &&
                        it.status != "cancelled" &&
                        it.status != "rejected"
                }
                .map { normalizeDigitsToAscii(it.providerMobile).filter { d -> d.isDigit() }.ifBlank { it.providerName.trim() } }
                .toSet()

            val orderedDonors = getOrderedWillingMealDonors()
            val alternateDonor = orderedDonors.firstOrNull { donor ->
                val donorDigits = donor.mobile.filter { it.isDigit() }
                val donorKey = donorDigits.ifBlank { donor.nameBn.trim() }
                val isRejectingMember = (currentDigits.length >= 6 && donorDigits == currentDigits) ||
                    donor.nameBn.trim().equals(schedule.providerName.trim(), ignoreCase = true)
                !isRejectingMember && donorKey !in usedSameDayKeys
            }

            if (alternateDonor != null) {
                val reassigned = schedule.copy(
                    providerName = alternateDonor.nameBn,
                    providerMobile = alternateDonor.mobile,
                    status = "scheduled"
                )
                repository.updateFoodSchedule(
                    schedule = reassigned,
                    actor = schedule.providerName,
                    auditDetails = "${schedule.providerName} অপারগতা → স্বয়ংক্রিয়ভাবে ${alternateDonor.nameBn}-কে দায়িত্ব (${schedule.dateStr}, ${schedule.receiverStaffName})"
                )
                onResult(alternateDonor.nameBn)
            } else {
                val rejected = schedule.copy(status = "rejected")
                repository.updateFoodSchedule(
                    schedule = rejected,
                    actor = schedule.providerName,
                    auditDetails = "খাবার দায়িত্ব অপারগতা (বিকল্প প্রয়োজন): ${schedule.dateStr} • ${schedule.providerName} (${schedule.receiverStaffName})"
                )
                onResult(null)
            }
        }
    }

    fun markFoodScheduleDelivered(schedule: FoodScheduleEntity) {
        viewModelScope.launch {
            val updated = schedule.copy(status = "delivered")
            repository.updateFoodSchedule(
                schedule = updated,
                actor = _currentUserRole.value.titleBn,
                auditDetails = "খাবার বিতরণ সম্পন্ন: ${schedule.dateStr} - দাতা: ${schedule.providerName} → ${schedule.receiverStaffName}"
            )
        }
    }

    fun cancelFoodSchedule(schedule: FoodScheduleEntity) {
        viewModelScope.launch {
            val updated = schedule.copy(status = "cancelled")
            repository.updateFoodSchedule(
                schedule = updated,
                actor = _currentUserRole.value.titleBn,
                auditDetails = "খাবার শিডিউল বাতিল: ${schedule.dateStr} - দাতা: ${schedule.providerName} (${schedule.receiverStaffName})"
            )
        }
    }

    fun rotateFoodScheduleDonor(
        schedule: FoodScheduleEntity,
        onRotated: (FoodScheduleEntity?) -> Unit = {}
    ) {
        viewModelScope.launch {
            val orderedDonors = getOrderedWillingMealDonors()
            val targetDate = normalizeDigitsToAscii(schedule.dateStr).trim()
            val currentDigits = normalizeDigitsToAscii(schedule.providerMobile).filter { it.isDigit() }
            val usedSameDayKeys = allFoodSchedules.value
                .filter {
                    normalizeDigitsToAscii(it.dateStr).trim() == targetDate &&
                        it.id != schedule.id &&
                        it.status != "cancelled" &&
                        it.status != "rejected"
                }
                .map { normalizeDigitsToAscii(it.providerMobile).filter { d -> d.isDigit() }.ifBlank { it.providerName.trim() } }
                .toSet()

            val nextDonor = orderedDonors.firstOrNull { donor ->
                val donorDigits = donor.mobile.filter { it.isDigit() }
                val donorKey = donorDigits.ifBlank { donor.nameBn.trim() }
                val sameMobile = currentDigits.length >= 6 && donorDigits == currentDigits
                val sameName = donor.nameBn.trim().equals(schedule.providerName.trim(), ignoreCase = true)
                !sameMobile && !sameName && donorKey !in usedSameDayKeys
            } ?: orderedDonors.firstOrNull { donor ->
                val donorDigits = donor.mobile.filter { it.isDigit() }
                val sameMobile = currentDigits.length >= 6 && donorDigits == currentDigits
                val sameName = donor.nameBn.trim().equals(schedule.providerName.trim(), ignoreCase = true)
                !sameMobile && !sameName
            } ?: orderedDonors.firstOrNull()

            if (nextDonor == null) {
                onRotated(null)
                return@launch
            }

            val updated = schedule.copy(
                providerName = nextDonor.nameBn,
                providerMobile = nextDonor.mobile,
                status = "scheduled"
            )
            repository.updateFoodSchedule(
                schedule = updated,
                actor = _currentUserRole.value.titleBn,
                auditDetails = "খাবার দাতা স্বয়ংক্রিয় পরিবর্তন: ${schedule.dateStr} (${schedule.receiverStaffName}) ${schedule.providerName} → ${nextDonor.nameBn}"
            )
            onRotated(updated)
        }
    }

    fun deleteFoodSchedule(schedule: FoodScheduleEntity) {
        viewModelScope.launch {
            repository.deleteFoodSchedule(
                schedule = schedule,
                actor = _currentUserRole.value.titleBn
            )
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

    fun markWelfareDisbursed(case: WelfareCaseEntity) {
        viewModelScope.launch {
            repository.markWelfareDisbursed(case)
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

    // --- Member Management Actions (Permanent ID & Safe Role Switching) ---
    private suspend fun syncStaffForMember(
        member: MemberEntity,
        salaryAmount: Double = 0.0,
        staffMealRequired: Boolean = true
    ) {
        val isStaffRole = member.roleCode.equals("imam", ignoreCase = true) ||
            member.roleCode.equals("muazzin", ignoreCase = true) ||
            member.roleCode.equals("khadem", ignoreCase = true)
        val normMobile = member.mobile.filter { it.isDigit() }
        val existingStaff = activeStaff.value.find { st ->
            st.id == "staff-${member.id}" ||
                (normMobile.length >= 6 && st.mobile.filter { it.isDigit() } == normMobile) ||
                st.nameBn.trim() == member.nameBn.trim()
        }
        val memberActive = member.status == "active"

        if (isStaffRole) {
            val roleTitle = UserRole.fromCode(member.roleCode).titleBn
            if (existingStaff != null) {
                repository.updateStaff(
                    existingStaff.copy(
                        nameBn = member.nameBn,
                        mobile = member.mobile,
                        roleCode = member.roleCode,
                        designationBn = roleTitle,
                        salaryAmount = if (salaryAmount > 0) salaryAmount else existingStaff.salaryAmount,
                        mealRequired = staffMealRequired,
                        isActive = memberActive
                    )
                )
            } else {
                repository.addStaff(
                    StaffEntity(
                        id = "staff-${member.id}",
                        nameBn = member.nameBn,
                        roleCode = member.roleCode,
                        designationBn = roleTitle,
                        joinDate = member.joinDate,
                        salaryAmount = if (salaryAmount > 0) salaryAmount else 12000.0,
                        mealRequired = staffMealRequired,
                        mobile = member.mobile,
                        isActive = memberActive
                    )
                )
            }
        } else if (existingStaff != null && existingStaff.isActive) {
            repository.updateStaff(existingStaff.copy(isActive = false))
        }
    }

    fun updateMemberRole(member: MemberEntity, newRole: UserRole) {
        viewModelScope.launch {
            val updated = member.copy(roleCode = newRole.code)
            repository.updateMember(updated)
            syncStaffForMember(updated)
        }
    }

    fun updateMemberDetails(
        member: MemberEntity,
        salaryAmount: Double = 0.0,
        staffMealRequired: Boolean = true
    ) {
        viewModelScope.launch {
            repository.updateMember(member)
            syncStaffForMember(
                member = member,
                salaryAmount = salaryAmount,
                staffMealRequired = staffMealRequired
            )
        }
    }

    fun toggleMemberStatus(member: MemberEntity) {
        viewModelScope.launch {
            val nextStatus = if (member.status == "active") "inactive" else "active"
            val updated = member.copy(status = nextStatus)
            repository.updateMember(updated)
            syncStaffForMember(updated)
        }
    }

    fun deleteMember(member: MemberEntity) {
        viewModelScope.launch {
            repository.deleteMember(member)
            val normMobile = member.mobile.filter { it.isDigit() }
            activeStaff.value.find { st ->
                st.id == "staff-${member.id}" ||
                    (normMobile.length >= 6 && st.mobile.filter { it.isDigit() } == normMobile) ||
                    st.nameBn.trim() == member.nameBn.trim()
            }?.let { st ->
                repository.updateStaff(st.copy(isActive = false))
            }
        }
    }

    fun updateStaffDetails(staff: StaffEntity) {
        viewModelScope.launch {
            repository.updateStaff(staff)
        }
    }

    fun updateMosqueDetails(updated: MosqueEntity) {
        viewModelScope.launch {
            repository.updateMosqueProfile(updated, _currentUserRole.value.titleBn)
        }
    }

    fun promoteStaffAndUpdateDetails(
        staff: StaffEntity,
        nameBn: String,
        nameEn: String,
        mobile: String,
        email: String,
        nid: String,
        address: String,
        villageArea: String,
        roleCode: String,
        monthlyChanda: Double,
        photoUrl: String?,
        willingToProvideStaffMeals: Boolean,
        salaryAmount: Double,
        staffMealRequired: Boolean
    ) {
        viewModelScope.launch {
            val normMobile = mobile.filter { it.isDigit() }
            val existingMember = allMembers.value.find { m ->
                (normMobile.length >= 6 && m.mobile.filter { it.isDigit() } == normMobile) ||
                    m.nameBn.trim() == staff.nameBn.trim()
            }
            val member = if (existingMember != null) {
                existingMember.copy(
                    nameBn = nameBn,
                    nameEn = nameEn,
                    mobile = mobile,
                    email = email,
                    nid = nid,
                    address = address,
                    villageArea = villageArea,
                    roleCode = roleCode,
                    monthlyChanda = monthlyChanda,
                    photoUrl = photoUrl,
                    willingToProvideStaffMeals = willingToProvideStaffMeals
                )
            } else {
                val maxNum = allMembers.value.mapNotNull {
                    it.memberCode.removePrefix("MEM-").toIntOrNull()
                }.maxOrNull() ?: 100
                MemberEntity(
                    id = UUID.randomUUID().toString(),
                    memberCode = "MEM-${maxNum + 1}",
                    nameBn = nameBn,
                    nameEn = nameEn,
                    mobile = mobile,
                    email = email,
                    nid = nid,
                    address = address,
                    villageArea = villageArea,
                    roleCode = roleCode,
                    monthlyChanda = monthlyChanda,
                    joinDate = staff.joinDate,
                    photoUrl = photoUrl,
                    status = if (staff.isActive) "active" else "inactive",
                    willingToProvideStaffMeals = willingToProvideStaffMeals
                )
            }
            if (existingMember != null) {
                repository.updateMember(member)
            } else {
                repository.addMember(member)
            }

            val isStaffRole = roleCode.equals("imam", ignoreCase = true) ||
                roleCode.equals("muazzin", ignoreCase = true) ||
                roleCode.equals("khadem", ignoreCase = true)
            if (isStaffRole) {
                val roleTitle = UserRole.fromCode(roleCode).titleBn
                repository.updateStaff(
                    staff.copy(
                        nameBn = nameBn,
                        mobile = mobile,
                        roleCode = roleCode,
                        designationBn = roleTitle,
                        salaryAmount = if (salaryAmount > 0) salaryAmount else staff.salaryAmount,
                        mealRequired = staffMealRequired,
                        isActive = member.status == "active"
                    )
                )
            } else {
                repository.updateStaff(staff.copy(isActive = false))
            }
        }
    }

    fun toggleStaffStatus(staff: StaffEntity) {
        viewModelScope.launch {
            repository.updateStaff(staff.copy(isActive = !staff.isActive))
        }
    }

    fun removeStaff(staff: StaffEntity) {
        viewModelScope.launch {
            repository.updateStaff(staff.copy(isActive = false))
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

    // --- মসজিদ পরিচালনা কমিটি নির্বাচন ও পুনর্গঠন ---
    val allElections: StateFlow<List<ElectionScheduleEntity>> = repository.allElections
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeElection: StateFlow<ElectionScheduleEntity?> = repository.activeElection
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allCandidates: StateFlow<List<ElectionCandidateEntity>> = repository.allCandidates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createElection(
        termTitle: String,
        termStartYear: Int,
        termEndYear: Int,
        chiefElectionCommissioner: String,
        commissionerMobile: String,
        scheduleDate: String,
        nominationDeadline: String,
        votingDate: String,
        electionType: String,
        gazetteNotes: String
    ) {
        viewModelScope.launch {
            val election = ElectionScheduleEntity(
                id = "elec-${UUID.randomUUID().toString().take(8)}",
                termTitle = termTitle,
                termStartYear = termStartYear,
                termEndYear = termEndYear,
                chiefElectionCommissioner = chiefElectionCommissioner,
                commissionerMobile = commissionerMobile,
                scheduleDate = scheduleDate,
                nominationDeadline = nominationDeadline,
                votingDate = votingDate,
                status = "nomination_open",
                electionType = electionType,
                gazetteNotes = gazetteNotes
            )
            repository.createElection(election, _currentUserRole.value.titleBn)
            if (sessionManager.isNotificationsEnabled()) {
                MosquePushNotificationHelper.postElectionNotification(getApplication(), election, "তফসিল ঘোষণা")
            }
        }
    }

    fun updateElectionStatus(election: ElectionScheduleEntity, newStatus: String) {
        viewModelScope.launch {
            val updated = election.copy(status = newStatus)
            repository.updateElection(updated, _currentUserRole.value.titleBn)
            if (sessionManager.isNotificationsEnabled()) {
                val milestone = when (newStatus) {
                    "voting_ongoing" -> "ভোটগ্রহণ শুরু"
                    "results_published" -> "ফলাফল ও গেজেট প্রকাশ"
                    "completed" -> "নতুন কমিটি শপথ ও দায়িত্ব গ্রহণ"
                    else -> "পর্যায় আপডেট"
                }
                MosquePushNotificationHelper.postElectionNotification(getApplication(), updated, milestone)
            }
        }
    }

    fun sendTestPushNotification() {
        MosquePushNotificationHelper.postTestNotification(getApplication())
    }

    fun addElectionCandidate(
        electionId: String,
        targetRoleCode: String,
        targetRoleTitleBn: String,
        candidateNameBn: String,
        candidateMobile: String,
        memberCode: String,
        proposerNameBn: String,
        seconderNameBn: String,
        symbolOrPanel: String
    ) {
        viewModelScope.launch {
            val candidate = ElectionCandidateEntity(
                id = "cand-${UUID.randomUUID().toString().take(8)}",
                electionId = electionId,
                targetRoleCode = targetRoleCode,
                targetRoleTitleBn = targetRoleTitleBn,
                candidateNameBn = candidateNameBn,
                candidateMobile = candidateMobile,
                memberCode = memberCode,
                proposerNameBn = proposerNameBn,
                seconderNameBn = seconderNameBn,
                status = "valid",
                symbolOrPanel = symbolOrPanel.ifBlank { "ঐক্য প্যানেল" }
            )
            repository.addCandidate(candidate, _currentUserRole.value.titleBn)
        }
    }

    fun markCandidateElected(candidate: ElectionCandidateEntity, isElected: Boolean, votes: Int = 0) {
        viewModelScope.launch {
            repository.updateCandidate(
                candidate.copy(
                    isElected = isElected,
                    status = if (isElected) "elected" else "valid",
                    votesReceived = votes
                ),
                _currentUserRole.value.titleBn
            )
        }
    }

    fun deleteCandidate(candidate: ElectionCandidateEntity) {
        viewModelScope.launch {
            repository.deleteCandidate(candidate, _currentUserRole.value.titleBn)
        }
    }

    fun activateNewCommittee(election: ElectionScheduleEntity) {
        viewModelScope.launch {
            repository.activateNewCommittee(election, _currentUserRole.value.titleBn)
            val m = mosque.value
            if (m != null) {
                repository.updateMosque(
                    m.copy(
                        committeeStartYear = election.termStartYear,
                        committeeEndYear = election.termEndYear
                    )
                )
            }
        }
    }

    // --- মসজিদের স্থাবর ও অস্থাবর সম্পত্তি (Mosque Properties & Assets) ---
    val mosqueProperties: StateFlow<List<MosquePropertyEntity>> = repository.allProperties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveMosqueProperty(property: MosquePropertyEntity) {
        viewModelScope.launch {
            val propId = if (property.id.isBlank()) "PROP-${UUID.randomUUID().toString().take(8).uppercase()}" else property.id
            val existing = mosqueProperties.value.find { it.id == propId }
            if (existing != null) {
                repository.updateProperty(property.copy(id = propId), _currentUserRole.value.titleBn)
            } else {
                repository.addProperty(property.copy(id = propId), _currentUserRole.value.titleBn)
            }
        }
    }

    fun deleteMosqueProperty(property: MosquePropertyEntity) {
        viewModelScope.launch {
            repository.deleteProperty(property, _currentUserRole.value.titleBn)
        }
    }
}
