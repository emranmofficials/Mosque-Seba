import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../authentication/providers/auth_provider.dart';
import '../models/hifz_record_model.dart';
import '../repositories/hifz_repository.dart';

final hifzRepositoryProvider = Provider<HifzRepository>((ref) {
  return HifzRepository();
});

class HifzLogNotifier extends StateNotifier<AsyncValue<List<HifzRecordModel>>> {
  final HifzRepository _repository;
  final String _mosqueId;

  HifzLogNotifier(this._repository, this._mosqueId)
      : super(const AsyncValue.loading()) {
    loadRecentLogs();
  }

  Future<void> loadRecentLogs({String? studentId}) async {
    try {
      state = const AsyncValue.loading();
      final logs = await _repository.fetchRecentLogs(
        mosqueId: _mosqueId,
        studentId: studentId,
      );
      if (!mounted) return;
      state = AsyncValue.data(logs);
    } catch (e, st) {
      if (!mounted) return;
      state = AsyncValue.error(e, st);
    }
  }

  Future<bool> logSession(HifzRecordModel record) async {
    try {
      final created = await _repository.logSession(
        mosqueId: _mosqueId,
        record: record,
      );
      if (!mounted) return created != null;
      if (created != null) {
        final current = state.valueOrNull ?? [];
        state = AsyncValue.data([created, ...current]);
        return true;
      }
      return false;
    } catch (e) {
      return false;
    }
  }
}

final hifzLogProvider =
    StateNotifierProvider<HifzLogNotifier, AsyncValue<List<HifzRecordModel>>>((ref) {
  final repo = ref.watch(hifzRepositoryProvider);
  final authState = ref.watch(authNotifierProvider);
  final mosqueId = authState.valueOrNull?.mosqueId ?? 'mosque-default';
  return HifzLogNotifier(repo, mosqueId);
});
