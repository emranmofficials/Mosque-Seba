import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../core/network/supabase_service.dart';
import 'offline_queue_manager.dart';

class SyncResult {
  final bool isSuccess;
  final int syncedItemsCount;
  final String? errorMessage;
  final DateTime syncedAt;

  const SyncResult({
    required this.isSuccess,
    required this.syncedItemsCount,
    this.errorMessage,
    required this.syncedAt,
  });
}

/// Bidirectional Supabase Cloud Synchronization Service
class SupabaseSyncService {
  final OfflineQueueManager _queueManager;

  SupabaseSyncService({
    OfflineQueueManager? queueManager,
  }) : _queueManager = queueManager ?? OfflineQueueManager();

  SupabaseClient get _client => SupabaseService.client;

  /// Perform full synchronization: Flush outbox while retaining failed items
  Future<SyncResult> performFullSync() async {
    try {
      final pendingItems = await _queueManager.getPendingQueue();
      int successCount = 0;
      final remainingItems = <OutboxItem>[];
      String? lastError;

      // 1. Process Outbox Queue (Upload offline changes)
      for (final item in pendingItems) {
        try {
          if (item.operation == 'insert') {
            await _client.from(item.tableName).upsert(item.payload);
            successCount++;
          } else if (item.operation == 'update') {
            final id = item.payload['id'];
            if (id != null) {
              await _client.from(item.tableName).update(item.payload).eq('id', id);
              successCount++;
            } else {
              remainingItems.add(item.copyWith(retryCount: item.retryCount + 1));
            }
          } else if (item.operation == 'delete') {
            final id = item.payload['id'];
            if (id != null) {
              await _client.from(item.tableName).delete().eq('id', id);
              successCount++;
            } else {
              remainingItems.add(item.copyWith(retryCount: item.retryCount + 1));
            }
          }
        } catch (e) {
          lastError = e.toString();
          remainingItems.add(item.copyWith(retryCount: item.retryCount + 1));
        }
      }

      await _queueManager.replaceQueue(remainingItems);

      return SyncResult(
        isSuccess: remainingItems.isEmpty,
        syncedItemsCount: successCount,
        errorMessage: remainingItems.isNotEmpty
            ? '${remainingItems.length} টি আইটেম সিঙ্ক ব্যর্থ হয়েছে: ${lastError ?? ''}'
            : null,
        syncedAt: DateTime.now(),
      );
    } catch (e) {
      return SyncResult(
        isSuccess: false,
        syncedItemsCount: 0,
        errorMessage: e.toString(),
        syncedAt: DateTime.now(),
      );
    }
  }
}
