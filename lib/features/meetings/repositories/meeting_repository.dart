import '../../../core/database/offline_cache_service.dart';
import '../models/meeting_model.dart';

/// Repository for Mosque Meetings & Resolutions (কমিটির সভা ও সিদ্ধান্ত রিপোজিটরি)
class MeetingRepository {
  final OfflineCacheService _cache;

  MeetingRepository({OfflineCacheService? cache})
      : _cache = cache ?? OfflineCacheService.instance;

  /// Fetch all meetings for a mosque
  Future<List<MeetingModel>> fetchMeetings({
    required String mosqueId,
    String? statusFilter,
  }) async {
    final equals = <String, dynamic>{};
    if (statusFilter != null && statusFilter.isNotEmpty && statusFilter != 'all') {
      equals['status'] = statusFilter;
    }

    final rows = await _cache.fetchList(
      tableName: 'meetings',
      mosqueId: mosqueId,
      orderBy: 'meeting_date',
      ascending: false,
      equalsFilters: equals,
    );

    return rows.map(MeetingModel.fromJson).toList();
  }

  /// Schedule a new meeting
  Future<MeetingModel?> scheduleMeeting({
    required String mosqueId,
    required MeetingModel meeting,
  }) async {
    try {
      final payload = meeting.toJson();
      if ((payload['id']?.toString().trim() ?? '').isEmpty) {
        payload.remove('id');
      }
      final res = await _cache.insertRecord(
        tableName: 'meetings',
        mosqueId: mosqueId,
        payload: payload,
      );
      return MeetingModel.fromJson(res);
    } catch (_) {
      return null;
    }
  }

  /// Update meeting details
  Future<bool> updateMeeting(MeetingModel meeting, {String? mosqueId}) async {
    try {
      return await _cache.updateRecord(
        tableName: 'meetings',
        id: meeting.id,
        mosqueId: mosqueId,
        updates: meeting.toJson(),
      );
    } catch (_) {
      return false;
    }
  }

  /// Record resolutions and mark meeting as completed
  Future<bool> recordResolution(
    String meetingId,
    String resolution, {
    String? mosqueId,
  }) async {
    try {
      return await _cache.updateRecord(
        tableName: 'meetings',
        id: meetingId,
        mosqueId: mosqueId,
        updates: {
          'resolution': resolution,
          'status': 'completed',
        },
      );
    } catch (_) {
      return false;
    }
  }

  /// Delete or cancel a meeting
  Future<bool> deleteMeeting(String meetingId) async {
    try {
      return await _cache.deleteRecord(tableName: 'meetings', id: meetingId);
    } catch (_) {
      return false;
    }
  }
}
