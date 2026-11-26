import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../authentication/providers/auth_provider.dart';
import '../models/meeting_model.dart';
import '../repositories/meeting_repository.dart';

final meetingRepositoryProvider = Provider<MeetingRepository>((ref) {
  return MeetingRepository();
});

final meetingStatusFilterProvider = StateProvider<String>((ref) => 'all');

class MeetingListNotifier extends StateNotifier<AsyncValue<List<MeetingModel>>> {
  final MeetingRepository _repository;
  final String _mosqueId;
  final Ref _ref;

  MeetingListNotifier(this._repository, this._mosqueId, this._ref)
      : super(const AsyncValue.loading()) {
    loadMeetings();
  }

  Future<void> loadMeetings() async {
    state = const AsyncValue.loading();
    try {
      final filter = _ref.read(meetingStatusFilterProvider);
      final list = await _repository.fetchMeetings(
        mosqueId: _mosqueId,
        statusFilter: filter,
      );
      if (!mounted) return;
      state = AsyncValue.data(list);
    } catch (e, st) {
      if (!mounted) return;
      state = AsyncValue.error(e, st);
    }
  }

  Future<bool> scheduleMeeting(MeetingModel meeting) async {
    final result = await _repository.scheduleMeeting(
      mosqueId: _mosqueId,
      meeting: meeting,
    );
    if (result != null) {
      await loadMeetings();
      return true;
    }
    return false;
  }

  Future<bool> recordResolution(String meetingId, String resolution) async {
    final ok = await _repository.recordResolution(meetingId, resolution);
    if (ok) {
      await loadMeetings();
      return true;
    }
    return false;
  }
}

final meetingListProvider = StateNotifierProvider<MeetingListNotifier, AsyncValue<List<MeetingModel>>>((ref) {
  final repo = ref.watch(meetingRepositoryProvider);
  final authState = ref.watch(authNotifierProvider);
  final mosqueId = authState.valueOrNull?.mosqueId ?? 'mosque-default';
  return MeetingListNotifier(repo, mosqueId, ref);
});
