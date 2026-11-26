import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/components/error_state_view.dart';
import '../../../shared/widgets/adaptive_scaffold.dart';
import '../../../shared/widgets/card_shell.dart';
import '../../membership/providers/member_provider.dart';
import '../dialogs/add_meal_schedule_dialog.dart';
import '../models/meal_schedule_item.dart';
import '../providers/meal_schedule_provider.dart';
import '../widgets/meal_slot_card.dart';

/// Screen presenting Mosque Meal Schedules for Imams and Staff (ইমাম-মুয়াজ্জিনদের খাবার সূচি)
class MealScheduleScreen extends ConsumerWidget {
  const MealScheduleScreen({super.key});

  void _showAddDialog(
    BuildContext context,
    WidgetRef ref,
    String dateStr,
    String mealType, {
    MealScheduleItem? existingItem,
  }) {
    final members = ref.read(memberListProvider).asData?.value ?? [];
    final volunteers = members.where((m) => m.willingToProvideStaffMeals).toList();

    showDialog<void>(
      context: context,
      builder: (ctx) => AddMealScheduleDialog(
        dateStr: dateStr,
        defaultMealType: mealType,
        existingItem: existingItem,
        volunteers: volunteers,
        onSubmit: (item) => ref.read(mealScheduleListProvider.notifier).addSchedule(item),
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final selectedDate = ref.watch(selectedMealDateProvider);
    final schedulesAsync = ref.watch(mealScheduleListProvider);

    return AdaptiveScaffold(
      title: 'ইমামদের খাবার সূচি ও ব্যবস্থাপনা',
      body: Column(
        children: [
          // Date bar with Prev / Next day arrows
          Container(
            padding: const EdgeInsets.symmetric(horizontal: AppDimens.spacing16, vertical: AppDimens.spacing10),
            color: AppColors.surface,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                IconButton(
                  icon: const Icon(Icons.chevron_left),
                  onPressed: () {
                    final curr = DateTime.tryParse(selectedDate) ?? DateTime.now();
                    final prev = curr.subtract(const Duration(days: 1));
                    ref.read(selectedMealDateProvider.notifier).state = prev.toIso8601String().split('T').first;
                    ref.read(mealScheduleListProvider.notifier).loadSchedules();
                  },
                ),
                Row(
                  children: [
                    const Icon(Icons.calendar_today, color: AppColors.primary, size: 18),
                    const SizedBox(width: 8),
                    Text(
                      'তারিখ: ${BengaliNumberFormatter.toBengali(selectedDate)}',
                      style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold),
                    ),
                  ],
                ),
                IconButton(
                  icon: const Icon(Icons.chevron_right),
                  onPressed: () {
                    final curr = DateTime.tryParse(selectedDate) ?? DateTime.now();
                    final next = curr.plusDays(1);
                    ref.read(selectedMealDateProvider.notifier).state = next.toIso8601String().split('T').first;
                    ref.read(mealScheduleListProvider.notifier).loadSchedules();
                  },
                ),
              ],
            ),
          ),
          // Content
          Expanded(
            child: schedulesAsync.when(
              loading: () => const Center(child: CircularProgressIndicator(color: AppColors.primary)),
              error: (err, _) => ErrorStateView(
                title: 'তথ্য লোড করতে সমস্যা হয়েছে',
                message: err.toString(),
                onRetry: () => ref.read(mealScheduleListProvider.notifier).loadSchedules(),
              ),
              data: (schedules) {
                final lunchItem = schedules.cast<MealScheduleItem?>().firstWhere(
                      (s) => s?.mealType == 'দুপুর',
                      orElse: () => null,
                    );
                final dinnerItem = schedules.cast<MealScheduleItem?>().firstWhere(
                      (s) => s?.mealType == 'রাত',
                      orElse: () => null,
                    );

                return ListView(
                  padding: const EdgeInsets.all(AppDimens.spacing16),
                  children: [
                    // Overview Notice Card
                    CardShell(
                      padding: const EdgeInsets.all(AppDimens.spacing12),
                      child: Row(
                        children: [
                          const Icon(Icons.restaurant_menu, color: AppColors.primary, size: 24),
                          const SizedBox(width: AppDimens.spacing10),
                          Expanded(
                            child: Text(
                              'মসজিদের সম্মানীয় পেশ ইমাম, মুয়াজ্জিন ও খাদেমদের খাবার আয়োজন সুচারুভাবে পরিচালনার সূচি।',
                              style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
                            ),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: AppDimens.spacing16),
                    // Lunch Slot
                    MealSlotCard(
                      mealType: 'দুপুর',
                      item: lunchItem,
                      onAssign: () => _showAddDialog(
                        context,
                        ref,
                        selectedDate,
                        'দুপুর',
                        existingItem: lunchItem,
                      ),
                      onStatusChanged: (status) {
                        if (lunchItem != null) {
                          ref.read(mealScheduleListProvider.notifier).updateStatus(lunchItem.id, status);
                        }
                      },
                    ),
                    const SizedBox(height: AppDimens.spacing16),
                    // Dinner Slot
                    MealSlotCard(
                      mealType: 'রাত',
                      item: dinnerItem,
                      onAssign: () => _showAddDialog(
                        context,
                        ref,
                        selectedDate,
                        'রাত',
                        existingItem: dinnerItem,
                      ),
                      onStatusChanged: (status) {
                        if (dinnerItem != null) {
                          ref.read(mealScheduleListProvider.notifier).updateStatus(dinnerItem.id, status);
                        }
                      },
                    ),
                  ],
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}

extension on DateTime {
  DateTime plusDays(int days) => add(Duration(days: days));
}
