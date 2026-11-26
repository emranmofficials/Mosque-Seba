import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../core/network/supabase_service.dart';
import '../models/meeting_model.dart';

/// Repository for Mosque Meetings & Resolutions (কমিটির সভা ও সিদ্ধান্ত রিপোজিটরি)
class MeetingRepository {
  SupabaseClient get _client => SupabaseService.client;

  /// Fetch all meetings for a mosque
  Future<List<MeetingModel>> fetchMeetings({
    required String mosqueId,
    String? statusFilter,
  }) async {
    try {
      var query = _client.from('meetings').select().eq('mosque_id', mosqueId);
      if (statusFilter != null && statusFilter.isNotEmpty && statusFilter != 'all') {
        query = query.eq('status', statusFilter);
      }

      final response = await query.order('meeting_date', ascending: false);
      final List<dynamic> data = response as List<dynamic>;

      return data
          .map((item) => MeetingModel.fromJson(item as Map<String, dynamic>))
          .toList();
    } catch (e) {
      return [];
    }
  }

  /// Schedule a new meeting
  Future<MeetingModel?> scheduleMeeting({
    required String mosqueId,
    required MeetingModel meeting,
  }) async {
    try {
      final payload = meeting.toJson();
      payload['mosque_id'] = mosqueId;
      if (payload['id'] == null || (payload['id'] as String).isEmpty) {
        payload['id'] = 'meet-${DateTime.now().millisecondsSinceEpoch}';
      }

      final res = await _client.from('meetings').insert(payload).select().single();
      return MeetingModel.fromJson(res);
    } catch (e) {
      return null;
    }
  }

  /// Record resolutions and mark meeting as completed
  Future<bool> recordResolution(String meetingId, String resolution) async {
    try {
      await _client.from('meetings').update({
        'resolution': resolution,
        'status': 'completed',
      }).eq('id', meetingId);
      return true;
    } catch (e) {
      return false;
    }
  }
}
