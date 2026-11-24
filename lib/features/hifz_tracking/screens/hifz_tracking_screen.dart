import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/utils/bengali_number_formatter.dart';
import '../../../../shared/components/empty_state_view.dart';
import '../../../../shared/widgets/card_shell.dart';
import '../dialogs/log_hifz_session_dialog.dart';
import '../models/hifz_record_model.dart';
import '../providers/hifz_provider.dart';
import '../widgets/star_rating_bar.dart';

/// Screen for tracking Quran & Hifz Progress (হিফজুল কুরআন ট্র্যাকিং স্ক্রিন)
class HifzTrackingScreen extends ConsumerWidget {
  const HifzTrackingScreen({super.key});

  void _showLogDialog(BuildContext context, WidgetRef ref) {
    showDialog(
      context: context,
      builder: (ctx) => LogHifzSessionDialog(
        onSubmit: (record) => ref.read(hifzLogProvider.notifier).logSession(record),
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final logsAsync = ref.watch(hifzLogProvider);

    return DefaultTabController(
      length: 2,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('হিফজুল কুরআন ট্র্যাকার'),
          bottom: const TabBar(
            indicatorColor: AppColors.goldDeep,
            labelColor: AppColors.goldDeep,
            unselectedLabelColor: Colors.grey,
            tabs: [
              Tab(icon: Icon(Icons.history_edu), text: 'দৈনিক মূল্যায়ন লগ'),
              Tab(icon: Icon(Icons.grid_on), text: '৩০ পারা মাইলফলক'),
            ],
          ),
          actions: [
            IconButton(
              icon: const Icon(Icons.refresh),
              onPressed: () => ref.read(hifzLogProvider.notifier).loadRecentLogs(),
            ),
          ],
        ),
        floatingActionButton: FloatingActionButton.extended(
          backgroundColor: AppColors.goldDeep,
          foregroundColor: Colors.white,
          icon: const Icon(Icons.add),
          label: const Text('নতুন ছবক এন্ট্রি', style: TextStyle(fontWeight: FontWeight.bold)),
          onPressed: () => _showLogDialog(context, ref),
        ),
        body: TabBarView(
          children: [
            // Tab 1: Daily Evaluation Logs
            logsAsync.when(
              loading: () => const Center(child: CircularProgressIndicator(color: AppColors.goldDeep)),
              error: (err, _) => Center(child: Text('ত্রুটি: $err')),
              data: (logs) {
                if (logs.isEmpty) {
                  return EmptyStateView(
                    title: 'কোনো ছবক মূল্যায়ন রেকর্ড নেই',
                    message: 'আজকের ছবক, ছবকী ও আমোখতা এন্ট্রি করতে নিচে চাপ দিন।',
                    actionLabel: 'ছবক এন্ট্রি করুন',
                    onAction: () => _showLogDialog(context, ref),
                  );
                }

                return ListView.builder(
                  padding: const EdgeInsets.all(AppDimens.spacing12),
                  itemCount: logs.length,
                  itemBuilder: (context, index) {
                    final log = logs[index];
                    return Padding(
                      padding: const EdgeInsets.only(bottom: AppDimens.spacing10),
                      child: _buildHifzLogCard(log),
                    );
                  },
                );
              },
            ),

            // Tab 2: 30 Paras Milestone Grid
            _buildParaMilestoneGrid(),
          ],
        ),
      ),
    );
  }

  Widget _buildHifzLogCard(HifzRecordModel log) {
    return CardShell(
      padding: const EdgeInsets.all(AppDimens.spacing14),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                log.studentNameBn,
                style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(
                  color: AppColors.goldDeep.withOpacity(0.12),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(
                  'পারা ${BengaliNumberFormatter.toBengali(log.paraNumber.toString())}',
                  style: const TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.bold,
                    color: AppColors.goldDeep,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 4),
          Text(
            'সূরা: ${log.surahName} (পৃষ্ঠা নং: ${BengaliNumberFormatter.toBengali(log.pageNumber.toString())})',
            style: TextStyle(fontSize: 13, color: Colors.grey.shade700),
          ),
          const SizedBox(height: 10),
          const Divider(height: 1),
          const SizedBox(height: 8),

          // 3-Row Star Ratings
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              _buildRatingColumn('ছবক', log.sabaqRating),
              _buildRatingColumn('ছবকী', log.sabaqiRating),
              _buildRatingColumn('আমোখতা', log.manzilRating),
            ],
          ),

          if (log.teacherRemarks.isNotEmpty) ...[
            const SizedBox(height: 8),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: Colors.grey.withOpacity(0.08),
                borderRadius: BorderRadius.circular(6),
              ),
              child: Text(
                'মন্তব্য: ${log.teacherRemarks}',
                style: const TextStyle(fontSize: 12, fontStyle: FontStyle.italic),
              ),
            ),
          ],
          const SizedBox(height: 6),
          Align(
            alignment: Alignment.centerRight,
            child: Text(
              'তারিখ: ${BengaliNumberFormatter.toBengali(log.recordedDate)}',
              style: TextStyle(fontSize: 11, color: Colors.grey.shade600),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildRatingColumn(String label, int rating) {
    return Column(
      children: [
        Text(label, style: const TextStyle(fontSize: 11, fontWeight: FontWeight.w600)),
        const SizedBox(height: 2),
        StarRatingBar(rating: rating, size: 16),
      ],
    );
  }

  Widget _buildParaMilestoneGrid() {
    return ListView(
      padding: const EdgeInsets.all(AppDimens.spacing16),
      children: [
        const Text(
          'কুরআনুল কারীমের ৩০ পারা অগ্রগতি নির্দেশক',
          style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 4),
        const Text(
          'হিফজ শিক্ষার্থীদের পারা সমাপ্তি ও রিভিশন ট্র্যাকিং সারসংক্ষেপ',
          style: TextStyle(fontSize: 12, color: Colors.grey),
        ),
        const SizedBox(height: 16),

        GridView.builder(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 5,
            crossAxisSpacing: 8,
            mainAxisSpacing: 8,
            childAspectRatio: 1.1,
          ),
          itemCount: 30,
          itemBuilder: (context, index) {
            final para = index + 1;
            final isCompleted = para <= 15; // Demonstration milestone state
            final isCurrent = para == 16;

            Color bgColor = Colors.grey.shade200;
            Color textColor = Colors.grey.shade800;
            if (isCompleted) {
              bgColor = AppColors.emeraldPrimary.withOpacity(0.15);
              textColor = AppColors.emeraldPrimary;
            } else if (isCurrent) {
              bgColor = AppColors.goldDeep.withOpacity(0.2);
              textColor = AppColors.goldDeep;
            }

            return Container(
              decoration: BoxDecoration(
                color: bgColor,
                borderRadius: BorderRadius.circular(8),
                border: Border.all(
                  color: isCurrent
                      ? AppColors.goldDeep
                      : (isCompleted ? AppColors.emeraldPrimary : Colors.transparent),
                  width: isCurrent ? 2 : 1,
                ),
              ),
              child: Center(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      BengaliNumberFormatter.toBengali(para.toString()),
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                        color: textColor,
                      ),
                    ),
                    Text(
                      isCompleted ? 'সমাপ্ত' : (isCurrent ? 'চলমান' : 'বাকি'),
                      style: TextStyle(
                        fontSize: 9,
                        color: textColor,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ],
                ),
              ),
            );
          },
        ),
      ],
    );
  }
}
