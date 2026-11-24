import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../shared/components/empty_state_view.dart';
import '../../../../shared/components/error_state_view.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../authentication/providers/auth_provider.dart';
import '../dialogs/publish_notice_dialog.dart';
import '../models/notice_model.dart';
import '../providers/notice_provider.dart';
import '../widgets/notice_card.dart';

/// Screen presenting the Official Notice Board (ডিজিটাল নোটিশ বোর্ড)
class NoticeListScreen extends ConsumerWidget {
  const NoticeListScreen({super.key});

  final List<String> _priorityTabs = const [
    'সকল',
    'জরুরি',
    'গুরুত্বপূর্ণ',
    'সাধারণ',
    'পিন করা',
  ];

  void _showPublishDialog(BuildContext context, WidgetRef ref) {
    final authState = ref.read(authProvider);
    final author = authState.user?.nameBn ?? 'সাধারণ সম্পাদক';

    showDialog(
      context: context,
      builder: (ctx) => PublishNoticeDialog(
        authorName: author,
        onSubmit: (newNotice) => ref.read(noticeListProvider.notifier).publishNotice(newNotice),
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final noticesAsync = ref.watch(noticeListProvider);
    final currentPriority = ref.watch(noticePriorityFilterProvider);
    final isPinnedOnly = ref.watch(noticePinnedOnlyFilterProvider);

    return AdaptiveScaffold(
      title: 'মসজিদ ডিজিটাল নোটিশ বোর্ড',
      floatingActionButton: FloatingActionButton.extended(
        backgroundColor: AppColors.primary,
        foregroundColor: AppColors.onPrimary,
        icon: const Icon(Icons.campaign_outlined),
        label: const Text('নতুন নোটিশ', style: TextStyle(fontWeight: FontWeight.bold)),
        onPressed: () => _showPublishDialog(context, ref),
      ),
      body: Column(
        children: [
          // Filter Tabs
          Container(
            height: 48,
            color: AppColors.surface,
            child: ListView.separated(
              padding: const EdgeInsets.symmetric(horizontal: AppDimens.spacing16, vertical: 6),
              scrollDirection: Axis.horizontal,
              itemCount: _priorityTabs.length,
              separatorBuilder: (_, __) => const SizedBox(width: 8),
              itemBuilder: (context, index) {
                final tab = _priorityTabs[index];
                bool isSelected = false;
                if (tab == 'সকল' && currentPriority == 'all' && !isPinnedOnly) isSelected = true;
                if (tab == 'জরুরি' && currentPriority == 'urgent') isSelected = true;
                if (tab == 'গুরুত্বপূর্ণ' && currentPriority == 'important') isSelected = true;
                if (tab == 'সাধারণ' && currentPriority == 'normal') isSelected = true;
                if (tab == 'পিন করা' && isPinnedOnly) isSelected = true;

                return ChoiceChip(
                  label: Text(tab),
                  selected: isSelected,
                  selectedColor: AppColors.primaryContainer,
                  labelStyle: TextStyle(
                    color: isSelected ? AppColors.primary : AppColors.textPrimary,
                    fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                    fontSize: 13,
                  ),
                  onSelected: (selected) {
                    if (selected) {
                      if (tab == 'পিন করা') {
                        ref.read(noticePinnedOnlyFilterProvider.notifier).state = true;
                        ref.read(noticePriorityFilterProvider.notifier).state = 'all';
                      } else {
                        ref.read(noticePinnedOnlyFilterProvider.notifier).state = false;
                        if (tab == 'সকল') ref.read(noticePriorityFilterProvider.notifier).state = 'all';
                        if (tab == 'জরুরি') ref.read(noticePriorityFilterProvider.notifier).state = 'urgent';
                        if (tab == 'গুরুত্বপূর্ণ') ref.read(noticePriorityFilterProvider.notifier).state = 'important';
                        if (tab == 'সাধারণ') ref.read(noticePriorityFilterProvider.notifier).state = 'normal';
                      }
                      ref.read(noticeListProvider.notifier).loadNotices();
                    }
                  },
                );
              },
            ),
          ),
          // Notice Cards List
          Expanded(
            child: noticesAsync.when(
              loading: () => const Center(child: CircularProgressIndicator(color: AppColors.primary)),
              error: (err, _) => ErrorStateView(
                title: 'তথ্য লোড করতে সমস্যা হয়েছে',
                message: err.toString(),
                onRetry: () => ref.read(noticeListProvider.notifier).loadNotices(),
              ),
              data: (notices) {
                if (notices.isEmpty) {
                  return EmptyStateView(
                    title: 'কোনো নোটিশ পাওয়া যায়নি',
                    message: 'মসজিদের জরুরি নোটিশ ও গুরুত্বপূর্ণ ঘোষণা প্রকাশ করতে নিচে চাপ দিন।',
                    actionLabel: 'নোটিশ প্রকাশ করুন',
                    onAction: () => _showPublishDialog(context, ref),
                  );
                }

                return ListView.builder(
                  padding: const EdgeInsets.all(AppDimens.spacing16),
                  itemCount: notices.length,
                  itemBuilder: (context, index) {
                    final notice = notices[index];
                    return Padding(
                      padding: const EdgeInsets.only(bottom: AppDimens.spacing12),
                      child: NoticeCard(
                        notice: notice,
                        onPinToggle: () => ref.read(noticeListProvider.notifier).togglePin(notice.id, !notice.isPinned),
                        onDelete: () => ref.read(noticeListProvider.notifier).deleteNotice(notice.id),
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
}
