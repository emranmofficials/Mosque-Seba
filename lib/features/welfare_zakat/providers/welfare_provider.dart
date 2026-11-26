import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../authentication/providers/auth_provider.dart';
import '../../finance/providers/finance_provider.dart';
import '../models/welfare_case_model.dart';
import '../repositories/welfare_repository.dart';

final welfareRepositoryProvider = Provider<WelfareRepository>((ref) {
  return WelfareRepository();
});

final welfareCategoryFilterProvider = StateProvider<String>((ref) => 'all');
final welfareStatusFilterProvider = StateProvider<String>((ref) => 'all');

class WelfareListNotifier extends StateNotifier<AsyncValue<List<WelfareCaseModel>>> {
  final WelfareRepository _repository;
  final String _mosqueId;
  final Ref _ref;

  WelfareListNotifier(this._repository, this._mosqueId, this._ref)
      : super(const AsyncValue.loading()) {
    loadCases();
  }

  Future<void> loadCases() async {
    try {
      state = const AsyncValue.loading();
      final cases = await _repository.fetchCases(mosqueId: _mosqueId);
      if (!mounted) return;
      state = AsyncValue.data(cases);
    } catch (e, st) {
      if (!mounted) return;
      state = AsyncValue.error(e, st);
    }
  }

  Future<bool> submitCase(WelfareCaseModel welfareCase) async {
    try {
      final created = await _repository.submitCase(
        mosqueId: _mosqueId,
        welfareCase: welfareCase,
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

  Future<bool> approveCase({
    required String caseId,
    required double approvedAmount,
    required String approverName,
    required bool disburseNow,
  }) async {
    try {
      final success = await _repository.approveAndDisburse(
        mosqueId: _mosqueId,
        caseId: caseId,
        approvedAmount: approvedAmount,
        approverName: approverName,
        disburseNow: disburseNow,
      );
      if (!mounted) return success;

      if (success) {
        final current = state.valueOrNull ?? [];
        state = AsyncValue.data(
          current.map((c) {
            if (c.id == caseId) {
              return c.copyWith(
                approvedAmount: approvedAmount,
                approvedBy: approverName,
                status: disburseNow ? 'disbursed' : 'approved',
              );
            }
            return c;
          }).toList(),
        );
        if (disburseNow) {
          _ref.invalidate(financeListProvider);
        }
        return true;
      }
      return false;
    } catch (e) {
      return false;
    }
  }
}

final welfareListProvider =
    StateNotifierProvider<WelfareListNotifier, AsyncValue<List<WelfareCaseModel>>>((ref) {
  final repo = ref.watch(welfareRepositoryProvider);
  final authState = ref.watch(authNotifierProvider);
  final mosqueId = authState.valueOrNull?.mosqueId ?? 'mosque-default';
  return WelfareListNotifier(repo, mosqueId, ref);
});

final filteredWelfareCasesProvider = Provider<List<WelfareCaseModel>>((ref) {
  final casesAsync = ref.watch(welfareListProvider);
  final catFilter = ref.watch(welfareCategoryFilterProvider);
  final statusFilter = ref.watch(welfareStatusFilterProvider);
  final cases = casesAsync.valueOrNull ?? [];

  return cases.where((c) {
    final matchesCat = catFilter == 'all' || c.category == catFilter;
    final matchesStatus = statusFilter == 'all' || c.status == statusFilter;
    return matchesCat && matchesStatus;
  }).toList();
});
