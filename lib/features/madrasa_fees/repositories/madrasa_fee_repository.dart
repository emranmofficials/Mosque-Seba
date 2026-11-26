import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../core/network/supabase_service.dart';
import '../../cloud_sync/services/offline_queue_manager.dart';
import '../models/madrasa_fee_payment_model.dart';

/// Repository for Madrasa Fee Collections (মাদরাসা ফি আদায় ও হিসাব রিপোজিটরি)
class MadrasaFeeRepository {
  final OfflineQueueManager _queueManager;

  MadrasaFeeRepository({OfflineQueueManager? queueManager})
      : _queueManager = queueManager ?? OfflineQueueManager();

  SupabaseClient get _client => SupabaseService.client;

  /// Fetch all student fee payments for a mosque
  Future<List<MadrasaFeePaymentModel>> fetchFeePayments({
    required String mosqueId,
    String? monthFilter,
  }) async {
    try {
      var query = _client.from('madrasa_fee_payments').select().eq('mosque_id', mosqueId);
      if (monthFilter != null && monthFilter.isNotEmpty && monthFilter != 'all') {
        query = query.eq('month', monthFilter);
      }

      final res = await query.order('created_at', ascending: false);
      final List<dynamic> data = res as List<dynamic>;

      return data
          .map((item) => MadrasaFeePaymentModel.fromJson(item as Map<String, dynamic>))
          .toList();
    } catch (e) {
      return [];
    }
  }

  /// Collect student fee and auto-post income transaction to Finance
  Future<MadrasaFeePaymentModel?> collectFee({
    required String mosqueId,
    required MadrasaFeePaymentModel payment,
  }) async {
    try {
      final payload = payment.toJson();
      payload['mosque_id'] = mosqueId;
      if (payload['id'] == null || (payload['id'] as String).isEmpty) {
        payload['id'] = 'mfp-${DateTime.now().millisecondsSinceEpoch}';
      }

      final res = await _client.from('madrasa_fee_payments').insert(payload).select().single();
      final feeResult = MadrasaFeePaymentModel.fromJson(res);

      // Auto-post to Finance Transactions ledger (PRD §20 integration requirement)
      final txnPayload = {
        'id': 'txn-mfp-${feeResult.id}',
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

      return feeResult;
    } catch (e) {
      return null;
    }
  }
}
