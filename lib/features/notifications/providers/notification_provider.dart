import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../authentication/providers/auth_provider.dart';
import '../models/app_notification_model.dart';
import '../repositories/notification_repository.dart';

final notificationRepositoryProvider = Provider<NotificationRepository>((ref) {
  return NotificationRepository();
});

class NotificationNotifier extends StateNotifier<AsyncValue<List<AppNotificationModel>>> {
  final NotificationRepository _repository;
  final String _mosqueId;

  NotificationNotifier(this._repository, this._mosqueId)
      : super(const AsyncValue.loading()) {
    loadNotifications();
  }

  Future<void> loadNotifications() async {
    state = const AsyncValue.loading();
    try {
      final list = await _repository.fetchNotifications(mosqueId: _mosqueId);
      state = AsyncValue.data(list);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  Future<void> markAsRead(String id) async {
    await _repository.markAsRead(id);
    await loadNotifications();
  }

  Future<void> markAllAsRead() async {
    await _repository.markAllAsRead(mosqueId: _mosqueId);
    await loadNotifications();
  }
}

final notificationListProvider = StateNotifierProvider<NotificationNotifier, AsyncValue<List<AppNotificationModel>>>((ref) {
  final repo = ref.watch(notificationRepositoryProvider);
  final authState = ref.watch(authProvider);
  final mosqueId = authState.user?.mosqueId ?? 'mosque-default';
  return NotificationNotifier(repo, mosqueId);
});

final unreadNotificationCountProvider = Provider<int>((ref) {
  final listAsync = ref.watch(notificationListProvider);
  return listAsync.maybeWhen(
    data: (list) => list.where((n) => !n.isRead).length,
    orElse: () => 0,
  );
});
