package com.mosqueseba.app.data.local

import android.content.Context
import com.mosqueseba.app.data.local.entity.SmartBoardQuoteEntity

object DatabaseInitializer {

    /**
     * Seeds initial production data (e.g. authentic Quran & Hadith quotes for smart board).
     * Zero mock mosques, zero mock members, zero mock transactions.
     */
    suspend fun populateInitialData(database: MosqueDatabase) {
        val smartQuoteDao = database.smartBoardQuoteDao()
        val quotes = listOf(
            SmartBoardQuoteEntity(
                id = "sbq-001",
                category = "হাদিস",
                title = "মসজিদ আবাদকারীর মর্যাদা",
                message = "যে ব্যক্তি আল্লাহর সন্তুষ্টির উদ্দেশ্যে একটি মসজিদ নির্মাণ করে, আল্লাহ তাআলা তার জন্য জান্নাতে অনুরূপ একটি গৃহ নির্মাণ করবেন।",
                isPinned = true,
                createdBy = "সহিহ বুখারি ও মুসলিম"
            ),
            SmartBoardQuoteEntity(
                id = "sbq-002",
                category = "কুরআন",
                title = "মসজিদসমূহ আল্লাহর জন্য",
                message = "নিশ্চয় মসজিদসমূহ আল্লাহরই জন্য, অতএব তোমরা আল্লাহর সাথে অন্য কাউকে ডেকো না। (সূরা আল-জ্বিন: ১৮)",
                isPinned = true,
                createdBy = "পবিত্র কুরআন"
            ),
            SmartBoardQuoteEntity(
                id = "sbq-003",
                category = "হাদিস",
                title = "জামাতে নামাজের ফজিলত",
                message = "জামাতের সাথে নামাজ একাকী পড়ার চেয়ে সাতাশ গুণ বেশি মর্যাদাপূর্ণ।",
                isPinned = false,
                createdBy = "সহিহ বুখারি"
            )
        )
        quotes.forEach { smartQuoteDao.insertQuote(it) }
    }

    /**
     * Purges legacy prototype/demo data (mosque-001, mem-101..110, tx-001..015, etc.)
     * from user devices so that all existing and updated installations are 100% clean production environments.
     */
    suspend fun purgeDemoDataIfPresent(database: MosqueDatabase, context: Context) {
        try {
            val sDb = database.openHelper.writableDatabase
            val cursor = sDb.query("SELECT COUNT(*) FROM mosques WHERE id = 'mosque-001'")
            var hasDemoMosque = false
            if (cursor.moveToFirst()) {
                hasDemoMosque = cursor.getInt(0) > 0
            }
            cursor.close()

            if (hasDemoMosque) {
                val totalCursor = sDb.query("SELECT COUNT(*) FROM mosques")
                var totalMosques = 0
                if (totalCursor.moveToFirst()) {
                    totalMosques = totalCursor.getInt(0)
                }
                totalCursor.close()

                if (totalMosques <= 1) {
                    // Only the prototype demo mosque existed. Wipe all mock records completely!
                    database.clearAllTables()
                    populateInitialData(database)
                } else {
                    // User has their own registered mosque. Delete only the demo mosque and its cascading mock items.
                    sDb.execSQL("DELETE FROM mosques WHERE id = 'mosque-001'")
                    sDb.execSQL("DELETE FROM members WHERE id LIKE 'mem-1%'")
                    sDb.execSQL("DELETE FROM transactions WHERE id LIKE 'tx-%'")
                    sDb.execSQL("DELETE FROM chanda_schedules WHERE id LIKE 'cs-%'")
                    sDb.execSQL("DELETE FROM chanda_payments WHERE id LIKE 'chp-%'")
                    sDb.execSQL("DELETE FROM staff WHERE id LIKE 'stf-%'")
                    sDb.execSQL("DELETE FROM notices WHERE id LIKE 'not-%'")
                    sDb.execSQL("DELETE FROM meetings WHERE id LIKE 'mtg-%'")
                    sDb.execSQL("DELETE FROM audit_logs WHERE id LIKE 'aud-%'")
                    sDb.execSQL("DELETE FROM madrasa_students WHERE id LIKE 'std-%'")
                    sDb.execSQL("DELETE FROM election_schedules WHERE id = 'elec-2026'")
                    sDb.execSQL("DELETE FROM election_candidates WHERE id LIKE 'cand-%'")
                    sDb.execSQL("DELETE FROM mosque_properties WHERE id LIKE 'prop-%'")
                    sDb.execSQL("DELETE FROM food_schedules WHERE id LIKE 'fs-%'")
                    sDb.execSQL("DELETE FROM welfare_cases WHERE id LIKE 'welf-%'")
                    sDb.execSQL("DELETE FROM mosque_events WHERE id LIKE 'ev-%'")
                    sDb.execSQL("DELETE FROM cashbox_records WHERE id LIKE 'cb-%'")
                    sDb.execSQL("DELETE FROM advisory_notes WHERE id LIKE 'adv-%'")
                }

                // If session was using demo credentials, reset session
                val sessionManager = SessionManager(context)
                val savedMember = sessionManager.getSavedMemberId()
                if (savedMember?.startsWith("mem-1") == true || sessionManager.getActiveMosqueId() == "mosque-001") {
                    sessionManager.clearSession()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
