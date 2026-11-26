import '../../../core/database/offline_cache_service.dart';
import '../../cloud_sync/services/offline_queue_manager.dart';
import '../models/welfare_case_model.dart';

/// Repository for Welfare & Zakat Applications (যাকাত ও সমাজকল্যাণ রিপোজিটরি)
class WelfareRepository {
  final OfflineCacheService _cache;

  WelfareRepository({
    OfflineQueueManager? queueManager,
    OfflineCacheService? cache,
  }) : _cache = cache ?? OfflineCacheService(queueManager: queueManager);

  /// Fetch all welfare cases for a mosque with optional category and status filters
  Future<List<WelfareCaseModel>> fetchCases({
    required String mosqueId,
    String? categoryFilter,
    String? statusFilter,
  }) async {
    final equals = <String, dynamic>{};
    if (categoryFilter != null && categoryFilter.isNotEmpty && categoryFilter != 'all') {
      equals['category'] = categoryFilter;
    }
    if (statusFilter != null && statusFilter.isNotEmpty && statusFilter != 'all') {
      equals['status'] = statusFilter;
    }

    final rows = await _cache.fetchList(
      tableName: 'welfare_cases',
      mosqueId: mosqueId,
      orderBy: 'created_at',
      ascending: false,
      equalsFilters: equals,
    );

    final list = rows.map(WelfareCaseModel.fromJson).toList();
    list.sort((a, b) => b.createdAt.compareTo(a.createdAt));
    return list;
  }

  /// Submit a new welfare case application
  Future<WelfareCaseModel?> submitCase({
    required String mosqueId,
    required WelfareCaseModel welfareCase,
  }) async {
    try {
      final payload = welfareCase.toJson();
      if ((payload['id']?.toString().trim() ?? '').isEmpty) {
        payload.remove('id');
      }
      final res = await _cache.insertRecord(
        tableName: 'welfare_cases',
        mosqueId: mosqueId,
        payload: payload,
      );
      return WelfareCaseModel.fromJson(res);
    } catch (_) {
      return null;
    }
  }

  /// Approve, disburse, or reject welfare case with Finance integration
  Future<bool> approveAndDisburse({
    required String mosqueId,
    required String caseId,
    required double approvedAmount,
    required String approverName,
    required bool disburseNow,
    String statusOverride = '',
  }) async {
    try {
      final now = DateTime.now();
      final dateStr =
          '${now.year}-${now.month.toString().padLeft(2, '0')}-${now.day.toString().padLeft(2, '0')}';

      final targetStatus = statusOverride.isNotEmpty
          ? statusOverride
          : (disburseNow ? 'disbursed' : 'approved');

      final updatePayload = {
        'approved_amount': approvedAmount,
        'approved_by': approverName,
        'status': targetStatus,
        if (disburseNow) 'disbursed_date': dateStr,
      };

      await _cache.updateRecord(
        tableName: 'welfare_cases',
        id: caseId,
        mosqueId: mosqueId,
        updates: updatePayload,
      );

      if (disburseNow && targetStatus == 'disbursed') {
        final txnPayload = {
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
        };
        await _cache.insertRecord(
          tableName: 'transactions',
          mosqueId: mosqueId,
          payload: txnPayload,
        );
      }

      return true;
    } catch (_) {
      return false;
    }
  }
}
