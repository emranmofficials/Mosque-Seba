package com.mosqueseba.app.data.remote.supabase

import android.content.Context
import android.content.SharedPreferences

object SupabaseConfig {
    private const val PREFS_NAME = "mosque_supabase_prefs"
    private const val KEY_URL = "supabase_url"
    private const val KEY_ANON_KEY = "supabase_anon_key"
    private const val KEY_LAST_SYNC_TIME = "last_sync_time"

    // Default or Fallback placeholder
    const val DEFAULT_FALLBACK_URL = ""
    const val DEFAULT_FALLBACK_KEY = ""

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isConfigured(context: Context): Boolean {
        val (url, key) = getCredentials(context)
        return url.isNotBlank() && key.isNotBlank() && url.startsWith("https://")
    }

    fun getCredentials(context: Context): Pair<String, String> {
        val prefs = getPrefs(context)
        val url = prefs.getString(KEY_URL, DEFAULT_FALLBACK_URL)?.trim() ?: ""
        val key = prefs.getString(KEY_ANON_KEY, DEFAULT_FALLBACK_KEY)?.trim() ?: ""
        return Pair(url, key)
    }

    fun saveCredentials(context: Context, url: String, key: String) {
        val cleanUrl = url.trim().trimEnd('/')
        val cleanKey = key.trim()
        getPrefs(context).edit()
            .putString(KEY_URL, cleanUrl)
            .putString(KEY_ANON_KEY, cleanKey)
            .apply()
    }

    fun getLastSyncTime(context: Context): String? {
        return getPrefs(context).getString(KEY_LAST_SYNC_TIME, null)
    }

    fun saveLastSyncTime(context: Context, timeStr: String) {
        getPrefs(context).edit()
            .putString(KEY_LAST_SYNC_TIME, timeStr)
            .apply()
    }

    fun clearCredentials(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
