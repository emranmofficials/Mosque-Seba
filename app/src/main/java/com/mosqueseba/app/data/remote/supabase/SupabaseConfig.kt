package com.mosqueseba.app.data.remote.supabase

import android.content.Context
import android.content.SharedPreferences

object SupabaseConfig {
    private const val PREFS_NAME = "mosque_supabase_prefs"
    private const val KEY_URL = "supabase_url"
    private const val KEY_ANON_KEY = "supabase_anon_key"
    private const val KEY_LAST_SYNC_TIME = "last_sync_time"

    // Live Pre-configured Mosque-Seba Supabase Backend
    const val DEFAULT_FALLBACK_URL = "https://donglyxtrfyzwgtrskhz.supabase.co"
    const val DEFAULT_FALLBACK_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRvbmdseXh0cmZ5endndHJza2h6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTQ5MTI4MTgsImV4cCI6MjExMDQ4ODgxOH0.0dVON58e2nsB6g2_UuMACtzvSJvPEcdOiBnjPovxcYU"

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
