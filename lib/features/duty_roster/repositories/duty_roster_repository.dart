import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../../core/network/supabase_service.dart';
import '../models/duty_roster_item.dart';

/// Repository for Weekly Duty Roster (দায়িত্ব রোস্টার রিপোজিটরি)
class DutyRosterRepository {
  final SupabaseService _supabaseService;

  DutyRosterRepository({SupabaseService? supabaseService})
      : _supabaseService = supabaseService ?? SupabaseService();

  SupabaseClient get _client => _supabaseService.client;

  /// Fetch duty roster items, optionally filtered by day of week
  Future<List<DutyRosterItem>> fetchRoster({
    required String mosqueId,
    String? dayOfWeek,
  }) async {
    try {
      var query = _client.from('duty_roster').select();
      if (dayOfWeek != null && dayOfWeek.isNotEmpty && dayOfWeek != 'সকল') {
        query = query.eq('day_of_week', dayOfWeek);
      }

      final response = await query.order('created_at', ascending: true);
      final List<dynamic> data = response as List<dynamic>;

      return data
          .map((item) => DutyRosterItem.fromJson(item as Map<String, dynamic>))
          .toList();
    } catch (e) {
      return [];
    }
  }

  /// Assign or update duty roster slot
  Future<DutyRosterItem?> assignDuty(DutyRosterItem item) async {
    try {
      final payload = item.toJson();
      if (payload['id'] == null || (payload['id'] as String).isEmpty) {
        payload['id'] = 'ros-${DateTime.now().millisecondsSinceEpoch}';
      }

      final res = await _client.from('duty_roster').upsert(payload).select().single();
      return DutyRosterItem.fromJson(res);
    } catch (e) {
      return null;
    }
  }

  /// Delete a duty assignment
  Future<bool> removeDuty(String id) async {
    try {
      await _client.from('duty_roster').delete().eq('id', id);
      return true;
    } catch (e) {
      return false;
    }
  }
}
