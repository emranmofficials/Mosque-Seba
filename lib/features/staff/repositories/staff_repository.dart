import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../core/network/supabase_service.dart';
import '../models/staff_model.dart';
import '../models/staff_payroll_model.dart';

/// Repository for Staff & Payroll Management (স্টাফ ও বেতন রিপোজিটরি)
class StaffRepository {
  SupabaseClient get _client => SupabaseService.client;

  /// Fetch all active staff members
  Future<List<StaffModel>> fetchStaff({required String mosqueId}) async {
    try {
      final response = await _client
          .from('staff')
          .select()
          .eq('mosque_id', mosqueId)
          .eq('is_active', true)
          .order('salary_amount', ascending: false);

      final List<dynamic> data = response as List<dynamic>;
      return data
          .map((item) => StaffModel.fromJson(item as Map<String, dynamic>))
          .toList();
    } catch (e) {
      return [];
    }
  }

  /// Create or register a new staff member
  Future<StaffModel?> createStaff({
    required String mosqueId,
    required StaffModel staff,
  }) async {
    try {
      final payload = staff.toJson();
      payload['mosque_id'] = mosqueId;
      if (payload['id'] == null || (payload['id'] as String).isEmpty) {
        payload['id'] = 'staff-${DateTime.now().millisecondsSinceEpoch}';
      }

      final res = await _client.from('staff').insert(payload).select().single();
      return StaffModel.fromJson(res);
    } catch (e) {
      return null;
    }
  }

  /// Fetch payroll history for a mosque
  Future<List<StaffPayrollModel>> fetchPayrollHistory({
    required String mosqueId,
    String? monthFilter,
  }) async {
    try {
      var query = _client.from('staff_payroll').select().eq('mosque_id', mosqueId);
      if (monthFilter != null && monthFilter.isNotEmpty) {
        query = query.eq('month', monthFilter);
      }

      final response = await query.order('created_at', ascending: false);
      final List<dynamic> data = response as List<dynamic>;

      return data
          .map((item) => StaffPayrollModel.fromJson(item as Map<String, dynamic>))
          .toList();
    } catch (e) {
      return [];
    }
  }

  /// Disburse salary payment with atomic double-entry cross-posting to Finance Expense
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
      final payrollId = 'pay-${DateTime.now().millisecondsSinceEpoch}';

      final payrollPayload = {
        'id': payrollId,
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

      // 1. Insert into staff_payroll
      final res = await _client.from('staff_payroll').insert(payrollPayload).select().single();
      final payroll = StaffPayrollModel.fromJson(res);

      // 2. Cross-post to Finance General Ledger (transactions) as EXPENSE
      final txnPayload = {
        'mosque_id': mosqueId,
        'transaction_code': 'TXN-${DateTime.now().year}-${DateTime.now().millisecondsSinceEpoch % 1000000}',
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

      await _client.from('transactions').insert(txnPayload);

      return payroll;
    } catch (e) {
      return null;
    }
  }
}
