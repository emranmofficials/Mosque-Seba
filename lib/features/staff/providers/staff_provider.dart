import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../authentication/providers/auth_provider.dart';
import '../../finance/providers/finance_provider.dart';
import '../models/staff_model.dart';
import '../models/staff_payroll_model.dart';
import '../repositories/staff_repository.dart';

final staffRepositoryProvider = Provider<StaffRepository>((ref) {
  return StaffRepository();
});

class StaffListNotifier extends StateNotifier<AsyncValue<List<StaffModel>>> {
  final StaffRepository _repository;
  final String _mosqueId;

  StaffListNotifier(this._repository, this._mosqueId)
      : super(const AsyncValue.loading()) {
    loadStaff();
  }

  Future<void> loadStaff() async {
    state = const AsyncValue.loading();
    try {
      final list = await _repository.fetchStaff(mosqueId: _mosqueId);
      if (!mounted) return;
      state = AsyncValue.data(list);
    } catch (e, st) {
      if (!mounted) return;
      state = AsyncValue.error(e, st);
    }
  }

  Future<bool> addStaff(StaffModel staff) async {
    final created = await _repository.createStaff(
      mosqueId: _mosqueId,
      staff: staff,
    );
    if (created != null) {
      await loadStaff();
      return true;
    }
    return false;
  }

  Future<bool> updateStaff(StaffModel staff) async {
    final ok = await _repository.updateStaff(staff, mosqueId: _mosqueId);
    if (ok) {
      await loadStaff();
    }
    return ok;
  }

  Future<bool> deactivateStaff(String staffId) async {
    final ok = await _repository.deactivateStaff(staffId, mosqueId: _mosqueId);
    if (ok) {
      await loadStaff();
    }
    return ok;
  }
}

final staffListProvider = StateNotifierProvider<StaffListNotifier, AsyncValue<List<StaffModel>>>((ref) {
  final repo = ref.watch(staffRepositoryProvider);
  final authState = ref.watch(authNotifierProvider);
  final mosqueId = authState.valueOrNull?.mosqueId ?? 'mosque-default';
  return StaffListNotifier(repo, mosqueId);
});

final payrollMonthFilterProvider = StateProvider<String>((ref) => 'সকল');

class PayrollHistoryNotifier extends StateNotifier<AsyncValue<List<StaffPayrollModel>>> {
  final StaffRepository _repository;
  final String _mosqueId;
  final Ref _ref;

  PayrollHistoryNotifier(this._repository, this._mosqueId, this._ref)
      : super(const AsyncValue.loading()) {
    loadPayroll();
  }

  Future<void> loadPayroll({String? monthFilter}) async {
    state = const AsyncValue.loading();
    try {
      final activeFilter = monthFilter ?? _ref.read(payrollMonthFilterProvider);
      final list = await _repository.fetchPayrollHistory(
        mosqueId: _mosqueId,
        monthFilter: activeFilter,
      );
      if (!mounted) return;
      state = AsyncValue.data(list);
    } catch (e, st) {
      if (!mounted) return;
      state = AsyncValue.error(e, st);
    }
  }

  Future<StaffPayrollModel?> disburseSalary({
    required String staffId,
    required String staffNameBn,
    required String month,
    required double baseSalary,
    required double bonusAmount,
    required String disbursedBy,
  }) async {
    final result = await _repository.disburseSalary(
      mosqueId: _mosqueId,
      staffId: staffId,
      staffNameBn: staffNameBn,
      month: month,
      baseSalary: baseSalary,
      bonusAmount: bonusAmount,
      disbursedBy: disbursedBy,
    );
    if (result != null) {
      await loadPayroll();
      _ref.invalidate(financeListProvider);
    }
    return result;
  }
}

final payrollHistoryProvider = StateNotifierProvider<PayrollHistoryNotifier, AsyncValue<List<StaffPayrollModel>>>((ref) {
  final repo = ref.watch(staffRepositoryProvider);
  final authState = ref.watch(authNotifierProvider);
  final mosqueId = authState.valueOrNull?.mosqueId ?? 'mosque-default';
  return PayrollHistoryNotifier(repo, mosqueId, ref);
});
