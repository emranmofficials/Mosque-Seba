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

  AuthNotifier(this._authService, this._ref) : super(const AsyncValue.loading()) {
    checkSession();
  }

  Future<void> checkSession() async {
    try {
      final user = await _authService.restoreSession();
      if (!mounted) return;
      if (user != null) {
        _ref.read(currentRoleProvider.notifier).state = CommitteeRole.fromCode(user.roleCode);
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
      final user = await _authService.loginWithEmail(email: email, password: password);
      if (!mounted) return;
      _ref.read(currentRoleProvider.notifier).state = CommitteeRole.fromCode(user.roleCode);
      state = AsyncValue.data(user);
    } catch (e, st) {
      if (!mounted) return;
      state = AsyncValue.error(e, st);
      rethrow;
    }
  }

  Future<void> loginWithMobile(String committeeId, String mobile, String password) async {
    state = const AsyncValue.loading();
    try {
      final user = await _authService.loginWithMobile(
        committeeId: committeeId,
        mobile: mobile,
        password: password,
      );
      if (!mounted) return;
      _ref.read(currentRoleProvider.notifier).state = CommitteeRole.fromCode(user.roleCode);
      state = AsyncValue.data(user);
    } catch (e, st) {
      if (!mounted) return;
      state = AsyncValue.error(e, st);
      rethrow;
    }
  }

  Future<void> logout() async {
    state = const AsyncValue.loading();
    await _authService.logout();
    if (!mounted) return;
    _ref.read(currentRoleProvider.notifier).state = CommitteeRole.generalMember;
    state = const AsyncValue.data(null);
  }
}

final authNotifierProvider = StateNotifierProvider<AuthNotifier, AsyncValue<AuthUser?>>((ref) {
  final authService = ref.watch(authServiceProvider);
  return AuthNotifier(authService, ref);
});
