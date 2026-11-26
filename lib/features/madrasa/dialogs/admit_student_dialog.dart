import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../models/student_model.dart';

/// Modal Dialog for Admitting a New Madrasa Student (শিক্ষার্থী ভর্তি ফরম)
class AdmitStudentDialog extends StatefulWidget {
  final Function(StudentModel) onSubmit;

  const AdmitStudentDialog({super.key, required this.onSubmit});

  @override
  State<AdmitStudentDialog> createState() => _AdmitStudentDialogState();
}

class _AdmitStudentDialogState extends State<AdmitStudentDialog> {
  final _formKey = GlobalKey<FormState>();

  final _nameBnController = TextEditingController();
  final _nameEnController = TextEditingController();
  final _guardianNameController = TextEditingController();
  final _guardianMobileController = TextEditingController();
  final _guardianNidController = TextEditingController();
  final _monthlyFeeController = TextEditingController(text: '1200');

  String _selectedClass = 'নূরানী বিভাগ';
  String _selectedBloodGroup = 'A+';
  DateTime _admissionDate = DateTime.now();

  final List<String> _classes = [
    'নূরানী বিভাগ',
    'নাজেরা বিভাগ',
    'হিফজুল কুরআন',
    'কিতাব বিভাগ',
  ];

  final List<String> _bloodGroups = ['A+', 'A-', 'B+', 'B-', 'O+', 'O-', 'AB+', 'AB-'];

  @override
  void dispose() {
    _nameBnController.dispose();
    _nameEnController.dispose();
    _guardianNameController.dispose();
    _guardianMobileController.dispose();
    _guardianNidController.dispose();
    _monthlyFeeController.dispose();
    super.dispose();
  }

  void _submit() {
    if (!_formKey.currentState!.validate()) return;

    final dateStr =
        '${_admissionDate.year}-${_admissionDate.month.toString().padLeft(2, '0')}-${_admissionDate.day.toString().padLeft(2, '0')}';
    final randomCode =
        'MS-${_selectedClass.characters.first}-${DateTime.now().millisecondsSinceEpoch % 1000}';

    final student = StudentModel(
      id: 'std-${DateTime.now().millisecondsSinceEpoch}',
      studentCode: randomCode,
      nameBn: _nameBnController.text.trim(),
      nameEn: _nameEnController.text.trim(),
      className: _selectedClass,
      guardianName: _guardianNameController.text.trim(),
      guardianMobile: _guardianMobileController.text.trim(),
      guardianNid: _guardianNidController.text.trim(),
      monthlyFee: double.tryParse(_monthlyFeeController.text.trim()) ?? 0.0,
      admissionDate: dateStr,
      bloodGroup: _selectedBloodGroup,
      status: 'active',
      createdAt: DateTime.now().millisecondsSinceEpoch,
    );

    widget.onSubmit(student);
    Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusCard)),
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 500, maxHeight: 680),
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
                            color: AppColors.emeraldPrimary.withOpacity(0.12),
                            shape: BoxShape.circle,
                          ),
                          child: const Icon(Icons.school, color: AppColors.emeraldPrimary, size: 22),
                        ),
                        const SizedBox(width: 10),
                        const Text(
                          'নতুন শিক্ষার্থী ভর্তি ফরম',
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
                const Divider(height: 24),
                Expanded(
                  child: SingleChildScrollView(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        // Class Selector
                        const Text('শ্রেণী / বিভাগ *', style: TextStyle(fontWeight: FontWeight.w600)),
                        const SizedBox(height: 6),
                        DropdownButtonFormField<String>(
                          value: _selectedClass,
                          decoration: InputDecoration(
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                            contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                          ),
                          items: _classes
                              .map((c) => DropdownMenuItem(value: c, child: Text(c)))
                              .toList(),
                          onChanged: (val) {
                            if (val != null) setState(() => _selectedClass = val);
                          },
                        ),
                        const SizedBox(height: 14),

                        // Student Name (Bengali)
                        TextFormField(
                          controller: _nameBnController,
                          decoration: InputDecoration(
                            labelText: 'শিক্ষার্থীর নাম (বাংলা) *',
                            hintText: 'যেমন: মুহাম্মদ তালহা জুবায়ের',
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                            prefixIcon: const Icon(Icons.person_outline),
                          ),
                          validator: (v) => (v == null || v.trim().isEmpty) ? 'শিক্ষার্থীর নাম আবশ্যক' : null,
                        ),
                        const SizedBox(height: 14),

                        // Student Name (English)
                        TextFormField(
                          controller: _nameEnController,
                          decoration: InputDecoration(
                            labelText: 'শিক্ষার্থীর নাম (ইংরেজি)',
                            hintText: 'e.g. Muhammad Talha Zubair',
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                            prefixIcon: const Icon(Icons.badge_outlined),
                          ),
                        ),
                        const SizedBox(height: 14),

                        // Guardian Name
                        TextFormField(
                          controller: _guardianNameController,
                          decoration: InputDecoration(
                            labelText: 'অভিভাবকের নাম *',
                            hintText: 'পিতা বা অভিভাবকের নাম',
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                            prefixIcon: const Icon(Icons.family_restroom_outlined),
                          ),
                          validator: (v) => (v == null || v.trim().isEmpty) ? 'অভিভাবকের নাম আবশ্যক' : null,
                        ),
                        const SizedBox(height: 14),

                        // Guardian Mobile
                        TextFormField(
                          controller: _guardianMobileController,
                          keyboardType: TextInputType.phone,
                          decoration: InputDecoration(
                            labelText: 'অভিভাবকের মোবাইল নম্বর *',
                            hintText: '01XXXXXXXXX',
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                            prefixIcon: const Icon(Icons.phone_outlined),
                          ),
                          validator: (v) {
                            if (v == null || v.trim().isEmpty) return 'মোবাইল নম্বর আবশ্যক';
                            if (v.trim().length < 11) return 'সঠিক ১১ ডিজিটের মোবাইল নম্বর দিন';
                            return null;
                          },
                        ),
                        const SizedBox(height: 14),

                        // Monthly Fee & Blood Group Row
                        Row(
                          children: [
                            Expanded(
                              flex: 2,
                              child: TextFormField(
                                controller: _monthlyFeeController,
                                keyboardType: TextInputType.number,
                                decoration: InputDecoration(
                                  labelText: 'মাসিক ফি (টাকা)',
                                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                                  prefixIcon: const Icon(Icons.currency_exchange),
                                ),
                              ),
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              flex: 1,
                              child: DropdownButtonFormField<String>(
                                value: _selectedBloodGroup,
                                decoration: InputDecoration(
                                  labelText: 'রক্তের গ্রুপ',
                                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                                  contentPadding: const EdgeInsets.symmetric(horizontal: 10, vertical: 10),
                                ),
                                items: _bloodGroups
                                    .map((b) => DropdownMenuItem(value: b, child: Text(b)))
                                    .toList(),
                                onChanged: (val) {
                                  if (val != null) setState(() => _selectedBloodGroup = val);
                                },
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 14),

                        // Admission Date Picker
                        InkWell(
                          onTap: () async {
                            final picked = await showDatePicker(
                              context: context,
                              initialDate: _admissionDate,
                              firstDate: DateTime(2020),
                              lastDate: DateTime(2030),
                            );
                            if (!mounted) return;
                            if (picked != null) {
                              setState(() => _admissionDate = picked);
                            }
                          },
                          child: Container(
                            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                            decoration: BoxDecoration(
                              border: Border.all(color: Colors.grey.shade400),
                              borderRadius: BorderRadius.circular(AppDimens.radiusSmall),
                            ),
                            child: Row(
                              children: [
                                const Icon(Icons.calendar_month, color: AppColors.emeraldPrimary),
                                const SizedBox(width: 10),
                                Text(
                                  'ভর্তির তারিখ: ${BengaliNumberFormatter.toBengali('${_admissionDate.day}/${_admissionDate.month}/${_admissionDate.year}')}',
                                  style: const TextStyle(fontWeight: FontWeight.w600),
                                ),
                              ],
                            ),
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
                    backgroundColor: AppColors.emeraldPrimary,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 14),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusButton)),
                  ),
                  child: const Text('ভর্তি নিশ্চিত করুন', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
