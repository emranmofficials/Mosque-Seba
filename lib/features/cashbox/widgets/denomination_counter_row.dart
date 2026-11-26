import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/utils/bengali_number_formatter.dart';

/// Single interactive banknote denomination row with quantity controls
class DenominationCounterRow extends StatelessWidget {
  final int denomination;
  final int count;
  final ValueChanged<int> onCountChanged;

  const DenominationCounterRow({
    super.key,
    required this.denomination,
    required this.count,
    required this.onCountChanged,
  });

  @override
  Widget build(BuildContext context) {
    final subtotal = denomination * count;

    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          // Banknote Badge
          Container(
            width: 72,
            padding: const EdgeInsets.symmetric(vertical: 6),
            decoration: BoxDecoration(
              color: AppColors.primaryContainer,
              borderRadius: BorderRadius.circular(AppDimens.radiusSm),
              border: Border.all(color: AppColors.primary.withValues(alpha: 0.3)),
            ),
            child: Text(
              '৳${BengaliNumberFormatter.toBengali(denomination.toString())}',
              style: AppTypography.bodySmall.copyWith(
                fontWeight: FontWeight.bold,
                color: AppColors.primary,
              ),
              textAlign: TextAlign.center,
            ),
          ),
          const SizedBox(width: AppDimens.spacing12),
          // Minus Button
          IconButton(
            icon: const Icon(Icons.remove_circle_outline, color: AppColors.textSecondary, size: 22),
            onPressed: count > 0 ? () => onCountChanged(count - 1) : null,
          ),
          // Count Text / Field
          Container(
            width: 48,
            padding: const EdgeInsets.symmetric(vertical: 4),
            alignment: Alignment.center,
            decoration: BoxDecoration(
              color: AppColors.surface,
              borderRadius: BorderRadius.circular(AppDimens.radiusSm),
              border: Border.all(color: AppColors.outline),
            ),
            child: Text(
              BengaliNumberFormatter.toBengali(count.toString()),
              style: AppTypography.bodyMedium.copyWith(fontWeight: FontWeight.bold),
            ),
          ),
          // Plus Button
          IconButton(
            icon: const Icon(Icons.add_circle_outline, color: AppColors.primary, size: 22),
            onPressed: () => onCountChanged(count + 1),
          ),
          const Spacer(),
          // Subtotal
          Text(
            BengaliNumberFormatter.formatCurrency(subtotal.toDouble()),
            style: AppTypography.bodyMedium.copyWith(
              fontWeight: FontWeight.bold,
              color: subtotal > 0 ? AppColors.textPrimary : AppColors.textSecondary,
            ),
          ),
        ],
      ),
    );
  }
}
