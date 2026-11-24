import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../../core/network/supabase_service.dart';
import '../models/notice_model.dart';

/// Repository for Official Mosque Notices (মসজিদ নোটিশ রিপোজিটরি)
class NoticeRepository {
  final SupabaseService _supabaseService;

  NoticeRepository({SupabaseService? supabaseService})
      : _supabaseService = supabaseService ?? SupabaseService();

  SupabaseClient get _client => _supabaseService.client;

  /// Fetch all notices for a mosque with optional priority filter
  Future<List<NoticeModel>> fetchNotices({
    required String mosqueId,
    String? priorityFilter,
    bool? pinnedOnly,
  }) async {
    try {
      var query = _client.from('notices').select().eq('mosque_id', mosqueId);

      if (priorityFilter != null && priorityFilter.isNotEmpty && priorityFilter != 'all') {
        query = query.eq('priority', priorityFilter.toLowerCase());
      }

      if (pinnedOnly == true) {
        query = query.eq('is_pinned', true);
      }

      final response = await query.order('is_pinned', ascending: false).order('created_at', ascending: false);
      final List<dynamic> data = response as List<dynamic>;

      return data
          .map((item) => NoticeModel.fromJson(item as Map<String, dynamic>))
          .toList();
    } catch (e) {
      return [];
    }
  }

  /// Publish a new notice
  Future<NoticeModel?> publishNotice({
    required String mosqueId,
    required NoticeModel notice,
  }) async {
    try {
      final payload = notice.toJson();
      payload['mosque_id'] = mosqueId;
      if (payload['id'] == null || (payload['id'] as String).isEmpty) {
        payload['id'] = 'not-${DateTime.now().millisecondsSinceEpoch}';
      }

      final res = await _client.from('notices').insert(payload).select().single();
      return NoticeModel.fromJson(res);
    } catch (e) {
      return null;
    }
  }

  /// Toggle pin status
  Future<bool> togglePin(String noticeId, bool isPinned) async {
    try {
      await _client.from('notices').update({'is_pinned': isPinned}).eq('id', noticeId);
      return true;
    } catch (e) {
      return false;
    }
  }

  /// Delete a notice
  Future<bool> deleteNotice(String noticeId) async {
    try {
      await _client.from('notices').delete().eq('id', noticeId);
      return true;
    } catch (e) {
      return false;
    }
  }
}
