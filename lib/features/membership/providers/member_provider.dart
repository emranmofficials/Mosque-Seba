import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../authentication/providers/auth_provider.dart';
import '../models/member_model.dart';
import '../repositories/member_repository.dart';

final memberRepositoryProvider = Provider<MemberRepository>((ref) {
  return MemberRepository();
});

final memberSearchQueryProvider = StateProvider<String>((ref) => '');
final memberRoleFilterProvider = StateProvider<String>((ref) => 'all');
final memberMealVolunteerFilterProvider = StateProvider<bool>((ref) => false);

/// State notifier for the members list
class MemberListNotifier extends StateNotifier<AsyncValue<List<MemberModel>>> {
  final MemberRepository _repository;
  final String _mosqueId;
  final Ref _ref;

  MemberListNotifier(this._repository, this._mosqueId, this._ref)
      : super(const AsyncValue.loading()) {
    loadMembers();
  }

  Future<void> loadMembers() async {
    state = const AsyncValue.loading();
    try {
      final search = _ref.read(memberSearchQueryProvider);
      final role = _ref.read(memberRoleFilterProvider);
      final mealOnly = _ref.read(memberMealVolunteerFilterProvider);

      final result = await _repository.fetchMembers(
        mosqueId: _mosqueId,
        searchQuery: search,
        roleFilter: role,
        mealVolunteerOnly: mealOnly,
      );
      if (!mounted) return;
      state = AsyncValue.data(result);
    } catch (e, st) {
      if (!mounted) return;
      state = AsyncValue.error(e, st);
    }
  }

  Future<bool> addMember(MemberModel member) async {
    final created = await _repository.createMember(
      mosqueId: _mosqueId,
      member: member,
    );
    if (created != null) {
      await loadMembers();
      return true;
    }
    return false;
  }

  Future<bool> updateMember(MemberModel member) async {
    final ok = await _repository.updateMember(member, mosqueId: _mosqueId);
    if (ok) {
      await loadMembers();
      return true;
    }
    return false;
  }

  Future<bool> deleteMember(String memberId) async {
    final ok = await _repository.deleteMember(memberId);
    if (ok) {
      await loadMembers();
      return true;
    }
    return false;
  }
}

final memberListProvider = StateNotifierProvider<MemberListNotifier, AsyncValue<List<MemberModel>>>((ref) {
  final repo = ref.watch(memberRepositoryProvider);
  final authState = ref.watch(authNotifierProvider);
  final mosqueId = authState.valueOrNull?.mosqueId ?? 'mosque-default';
  return MemberListNotifier(repo, mosqueId, ref);
});
