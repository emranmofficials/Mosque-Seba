package com.mosqueseba.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mosqueseba.app.MainActivity
import com.mosqueseba.app.R
import com.mosqueseba.app.data.local.entity.ChandaPaymentEntity
import com.mosqueseba.app.data.local.entity.ElectionScheduleEntity
import com.mosqueseba.app.data.local.entity.MeetingEntity
import com.mosqueseba.app.data.local.entity.NoticeEntity
import com.mosqueseba.app.ui.components.toBengaliDigits

/**
 * Android System Push Notification Helper for Mosque Seba.
 * Manages notification channels, builds cohesive Islamic status-bar notifications,
 * and handles deep-link routing intents when notifications are clicked.
 */
object MosquePushNotificationHelper {

    const val EXTRA_OPEN_MODULE = "open_module"
    const val EXTRA_TARGET_ID = "target_id"

    const val CHANNEL_NOTICES = "mosque_notices_channel"
    const val CHANNEL_RECEIPTS = "mosque_receipts_channel"
    const val CHANNEL_ELECTION = "mosque_election_channel"
    const val CHANNEL_MEETINGS = "mosque_meetings_channel"

    private const val NOTIF_ID_NOTICE_BASE = 1000
    private const val NOTIF_ID_RECEIPT_BASE = 2000
    private const val NOTIF_ID_ELECTION_BASE = 3000
    private const val NOTIF_ID_MEETING_BASE = 4000
    private const val NOTIF_ID_TEST = 9999

    /**
     * Initializes all notification channels on Android Oreo (API 26) and above.
     */
    fun initChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val channels = listOf(
            NotificationChannel(
                CHANNEL_NOTICES,
                "মসজিদ নোটিশ ও ঘোষণা",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "মসজিদের জরুরি নোটিশ, জানাজা ও বিশেষ বিজ্ঞপ্তি"
                enableVibration(true)
            },
            NotificationChannel(
                CHANNEL_RECEIPTS,
                "চাঁদা আদায় ও ডিজিটাল রসিদ",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "মাসিক ও বিশেষ চাঁদা প্রাপ্তির ডিজিটাল রসিদ নিশ্চিতকরণ"
                enableVibration(true)
            },
            NotificationChannel(
                CHANNEL_ELECTION,
                "কমিটি নির্বাচন ও তফসিল",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "কমিটি নির্বাচন তফসিল, মনোনয়নপত্র ও ফলাফল গেজেট"
                enableVibration(true)
            },
            NotificationChannel(
                CHANNEL_MEETINGS,
                "কমিটি সভা ও রেজুলেশন",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "আসন্ন সাধারণ ও জরুরি কমিটি সভার তারিখ ও আলোচ্যসূচি"
                enableVibration(true)
            }
        )

        channels.forEach { channel ->
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Checks if notification permission is granted on Android 13+ (API 33).
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Creates a PendingIntent targeting MainActivity with deep-link extras.
     */
    private fun createPendingIntent(
        context: Context,
        moduleKey: String,
        targetId: String? = null,
        requestCode: Int
    ): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_MODULE, moduleKey)
            if (targetId != null) {
                putExtra(EXTRA_TARGET_ID, targetId)
            }
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)

        return PendingIntent.getActivity(context, requestCode, intent, flags)
    }

    /**
     * Posts a notification for newly published notices.
     */
    fun postNoticeNotification(context: Context, notice: NoticeEntity) {
        if (!hasNotificationPermission(context)) return

        try {
            initChannels(context)

            val pendingIntent = createPendingIntent(
                context = context,
                moduleKey = "notices",
                targetId = notice.id,
                requestCode = NOTIF_ID_NOTICE_BASE + (notice.id.hashCode() and 0x7FFF)
            )

            val priorityLabel = when (notice.priority) {
                "urgent" -> "জরুরি বিজ্ঞপ্তি ⚠️"
                "janaza" -> "জানাজার নোটিশ 🕊️"
                else -> "মসজিদ নোটিশ 📢"
            }

            val notification = NotificationCompat.Builder(context, CHANNEL_NOTICES)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("$priorityLabel: ${notice.title}")
                .setContentText(notice.content.take(90))
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("${notice.content}\n\nপ্রকাশক: ${notice.createdBy} (${notice.publishDate})")
                        .setSummaryText("মসজিদ নোটিশ বোর্ড")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            val notifId = NOTIF_ID_NOTICE_BASE + (notice.id.hashCode() and 0xFF)
            NotificationManagerCompat.from(context).notify(notifId, notification)
        } catch (_: SecurityException) {
            // Permission denied on Android 13
        } catch (_: Exception) {
            // Safe fallback
        }
    }

    /**
     * Posts a notification for newly recorded chanda payment with official receipt details.
     */
    fun postChandaReceiptNotification(context: Context, payment: ChandaPaymentEntity) {
        if (!hasNotificationPermission(context)) return

        try {
            initChannels(context)

            val pendingIntent = createPendingIntent(
                context = context,
                moduleKey = "chanda",
                targetId = payment.id,
                requestCode = NOTIF_ID_RECEIPT_BASE + (payment.id.hashCode() and 0x7FFF)
            )

            val amountBn = toBengaliDigits(payment.amountPaid.toInt().toString())
            val title = "চাঁদা প্রাপ্তি রসিদ: ${payment.receiptCode} ৳$amountBn"
            val text = "${payment.memberNameBn}-এর ${payment.month} মাসের চাঁদা গ্রহণ সম্পন্ন হয়েছে।"

            val notification = NotificationCompat.Builder(context, CHANNEL_RECEIPTS)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("$text\n\nপরিশোধিত অঙ্ক: ৳$amountBn\nমাধ্যম: ${payment.method}\nপ্রাপ্তি সময়: ${payment.paidAt}\nরসিদ নং: ${payment.receiptCode}")
                        .setSummaryText("ডিজিটাল রসিদ")
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            val notifId = NOTIF_ID_RECEIPT_BASE + (payment.id.hashCode() and 0xFF)
            NotificationManagerCompat.from(context).notify(notifId, notification)
        } catch (_: SecurityException) {
            // Permission denied
        } catch (_: Exception) {
            // Safe fallback
        }
    }

    /**
     * Posts a notification for committee election milestones (schedule announcement, voting, etc.).
     */
    fun postElectionNotification(
        context: Context,
        election: ElectionScheduleEntity,
        milestoneDescription: String = "তফসিল ঘোষণা"
    ) {
        if (!hasNotificationPermission(context)) return

        try {
            initChannels(context)

            val pendingIntent = createPendingIntent(
                context = context,
                moduleKey = "election",
                targetId = election.id,
                requestCode = NOTIF_ID_ELECTION_BASE + (election.id.hashCode() and 0x7FFF)
            )

            val title = "কমিটি নির্বাচন ও পুনর্গঠন: $milestoneDescription"
            val body = "${election.termTitle} মেয়াদের নির্বাচন তফসিল ও মনোনয়নপত্র জমাদান শুরু হয়েছে।"

            val notification = NotificationCompat.Builder(context, CHANNEL_ELECTION)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("$body\n\nমনোনয়নপত্র জমার শেষ তারিখ: ${toBengaliDigits(election.nominationDeadline)}\nভোটগ্রহণের তারিখ: ${toBengaliDigits(election.votingDate)}\nপ্রধান নির্বাচন কমিশনার: ${election.chiefElectionCommissioner}")
                        .setSummaryText("মসজিদ নির্বাচন কমিশন")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            val notifId = NOTIF_ID_ELECTION_BASE + (election.id.hashCode() and 0xFF)
            NotificationManagerCompat.from(context).notify(notifId, notification)
        } catch (_: SecurityException) {
            // Permission denied
        } catch (_: Exception) {
            // Safe fallback
        }
    }

    /**
     * Posts a notification for newly scheduled committee meetings.
     */
    fun postMeetingNotification(context: Context, meeting: MeetingEntity) {
        if (!hasNotificationPermission(context)) return

        try {
            initChannels(context)

            val pendingIntent = createPendingIntent(
                context = context,
                moduleKey = "meetings",
                targetId = meeting.id,
                requestCode = NOTIF_ID_MEETING_BASE + (meeting.id.hashCode() and 0x7FFF)
            )

            val title = "আসন্ন কমিটি সভা: ${meeting.title}"
            val body = "তারিখ: ${meeting.meetingDate} | স্থান: ${meeting.location}"

            val notification = NotificationCompat.Builder(context, CHANNEL_MEETINGS)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("$body\n\nআলোচ্যসূচি / এজেন্ডা:\n${meeting.agenda}")
                        .setSummaryText("কমিটি সভা নোটিফিকেশন")
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            val notifId = NOTIF_ID_MEETING_BASE + (meeting.id.hashCode() and 0xFF)
            NotificationManagerCompat.from(context).notify(notifId, notification)
        } catch (_: SecurityException) {
            // Permission denied
        } catch (_: Exception) {
            // Safe fallback
        }
    }

    /**
     * Sends a test push notification to verify status bar notification delivery.
     */
    fun postTestNotification(context: Context) {
        if (!hasNotificationPermission(context)) return

        try {
            initChannels(context)

            val pendingIntent = createPendingIntent(
                context = context,
                moduleKey = "notices",
                requestCode = NOTIF_ID_TEST
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_NOTICES)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("মসজিদ সেবা পুশ নোটিফিকেশন সচল রয়েছে ✅")
                .setContentText("নতুন নোটিশ, চাঁদার রসিদ ও সভার নোটিফিকেশন সরাসরি আপনার নোটিফিকেশন বারে পৌঁছাবে।")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("আলহামদুলিল্লাহ! আপনার ডিভাইসে মসজিদ সেবা পুশ নোটিফিকেশন সিস্টেম সফলভাবে সক্রিয় আছে। মসজিদ কমিটির নোটিশ, চাঁদার রশিদ ও নির্বাচন তফসিলের আপডেট সরাসরি আপনার নোটিফিকেশন বারে প্রদর্শিত হবে।")
                        .setSummaryText("সিস্টেম টেস্ট নোটিফিকেশন")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            NotificationManagerCompat.from(context).notify(NOTIF_ID_TEST, notification)
        } catch (_: SecurityException) {
            // Permission denied
        } catch (_: Exception) {
            // Safe fallback
        }
    }
}
