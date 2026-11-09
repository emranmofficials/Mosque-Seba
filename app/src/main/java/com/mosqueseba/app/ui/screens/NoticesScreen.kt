package com.mosqueseba.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.data.local.entity.NoticeEntity
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel

@Composable
fun NoticesScreen(
    viewModel: MosqueViewModel,
    onPublishNoticeClick: () -> Unit
) {
    val notices by viewModel.allNotices.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()
    val canPublish = currentRole in listOf(
        UserRole.PRESIDENT,
        UserRole.VICE_PRESIDENT,
        UserRole.GENERAL_SECRETARY,
        UserRole.ORGANIZING_SECRETARY
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MosqueBgLight)
    ) {
        // Sticky Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "মসজিদ নোটিশ ও ঘোষণা বোর্ড",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldDark
                )
                Text(
                    text = "মুসল্লি ও কমিটির জন্য গুরুত্বপূর্ণ তথ্যাবলী",
                    style = MaterialTheme.typography.bodySmall,
                    color = MosqueTextSecondary
                )
            }

            if (canPublish) {
                Button(
                    onClick = onPublishNoticeClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("নোটিশ প্রকাশ", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Notices List
        LazyColumn(
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (notices.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "বর্তমানে কোনো সক্রিয় নোটিশ নেই",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MosqueTextMuted
                        )
                    }
                }
            } else {
                items(notices, key = { it.id }) { notice ->
                    NoticeCardItem(
                        notice = notice,
                        canDelete = canPublish,
                        onDelete = { viewModel.deleteNotice(notice) }
                    )
                }
            }
        }
    }
}

@Composable
fun NoticeCardItem(
    notice: NoticeEntity,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    val priorityColor = when (notice.priority) {
        "urgent" -> MosqueUrgentRed
        "important" -> MosqueWarningAmber
        else -> MosqueEmeraldLight
    }
    val priorityLabel = when (notice.priority) {
        "urgent" -> "জরুরি"
        "important" -> "গুরুত্বপূর্ণ"
        else -> "সাধারণ"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, if (notice.priority == "urgent") MosqueUrgentRed.copy(alpha = 0.35f) else MosqueCardBorderSubtle)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Priority & Pinned Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Priority Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(priorityColor.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = priorityLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (notice.isPinned) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MosqueGoldLight)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = null,
                                tint = MosqueGoldDeep,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "শীর্ষে পিন করা",
                                style = MaterialTheme.typography.labelSmall,
                                color = MosqueEmeraldDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Notice",
                            tint = MosqueTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Notice Title
            Text(
                text = notice.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MosqueTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Notice Content
            Text(
                text = notice.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MosqueTextSecondary,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MosqueCardBorder.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(6.dp))

            // Footer metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MosqueTextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "প্রকাশ: ${notice.publishDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MosqueTextMuted,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = "প্রচারে: ${notice.createdBy}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MosqueEmeraldDark,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                )
            }
        }
    }
}
