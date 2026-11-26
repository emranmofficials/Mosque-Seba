import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../authentication/providers/auth_provider.dart';
import '../../madrasa/models/student_model.dart';
import '../../madrasa/providers/madrasa_provider.dart';
import '../models/exam_model.dart';
import '../providers/exam_provider.dart';

/// Dialog for entering subject-wise student marks into an examination.
class EnterExamMarksDialog extends ConsumerStatefulWidget {
  final ExamModel exam;
  final StudentModel? initialStudent;

  const EnterExamMarksDialog({
    super.key,
    required this.exam,
    this.initialStudent,
  });

  @override
  ConsumerState<EnterExamMarksDialog> createState() =>
      _EnterExamMarksDialogState();
}

class _EnterExamMarksDialogState extends ConsumerState<EnterExamMarksDialog> {
  final _formKey = GlobalKey<FormState>();
  final _subjectController =
      TextEditingController(text: 'কুরআন তিলাওয়াত ও তাজবীদ');
  final _obtainedController = TextEditingController();
  final _totalController = TextEditingController(text: '100');
  final _remarksController = TextEditingController();

  StudentModel? _selectedStudent;
  bool _saving = false;

  @override
  void initState() {
    super.initState();
    _selectedStudent = widget.initialStudent;
  }

  @override
  void dispose() {
    _subjectController.dispose();
    _obtainedController.dispose();
    _totalController.dispose();
    _remarksController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!(_formKey.currentState?.validate() ?? false)) return;
    if (_selectedStudent == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('অনুগ্রহ করে একজন শিক্ষার্থী নির্বাচন করুন')),
      );
      return;
    }

    final obtained =
        BengaliNumberFormatter.parseDouble(_obtainedController.text.trim());
    final parsedTotal =
        BengaliNumberFormatter.parseDouble(_totalController.text.trim());
    final total = parsedTotal > 0 ? parsedTotal : 100.0;

    if (total <= 0 || obtained < 0 || obtained > total) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('প্রাপ্ত নম্বর ০ থেকে মোট নম্বরের মধ্যে হতে হবে'),
          backgroundColor: Colors.red,
        ),
      );
      return;
    }

    setState(() => _saving = true);
    final mosqueId =
        ref.read(authNotifierProvider).valueOrNull?.mosqueId ?? 'mosque-default';
    final grade = ExamResultModel.calculateIslamicGrade(obtained, total);

    final result = ExamResultModel(
      id: '',
      examId: widget.exam.id,
      studentId: _selectedStudent!.id,
      studentNameBn: _selectedStudent!.nameBn,
      className: _selectedStudent!.className,
      subjectName: _subjectController.text.trim(),
      marksObtained: obtained,
      totalMarks: total,
      grade: grade,
      remarks: _remarksController.text.trim(),
      createdAt: DateTime.now().millisecondsSinceEpoch,
    );

    final saved = await ref.read(examRepositoryProvider).recordResult(
          mosqueId: mosqueId,
          result: result,
        );

    if (!mounted) return;
    setState(() => _saving = false);

    if (saved != null) {
      ref.invalidate(examResultsProvider(widget.exam.id));
      ref.invalidate(studentReportCardProvider(_selectedStudent!.id));
      Navigator.of(context).pop(true);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            '${_selectedStudent!.nameBn}-এর নম্বর সংরক্ষিত হয়েছে ($grade)',
          ),
          backgroundColor: AppColors.emeraldPrimary,
        ),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('নম্বর সংরক্ষণ করতে ব্যর্থ হয়েছে'),
          backgroundColor: Colors.red,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final students = ref.watch(studentListProvider).valueOrNull ?? [];
    if (_selectedStudent == null && students.isNotEmpty) {
      _selectedStudent = students.first;
    }

    return AlertDialog(
      title: Text(
        'নম্বর এন্ট্রি — ${widget.exam.examNameBn}',
        style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
      ),
      content: SizedBox(
        width: double.maxFinite,
        child: Form(
          key: _formKey,
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                if (students.isEmpty)
                  const Padding(
                    padding: EdgeInsets.all(8.0),
                    child: Text(
                      'প্রথমে মাদরাসা ডিরেক্টরিতে শিক্ষার্থী ভর্তি করুন।',
                      style: TextStyle(color: Colors.red, fontSize: 12),
                    ),
                  )
                else
                  DropdownButtonFormField<StudentModel>(
                    value: students.contains(_selectedStudent)
                        ? _selectedStudent
                        : students.first,
                    isExpanded: true,
                    decoration: const InputDecoration(
                      labelText: 'শিক্ষার্থী *',
                      border: OutlineInputBorder(),
                    ),
                    items: students
                        .map(
                          (s) => DropdownMenuItem(
                            value: s,
                            child: Text('${s.nameBn} (${s.className})'),
                          ),
                        )
                        .toList(),
                    onChanged: (val) => setState(() => _selectedStudent = val),
                  ),
                const SizedBox(height: 12),
                TextFormField(
                  controller: _subjectController,
                  decoration: const InputDecoration(
                    labelText: 'বিষয়ের নাম *',
                    border: OutlineInputBorder(),
                  ),
                  validator: (v) =>
                      (v == null || v.trim().isEmpty) ? 'বিষয় লিখুন' : null,
                ),
                const SizedBox(height: 12),
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: _obtainedController,
                        keyboardType: TextInputType.number,
                        decoration: const InputDecoration(
                          labelText: 'প্রাপ্ত নম্বর *',
                          border: OutlineInputBorder(),
                        ),
                        validator: (v) => (v == null || v.trim().isEmpty)
                            ? 'নম্বর দিন'
                            : null,
                      ),
                    ),
                    const SizedBox(width: 10),
                    Expanded(
                      child: TextFormField(
                        controller: _totalController,
                        keyboardType: TextInputType.number,
                        decoration: const InputDecoration(
                          labelText: 'মোট নম্বর',
                          border: OutlineInputBorder(),
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                TextFormField(
                  controller: _remarksController,
                  decoration: const InputDecoration(
                    labelText: 'শিক্ষকের মন্তব্য (ঐচ্ছিক)',
                    border: OutlineInputBorder(),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: _saving ? null : () => Navigator.of(context).pop(false),
          child: const Text('বাতিল'),
        ),
        ElevatedButton(
          onPressed: (_saving || students.isEmpty) ? null : _submit,
          style: ElevatedButton.styleFrom(
            backgroundColor: AppColors.emeraldPrimary,
            foregroundColor: Colors.white,
          ),
          child: _saving
              ? const SizedBox(
                  width: 16,
                  height: 16,
                  child: CircularProgressIndicator(
                    strokeWidth: 2,
                    color: Colors.white,
                  ),
                )
              : const Text('নম্বর সংরক্ষণ'),
        ),
      ],
    );
  }
}
