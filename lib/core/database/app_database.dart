import 'dart:io';
import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart' as p;

part 'app_database.g.dart';

// --- CORE TABLES ---

class MosquesTable extends Table {
  TextColumn get id => text()();
  TextColumn get committeeId => text()();
  TextColumn get nameBn => text()();
  TextColumn get nameEn => text().withDefault(const Constant(''))();
  TextColumn get address => text()();
  IntColumn get establishedYear => integer().withDefault(const Constant(0))();
  IntColumn get committeeStartYear => integer().withDefault(const Constant(2026))();
  IntColumn get committeeEndYear => integer().withDefault(const Constant(2028))();
  TextColumn get status => text().withDefault(const Constant('active'))();
  TextColumn get district => text().withDefault(const Constant(''))();
  TextColumn get upazila => text().withDefault(const Constant(''))();
  TextColumn get waqfRegNo => text().withDefault(const Constant(''))();
  IntColumn get capacity => integer().nullable()();
  TextColumn get bankAccountInfo => text().withDefault(const Constant(''))();
  TextColumn get logoUrl => text().nullable()();
  TextColumn get sealUrl => text().nullable()();

  @override
  Set<Column> get primaryKey => {id};
}

class MembersTable extends Table {
  TextColumn get id => text()();
  TextColumn get memberCode => text()();
  TextColumn get nameBn => text()();
  TextColumn get nameEn => text().withDefault(const Constant(''))();
  TextColumn get mobile => text()();
  TextColumn get email => text().withDefault(const Constant(''))();
  TextColumn get nid => text().withDefault(const Constant(''))();
  TextColumn get address => text().withDefault(const Constant(''))();
  TextColumn get villageArea => text().withDefault(const Constant(''))();
  TextColumn get roleCode => text().withDefault(const Constant('general_member'))();
  RealColumn get monthlyChanda => real().withDefault(const Constant(0.0))();
  TextColumn get joinDate => text().withDefault(const Constant(''))();
  TextColumn get photoUrl => text().nullable()();
  TextColumn get signatureUrl => text().nullable()();
  TextColumn get stampUrl => text().nullable()();
  TextColumn get designationBn => text().nullable()();
  TextColumn get status => text().withDefault(const Constant('active'))();
  BoolColumn get willingToProvideStaffMeals => boolean().withDefault(const Constant(false))();

  @override
  Set<Column> get primaryKey => {id};
}

class TransactionsTable extends Table {
  TextColumn get id => text()();
  TextColumn get transactionCode => text()();
  TextColumn get categoryNameBn => text()();
  TextColumn get type => text()(); // 'income' or 'expense'
  RealColumn get amount => real()();
  TextColumn get txnDate => text()();
  TextColumn get description => text().withDefault(const Constant(''))();
  TextColumn get sourceType => text().withDefault(const Constant('manual'))();
  TextColumn get sourceId => text().nullable()();
  TextColumn get createdBy => text()();
  TextColumn get approvedBy => text().nullable()();
  TextColumn get approvalStatus => text().withDefault(const Constant('approved'))();
  BoolColumn get isReversal => boolean().withDefault(const Constant(false))();
  IntColumn get createdAt => integer()();

  @override
  Set<Column> get primaryKey => {id};
}

class ChandaPaymentsTable extends Table {
  TextColumn get id => text()();
  TextColumn get memberId => text()();
  TextColumn get memberNameBn => text()();
  TextColumn get month => text()();
  RealColumn get amountDue => real().withDefault(const Constant(0.0))();
  RealColumn get amountPaid => real()();
  TextColumn get method => text().withDefault(const Constant('cash'))();
  TextColumn get status => text().withDefault(const Constant('paid'))();
  TextColumn get receiptCode => text()();
  TextColumn get qrVerificationCode => text()();
  TextColumn get receivedBy => text()();
  TextColumn get paidAt => text()();
  IntColumn get createdAt => integer()();

  @override
  Set<Column> get primaryKey => {id};
}

class StaffTable extends Table {
  TextColumn get id => text()();
  TextColumn get nameBn => text()();
  TextColumn get roleCode => text()();
  TextColumn get designationBn => text()();
  TextColumn get employmentType => text().withDefault(const Constant('full_time'))();
  TextColumn get joinDate => text()();
  RealColumn get salaryAmount => real()();
  TextColumn get salaryFrequency => text().withDefault(const Constant('monthly'))();
  BoolColumn get mealRequired => boolean().withDefault(const Constant(true))();
  TextColumn get mobile => text()();
  BoolColumn get isActive => boolean().withDefault(const Constant(true))();
  TextColumn get signatureUrl => text().nullable()();
  TextColumn get stampUrl => text().nullable()();

  @override
  Set<Column> get primaryKey => {id};
}

class NoticesTable extends Table {
  TextColumn get id => text()();
  TextColumn get title => text()();
  TextColumn get content => text()();
  TextColumn get priority => text().withDefault(const Constant('normal'))();
  BoolColumn get isPinned => boolean().withDefault(const Constant(false))();
  TextColumn get publishDate => text()();
  TextColumn get expiryDate => text().nullable()();
  TextColumn get status => text().withDefault(const Constant('published'))();
  TextColumn get createdBy => text()();
  IntColumn get createdAt => integer()();
  TextColumn get imageUrl => text().nullable()();

  @override
  Set<Column> get primaryKey => {id};
}

class MeetingsTable extends Table {
  TextColumn get id => text()();
  TextColumn get title => text()();
  TextColumn get meetingDate => text()();
  TextColumn get location => text()();
  TextColumn get agenda => text()();
  TextColumn get status => text().withDefault(const Constant('upcoming'))();
  TextColumn get resolution => text().withDefault(const Constant(''))();

  @override
  Set<Column> get primaryKey => {id};
}

class CashboxRecordsTable extends Table {
  TextColumn get id => text()();
  TextColumn get waqtDate => text()();
  IntColumn get note1000 => integer().withDefault(const Constant(0))();
  IntColumn get note500 => integer().withDefault(const Constant(0))();
  IntColumn get note200 => integer().withDefault(const Constant(0))();
  IntColumn get note100 => integer().withDefault(const Constant(0))();
  IntColumn get note50 => integer().withDefault(const Constant(0))();
  IntColumn get note20 => integer().withDefault(const Constant(0))();
  IntColumn get note10 => integer().withDefault(const Constant(0))();
  IntColumn get note5 => integer().withDefault(const Constant(0))();
  RealColumn get totalAmount => real()();
  TextColumn get witness1Name => text()();
  TextColumn get witness2Name => text()();
  TextColumn get witness3Name => text().withDefault(const Constant(''))();
  TextColumn get witness1SignatureUrl => text().nullable()();
  TextColumn get witness2SignatureUrl => text().nullable()();
  TextColumn get witness3SignatureUrl => text().nullable()();
  TextColumn get voucherCode => text().nullable()();
  IntColumn get countedAt => integer()();

  @override
  Set<Column> get primaryKey => {id};
}

// --- PHASE 7: MADRASA & ISLAMIC EDUCATION TABLES ---

class MadrasaStudentsTable extends Table {
  TextColumn get id => text()();
  TextColumn get studentCode => text()();
  TextColumn get nameBn => text()();
  TextColumn get nameEn => text().withDefault(const Constant(''))();
  TextColumn get className => text()(); // নূরানী, নাজেরা, হিফজ, কিতাব
  TextColumn get guardianName => text()();
  TextColumn get guardianMobile => text()();
  TextColumn get guardianNid => text().withDefault(const Constant(''))();
  RealColumn get monthlyFee => real().withDefault(const Constant(0.0))();
  TextColumn get admissionDate => text().withDefault(const Constant(''))();
  TextColumn get bloodGroup => text().withDefault(const Constant(''))();
  TextColumn get status => text().withDefault(const Constant('active'))(); // active, inactive, graduated
  TextColumn get photoUrl => text().nullable()();
  IntColumn get createdAt => integer()();

  @override
  Set<Column> get primaryKey => {id};
}

class MadrasaAttendanceTable extends Table {
  TextColumn get id => text()();
  TextColumn get studentId => text()();
  TextColumn get studentNameBn => text()();
  TextColumn get className => text()();
  TextColumn get attendanceDate => text()(); // YYYY-MM-DD
  TextColumn get status => text().withDefault(const Constant('present'))(); // present, absent, late, leave
  TextColumn get remarks => text().withDefault(const Constant(''))();
  TextColumn get recordedBy => text()();
  IntColumn get createdAt => integer()();

  @override
  Set<Column> get primaryKey => {id};
}

class HifzProgressTable extends Table {
  TextColumn get id => text()();
  TextColumn get studentId => text()();
  TextColumn get studentNameBn => text()();
  IntColumn get paraNumber => integer()(); // 1 to 30
  TextColumn get surahName => text()();
  IntColumn get pageNumber => integer().withDefault(const Constant(1))();
  IntColumn get sabaqRating => integer().withDefault(const Constant(5))(); // 1-5 stars
  IntColumn get sabaqiRating => integer().withDefault(const Constant(5))(); // 1-5 stars
  IntColumn get manzilRating => integer().withDefault(const Constant(5))(); // 1-5 stars
  TextColumn get teacherRemarks => text().withDefault(const Constant(''))();
  TextColumn get recordedDate => text()();
  IntColumn get createdAt => integer()();

  @override
  Set<Column> get primaryKey => {id};
}

class ExaminationsTable extends Table {
  TextColumn get id => text()();
  TextColumn get examNameBn => text()(); // e.g. ১ম সাময়িক পরীক্ষা ২০২৬
  TextColumn get examType => text()(); // quarterly, half_yearly, annual, hifz_eval
  TextColumn get academicYear => text()();
  TextColumn get startDate => text()();
  TextColumn get status => text().withDefault(const Constant('scheduled'))(); // scheduled, ongoing, completed, published
  IntColumn get createdAt => integer()();

  @override
  Set<Column> get primaryKey => {id};
}

class ExamResultsTable extends Table {
  TextColumn get id => text()();
  TextColumn get examId => text()();
  TextColumn get studentId => text()();
  TextColumn get studentNameBn => text()();
  TextColumn get className => text()();
  TextColumn get subjectName => text()();
  RealColumn get marksObtained => real()();
  RealColumn get totalMarks => real().withDefault(const Constant(100.0))();
  TextColumn get grade => text()(); // মুমতাজ, জাইয়িদ জিদ্দান, জাইয়িদ, মাকবুল, রাসিব
  TextColumn get remarks => text().withDefault(const Constant(''))();
  IntColumn get createdAt => integer()();

  @override
  Set<Column> get primaryKey => {id};
}

class MadrasaFeePaymentsTable extends Table {
  TextColumn get id => text()();
  TextColumn get studentId => text()();
  TextColumn get studentNameBn => text()();
  TextColumn get className => text()();
  TextColumn get month => text()(); // e.g. ২০২৬-১১
  RealColumn get amountDue => real().withDefault(const Constant(0.0))();
  RealColumn get amountPaid => real()();
  RealColumn get discountAmount => real().withDefault(const Constant(0.0))();
  TextColumn get paymentMethod => text().withDefault(const Constant('cash'))();
  TextColumn get receiptCode => text()();
  TextColumn get status => text().withDefault(const Constant('paid'))(); // paid, partial, due
  TextColumn get receivedBy => text()();
  TextColumn get paidAt => text()();
  IntColumn get createdAt => integer()();

  @override
  Set<Column> get primaryKey => {id};
}

// --- PHASE 8: EVENTS & WELFARE TABLES ---

class MosqueEventsTable extends Table {
  TextColumn get id => text()();
  TextColumn get title => text()();
  TextColumn get eventType => text()(); // 'waz_mahfil', 'shab_e_qadr', 'iftar_party', 'milad_doa', 'eid_gathering'
  TextColumn get eventDate => text()(); // YYYY-MM-DD
  TextColumn get eventTime => text().withDefault(const Constant('বাদ আছর'))();
  TextColumn get venue => text().withDefault(const Constant('মসজিদ প্রাঙ্গণ'))();
  TextColumn get chiefGuest => text().withDefault(const Constant(''))();
  TextColumn get specialSpeakers => text().withDefault(const Constant(''))();
  RealColumn get estimatedBudget => real().withDefault(const Constant(0.0))();
  RealColumn get actualExpense => real().withDefault(const Constant(0.0))();
  TextColumn get status => text().withDefault(const Constant('upcoming'))(); // upcoming, ongoing, completed, cancelled
  TextColumn get description => text().withDefault(const Constant(''))();
  IntColumn get createdAt => integer()();

  @override
  Set<Column> get primaryKey => {id};
}

class WelfareCasesTable extends Table {
  TextColumn get id => text()();
  TextColumn get caseCode => text()(); // e.g. WLF-2026-001
  TextColumn get applicantName => text()();
  TextColumn get applicantMobile => text()();
  TextColumn get applicantNid => text().withDefault(const Constant(''))();
  TextColumn get address => text()();
  TextColumn get category => text()(); // 'medical', 'widow', 'orphan', 'disaster', 'debt_relief', 'education'
  TextColumn get fundType => text().withDefault(const Constant('zakat'))(); // 'zakat', 'sadakah', 'general_welfare'
  RealColumn get requestedAmount => real()();
  RealColumn get approvedAmount => real().withDefault(const Constant(0.0))();
  TextColumn get status => text().withDefault(const Constant('pending'))(); // pending, verified, approved, disbursed, rejected
  TextColumn get verifiedBy => text().nullable()();
  TextColumn get approvedBy => text().nullable()();
  TextColumn get notes => text().withDefault(const Constant(''))();
  TextColumn get applicationDate => text()();
  TextColumn get disbursedDate => text().nullable()();
  IntColumn get createdAt => integer()();

  @override
  Set<Column> get primaryKey => {id};
}

@DriftDatabase(tables: [
  MosquesTable,
  MembersTable,
  TransactionsTable,
  ChandaPaymentsTable,
  StaffTable,
  NoticesTable,
  MeetingsTable,
  CashboxRecordsTable,
  MadrasaStudentsTable,
  MadrasaAttendanceTable,
  HifzProgressTable,
  ExaminationsTable,
  ExamResultsTable,
  MadrasaFeePaymentsTable,
  MosqueEventsTable,
  WelfareCasesTable,
])
class AppDatabase extends _$AppDatabase {
  AppDatabase() : super(_openConnection());

  @override
  int get schemaVersion => 1;

  /// Clears all tenant-specific data when switching or logging out of a mosque
  Future<void> clearAllTenantData() async {
    await transaction(() async {
      await delete(mosquesTable).go();
      await delete(membersTable).go();
      await delete(transactionsTable).go();
      await delete(chandaPaymentsTable).go();
      await delete(staffTable).go();
      await delete(noticesTable).go();
      await delete(meetingsTable).go();
      await delete(cashboxRecordsTable).go();
      await delete(madrasaStudentsTable).go();
      await delete(madrasaAttendanceTable).go();
      await delete(hifzProgressTable).go();
      await delete(examinationsTable).go();
      await delete(examResultsTable).go();
      await delete(madrasaFeePaymentsTable).go();
      await delete(mosqueEventsTable).go();
      await delete(welfareCasesTable).go();
    });
  }
}

LazyDatabase _openConnection() {
  return LazyDatabase(() async {
    final dbFolder = await getApplicationDocumentsDirectory();
    final file = File(p.join(dbFolder.path, 'mosque_seba.sqlite'));
    return NativeDatabase.createInBackground(file);
  });
}
