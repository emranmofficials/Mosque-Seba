import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../authentication/providers/auth_provider.dart';
import '../models/election_candidate_model.dart';
import '../models/election_schedule_model.dart';
import '../repositories/election_repository.dart';

final electionRepositoryProvider = Provider<ElectionRepository>((ref) {
  return ElectionRepository();
});

class ElectionNotifier extends StateNotifier<AsyncValue<List<ElectionScheduleModel>>> {
  final ElectionRepository _repository;
  final String _mosqueId;

  ElectionNotifier(this._repository, this._mosqueId)
      : super(const AsyncValue.loading()) {
    loadSchedules();
  }

  Future<void> loadSchedules() async {
    state = const AsyncValue.loading();
    try {
      final list = await _repository.fetchSchedules(mosqueId: _mosqueId);
      state = AsyncValue.data(list);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  Future<bool> createSchedule(ElectionScheduleModel schedule) async {
    final result = await _repository.createSchedule(schedule);
    if (result != null) {
      await loadSchedules();
      return true;
    }
    return false;
  }
}

final electionSchedulesProvider = StateNotifierProvider<ElectionNotifier, AsyncValue<List<ElectionScheduleModel>>>((ref) {
  final repo = ref.watch(electionRepositoryProvider);
  final authState = ref.watch(authProvider);
  final mosqueId = authState.user?.mosqueId ?? 'mosque-default';
  return ElectionNotifier(repo, mosqueId);
});

final activeElectionScheduleProvider = Provider<ElectionScheduleModel?>((ref) {
  final schedules = ref.watch(electionSchedulesProvider).asData?.value ?? [];
  return schedules.isNotEmpty ? schedules.first : null;
});

class CandidateListNotifier extends StateNotifier<AsyncValue<List<ElectionCandidateModel>>> {
  final ElectionRepository _repository;
  final String _scheduleId;

  CandidateListNotifier(this._repository, this._scheduleId)
      : super(const AsyncValue.loading()) {
    loadCandidates();
  }

  Future<void> loadCandidates() async {
    if (_scheduleId.isEmpty) {
      state = const AsyncValue.data([]);
      return;
    }
    state = const AsyncValue.loading();
    try {
      final list = await _repository.fetchCandidates(_scheduleId);
      state = AsyncValue.data(list);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  Future<bool> nominate(ElectionCandidateModel candidate) async {
    final result = await _repository.nominateCandidate(candidate);
    if (result != null) {
      await loadCandidates();
      return true;
    }
    return false;
  }

  Future<bool> recordVotes(String candidateId, int votes, bool isElected) async {
    final ok = await _repository.updateVoteCount(candidateId, votes, isElected);
    if (ok) {
      await loadCandidates();
      return true;
    }
    return false;
  }
}

final electionCandidatesProvider = StateNotifierProvider<CandidateListNotifier, AsyncValue<List<ElectionCandidateModel>>>((ref) {
  final repo = ref.watch(electionRepositoryProvider);
  final activeSchedule = ref.watch(activeElectionScheduleProvider);
  return CandidateListNotifier(repo, activeSchedule?.id ?? '');
});
