package com.mosqueseba.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mosqueseba.app.data.local.entity.MemberEntity
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.components.KpiCardItem
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel

@Composable
fun MembersScreen(
    viewModel: MosqueViewModel,
    onAddMemberClick: (() -> Unit)? = null
) {
    val members by viewModel.filteredMembers.collectAsState()
    val totalCount by viewModel.memberCount.collectAsState()
    val activeCount by viewModel.activeMemberCount.collectAsState()
    val searchQuery by viewModel.memberSearchQuery.collectAsState()
    val selectedRoleFilter by viewModel.selectedRoleFilter.collectAsState()

    var selectedMemberForDetail by remember { mutableStateOf<MemberEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MosqueBgLight)
    ) {
        // Sticky Top Section: KPIs and Search
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(14.dp)
        ) {
            // KPI exactly 3 compact cards (PRD §7)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiCardItem(
                    modifier = Modifier.weight(1f),
                    title = "মোট সদস্য",
                    value = "$totalCount জন",
                    subtext = "নিবন্ধিত",
                    icon = Icons.Default.People,
                    iconBg = MosqueEmeraldPrimary.copy(alpha = 0.1f),
                    iconColor = MosqueEmeraldPrimary
                )
                KpiCardItem(
                    modifier = Modifier.weight(1f),
                    title = "সক্রিয় সদস্য",
                    value = "$activeCount জন",
                    subtext = "নিয়মিত",
                    icon = Icons.Default.CheckCircle,
                    iconBg = MosqueSuccessGreen.copy(alpha = 0.1f),
                    iconColor = MosqueSuccessGreen
                )
                KpiCardItem(
                    modifier = Modifier.weight(1f),
                    title = "বকেয়া চাঁদা",
                    value = "৳৩,৫০০",
                    subtext = "চলতি মাস",
                    icon = Icons.Default.PendingActions,
                    iconBg = MosqueWarningAmber.copy(alpha = 0.1f),
                    iconColor = MosqueWarningAmber
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar with exact placeholder "সদস্য খুঁজুন (নাম, মোবাইল, আইডি)"
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setMemberSearchQuery(it) },
                placeholder = { Text("সদস্য খুঁজুন (নাম, মোবাইল, আইডি)", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MosqueTextMuted
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setMemberSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MosqueTextMuted)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MosqueEmeraldPrimary,
                    unfocusedBorderColor = MosqueCardBorder
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips + Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        FilterChip(
                            selected = selectedRoleFilter == null,
                            onClick = { viewModel.setSelectedRoleFilter(null) },
                            label = { Text("সকল") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedRoleFilter == "president",
                            onClick = { viewModel.setSelectedRoleFilter(if (selectedRoleFilter == "president") null else "president") },
                            label = { Text("সভাপতি") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedRoleFilter == "general_secretary",
                            onClick = { viewModel.setSelectedRoleFilter(if (selectedRoleFilter == "general_secretary") null else "general_secretary") },
                            label = { Text("সম্পাদক") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedRoleFilter == "treasurer",
                            onClick = { viewModel.setSelectedRoleFilter(if (selectedRoleFilter == "treasurer") null else "treasurer") },
                            label = { Text("কোষাধ্যক্ষ") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedRoleFilter == "general_member",
                            onClick = { viewModel.setSelectedRoleFilter(if (selectedRoleFilter == "general_member") null else "general_member") },
                            label = { Text("সাধারণ সদস্য") }
                        )
                    }
                }

                if (onAddMemberClick != null) {
                    Spacer(modifier = Modifier.width(8.dp))

                    // Context Action: Add Member
                    Button(
                        onClick = onAddMemberClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "নতুন সদস্য",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Member List
        LazyColumn(
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (members.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = MosqueTextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "কোনো সদস্য পাওয়া যায়নি",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MosqueTextMuted
                            )
                        }
                    }
                }
            } else {
                items(members, key = { it.id }) { member ->
                    MemberCard(
                        member = member,
                        onClick = { selectedMemberForDetail = member }
                    )
                }
            }
        }
    }

    // Member Details Dialog
    selectedMemberForDetail?.let { member ->
        MemberDetailDialog(
            member = member,
            onDismiss = { selectedMemberForDetail = null }
        )
    }
}

@Composable
fun MemberCard(
    member: MemberEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 5.dp),
        border = BorderStroke(1.dp, MosqueCardBorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with initials
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MosqueEmeraldLight.copy(alpha = 0.15f))
                    .border(1.dp, MosqueEmeraldPrimary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.nameBn.firstOrNull()?.toString() ?: "ম",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldDark
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = member.nameBn,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MosqueTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Role Badge
                    val role = UserRole.fromCode(member.roleCode)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (role.level <= 2) MosqueGoldLight else MosqueBgLight)
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = role.titleBn,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (role.level <= 2) MosqueEmeraldDark else MosqueTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "আইডি: ${member.memberCode} • ${member.mobile}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MosqueTextSecondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ঠিকানা: ${member.address}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MosqueTextMuted,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    // Monthly Chanda Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MosqueSuccessGreen.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "মাসিক ৳${member.monthlyChanda.toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MosqueSuccessGreen,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MosqueTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun MemberDetailDialog(
    member: MemberEntity,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Avatar
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MosqueEmeraldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.nameBn.firstOrNull()?.toString() ?: "ম",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = member.nameBn,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MosqueTextPrimary
                )
                Text(
                    text = member.nameEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MosqueTextSecondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                val role = UserRole.fromCode(member.roleCode)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MosqueGoldLight)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = role.titleBn,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MosqueEmeraldDark
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                DetailItemRow("সদস্য কোড", member.memberCode)
                DetailItemRow("মোবাইল", member.mobile)
                DetailItemRow("ইমেইল", member.email.ifBlank { "প্রযোজ্য নয়" })
                DetailItemRow("জাতীয় পরিচয়পত্র", member.nid)
                DetailItemRow("বাসার ঠিকানা", member.address)
                DetailItemRow("মাসিক চাঁদার অঙ্গীকার", "৳${member.monthlyChanda.toInt()}")
                DetailItemRow("যোগদানের তারিখ", member.joinDate)
                DetailItemRow("বর্তমান স্থিতি", if (member.status == "active") "সক্রিয় সদস্য" else "নিষ্ক্রিয়")

                Spacer(modifier = Modifier.height(18.dp))

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

@Composable
fun DetailItemRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MosqueTextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MosqueTextPrimary
        )
    }
}
