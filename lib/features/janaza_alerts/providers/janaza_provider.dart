import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../authentication/providers/auth_provider.dart';
import '../models/janaza_alert_model.dart';
import '../repositories/janaza_repository.dart';

final janazaRepositoryProvider = Provider<JanazaRepository>((ref) {
  return JanazaRepository();
});

class JanazaListNotifier extends StateNotifier<AsyncValue<List<JanazaAlertModel>>> {
  final JanazaRepository _repository;
  final String _mosqueId;
  final String _mosqueName;

  JanazaListNotifier(this._repository, this._mosqueId, this._mosqueName)
      : super(const AsyncValue.loading()) {
    loadAlerts();
  }

  Future<void> loadAlerts() async {
    state = const AsyncValue.loading();
    try {
      final list = await _repository.fetchJanazaAlerts(mosqueId: _mosqueId);
      state = AsyncValue.data(list);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  Future<bool> broadcastAlert(JanazaAlertModel alert, String author) async {
    final result = await _repository.broadcastAlert(
      mosqueId: _mosqueId,
      mosqueName: _mosqueName,
      alert: alert,
      broadcastBy: author,
    );
    if (result != null) {
      await loadAlerts();
      return true;
    }
    return false;
  }
}

final janazaListProvider = StateNotifierProvider<JanazaListNotifier, AsyncValue<List<JanazaAlertModel>>>((ref) {
  final repo = ref.watch(janazaRepositoryProvider);
  final authState = ref.watch(authProvider);
  final mosqueId = authState.user?.mosqueId ?? 'mosque-default';
  final mosqueName = 'মসজিদ সেবা জামে মসজিদ';
  return JanazaListNotifier(repo, mosqueId, mosqueName);
});
