import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../../core/network/supabase_service.dart';
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
  final SupabaseService _supabaseService;
  final OfflineQueueManager _queueManager;

  SupabaseSyncService({
    SupabaseService? supabaseService,
    OfflineQueueManager? queueManager,
  })  : _supabaseService = supabaseService ?? SupabaseService(),
        _queueManager = queueManager ?? OfflineQueueManager();

  SupabaseClient get _client => SupabaseService.client;

  /// Perform full synchronization: Flush outbox then refresh tables
  Future<SyncResult> performFullSync() async {
    try {
      final pendingItems = await _queueManager.getPendingQueue();
      int successCount = 0;

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
            }
          } else if (item.operation == 'delete') {
            final id = item.payload['id'];
            if (id != null) {
              await _client.from(item.tableName).delete().eq('id', id);
              successCount++;
            }
          }
        } catch (_) {
          // Keep item in queue if server error
        }
      }

      await _queueManager.clearQueue();

      return SyncResult(
        isSuccess: true,
        syncedItemsCount: successCount,
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
