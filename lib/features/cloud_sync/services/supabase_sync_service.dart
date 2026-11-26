import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../core/database/offline_cache_service.dart';
import '../../../core/network/supabase_service.dart';
import 'offline_queue_manager.dart';

class SyncResult {
  final bool isSuccess;
  final int syncedItemsCount;
  final int pulledRecordsCount;
  final String? errorMessage;
  final DateTime syncedAt;

  const SyncResult({
    required this.isSuccess,
    required this.syncedItemsCount,
    this.pulledRecordsCount = 0,
    this.errorMessage,
    required this.syncedAt,
  });
}

/// Bidirectional Supabase Cloud Synchronization Service
class SupabaseSyncService {
  static const List<String> syncableTables = [
    'members',
    'transactions',
    'chanda_payments',
    'cashbox_records',
    'staff',
    'staff_payroll',
    'duty_roster',
    'food_schedules',
    'notices',
    'notifications',
    'meetings',
    'election_schedules',
    'mosque_properties',
    'madrasa_students',
    'madrasa_attendance',
    'hifz_progress',
    'examinations',
    'exam_results',
    'madrasa_fee_payments',
    'mosque_events',
    'welfare_cases',
  ];

  final OfflineQueueManager _queueManager;
  final OfflineCacheService _cacheService;

  SupabaseSyncService({
    OfflineQueueManager? queueManager,
    OfflineCacheService? cacheService,
  })  : _queueManager = queueManager ?? OfflineQueueManager(),
        _cacheService = cacheService ?? OfflineCacheService.instance;

  SupabaseClient get _client => SupabaseService.client;

  /// Performs two-way sync:
  /// 1) Flushes queued offline mutations (retaining failures)
  /// 2) Pulls latest records for all core mosque tables into SQLite cache
  Future<SyncResult> performFullSync({String? mosqueId}) async {
    try {
      if (!SupabaseService.isInitialized) {
        return SyncResult(
          isSuccess: false,
          syncedItemsCount: 0,
          errorMessage: 'ক্লাউড সার্ভারের সাথে সংযোগ নেই',
          syncedAt: DateTime.now(),
        );
      }

      final pendingItems = await _queueManager.getPendingQueue();
      int successCount = 0;
      final remainingItems = <OutboxItem>[];
      String? lastError;

      // 1. Push Outbox Queue (Upload offline changes)
      for (final item in pendingItems) {
        try {
          if (item.operation == 'insert') {
            final cleanPayload = OfflineCacheService.normalizeCloudPayload(item.payload);
            await _client.from(item.tableName).upsert(cleanPayload);
            successCount++;
          } else if (item.operation == 'update') {
            final id = item.payload['id'];
            if (id != null) {
              final updates = OfflineCacheService.normalizeCloudPayload(item.payload)
                ..remove('id');
              await _client.from(item.tableName).update(updates).eq('id', id);
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

      // 2. Pull fresh snapshots into Drift SQLite when mosqueId is active
      int pulledCount = 0;
      if (mosqueId != null && mosqueId.isNotEmpty) {
        for (final table in syncableTables) {
          try {
            final rows = await _cacheService.fetchList(
              tableName: table,
              mosqueId: mosqueId,
            );
            pulledCount += rows.length;
          } catch (_) {}
        }
      }

      return SyncResult(
        isSuccess: remainingItems.isEmpty,
        syncedItemsCount: successCount,
        pulledRecordsCount: pulledCount,
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
