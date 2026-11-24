import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/hifz_record_model.dart';
import '../repositories/hifz_repository.dart';

final hifzRepositoryProvider = Provider<HifzRepository>((ref) {
  return HifzRepository();
});

class HifzLogNotifier extends StateNotifier<AsyncValue<List<HifzRecordModel>>> {
  final HifzRepository _repository;

  HifzLogNotifier(this._repository) : super(const AsyncValue.loading()) {
    loadRecentLogs();
  }

  Future<void> loadRecentLogs({String? studentId}) async {
    try {
      state = const AsyncValue.loading();
      final logs = await _repository.fetchRecentLogs(studentId: studentId);
      state = AsyncValue.data(logs);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  Future<bool> logSession(HifzRecordModel record) async {
    try {
      final created = await _repository.logSession(record);
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
  return HifzLogNotifier(repo);
});
