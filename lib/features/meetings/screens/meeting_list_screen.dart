import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../shared/components/empty_state_view.dart';
import '../../../shared/components/error_state_view.dart';
import '../../../shared/widgets/adaptive_scaffold.dart';
import '../dialogs/record_resolution_dialog.dart';
import '../dialogs/schedule_meeting_dialog.dart';
import '../models/meeting_model.dart';
import '../providers/meeting_provider.dart';
import '../widgets/meeting_card.dart';

/// Screen presenting Executive Committee Meetings & Resolutions (কমিটির সভা ও সিদ্ধান্ত)
class MeetingListScreen extends ConsumerWidget {
  const MeetingListScreen({super.key});

  void _showScheduleDialog(BuildContext context, WidgetRef ref) {
    showDialog(
      context: context,
      builder: (ctx) => ScheduleMeetingDialog(
        onSubmit: (newMeeting) => ref.read(meetingListProvider.notifier).scheduleMeeting(newMeeting),
      ),
    );
  }

  void _showResolutionDialog(BuildContext context, WidgetRef ref, MeetingModel meeting) {
    showDialog(
      context: context,
      builder: (ctx) => RecordResolutionDialog(
        meeting: meeting,
        onSave: (id, res) => ref.read(meetingListProvider.notifier).recordResolution(id, res),
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final meetingsAsync = ref.watch(meetingListProvider);
    final statusFilter = ref.watch(meetingStatusFilterProvider);

    return AdaptiveScaffold(
      title: 'কমিটির সভা ও রেজুলেশন',
      floatingActionButton: FloatingActionButton.extended(
        backgroundColor: AppColors.primary,
        foregroundColor: AppColors.onPrimary,
        icon: const Icon(Icons.add_circle_outline),
        label: const Text('সভা আহ্বান', style: TextStyle(fontWeight: FontWeight.bold)),
        onPressed: () => _showScheduleDialog(context, ref),
      ),
      body: Column(
        children: [
          // Filter Tabs (সকল সভা, আসন্ন, সম্পন্ন)
          Container(
            color: AppColors.surface,
            padding: const EdgeInsets.symmetric(horizontal: AppDimens.spacing16, vertical: 8),
            child: Row(
              children: [
                _buildFilterChip(ref, 'সকল সভা', 'all', statusFilter),
                const SizedBox(width: 8),
                _buildFilterChip(ref, 'আসন্ন সভা', 'upcoming', statusFilter),
                const SizedBox(width: 8),
                _buildFilterChip(ref, 'সম্পন্ন সভা', 'completed', statusFilter),
              ],
            ),
          ),
          Expanded(
            child: meetingsAsync.when(
              loading: () => const Center(child: CircularProgressIndicator(color: AppColors.primary)),
              error: (err, _) => ErrorStateView(
                title: 'তথ্য লোড করতে সমস্যা হয়েছে',
                message: err.toString(),
                onRetry: () => ref.read(meetingListProvider.notifier).loadMeetings(),
              ),
              data: (meetings) {
                if (meetings.isEmpty) {
                  return EmptyStateView(
                    title: 'কোনো সভার তথ্য নেই',
                    message: 'মসজিদ কমিটির প্রয়োজনীয় সভা ও রেজুলেশন রেকর্ড করতে নিচে চাপ দিন।',
                    actionLabel: 'সভা আহ্বান করুন',
                    onAction: () => _showScheduleDialog(context, ref),
                  );
                }

                return ListView.builder(
                  padding: const EdgeInsets.all(AppDimens.spacing16),
                  itemCount: meetings.length,
                  itemBuilder: (context, index) {
                    final meeting = meetings[index];
                    return Padding(
                      padding: const EdgeInsets.only(bottom: AppDimens.spacing12),
                      child: MeetingCard(
                        meeting: meeting,
                        onRecordResolution: () => _showResolutionDialog(context, ref, meeting),
                      ),
                    );
                  },
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFilterChip(WidgetRef ref, String label, String value, String current) {
    final isSelected = value == current;
    return ChoiceChip(
      label: Text(label),
      selected: isSelected,
      selectedColor: AppColors.primaryContainer,
      labelStyle: TextStyle(
        color: isSelected ? AppColors.primary : AppColors.textPrimary,
        fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
        fontSize: 12,
      ),
      onSelected: (selected) {
        if (selected) {
          ref.read(meetingStatusFilterProvider.notifier).state = value;
          ref.read(meetingListProvider.notifier).loadMeetings();
        }
      },
    );
  }
}
