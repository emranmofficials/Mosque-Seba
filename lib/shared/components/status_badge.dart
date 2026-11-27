import 'package:flutter/material.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_dimens.dart';

/// Status Badge for Financial Approvals, Notices, and Duty Status
class StatusBadge extends StatelessWidget {
  final String label;
  final Color backgroundColor;
  final Color textColor;
  final IconData? icon;

  const StatusBadge({
    super.key,
    required this.label,
    required this.backgroundColor,
    required this.textColor,
    this.icon,
  });

  factory StatusBadge.approved({String label = 'অনুমোদিত'}) {
    return StatusBadge(
      label: label,
      backgroundColor: AppColors.emeraldSoft,
      textColor: AppColors.emeraldPrimary,
      icon: Icons.check_circle_outline,
    );
  }

  factory StatusBadge.pending({String label = 'অপেক্ষমান'}) {
    return StatusBadge(
      label: label,
      backgroundColor: AppColors.goldLight,
      textColor: AppColors.goldDark,
      icon: Icons.access_time,
    );
  }

  factory StatusBadge.urgent({String label = 'জরুরি'}) {
    return StatusBadge(
      label: label,
      backgroundColor: AppColors.urgentRed.withOpacity(0.12),
      textColor: AppColors.urgentRed,
      icon: Icons.warning_amber_rounded,
    );
  }

  factory StatusBadge.rejected({String label = 'বাতিল'}) {
    return StatusBadge(
      label: label,
      backgroundColor: AppColors.urgentRed.withOpacity(0.12),
      textColor: AppColors.urgentRed,
      icon: Icons.cancel_outlined,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: MosqueSpace.sm,
        vertical: MosqueSpace.xxs,
      ),
      decoration: BoxDecoration(
        color: backgroundColor,
        borderRadius: BorderRadius.circular(MosqueRadius.pill),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (icon != null) ...[
            Icon(icon, size: 12, color: textColor),
            const SizedBox(width: MosqueSpace.xs),
          ],
          Text(
            label,
            style: Theme.of(context).textTheme.labelSmall?.copyWith(
              fontWeight: FontWeight.bold,
              color: textColor,
            ),
          ),
        ],
      ),
    );
  }
}
