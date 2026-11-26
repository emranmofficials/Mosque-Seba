import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../core/network/supabase_service.dart';
import '../../cloud_sync/services/offline_queue_manager.dart';
import '../models/welfare_case_model.dart';

/// Repository for Welfare & Zakat Applications (যাকাত ও সমাজকল্যাণ রিপোজিটরি)
class WelfareRepository {
  final OfflineQueueManager _queueManager;

  WelfareRepository({OfflineQueueManager? queueManager})
      : _queueManager = queueManager ?? OfflineQueueManager();

  SupabaseClient get _client => SupabaseService.client;

  /// Fetch all welfare cases for a mosque with optional category and status filters
  Future<List<WelfareCaseModel>> fetchCases({
    required String mosqueId,
    String? categoryFilter,
    String? statusFilter,
  }) async {
    try {
      var query = _client.from('welfare_cases').select().eq('mosque_id', mosqueId);
      if (categoryFilter != null && categoryFilter.isNotEmpty && categoryFilter != 'all') {
        query = query.eq('category', categoryFilter);
      }
      if (statusFilter != null && statusFilter.isNotEmpty && statusFilter != 'all') {
        query = query.eq('status', statusFilter);
      }

      final res = await query.order('created_at', ascending: false);
      final List<dynamic> data = res as List<dynamic>;

      return data
          .map((item) => WelfareCaseModel.fromJson(item as Map<String, dynamic>))
          .toList();
    } catch (e) {
      return [];
    }
  }

  /// Submit a new welfare case application
  Future<WelfareCaseModel?> submitCase({
    required String mosqueId,
    required WelfareCaseModel welfareCase,
  }) async {
    try {
      final payload = welfareCase.toJson();
      payload['mosque_id'] = mosqueId;
      if (payload['id'] == null || (payload['id'] as String).isEmpty) {
        payload['id'] = 'wlf-${DateTime.now().millisecondsSinceEpoch}';
      }

      final res = await _client.from('welfare_cases').insert(payload).select().single();
      return WelfareCaseModel.fromJson(res);
    } catch (e) {
      return null;
    }
  }

  /// Approve grant and record disbursement with Finance integration
  Future<bool> approveAndDisburse({
    required String mosqueId,
    required String caseId,
    required double approvedAmount,
    required String approverName,
    required bool disburseNow,
  }) async {
    try {
      final now = DateTime.now();
      final dateStr =
          '${now.year}-${now.month.toString().padLeft(2, '0')}-${now.day.toString().padLeft(2, '0')}';

      final updatePayload = {
        'approved_amount': approvedAmount,
        'approved_by': approverName,
        'status': disburseNow ? 'disbursed' : 'approved',
        if (disburseNow) 'disbursed_date': dateStr,
      };

      await _client
          .from('welfare_cases')
          .update(updatePayload)
          .eq('id', caseId)
          .eq('mosque_id', mosqueId);

      // Auto-post to Finance Transactions ledger (PRD §26)
      if (disburseNow) {
        final txnPayload = {
          'id': 'txn-wlf-$caseId',
          'mosque_id': mosqueId,
          'transaction_code': 'TXN-WLF-${caseId.hashCode.abs() % 10000}',
          'category_name_bn': 'যাকাত ও সমাজকল্যাণ বিতরণ',
          'type': 'expense',
          'amount': approvedAmount,
          'txn_date': dateStr,
          'description': 'সমাজকল্যাণ অনুদান বিতরণ (কেস আইডি: $caseId)',
          'source_type': 'welfare_grant',
          'source_id': caseId,
          'created_by': approverName,
          'approval_status': 'approved',
          'is_reversal': false,
          'created_at': DateTime.now().millisecondsSinceEpoch,
        };
        try {
          await _client.from('transactions').insert(txnPayload);
        } catch (_) {
          await _queueManager.enqueue(
            tableName: 'transactions',
            operation: 'insert',
            payload: txnPayload,
          );
        }
      }

      return true;
    } catch (e) {
      return false;
    }
  }
}
