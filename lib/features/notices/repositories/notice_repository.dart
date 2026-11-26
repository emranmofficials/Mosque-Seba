import '../../../core/database/offline_cache_service.dart';
import '../../../core/services/notification_service.dart';
import '../models/notice_model.dart';

/// Repository for Official Mosque Notices (মসজিদ নোটিশ রিপোজিটরি)
class NoticeRepository {
  final OfflineCacheService _cache;

  NoticeRepository({OfflineCacheService? cache})
      : _cache = cache ?? OfflineCacheService.instance;

  /// Fetch all notices for a mosque with optional priority filter
  Future<List<NoticeModel>> fetchNotices({
    required String mosqueId,
    String? priorityFilter,
    bool? pinnedOnly,
  }) async {
    final equals = <String, dynamic>{};
    if (priorityFilter != null && priorityFilter.isNotEmpty && priorityFilter != 'all') {
      equals['priority'] = priorityFilter.toLowerCase();
    }
    if (pinnedOnly == true) {
      equals['is_pinned'] = true;
    }

    final rows = await _cache.fetchList(
      tableName: 'notices',
      mosqueId: mosqueId,
      orderBy: 'created_at',
      ascending: false,
      equalsFilters: equals,
    );

    final list = rows.map(NoticeModel.fromJson).toList();
    list.sort((a, b) {
      if (a.isPinned != b.isPinned) return a.isPinned ? -1 : 1;
      return b.createdAt.compareTo(a.createdAt);
    });
    return list;
  }

  /// Publish a new notice, create in-app notification, and trigger local push for urgent items
  Future<NoticeModel?> publishNotice({
    required String mosqueId,
    required NoticeModel notice,
  }) async {
    try {
      final payload = notice.toJson();
      if ((payload['id']?.toString().trim() ?? '').isEmpty) {
        payload.remove('id');
      }
      final res = await _cache.insertRecord(
        tableName: 'notices',
        mosqueId: mosqueId,
        payload: payload,
      );
      final saved = NoticeModel.fromJson(res);

      // Record inside Notification Center
      await _cache.insertRecord(
        tableName: 'notifications',
        mosqueId: mosqueId,
        payload: {
          'mosque_id': mosqueId,
          'title': saved.isUrgent ? '🚨 জরুরি নোটিশ: ${saved.title}' : saved.title,
          'body': saved.content.length > 140
              ? '${saved.content.substring(0, 140)}...'
              : saved.content,
          'priority': saved.priority,
          'entity_type': 'notice',
          'entity_id': saved.id,
          'is_read': false,
        },
      );

      if (saved.isUrgent || saved.isImportant) {
        await NotificationService.instance.showInstantAlert(
          title: saved.isUrgent ? '🚨 ${saved.title}' : '📢 ${saved.title}',
          body: saved.content.length > 120
              ? '${saved.content.substring(0, 120)}...'
              : saved.content,
          payload: '/notices',
          isUrgent: saved.isUrgent,
        );
      }

      return saved;
    } catch (_) {
      return null;
    }
  }

  /// Update an existing notice
  Future<bool> updateNotice(NoticeModel notice, {String? mosqueId}) async {
    try {
      return await _cache.updateRecord(
        tableName: 'notices',
        id: notice.id,
        mosqueId: mosqueId,
        updates: notice.toJson(),
      );
    } catch (_) {
      return false;
    }
  }

  /// Toggle pin status
  Future<bool> togglePin(String noticeId, bool isPinned, {String? mosqueId}) async {
    try {
      return await _cache.updateRecord(
        tableName: 'notices',
        id: noticeId,
        mosqueId: mosqueId,
        updates: {'is_pinned': isPinned},
      );
    } catch (_) {
      return false;
    }
  }

  /// Delete a notice
  Future<bool> deleteNotice(String noticeId) async {
    try {
      return await _cache.deleteRecord(tableName: 'notices', id: noticeId);
    } catch (_) {
      return false;
    }
  }
}
