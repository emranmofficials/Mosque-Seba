import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../core/network/supabase_service.dart';
import '../models/meal_schedule_item.dart';

/// Repository for Mosque Meal Schedule (খাবার সূচি রিপোজিটরি)
class MealScheduleRepository {
  SupabaseClient get _client => SupabaseService.client;

  /// Fetch meal schedules for a specific date or date range
  Future<List<MealScheduleItem>> fetchSchedules({
    required String mosqueId,
    String? dateStr,
  }) async {
    try {
      var query = _client.from('food_schedules').select().eq('mosque_id', mosqueId);
      if (dateStr != null && dateStr.isNotEmpty) {
        query = query.eq('date_str', dateStr);
      }

      final response = await query.order('created_at', ascending: true);
      final List<dynamic> data = response as List<dynamic>;

      return data
          .map((item) => MealScheduleItem.fromJson(item as Map<String, dynamic>))
          .toList();
    } catch (e) {
      return [];
    }
  }

  /// Create a new meal schedule slot
  Future<MealScheduleItem?> addSchedule({
    required String mosqueId,
    required MealScheduleItem item,
  }) async {
    try {
      final payload = item.toJson();
      payload['mosque_id'] = mosqueId;
      if (payload['id'] == null || (payload['id'] as String).isEmpty) {
        payload['id'] = 'meal-${DateTime.now().millisecondsSinceEpoch}';
      }

      final res = await _client.from('food_schedules').insert(payload).select().single();
      return MealScheduleItem.fromJson(res);
    } catch (e) {
      return null;
    }
  }

  /// Update meal delivery status (e.g. marked as delivered)
  Future<bool> updateStatus(String id, String newStatus) async {
    try {
      await _client.from('food_schedules').update({'status': newStatus}).eq('id', id);
      return true;
    } catch (e) {
      return false;
    }
  }
}
