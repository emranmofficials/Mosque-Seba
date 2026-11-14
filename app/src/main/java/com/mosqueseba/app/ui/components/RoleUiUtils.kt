package com.mosqueseba.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.theme.*

fun UserRole.roleIcon(): ImageVector = when (this) {
    UserRole.PRESIDENT -> Icons.Default.WorkspacePremium
    UserRole.VICE_PRESIDENT -> Icons.Default.MilitaryTech
    UserRole.GENERAL_SECRETARY -> Icons.Default.EditNote
    UserRole.ASSISTANT_SECRETARY -> Icons.Default.FolderShared
    UserRole.TREASURER -> Icons.Default.AccountBalanceWallet
    UserRole.ASSISTANT_TREASURER -> Icons.Default.AccountBalance
    UserRole.ORGANIZING_SECRETARY -> Icons.Default.Handshake
    UserRole.PUBLICITY_SECRETARY -> Icons.Default.Campaign
    UserRole.OFFICE_SECRETARY -> Icons.Default.Inventory2
    UserRole.WELFARE_EDUCATION_SECRETARY -> Icons.Default.School
    UserRole.EXECUTIVE_MEMBER -> Icons.Default.VerifiedUser
    UserRole.IMAM -> Icons.Default.Mosque
    UserRole.ADVISORY_BOARD -> Icons.Default.SupervisorAccount
    UserRole.MUAZZIN -> Icons.Default.RecordVoiceOver
    UserRole.KHADEM -> Icons.Default.VolunteerActivism
    UserRole.GENERAL_MEMBER -> Icons.Default.Groups
}

@Composable
fun MosqueConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String = "বাতিল",
    isDestructive: Boolean = true,
    icon: ImageVector = if (isDestructive) Icons.Default.WarningAmber else Icons.Default.HelpOutline,
    confirmTag: String = "confirm_action_button",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val accentColor = if (isDestructive) MosqueUrgentRed else MosqueEmeraldPrimary
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        MehrabArchCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .padding(horizontal = 20.dp),
            backgroundColor = Color.White,
            borderColor = if (isDestructive) MosqueUrgentRed.copy(alpha = 0.45f) else MosqueCardBorderGold,
            elevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MosqueEmeraldDarkGradient)
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isDestructive) MosqueUrgentRed.copy(alpha = 0.25f) else MosqueGold.copy(alpha = 0.22f),
                            border = BorderStroke(1.dp, if (isDestructive) MosqueGoldLight else MosqueGold),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MosqueGoldLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(IslamicGoldTrimGradient)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDestructive) MosqueUrgentRed.copy(alpha = 0.06f) else MosqueBgLight,
                        border = BorderStroke(
                            1.dp,
                            if (isDestructive) MosqueUrgentRed.copy(alpha = 0.22f) else MosqueCardBorderGold
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MosqueTextPrimary,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = dismissText,
                                color = MosqueTextSecondary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Button(
                            onClick = onConfirm,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            modifier = Modifier
                                .weight(1.2f)
                                .heightIn(min = 48.dp)
                                .testTag(confirmTag)
                        ) {
                            Text(
                                text = confirmText,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
