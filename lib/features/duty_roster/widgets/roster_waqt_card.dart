import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../shared/widgets/card_shell.dart';
import '../models/duty_roster_item.dart';

/// Card showing assigned duty for a specific waqt (ফজর, যোহর, আসর, মাগরিব, ইশা, জুমা)
class RosterWaqtCard extends StatelessWidget {
  final String waqtName;
  final List<DutyRosterItem> assignments;
  final VoidCallback onAssign;
  final ValueChanged<String>? onRemove;

  const RosterWaqtCard({
    super.key,
    required this.waqtName,
    required this.assignments,
    required this.onAssign,
    this.onRemove,
  });

  IconData _getWaqtIcon(String waqt) {
    switch (waqt) {
      case 'ফজর':
        return Icons.wb_twilight;
      case 'যোহর':
        return Icons.wb_sunny;
      case 'আসর':
        return Icons.wb_sunny_outlined;
      case 'মাগরিব':
        return Icons.nights_stay_outlined;
      case 'ইশা':
        return Icons.bedtime;
      case 'জুমা':
        return Icons.mosque;
      default:
        return Icons.access_time;
    }
  }

  @override
  Widget build(BuildContext context) {
    return CardShell(
      padding: const EdgeInsets.all(AppDimens.spacing16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(AppDimens.spacing8),
                decoration: BoxDecoration(
                  color: AppColors.primaryContainer,
                  borderRadius: BorderRadius.circular(AppDimens.radiusSm),
                ),
                child: Icon(_getWaqtIcon(waqtName), color: AppColors.primary, size: 22),
              ),
              const SizedBox(width: AppDimens.spacing12),
              Text(
                '$waqtName জামাত',
                style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold),
              ),
              const Spacer(),
              IconButton(
                icon: const Icon(Icons.edit_calendar_outlined, color: AppColors.primary, size: 20),
                tooltip: 'দায়িত্ব পরিবর্তন',
                onPressed: onAssign,
              ),
            ],
          ),
          const SizedBox(height: AppDimens.spacing12),
          const Divider(),
          const SizedBox(height: AppDimens.spacing8),
          if (assignments.isEmpty)
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 8),
              child: Text(
                'কোনো নির্দিষ্ট দায়িত্ব বরাদ্দ নেই (স্বাভাবিক সূচি প্রযোজ্য)',
                style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
              ),
            )
          else
            Column(
              children: assignments.map((item) {
                return Padding(
                  padding: const EdgeInsets.symmetric(vertical: 4),
                  child: Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                        decoration: BoxDecoration(
                          color: AppColors.gold.withOpacity(0.15),
                          borderRadius: BorderRadius.circular(4),
                        ),
                        child: Text(
                          item.roleTitle,
                          style: AppTypography.caption.copyWith(
                            color: AppColors.textPrimary,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                      const SizedBox(width: 8),
                      Text(
                        item.staffNameBn,
                        style: AppTypography.bodyMedium.copyWith(fontWeight: FontWeight.w600),
                      ),
                      if (item.notes.isNotEmpty) ...[
                        const SizedBox(width: 6),
                        Expanded(
                          child: Text(
                            '(${item.notes})',
                            style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                      ] else
                        const Spacer(),
                      if (onRemove != null)
                        IconButton(
                          icon: const Icon(Icons.delete_outline, size: 18, color: Colors.red),
                          padding: EdgeInsets.zero,
                          constraints: const BoxConstraints(),
                          tooltip: 'দায়িত্ব সরান',
                          onPressed: () => onRemove!(item.id),
                        ),
                    ],
                  ),
                );
              }).toList(),
            ),
        ],
      ),
    );
  }
}
