import '../../../core/database/offline_cache_service.dart';
import '../models/madrasa_attendance_model.dart';

/// Repository for Madrasa Daily Attendance (দৈনিক উপস্থিতি রিপোজিটরি)
class MadrasaAttendanceRepository {
  final OfflineCacheService _cache;

  MadrasaAttendanceRepository({OfflineCacheService? cache})
      : _cache = cache ?? OfflineCacheService.instance;

  /// Fetch attendance records for a mosque, date, and optional class
  Future<List<MadrasaAttendanceModel>> fetchAttendance({
    required String mosqueId,
    required String date,
    String? className,
  }) async {
    final equals = <String, dynamic>{'attendance_date': date};
    if (className != null && className.isNotEmpty && className != 'all') {
      equals['class_name'] = className;
    }

    final rows = await _cache.fetchList(
      tableName: 'madrasa_attendance',
      mosqueId: mosqueId,
      equalsFilters: equals,
    );

    return rows.map(MadrasaAttendanceModel.fromJson).toList();
  }

  /// Save or update a list of attendance records (Drift SQLite + Supabase batch upsert)
  Future<bool> saveAttendanceBatch({
    required String mosqueId,
    required List<MadrasaAttendanceModel> records,
  }) async {
    try {
      final prepared = records.map((r) {
        final map = r.toJson();
        if ((map['id']?.toString().trim() ?? '').isEmpty) {
          map['id'] = 'att_${r.studentId}_${r.attendanceDate}';
        }
        if (map['created_at'] == 0) map.remove('created_at');
        return map;
      }).toList();

      return await _cache.upsertBatch(
        tableName: 'madrasa_attendance',
        mosqueId: mosqueId,
        records: prepared,
      );
    } catch (_) {
      return false;
    }
  }
}
