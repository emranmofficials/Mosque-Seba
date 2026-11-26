import 'package:flutter_test/flutter_test.dart';
import 'package:mosqueseba/core/database/offline_cache_service.dart';
import 'package:mosqueseba/features/app_security/security/encryption_helper.dart';
import 'package:mosqueseba/features/cashbox/models/cashbox_record_model.dart';
import 'package:mosqueseba/features/cloud_sync/services/offline_queue_manager.dart';
import 'package:mosqueseba/features/elections/models/election_candidate_model.dart';
import 'package:mosqueseba/features/madrasa_attendance/models/madrasa_attendance_model.dart';
import 'package:mosqueseba/features/notices/models/notice_model.dart';
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

    test('normalizeCloudPayload converts epoch millis and strips non-positive timestamps', () {
      final positive = OfflineCacheService.normalizeCloudPayload({
        'id': 'evt-1',
        'created_at': 1795689600000,
        'counted_at': 1795689600000,
      });
      expect(positive['created_at'], isA<String>());
      expect((positive['created_at'] as String).endsWith('Z'), isTrue);
      expect(positive['counted_at'], isA<String>());

      final zeroRemoved = OfflineCacheService.normalizeCloudPayload({
        'id': 'evt-2',
        'created_at': 0,
        'counted_at': '',
      });
      expect(zeroRemoved.containsKey('created_at'), isFalse);
      expect(zeroRemoved.containsKey('counted_at'), isFalse);
    });

    test('models decode legacy Supabase column names transparently', () {
      final notice = NoticeModel.fromJson({
        'id': 'n1',
        'title': 'জুমার নামাজ',
        'content': 'সময়মতো আসুন',
        'publish_at': '2026-11-26T10:00:00Z',
        'expiry_at': '2026-12-01',
      });
      expect(notice.publishDate, '2026-11-26');
      expect(notice.expiryDate, '2026-12-01');

      final attendance = MadrasaAttendanceModel.fromJson({
        'id': 'a1',
        'student_id': 's1',
        'date': '2026-11-26',
        'status': 'present',
      });
      expect(attendance.attendanceDate, '2026-11-26');

      final candidate = ElectionCandidateModel.fromJson({
        'id': 'c1',
        'election_id': 'el-1',
        'target_role_code': 'secretary',
        'target_role_title_bn': 'সাধারণ সম্পাদক',
        'candidate_name_bn': 'আবদুল করিম',
        'proposer_name_bn': 'রফিকুল ইসলাম',
        'seconder_name_bn': 'নজরুল ইসলাম',
      });
      expect(candidate.scheduleId, 'el-1');
      expect(candidate.targetRole, 'secretary');
      expect(candidate.targetRoleBn, 'সাধারণ সম্পাদক');
      expect(candidate.candidateName, 'আবদুল করিম');

      final cashbox = CashboxRecordModel.fromJson({
        'id': 'cb1',
        'collection_date': '2026-11-26',
        'witness_1_name': 'সাক্ষী ১',
        'witness_2_name': 'সাক্ষী ২',
        'created_at': '2026-11-26T12:00:00Z',
      });
      expect(cashbox.waqtDate, '2026-11-26');
      expect(cashbox.countedAt, greaterThan(0));
    });
  });
}
