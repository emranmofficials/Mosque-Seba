import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../shared/components/error_state_view.dart';
import '../../../shared/widgets/adaptive_scaffold.dart';
import '../../staff/providers/staff_provider.dart';
import '../dialogs/assign_duty_dialog.dart';
import '../models/duty_roster_item.dart';
import '../providers/duty_roster_provider.dart';
import '../widgets/roster_waqt_card.dart';

/// Screen presenting the Weekly Waqt Duty Roster for Imams, Muazzins, and Khadems (ওয়াক্তভিত্তিক দায়িত্ব রোস্টার)
class DutyRosterScreen extends ConsumerWidget {
  const DutyRosterScreen({super.key});

  final List<String> _daysOfWeek = const [
    'শুক্রবার',
    'শনিবার',
    'রবিবার',
    'সোমবার',
    'মঙ্গলবার',
    'বুধবার',
    'বৃহস্পতিবার',
  ];

  final List<String> _waqts = const [
    'ফজর',
    'যোহর',
    'আসর',
    'মাগরিব',
    'ইশা',
    'জুমা',
  ];

  void _showAssignDialog(BuildContext context, WidgetRef ref, String waqt, String day) {
    final staffList = ref.read(staffListProvider).asData?.value ?? [];

    showDialog(
      context: context,
      builder: (ctx) => AssignDutyDialog(
        dayOfWeek: day,
        waqtName: waqt,
        staffList: staffList,
        onAssign: (item) => ref.read(dutyRosterListProvider.notifier).assignDuty(item),
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final rosterAsync = ref.watch(dutyRosterListProvider);
    final selectedDay = ref.watch(selectedRosterDayProvider);

    return AdaptiveScaffold(
      title: 'ওয়াক্তভিত্তিক দায়িত্ব রোস্টার',
      body: Column(
        children: [
          // 7-day selector strip
          Container(
            height: 52,
            color: AppColors.surface,
            child: ListView.separated(
              padding: const EdgeInsets.symmetric(horizontal: AppDimens.spacing16, vertical: 8),
              scrollDirection: Axis.horizontal,
              itemCount: _daysOfWeek.length,
              separatorBuilder: (_, __) => const SizedBox(width: 8),
              itemBuilder: (context, index) {
                final day = _daysOfWeek[index];
                final isSelected = day == selectedDay;
                return ChoiceChip(
                  label: Text(day),
                  selected: isSelected,
                  selectedColor: AppColors.primaryContainer,
                  labelStyle: TextStyle(
                    color: isSelected ? AppColors.primary : AppColors.textPrimary,
                    fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                    fontSize: 13,
                  ),
                  onSelected: (selected) {
                    if (selected) {
                      ref.read(selectedRosterDayProvider.notifier).state = day;
                      ref.read(dutyRosterListProvider.notifier).loadRoster();
                    }
                  },
                );
              },
            ),
          ),
          // Roster waqt cards
          Expanded(
            child: rosterAsync.when(
              loading: () => const Center(child: CircularProgressIndicator(color: AppColors.primary)),
              error: (err, _) => ErrorStateView(
                title: 'তথ্য লোড করতে সমস্যা হয়েছে',
                message: err.toString(),
                onRetry: () => ref.read(dutyRosterListProvider.notifier).loadRoster(),
              ),
              data: (rosterItems) {
                final activeWaqts = selectedDay == 'শুক্রবার'
                    ? _waqts
                    : _waqts.where((w) => w != 'জুমা').toList();

                return ListView(
                  padding: const EdgeInsets.all(AppDimens.spacing16),
                  children: activeWaqts.map((waqt) {
                    final assignments = rosterItems.where((r) => r.prayerName == waqt).toList();
                    return Padding(
                      padding: const EdgeInsets.only(bottom: AppDimens.spacing12),
                      child: RosterWaqtCard(
                        waqtName: waqt,
                        assignments: assignments,
                        onAssign: () => _showAssignDialog(context, ref, waqt, selectedDay),
                      ),
                    );
                  }).toList(),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
