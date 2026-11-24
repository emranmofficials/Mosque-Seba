import 'package:flutter/material.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/utils/bengali_number_formatter.dart';
import '../../../../shared/widgets/card_shell.dart';
import '../models/staff_model.dart';

/// Card component presenting a mosque staff member (পেশ ইমাম, মুয়াজ্জিন, খাদেম)
class StaffCard extends StatelessWidget {
  final StaffModel staff;
  final VoidCallback onPaySalary;

  const StaffCard({
    super.key,
    required this.staff,
    required this.onPaySalary,
  });

  @override
  Widget build(BuildContext context) {
    return CardShell(
      padding: const EdgeInsets.all(AppDimens.spacing16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              CircleAvatar(
                radius: 26,
                backgroundColor: AppColors.primaryContainer,
                child: const Icon(Icons.person, color: AppColors.primary, size: 28),
              ),
              const SizedBox(width: AppDimens.spacing12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      staff.nameBn,
                      style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    const SizedBox(height: 2),
                    Row(
                      children: [
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                          decoration: BoxDecoration(
                            color: AppColors.primaryContainer,
                            borderRadius: BorderRadius.circular(AppDimens.radiusXs),
                          ),
                          child: Text(
                            staff.designationBn,
                            style: AppTypography.caption.copyWith(
                              color: AppColors.primary,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ),
                        if (staff.mealRequired) ...[
                          const SizedBox(width: 6),
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                            decoration: BoxDecoration(
                              color: AppColors.gold.withValues(alpha: 0.15),
                              borderRadius: BorderRadius.circular(AppDimens.radiusXs),
                            ),
                            child: Text(
                              'খাবার প্রযোজ্য',
                              style: AppTypography.caption.copyWith(
                                color: AppColors.textPrimary,
                                fontSize: 10,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                        ],
                      ],
                    ),
                  ],
                ),
              ),
              Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Text('মাসিক বেতন', style: AppTypography.caption.copyWith(color: AppColors.textSecondary)),
                  Text(
                    BengaliNumberFormatter.formatCurrency(staff.salaryAmount),
                    style: AppTypography.titleMedium.copyWith(
                      fontWeight: FontWeight.bold,
                      color: AppColors.primary,
                    ),
                  ),
                ],
              ),
            ],
          ),
          const SizedBox(height: AppDimens.spacing12),
          const Divider(),
          const SizedBox(height: AppDimens.spacing8),
          Row(
            children: [
              const Icon(Icons.phone_outlined, size: 14, color: AppColors.textSecondary),
              const SizedBox(width: 4),
              Text(
                BengaliNumberFormatter.toBengali(staff.mobile),
                style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
              ),
              const Spacer(),
              ElevatedButton.icon(
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.primary,
                  foregroundColor: AppColors.onPrimary,
                  padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSm)),
                ),
                icon: const Icon(Icons.payments_outlined, size: 16),
                label: const Text('বেতন পরিশোধ', style: TextStyle(fontWeight: FontWeight.bold)),
                onPressed: onPaySalary,
              ),
            ],
          ),
        ],
      ),
    );
  }
}
