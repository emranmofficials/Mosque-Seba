import '../../../core/database/offline_cache_service.dart';
import '../models/duty_roster_item.dart';

/// Repository for Weekly Duty Roster (দায়িত্ব রোস্টার রিপোজিটরি)
class DutyRosterRepository {
  final OfflineCacheService _cache;

  DutyRosterRepository({OfflineCacheService? cache})
      : _cache = cache ?? OfflineCacheService.instance;

  /// Fetch duty roster items, optionally filtered by day of week
  Future<List<DutyRosterItem>> fetchRoster({
    required String mosqueId,
    String? dayOfWeek,
  }) async {
    final equals = <String, dynamic>{};
    if (dayOfWeek != null && dayOfWeek.isNotEmpty && dayOfWeek != 'সকল') {
      equals['day_of_week'] = dayOfWeek;
    }

    final rows = await _cache.fetchList(
      tableName: 'duty_roster',
      mosqueId: mosqueId,
      orderBy: 'created_at',
      ascending: true,
      equalsFilters: equals,
    );

    return rows.map(DutyRosterItem.fromJson).toList();
  }

  /// Assign or update duty roster slot
  Future<DutyRosterItem?> assignDuty({
    required String mosqueId,
    required DutyRosterItem item,
  }) async {
    try {
      final payload = item.toJson();
      if ((payload['id']?.toString().trim() ?? '').isEmpty) {
        payload.remove('id');
      }
      final res = await _cache.insertRecord(
        tableName: 'duty_roster',
        mosqueId: mosqueId,
        payload: payload,
        upsert: true,
      );
      return DutyRosterItem.fromJson(res);
    } catch (_) {
      return null;
    }
  }

  /// Delete a duty assignment
  Future<bool> removeDuty(String id) async {
    try {
      return await _cache.deleteRecord(tableName: 'duty_roster', id: id);
    } catch (_) {
      return false;
    }
  }
}
