package com.mosqueseba.app

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.mosqueseba.app.data.local.entity.ChandaPaymentEntity
import com.mosqueseba.app.data.local.entity.MemberEntity
import com.mosqueseba.app.data.local.entity.StaffEntity
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.components.*
import com.mosqueseba.app.ui.dialogs.*
import com.mosqueseba.app.ui.screens.*
import com.mosqueseba.app.ui.theme.MosqueCommitteeTheme
import com.mosqueseba.app.ui.viewmodel.AiDawatnamaUiState
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel
import com.mosqueseba.app.util.MosquePushNotificationHelper
import com.mosqueseba.app.util.MosqueSecurityManager
import kotlinx.coroutines.flow.MutableStateFlow

enum class AppScreen {
    SPLASH,
    ONBOARDING,
    LOGIN,
    REGISTRATION,
    MAIN
}

class MainActivity : ComponentActivity() {

    private val viewModel: MosqueViewModel by viewModels()
    private val deepLinkModule = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MosqueSecurityManager.enableScreenProtection(this)
        configureAppCheck(this, intent)
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        MosquePushNotificationHelper.initChannels(this)
        handleIncomingIntent(intent)

        setContent {
            val darkModeEnabled by viewModel.darkModeEnabled.collectAsState()
            val darkModeFollowSystem by viewModel.darkModeFollowSystem.collectAsState()
            val systemInDark = androidx.compose.foundation.isSystemInDarkTheme()
            val effectiveDarkTheme = if (darkModeFollowSystem) systemInDark else darkModeEnabled
            val requestedModule by deepLinkModule.collectAsState()
            val context = LocalContext.current

            // Android 13+ Notification Permission Request
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    MosqueToastController.success("পুশ নোটিফিকেশন সফলভাবে সক্রিয় হয়েছে")
                }
            }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            MosqueCommitteeTheme(darkTheme = effectiveDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }

                        BackHandler(enabled = currentScreen == AppScreen.REGISTRATION) {
                            currentScreen = AppScreen.LOGIN
                        }

                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(250))
                            },
                            label = "AppScreenTransition"
                        ) { screen ->
                            when (screen) {
                                AppScreen.SPLASH -> {
                                    SplashScreen(
                                        onSplashFinished = {
                                            val isOnboarded = viewModel.sessionManager.isOnboardingCompleted()
                                            currentScreen = when {
                                                !isOnboarded -> AppScreen.ONBOARDING
                                                viewModel.sessionManager.isLoggedIn() -> AppScreen.MAIN
                                                else -> AppScreen.LOGIN
                                            }
                                        }
                                    )
                                }
                                AppScreen.ONBOARDING -> {
                                    OnboardingScreen(
                                        onOnboardingFinished = {
                                            viewModel.sessionManager.setOnboardingCompleted(true)
                                            currentScreen = if (viewModel.sessionManager.isLoggedIn()) {
                                                AppScreen.MAIN
                                            } else {
                                                AppScreen.LOGIN
                                            }
                                        }
                                    )
                                }
                                AppScreen.LOGIN -> {
                                    LoginScreen(
                                        viewModel = viewModel,
                                        onLoginSuccess = {
                                            currentScreen = AppScreen.MAIN
                                        },
                                        onNavigateToRegister = {
                                            currentScreen = AppScreen.REGISTRATION
                                        }
                                    )
                                }
                                AppScreen.REGISTRATION -> {
                                    RegistrationScreen(
                                        viewModel = viewModel,
                                        onNavigateBackToLogin = {
                                            currentScreen = AppScreen.LOGIN
                                        },
                                        onRegistrationSuccess = {
                                            currentScreen = AppScreen.MAIN
                                        }
                                    )
                                }
                                AppScreen.MAIN -> {
                                    MosqueAppShell(
                                        viewModel = viewModel,
                                        requestedModule = requestedModule,
                                        onClearDeepLink = { deepLinkModule.value = null },
                                        onOpenOnboarding = {
                                            currentScreen = AppScreen.ONBOARDING
                                        },
                                        onLogout = {
                                            viewModel.logout()
                                            currentScreen = AppScreen.LOGIN
                                        }
                                    )
                                }
                            }
                        }

                        // Global Floating Islamic Toast Host
                        MosqueToastHost(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .statusBarsPadding()
                                .padding(top = 10.dp, start = 14.dp, end = 14.dp)
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val targetModule = intent?.getStringExtra(MosquePushNotificationHelper.EXTRA_OPEN_MODULE)
        if (!targetModule.isNullOrBlank()) {
            deepLinkModule.value = targetModule
        }
    }
}


@Composable
fun MosqueAppShell(
    viewModel: MosqueViewModel,
    requestedModule: String? = null,
    onClearDeepLink: () -> Unit = {},
    onOpenOnboarding: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    var currentTabIndex by remember { mutableIntStateOf(0) }
    var overlaySubModule by remember { mutableStateOf<String?>(null) }
    var overlayUserProfile by remember { mutableStateOf<MemberProfileTarget?>(null) }
    var showAccountHub by remember { mutableStateOf(false) }
    var showNotificationsPage by remember { mutableStateOf(false) }

    LaunchedEffect(requestedModule) {
        if (!requestedModule.isNullOrBlank()) {
            overlaySubModule = requestedModule
            onClearDeepLink()
        }
    }
    val currentRole by viewModel.currentUserRole.collectAsState()
    val mosque by viewModel.mosque.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()
    val activeStaff by viewModel.activeStaff.collectAsState()
    val allChandaPayments by viewModel.allChandaPayments.collectAsState()
    val aiNoticeState by viewModel.aiNoticeState.collectAsState()
    val aiDawatnamaState by viewModel.aiDawatnamaState.collectAsState()
    val pendingTransactions by viewModel.pendingTransactions.collectAsState()
    val allCashboxRecords by viewModel.allCashboxRecords.collectAsState()
    val allOfficeDocuments by viewModel.allOfficeDocuments.collectAsState()
    val allAdvisoryNotes by viewModel.allAdvisoryNotes.collectAsState()
    val allNotices by viewModel.allNotices.collectAsState()
    val allMeetings by viewModel.allMeetings.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()
    val allDutyRosters by viewModel.allDutyRosters.collectAsState()
    val allFoodSchedules by viewModel.allFoodSchedules.collectAsState()
    val allWelfareCases by viewModel.allWelfareCases.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val readNotificationIds by viewModel.readNotificationIds.collectAsState()
    val activeCycleMonth = remember(allChandaPayments) {
        ChandaMonthUtils.resolveLatestSingleMonth(
            allChandaPayments.map { it.month },
            ChandaMonthUtils.getCurrentMonthYear()
        )
    }

    val savedRole = remember(currentRole) { viewModel.sessionManager.getSavedRole() }
    val savedName = remember(currentRole) { viewModel.sessionManager.getSavedMemberName() }
    val savedMemberId = remember(currentRole) { viewModel.sessionManager.getSavedMemberId() }
    val currentMember = remember(currentRole, allMembers, savedRole, savedMemberId) {
        allMembers.firstOrNull { it.id == savedMemberId && currentRole == savedRole }
            ?: allMembers.firstOrNull { it.roleCode.equals(currentRole.code, ignoreCase = true) }
    }
    val currentStaffForRole = remember(currentRole, currentMember, activeStaff) {
        val normMemberMobile = currentMember?.mobile?.filter { it.isDigit() }.orEmpty()
        activeStaff.firstOrNull { st ->
            (currentMember != null && (
                st.id == "staff-${currentMember.id}" ||
                    (normMemberMobile.length >= 6 && st.mobile.filter { it.isDigit() } == normMemberMobile) ||
                    st.nameBn.trim() == currentMember.nameBn.trim()
                )) || st.roleCode.equals(currentRole.code, ignoreCase = true)
        }
    }
    val currentUserProfileTarget = remember(currentRole, currentMember, currentStaffForRole) {
        val isSalaried = currentRole == UserRole.IMAM ||
            currentRole == UserRole.MUAZZIN ||
            currentRole == UserRole.KHADEM ||
            currentStaffForRole != null
        when {
            isSalaried && (currentStaffForRole != null || currentMember != null) ->
                MemberProfileTarget.SalariedStaff(
                    staffId = currentStaffForRole?.id,
                    memberId = currentMember?.id
                )
            currentMember != null -> MemberProfileTarget.ChandaMember(currentMember.id)
            else -> null
        }
    }
    val currentMemberName = remember(currentRole, currentMember, currentStaffForRole, savedRole, savedName) {
        if (currentRole == savedRole && savedName.isNotBlank() && savedName != "সদস্য") {
            savedName
        } else {
            currentMember?.nameBn ?: currentStaffForRole?.nameBn
        }
    }

    val roleNotificationItems = remember(
        currentRole,
        savedMemberId,
        currentMember,
        pendingTransactions,
        allCashboxRecords,
        allOfficeDocuments,
        allAdvisoryNotes,
        allNotices,
        allMeetings,
        allEvents,
        allDutyRosters,
        allFoodSchedules,
        allChandaPayments,
        allWelfareCases
    ) {
        buildRoleNotificationItems(
            role = currentRole,
            savedMemberId = savedMemberId,
            currentMember = currentMember,
            pendingTransactions = pendingTransactions,
            cashboxRecords = allCashboxRecords,
            officeDocuments = allOfficeDocuments,
            advisoryNotes = allAdvisoryNotes,
            notices = allNotices,
            meetings = allMeetings,
            events = allEvents,
            dutyRosters = allDutyRosters,
            foodSchedules = allFoodSchedules,
            chandaPayments = allChandaPayments,
            welfareCases = allWelfareCases
        )
    }
    val unreadNotificationCount = remember(roleNotificationItems, readNotificationIds) {
        roleNotificationItems.count { it.id !in readNotificationIds }
    }
    val notificationCount = if (notificationsEnabled) unreadNotificationCount else 0

    BackHandler(enabled = overlayUserProfile != null || overlaySubModule != null || showNotificationsPage || showAccountHub || currentTabIndex != 0) {
        when {
            overlayUserProfile != null -> overlayUserProfile = null
            overlaySubModule != null -> overlaySubModule = null
            showNotificationsPage -> showNotificationsPage = false
            showAccountHub -> showAccountHub = false
            else -> currentTabIndex = 0
        }
    }

    // Reset tab and overlay whenever role switches
    LaunchedEffect(currentRole) {
        currentTabIndex = 0
        overlaySubModule = null
        overlayUserProfile = null
        showAccountHub = false
        showNotificationsPage = false
    }

    val currentNavItems = remember(currentRole) {
        getNavItemsForRole(currentRole)
    }

    val activeDestination = currentNavItems.getOrNull(currentTabIndex)?.destinationKey ?: "dashboard"

    // Dialog controllers
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var transactionInitialType by remember { mutableStateOf("INCOME") }
    var transactionInitialCategory by remember { mutableStateOf<String?>(null) }
    var showReceiveChandaDialog by remember { mutableStateOf(false) }
    var chandaPreselectedTarget by remember { mutableStateOf<Pair<MemberEntity, Double>?>(null) }
    var paySalaryTargetStaff by remember { mutableStateOf<StaffEntity?>(null) }
    var chandaReceiptPreview by remember { mutableStateOf<ChandaPaymentEntity?>(null) }
    var showPublishNoticeDialog by remember { mutableStateOf(false) }
    var showScheduleMeetingDialog by remember { mutableStateOf(false) }
    var showRamadanHubDialog by remember { mutableStateOf(false) }
    var showCloudSyncDialog by remember { mutableStateOf(false) }
    var showInvitationPosterDialog by remember { mutableStateOf(false) }
    var showCashboxCounterDialog by remember { mutableStateOf(false) }
    var showJanazaAlertDialog by remember { mutableStateOf(false) }
    var showOfficialSealsDialog by remember { mutableStateOf(false) }
    var openFinanceReportRequest by remember { mutableIntStateOf(0) }
    var openFinancePendingRequest by remember { mutableIntStateOf(0) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 600.dp
        Scaffold(
            topBar = {
                GlobalHeader(
                    currentRole = currentRole,
                    memberName = currentMemberName,
                    mosqueName = mosque?.nameBn,
                    mosqueLogoUrl = mosque?.logoUrl,
                    userPhotoUrl = currentMember?.photoUrl,
                    notificationCount = notificationCount,
                    onRoleSelected = { viewModel.setUserRole(it) },
                    onNotificationClick = {
                        overlayUserProfile = null
                        overlaySubModule = null
                        showAccountHub = false
                        showNotificationsPage = true
                    },
                    onMosqueProfileClick = {
                        overlayUserProfile = null
                        showAccountHub = false
                        showNotificationsPage = false
                        overlaySubModule = "about"
                    },
                    onUserProfileClick = {
                        overlaySubModule = null
                        showAccountHub = false
                        showNotificationsPage = false
                        val target = currentUserProfileTarget
                        if (target != null) {
                            overlayUserProfile = target
                        } else {
                            MosqueToastController.info("ব্যক্তিগত প্রোফাইল তথ্য লোড হচ্ছে, অনুগ্রহ করে অপেক্ষা করুন।")
                        }
                    },
                    onAvatarClick = {
                        overlayUserProfile = null
                        showAccountHub = false
                        showNotificationsPage = false
                        overlaySubModule = "about"
                    },
                    onLogout = onLogout
                )
            },
            bottomBar = {
                if (!isExpandedScreen) {
                    NavigationBottomBar(
                        currentTabIndex = currentTabIndex,
                        currentRole = currentRole,
                        onTabSelected = {
                            currentTabIndex = it
                            overlaySubModule = null
                            overlayUserProfile = null
                            showAccountHub = false
                            showNotificationsPage = false
                        }
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isExpandedScreen) {
                    MosqueNavigationRail(
                        currentTabIndex = currentTabIndex,
                        currentRole = currentRole,
                        onTabSelected = {
                            currentTabIndex = it
                            overlaySubModule = null
                            overlayUserProfile = null
                            showAccountHub = false
                            showNotificationsPage = false
                        }
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
            if (overlayUserProfile != null) {
                MemberProfileScreen(
                    target = overlayUserProfile!!,
                    viewModel = viewModel,
                    activeCycleMonth = activeCycleMonth,
                    isOwnProfile = true,
                    showBackButton = true,
                    onOpenMosqueProfile = {
                        overlayUserProfile = null
                        overlaySubModule = "about"
                    },
                    onBack = { overlayUserProfile = null },
                    onOpenReceiveChanda = { member, suggested ->
                        chandaPreselectedTarget = member to suggested
                        showReceiveChandaDialog = true
                    },
                    onOpenPaySalary = { staff ->
                        paySalaryTargetStaff = staff
                    }
                )
            } else if (overlaySubModule == "about") {
                MosqueProfileScreen(
                    viewModel = viewModel,
                    showTopBar = true,
                    onBack = { overlaySubModule = null },
                    onOpenMyProfile = {
                        overlaySubModule = null
                        val target = currentUserProfileTarget
                        if (target != null) {
                            overlayUserProfile = target
                        } else {
                            MosqueToastController.warning("ব্যক্তিগত প্রোফাইল তথ্য পাওয়া যায়নি।")
                        }
                    }
                )
            } else if (overlaySubModule != null) {
                MoreScreen(
                    viewModel = viewModel,
                    initialSubModule = overlaySubModule,
                    onBack = { overlaySubModule = null },
                    onOpenReceiveChanda = { showReceiveChandaDialog = true },
                    onOpenScheduleMeeting = { showScheduleMeetingDialog = true },
                    onOpenPublishNotice = { showPublishNoticeDialog = true },
                    onOpenAddMember = { showAddMemberDialog = true },
                    onOpenMyProfile = {
                        overlaySubModule = null
                        currentUserProfileTarget?.let { overlayUserProfile = it }
                    }
                )
            } else if (showNotificationsPage) {
                NotificationsScreen(
                    viewModel = viewModel,
                    onBack = { showNotificationsPage = false },
                    onNavigateToModule = { moduleKey ->
                        showNotificationsPage = false
                        val matchingTabIdx = when (moduleKey) {
                            "finance" -> currentNavItems.indexOfFirst {
                                it.destinationKey == "finance" || it.destinationKey == "treasurer_report" || it.destinationKey == "transparency"
                            }
                            "meetings" -> currentNavItems.indexOfFirst { it.destinationKey == "secretary_meetings" }
                            "welfare" -> currentNavItems.indexOfFirst { it.destinationKey == "welfare" || it.destinationKey == "secretary_welfare" }
                            "chanda" -> currentNavItems.indexOfFirst { it.destinationKey == "my_chanda" || it.destinationKey == "treasurer_chanda" }
                            else -> currentNavItems.indexOfFirst { it.destinationKey == moduleKey }
                        }
                        if (matchingTabIdx != -1) {
                            overlaySubModule = null
                            currentTabIndex = matchingTabIdx
                        } else {
                            overlaySubModule = moduleKey
                        }
                    }
                )
            } else if (showAccountHub) {
                AccountHubScreen(
                    viewModel = viewModel,
                    mosque = mosque,
                    onBack = { showAccountHub = false },
                    onOpenMosqueProfile = {
                        overlaySubModule = "about"
                    },
                    onOpenMyProfile = {
                        currentUserProfileTarget?.let { overlayUserProfile = it }
                    },
                    onOpenOnboarding = {
                        showAccountHub = false
                        onOpenOnboarding()
                    },
                    onLogout = onLogout
                )
            } else {
                AnimatedContent(
                    targetState = activeDestination,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                    },
                    label = "MainTabTransition"
                ) { destination ->
                    when (destination) {
                        "dashboard" -> DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToTab = { tabIndex ->
                                if (tabIndex in currentNavItems.indices) {
                                    currentTabIndex = tabIndex
                                    overlaySubModule = null
                                }
                            },
                            onOpenDialog = { dialogKey ->
                                when (dialogKey) {
                                    "add_member" -> {
                                        if (currentRole.canManageMembers()) {
                                            showAddMemberDialog = true
                                        }
                                    }
                                    "add_income" -> {
                                        if (currentRole.canManageFinance()) {
                                            transactionInitialType = "INCOME"
                                            transactionInitialCategory = null
                                            showAddTransactionDialog = true
                                        }
                                    }
                                    "add_expense" -> {
                                        if (currentRole.canManageFinance()) {
                                            transactionInitialType = "EXPENSE"
                                            transactionInitialCategory = null
                                            showAddTransactionDialog = true
                                        }
                                    }
                                    "receive_chanda" -> {
                                        if (currentRole.canManageFinance()) {
                                            showReceiveChandaDialog = true
                                        }
                                    }
                                    "schedule_meeting" -> {
                                        if (currentRole.canScheduleMeetings()) {
                                            showScheduleMeetingDialog = true
                                        }
                                    }
                                    "publish_notice" -> {
                                        if (currentRole.canPublishNotices()) {
                                            showPublishNoticeDialog = true
                                        }
                                    }
                                    "members" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "members" || it.destinationKey == "committee" }
                                        if (idx != -1) currentTabIndex = idx else overlaySubModule = "members"
                                    }
                                    "finance", "transparency" -> {
                                        val idx = currentNavItems.indexOfFirst {
                                            it.destinationKey == "finance" || it.destinationKey == "transparency" || it.destinationKey == "treasurer_report"
                                        }
                                        if (idx != -1) {
                                            currentTabIndex = idx
                                        } else {
                                            overlaySubModule = if (dialogKey == "finance" && currentRole.canManageFinance()) "finance" else "transparency"
                                        }
                                    }
                                    "pending_approval" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "finance" }
                                        if (idx != -1) {
                                            currentTabIndex = idx
                                            openFinancePendingRequest++
                                        }
                                    }
                                    "notices", "view_notices" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "notices" }
                                        if (idx != -1) currentTabIndex = idx else overlaySubModule = "notices"
                                    }
                                    "my_chanda", "view_receipt" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "my_chanda" }
                                        if (idx != -1) currentTabIndex = idx else overlaySubModule = "chanda"
                                    }
                                    "roster" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "roster" }
                                        if (idx != -1) currentTabIndex = idx else overlaySubModule = "roster"
                                    }
                                    "food" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "food" }
                                        if (idx != -1) currentTabIndex = idx else overlaySubModule = "food"
                                    }
                                    "madrasa" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "madrasa" }
                                        if (idx != -1) currentTabIndex = idx else overlaySubModule = "madrasa"
                                    }
                                    "madrasa_fee" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "treasurer_payroll" || it.destinationKey == "madrasa" }
                                        if (idx != -1) currentTabIndex = idx else overlaySubModule = "madrasa"
                                    }
                                    "staff_profile" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "staff_profile" }
                                        if (idx != -1) currentTabIndex = idx else {
                                            overlaySubModule = null
                                            currentUserProfileTarget?.let { overlayUserProfile = it }
                                        }
                                    }
                                    "mosque_profile", "about" -> {
                                        overlayUserProfile = null
                                        overlaySubModule = "about"
                                    }
                                    "my_profile" -> {
                                        overlaySubModule = null
                                        currentUserProfileTarget?.let { overlayUserProfile = it }
                                    }
                                    "welfare", "welfare_apply" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "welfare" || it.destinationKey == "secretary_welfare" }
                                        if (idx != -1) currentTabIndex = idx else overlaySubModule = "welfare"
                                    }
                                    "meetings" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "secretary_meetings" }
                                        if (idx != -1) currentTabIndex = idx else overlaySubModule = "meetings"
                                    }
                                    "events" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "events" }
                                        if (idx != -1) currentTabIndex = idx else overlaySubModule = "events"
                                    }
                                    "charter" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "charter" }
                                        if (idx != -1) currentTabIndex = idx else overlaySubModule = "charter"
                                    }
                                    "cashbox" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "cashbox" }
                                        if (idx != -1) currentTabIndex = idx else overlaySubModule = "cashbox"
                                    }
                                    "office_docs" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "office_docs" }
                                        if (idx != -1) currentTabIndex = idx else overlaySubModule = "office_docs"
                                    }
                                    "sharia_advisory" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "sharia_advisory" }
                                        if (idx != -1) currentTabIndex = idx else overlaySubModule = "sharia_advisory"
                                    }
                                    "publicity" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "publicity" }
                                        if (idx != -1) currentTabIndex = idx else overlaySubModule = "publicity"
                                    }
                                    "audit" -> {
                                        overlaySubModule = "audit"
                                    }
                                    "view_prayers", "prayers", "prayer" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "prayer" }
                                        if (idx != -1) currentTabIndex = idx else overlaySubModule = "prayer"
                                    }
                                    "treasurer_payroll" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "treasurer_payroll" }
                                        if (idx != -1) currentTabIndex = idx else overlaySubModule = "staff"
                                    }
                                    "treasurer_report" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "treasurer_report" || it.destinationKey == "finance" }
                                        if (idx != -1) {
                                            currentTabIndex = idx
                                            openFinanceReportRequest++
                                        }
                                    }
                                    "treasurer_chanda" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "treasurer_chanda" }
                                        if (idx != -1) currentTabIndex = idx else overlaySubModule = "chanda"
                                    }
                                    "election" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "election" }
                                        if (idx != -1) currentTabIndex = idx else overlaySubModule = "election"
                                    }
                                    "islamic_calendar" -> {
                                        overlaySubModule = "islamic_calendar"
                                    }
                                    "ramadan_hub" -> {
                                        showRamadanHubDialog = true
                                    }
                                    "cloud_sync", "supabase_sync", "sync" -> {
                                        showCloudSyncDialog = true
                                    }
                                    "invitation", "invitation_poster", "dawatnama" -> {
                                        showInvitationPosterDialog = true
                                    }
                                    "cashbox_counter", "jummah_cashbox" -> {
                                        showCashboxCounterDialog = true
                                    }
                                    "janaza", "janaza_alert" -> {
                                        showJanazaAlertDialog = true
                                    }
                                    "official_seals", "seals", "signatures" -> {
                                        showOfficialSealsDialog = true
                                    }
                                }
                            }
                        )
                "members" -> MembersScreen(
                    viewModel = viewModel,
                    onAddMemberClick = if (currentRole.canManageMembers()) { { showAddMemberDialog = true } } else null
                )
                "committee" -> MembersScreen(
                    viewModel = viewModel,
                    onAddMemberClick = null
                )
                "finance" -> FinanceScreen(
                    viewModel = viewModel,
                    onAddIncomeClick = if (currentRole.canManageFinance()) {
                        {
                            transactionInitialType = "INCOME"
                            transactionInitialCategory = null
                            showAddTransactionDialog = true
                        }
                    } else null,
                    onAddExpenseClick = if (currentRole.canManageFinance()) {
                        {
                            transactionInitialType = "EXPENSE"
                            transactionInitialCategory = null
                            showAddTransactionDialog = true
                        }
                    } else null,
                    isReadOnlyTransparencyMode = false,
                    openReportTrigger = openFinanceReportRequest,
                    onReportTriggerConsumed = { openFinanceReportRequest = 0 },
                    openPendingTabTrigger = openFinancePendingRequest,
                    onPendingTabTriggerConsumed = { openFinancePendingRequest = 0 }
                )
                "transparency" -> FinanceScreen(
                    viewModel = viewModel,
                    onAddIncomeClick = null,
                    onAddExpenseClick = null,
                    isReadOnlyTransparencyMode = true
                )
                "treasurer_chanda" -> MoreScreen(
                    viewModel = viewModel,
                    initialSubModule = "chanda",
                    onOpenReceiveChanda = { showReceiveChandaDialog = true },
                    onOpenScheduleMeeting = { showScheduleMeetingDialog = true }
                )
                "treasurer_payroll" -> MoreScreen(
                    viewModel = viewModel,
                    initialSubModule = "staff",
                    onOpenReceiveChanda = { showReceiveChandaDialog = true },
                    onOpenScheduleMeeting = { showScheduleMeetingDialog = true }
                )
                "treasurer_report" -> FinanceScreen(
                    viewModel = viewModel,
                    onAddIncomeClick = {
                        transactionInitialType = "INCOME"
                        transactionInitialCategory = null
                        showAddTransactionDialog = true
                    },
                    onAddExpenseClick = {
                        transactionInitialType = "EXPENSE"
                        transactionInitialCategory = null
                        showAddTransactionDialog = true
                    },
                    isReadOnlyTransparencyMode = false
                )
                "secretary_meetings" -> MoreScreen(
                    viewModel = viewModel,
                    initialSubModule = "meetings",
                    onOpenReceiveChanda = { showReceiveChandaDialog = true },
                    onOpenScheduleMeeting = { showScheduleMeetingDialog = true }
                )
                "secretary_welfare" -> MoreScreen(
                    viewModel = viewModel,
                    initialSubModule = "welfare",
                    onOpenReceiveChanda = { showReceiveChandaDialog = true },
                    onOpenScheduleMeeting = { showScheduleMeetingDialog = true }
                )
                "notices" -> NoticesScreen(
                    viewModel = viewModel,
                    onPublishNoticeClick = { showPublishNoticeDialog = true },
                    onScheduleMeetingClick = if (currentRole.canScheduleMeetings()) {
                        { showScheduleMeetingDialog = true }
                    } else null,
                    onOpenInvitationPosterClick = { showInvitationPosterDialog = true },
                    onOpenJanazaAlertClick = { showJanazaAlertDialog = true }
                )
                "more" -> MoreScreen(
                    viewModel = viewModel,
                    initialSubModule = null,
                    onOpenReceiveChanda = { showReceiveChandaDialog = true },
                    onOpenScheduleMeeting = { showScheduleMeetingDialog = true },
                    onOpenPublishNotice = { showPublishNoticeDialog = true },
                    onOpenAddMember = { showAddMemberDialog = true },
                    onOpenMyProfile = {
                        currentUserProfileTarget?.let { overlayUserProfile = it }
                    }
                )
                "roster" -> MoreScreen(
                    viewModel = viewModel,
                    initialSubModule = "roster",
                    onOpenReceiveChanda = { showReceiveChandaDialog = true },
                    onOpenScheduleMeeting = { showScheduleMeetingDialog = true }
                )
                "food" -> MoreScreen(
                    viewModel = viewModel,
                    initialSubModule = "food",
                    onOpenReceiveChanda = { showReceiveChandaDialog = true },
                    onOpenScheduleMeeting = { showScheduleMeetingDialog = true }
                )
                "madrasa" -> MoreScreen(
                    viewModel = viewModel,
                    initialSubModule = "madrasa",
                    onOpenReceiveChanda = { showReceiveChandaDialog = true },
                    onOpenScheduleMeeting = { showScheduleMeetingDialog = true }
                )
                "staff_profile" -> {
                    val selfTarget = currentUserProfileTarget
                    if (selfTarget != null) {
                        MemberProfileScreen(
                            target = selfTarget,
                            viewModel = viewModel,
                            activeCycleMonth = activeCycleMonth,
                            isOwnProfile = true,
                            showBackButton = false,
                            onOpenMosqueProfile = { overlaySubModule = "about" },
                            onBack = { currentTabIndex = 0 },
                            onOpenReceiveChanda = { member, suggested ->
                                chandaPreselectedTarget = member to suggested
                                showReceiveChandaDialog = true
                            },
                            onOpenPaySalary = { staff ->
                                paySalaryTargetStaff = staff
                            }
                        )
                    } else {
                        MoreScreen(
                            viewModel = viewModel,
                            initialSubModule = "staff",
                            onOpenReceiveChanda = { showReceiveChandaDialog = true },
                            onOpenScheduleMeeting = { showScheduleMeetingDialog = true }
                        )
                    }
                }
                "my_chanda" -> MoreScreen(
                    viewModel = viewModel,
                    initialSubModule = "chanda",
                    onOpenReceiveChanda = { showReceiveChandaDialog = true },
                    onOpenScheduleMeeting = { showScheduleMeetingDialog = true }
                )
                "welfare" -> MoreScreen(
                    viewModel = viewModel,
                    initialSubModule = "welfare",
                    onOpenReceiveChanda = { showReceiveChandaDialog = true },
                    onOpenScheduleMeeting = { showScheduleMeetingDialog = true }
                )
                "charter", "cashbox", "office_docs", "sharia_advisory", "publicity", "events", "prayer", "islamic_calendar", "ramadan_hub" -> MoreScreen(
                    viewModel = viewModel,
                    initialSubModule = destination,
                    onOpenReceiveChanda = { showReceiveChandaDialog = true },
                    onOpenScheduleMeeting = { showScheduleMeetingDialog = true }
                )
                "profile" -> AccountHubScreen(
                    viewModel = viewModel,
                    mosque = mosque,
                    onBack = { currentTabIndex = 0 },
                    onOpenMosqueProfile = {
                        overlaySubModule = "about"
                    },
                    onOpenMyProfile = {
                        currentUserProfileTarget?.let { overlayUserProfile = it }
                    },
                    onOpenOnboarding = {
                        onOpenOnboarding()
                    },
                    onLogout = onLogout
                )
                else -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToTab = { currentTabIndex = 0 },
                    onOpenDialog = {}
                )
            }
        }
    }
    }
    }
    }
    }


    // Add Member Dialog
    if (showAddMemberDialog) {
        AddMemberDialog(
            defaultMosqueAddress = mosque?.address ?: "",
            onDismiss = { showAddMemberDialog = false },
            onConfirm = { nameBn, nameEn, mobile, email, nid, address, villageArea, roleCode, monthlyChanda, photoUrl, willingToProvideStaffMeals, salaryAmount, staffMealRequired ->
                viewModel.addMember(
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
                    willingToProvideStaffMeals = willingToProvideStaffMeals,
                    salaryAmount = salaryAmount,
                    staffMealRequired = staffMealRequired
                )
                MosqueToastController.success(
                    "$nameBn-কে সদস্য তালিকায় যুক্ত করা হয়েছে।",
                    "সদস্য এন্ট্রি সম্পন্ন"
                )
            }
        )
    }

    // Add Transaction Dialog
    if (showAddTransactionDialog) {
        AddTransactionDialog(
            initialType = transactionInitialType,
            initialCategory = transactionInitialCategory,
            onDismiss = {
                showAddTransactionDialog = false
                transactionInitialCategory = null
            },
            onConfirm = { category, type, amount, description, method ->
                viewModel.addTransaction(
                    categoryNameBn = category,
                    type = type,
                    amount = amount,
                    description = description,
                    paymentMethod = method
                )
                val typeLabel = if (type == "INCOME") "আয়" else "ব্যয়"
                MosqueToastController.success(
                    "৳${toBengaliDigits(amount.toInt().toString())} $typeLabel এন্ট্রি সংরক্ষিত হয়েছে।",
                    "হিসাব এন্ট্রি সফল"
                )
            }
        )
    }

    // Receive Chanda Dialog
    if (showReceiveChandaDialog) {
        ReceiveChandaDialog(
            members = allMembers,
            allPayments = allChandaPayments,
            preselectedMember = chandaPreselectedTarget?.first,
            defaultMonth = activeCycleMonth,
            suggestedAmount = chandaPreselectedTarget?.second,
            onDismiss = {
                showReceiveChandaDialog = false
                chandaPreselectedTarget = null
            },
            onConfirm = { member, month, amount, method, monthCount ->
                viewModel.recordChandaPayment(
                    member = member,
                    month = month,
                    amount = amount,
                    method = method,
                    selectedMonthCount = monthCount,
                    onRecorded = { createdPayment ->
                        chandaReceiptPreview = createdPayment
                        MosqueToastController.success(
                            "৳${toBengaliDigits(amount.toInt().toString())} চাঁদা গৃহীত হয়েছে এবং আয়ে যুক্ত হয়েছে।",
                            "চাঁদা রসিদ প্রস্তুত"
                        )
                    }
                )
                chandaPreselectedTarget = null
            }
        )
    }

    // Pay Salary Dialog (triggered from profile)
    paySalaryTargetStaff?.let { staff ->
        PaySalaryFormDialog(
            staff = staff,
            defaultMonth = activeCycleMonth,
            onDismiss = { paySalaryTargetStaff = null },
            onConfirm = { month, bonus, paymentMethod ->
                viewModel.disburseSalary(
                    staff = staff,
                    month = month,
                    bonus = bonus,
                    paymentMethod = paymentMethod,
                    onDisbursed = { payroll ->
                        MosqueToastController.success(
                            "${staff.nameBn}-এর বেতন ৳${toBengaliDigits(payroll.netSalary.toInt().toString())} পরিশোধিত হয়েছে।",
                            "বেতন পরিশোধ সফল"
                        )
                    }
                )
                paySalaryTargetStaff = null
            }
        )
    }

    chandaReceiptPreview?.let { payment ->
        val treasurer = viewModel.getTreasurerMember()
        OfficialReceiptDialog(
            payment = payment,
            mosqueName = mosque?.nameBn ?: "মসজিদ সেবা",
            mosqueAddress = mosque?.address ?: "",
            committeeId = mosque?.committeeId ?: "",
            mosqueLogoUrl = mosque?.logoUrl,
            treasurerSignatureUrl = treasurer?.signatureUrl,
            treasurerStampUrl = treasurer?.stampUrl,
            mosqueSealUrl = mosque?.sealUrl,
            onDismiss = { chandaReceiptPreview = null }
        )
    }

    // Publish Notice Dialog
    if (showPublishNoticeDialog) {
        PublishNoticeDialog(
            aiNoticeState = aiNoticeState,
            onGenerateAiDraft = { instruction -> viewModel.draftNoticeWithAi(instruction) },
            onClearAiDraft = { viewModel.clearAiNoticeState() },
            onDismiss = { showPublishNoticeDialog = false },
            onConfirm = { title, content, priority, isPinned, imageUrl ->
                viewModel.publishNotice(
                    title = title,
                    content = content,
                    priority = priority,
                    isPinned = isPinned,
                    imageUrl = imageUrl
                )
                MosqueToastController.success(
                    "নোটিশটি সফলভাবে প্রকাশিত হয়েছে।",
                    "নোটিশ প্রকাশিত"
                )
            }
        )
    }

    // Schedule Meeting Dialog
    if (showScheduleMeetingDialog) {
        ScheduleMeetingDialog(
            onDismiss = { showScheduleMeetingDialog = false },
            onConfirm = { title, dateStr, location, agenda ->
                viewModel.scheduleMeeting(
                    title = title,
                    dateStr = dateStr,
                    location = location,
                    agenda = agenda
                )
                MosqueToastController.success(
                    "কমিটি সভা সফলভাবে নির্ধারিত হয়েছে।",
                    "সভা নির্ধারিত"
                )
            }
        )
    }

    // Holy Ramadan Hub Dialog
    if (showRamadanHubDialog) {
        RamadanHubDialog(
            viewModel = viewModel,
            onDismiss = { showRamadanHubDialog = false }
        )
    }

    // Cloud Database & Supabase Sync Dialog
    if (showCloudSyncDialog) {
        CloudSyncDialog(
            viewModel = viewModel,
            onDismiss = { showCloudSyncDialog = false }
        )
    }

    // Islamic Dawatnama & Event Poster AI Dialog
    if (showInvitationPosterDialog) {
        val president = viewModel.getPresidentMember()
        IslamicDawatnamaDialog(
            defaultMosqueName = mosque?.nameBn ?: "মসজিদ সেবা",
            defaultMosqueAddress = mosque?.address ?: "বাংলাদেশ",
            presidentSignatureUrl = president?.signatureUrl,
            presidentStampUrl = president?.stampUrl,
            mosqueSealUrl = mosque?.sealUrl,
            aiState = aiDawatnamaState,
            onPolishWithAi = { eventType, title, chiefGuest, date, time, venue ->
                viewModel.generateDawatnamaDescription(
                    mosqueName = mosque?.nameBn ?: "মসজিদ সেবা",
                    mosqueAddress = mosque?.address ?: "বাংলাদেশ",
                    eventType = eventType,
                    title = title,
                    chiefGuest = chiefGuest,
                    date = date,
                    time = time,
                    venue = venue
                )
            },
            onClearAiState = { viewModel.clearAiDawatnamaState() },
            onPublishAsNotice = { title, content ->
                viewModel.publishNotice(
                    title = title,
                    content = content,
                    priority = "IMPORTANT",
                    isPinned = true,
                    imageUrl = null
                )
                MosqueToastController.success("দাওয়াতনামাটি নোটিশ বোর্ডে প্রকাশিত হয়েছে।", "নোটিশ প্রকাশিত")
            },
            onDismiss = { showInvitationPosterDialog = false }
        )
    }

    // Jummah Cashbox Dual-Witness Counter Dialog
    if (showCashboxCounterDialog) {
        val imam = viewModel.getImamStaff()
        val treasurer = viewModel.getTreasurerMember()
        JummahCashboxDialog(
            mosqueName = mosque?.nameBn ?: "মসজিদ সেবা",
            imamSignatureUrl = imam?.signatureUrl,
            treasurerSignatureUrl = treasurer?.signatureUrl,
            mosqueSealUrl = mosque?.sealUrl,
            onConfirm = { totalAmount, breakdownText, witness1, witness2, witness3, notes ->
                viewModel.recordJummahCashboxCount(
                    countedAmount = totalAmount,
                    denominationBreakdown = breakdownText,
                    witness1 = witness1,
                    witness2 = witness2,
                    witness3 = witness3,
                    notes = notes,
                    autoAddIncome = true
                )
            },
            onDismiss = { showCashboxCounterDialog = false }
        )
    }

    // Emergency Janaza Alert Broadcast Dialog
    if (showJanazaAlertDialog) {
        val president = viewModel.getPresidentMember()
        JanazaAlertDialog(
            mosqueName = mosque?.nameBn ?: "মসজিদ সেবা",
            presidentSignatureUrl = president?.signatureUrl,
            presidentStampUrl = president?.stampUrl,
            mosqueSealUrl = mosque?.sealUrl,
            onBroadcastConfirm = { deceasedName, relativeRelation, guardianName, age, villageArea, timeOfDeath, janazaVenue, janazaTime, graveyardVenue, contactPhone, extraNotes ->
                viewModel.broadcastJanazaAlert(
                    deceasedName = deceasedName,
                    relativeRelation = relativeRelation,
                    guardianName = guardianName,
                    age = age,
                    villageArea = villageArea,
                    timeOfDeath = timeOfDeath,
                    janazaVenue = janazaVenue,
                    janazaTime = janazaTime,
                    graveyardVenue = graveyardVenue,
                    contactPhone = contactPhone,
                    extraNotes = extraNotes
                )
            },
            onDismiss = { showJanazaAlertDialog = false }
        )
    }

    // Official Seals & Digital Signatures Management Dialog
    if (showOfficialSealsDialog) {
        OfficialSealSignatureDialog(
            viewModel = viewModel,
            onDismiss = { showOfficialSealsDialog = false }
        )
    }

    // Official App Update Dialog
    val availableUpdate by viewModel.availableUpdate.collectAsState()
    if (availableUpdate != null && availableUpdate?.isUpdateAvailable == true) {
        AppUpdateDialog(
            updateInfo = availableUpdate!!,
            onDismiss = {
                viewModel.dismissUpdateDialog(availableUpdate!!.latestVersionName)
            }
        )
    }
}
