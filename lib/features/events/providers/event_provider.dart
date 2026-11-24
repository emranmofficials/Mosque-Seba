import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/mosque_event_model.dart';
import '../repositories/event_repository.dart';

final eventRepositoryProvider = Provider<EventRepository>((ref) {
  return EventRepository();
});

final eventStatusFilterProvider = StateProvider<String>((ref) => 'all');

class EventListNotifier extends StateNotifier<AsyncValue<List<MosqueEventModel>>> {
  final EventRepository _repository;

  EventListNotifier(this._repository) : super(const AsyncValue.loading()) {
    loadEvents();
  }

  Future<void> loadEvents() async {
    try {
      state = const AsyncValue.loading();
      final events = await _repository.fetchEvents();
      state = AsyncValue.data(events);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  Future<bool> createEvent(MosqueEventModel event) async {
    try {
      final created = await _repository.createEvent(event);
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

final eventListProvider =
    StateNotifierProvider<EventListNotifier, AsyncValue<List<MosqueEventModel>>>((ref) {
  final repo = ref.watch(eventRepositoryProvider);
  return EventListNotifier(repo);
});

final filteredEventsProvider = Provider<List<MosqueEventModel>>((ref) {
  final eventsAsync = ref.watch(eventListProvider);
  final filter = ref.watch(eventStatusFilterProvider);
  final events = eventsAsync.valueOrNull ?? [];

  if (filter == 'all') return events;
  return events.where((e) => e.status == filter).toList();
});
