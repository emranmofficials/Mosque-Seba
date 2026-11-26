import '../../../core/database/offline_cache_service.dart';
import '../models/meal_schedule_item.dart';

/// Repository for Mosque Meal Schedule (খাবার সূচি রিপোজিটরি)
class MealScheduleRepository {
  final OfflineCacheService _cache;

  MealScheduleRepository({OfflineCacheService? cache})
      : _cache = cache ?? OfflineCacheService.instance;

  /// Fetch meal schedules for a specific date or date range
  Future<List<MealScheduleItem>> fetchSchedules({
    required String mosqueId,
    String? dateStr,
  }) async {
    final equals = <String, dynamic>{};
    if (dateStr != null && dateStr.isNotEmpty) {
      equals['date_str'] = dateStr;
    }

    final rows = await _cache.fetchList(
      tableName: 'food_schedules',
      mosqueId: mosqueId,
      orderBy: 'created_at',
      ascending: true,
      equalsFilters: equals,
    );

    return rows.map(MealScheduleItem.fromJson).toList();
  }

  /// Create or update a meal schedule slot (upsert if existing id is provided)
  Future<MealScheduleItem?> addSchedule({
    required String mosqueId,
    required MealScheduleItem item,
  }) async {
    try {
      final payload = item.toJson();
      final hasId = (payload['id']?.toString().trim() ?? '').isNotEmpty;
      if (!hasId) {
        payload.remove('id');
      }
      final res = await _cache.insertRecord(
        tableName: 'food_schedules',
        mosqueId: mosqueId,
        payload: payload,
        upsert: hasId,
      );
      return MealScheduleItem.fromJson(res);
    } catch (_) {
      return null;
    }
  }

  /// Update meal delivery status (e.g. marked as delivered)
  Future<bool> updateStatus(String id, String newStatus, {String? mosqueId}) async {
    try {
      return await _cache.updateRecord(
        tableName: 'food_schedules',
        id: id,
        mosqueId: mosqueId,
        updates: {'status': newStatus},
      );
    } catch (_) {
      return false;
    }
  }

  /// Delete a meal schedule slot
  Future<bool> deleteSchedule(String id) async {
    try {
      return await _cache.deleteRecord(tableName: 'food_schedules', id: id);
    } catch (_) {
      return false;
    }
  }
}
