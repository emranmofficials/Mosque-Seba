import '../../../core/database/offline_cache_service.dart';
import '../models/app_notification_model.dart';

/// Repository for Notifications Center (নোটিফিকেশন সেন্টার রিপোজিটরি)
class NotificationRepository {
  final OfflineCacheService _cache;

  NotificationRepository({OfflineCacheService? cache})
      : _cache = cache ?? OfflineCacheService.instance;

  /// Fetch notifications for a mosque (Supabase with Drift SQLite fallback)
  Future<List<AppNotificationModel>> fetchNotifications({
    required String mosqueId,
  }) async {
    final rows = await _cache.fetchList(
      tableName: 'notifications',
      mosqueId: mosqueId,
      orderBy: 'created_at',
      ascending: false,
      limit: 50,
    );

    final list = rows.map(AppNotificationModel.fromJson).toList();
    list.sort((a, b) => b.createdAt.compareTo(a.createdAt));
    return list;
  }

  /// Mark single notification as read
  Future<bool> markAsRead(String id, {String? mosqueId}) async {
    try {
      return await _cache.updateRecord(
        tableName: 'notifications',
        id: id,
        mosqueId: mosqueId,
        updates: {'is_read': true},
      );
    } catch (_) {
      return false;
    }
  }

  /// Mark all notifications as read
  Future<bool> markAllAsRead({required String mosqueId}) async {
    try {
      final list = await fetchNotifications(mosqueId: mosqueId);
      for (final item in list.where((n) => !n.isRead)) {
        await _cache.updateRecord(
          tableName: 'notifications',
          id: item.id,
          mosqueId: mosqueId,
          updates: {'is_read': true},
        );
      }
      return true;
    } catch (_) {
      return false;
    }
  }

  /// Delete a single notification
  Future<bool> deleteNotification(String id) async {
    try {
      return await _cache.deleteRecord(tableName: 'notifications', id: id);
    } catch (_) {
      return false;
    }
  }
}
