package com.mosqueseba.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.mosqueseba.app.data.model.UserRole

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val credentialPrefs: SharedPreferences = context.getSharedPreferences(CRED_PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "mosque_seba_session_prefs"
        private const val CRED_PREF_NAME = "mosque_seba_credentials_prefs"
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_ROLE_CODE = "key_role_code"
        private const val KEY_MEMBER_ID = "key_member_id"
        private const val KEY_MEMBER_NAME = "key_member_name"
        private const val KEY_COMMITTEE_ID = "key_committee_id"
        private const val KEY_MOBILE = "key_mobile"
        private const val KEY_REMEMBER_ME = "key_remember_me"
        private const val KEY_ACTIVE_MOSQUE_ID = "key_active_mosque_id"
        private const val KEY_NOTIFICATIONS_ENABLED = "key_notifications_enabled"
        private const val KEY_SHOW_HIJRI_BANNER = "key_show_hijri_banner"
        private const val KEY_DARK_MODE_ENABLED = "key_dark_mode_enabled"
        private const val KEY_READ_NOTIFICATION_IDS = "key_read_notification_ids"
        private const val KEY_ONBOARDING_COMPLETED = "key_onboarding_completed"
        private const val DEFAULT_MOSQUE_ID = "mosque-001"
    }

    fun saveSession(
        role: UserRole,
        memberId: String,
        memberName: String,
        committeeId: String,
        mobile: String,
        rememberMe: Boolean = true,
        mosqueId: String = getActiveMosqueId()
    ) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_ROLE_CODE, role.code)
            putString(KEY_MEMBER_ID, memberId)
            putString(KEY_MEMBER_NAME, memberName)
            putString(KEY_COMMITTEE_ID, committeeId)
            putString(KEY_MOBILE, mobile)
            putBoolean(KEY_REMEMBER_ME, rememberMe)
            putString(KEY_ACTIVE_MOSQUE_ID, mosqueId)
            apply()
        }
    }

    fun saveActiveMosqueId(mosqueId: String) {
        prefs.edit().putString(KEY_ACTIVE_MOSQUE_ID, mosqueId).apply()
    }

    fun getActiveMosqueId(): String =
        prefs.getString(KEY_ACTIVE_MOSQUE_ID, DEFAULT_MOSQUE_ID) ?: DEFAULT_MOSQUE_ID

    fun saveRegisteredCredential(mobile: String, committeeId: String, mosqueId: String, password: String) {
        val cleanMobile = mobile.replace("-", "").replace(" ", "").trim()
        if (cleanMobile.isBlank()) return
        credentialPrefs.edit().apply {
            putString("pwd_$cleanMobile", password)
            putString("cid_$cleanMobile", committeeId)
            putString("mid_$cleanMobile", mosqueId)
            apply()
        }
    }

    fun saveRegisteredPresidentEmailCredential(email: String, committeeId: String, mosqueId: String, password: String) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank()) return
        credentialPrefs.edit().apply {
            putString("pwd_email_$cleanEmail", password)
            putString("cid_email_$cleanEmail", committeeId)
            putString("mid_email_$cleanEmail", mosqueId)
            apply()
        }
    }

    fun verifyRegisteredPassword(mobile: String, password: String): Boolean? {
        val cleanMobile = mobile.replace("-", "").replace(" ", "").trim()
        val saved = credentialPrefs.getString("pwd_$cleanMobile", null) ?: return null
        return saved == password
    }

    fun verifyRegisteredPresidentPassword(email: String, password: String): Boolean? {
        val cleanEmail = email.trim().lowercase()
        val saved = credentialPrefs.getString("pwd_email_$cleanEmail", null) ?: return null
        return saved == password
    }

    fun getRegisteredMosqueIdForMobile(mobile: String): String? {
        val cleanMobile = mobile.replace("-", "").replace(" ", "").trim()
        return credentialPrefs.getString("mid_$cleanMobile", null)
    }

    fun getRegisteredMosqueIdForEmail(email: String): String? {
        val cleanEmail = email.trim().lowercase()
        return credentialPrefs.getString("mid_email_$cleanEmail", null)
    }

    fun isLoggedIn(): Boolean {
        val rememberMe = prefs.getBoolean(KEY_REMEMBER_ME, true)
        return rememberMe && prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun isRememberMeEnabled(): Boolean = prefs.getBoolean(KEY_REMEMBER_ME, true)

    fun setRememberMeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMEMBER_ME, enabled).apply()
    }

    fun isNotificationsEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun isHijriBannerVisible(): Boolean = prefs.getBoolean(KEY_SHOW_HIJRI_BANNER, true)

    fun setHijriBannerVisible(visible: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_HIJRI_BANNER, visible).apply()
    }

    fun isDarkModeEnabled(): Boolean = prefs.getBoolean(KEY_DARK_MODE_ENABLED, false)

    fun hasUserSetDarkMode(): Boolean = prefs.contains(KEY_DARK_MODE_ENABLED)

    fun setDarkModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE_ENABLED, enabled).apply()
    }

    fun isOnboardingCompleted(): Boolean = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    fun getReadNotificationIds(): Set<String> =
        prefs.getStringSet(KEY_READ_NOTIFICATION_IDS, emptySet())?.toSet() ?: emptySet()

    fun markNotificationRead(notificationId: String) {
        val updated = getReadNotificationIds() + notificationId
        prefs.edit().putStringSet(KEY_READ_NOTIFICATION_IDS, updated).apply()
    }

    fun markAllNotificationsRead(notificationIds: Collection<String>) {
        if (notificationIds.isEmpty()) return
        val updated = getReadNotificationIds() + notificationIds
        prefs.edit().putStringSet(KEY_READ_NOTIFICATION_IDS, updated).apply()
    }

    fun getKhademCheckedTasksForDate(dateIso: String): Set<String> =
        prefs.getStringSet("key_khadem_tasks_$dateIso", emptySet())?.toSet() ?: emptySet()

    fun toggleKhademTaskForDate(dateIso: String, taskId: String): Set<String> {
        val current = getKhademCheckedTasksForDate(dateIso)
        val updated = if (taskId in current) current - taskId else current + taskId
        prefs.edit().putStringSet("key_khadem_tasks_$dateIso", updated).apply()
        return updated
    }

    fun getSavedRole(): UserRole {
        val code = prefs.getString(KEY_ROLE_CODE, UserRole.PRESIDENT.code) ?: UserRole.PRESIDENT.code
        return UserRole.fromCode(code)
    }

    fun getSavedMemberId(): String? = prefs.getString(KEY_MEMBER_ID, null)

    fun getSavedMemberName(): String = prefs.getString(KEY_MEMBER_NAME, "মো: রফিকুল ইসলাম") ?: "মো: রফিকুল ইসলাম"

    fun getSavedCommitteeId(): String = prefs.getString(KEY_COMMITTEE_ID, "MC-DHK-2024") ?: "MC-DHK-2024"

    fun getSavedMobile(): String = prefs.getString(KEY_MOBILE, "01711-223344") ?: "01711-223344"

    fun clearSession() {
        val currentMosque = getActiveMosqueId()
        val notif = isNotificationsEnabled()
        val hijri = isHijriBannerVisible()
        val darkMode = isDarkModeEnabled()
        val onboarding = isOnboardingCompleted()
        prefs.edit()
            .clear()
            .putString(KEY_ACTIVE_MOSQUE_ID, currentMosque)
            .putBoolean(KEY_NOTIFICATIONS_ENABLED, notif)
            .putBoolean(KEY_SHOW_HIJRI_BANNER, hijri)
            .putBoolean(KEY_DARK_MODE_ENABLED, darkMode)
            .putBoolean(KEY_ONBOARDING_COMPLETED, onboarding)
            .apply()
    }
}
