import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../authentication/providers/auth_provider.dart';
import '../../finance/providers/finance_provider.dart';
import '../models/cashbox_record_model.dart';
import '../repositories/cashbox_repository.dart';

final cashboxRepositoryProvider = Provider<CashboxRepository>((ref) {
  return CashboxRepository();
});

class CashboxListNotifier extends StateNotifier<AsyncValue<List<CashboxRecordModel>>> {
  final CashboxRepository _repository;
  final String _mosqueId;
  final Ref _ref;

  CashboxListNotifier(this._repository, this._mosqueId, this._ref)
      : super(const AsyncValue.loading()) {
    loadRecords();
  }

  Future<void> loadRecords() async {
    state = const AsyncValue.loading();
    try {
      final list = await _repository.fetchRecords(mosqueId: _mosqueId);
      if (!mounted) return;
      state = AsyncValue.data(list);
    } catch (e, st) {
      if (!mounted) return;
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
      _ref.invalidate(financeListProvider);
    }
    return result;
  }
}

final cashboxListProvider = StateNotifierProvider<CashboxListNotifier, AsyncValue<List<CashboxRecordModel>>>((ref) {
  final repo = ref.watch(cashboxRepositoryProvider);
  final authState = ref.watch(authNotifierProvider);
  final mosqueId = authState.valueOrNull?.mosqueId ?? 'mosque-default';
  return CashboxListNotifier(repo, mosqueId, ref);
});
