import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/utils/bengali_number_formatter.dart';
import '../../madrasa/models/student_model.dart';
import '../../madrasa/providers/madrasa_provider.dart';
import '../models/hifz_record_model.dart';
import '../widgets/star_rating_bar.dart';

/// Modal dialog for logging a daily Hifz evaluation session
class LogHifzSessionDialog extends ConsumerStatefulWidget {
  final StudentModel? preselectedStudent;
  final Function(HifzRecordModel) onSubmit;

  const LogHifzSessionDialog({
    super.key,
    this.preselectedStudent,
    required this.onSubmit,
  });

  @override
  ConsumerState<LogHifzSessionDialog> createState() => _LogHifzSessionDialogState();
}

class _LogHifzSessionDialogState extends ConsumerState<LogHifzSessionDialog> {
  final _formKey = GlobalKey<FormState>();

  StudentModel? _selectedStudent;
  int _selectedPara = 1;
  final _surahController = TextEditingController(text: 'সূরা আল-ফাতিহা');
  final _pageController = TextEditingController(text: '১');
  final _remarksController = TextEditingController();

  int _sabaqRating = 5;
  int _sabaqiRating = 5;
  int _manzilRating = 5;

  @override
  void initState() {
    super.initState();
    _selectedStudent = widget.preselectedStudent;
  }

  @override
  void dispose() {
    _surahController.dispose();
    _pageController.dispose();
    _remarksController.dispose();
    super.dispose();
  }

  void _submit() {
    if (_selectedStudent == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('অনুগ্রহ করে একজন শিক্ষার্থী নির্বাচন করুন')),
      );
      return;
    }

    if (!_formKey.currentState!.validate()) return;

    final now = DateTime.now();
    final dateStr =
        '${now.year}-${now.month.toString().padLeft(2, '0')}-${now.day.toString().padLeft(2, '0')}';

    final record = HifzRecordModel(
      id: 'hifz-${DateTime.now().millisecondsSinceEpoch}',
      studentId: _selectedStudent!.id,
      studentNameBn: _selectedStudent!.nameBn,
      paraNumber: _selectedPara,
      surahName: _surahController.text.trim(),
      pageNumber: int.tryParse(_pageController.text.trim()) ?? 1,
      sabaqRating: _sabaqRating,
      sabaqiRating: _sabaqiRating,
      manzilRating: _manzilRating,
      teacherRemarks: _remarksController.text.trim(),
      recordedDate: dateStr,
      createdAt: DateTime.now().millisecondsSinceEpoch,
    );

    widget.onSubmit(record);
    Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    final studentsAsync = ref.watch(studentListProvider);
    final allStudents = studentsAsync.valueOrNull ?? [];
    final hifzStudents = allStudents.where((s) => s.className.contains('হিফজ') || s.className.contains('নাজেরা')).toList();

    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusCard)),
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 520, maxHeight: 660),
        child: Padding(
          padding: const EdgeInsets.all(AppDimens.spacing20),
          child: Form(
            key: _formKey,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Row(
                      children: [
                        Container(
                          padding: const EdgeInsets.all(8),
                          decoration: BoxDecoration(
                            color: AppColors.goldDeep.withOpacity(0.12),
                            shape: BoxShape.circle,
                          ),
                          child: const Icon(Icons.auto_stories, color: AppColors.goldDeep, size: 22),
                        ),
                        const SizedBox(width: 10),
                        const Text(
                          'হিফজ ছবক ও পাঠ মূল্যায়ন',
                          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                        ),
                      ],
                    ),
                    IconButton(
                      icon: const Icon(Icons.close),
                      onPressed: () => Navigator.of(context).pop(),
                    ),
                  ],
                ),
                const Divider(height: 20),

                Expanded(
                  child: SingleChildScrollView(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        // Student Selector
                        const Text('শিক্ষার্থী নির্বাচন *', style: TextStyle(fontWeight: FontWeight.w600)),
                        const SizedBox(height: 6),
                        DropdownButtonFormField<StudentModel>(
                          value: _selectedStudent,
                          decoration: InputDecoration(
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                            contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                          ),
                          hint: const Text('শিক্ষার্থী বেছে নিন'),
                          items: hifzStudents.map((s) {
                            return DropdownMenuItem(
                              value: s,
                              child: Text('${s.nameBn} (${s.studentCode})'),
                            );
                          }).toList(),
                          onChanged: (val) {
                            if (val != null) setState(() => _selectedStudent = val);
                          },
                        ),
                        const SizedBox(height: 14),

                        // Para Selector & Page Row
                        Row(
                          children: [
                            Expanded(
                              flex: 2,
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  const Text('পারা নম্বর *', style: TextStyle(fontWeight: FontWeight.w600)),
                                  const SizedBox(height: 6),
                                  DropdownButtonFormField<int>(
                                    value: _selectedPara,
                                    decoration: InputDecoration(
                                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                                      contentPadding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
                                    ),
                                    items: List.generate(30, (i) => i + 1).map((p) {
                                      return DropdownMenuItem(
                                        value: p,
                                        child: Text('${BengaliNumberFormatter.toBengali(p.toString())} নং পারা'),
                                      );
                                    }).toList(),
                                    onChanged: (val) {
                                      if (val != null) setState(() => _selectedPara = val);
                                    },
                                  ),
                                ],
                              ),
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              flex: 1,
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  const Text('পৃষ্ঠা নং', style: TextStyle(fontWeight: FontWeight.w600)),
                                  const SizedBox(height: 6),
                                  TextFormField(
                                    controller: _pageController,
                                    keyboardType: TextInputType.number,
                                    decoration: InputDecoration(
                                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                                      contentPadding: const EdgeInsets.symmetric(horizontal: 10, vertical: 10),
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 14),

                        // Surah Name
                        TextFormField(
                          controller: _surahController,
                          decoration: InputDecoration(
                            labelText: 'সূরা / পাঠ্যাংশ *',
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                            prefixIcon: const Icon(Icons.bookmark_border),
                          ),
                          validator: (v) => (v == null || v.trim().isEmpty) ? 'সূরার নাম আবশ্যক' : null,
                        ),
                        const SizedBox(height: 16),

                        // 3-Way Islamic Hifz Evaluation (Sabaq, Sabaqi, Manzil)
                        Container(
                          padding: const EdgeInsets.all(12),
                          decoration: BoxDecoration(
                            color: Colors.grey.withOpacity(0.06),
                            borderRadius: BorderRadius.circular(AppDimens.radiusSmall),
                            border: Border.all(color: Colors.grey.shade300),
                          ),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              const Text('ত্রিপক্ষীয় হিফজ মূল্যায়ন', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                              const SizedBox(height: 10),

                              // Sabaq
                              Row(
                                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                children: [
                                  const Text('১. ছবক (নতুন পড়া):', style: TextStyle(fontWeight: FontWeight.w500)),
                                  StarRatingBar(
                                    rating: _sabaqRating,
                                    size: 24,
                                    onRatingChanged: (r) => setState(() => _sabaqRating = r),
                                  ),
                                ],
                              ),
                              const SizedBox(height: 8),

                              // Sabaqi
                              Row(
                                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                children: [
                                  const Text('২. ছবকী (চলতি পড়া):', style: TextStyle(fontWeight: FontWeight.w500)),
                                  StarRatingBar(
                                    rating: _sabaqiRating,
                                    size: 24,
                                    onRatingChanged: (r) => setState(() => _sabaqiRating = r),
                                  ),
                                ],
                              ),
                              const SizedBox(height: 8),

                              // Manzil / Amokhta
                              Row(
                                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                children: [
                                  const Text('৩. আমোখতা / মঞ্জিল:', style: TextStyle(fontWeight: FontWeight.w500)),
                                  StarRatingBar(
                                    rating: _manzilRating,
                                    size: 24,
                                    onRatingChanged: (r) => setState(() => _manzilRating = r),
                                  ),
                                ],
                              ),
                            ],
                          ),
                        ),
                        const SizedBox(height: 14),

                        // Teacher Remarks
                        TextFormField(
                          controller: _remarksController,
                          maxLines: 2,
                          decoration: InputDecoration(
                            labelText: 'শিক্ষকের মন্তব্য ও দিকনির্দেশনা',
                            hintText: 'উচ্চারণ, তাজবীদ ও পড়ার মান সম্পর্কে মন্তব্য...',
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 16),

                ElevatedButton(
                  onPressed: _submit,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.goldDeep,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 14),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusButton)),
                  ),
                  child: const Text('মূল্যায়ন সংরক্ষণ করুন', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
