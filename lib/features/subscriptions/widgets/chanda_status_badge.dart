import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';

/// Status badge showing payment standing (পরিশোধিত, বকেয়া, আংশিক)
class ChandaStatusBadge extends StatelessWidget {
  final String status;

  const ChandaStatusBadge({super.key, required this.status});

  @override
  Widget build(BuildContext context) {
    Color bg;
    Color fg;
    String label;

    switch (status.toLowerCase()) {
      case 'paid':
      case 'পরিশোধিত':
        bg = AppColors.success.withValues(alpha: 0.12);
        fg = AppColors.success;
        label = 'পরিশোধিত';
        break;
      case 'partial':
      case 'আংশিক':
        bg = AppColors.warning.withValues(alpha: 0.12);
        fg = AppColors.warning;
        label = 'আংশিক';
        break;
      default:
        bg = AppColors.error.withValues(alpha: 0.12);
        fg = AppColors.error;
        label = 'বকেয়া';
        break;
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(AppDimens.radiusSm),
        border: Border.all(color: fg.withValues(alpha: 0.3)),
      ),
      child: Text(
        label,
        style: AppTypography.caption.copyWith(
          color: fg,
          fontWeight: FontWeight.bold,
        ),
      ),
    );
  }
}
