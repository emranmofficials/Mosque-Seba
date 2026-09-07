package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.MemberEntity
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MosqueCommitteeTheme
import com.example.ui.theme.MosqueEmeraldDark
import com.example.ui.theme.MosqueEmeraldPrimary
import com.example.ui.viewmodel.MosqueViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MosqueViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MosqueCommitteeTheme {
                MosqueAppShell(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MosqueAppShell(viewModel: MosqueViewModel) {
    var currentTabIndex by remember { mutableIntStateOf(0) }
    val currentRole by viewModel.currentUserRole.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()

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
                onRoleSelected = { viewModel.setUserRole(it) },
                onNotificationClick = { showNotificationDialog = true }
            )
        },
        bottomBar = {
            NavigationBottomBar(
                currentTabIndex = currentTabIndex,
                onTabSelected = { currentTabIndex = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTabIndex) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToTab = { currentTabIndex = it },
                    onOpenDialog = { dialogKey ->
                        when (dialogKey) {
                            "add_member" -> showAddMemberDialog = true
                            "add_income" -> {
                                transactionInitialType = "INCOME"
                                showAddTransactionDialog = true
                            }
                            "add_expense" -> {
                                transactionInitialType = "EXPENSE"
                                showAddTransactionDialog = true
                            }
                            "receive_chanda" -> showReceiveChandaDialog = true
                            "schedule_meeting" -> showScheduleMeetingDialog = true
                            "publish_notice" -> showPublishNoticeDialog = true
                        }
                    }
                )
                1 -> MembersScreen(
                    viewModel = viewModel,
                    onAddMemberClick = { showAddMemberDialog = true }
                )
                2 -> FinanceScreen(
                    viewModel = viewModel,
                    onAddIncomeClick = {
                        transactionInitialType = "INCOME"
                        showAddTransactionDialog = true
                    },
                    onAddExpenseClick = {
                        transactionInitialType = "EXPENSE"
                        showAddTransactionDialog = true
                    }
                )
                3 -> NoticesScreen(
                    viewModel = viewModel,
                    onPublishNoticeClick = { showPublishNoticeDialog = true }
                )
                4 -> MoreScreen(
                    viewModel = viewModel,
                    onOpenReceiveChanda = { showReceiveChandaDialog = true },
                    onOpenScheduleMeeting = { showScheduleMeetingDialog = true }
                )
            }
        }
    }

    // Add Member Dialog
    if (showAddMemberDialog) {
        AddMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onConfirm = { nameBn, nameEn, mobile, email, nid, address, villageArea, roleCode, monthlyChanda ->
                viewModel.addMember(
                    nameBn = nameBn,
                    nameEn = nameEn,
                    mobile = mobile,
                    email = email,
                    nid = nid,
                    address = address,
                    villageArea = villageArea,
                    roleCode = roleCode,
                    monthlyChanda = monthlyChanda
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
        NotificationsDialog(onDismiss = { showNotificationDialog = false })
    }
}

@Composable
fun NotificationsDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "বিজ্ঞপ্তি ও বার্তা (Notifications)",
                    style = MaterialTheme.typography.titleLarge,
                    color = MosqueEmeraldDark
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "• জুমার দান বাক্সের নতুন লেনদেন অনুমোদনের জন্য অপেক্ষমান।",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• আগামী ১৫ সেপ্টেম্বর বাদ আসর কার্যকরী কমিটির জরুরি সাধারণ সভা আহ্বান করা হয়েছে।",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(14.dp))
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
