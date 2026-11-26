import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../authentication/providers/auth_provider.dart';
import '../models/mosque_event_model.dart';
import '../repositories/event_repository.dart';

final eventRepositoryProvider = Provider<EventRepository>((ref) {
  return EventRepository();
});

final eventStatusFilterProvider = StateProvider<String>((ref) => 'all');

class EventListNotifier extends StateNotifier<AsyncValue<List<MosqueEventModel>>> {
  final EventRepository _repository;
  final String _mosqueId;

  EventListNotifier(this._repository, this._mosqueId)
      : super(const AsyncValue.loading()) {
    loadEvents();
  }

  Future<void> loadEvents() async {
    try {
      state = const AsyncValue.loading();
      final events = await _repository.fetchEvents(mosqueId: _mosqueId);
      if (!mounted) return;
      state = AsyncValue.data(events);
    } catch (e, st) {
      if (!mounted) return;
      state = AsyncValue.error(e, st);
    }
  }

  Future<bool> createEvent(MosqueEventModel event) async {
    try {
      final created = await _repository.createEvent(
        mosqueId: _mosqueId,
        event: event,
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

final eventListProvider =
    StateNotifierProvider<EventListNotifier, AsyncValue<List<MosqueEventModel>>>((ref) {
  final repo = ref.watch(eventRepositoryProvider);
  final authState = ref.watch(authNotifierProvider);
  final mosqueId = authState.valueOrNull?.mosqueId ?? 'mosque-default';
  return EventListNotifier(repo, mosqueId);
});

final filteredEventsProvider = Provider<List<MosqueEventModel>>((ref) {
  final eventsAsync = ref.watch(eventListProvider);
  final filter = ref.watch(eventStatusFilterProvider);
  final events = eventsAsync.valueOrNull ?? [];

  if (filter == 'all') return events;
  return events.where((e) => e.status == filter).toList();
});
