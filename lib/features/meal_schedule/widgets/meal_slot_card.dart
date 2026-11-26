import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/widgets/card_shell.dart';
import '../models/meal_schedule_item.dart';

/// Card showing a meal slot (দুপুরের খাবার / রাতের খাবার) and volunteer provider details
class MealSlotCard extends StatelessWidget {
  final String mealType;
  final MealScheduleItem? item;
  final VoidCallback onAssign;
  final ValueChanged<String>? onStatusChanged;

  const MealSlotCard({
    super.key,
    required this.mealType,
    this.item,
    required this.onAssign,
    this.onStatusChanged,
  });

  @override
  Widget build(BuildContext context) {
    final isDelivered = item?.isDelivered ?? false;

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
                child: Icon(
                  mealType == 'দুপুর' ? Icons.wb_sunny_outlined : Icons.nights_stay_outlined,
                  color: AppColors.primary,
                  size: 22,
                ),
              ),
              const SizedBox(width: AppDimens.spacing12),
              Text(
                '$mealTypeের খাবার',
                style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold),
              ),
              const Spacer(),
              if (item != null)
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                  decoration: BoxDecoration(
                    color: isDelivered
                        ? AppColors.success.withOpacity(0.12)
                        : AppColors.warning.withOpacity(0.12),
                    borderRadius: BorderRadius.circular(4),
                    border: Border.all(
                      color: isDelivered ? AppColors.success : AppColors.warning,
                      width: 0.8,
                    ),
                  ),
                  child: Text(
                    isDelivered ? 'পৌঁছেছে' : 'অপেক্ষমাণ',
                    style: AppTypography.caption.copyWith(
                      color: isDelivered ? AppColors.success : AppColors.warning,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
            ],
          ),
          const SizedBox(height: AppDimens.spacing12),
          const Divider(),
          const SizedBox(height: AppDimens.spacing8),
          if (item == null)
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'এখনও কোনো জোগানদাতা নির্ধারিত হয়নি',
                  style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
                ),
                ElevatedButton.icon(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.primary,
                    foregroundColor: AppColors.onPrimary,
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSm)),
                  ),
                  icon: const Icon(Icons.add, size: 16),
                  label: const Text('নির্ধারণ করুন'),
                  onPressed: onAssign,
                ),
              ],
            )
          else ...[
            Row(
              children: [
                const Icon(Icons.person_pin, size: 18, color: AppColors.primary),
                const SizedBox(width: 6),
                Text(
                  'জোগানদাতা: ${item!.providerName}',
                  style: AppTypography.bodyMedium.copyWith(fontWeight: FontWeight.bold),
                ),
              ],
            ),
            const SizedBox(height: 4),
            Row(
              children: [
                const Icon(Icons.phone_outlined, size: 15, color: AppColors.textSecondary),
                const SizedBox(width: 6),
                Text(
                  BengaliNumberFormatter.toBengali(item!.providerMobile),
                  style: AppTypography.bodySmall.copyWith(color: AppColors.textSecondary),
                ),
                const Spacer(),
                Text(
                  'প্রাপক: ${item!.receiverStaffName}',
                  style: AppTypography.caption.copyWith(color: AppColors.gold, fontWeight: FontWeight.bold),
                ),
              ],
            ),
            const SizedBox(height: AppDimens.spacing12),
            Row(
              children: [
                OutlinedButton.icon(
                  icon: Icon(
                    isDelivered ? Icons.undo : Icons.check_circle,
                    size: 16,
                    color: isDelivered ? AppColors.textSecondary : AppColors.success,
                  ),
                  label: Text(
                    isDelivered ? 'অপেক্ষমাণ করুন' : 'খাবার পৌঁছেছে নিশ্চিত করুন',
                    style: TextStyle(
                      color: isDelivered ? AppColors.textSecondary : AppColors.success,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  onPressed: () {
                    onStatusChanged?.call(isDelivered ? 'scheduled' : 'delivered');
                  },
                ),
                const Spacer(),
                IconButton(
                  icon: const Icon(Icons.edit_outlined, size: 18),
                  tooltip: 'পরিবর্তন',
                  onPressed: onAssign,
                ),
              ],
            ),
          ],
        ],
      ),
    );
  }
}
