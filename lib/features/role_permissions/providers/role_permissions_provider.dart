import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../bootstrap/dependency_injection.dart';
import '../models/committee_role.dart';
import '../services/permission_engine.dart';

final currentRoleProvider = StateProvider<CommitteeRole>((ref) {
  final storage = ref.watch(sessionStorageServiceProvider);
  return CommitteeRole.fromCode(storage.userRoleCode);
});

final currentUiProfileProvider = Provider<UiProfile>((ref) {
  final role = ref.watch(currentRoleProvider);
  return PermissionEngine.resolveUiProfile(role);
});

final hasPermissionProvider = Provider.family<bool, String>((ref, permission) {
  final role = ref.watch(currentRoleProvider);
  return PermissionEngine.hasPermission(role, permission);
});
