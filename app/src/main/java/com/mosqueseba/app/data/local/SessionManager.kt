package com.mosqueseba.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.mosqueseba.app.data.model.UserRole

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "mosque_seba_session_prefs"
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_ROLE_CODE = "key_role_code"
        private const val KEY_MEMBER_ID = "key_member_id"
        private const val KEY_MEMBER_NAME = "key_member_name"
        private const val KEY_COMMITTEE_ID = "key_committee_id"
        private const val KEY_MOBILE = "key_mobile"
        private const val KEY_REMEMBER_ME = "key_remember_me"
    }

    fun saveSession(
        role: UserRole,
        memberId: String,
        memberName: String,
        committeeId: String,
        mobile: String,
        rememberMe: Boolean = true
    ) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_ROLE_CODE, role.code)
            putString(KEY_MEMBER_ID, memberId)
            putString(KEY_MEMBER_NAME, memberName)
            putString(KEY_COMMITTEE_ID, committeeId)
            putString(KEY_MOBILE, mobile)
            putBoolean(KEY_REMEMBER_ME, rememberMe)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        val rememberMe = prefs.getBoolean(KEY_REMEMBER_ME, true)
        return rememberMe && prefs.getBoolean(KEY_IS_LOGGED_IN, false)
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
        prefs.edit().clear().apply()
    }
}
