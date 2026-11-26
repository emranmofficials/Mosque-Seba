import '../../../core/database/offline_cache_service.dart';
import '../../app_security/security/encryption_helper.dart';
import '../models/chanda_payment_model.dart';

/// Repository for Chanda Payments & Collection (চাঁদা কালেকশন রিপোজিটরি)
class ChandaRepository {
  final OfflineCacheService _cache;

  ChandaRepository({OfflineCacheService? cache})
      : _cache = cache ?? OfflineCacheService.instance;

  /// Fetch all chanda payments for a mosque
  Future<List<ChandaPaymentModel>> fetchPayments({
    required String mosqueId,
    String? monthFilter,
    String? memberId,
  }) async {
    final equals = <String, dynamic>{};
    if (monthFilter != null && monthFilter.isNotEmpty && monthFilter != 'সকল') {
      equals['month'] = monthFilter;
    }
    if (memberId != null && memberId.isNotEmpty) {
      equals['member_id'] = memberId;
    }

    final rows = await _cache.fetchList(
      tableName: 'chanda_payments',
      mosqueId: mosqueId,
      orderBy: 'created_at',
      ascending: false,
      equalsFilters: equals,
    );

    final list = rows.map(ChandaPaymentModel.fromJson).toList();
    list.sort((a, b) => b.createdAt.compareTo(a.createdAt));
    return list;
  }

  /// Receive Chanda Payment with HMAC-signed QR & offline-safe Finance cross-posting
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
      final receiptCode =
          'RCP-${DateTime.now().year}-${DateTime.now().millisecondsSinceEpoch % 100000}';
      final hmacSig = EncryptionHelper.generateReceiptSignature(
        receiptNo: receiptCode,
        amount: amount,
        memberCode: memberId,
      );
      final qrCode = 'MOSQUE_SEBA:CHANDA:$receiptCode:$amount:$memberId:$hmacSig';
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

      // 1. Insert into chanda_payments (offline-safe)
      final res = await _cache.insertRecord(
        tableName: 'chanda_payments',
        mosqueId: mosqueId,
        payload: payload,
      );
      final payment = ChandaPaymentModel.fromJson(res);

      // 2. Cross-post to transactions (General Ledger) as INCOME (offline-safe)
      final txnPayload = {
        'mosque_id': mosqueId,
        'transaction_code': 'TXN-${DateTime.now().millisecondsSinceEpoch % 1000000}',
        'category_name_bn': 'মাসিক চাঁদা',
        'type': 'income',
        'amount': amount,
        'txn_date': paidAt,
        'description':
            '$memberNameBn এর $month এর মাসিক চাঁদা পরিশোধ (রসিদ: $receiptCode)',
        'source_type': 'chanda_payment',
        'source_id': payment.id,
        'created_by': receivedBy,
        'approval_status': 'approved',
        'is_reversal': false,
      };

      await _cache.insertRecord(
        tableName: 'transactions',
        mosqueId: mosqueId,
        payload: txnPayload,
      );

      return payment;
    } catch (_) {
      return null;
    }
  }
}
