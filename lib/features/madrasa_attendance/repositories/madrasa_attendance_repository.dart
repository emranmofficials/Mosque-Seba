import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../../core/network/supabase_service.dart';
import '../models/madrasa_attendance_model.dart';

/// Repository for Madrasa Daily Attendance (দৈনিক উপস্থিতি রিপোজিটরি)
class MadrasaAttendanceRepository {
  final SupabaseService _supabaseService;

  MadrasaAttendanceRepository({SupabaseService? supabaseService})
      : _supabaseService = supabaseService ?? SupabaseService();

  SupabaseClient get _client => SupabaseService.client;

  /// In-memory attendance cache for offline and instant UI response
  final Map<String, List<MadrasaAttendanceModel>> _localCache = {};

  /// Fetch attendance records for a specific date and class
  Future<List<MadrasaAttendanceModel>> fetchAttendance({
    required String date,
    String? className,
  }) async {
    try {
      var query = _client.from('madrasa_attendance').select().eq('attendance_date', date);

      if (className != null && className.isNotEmpty && className != 'all') {
        query = query.eq('class_name', className);
      }

      final response = await query;
      final List<dynamic> data = response as List<dynamic>;

      final records = data
          .map((item) => MadrasaAttendanceModel.fromJson(item as Map<String, dynamic>))
          .toList();

      if (records.isNotEmpty) {
        _localCache[date] = records;
      }
      return records;
    } catch (e) {
      return _localCache[date] ?? [];
    }
  }

  /// Save or update a list of attendance records
  Future<bool> saveAttendanceBatch(List<MadrasaAttendanceModel> records) async {
    try {
      final payload = records.map((r) => r.toJson()).toList();
      await _client.from('madrasa_attendance').upsert(payload);

      if (records.isNotEmpty) {
        final date = records.first.attendanceDate;
        _localCache[date] = records;
      }
      return true;
    } catch (e) {
      if (records.isNotEmpty) {
        final date = records.first.attendanceDate;
        _localCache[date] = records;
      }
      return true;
    }
  }
}
