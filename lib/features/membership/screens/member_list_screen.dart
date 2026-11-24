import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/utils/bengali_number_formatter.dart';
import '../../../../shared/components/empty_state_view.dart';
import '../../../../shared/components/error_state_view.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../../../shared/widgets/card_shell.dart';
import '../dialogs/add_member_dialog.dart';
import '../dialogs/edit_member_dialog.dart';
import '../models/member_model.dart';
import '../providers/member_provider.dart';
import '../widgets/member_card.dart';
import '../widgets/member_filter_sheet.dart';

/// Screen displaying the complete directory of mosque members (সদস্য তালিকা)
class MemberListScreen extends ConsumerWidget {
  const MemberListScreen({super.key});

  void _showFilterSheet(BuildContext context, WidgetRef ref) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (ctx) => MemberFilterSheet(
        currentRole: ref.read(memberRoleFilterProvider),
        mealVolunteerOnly: ref.read(memberMealVolunteerFilterProvider),
        onApply: (role, mealOnly) {
          ref.read(memberRoleFilterProvider.notifier).state = role;
          ref.read(memberMealVolunteerFilterProvider.notifier).state = mealOnly;
          ref.read(memberListProvider.notifier).loadMembers();
        },
      ),
    );
  }

  void _showAddDialog(BuildContext context, WidgetRef ref) {
    showDialog(
      context: context,
      builder: (ctx) => AddMemberDialog(
        onSubmit: (newMember) => ref.read(memberListProvider.notifier).addMember(newMember),
      ),
    );
  }

  void _showEditDialog(BuildContext context, WidgetRef ref, MemberModel member) {
    showDialog(
      context: context,
      builder: (ctx) => EditMemberDialog(
        member: member,
        onUpdate: (updated) => ref.read(memberListProvider.notifier).updateMember(updated),
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final membersAsync = ref.watch(memberListProvider);
    final search = ref.watch(memberSearchQueryProvider);

    return AdaptiveScaffold(
      title: 'মসজিদ সদস্য তালিকা',
      actions: [
        IconButton(
          icon: const Icon(Icons.filter_list),
          tooltip: 'ফিল্টার',
          onPressed: () => _showFilterSheet(context, ref),
        ),
      ],
      floatingActionButton: FloatingActionButton.extended(
        backgroundColor: AppColors.primary,
        foregroundColor: AppColors.onPrimary,
        icon: const Icon(Icons.person_add),
        label: const Text('নতুন সদস্য', style: TextStyle(fontWeight: FontWeight.bold)),
        onPressed: () => _showAddDialog(context, ref),
      ),
      body: Column(
        children: [
          // Search & Summary Header
          Container(
            padding: const EdgeInsets.symmetric(
              horizontal: AppDimens.spacing16,
              vertical: AppDimens.spacing12,
            ),
            color: AppColors.surface,
            child: Column(
              children: [
                TextField(
                  decoration: InputDecoration(
                    hintText: 'নাম, সদস্য কোড বা মোবাইল দিয়ে খুঁজুন...',
                    prefixIcon: const Icon(Icons.search, color: AppColors.primary),
                    suffixIcon: search.isNotEmpty
                        ? IconButton(
                            icon: const Icon(Icons.clear, size: 18),
                            onPressed: () {
                              ref.read(memberSearchQueryProvider.notifier).state = '';
                              ref.read(memberListProvider.notifier).loadMembers();
                            },
                          )
                        : null,
                    filled: true,
                    fillColor: AppColors.background,
                    contentPadding: const EdgeInsets.symmetric(
                      horizontal: AppDimens.spacing12,
                      vertical: AppDimens.spacing8,
                    ),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(AppDimens.radiusMd),
                      borderSide: BorderSide.none,
                    ),
                  ),
                  onChanged: (val) {
                    ref.read(memberSearchQueryProvider.notifier).state = val;
                    ref.read(memberListProvider.notifier).loadMembers();
                  },
                ),
              ],
            ),
          ),
          // Content
          Expanded(
            child: membersAsync.when(
              loading: () => const Center(
                child: CircularProgressIndicator(color: AppColors.primary),
              ),
              error: (err, _) => ErrorStateView(
                title: 'তথ্য লোড করতে সমস্যা হয়েছে',
                message: err.toString(),
                onRetry: () => ref.read(memberListProvider.notifier).loadMembers(),
              ),
              data: (members) {
                if (members.isEmpty) {
                  return EmptyStateView(
                    title: 'কোনো সদস্য পাওয়া যায়নি',
                    message: search.isNotEmpty
                        ? 'আপনার খোঁজা তথ্যের সাথে কোনো সদস্য মেলেনি'
                        : 'এখনও কোনো সদস্য যুক্ত করা হয়নি। "নতুন সদস্য" বাটনে চাপ দিয়ে যুক্ত করুন।',
                    actionLabel: search.isNotEmpty ? 'ফিল্টার পরিষ্কার করুন' : 'সদস্য যুক্ত করুন',
                    onAction: search.isNotEmpty
                        ? () {
                            ref.read(memberSearchQueryProvider.notifier).state = '';
                            ref.read(memberRoleFilterProvider.notifier).state = 'all';
                            ref.read(memberMealVolunteerFilterProvider.notifier).state = false;
                            ref.read(memberListProvider.notifier).loadMembers();
                          }
                        : () => _showAddDialog(context, ref),
                  );
                }

                final totalChanda = members.fold<double>(0.0, (acc, m) => acc + m.monthlyChanda);
                final mealVolunteers = members.where((m) => m.willingToProvideStaffMeals).length;

                return ListView(
                  padding: const EdgeInsets.all(AppDimens.spacing16),
                  children: [
                    // Metrics Row
                    Row(
                      children: [
                        Expanded(
                          child: CardShell(
                            padding: const EdgeInsets.all(AppDimens.spacing12),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  'মোট সদস্য',
                                  style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
                                ),
                                const SizedBox(height: 2),
                                Text(
                                  '${BengaliNumberFormatter.toBengali(members.length)} জন',
                                  style: AppTypography.titleMedium.copyWith(
                                    fontWeight: FontWeight.bold,
                                    color: AppColors.primary,
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ),
                        const SizedBox(width: AppDimens.spacing8),
                        Expanded(
                          child: CardShell(
                            padding: const EdgeInsets.all(AppDimens.spacing12),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  'মাসিক চাঁদা অঙ্গীকার',
                                  style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
                                ),
                                const SizedBox(height: 2),
                                Text(
                                  BengaliNumberFormatter.formatCurrency(totalChanda),
                                  style: AppTypography.titleMedium.copyWith(
                                    fontWeight: FontWeight.bold,
                                    color: AppColors.gold,
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ),
                        const SizedBox(width: AppDimens.spacing8),
                        Expanded(
                          child: CardShell(
                            padding: const EdgeInsets.all(AppDimens.spacing12),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  'খাবার জোগানদাতা',
                                  style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
                                ),
                                const SizedBox(height: 2),
                                Text(
                                  '${BengaliNumberFormatter.toBengali(mealVolunteers)} জন',
                                  style: AppTypography.titleMedium.copyWith(
                                    fontWeight: FontWeight.bold,
                                    color: AppColors.success,
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: AppDimens.spacing16),
                    // Member Cards
                    ...members.map(
                      (member) => Padding(
                        padding: const EdgeInsets.only(bottom: AppDimens.spacing12),
                        child: MemberCard(
                          member: member,
                          onTap: () {
                            context.push('/member-profile', extra: member);
                          },
                          onEdit: () => _showEditDialog(context, ref, member),
                          onCollectChanda: () {
                            context.push('/chanda-ledger', extra: member);
                          },
                        ),
                      ),
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
