import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../authentication/providers/auth_provider.dart';
import '../models/madrasa_fee_payment_model.dart';
import '../repositories/madrasa_fee_repository.dart';

final madrasaFeeRepositoryProvider = Provider<MadrasaFeeRepository>((ref) {
  return MadrasaFeeRepository();
});

final feeMonthFilterProvider = StateProvider<String>((ref) => 'all');

class MadrasaFeeListNotifier extends StateNotifier<AsyncValue<List<MadrasaFeePaymentModel>>> {
  final MadrasaFeeRepository _repository;
  final String _mosqueId;

  MadrasaFeeListNotifier(this._repository, this._mosqueId)
      : super(const AsyncValue.loading()) {
    loadFeePayments();
  }

  Future<void> loadFeePayments({String? month}) async {
    try {
      state = const AsyncValue.loading();
      final payments = await _repository.fetchFeePayments(
        mosqueId: _mosqueId,
        monthFilter: month,
      );
      if (!mounted) return;
      state = AsyncValue.data(payments);
    } catch (e, st) {
      if (!mounted) return;
      state = AsyncValue.error(e, st);
    }
  }

  Future<bool> collectFee(MadrasaFeePaymentModel payment) async {
    try {
      final created = await _repository.collectFee(
        mosqueId: _mosqueId,
        payment: payment,
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

final madrasaFeeListProvider =
    StateNotifierProvider<MadrasaFeeListNotifier, AsyncValue<List<MadrasaFeePaymentModel>>>((ref) {
  final repo = ref.watch(madrasaFeeRepositoryProvider);
  final authState = ref.watch(authNotifierProvider);
  final mosqueId = authState.valueOrNull?.mosqueId ?? 'mosque-default';
  return MadrasaFeeListNotifier(repo, mosqueId);
});
