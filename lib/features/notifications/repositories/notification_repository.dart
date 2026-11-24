import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../../core/network/supabase_service.dart';
import '../models/app_notification_model.dart';

/// Repository for Notifications Center (নোটিফিকেশন সেন্টার রিপোজিটরি)
class NotificationRepository {
  final SupabaseService _supabaseService;

  NotificationRepository({SupabaseService? supabaseService})
      : _supabaseService = supabaseService ?? SupabaseService();

  SupabaseClient get _client => _supabaseService.client;

  /// Fetch notifications for a mosque
  Future<List<AppNotificationModel>> fetchNotifications({required String mosqueId}) async {
    try {
      final response = await _client
          .from('notifications')
          .select()
          .eq('mosque_id', mosqueId)
          .order('created_at', ascending: false)
          .limit(50);

      final List<dynamic> data = response as List<dynamic>;
      return data
          .map((item) => AppNotificationModel.fromJson(item as Map<String, dynamic>))
          .toList();
    } catch (e) {
      return [];
    }
  }

  /// Mark single notification as read
  Future<bool> markAsRead(String id) async {
    try {
      await _client.from('notifications').update({'is_read': true}).eq('id', id);
      return true;
    } catch (e) {
      return false;
    }
  }

  /// Mark all notifications as read
  Future<bool> markAllAsRead({required String mosqueId}) async {
    try {
      await _client.from('notifications').update({'is_read': true}).eq('mosque_id', mosqueId);
      return true;
    } catch (e) {
      return false;
    }
  }
}
