import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../authentication/providers/auth_provider.dart';
import '../models/meal_schedule_item.dart';
import '../repositories/meal_schedule_repository.dart';

final mealScheduleRepositoryProvider = Provider<MealScheduleRepository>((ref) {
  return MealScheduleRepository();
});

final selectedMealDateProvider = StateProvider<String>((ref) {
  return DateTime.now().toIso8601String().split('T').first;
});

class MealScheduleNotifier extends StateNotifier<AsyncValue<List<MealScheduleItem>>> {
  final MealScheduleRepository _repository;
  final String _mosqueId;
  final Ref _ref;

  MealScheduleNotifier(this._repository, this._mosqueId, this._ref)
      : super(const AsyncValue.loading()) {
    loadSchedules();
  }

  Future<void> loadSchedules() async {
    state = const AsyncValue.loading();
    try {
      final date = _ref.read(selectedMealDateProvider);
      final list = await _repository.fetchSchedules(
        mosqueId: _mosqueId,
        dateStr: date,
      );
      state = AsyncValue.data(list);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  Future<bool> addSchedule(MealScheduleItem item) async {
    final result = await _repository.addSchedule(item);
    if (result != null) {
      await loadSchedules();
      return true;
    }
    return false;
  }

  Future<bool> updateStatus(String id, String status) async {
    final ok = await _repository.updateStatus(id, status);
    if (ok) {
      await loadSchedules();
      return true;
    }
    return false;
  }
}

final mealScheduleListProvider = StateNotifierProvider<MealScheduleNotifier, AsyncValue<List<MealScheduleItem>>>((ref) {
  final repo = ref.watch(mealScheduleRepositoryProvider);
  final authState = ref.watch(authProvider);
  final mosqueId = authState.user?.mosqueId ?? 'mosque-default';
  return MealScheduleNotifier(repo, mosqueId, ref);
});
