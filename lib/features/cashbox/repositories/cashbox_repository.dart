import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../../core/network/supabase_service.dart';
import '../models/cashbox_record_model.dart';

/// Repository for Jumu'ah Cashbox Records (জুমার দানবাক্স রিপোজিটরি)
class CashboxRepository {
  final SupabaseService _supabaseService;

  CashboxRepository({SupabaseService? supabaseService})
      : _supabaseService = supabaseService ?? SupabaseService();

  SupabaseClient get _client => _supabaseService.client;

  /// Fetch all Friday cashbox counting records
  Future<List<CashboxRecordModel>> fetchRecords({required String mosqueId}) async {
    try {
      final response = await _client
          .from('cashbox_records')
          .select()
          .order('counted_at', ascending: false);

      final List<dynamic> data = response as List<dynamic>;
      return data
          .map((item) => CashboxRecordModel.fromJson(item as Map<String, dynamic>))
          .toList();
    } catch (e) {
      return [];
    }
  }

  /// Save new counting record and auto double-entry post to Finance Transactions
  Future<CashboxRecordModel?> recordCashboxCount({
    required String mosqueId,
    required CashboxRecordModel record,
  }) async {
    try {
      final voucherCode = 'BOX-${DateTime.now().year}-${DateTime.now().millisecondsSinceEpoch % 100000}';
      final payload = record.toJson();
      if (payload['id'] == null || (payload['id'] as String).isEmpty) {
        payload['id'] = 'cb-${DateTime.now().millisecondsSinceEpoch}';
      }
      payload['voucher_code'] = voucherCode;

      // 1. Insert into cashbox_records
      final res = await _client.from('cashbox_records').insert(payload).select().single();
      final savedRecord = CashboxRecordModel.fromJson(res);

      // 2. Auto-post to Finance General Ledger as INCOME under "জুমার দানবাক্স"
      final txnPayload = {
        'mosque_id': mosqueId,
        'transaction_code': 'TXN-${DateTime.now().year}-${DateTime.now().millisecondsSinceEpoch % 1000000}',
        'category_name_bn': 'জুমার দানবাক্স',
        'type': 'income',
        'amount': savedRecord.totalAmount,
        'txn_date': savedRecord.waqtDate,
        'description': 'পবিত্র জুমার জামাত শেষে ৩ সাক্ষীর উপস্থিতিতে দানবাক্স গণনা (ভাউচার: $voucherCode)',
        'source_type': 'jummah_collection',
        'source_id': savedRecord.id,
        'created_by': savedRecord.witness1Name,
        'approved_by': savedRecord.witness2Name,
        'approval_status': 'approved',
        'is_reversal': false,
      };

      await _client.from('transactions').insert(txnPayload);

      return savedRecord;
    } catch (e) {
      return null;
    }
  }
}
