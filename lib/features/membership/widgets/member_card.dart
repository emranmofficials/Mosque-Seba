import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/widgets/card_shell.dart';
import '../models/member_model.dart';

/// Presentation card for a mosque member in the member list
class MemberCard extends StatelessWidget {
  final MemberModel member;
  final VoidCallback onTap;
  final VoidCallback? onEdit;
  final VoidCallback? onCollectChanda;

  const MemberCard({
    super.key,
    required this.member,
    required this.onTap,
    this.onEdit,
    this.onCollectChanda,
  });

  @override
  Widget build(BuildContext context) {
    return CardShell(
      onTap: onTap,
      padding: const EdgeInsets.all(AppDimens.spacing12),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          // Avatar or initial
          CircleAvatar(
            radius: 24,
            backgroundColor: AppColors.primaryContainer,
            child: Text(
              member.nameBn.isNotEmpty ? member.nameBn.characters.first : 'স',
              style: AppTypography.titleMedium.copyWith(
                color: AppColors.primary,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
          const SizedBox(width: AppDimens.spacing12),
          // Info column
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Flexible(
                      child: Text(
                        member.nameBn,
                        style: AppTypography.titleMedium.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                    const SizedBox(width: AppDimens.spacing6),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                      decoration: BoxDecoration(
                        color: AppColors.gold.withOpacity(0.15),
                        borderRadius: BorderRadius.circular(AppDimens.radiusXs),
                        border: Border.all(
                          color: AppColors.gold.withOpacity(0.4),
                          width: 0.8,
                        ),
                      ),
                      child: Text(
                        BengaliNumberFormatter.toBengali(member.memberCode),
                        style: AppTypography.caption.copyWith(
                          color: AppColors.textPrimary,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 3),
                Row(
                  children: [
                    const Icon(
                      Icons.phone_outlined,
                      size: 13,
                      color: AppColors.textSecondary,
                    ),
                    const SizedBox(width: 4),
                    Text(
                      BengaliNumberFormatter.toBengali(member.mobile),
                      style: AppTypography.caption.copyWith(
                        color: AppColors.textSecondary,
                      ),
                    ),
                    if (member.villageArea.isNotEmpty) ...[
                      const SizedBox(width: 8),
                      const Text('•', style: TextStyle(color: AppColors.textSecondary, fontSize: 10)),
                      const SizedBox(width: 8),
                      Flexible(
                        child: Text(
                          member.villageArea,
                          style: AppTypography.caption.copyWith(
                            color: AppColors.textSecondary,
                          ),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    ],
                  ],
                ),
                const SizedBox(height: 4),
                Row(
                  children: [
                    Text(
                      'মাসিক চাঁদা: ${BengaliNumberFormatter.formatCurrency(member.monthlyChanda)}',
                      style: AppTypography.caption.copyWith(
                        color: AppColors.primary,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    if (member.willingToProvideStaffMeals) ...[
                      const SizedBox(width: 6),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 5, vertical: 1),
                        decoration: BoxDecoration(
                          color: AppColors.success.withOpacity(0.12),
                          borderRadius: BorderRadius.circular(4),
                        ),
                        child: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            const Icon(
                              Icons.restaurant,
                              size: 11,
                              color: AppColors.success,
                            ),
                            const SizedBox(width: 3),
                            Text(
                              'খাবার জোগানদাতা',
                              style: AppTypography.caption.copyWith(
                                color: AppColors.success,
                                fontSize: 10,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ],
                ),
              ],
            ),
          ),
          // Action Buttons
          Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (onCollectChanda != null)
                IconButton(
                  icon: const Icon(
                    Icons.payments_outlined,
                    color: AppColors.primary,
                    size: 22,
                  ),
                  tooltip: 'চাঁদা গ্রহণ',
                  onPressed: onCollectChanda,
                ),
              if (onEdit != null)
                IconButton(
                  icon: const Icon(
                    Icons.edit_outlined,
                    color: AppColors.textSecondary,
                    size: 18,
                  ),
                  tooltip: 'সম্পাদনা',
                  onPressed: onEdit,
                ),
            ],
          ),
        ],
      ),
    );
  }
}
