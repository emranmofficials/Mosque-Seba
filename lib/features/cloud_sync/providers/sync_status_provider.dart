import 'dart:async';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../authentication/providers/auth_provider.dart';
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
  final Map<String, int> pendingByTable;
  final int pulledRecordsCount;
  final DateTime? lastSyncedAt;
  final String? errorMessage;

  const SyncState({
    this.isSyncing = false,
    this.pendingOutboxCount = 0,
    this.pendingByTable = const {},
    this.pulledRecordsCount = 0,
    this.lastSyncedAt,
    this.errorMessage,
  });

  SyncState copyWith({
    bool? isSyncing,
    int? pendingOutboxCount,
    Map<String, int>? pendingByTable,
    int? pulledRecordsCount,
    DateTime? lastSyncedAt,
    String? errorMessage,
  }) {
    return SyncState(
      isSyncing: isSyncing ?? this.isSyncing,
      pendingOutboxCount: pendingOutboxCount ?? this.pendingOutboxCount,
      pendingByTable: pendingByTable ?? this.pendingByTable,
      pulledRecordsCount: pulledRecordsCount ?? this.pulledRecordsCount,
      lastSyncedAt: lastSyncedAt ?? this.lastSyncedAt,
      errorMessage: errorMessage,
    );
  }
}

class SyncNotifier extends StateNotifier<SyncState> {
  final SupabaseSyncService _syncService;
  final OfflineQueueManager _queueManager;
  final String Function() _getMosqueId;
  StreamSubscription<List<ConnectivityResult>>? _connectivitySub;

  SyncNotifier(this._syncService, this._queueManager, this._getMosqueId)
      : super(const SyncState()) {
    checkPendingCount();
    _initConnectivityListener();
  }

  void _initConnectivityListener() {
    try {
      _connectivitySub = Connectivity().onConnectivityChanged.listen((results) async {
        final hasNetwork = results.any((r) => r != ConnectivityResult.none);
        if (!hasNetwork) return;
        final queue = await _queueManager.getPendingQueue();
        if (queue.isNotEmpty && !state.isSyncing) {
          await triggerSync();
        }
      });
    } catch (_) {}
  }

  Future<void> checkPendingCount() async {
    final queue = await _queueManager.getPendingQueue();
    final counts = await _queueManager.getPendingCountsByTable();
    if (!mounted) return;
    state = state.copyWith(
      pendingOutboxCount: queue.length,
      pendingByTable: counts,
    );
  }

  Future<bool> triggerSync() async {
    if (state.isSyncing) return false;
    state = state.copyWith(isSyncing: true, errorMessage: null);

    final result = await _syncService.performFullSync(
      mosqueId: _getMosqueId(),
    );
    final remaining = await _queueManager.getPendingQueue();
    final counts = await _queueManager.getPendingCountsByTable();
    if (!mounted) return result.isSuccess;

    state = state.copyWith(
      isSyncing: false,
      pendingOutboxCount: remaining.length,
      pendingByTable: counts,
      pulledRecordsCount: result.pulledRecordsCount,
      lastSyncedAt: result.syncedAt,
      errorMessage: result.isSuccess ? null : result.errorMessage,
    );

    return result.isSuccess;
  }

  @override
  void dispose() {
    _connectivitySub?.cancel();
    super.dispose();
  }
}

final syncNotifierProvider = StateNotifierProvider<SyncNotifier, SyncState>((ref) {
  final service = ref.watch(supabaseSyncServiceProvider);
  final queue = ref.watch(offlineQueueManagerProvider);
  return SyncNotifier(
    service,
    queue,
    () => ref.read(authNotifierProvider).valueOrNull?.mosqueId ?? '',
  );
});
