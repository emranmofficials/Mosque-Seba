import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/utils/bengali_number_formatter.dart';
import '../../../../shared/components/empty_state_view.dart';
import '../../../../shared/widgets/card_shell.dart';
import '../models/exam_model.dart';
import '../providers/exam_provider.dart';
import 'student_report_card_screen.dart';

/// Screen listing Madrasa Examinations & Academic Results (পরীক্ষা ও ফলাফল তালিকা)
class ExamListScreen extends ConsumerWidget {
  const ExamListScreen({super.key});

  void _showCreateExamDialog(BuildContext context, WidgetRef ref) {
    final titleController = TextEditingController();
    String selectedType = 'annual';

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('নতুন পরীক্ষা শিডিউল করুন'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: titleController,
              decoration: const InputDecoration(
                labelText: 'পরীক্ষার নাম *',
                hintText: 'যেমন: বার্ষিক পরীক্ষা ২০২৬',
              ),
            ),
            const SizedBox(height: 12),
            DropdownButtonFormField<String>(
              value: selectedType,
              items: const [
                DropdownMenuItem(value: 'quarterly', child: Text('১ম সাময়িক পরীক্ষা')),
                DropdownMenuItem(value: 'half_yearly', child: Text('অর্ধবার্ষিক পরীক্ষা')),
                DropdownMenuItem(value: 'annual', child: Text('বার্ষিক পরীক্ষা')),
                DropdownMenuItem(value: 'hifz_eval', child: Text('হিফজ বিশেষ মূল্যায়ন')),
              ],
              onChanged: (v) {
                if (v != null) selectedType = v;
              },
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(),
            child: const Text('বাতিল'),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: AppColors.emeraldPrimary),
            onPressed: () {
              if (titleController.text.trim().isEmpty) return;
              final exam = ExamModel(
                id: 'ex-${DateTime.now().millisecondsSinceEpoch}',
                examNameBn: titleController.text.trim(),
                examType: selectedType,
                academicYear: '২০২৬',
                startDate: '2026-11-25',
                status: 'published',
                createdAt: DateTime.now().millisecondsSinceEpoch,
              );
              ref.read(examListProvider.notifier).createExam(exam);
              Navigator.of(ctx).pop();
            },
            child: const Text('সংরক্ষণ করুন', style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final examsAsync = ref.watch(examListProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('পরীক্ষা ও শিক্ষাবর্ষ মূল্যায়ন'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () => ref.read(examListProvider.notifier).loadExams(),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        backgroundColor: Colors.purple.shade700,
        foregroundColor: Colors.white,
        icon: const Icon(Icons.add_task),
        label: const Text('নতুন পরীক্ষা', style: TextStyle(fontWeight: FontWeight.bold)),
        onPressed: () => _showCreateExamDialog(context, ref),
      ),
      body: examsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator(color: AppColors.emeraldPrimary)),
        error: (err, _) => Center(child: Text('ত্রুটি: $err')),
        data: (exams) {
          if (exams.isEmpty) {
            return EmptyStateView(
              title: 'কোনো পরীক্ষা শিডিউল করা নেই',
              message: 'শিক্ষার্থীদের বার্ষিক বা সাময়িক পরীক্ষার তথ্য যুক্ত করতে চাপ দিন।',
              actionLabel: 'পরীক্ষা শিডিউল করুন',
              onAction: () => _showCreateExamDialog(context, ref),
            );
          }

          return ListView.builder(
            padding: const EdgeInsets.all(AppDimens.spacing14),
            itemCount: exams.length,
            itemBuilder: (context, index) {
              final exam = exams[index];
              return Padding(
                padding: const EdgeInsets.only(bottom: AppDimens.spacing12),
                child: CardShell(
                  onTap: () {
                    Navigator.of(context).push(
                      MaterialPageRoute(
                        builder: (_) => StudentReportCardScreen(
                          studentId: 'std-001',
                          studentNameBn: 'মুহাম্মদ তালহা জুবায়ের',
                          examTitle: exam.examNameBn,
                        ),
                      ),
                    );
                  },
                  padding: const EdgeInsets.all(AppDimens.spacing14),
                  child: Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: Colors.purple.shade50,
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Icon(Icons.auto_stories, color: Colors.purple.shade700, size: 28),
                      ),
                      const SizedBox(width: 14),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              exam.examNameBn,
                              style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                            ),
                            const SizedBox(height: 4),
                            Text(
                              'শিক্ষাবর্ষ: ${BengaliNumberFormatter.toBengali(exam.academicYear)} • তারিখ: ${BengaliNumberFormatter.toBengali(exam.startDate)}',
                              style: TextStyle(fontSize: 12, color: Colors.grey.shade600),
                            ),
                          ],
                        ),
                      ),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                        decoration: BoxDecoration(
                          color: AppColors.emeraldPrimary.withOpacity(0.12),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: const Text(
                          'প্রকাশিত',
                          style: TextStyle(
                            fontSize: 11,
                            fontWeight: FontWeight.bold,
                            color: AppColors.emeraldPrimary,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              );
            },
          );
        },
      ),
    );
  }
}
