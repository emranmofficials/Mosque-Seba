import '../../../core/database/offline_cache_service.dart';
import '../../../core/network/supabase_service.dart';
import '../models/member_model.dart';

/// Repository for Member CRUD and Search (মসজিদ সদস্য রিপোজিটরি)
class MemberRepository {
  final OfflineCacheService _cache;

  MemberRepository({OfflineCacheService? cache})
      : _cache = cache ?? OfflineCacheService.instance;

  /// Fetch all members for the given mosque
  Future<List<MemberModel>> fetchMembers({
    required String mosqueId,
    String? searchQuery,
    String? roleFilter,
    bool? mealVolunteerOnly,
  }) async {
    final equals = <String, dynamic>{};
    if (roleFilter != null && roleFilter.isNotEmpty && roleFilter != 'all') {
      equals['role_code'] = roleFilter;
    }
    if (mealVolunteerOnly == true) {
      equals['willing_to_provide_staff_meals'] = true;
    }

    final rows = await _cache.fetchList(
      tableName: 'members',
      mosqueId: mosqueId,
      orderBy: 'created_at',
      ascending: false,
      equalsFilters: equals,
    );

    var members = rows.map(MemberModel.fromJson).toList();

    if (searchQuery != null && searchQuery.trim().isNotEmpty) {
      final q = searchQuery.trim().toLowerCase();
      members = members.where((m) {
        return m.nameBn.toLowerCase().contains(q) ||
            m.memberCode.toLowerCase().contains(q) ||
            m.mobile.contains(q) ||
            m.villageArea.toLowerCase().contains(q);
      }).toList();
    }

    return members;
  }

  /// Create a new member (offline-first with SQLite cache & sync queue)
  Future<MemberModel?> createMember({
    required String mosqueId,
    required MemberModel member,
  }) async {
    try {
      final payload = member.toJson();
      if ((payload['id']?.toString().trim() ?? '').isEmpty) {
        payload.remove('id');
      }
      final res = await _cache.insertRecord(
        tableName: 'members',
        mosqueId: mosqueId,
        payload: payload,
      );
      return MemberModel.fromJson(res);
    } catch (_) {
      return null;
    }
  }

  /// Update an existing member
  Future<bool> updateMember(MemberModel member, {String? mosqueId}) async {
    try {
      return await _cache.updateRecord(
        tableName: 'members',
        id: member.id,
        mosqueId: mosqueId,
        updates: member.toJson(),
      );
    } catch (_) {
      return false;
    }
  }

  /// Delete or archive a member
  Future<bool> deleteMember(String memberId) async {
    try {
      return await _cache.deleteRecord(tableName: 'members', id: memberId);
    } catch (_) {
      return false;
    }
  }

  /// Get a single member by ID
  Future<MemberModel?> getMemberById(String id) async {
    try {
      if (SupabaseService.isInitialized) {
        final res = await SupabaseService.client
            .from('members')
            .select()
            .eq('id', id)
            .single();
        return MemberModel.fromJson(res);
      }
    } catch (_) {}
    final local = await _cache.readLocalById(tableName: 'members', id: id);
    return local != null ? MemberModel.fromJson(local) : null;
  }
}
