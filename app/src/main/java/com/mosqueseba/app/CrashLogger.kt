package com.mosqueseba.app

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Date

/**
 * Installs a default uncaught-exception handler that writes the full stack
 * trace to a file in the public Downloads folder. This makes crashes debuggable
 * even when logcat access is restricted (e.g. MIUI / non-rooted devices).
 */
object CrashLogger {

    private const val TAG = "MOSQUE_CRASH"
    const val FILE_NAME = "mosque_seba_crash.txt"

    fun install(context: Context) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val report = buildReport(thread, throwable)
            Log.e(TAG, report)
            writeToDownloads(context.applicationContext, report)
            // Preserve normal crash behaviour (system still records the crash).
            previous?.uncaughtException(thread, throwable)
                ?: kotlin.system.exitProcess(1)
        }
    }

    private fun buildReport(thread: Thread, throwable: Throwable): String {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        return buildString {
            append("=== MOSQUE SEBA CRASH ===\n")
            append("Time: ${Date()}\n")
            append("Thread: ${thread.name}\n")
            append("Device: ${Build.MANUFACTURER} ${Build.MODEL} (API ${Build.VERSION.SDK_INT})\n")
            append("App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n\n")
            append(sw.toString())
        }
    }

    private fun writeToDownloads(context: Context, report: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, FILE_NAME)
                    put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
                )
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(report.toByteArray(Charsets.UTF_8))
                        out.flush()
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                java.io.File(dir, FILE_NAME).writeText(report, Charsets.UTF_8)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write crash report", e)
        }
    }
}
