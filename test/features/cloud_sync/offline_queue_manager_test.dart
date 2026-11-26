import 'package:flutter_test/flutter_test.dart';
import 'package:mosqueseba/features/cloud_sync/services/offline_queue_manager.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  group('OfflineQueueManager', () {
    setUp(() {
      SharedPreferences.setMockInitialValues({});
    });

    test('enqueues, reads, replaces, and clears outbox items', () async {
      final manager = OfflineQueueManager();
      expect(await manager.getPendingQueue(), isEmpty);

      await manager.enqueue(
        tableName: 'transactions',
        operation: 'insert',
        payload: {'id': 'txn-1', 'amount': 500},
      );
      await manager.enqueue(
        tableName: 'notices',
        operation: 'insert',
        payload: {'id': 'not-1', 'title': 'Test'},
      );

      final items = await manager.getPendingQueue();
      expect(items.length, 2);
      expect(items.first.tableName, 'transactions');

      // Retain only failed second item with incremented retryCount
      await manager.replaceQueue([items.last.copyWith(retryCount: 1)]);
      final remaining = await manager.getPendingQueue();
      expect(remaining.length, 1);
      expect(remaining.first.tableName, 'notices');
      expect(remaining.first.retryCount, 1);

      await manager.clearQueue();
      expect(await manager.getPendingQueue(), isEmpty);
    });
  });
}
