import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../authentication/providers/auth_provider.dart';
import '../models/cashbox_record_model.dart';
import '../repositories/cashbox_repository.dart';

final cashboxRepositoryProvider = Provider<CashboxRepository>((ref) {
  return CashboxRepository();
});

class CashboxListNotifier extends StateNotifier<AsyncValue<List<CashboxRecordModel>>> {
  final CashboxRepository _repository;
  final String _mosqueId;

  CashboxListNotifier(this._repository, this._mosqueId)
      : super(const AsyncValue.loading()) {
    loadRecords();
  }

  Future<void> loadRecords() async {
    state = const AsyncValue.loading();
    try {
      final list = await _repository.fetchRecords(mosqueId: _mosqueId);
      state = AsyncValue.data(list);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  Future<CashboxRecordModel?> submitCount(CashboxRecordModel record) async {
    final result = await _repository.recordCashboxCount(
      mosqueId: _mosqueId,
      record: record,
    );
    if (result != null) {
      await loadRecords();
    }
    return result;
  }
}

final cashboxListProvider = StateNotifierProvider<CashboxListNotifier, AsyncValue<List<CashboxRecordModel>>>((ref) {
  final repo = ref.watch(cashboxRepositoryProvider);
  final authState = ref.watch(authProvider);
  final mosqueId = authState.user?.mosqueId ?? 'mosque-default';
  return CashboxListNotifier(repo, mosqueId);
});
