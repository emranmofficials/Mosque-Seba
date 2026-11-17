package com.mosqueseba.app.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Debug
import android.view.WindowManager
import java.io.File
import java.security.MessageDigest

/**
 * Enterprise-grade Security Guardian for Mosque Seba.
 * Provides root detection, screen recording / screenshot prevention,
 * integrity verification, and secure hashing.
 */
object MosqueSecurityManager {

    private val ROOT_PATHS = arrayOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/system/bin/.ext/.su",
        "/system/usr/we-need-root/su-backup"
    )

    /**
     * Checks if the device appears to be rooted using multiple heuristics.
     */
    fun isDeviceRooted(): Boolean {
        return checkBuildTags() || checkRootBinaries() || checkSuBinaryExecution()
    }

    private fun checkBuildTags(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkRootBinaries(): Boolean {
        for (path in ROOT_PATHS) {
            try {
                if (File(path).exists()) return true
            } catch (_: Exception) {
                // Ignore security exceptions
            }
        }
        return false
    }

    private fun checkSuBinaryExecution(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val reader = process.inputStream.bufferedReader()
            reader.readLine() != null
        } catch (_: Exception) {
            false
        } finally {
            process?.destroy()
        }
    }

    /**
     * Prevents screen recording, screenshots, and task switcher previews
     * on sensitive financial and management screens.
     */
    fun enableScreenProtection(activity: Activity) {
        try {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } catch (_: Exception) {
            // Ignore if window is not attached
        }
    }

    /**
     * Removes screen protection if needed.
     */
    fun disableScreenProtection(activity: Activity) {
        try {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } catch (_: Exception) {
            // Ignore
        }
    }

    /**
     * Checks if a debugger is attached to the process.
     */
    fun isDebuggerAttached(): Boolean {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger()
    }

    /**
     * Computes SHA-256 cryptographic hash of a given string.
     */
    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Securely clears a char array in memory.
     */
    fun wipeSensitiveChars(chars: CharArray) {
        chars.fill('\u0000')
    }
}
