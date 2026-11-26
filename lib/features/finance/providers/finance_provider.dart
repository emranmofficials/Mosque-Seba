import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../authentication/providers/auth_provider.dart';
import '../models/finance_transaction_model.dart';
import '../repositories/finance_repository.dart';

final financeRepositoryProvider = Provider<FinanceRepository>((ref) {
  return FinanceRepository();
});

final financeTypeFilterProvider = StateProvider<String>((ref) => 'all');
final financeStatusFilterProvider = StateProvider<String>((ref) => 'all');

class FinanceStateNotifier extends StateNotifier<AsyncValue<List<FinanceTransactionModel>>> {
  final FinanceRepository _repository;
  final String _mosqueId;
  final Ref _ref;

  FinanceStateNotifier(this._repository, this._mosqueId, this._ref)
      : super(const AsyncValue.loading()) {
    loadTransactions();
  }

  Future<void> loadTransactions() async {
    state = const AsyncValue.loading();
    try {
      final type = _ref.read(financeTypeFilterProvider);
      final status = _ref.read(financeStatusFilterProvider);

      final list = await _repository.fetchTransactions(
        mosqueId: _mosqueId,
        typeFilter: type,
        statusFilter: status,
      );
      if (!mounted) return;
      state = AsyncValue.data(list);
    } catch (e, st) {
      if (!mounted) return;
      state = AsyncValue.error(e, st);
    }
  }

  Future<bool> submitVoucher({
    required String categoryNameBn,
    required String type,
    required double amount,
    required String txnDate,
    required String description,
    required String createdBy,
    required String userRole,
  }) async {
    final result = await _repository.submitTransaction(
      mosqueId: _mosqueId,
      categoryNameBn: categoryNameBn,
      type: type,
      amount: amount,
      txnDate: txnDate,
      description: description,
      createdBy: createdBy,
      userRole: userRole,
    );
    if (result != null) {
      await loadTransactions();
      return true;
    }
    return false;
  }

  Future<bool> reviewVoucher({
    required String transactionId,
    required String reviewerName,
    required String creatorName,
    required bool isApproved,
  }) async {
    final ok = await _repository.reviewTransaction(
      transactionId: transactionId,
      reviewerName: reviewerName,
      creatorName: creatorName,
      isApproved: isApproved,
    );
    if (ok) {
      await loadTransactions();
      return true;
    }
    return false;
  }
}

final financeListProvider = StateNotifierProvider<FinanceStateNotifier, AsyncValue<List<FinanceTransactionModel>>>((ref) {
  final repo = ref.watch(financeRepositoryProvider);
  final authState = ref.watch(authNotifierProvider);
  final mosqueId = authState.valueOrNull?.mosqueId ?? 'mosque-default';
  return FinanceStateNotifier(repo, mosqueId, ref);
});

final financeSummaryProvider = Provider<FinancialSummary>((ref) {
  final txnsAsync = ref.watch(financeListProvider);
  final repo = ref.watch(financeRepositoryProvider);
  return txnsAsync.maybeWhen(
    data: (txns) => repo.calculateSummary(txns),
    orElse: () => FinancialSummary.empty(),
  );
});
