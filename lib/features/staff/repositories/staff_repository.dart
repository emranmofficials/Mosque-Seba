import '../../../core/database/offline_cache_service.dart';
import '../models/staff_model.dart';
import '../models/staff_payroll_model.dart';

/// Repository for Staff & Payroll Management (স্টাফ ও বেতন রিপোজিটরি)
class StaffRepository {
  final OfflineCacheService _cache;

  StaffRepository({OfflineCacheService? cache})
      : _cache = cache ?? OfflineCacheService.instance;

  /// Fetch all active staff members
  Future<List<StaffModel>> fetchStaff({required String mosqueId}) async {
    final rows = await _cache.fetchList(
      tableName: 'staff',
      mosqueId: mosqueId,
      orderBy: 'salary_amount',
      ascending: false,
      equalsFilters: {'is_active': true},
    );

    final list = rows.map(StaffModel.fromJson).where((s) => s.isActive).toList();
    list.sort((a, b) => b.salaryAmount.compareTo(a.salaryAmount));
    return list;
  }

  /// Create or register a new staff member
  Future<StaffModel?> createStaff({
    required String mosqueId,
    required StaffModel staff,
  }) async {
    try {
      final payload = staff.toJson();
      if ((payload['id']?.toString().trim() ?? '').isEmpty) {
        payload.remove('id');
      }
      final res = await _cache.insertRecord(
        tableName: 'staff',
        mosqueId: mosqueId,
        payload: payload,
      );
      return StaffModel.fromJson(res);
    } catch (_) {
      return null;
    }
  }

  /// Update an existing staff record
  Future<bool> updateStaff(StaffModel staff, {String? mosqueId}) async {
    try {
      return await _cache.updateRecord(
        tableName: 'staff',
        id: staff.id,
        mosqueId: mosqueId,
        updates: staff.toJson(),
      );
    } catch (_) {
      return false;
    }
  }

  /// Deactivate a staff member without erasing payroll history
  Future<bool> deactivateStaff(String staffId, {String? mosqueId}) async {
    try {
      return await _cache.updateRecord(
        tableName: 'staff',
        id: staffId,
        mosqueId: mosqueId,
        updates: {'is_active': false},
      );
    } catch (_) {
      return false;
    }
  }

  /// Fetch payroll history for a mosque
  Future<List<StaffPayrollModel>> fetchPayrollHistory({
    required String mosqueId,
    String? monthFilter,
  }) async {
    final equals = <String, dynamic>{};
    if (monthFilter != null && monthFilter.isNotEmpty && monthFilter != 'সকল') {
      equals['month'] = monthFilter;
    }

    final rows = await _cache.fetchList(
      tableName: 'staff_payroll',
      mosqueId: mosqueId,
      orderBy: 'created_at',
      ascending: false,
      equalsFilters: equals,
    );

    final list = rows.map(StaffPayrollModel.fromJson).toList();
    list.sort((a, b) => b.createdAt.compareTo(a.createdAt));
    return list;
  }

  /// Disburse salary payment with offline-safe Finance cross-posting
  Future<StaffPayrollModel?> disburseSalary({
    required String mosqueId,
    required String staffId,
    required String staffNameBn,
    required String month,
    required double baseSalary,
    required double bonusAmount,
    required String disbursedBy,
  }) async {
    try {
      final netSalary = baseSalary + bonusAmount;
      final paidDate = DateTime.now().toIso8601String().split('T').first;

      final payrollPayload = {
        'mosque_id': mosqueId,
        'staff_id': staffId,
        'staff_name_bn': staffNameBn,
        'month': month,
        'salary_amount': baseSalary,
        'bonus_amount': bonusAmount,
        'net_salary': netSalary,
        'payment_status': 'paid',
        'paid_date': paidDate,
      };

      final res = await _cache.insertRecord(
        tableName: 'staff_payroll',
        mosqueId: mosqueId,
        payload: payrollPayload,
      );
      final payroll = StaffPayrollModel.fromJson(res);

      final txnPayload = {
        'mosque_id': mosqueId,
        'transaction_code':
            'TXN-${DateTime.now().year}-${DateTime.now().millisecondsSinceEpoch % 1000000}',
        'category_name_bn': 'ইমাম-মুয়াজ্জিন বেতনভাতা',
        'type': 'expense',
        'amount': netSalary,
        'txn_date': paidDate,
        'description': '$staffNameBn এর $month মাসের বেতন ও ভাতা প্রদান',
        'source_type': 'payroll',
        'source_id': payroll.id,
        'created_by': disbursedBy,
        'approval_status': 'approved',
        'is_reversal': false,
      };

      await _cache.insertRecord(
        tableName: 'transactions',
        mosqueId: mosqueId,
        payload: txnPayload,
      );

      return payroll;
    } catch (_) {
      return null;
    }
  }
}
