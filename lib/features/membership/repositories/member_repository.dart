import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../core/network/supabase_service.dart';
import '../models/member_model.dart';

/// Repository for Member CRUD and Search (মসজিদ সদস্য রিপোজিটরি)
class MemberRepository {
  SupabaseClient get _client => SupabaseService.client;

  /// Fetch all members for the given mosque
  Future<List<MemberModel>> fetchMembers({
    required String mosqueId,
    String? searchQuery,
    String? roleFilter,
    bool? mealVolunteerOnly,
  }) async {
    try {
      var query = _client.from('members').select().eq('mosque_id', mosqueId);

      if (roleFilter != null && roleFilter.isNotEmpty && roleFilter != 'all') {
        query = query.eq('role_code', roleFilter);
      }

      if (mealVolunteerOnly == true) {
        query = query.eq('willing_to_provide_staff_meals', true);
      }

      final response = await query.order('created_at', ascending: false);
      final List<dynamic> data = response as List<dynamic>;

      var members = data
          .map((item) => MemberModel.fromJson(item as Map<String, dynamic>))
          .toList();

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
    } catch (e) {
      return [];
    }
  }

  /// Create a new member
  Future<MemberModel?> createMember({
    required String mosqueId,
    required MemberModel member,
  }) async {
    try {
      final payload = member.toJson();
      payload['mosque_id'] = mosqueId;
      payload.remove('id'); // let PostgreSQL auto-generate UUID or take provided

      final res = await _client.from('members').insert(payload).select().single();
      return MemberModel.fromJson(res);
    } catch (e) {
      return null;
    }
  }

  /// Update an existing member
  Future<bool> updateMember(MemberModel member) async {
    try {
      await _client
          .from('members')
          .update(member.toJson())
          .eq('id', member.id);
      return true;
    } catch (e) {
      return false;
    }
  }

  /// Get a single member by ID
  Future<MemberModel?> getMemberById(String id) async {
    try {
      final res = await _client.from('members').select().eq('id', id).single();
      return MemberModel.fromJson(res);
    } catch (e) {
      return null;
    }
  }
}
