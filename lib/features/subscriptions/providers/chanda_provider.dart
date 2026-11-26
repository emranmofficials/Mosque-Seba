import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../authentication/providers/auth_provider.dart';
import '../models/chanda_payment_model.dart';
import '../repositories/chanda_repository.dart';

final chandaRepositoryProvider = Provider<ChandaRepository>((ref) {
  return ChandaRepository();
});

final chandaMonthFilterProvider = StateProvider<String>((ref) => '');

class ChandaListNotifier extends StateNotifier<AsyncValue<List<ChandaPaymentModel>>> {
  final ChandaRepository _repository;
  final String _mosqueId;
  final Ref _ref;

  ChandaListNotifier(this._repository, this._mosqueId, this._ref)
      : super(const AsyncValue.loading()) {
    loadPayments();
  }

  Future<void> loadPayments() async {
    state = const AsyncValue.loading();
    try {
      final month = _ref.read(chandaMonthFilterProvider);
      final list = await _repository.fetchPayments(
        mosqueId: _mosqueId,
        monthFilter: month.isEmpty ? null : month,
      );
      if (!mounted) return;
      state = AsyncValue.data(list);
    } catch (e, st) {
      if (!mounted) return;
      state = AsyncValue.error(e, st);
    }
  }

  Future<ChandaPaymentModel?> receivePayment({
    required String memberId,
    required String memberNameBn,
    required String month,
    required double amount,
    required String method,
    required String receivedBy,
  }) async {
    final payment = await _repository.receivePayment(
      mosqueId: _mosqueId,
      memberId: memberId,
      memberNameBn: memberNameBn,
      month: month,
      amount: amount,
      method: method,
      receivedBy: receivedBy,
    );
    if (payment != null) {
      await loadPayments();
    }
    return payment;
  }
}

final chandaListProvider = StateNotifierProvider<ChandaListNotifier, AsyncValue<List<ChandaPaymentModel>>>((ref) {
  final repo = ref.watch(chandaRepositoryProvider);
  final authState = ref.watch(authNotifierProvider);
  final mosqueId = authState.valueOrNull?.mosqueId ?? 'mosque-default';
  return ChandaListNotifier(repo, mosqueId, ref);
});
