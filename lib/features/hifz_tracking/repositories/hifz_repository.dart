import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../core/network/supabase_service.dart';
import '../models/hifz_record_model.dart';

/// Repository for Quran & Hifz Progress Tracking (হিফজুল কুরআন রিপোজিটরি)
class HifzRepository {
  SupabaseClient get _client => SupabaseService.client;

  /// Fetch recent Hifz session logs for a mosque
  Future<List<HifzRecordModel>> fetchRecentLogs({
    required String mosqueId,
    String? studentId,
  }) async {
    try {
      var query = _client.from('hifz_progress').select().eq('mosque_id', mosqueId);
      if (studentId != null && studentId.isNotEmpty) {
        query = query.eq('student_id', studentId);
      }

      final res = await query.order('created_at', ascending: false).limit(50);
      final List<dynamic> data = res as List<dynamic>;

      return data
          .map((item) => HifzRecordModel.fromJson(item as Map<String, dynamic>))
          .toList();
    } catch (e) {
      return [];
    }
  }

  /// Log a new Hifz session
  Future<HifzRecordModel?> logSession({
    required String mosqueId,
    required HifzRecordModel record,
  }) async {
    try {
      final payload = record.toJson();
      payload['mosque_id'] = mosqueId;
      if (payload['id'] == null || (payload['id'] as String).isEmpty) {
        payload['id'] = 'hifz-${DateTime.now().millisecondsSinceEpoch}';
      }

      final res = await _client.from('hifz_progress').insert(payload).select().single();
      return HifzRecordModel.fromJson(res);
    } catch (e) {
      return null;
    }
  }
}
