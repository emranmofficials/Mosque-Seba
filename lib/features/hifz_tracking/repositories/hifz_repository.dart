import '../../../core/database/offline_cache_service.dart';
import '../models/hifz_record_model.dart';

/// Repository for Quran & Hifz Progress Tracking (হিফজুল কুরআন রিপোজিটরি)
class HifzRepository {
  final OfflineCacheService _cache;

  HifzRepository({OfflineCacheService? cache})
      : _cache = cache ?? OfflineCacheService.instance;

  /// Fetch recent Hifz session logs for a mosque
  Future<List<HifzRecordModel>> fetchRecentLogs({
    required String mosqueId,
    String? studentId,
  }) async {
    final equals = <String, dynamic>{};
    if (studentId != null && studentId.isNotEmpty) {
      equals['student_id'] = studentId;
    }

    final rows = await _cache.fetchList(
      tableName: 'hifz_progress',
      mosqueId: mosqueId,
      orderBy: 'created_at',
      ascending: false,
      limit: studentId == null ? 100 : null,
      equalsFilters: equals,
    );

    final list = rows.map(HifzRecordModel.fromJson).toList();
    list.sort((a, b) => b.createdAt.compareTo(a.createdAt));
    return list;
  }

  /// Log a new Hifz session
  Future<HifzRecordModel?> logSession({
    required String mosqueId,
    required HifzRecordModel record,
  }) async {
    try {
      final payload = record.toJson();
      if ((payload['id']?.toString().trim() ?? '').isEmpty) {
        payload.remove('id');
      }
      if (payload['created_at'] == 0) payload.remove('created_at');

      final res = await _cache.insertRecord(
        tableName: 'hifz_progress',
        mosqueId: mosqueId,
        payload: payload,
      );
      return HifzRecordModel.fromJson(res);
    } catch (_) {
      return null;
    }
  }
}
