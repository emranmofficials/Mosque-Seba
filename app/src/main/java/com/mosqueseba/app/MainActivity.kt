package com.mosqueseba.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontWeight
import com.mosqueseba.app.data.local.entity.MeetingEntity
import com.mosqueseba.app.data.local.entity.MemberEntity
import com.mosqueseba.app.data.local.entity.NoticeEntity
import com.mosqueseba.app.data.local.entity.TransactionEntity
import com.mosqueseba.app.ui.components.*
import com.mosqueseba.app.ui.screens.*
import com.mosqueseba.app.ui.theme.MosqueCommitteeTheme
import androidx.activity.compose.BackHandler
import com.mosqueseba.app.ui.theme.MosqueEmeraldDark
import com.mosqueseba.app.ui.theme.MosqueEmeraldPrimary
import com.mosqueseba.app.ui.theme.MosqueTextMuted
import com.mosqueseba.app.ui.theme.MosqueTextSecondary
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel

enum class AppScreen {
    SPLASH,
    LOGIN,
    REGISTRATION,
    MAIN
}

class MainActivity : ComponentActivity() {

    private val viewModel: MosqueViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge()

        setContent {
            MosqueCommitteeTheme {
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
                                onLogout = {
                                    viewModel.logout()
                                    currentScreen = AppScreen.LOGIN
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun MosqueAppShell(
    viewModel: MosqueViewModel,
    onLogout: () -> Unit = {}
) {
    var currentTabIndex by remember { mutableIntStateOf(0) }
    var overlaySubModule by remember { mutableStateOf<String?>(null) }
    val currentRole by viewModel.currentUserRole.collectAsState()
    val mosque by viewModel.mosque.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()
    val aiNoticeState by viewModel.aiNoticeState.collectAsState()
    val pendingTransactions by viewModel.pendingTransactions.collectAsState()
    val allNotices by viewModel.allNotices.collectAsState()
    val allMeetings by viewModel.allMeetings.collectAsState()

    val savedRole = remember(currentRole) { viewModel.sessionManager.getSavedRole() }
    val savedName = remember(currentRole) { viewModel.sessionManager.getSavedMemberName() }
    val savedMemberId = remember(currentRole) { viewModel.sessionManager.getSavedMemberId() }
    val currentMember = remember(currentRole, allMembers, savedRole, savedMemberId) {
        allMembers.firstOrNull { it.id == savedMemberId && currentRole == savedRole }
            ?: allMembers.firstOrNull { it.roleCode.equals(currentRole.code, ignoreCase = true) }
    }
    val currentMemberName = remember(currentRole, currentMember, savedRole, savedName) {
        if (currentRole == savedRole && savedName.isNotBlank() && savedName != "সদস্য") {
            savedName
        } else {
            currentMember?.nameBn
        }
    }

    val upcomingMeetings = remember(allMeetings) { allMeetings.filter { it.status == "upcoming" } }
    val recentNotices = remember(allNotices) { allNotices.take(2) }
    val notificationCount = pendingTransactions.size + recentNotices.size + upcomingMeetings.size

    BackHandler(enabled = overlaySubModule != null || currentTabIndex != 0) {
        if (overlaySubModule != null) {
            overlaySubModule = null
        } else {
            currentTabIndex = 0
        }
    }

    // Reset tab and overlay whenever role switches
    LaunchedEffect(currentRole) {
        currentTabIndex = 0
        overlaySubModule = null
    }

    val currentNavItems = remember(currentRole) {
        getNavItemsForRole(currentRole)
    }

    val activeDestination = currentNavItems.getOrNull(currentTabIndex)?.destinationKey ?: "dashboard"

    // Dialog controllers
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var transactionInitialType by remember { mutableStateOf("INCOME") }
    var showReceiveChandaDialog by remember { mutableStateOf(false) }
    var showPublishNoticeDialog by remember { mutableStateOf(false) }
    var showScheduleMeetingDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            GlobalHeader(
                currentRole = currentRole,
                memberName = currentMemberName,
                mosqueLogoUrl = mosque?.logoUrl,
                userPhotoUrl = currentMember?.photoUrl,
                notificationCount = notificationCount,
                onRoleSelected = { viewModel.setUserRole(it) },
                onNotificationClick = { showNotificationDialog = true },
                onLogout = onLogout
            )
        },
        bottomBar = {
            NavigationBottomBar(
                currentTabIndex = currentTabIndex,
                currentRole = currentRole,
                onTabSelected = { 
                    currentTabIndex = it
                    overlaySubModule = null
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (overlaySubModule != null) {
                MoreScreen(
                    viewModel = viewModel,
                    initialSubModule = overlaySubModule,
                    onBack = { overlaySubModule = null },
                    onOpenReceiveChanda = { showReceiveChandaDialog = true },
                    onOpenScheduleMeeting = { showScheduleMeetingDialog = true }
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
                                            showAddTransactionDialog = true
                                        }
                                    }
                                    "add_expense" -> {
                                        if (currentRole.canManageFinance()) {
                                            transactionInitialType = "EXPENSE"
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
                                        if (idx != -1) currentTabIndex = idx
                                    }
                                    "finance", "transparency" -> {
                                        val idx = currentNavItems.indexOfFirst {
                                            it.destinationKey == "finance" || it.destinationKey == "transparency" || it.destinationKey == "treasurer_report"
                                        }
                                        if (idx != -1) currentTabIndex = idx
                                    }
                                    "pending_approval" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "finance" }
                                        if (idx != -1) currentTabIndex = idx
                                    }
                                    "notices", "view_notices" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "notices" }
                                        if (idx != -1) currentTabIndex = idx
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
                                        if (idx != -1) currentTabIndex = idx else overlaySubModule = "staff"
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
                                    "view_prayers", "prayers" -> {
                                        overlaySubModule = "prayer"
                                    }
                                    "treasurer_payroll" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "treasurer_payroll" }
                                        if (idx != -1) currentTabIndex = idx else overlaySubModule = "staff"
                                    }
                                    "treasurer_report" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "treasurer_report" || it.destinationKey == "finance" }
                                        if (idx != -1) currentTabIndex = idx
                                    }
                                    "treasurer_chanda" -> {
                                        val idx = currentNavItems.indexOfFirst { it.destinationKey == "treasurer_chanda" }
                                        if (idx != -1) currentTabIndex = idx else overlaySubModule = "chanda"
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
                            showAddTransactionDialog = true
                        }
                    } else null,
                    onAddExpenseClick = if (currentRole.canManageFinance()) {
                        {
                            transactionInitialType = "EXPENSE"
                            showAddTransactionDialog = true
                        }
                    } else null,
                    isReadOnlyTransparencyMode = false
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
                        showAddTransactionDialog = true
                    },
                    onAddExpenseClick = {
                        transactionInitialType = "EXPENSE"
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
                    onPublishNoticeClick = { showPublishNoticeDialog = true }
                )
                "more" -> MoreScreen(
                    viewModel = viewModel,
                    initialSubModule = null,
                    onOpenReceiveChanda = { showReceiveChandaDialog = true },
                    onOpenScheduleMeeting = { showScheduleMeetingDialog = true }
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
                "staff_profile" -> MoreScreen(
                    viewModel = viewModel,
                    initialSubModule = "staff",
                    onOpenReceiveChanda = { showReceiveChandaDialog = true },
                    onOpenScheduleMeeting = { showScheduleMeetingDialog = true }
                )
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
                "charter", "cashbox", "office_docs", "sharia_advisory", "publicity", "events" -> MoreScreen(
                    viewModel = viewModel,
                    initialSubModule = destination,
                    onOpenReceiveChanda = { showReceiveChandaDialog = true },
                    onOpenScheduleMeeting = { showScheduleMeetingDialog = true }
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


    // Add Member Dialog
    if (showAddMemberDialog) {
        AddMemberDialog(
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
            }
        )
    }

    // Add Transaction Dialog
    if (showAddTransactionDialog) {
        AddTransactionDialog(
            initialType = transactionInitialType,
            onDismiss = { showAddTransactionDialog = false },
            onConfirm = { category, type, amount, description ->
                viewModel.addTransaction(
                    categoryNameBn = category,
                    type = type,
                    amount = amount,
                    description = description
                )
            }
        )
    }

    // Receive Chanda Dialog
    if (showReceiveChandaDialog) {
        ReceiveChandaDialog(
            members = allMembers,
            onDismiss = { showReceiveChandaDialog = false },
            onConfirm = { member, month, amount, method ->
                viewModel.recordChandaPayment(
                    member = member,
                    month = month,
                    amount = amount,
                    method = method
                )
            }
        )
    }

    // Publish Notice Dialog
    if (showPublishNoticeDialog) {
        PublishNoticeDialog(
            aiNoticeState = aiNoticeState,
            onGenerateAiDraft = { instruction -> viewModel.draftNoticeWithAi(instruction) },
            onClearAiDraft = { viewModel.clearAiNoticeState() },
            onDismiss = { showPublishNoticeDialog = false },
            onConfirm = { title, content, priority, isPinned ->
                viewModel.publishNotice(
                    title = title,
                    content = content,
                    priority = priority,
                    isPinned = isPinned
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
            }
        )
    }

    // Notifications Dialog
    if (showNotificationDialog) {
        NotificationsDialog(
            pendingTransactions = pendingTransactions,
            recentNotices = recentNotices,
            upcomingMeetings = upcomingMeetings,
            onDismiss = { showNotificationDialog = false }
        )
    }
}

@Composable
fun NotificationsDialog(
    pendingTransactions: List<TransactionEntity>,
    recentNotices: List<NoticeEntity>,
    upcomingMeetings: List<MeetingEntity>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "বিজ্ঞপ্তি ও বার্তা (Notifications)",
                    style = MaterialTheme.typography.titleLarge,
                    color = MosqueEmeraldDark
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (pendingTransactions.isEmpty() && recentNotices.isEmpty() && upcomingMeetings.isEmpty()) {
                    Text(
                        text = "বর্তমানে কোনো নতুন বিজ্ঞপ্তি বা অপেক্ষমান কাজ নেই।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MosqueTextMuted
                    )
                } else {
                    pendingTransactions.forEach { txn ->
                        Text(
                            text = "• অনুমোদন বাকি: ${txn.categoryNameBn} (৳${String.format("%,.0f", txn.amount)}) — ${txn.createdBy}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    upcomingMeetings.forEach { meeting ->
                        Text(
                            text = "• আসন্ন সভা: ${meeting.title} (${meeting.meetingDate})",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    recentNotices.forEach { notice ->
                        Text(
                            text = "• নোটিশ: ${notice.title}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MosqueTextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                ) {
                    Text("বন্ধ করুন")
                }
            }
        }
    }
}
