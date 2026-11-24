part of 'app_database.dart';

// Placeholder generated file until build_runner is executed
abstract class _$AppDatabase extends GeneratedDatabase {
  _$AppDatabase(QueryExecutor e) : super(e);

  late final MosquesTable mosquesTable = MosquesTable();
  late final MembersTable membersTable = MembersTable();
  late final TransactionsTable transactionsTable = TransactionsTable();
  late final ChandaPaymentsTable chandaPaymentsTable = ChandaPaymentsTable();
  late final StaffTable staffTable = StaffTable();
  late final NoticesTable noticesTable = NoticesTable();
  late final MeetingsTable meetingsTable = MeetingsTable();
  late final CashboxRecordsTable cashboxRecordsTable = CashboxRecordsTable();

  @override
  Iterable<TableInfo<Table, dynamic>> get allTables => allSchemaEntities.whereType<TableInfo<Table, dynamic>>();

  @override
  List<DatabaseSchemaEntity> get allSchemaEntities => [
    mosquesTable,
    membersTable,
    transactionsTable,
    chandaPaymentsTable,
    staffTable,
    noticesTable,
    meetingsTable,
    cashboxRecordsTable,
  ];
}
