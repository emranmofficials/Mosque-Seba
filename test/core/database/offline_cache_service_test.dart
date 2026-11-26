import 'package:flutter_test/flutter_test.dart';
import 'package:mosqueseba/core/database/offline_cache_service.dart';
import 'package:mosqueseba/features/app_security/security/encryption_helper.dart';
import 'package:mosqueseba/features/cloud_sync/services/offline_queue_manager.dart';
import 'package:mosqueseba/features/subscriptions/repositories/chanda_repository.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group('OfflineCacheService & Chanda HMAC Offline Integration', () {
    late OfflineQueueManager queueManager;
    late OfflineCacheService cache;

    setUp(() {
      SharedPreferences.setMockInitialValues({});
      queueManager = OfflineQueueManager();
      cache = OfflineCacheService(queueManager: queueManager);
    });

    test('inserts, updates, counts, and deletes records offline with outbox queuing', () async {
      final inserted = await cache.insertRecord(
        tableName: 'notices',
        mosqueId: 'mosque-1',
        payload: {
          'title': 'জুমার বয়ান',
          'content': 'সময়মতো উপস্থিত হোন',
          'priority': 'important',
          'is_pinned': true,
        },
      );

      final recordId = inserted['id'] as String;
      expect(recordId, isNotEmpty);

      final fetched = await cache.fetchList(
        tableName: 'notices',
        mosqueId: 'mosque-1',
      );
      expect(fetched.length, 1);
      expect(fetched.first['title'], 'জুমার বয়ান');

      final counts = await cache.countCachedByTable('mosque-1');
      expect(counts['notices'], 1);

      await cache.updateRecord(
        tableName: 'notices',
        id: recordId,
        mosqueId: 'mosque-1',
        updates: {'title': 'সংশোধিত নোটিশ'},
      );
      final updated = await cache.fetchList(
        tableName: 'notices',
        mosqueId: 'mosque-1',
      );
      expect(updated.first['title'], 'সংশোধিত নোটিশ');

      await cache.deleteRecord(tableName: 'notices', id: recordId);
      final afterDelete = await cache.fetchList(
        tableName: 'notices',
        mosqueId: 'mosque-1',
      );
      expect(afterDelete, isEmpty);

      final pendingCounts = await queueManager.getPendingCountsByTable();
      expect(pendingCounts['notices'], 3);
    });

    test('ChandaRepository generates HMAC-signed QR receipt and cross-posts to finance offline', () async {
      final repo = ChandaRepository(cache: cache);
      final payment = await repo.receivePayment(
        mosqueId: 'mosque-1',
        memberId: 'mem-101',
        memberNameBn: 'আব্দুর রহমান',
        month: 'মার্চ ২০২৬',
        amount: 500.0,
        method: 'নগদ',
        receivedBy: 'কোষাধ্যক্ষ',
      );

      expect(payment, isNotNull);
      final qr = payment!.qrVerificationCode;
      final parts = qr.split(':');
      expect(parts.length, 6);
      expect(parts[0], 'MOSQUE_SEBA');
      expect(parts[1], 'CHANDA');

      final isValidSig = EncryptionHelper.verifyReceiptCode(
        receiptNo: parts[2],
        amount: double.parse(parts[3]),
        memberCode: parts[4],
        signature: parts[5],
      );
      expect(isValidSig, isTrue);

      final txns = await cache.fetchList(
        tableName: 'transactions',
        mosqueId: 'mosque-1',
      );
      expect(txns.length, 1);
      expect(txns.first['type'], 'income');
      expect((txns.first['amount'] as num).toDouble(), 500.0);
    });
  });
}
