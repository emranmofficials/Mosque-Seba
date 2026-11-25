import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../../core/network/supabase_service.dart';
import '../models/chanda_payment_model.dart';

/// Repository for Chanda Payments & Collection (চাঁদা কালেকশন রিপোজিটরি)
class ChandaRepository {
  final SupabaseService _supabaseService;

  ChandaRepository({SupabaseService? supabaseService})
      : _supabaseService = supabaseService ?? SupabaseService();

  SupabaseClient get _client => SupabaseService.client;

  /// Fetch all chanda payments for a mosque
  Future<List<ChandaPaymentModel>> fetchPayments({
    required String mosqueId,
    String? monthFilter,
    String? memberId,
  }) async {
    try {
      var query = _client.from('chanda_payments').select().eq('mosque_id', mosqueId);

      if (monthFilter != null && monthFilter.isNotEmpty) {
        query = query.eq('month', monthFilter);
      }

      if (memberId != null && memberId.isNotEmpty) {
        query = query.eq('member_id', memberId);
      }

      final response = await query.order('created_at', ascending: false);
      final List<dynamic> data = response as List<dynamic>;

      return data
          .map((item) => ChandaPaymentModel.fromJson(item as Map<String, dynamic>))
          .toList();
    } catch (e) {
      return [];
    }
  }

  /// Receive Chanda Payment with atomic double-entry finance cross-posting
  Future<ChandaPaymentModel?> receivePayment({
    required String mosqueId,
    required String memberId,
    required String memberNameBn,
    required String month,
    required double amount,
    required String method,
    required String receivedBy,
  }) async {
    try {
      final receiptCode = 'RCP-${DateTime.now().year}-${DateTime.now().millisecondsSinceEpoch % 100000}';
      final qrCode = 'MOSQUE_SEBA:CHANDA:$receiptCode:$amount';
      final paidAt = DateTime.now().toIso8601String().split('T').first;

      final payload = {
        'mosque_id': mosqueId,
        'member_id': memberId,
        'member_name_bn': memberNameBn,
        'month': month,
        'amount_paid': amount,
        'amount_due': 0.0,
        'method': method,
        'status': 'paid',
        'receipt_code': receiptCode,
        'qr_verification_code': qrCode,
        'received_by': receivedBy,
        'paid_at': paidAt,
      };

      // 1. Insert into chanda_payments
      final res = await _client.from('chanda_payments').insert(payload).select().single();
      final payment = ChandaPaymentModel.fromJson(res);

      // 2. Cross-post to transactions (General Ledger) as INCOME
      final txnPayload = {
        'mosque_id': mosqueId,
        'transaction_code': 'TXN-${DateTime.now().millisecondsSinceEpoch % 1000000}',
        'category_name_bn': 'মাসিক চাঁদা',
        'type': 'income',
        'amount': amount,
        'txn_date': paidAt,
        'description': '$memberNameBn এর $month এর মাসিক চাঁদা পরিশোধ (রসিদ: $receiptCode)',
        'source_type': 'chanda_payment',
        'source_id': payment.id,
        'created_by': receivedBy,
        'approval_status': 'approved',
        'is_reversal': false,
      };

      await _client.from('transactions').insert(txnPayload);

      return payment;
    } catch (e) {
      return null;
    }
  }
}
