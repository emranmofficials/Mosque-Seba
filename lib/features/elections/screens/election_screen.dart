import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/utils/bengali_number_formatter.dart';
import '../../../../shared/components/empty_state_view.dart';
import '../../../../shared/components/error_state_view.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../membership/providers/member_provider.dart';
import '../dialogs/add_election_schedule_dialog.dart';
import '../dialogs/nominate_candidate_dialog.dart';
import '../dialogs/record_vote_dialog.dart';
import '../models/election_candidate_model.dart';
import '../providers/election_provider.dart';
import '../widgets/candidate_card.dart';
import '../widgets/election_schedule_card.dart';

/// Screen managing Mosque Committee Elections, Nomination, Voters, and Gazette (কমিটি নির্বাচন ও পুনর্গঠন)
class ElectionScreen extends ConsumerStatefulWidget {
  const ElectionScreen({super.key});

  @override
  ConsumerState<ElectionScreen> createState() => _ElectionScreenState();
}

class _ElectionScreenState extends ConsumerState<ElectionScreen> with SingleTickerProviderStateMixin {
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 4, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  void _showAddScheduleDialog() {
    showDialog(
      context: context,
      builder: (ctx) => AddElectionScheduleDialog(
        onSubmit: (newSchedule) => ref.read(electionSchedulesProvider.notifier).createSchedule(newSchedule),
      ),
    );
  }

  void _showNominateDialog(String scheduleId) {
    showDialog(
      context: context,
      builder: (ctx) => NominateCandidateDialog(
        scheduleId: scheduleId,
        onSubmit: (newCandidate) => ref.read(electionCandidatesProvider.notifier).nominate(newCandidate),
      ),
    );
  }

  void _showRecordVoteDialog(ElectionCandidateModel candidate) {
    showDialog(
      context: context,
      builder: (ctx) => RecordVoteDialog(
        candidate: candidate,
        onSubmit: (id, votes, isElected) =>
            ref.read(electionCandidatesProvider.notifier).recordVotes(id, votes, isElected),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final activeSchedule = ref.watch(activeElectionScheduleProvider);
    final candidatesAsync = ref.watch(electionCandidatesProvider);
    final membersAsync = ref.watch(memberListProvider);
    final activeMembers = membersAsync.asData?.value.where((m) => m.isActive).toList() ?? [];

    return AdaptiveScaffold(
      title: 'কমিটি নির্বাচন ও পুনর্গঠন',
      floatingActionButton: activeSchedule == null
          ? FloatingActionButton.extended(
              backgroundColor: AppColors.primary,
              foregroundColor: AppColors.onPrimary,
              icon: const Icon(Icons.add_task),
              label: const Text('তফসিল ঘোষণা', style: TextStyle(fontWeight: FontWeight.bold)),
              onPressed: _showAddScheduleDialog,
            )
          : FloatingActionButton.extended(
              backgroundColor: AppColors.primary,
              foregroundColor: AppColors.onPrimary,
              icon: const Icon(Icons.person_add),
              label: const Text('মনোনয়ন দাখিল', style: TextStyle(fontWeight: FontWeight.bold)),
              onPressed: () => _showNominateDialog(activeSchedule.id),
            ),
      body: Column(
        children: [
          Container(
            color: AppColors.surface,
            child: TabBar(
              controller: _tabController,
              labelColor: AppColors.primary,
              unselectedLabelColor: AppColors.textSecondary,
              indicatorColor: AppColors.primary,
              indicatorWeight: 3,
              labelStyle: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
              tabs: const [
                Tab(icon: Icon(Icons.event_note, size: 18), text: 'তফসিল'),
                Tab(icon: Icon(Icons.how_to_vote, size: 18), text: 'মনোনয়ন'),
                Tab(icon: Icon(Icons.people_alt_outlined, size: 18), text: 'ভোটার'),
                Tab(icon: Icon(Icons.workspace_premium_outlined, size: 18), text: 'গেজেট'),
              ],
            ),
          ),
          Expanded(
            child: TabBarView(
              controller: _tabController,
              children: [
                // 1. Schedule Tab
                ListView(
                  padding: const EdgeInsets.all(AppDimens.spacing16),
                  children: [
                    if (activeSchedule != null)
                      ElectionScheduleCard(schedule: activeSchedule)
                    else
                      EmptyStateView(
                        title: 'কোনো সক্রিয় নির্বাচনী তফসিল নেই',
                        message: 'নতুন সেশনের পরিচালনা কমিটি গঠনের জন্য তফসিল ঘোষণা করুন।',
                        actionLabel: 'তফসিল ঘোষণা করুন',
                        onAction: _showAddScheduleDialog,
                      ),
                  ],
                ),
                // 2. Candidates Tab
                candidatesAsync.when(
                  loading: () => const Center(child: CircularProgressIndicator(color: AppColors.primary)),
                  error: (err, _) => ErrorStateView(
                    title: 'প্রার্থী লোড করতে সমস্যা',
                    message: err.toString(),
                  ),
                  data: (candidates) {
                    if (candidates.isEmpty) {
                      return EmptyStateView(
                        title: 'এখনও কোনো প্রার্থী দাখিল করেননি',
                        message: 'সভাপতি, সম্পাদক বা কোষাধ্যক্ষ পদের জন্য প্রার্থীর মনোনয়ন দাখিল করুন।',
                        actionLabel: activeSchedule != null ? 'মনোনয়ন দাখিল করুন' : null,
                        onAction: activeSchedule != null ? () => _showNominateDialog(activeSchedule.id) : null,
                      );
                    }
                    return ListView.separated(
                      padding: const EdgeInsets.all(AppDimens.spacing16),
                      itemCount: candidates.length,
                      separatorBuilder: (_, __) => const SizedBox(height: 8),
                      itemBuilder: (ctx, idx) => CandidateCard(
                        candidate: candidates[idx],
                        onRecordVote: () => _showRecordVoteDialog(candidates[idx]),
                      ),
                    );
                  },
                ),
                // 3. Voters Tab
                ListView(
                  padding: const EdgeInsets.all(AppDimens.spacing16),
                  children: [
                    Text(
                      'যোগ্য ভোটার তালিকা (${BengaliNumberFormatter.toBengali(activeMembers.length)} জন মুসল্লি)',
                      style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 8),
                    ...activeMembers.map((m) {
                      return ListTile(
                        contentPadding: EdgeInsets.zero,
                        leading: CircleAvatar(
                          backgroundColor: AppColors.primaryContainer,
                          child: Text(m.nameBn.characters.first, style: const TextStyle(fontWeight: FontWeight.bold)),
                        ),
                        title: Text(m.nameBn, style: AppTypography.bodyMedium.copyWith(fontWeight: FontWeight.bold)),
                        subtitle: Text('কোড: ${BengaliNumberFormatter.toBengali(m.memberCode)} • মোবাইল: ${BengaliNumberFormatter.toBengali(m.mobile)}'),
                        trailing: const Icon(Icons.check_circle, color: AppColors.success, size: 20),
                      );
                    }),
                  ],
                ),
                // 4. Gazette & Results Tab
                candidatesAsync.when(
                  loading: () => const Center(child: CircularProgressIndicator(color: AppColors.primary)),
                  error: (_, __) => const SizedBox.shrink(),
                  data: (candidates) {
                    final elected = candidates.where((c) => c.isElected).toList();
                    if (elected.isEmpty) {
                      return const EmptyStateView(
                        title: 'চূড়ান্ত ফলাফল ও গেজেট এখনও প্রকাশিত হয়নি',
                        message: 'ভোট গণনা সমাপ্ত হলে ফলাফল এখানে অফিসিয়াল গেজেট আকারে প্রদর্শিত হবে।',
                      );
                    }
                    return ListView(
                      padding: const EdgeInsets.all(AppDimens.spacing16),
                      children: [
                        Text(
                          'অফিসিয়াল নির্বাচনী ফলাফল ও গেজেট',
                          style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold, color: AppColors.primary),
                        ),
                        const SizedBox(height: 12),
                        ...elected.map((c) => Padding(
                              padding: const EdgeInsets.only(bottom: 8),
                              child: CandidateCard(
                                candidate: c,
                                onRecordVote: () => _showRecordVoteDialog(c),
                              ),
                            )),
                      ],
                    );
                  },
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
