import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../authentication/providers/auth_provider.dart';
import '../models/duty_roster_item.dart';
import '../repositories/duty_roster_repository.dart';

final dutyRosterRepositoryProvider = Provider<DutyRosterRepository>((ref) {
  return DutyRosterRepository();
});

final selectedRosterDayProvider = StateProvider<String>((ref) => 'শুক্রবার');

class DutyRosterNotifier extends StateNotifier<AsyncValue<List<DutyRosterItem>>> {
  final DutyRosterRepository _repository;
  final String _mosqueId;
  final Ref _ref;

  DutyRosterNotifier(this._repository, this._mosqueId, this._ref)
      : super(const AsyncValue.loading()) {
    loadRoster();
  }

  Future<void> loadRoster() async {
    state = const AsyncValue.loading();
    try {
      final day = _ref.read(selectedRosterDayProvider);
      final list = await _repository.fetchRoster(
        mosqueId: _mosqueId,
        dayOfWeek: day,
      );
      state = AsyncValue.data(list);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  Future<bool> assignDuty(DutyRosterItem item) async {
    final result = await _repository.assignDuty(item);
    if (result != null) {
      await loadRoster();
      return true;
    }
    return false;
  }

  Future<bool> removeDuty(String id) async {
    final ok = await _repository.removeDuty(id);
    if (ok) {
      await loadRoster();
      return true;
    }
    return false;
  }
}

final dutyRosterListProvider = StateNotifierProvider<DutyRosterNotifier, AsyncValue<List<DutyRosterItem>>>((ref) {
  final repo = ref.watch(dutyRosterRepositoryProvider);
  final authState = ref.watch(authProvider);
  final mosqueId = authState.user?.mosqueId ?? 'mosque-default';
  return DutyRosterNotifier(repo, mosqueId, ref);
});
