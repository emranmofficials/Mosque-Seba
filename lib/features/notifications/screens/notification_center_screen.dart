import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/router/app_router.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../shared/components/empty_state_view.dart';
import '../../../shared/components/error_state_view.dart';
import '../../../shared/widgets/adaptive_scaffold.dart';
import '../../../shared/widgets/card_shell.dart';
import '../providers/notification_provider.dart';

/// Screen displaying user notifications and administrative alerts (বিজ্ঞপ্তি ও বার্তা কেন্দ্র)
class NotificationCenterScreen extends ConsumerWidget {
  const NotificationCenterScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final notificationsAsync = ref.watch(notificationListProvider);

    return AdaptiveScaffold(
      title: 'বিজ্ঞপ্তি কেন্দ্র',
      actions: [
        IconButton(
          icon: const Icon(Icons.done_all),
          tooltip: 'সব পঠিত হিসেবে চিহ্নিত করুন',
          onPressed: () => ref.read(notificationListProvider.notifier).markAllAsRead(),
        ),
      ],
      body: notificationsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator(color: AppColors.primary)),
        error: (err, _) => ErrorStateView(
          title: 'তথ্য লোড করতে সমস্যা হয়েছে',
          message: err.toString(),
          onRetry: () => ref.read(notificationListProvider.notifier).loadNotifications(),
        ),
        data: (notifications) {
          if (notifications.isEmpty) {
            return const EmptyStateView(
              title: 'কোনো নতুন বিজ্ঞপ্তি নেই',
              message: 'আপনার মসজিদের গুরুত্বপূর্ণ নোটিশ, ভাউচার ও জানাজা এলার্ট এখানে প্রদর্শিত হবে।',
            );
          }

          return ListView.separated(
            padding: const EdgeInsets.all(AppDimens.spacing16),
            itemCount: notifications.length,
            separatorBuilder: (_, __) => const SizedBox(height: 8),
            itemBuilder: (context, index) {
              final notif = notifications[index];
              return CardShell(
                onTap: () {
                  if (!notif.isRead) {
                    ref.read(notificationListProvider.notifier).markAsRead(notif.id);
                  }
                  if (notif.entityType == 'janaza') {
                    context.push(AppRouter.janazaAlerts);
                  } else if (notif.entityType == 'notice') {
                    context.push(AppRouter.notices);
                  } else if (notif.entityType == 'finance') {
                    context.push(AppRouter.finance);
                  }
                },
                padding: const EdgeInsets.all(AppDimens.spacing12),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    CircleAvatar(
                      radius: 18,
                      backgroundColor: notif.isRead
                          ? AppColors.surface
                          : AppColors.primary.withOpacity(0.15),
                      child: Icon(
                        notif.priority == 'urgent' ? Icons.warning_amber : Icons.notifications_none,
                        color: notif.priority == 'urgent' ? AppColors.error : AppColors.primary,
                        size: 20,
                      ),
                    ),
                    const SizedBox(width: AppDimens.spacing12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Flexible(
                                child: Text(
                                  notif.title,
                                  style: AppTypography.titleSmall.copyWith(
                                    fontWeight: notif.isRead ? FontWeight.normal : FontWeight.bold,
                                  ),
                                ),
                              ),
                              if (!notif.isRead)
                                Container(
                                  width: 8,
                                  height: 8,
                                  decoration: const BoxDecoration(
                                    color: AppColors.primary,
                                    shape: BoxShape.circle,
                                  ),
                                ),
                            ],
                          ),
                          const SizedBox(height: 4),
                          Text(
                            notif.body,
                            style: AppTypography.bodySmall.copyWith(color: AppColors.textSecondary),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              );
            },
          );
        },
      ),
    );
  }
}
