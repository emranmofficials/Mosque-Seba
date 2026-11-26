import '../../../core/database/offline_cache_service.dart';
import '../models/cashbox_record_model.dart';

/// Repository for Jumu'ah Cashbox Records (জুমার দানবাক্স রিপোজিটরি)
class CashboxRepository {
  final OfflineCacheService _cache;

  CashboxRepository({OfflineCacheService? cache})
      : _cache = cache ?? OfflineCacheService.instance;

  /// Fetch all Friday cashbox counting records for a mosque
  Future<List<CashboxRecordModel>> fetchRecords({required String mosqueId}) async {
    final rows = await _cache.fetchList(
      tableName: 'cashbox_records',
      mosqueId: mosqueId,
      orderBy: 'counted_at',
      ascending: false,
    );

    final list = rows.map(CashboxRecordModel.fromJson).toList();
    list.sort((a, b) => b.countedAt.compareTo(a.countedAt));
    return list;
  }

  /// Save new counting record and auto double-entry post to Finance Transactions
  Future<CashboxRecordModel?> recordCashboxCount({
    required String mosqueId,
    required CashboxRecordModel record,
  }) async {
    try {
      final voucherCode =
          'BOX-${DateTime.now().year}-${DateTime.now().millisecondsSinceEpoch % 100000}';
      final payload = record.toJson();
      if ((payload['id']?.toString().trim() ?? '').isEmpty) {
        payload.remove('id');
      }
      payload['mosque_id'] = mosqueId;
      payload['voucher_code'] = voucherCode;

      final res = await _cache.insertRecord(
        tableName: 'cashbox_records',
        mosqueId: mosqueId,
        payload: payload,
      );
      final savedRecord = CashboxRecordModel.fromJson(res);

      // Auto-post to Finance General Ledger as INCOME under "জুমার দানবাক্স"
      final txnPayload = {
        'mosque_id': mosqueId,
        'transaction_code':
            'TXN-${DateTime.now().year}-${DateTime.now().millisecondsSinceEpoch % 1000000}',
        'category_name_bn': 'জুমার দানবাক্স',
        'type': 'income',
        'amount': savedRecord.totalAmount,
        'txn_date': savedRecord.waqtDate,
        'description':
            'পবিত্র জুমার জামাত শেষে ৩ সাক্ষীর উপস্থিতিতে দানবাক্স গণনা (ভাউচার: $voucherCode)',
        'source_type': 'jummah_collection',
        'source_id': savedRecord.id,
        'created_by': savedRecord.witness1Name,
        'approved_by': savedRecord.witness2Name,
        'approval_status': 'approved',
        'is_reversal': false,
      };

      await _cache.insertRecord(
        tableName: 'transactions',
        mosqueId: mosqueId,
        payload: txnPayload,
      );

      return savedRecord;
    } catch (_) {
      return null;
    }
  }

  /// Delete a cashbox counting record
  Future<bool> deleteCashboxRecord(String id) async {
    try {
      return await _cache.deleteRecord(tableName: 'cashbox_records', id: id);
    } catch (_) {
      return false;
    }
  }
}
