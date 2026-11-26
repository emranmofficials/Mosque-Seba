import '../../../core/database/offline_cache_service.dart';
import '../../cloud_sync/services/offline_queue_manager.dart';
import '../models/madrasa_fee_payment_model.dart';

/// Repository for Madrasa Fee Collections (মাদরাসা ফি আদায় ও হিসাব রিপোজিটরি)
class MadrasaFeeRepository {
  final OfflineCacheService _cache;

  MadrasaFeeRepository({
    OfflineQueueManager? queueManager,
    OfflineCacheService? cache,
  }) : _cache = cache ?? OfflineCacheService(queueManager: queueManager);

  /// Fetch all student fee payments for a mosque
  Future<List<MadrasaFeePaymentModel>> fetchFeePayments({
    required String mosqueId,
    String? monthFilter,
  }) async {
    final equals = <String, dynamic>{};
    if (monthFilter != null &&
        monthFilter.isNotEmpty &&
        monthFilter != 'all' &&
        monthFilter != 'সকল') {
      equals['month'] = monthFilter;
    }

    final rows = await _cache.fetchList(
      tableName: 'madrasa_fee_payments',
      mosqueId: mosqueId,
      orderBy: 'created_at',
      ascending: false,
      equalsFilters: equals,
    );

    final list = rows.map(MadrasaFeePaymentModel.fromJson).toList();
    list.sort((a, b) => b.createdAt.compareTo(a.createdAt));
    return list;
  }

  /// Collect student fee and auto-post income transaction to Finance (offline-safe)
  Future<MadrasaFeePaymentModel?> collectFee({
    required String mosqueId,
    required MadrasaFeePaymentModel payment,
  }) async {
    try {
      final payload = payment.toJson();
      if ((payload['id']?.toString().trim() ?? '').isEmpty) {
        payload.remove('id');
      }
      if (payload['created_at'] == 0) payload.remove('created_at');

      final res = await _cache.insertRecord(
        tableName: 'madrasa_fee_payments',
        mosqueId: mosqueId,
        payload: payload,
      );
      final feeResult = MadrasaFeePaymentModel.fromJson(res);

      final txnPayload = {
        'mosque_id': mosqueId,
        'transaction_code': 'TXN-${feeResult.receiptCode}',
        'category_name_bn': 'মাদরাসা ছাত্র ফি',
        'type': 'income',
        'amount': feeResult.amountPaid,
        'txn_date': feeResult.paidAt,
        'description': 'ছাত্র ফি: ${feeResult.studentNameBn} (${feeResult.month})',
        'source_type': 'madrasa_fee',
        'source_id': feeResult.id,
        'created_by': feeResult.receivedBy,
        'approval_status': 'approved',
        'is_reversal': false,
      };

      await _cache.insertRecord(
        tableName: 'transactions',
        mosqueId: mosqueId,
        payload: txnPayload,
      );

      return feeResult;
    } catch (_) {
      return null;
    }
  }
}
