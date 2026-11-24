import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../services/offline_queue_manager.dart';
import '../services/supabase_sync_service.dart';

final offlineQueueManagerProvider = Provider<OfflineQueueManager>((ref) {
  return OfflineQueueManager();
});

final supabaseSyncServiceProvider = Provider<SupabaseSyncService>((ref) {
  final queue = ref.watch(offlineQueueManagerProvider);
  return SupabaseSyncService(queueManager: queue);
});

class SyncState {
  final bool isSyncing;
  final int pendingOutboxCount;
  final DateTime? lastSyncedAt;
  final String? errorMessage;

  const SyncState({
    this.isSyncing = false,
    this.pendingOutboxCount = 0,
    this.lastSyncedAt,
    this.errorMessage,
  });

  SyncState copyWith({
    bool? isSyncing,
    int? pendingOutboxCount,
    DateTime? lastSyncedAt,
    String? errorMessage,
  }) {
    return SyncState(
      isSyncing: isSyncing ?? this.isSyncing,
      pendingOutboxCount: pendingOutboxCount ?? this.pendingOutboxCount,
      lastSyncedAt: lastSyncedAt ?? this.lastSyncedAt,
      errorMessage: errorMessage,
    );
  }
}

class SyncNotifier extends StateNotifier<SyncState> {
  final SupabaseSyncService _syncService;
  final OfflineQueueManager _queueManager;

  SyncNotifier(this._syncService, this._queueManager)
      : super(SyncState(lastSyncedAt: DateTime.now())) {
    checkPendingCount();
  }

  Future<void> checkPendingCount() async {
    final queue = await _queueManager.getPendingQueue();
    state = state.copyWith(pendingOutboxCount: queue.length);
  }

  Future<bool> triggerSync() async {
    state = state.copyWith(isSyncing: true, errorMessage: null);

    final result = await _syncService.performFullSync();

    state = state.copyWith(
      isSyncing: false,
      pendingOutboxCount: 0,
      lastSyncedAt: result.syncedAt,
      errorMessage: result.isSuccess ? null : result.errorMessage,
    );

    return result.isSuccess;
  }
}

final syncNotifierProvider = StateNotifierProvider<SyncNotifier, SyncState>((ref) {
  final service = ref.watch(supabaseSyncServiceProvider);
  final queue = ref.watch(offlineQueueManagerProvider);
  return SyncNotifier(service, queue);
});
