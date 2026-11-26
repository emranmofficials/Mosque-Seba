import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/components/empty_state_view.dart';
import '../../../shared/widgets/card_shell.dart';
import '../../madrasa/models/student_model.dart';
import '../../madrasa/providers/madrasa_provider.dart';
import '../dialogs/enter_exam_marks_dialog.dart';
import '../models/exam_model.dart';
import '../providers/exam_provider.dart';
import 'student_report_card_screen.dart';

/// Screen listing Madrasa Examinations, Marks Entry, & Academic Results
class ExamListScreen extends ConsumerWidget {
  const ExamListScreen({super.key});

  void _showCreateExamDialog(BuildContext context, WidgetRef ref) {
    showDialog<void>(
      context: context,
      builder: (ctx) => _CreateExamDialog(
        onSave: (exam) => ref.read(examListProvider.notifier).createExam(exam),
      ),
    );
  }

  void _openExamStudentsSheet(
    BuildContext context,
    WidgetRef ref,
    ExamModel exam,
  ) {
    showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (ctx) => _ExamStudentsSheet(exam: exam),
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
        label: const Text(
          'নতুন পরীক্ষা',
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        onPressed: () => _showCreateExamDialog(context, ref),
      ),
      body: examsAsync.when(
        loading: () => const Center(
          child: CircularProgressIndicator(color: AppColors.emeraldPrimary),
        ),
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
                  onTap: () => _openExamStudentsSheet(context, ref, exam),
                  padding: const EdgeInsets.all(AppDimens.spacing14),
                  child: Column(
                    children: [
                      Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.all(12),
                            decoration: BoxDecoration(
                              color: Colors.purple.shade50,
                              borderRadius: BorderRadius.circular(12),
                            ),
                            child: Icon(
                              Icons.auto_stories,
                              color: Colors.purple.shade700,
                              size: 28,
                            ),
                          ),
                          const SizedBox(width: 14),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  exam.examNameBn,
                                  style: const TextStyle(
                                    fontSize: 16,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                                const SizedBox(height: 4),
                                Text(
                                  'শিক্ষাবর্ষ: ${BengaliNumberFormatter.toBengali(exam.academicYear)} • তারিখ: ${BengaliNumberFormatter.toBengali(exam.startDate)}',
                                  style: TextStyle(
                                    fontSize: 12,
                                    color: Colors.grey.shade600,
                                  ),
                                ),
                              ],
                            ),
                          ),
                          Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 8,
                              vertical: 4,
                            ),
                            decoration: BoxDecoration(
                              color: AppColors.emeraldPrimary.withOpacity(0.12),
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: const Text(
                              'নম্বর ও ফলাফল',
                              style: TextStyle(
                                fontSize: 11,
                                fontWeight: FontWeight.bold,
                                color: AppColors.emeraldPrimary,
                              ),
                            ),
                          ),
                        ],
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

class _ExamStudentsSheet extends ConsumerWidget {
  final ExamModel exam;

  const _ExamStudentsSheet({required this.exam});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final students = ref.watch(studentListProvider).valueOrNull ?? <StudentModel>[];
    final resultsAsync = ref.watch(examResultsProvider(exam.id));

    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: SizedBox(
          height: MediaQuery.of(context).size.height * 0.65,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Expanded(
                    child: Text(
                      '${exam.examNameBn} — ফলাফল ও নম্বর',
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                  ElevatedButton.icon(
                    onPressed: () {
                      showDialog<void>(
                        context: context,
                        builder: (_) => EnterExamMarksDialog(exam: exam),
                      );
                    },
                    icon: const Icon(Icons.edit_note, size: 16),
                    label: const Text('নম্বর দিন', style: TextStyle(fontSize: 12)),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.emeraldPrimary,
                      foregroundColor: Colors.white,
                    ),
                  ),
                ],
              ),
              const Divider(height: 20),
              Expanded(
                child: resultsAsync.when(
                  loading: () => const Center(child: CircularProgressIndicator()),
                  error: (e, _) => Center(child: Text('ত্রুটি: $e')),
                  data: (results) {
                    if (students.isEmpty && results.isEmpty) {
                      return const Center(
                        child: Text(
                          'প্রথমে মাদরাসা ডিরেক্টরিতে শিক্ষার্থী যুক্ত করুন, তারপর নম্বর এন্ট্রি দিন।',
                          textAlign: TextAlign.center,
                        ),
                      );
                    }

                    // Build unified list of students from roster + recorded results
                    final seenIds = <String>{};
                    final entries = <Map<String, String>>[];
                    for (final s in students) {
                      seenIds.add(s.id);
                      final count =
                          results.where((r) => r.studentId == s.id).length;
                      entries.add({
                        'id': s.id,
                        'name': s.nameBn,
                        'className': s.className,
                        'subtitle':
                            '${s.className} • ${BengaliNumberFormatter.toBengali(count)} টি বিষয় এন্ট্রি',
                      });
                    }
                    for (final r in results) {
                      if (seenIds.add(r.studentId)) {
                        entries.add({
                          'id': r.studentId,
                          'name': r.studentNameBn,
                          'className': r.className,
                          'subtitle': '${r.className} • ${r.subjectName}: ${r.marksObtained}',
                        });
                      }
                    }

                    return ListView.separated(
                      itemCount: entries.length,
                      separatorBuilder: (_, __) => const Divider(height: 1),
                      itemBuilder: (ctx, i) {
                        final item = entries[i];
                        return ListTile(
                          leading: const CircleAvatar(
                            child: Icon(Icons.person_outline),
                          ),
                          title: Text(
                            item['name']!,
                            style: const TextStyle(fontWeight: FontWeight.bold),
                          ),
                          subtitle: Text(
                            item['subtitle']!,
                            style: const TextStyle(fontSize: 12),
                          ),
                          trailing: Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              IconButton(
                                icon: const Icon(Icons.add_circle_outline),
                                tooltip: 'নম্বর যোগ',
                                onPressed: () {
                                  final matches = students
                                      .where((s) => s.id == item['id']);
                                  final matched =
                                      matches.isNotEmpty ? matches.first : null;
                                  showDialog<void>(
                                    context: context,
                                    builder: (_) => EnterExamMarksDialog(
                                      exam: exam,
                                      initialStudent: matched,
                                    ),
                                  );
                                },
                              ),
                              const Icon(Icons.arrow_forward_ios, size: 14),
                            ],
                          ),
                          onTap: () {
                            Navigator.of(ctx).pop();
                            Navigator.of(context).push(
                              MaterialPageRoute(
                                builder: (_) => StudentReportCardScreen(
                                  studentId: item['id']!,
                                  studentNameBn: item['name']!,
                                  className: item['className']!,
                                  examTitle: exam.examNameBn,
                                  examId: exam.id,
                                ),
                              ),
                            );
                          },
                        );
                      },
                    );
                  },
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _CreateExamDialog extends StatefulWidget {
  final ValueChanged<ExamModel> onSave;

  const _CreateExamDialog({required this.onSave});

  @override
  State<_CreateExamDialog> createState() => _CreateExamDialogState();
}

class _CreateExamDialogState extends State<_CreateExamDialog> {
  final _titleController = TextEditingController();
  String _selectedType = 'annual';

  @override
  void dispose() {
    _titleController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('নতুন পরীক্ষা শিডিউল করুন'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          TextField(
            controller: _titleController,
            decoration: const InputDecoration(
              labelText: 'পরীক্ষার নাম *',
              hintText: 'যেমন: বার্ষিক পরীক্ষা ২০২৬',
            ),
          ),
          const SizedBox(height: 12),
          DropdownButtonFormField<String>(
            value: _selectedType,
            items: const [
              DropdownMenuItem(value: 'quarterly', child: Text('১ম সাময়িক পরীক্ষা')),
              DropdownMenuItem(value: 'half_yearly', child: Text('অর্ধবার্ষিক পরীক্ষা')),
              DropdownMenuItem(value: 'annual', child: Text('বার্ষিক পরীক্ষা')),
              DropdownMenuItem(value: 'hifz_eval', child: Text('হিফজ বিশেষ মূল্যায়ন')),
            ],
            onChanged: (v) {
              if (v != null) setState(() => _selectedType = v);
            },
          ),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('বাতিল'),
        ),
        ElevatedButton(
          style: ElevatedButton.styleFrom(backgroundColor: AppColors.emeraldPrimary),
          onPressed: () {
            if (_titleController.text.trim().isEmpty) return;
            final exam = ExamModel(
              id: '',
              examNameBn: _titleController.text.trim(),
              examType: _selectedType,
              academicYear: DateTime.now().year.toString(),
              startDate: DateTime.now().toIso8601String().split('T').first,
              status: 'published',
              createdAt: DateTime.now().millisecondsSinceEpoch,
            );
            widget.onSave(exam);
            Navigator.of(context).pop();
          },
          child: const Text('সংরক্ষণ করুন', style: TextStyle(color: Colors.white)),
        ),
      ],
    );
  }
}
