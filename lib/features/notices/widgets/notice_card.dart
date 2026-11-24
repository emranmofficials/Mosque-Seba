import 'package:flutter/material.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/utils/bengali_number_formatter.dart';
import '../../../../shared/widgets/card_shell.dart';
import '../models/notice_model.dart';

/// Card component presenting a mosque notice with priority and pin badges
class NoticeCard extends StatelessWidget {
  final NoticeModel notice;
  final VoidCallback? onPinToggle;
  final VoidCallback? onDelete;

  const NoticeCard({
    super.key,
    required this.notice,
    this.onPinToggle,
    this.onDelete,
  });

  Color _getPriorityColor() {
    switch (notice.priority.toLowerCase()) {
      case 'urgent':
        return AppColors.error;
      case 'important':
        return AppColors.gold;
      default:
        return AppColors.primary;
    }
  }

  String _getPriorityLabel() {
    switch (notice.priority.toLowerCase()) {
      case 'urgent':
        return 'জরুরি ঘোষণা';
      case 'important':
        return 'গুরুত্বপূর্ণ';
      default:
        return 'সাধারণ নোটিশ';
    }
  }

  @override
  Widget build(BuildContext context) {
    final priorityColor = _getPriorityColor();

    return CardShell(
      padding: const EdgeInsets.all(AppDimens.spacing16),
      border: notice.isPinned
          ? Border.all(color: AppColors.gold.withValues(alpha: 0.6), width: 1.5)
          : null,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(
                  color: priorityColor.withValues(alpha: 0.12),
                  borderRadius: BorderRadius.circular(AppDimens.radiusXs),
                  border: Border.all(color: priorityColor.withValues(alpha: 0.4)),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    if (notice.isUrgent) ...[
                      const Icon(Icons.warning_amber_rounded, size: 14, color: AppColors.error),
                      const SizedBox(width: 4),
                    ],
                    Text(
                      _getPriorityLabel(),
                      style: AppTypography.caption.copyWith(
                        color: priorityColor,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
              ),
              if (notice.isPinned) ...[
                const SizedBox(width: 6),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                  decoration: BoxDecoration(
                    color: AppColors.gold.withValues(alpha: 0.15),
                    borderRadius: BorderRadius.circular(AppDimens.radiusXs),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Icon(Icons.push_pin, size: 12, color: AppColors.gold),
                      const SizedBox(width: 2),
                      Text(
                        'পিন করা',
                        style: AppTypography.caption.copyWith(
                          color: AppColors.textPrimary,
                          fontSize: 10,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
              const Spacer(),
              Text(
                BengaliNumberFormatter.toBengali(notice.publishDate),
                style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
              ),
              if (onPinToggle != null || onDelete != null)
                PopupMenuButton<String>(
                  icon: const Icon(Icons.more_vert, size: 18, color: AppColors.textSecondary),
                  onSelected: (val) {
                    if (val == 'pin') onPinToggle?.call();
                    if (val == 'delete') onDelete?.call();
                  },
                  itemBuilder: (ctx) => [
                    PopupMenuItem(
                      value: 'pin',
                      child: Text(notice.isPinned ? 'আন-পিন করুন' : 'পিন করুন'),
                    ),
                    if (onDelete != null)
                      const PopupMenuItem(
                        value: 'delete',
                        child: Text('মুছে ফেলুন', style: TextStyle(color: AppColors.error)),
                      ),
                  ],
                ),
            ],
          ),
          const SizedBox(height: AppDimens.spacing10),
          Text(
            notice.title,
            style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 6),
          Text(
            notice.content,
            style: AppTypography.bodyMedium.copyWith(color: AppColors.textPrimary),
          ),
          const SizedBox(height: AppDimens.spacing12),
          const Divider(),
          const SizedBox(height: 4),
          Row(
            children: [
              const Icon(Icons.person_outline, size: 14, color: AppColors.textSecondary),
              const SizedBox(width: 4),
              Text(
                'প্রচারক: ${notice.createdBy}',
                style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
