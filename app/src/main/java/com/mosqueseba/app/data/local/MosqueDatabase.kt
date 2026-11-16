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
        SmartBoardQuoteEntity::class,
        ElectionScheduleEntity::class,
        ElectionCandidateEntity::class,
        MosquePropertyEntity::class
    ],
    version = 8,
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
    abstract fun electionDao(): ElectionDao
    abstract fun mosquePropertyDao(): MosquePropertyDao


    companion object {
        @Volatile
        private var INSTANCE: MosqueDatabase? = null

        val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS election_schedules (
                        id TEXT NOT NULL PRIMARY KEY,
                        termTitle TEXT NOT NULL,
                        termStartYear INTEGER NOT NULL,
                        termEndYear INTEGER NOT NULL,
                        chiefElectionCommissioner TEXT NOT NULL,
                        commissionerMobile TEXT NOT NULL,
                        scheduleDate TEXT NOT NULL,
                        nominationDeadline TEXT NOT NULL,
                        votingDate TEXT NOT NULL,
                        status TEXT NOT NULL,
                        electionType TEXT NOT NULL,
                        gazetteNotes TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS election_candidates (
                        id TEXT NOT NULL PRIMARY KEY,
                        electionId TEXT NOT NULL,
                        targetRoleCode TEXT NOT NULL,
                        targetRoleTitleBn TEXT NOT NULL,
                        candidateNameBn TEXT NOT NULL,
                        candidateMobile TEXT NOT NULL,
                        memberCode TEXT NOT NULL,
                        proposerNameBn TEXT NOT NULL,
                        seconderNameBn TEXT NOT NULL,
                        status TEXT NOT NULL,
                        symbolOrPanel TEXT NOT NULL,
                        votesReceived INTEGER NOT NULL,
                        isElected INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS mosque_properties (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        category TEXT NOT NULL,
                        areaOrQuantity TEXT NOT NULL,
                        locationOrDagNo TEXT NOT NULL,
                        deedOrRegNo TEXT NOT NULL,
                        estimatedValueOrIncome TEXT NOT NULL,
                        photoUrl TEXT,
                        status TEXT NOT NULL,
                        description TEXT NOT NULL,
                        recordedDate TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): MosqueDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MosqueDatabase::class.java,
                    "mosque_management_db"
                )
                .addCallback(MosqueDatabaseCallback(scope))
                .addMigrations(MIGRATION_6_7, MIGRATION_7_8)
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
