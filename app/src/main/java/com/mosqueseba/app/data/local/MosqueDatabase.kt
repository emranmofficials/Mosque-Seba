package com.mosqueseba.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mosqueseba.app.data.local.dao.*
import com.mosqueseba.app.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MosqueEntity::class,
        MemberEntity::class,
        TransactionEntity::class,
        ChandaPaymentEntity::class,
        StaffEntity::class,
        NoticeEntity::class,
        MeetingEntity::class,
        AuditLogEntity::class,
        StudentEntity::class,
        MadrasaAttendanceEntity::class,
        HifzProgressEntity::class,
        MadrasaFeePaymentEntity::class,
        StaffPayrollEntity::class,
        DutyRosterEntity::class,
        FoodScheduleEntity::class,
        WelfareCaseEntity::class,
        MosqueEventEntity::class,
        CashboxRecordEntity::class,
        OfficeDocumentEntity::class,
        AdvisoryNoteEntity::class,
        SmartBoardQuoteEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class MosqueDatabase : RoomDatabase() {

    abstract fun mosqueDao(): MosqueDao
    abstract fun memberDao(): MemberDao
    abstract fun transactionDao(): TransactionDao
    abstract fun chandaDao(): ChandaDao
    abstract fun staffDao(): StaffDao
    abstract fun noticeDao(): NoticeDao
    abstract fun meetingDao(): MeetingDao
    abstract fun auditDao(): AuditDao

    // Madrasa DAOs
    abstract fun studentDao(): StudentDao
    abstract fun madrasaAttendanceDao(): MadrasaAttendanceDao
    abstract fun hifzDao(): HifzDao
    abstract fun madrasaFeeDao(): MadrasaFeeDao

    // Staff Operations DAOs
    abstract fun staffPayrollDao(): StaffPayrollDao
    abstract fun dutyRosterDao(): DutyRosterDao
    abstract fun foodScheduleDao(): FoodScheduleDao

    // Welfare & Events DAOs
    abstract fun welfareDao(): WelfareDao
    abstract fun eventDao(): EventDao

    // Committee Operations DAOs
    abstract fun cashboxDao(): CashboxDao
    abstract fun officeDocumentDao(): OfficeDocumentDao
    abstract fun advisoryNoteDao(): AdvisoryNoteDao
    abstract fun smartBoardQuoteDao(): SmartBoardQuoteDao


    companion object {
        @Volatile
        private var INSTANCE: MosqueDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): MosqueDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MosqueDatabase::class.java,
                    "mosque_management_db"
                )
                .addCallback(MosqueDatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class MosqueDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    DatabaseInitializer.populateInitialData(database)
                }
            }
        }
    }
}
