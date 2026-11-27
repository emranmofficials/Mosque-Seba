import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../bootstrap/dependency_injection.dart';
import '../../role_permissions/models/committee_role.dart';
import '../../role_permissions/providers/role_permissions_provider.dart';
import '../models/auth_user.dart';
import '../services/auth_service.dart';

final authServiceProvider = Provider<AuthService>((ref) {
  final storage = ref.watch(sessionStorageServiceProvider);
  final database = ref.watch(appDatabaseProvider);
  return AuthService(storage, database);
});

class AuthNotifier extends StateNotifier<AsyncValue<AuthUser?>> {
  final AuthService _authService;
  final Ref _ref;

  AuthNotifier(this._authService, this._ref)
    : super(const AsyncValue.loading()) {
    checkSession();
  }

  Future<void> checkSession() async {
    try {
      final user = await _authService.restoreSession();
      if (!mounted) return;
      if (user != null) {
        _ref.read(currentRoleProvider.notifier).state = CommitteeRole.fromCode(
          user.roleCode,
        );
      }
      state = AsyncValue.data(user);
    } catch (e, st) {
      if (!mounted) return;
      state = AsyncValue.error(e, st);
    }
  }

  Future<void> loginWithEmail(String email, String password) async {
    state = const AsyncValue.loading();
    try {
      final user = await _authService.loginWithEmail(
        email: email,
        password: password,
      );
      if (!mounted) return;
      _ref.read(currentRoleProvider.notifier).state = CommitteeRole.fromCode(
        user.roleCode,
      );
      state = AsyncValue.data(user);
    } catch (e, st) {
      if (!mounted) return;
      state = AsyncValue.error(e, st);
      rethrow;
    }
  }

  Future<void> loginWithMobile(
    String committeeId,
    String mobile,
    String password,
  ) async {
    state = const AsyncValue.loading();
    try {
      final user = await _authService.loginWithMobile(
        committeeId: committeeId,
        mobile: mobile,
        password: password,
      );
      if (!mounted) return;
      _ref.read(currentRoleProvider.notifier).state = CommitteeRole.fromCode(
        user.roleCode,
      );
      state = AsyncValue.data(user);
    } catch (e, st) {
      if (!mounted) return;
      state = AsyncValue.error(e, st);
      rethrow;
    }
  }

  Future<void> updateMosqueProfile({
    required String nameBn,
    required String address,
    int? establishedYear,
  }) async {
    final current = state.valueOrNull;
    if (current == null || current.mosqueId.isEmpty) return;

    await _authService.updateMosqueProfile(
      mosqueId: current.mosqueId,
      nameBn: nameBn,
      address: address,
      establishedYear: establishedYear,
    );

    if (!mounted) return;
    state = AsyncValue.data(
      current.copyWith(
        mosqueNameBn: nameBn.trim(),
        mosqueAddress: address.trim(),
        establishedYear: establishedYear ?? current.establishedYear,
      ),
    );
  }

  Future<void> logout() async {
    state = const AsyncValue.loading();
    await _authService.logout();
    if (!mounted) return;
    _ref.read(currentRoleProvider.notifier).state = CommitteeRole.generalMember;
    state = const AsyncValue.data(null);
  }

  /// Signs into an offline-ready Demo session for the selected [CommitteeRole].
  void enterDemoMode(CommitteeRole role) {
    _ref.read(currentRoleProvider.notifier).state = role;
    state = AsyncValue.data(
      AuthUser(
        uid: 'demo-${role.code}',
        email: '${role.code}@demo.mosqueseba.app',
        mobile: '01700000000',
        memberId: 'demo-member-${role.code}',
        memberName: 'ডেমো ${role.titleBn}',
        committeeId: 'DEMO-2026',
        roleCode: role.code,
        mosqueId: 'demo-mosque-001',
        mosqueNameBn: 'বায়তুল আমান জামে মসজিদ (ডেমো)',
        mosqueAddress: 'মিরপুর-১০, ঢাকা-১২১৬',
        establishedYear: 1998,
      ),
    );
  }
}

final authNotifierProvider =
    StateNotifierProvider<AuthNotifier, AsyncValue<AuthUser?>>((ref) {
      final authService = ref.watch(authServiceProvider);
      return AuthNotifier(authService, ref);
    });
