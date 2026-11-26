import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../core/network/supabase_service.dart';
import '../models/madrasa_attendance_model.dart';

/// Repository for Madrasa Daily Attendance (দৈনিক উপস্থিতি রিপোজিটরি)
class MadrasaAttendanceRepository {
  SupabaseClient get _client => SupabaseService.client;

  /// In-memory attendance cache scoped by mosqueId and date
  final Map<String, List<MadrasaAttendanceModel>> _localCache = {};

  /// Fetch attendance records for a mosque, date, and optional class
  Future<List<MadrasaAttendanceModel>> fetchAttendance({
    required String mosqueId,
    required String date,
    String? className,
  }) async {
    final cacheKey = '$mosqueId|$date';
    try {
      var query = _client
          .from('madrasa_attendance')
          .select()
          .eq('mosque_id', mosqueId)
          .eq('attendance_date', date);

      if (className != null && className.isNotEmpty && className != 'all') {
        query = query.eq('class_name', className);
      }

      final response = await query;
      final List<dynamic> data = response as List<dynamic>;

      final records = data
          .map((item) => MadrasaAttendanceModel.fromJson(item as Map<String, dynamic>))
          .toList();

      if (records.isNotEmpty) {
        _localCache[cacheKey] = records;
      }
      return records;
    } catch (e) {
      return _localCache[cacheKey] ?? [];
    }
  }

  /// Save or update a list of attendance records
  Future<bool> saveAttendanceBatch({
    required String mosqueId,
    required List<MadrasaAttendanceModel> records,
  }) async {
    try {
      final payload = records.map((r) {
        final map = r.toJson();
        map['mosque_id'] = mosqueId;
        return map;
      }).toList();
      await _client.from('madrasa_attendance').upsert(payload);

      if (records.isNotEmpty) {
        final cacheKey = '$mosqueId|${records.first.attendanceDate}';
        _localCache[cacheKey] = records;
      }
      return true;
    } catch (e) {
      return false;
    }
  }
}
