package com.mosqueseba.app.data.updater

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mosqueseba.app.BuildConfig
import com.mosqueseba.app.MainActivity
import com.mosqueseba.app.R
import com.mosqueseba.app.ui.components.MosqueToastController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val latestVersionName: String,
    val latestVersionCode: Int,
    val releaseTitle: String,
    val releaseNotes: String,
    val releasePageUrl: String,
    val apkDownloadUrl: String,
    val aabDownloadUrl: String?,
    val isUpdateAvailable: Boolean
)

object AppUpdateManager {
    private const val PREFS_NAME = "mosque_app_update_prefs"
    private const val KEY_LAST_CHECK_TIME = "last_check_time"
    private const val KEY_DISMISSED_VERSION = "dismissed_version"
    private const val CHANNEL_ID = "mosque_app_updates_channel"
    private const val NOTIFICATION_ID = 9901

    private const val GITHUB_LATEST_RELEASE_API =
        "https://api.github.com/repos/emranmofficials/Mosque-Seba/releases/latest"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Checks if the remote version string is newer than current version.
     */
    fun isNewerVersion(remoteVersion: String, currentVersion: String): Boolean {
        try {
            val cleanRemote = remoteVersion.trim().removePrefix("v").removePrefix("V")
            val cleanCurrent = currentVersion.trim().removePrefix("v").removePrefix("V")

            val remoteParts = cleanRemote.split(".").mapNotNull { it.takeWhile { ch -> ch.isDigit() }.toIntOrNull() }
            val currentParts = cleanCurrent.split(".").mapNotNull { it.takeWhile { ch -> ch.isDigit() }.toIntOrNull() }

            val maxParts = maxOf(remoteParts.size, currentParts.size)
            for (i in 0 until maxParts) {
                val r = remoteParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
        } catch (_: Exception) {
            // Fallback string inequality check
            return remoteVersion.trim().removePrefix("v") != currentVersion.trim().removePrefix("v")
        }
        return false
    }

    /**
     * Fetches the latest release info from GitHub API asynchronously.
     */
    suspend fun checkLatestRelease(
        context: Context,
        forceCheck: Boolean = false
    ): AppUpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val prefs = getPrefs(context)
            val now = System.currentTimeMillis()
            val lastCheck = prefs.getLong(KEY_LAST_CHECK_TIME, 0L)

            // Rate-limit automated checks to once every 4 hours unless manual
            if (!forceCheck && (now - lastCheck) < 4 * 60 * 60 * 1000L) {
                // Skip background check to be network-friendly
                return@withContext null
            }

            prefs.edit().putLong(KEY_LAST_CHECK_TIME, now).apply()

            val url = URL(GITHUB_LATEST_RELEASE_API)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 7000
                readTimeout = 7000
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "MosqueSeba-App-UpdateChecker")
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext null
            }

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val jsonText = reader.use { it.readText() }
            val root = JSONObject(jsonText)

            val rawTagName = root.optString("tag_name", "").trim()
            val cleanVersion = rawTagName.removePrefix("v").removePrefix("V")
            val releaseTitle = root.optString("name", "মসজিদ সেবা নতুন সংস্করণ")
            val releaseNotes = root.optString("body", "নতুন ফিচার ও উন্নতি অন্তর্ভুক্ত করা হয়েছে।")
            val releasePageUrl = root.optString("html_url", "https://github.com/emranmofficials/Mosque-Seba/releases")

            var apkUrl = ""
            var aabUrl: String? = null

            val assets = root.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.optJSONObject(i) ?: continue
                    val name = asset.optString("name", "")
                    val downloadUrl = asset.optString("browser_download_url", "")
                    if (name.endsWith(".apk", ignoreCase = true) && apkUrl.isEmpty()) {
                        apkUrl = downloadUrl
                    } else if (name.endsWith(".aab", ignoreCase = true)) {
                        aabUrl = downloadUrl
                    }
                }
            }

            if (apkUrl.isEmpty()) {
                apkUrl = "$releasePageUrl/download/$rawTagName/Mosque-Seba-$rawTagName.apk"
            }

            val currentVersionName = BuildConfig.VERSION_NAME
            val isNewer = isNewerVersion(cleanVersion, currentVersionName)

            val updateInfo = AppUpdateInfo(
                latestVersionName = cleanVersion.ifBlank { rawTagName },
                latestVersionCode = BuildConfig.VERSION_CODE + 1,
                releaseTitle = releaseTitle,
                releaseNotes = releaseNotes,
                releasePageUrl = releasePageUrl,
                apkDownloadUrl = apkUrl,
                aabDownloadUrl = aabUrl,
                isUpdateAvailable = isNewer
            )

            // If a newer version is found, trigger system notification
            if (isNewer) {
                postUpdateSystemNotification(context, updateInfo)
            }

            updateInfo
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Posts a native Android system notification alerting about the update.
     */
    fun postUpdateSystemNotification(context: Context, updateInfo: AppUpdateInfo) {
        try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "অ্যাপ আপডেট ও নোটিশ",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "মসজিদ সেবার নতুন ভার্সন ও আপডেট সংক্রান্ত নোটিফিকেশন"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.apkDownloadUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("মসজিদ সেবা: নতুন সংস্করণ v${updateInfo.latestVersionName} উপলব্ধ!")
                .setContentText("নতুন আপডেট পেতে ট্যাপ করে সরাসরি ইনস্টল করুন।")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("মসজিদ সেবার নতুন সংস্করণ v${updateInfo.latestVersionName} এসেছে। কোনো ডাটা হারাবে না এবং আনইনস্টল করতে হবে না। এখনই আপডেট করুন।")
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(
                    android.R.drawable.stat_sys_download,
                    "আপডেট ডাউনলোড",
                    pendingIntent
                )
                .build()

            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Android 13 POST_NOTIFICATIONS permission not granted yet
        } catch (_: Exception) {
            // Ignore system notification errors
        }
    }

    fun isVersionDismissed(context: Context, versionName: String): Boolean {
        val prefs = getPrefs(context)
        val dismissed = prefs.getString(KEY_DISMISSED_VERSION, null)
        return dismissed != null && dismissed.equals(versionName, ignoreCase = true)
    }

    fun dismissVersion(context: Context, versionName: String) {
        getPrefs(context).edit().putString(KEY_DISMISSED_VERSION, versionName).apply()
    }

    /**
     * Directly downloads and triggers installation of the new APK using Android DownloadManager.
     */
    fun downloadAndInstallApk(context: Context, updateInfo: AppUpdateInfo) {
        val apkUrl = updateInfo.apkDownloadUrl.ifBlank { updateInfo.releasePageUrl }
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            if (downloadManager != null && apkUrl.startsWith("http")) {
                val fileName = "Mosque-Seba-v${updateInfo.latestVersionName}.apk"
                val request = DownloadManager.Request(Uri.parse(apkUrl)).apply {
                    setTitle("Mosque Seba v${updateInfo.latestVersionName}")
                    setDescription("মসজিদ সেবার নতুন সংস্করণ ডাউনলোড হচ্ছে...")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
                    setMimeType("application/vnd.android.package-archive")
                }
                downloadManager.enqueue(request)
                MosqueToastController.info("আপডেট ডাউনলোড শুরু হয়েছে। নোটিফিকেশন বারে লক্ষ্য করুন।")
            } else {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        } catch (_: Exception) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }
}
