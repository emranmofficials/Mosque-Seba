package com.mosqueseba.app

import android.app.Application

class MosqueApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashLogger.install(this)
    }
}
