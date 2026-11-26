import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../models/welfare_case_model.dart';

/// Modal dialog for submitting a new Welfare or Zakat aid application
class AddWelfareCaseDialog extends StatefulWidget {
  final Function(WelfareCaseModel) onSubmit;

  const AddWelfareCaseDialog({super.key, required this.onSubmit});

  @override
  State<AddWelfareCaseDialog> createState() => _AddWelfareCaseDialogState();
}

class _AddWelfareCaseDialogState extends State<AddWelfareCaseDialog> {
  final _formKey = GlobalKey<FormState>();

  final _applicantNameController = TextEditingController();
  final _mobileController = TextEditingController();
  final _nidController = TextEditingController();
  final _addressController = TextEditingController();
  final _amountController = TextEditingController(text: '10000');
  final _notesController = TextEditingController();

  String _category = 'medical';
  String _fundType = 'zakat';

  final List<Map<String, String>> _categories = [
    {'code': 'medical', 'label': 'চিকিৎসা সহায়তা'},
    {'code': 'widow', 'label': 'বিধবা ও দুস্থ ভাতা'},
    {'code': 'orphan', 'label': 'এতিম ও শিক্ষা সহায়তা'},
    {'code': 'disaster', 'label': 'প্রাকৃতিক দুর্যোগ ত্রাণ'},
    {'code': 'debt_relief', 'label': 'ঋণগ্রস্তদের সাহায্য'},
  ];

  @override
  void dispose() {
    _applicantNameController.dispose();
    _mobileController.dispose();
    _nidController.dispose();
    _addressController.dispose();
    _amountController.dispose();
    _notesController.dispose();
    super.dispose();
  }

  void _submit() {
    if (!_formKey.currentState!.validate()) return;

    final now = DateTime.now();
    final dateStr =
        '${now.year}-${now.month.toString().padLeft(2, '0')}-${now.day.toString().padLeft(2, '0')}';
    final code = 'WLF-${now.year}-${(now.millisecondsSinceEpoch % 1000).toString().padLeft(3, '0')}';

    final welfareCase = WelfareCaseModel(
      id: 'wlf-${DateTime.now().millisecondsSinceEpoch}',
      caseCode: code,
      applicantName: _applicantNameController.text.trim(),
      applicantMobile: _mobileController.text.trim(),
      applicantNid: _nidController.text.trim(),
      address: _addressController.text.trim(),
      category: _category,
      fundType: _fundType,
      requestedAmount: double.tryParse(_amountController.text.trim()) ?? 0.0,
      status: 'pending',
      notes: _notesController.text.trim(),
      applicationDate: dateStr,
      createdAt: DateTime.now().millisecondsSinceEpoch,
    );

    widget.onSubmit(welfareCase);
    Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusCard)),
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 500, maxHeight: 660),
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
                          child: const Icon(Icons.volunteer_activism, color: AppColors.emeraldPrimary, size: 22),
                        ),
                        const SizedBox(width: 10),
                        const Text(
                          'যাকাত ও সমাজকল্যাণ আবেদন',
                          style: TextStyle(fontSize: 17, fontWeight: FontWeight.bold),
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
                        // Category & Fund Type Row
                        Row(
                          children: [
                            Expanded(
                              flex: 3,
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  const Text('সাহায্যের খাত *', style: TextStyle(fontWeight: FontWeight.w600)),
                                  const SizedBox(height: 6),
                                  DropdownButtonFormField<String>(
                                    value: _category,
                                    decoration: InputDecoration(
                                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                                      contentPadding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
                                    ),
                                    items: _categories
                                        .map((c) => DropdownMenuItem(value: c['code']!, child: Text(c['label']!)))
                                        .toList(),
                                    onChanged: (val) {
                                      if (val != null) setState(() => _category = val);
                                    },
                                  ),
                                ],
                              ),
                            ),
                            const SizedBox(width: 10),
                            Expanded(
                              flex: 2,
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  const Text('তহবিল', style: TextStyle(fontWeight: FontWeight.w600)),
                                  const SizedBox(height: 6),
                                  DropdownButtonFormField<String>(
                                    value: _fundType,
                                    decoration: InputDecoration(
                                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                                      contentPadding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
                                    ),
                                    items: const [
                                      DropdownMenuItem(value: 'zakat', child: Text('যাকাত')),
                                      DropdownMenuItem(value: 'sadakah', child: Text('সদকা')),
                                      DropdownMenuItem(value: 'general_welfare', child: Text('সাধারণ কল্যাণ')),
                                    ],
                                    onChanged: (val) {
                                      if (val != null) setState(() => _fundType = val);
                                    },
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 14),

                        // Applicant Name
                        TextFormField(
                          controller: _applicantNameController,
                          decoration: InputDecoration(
                            labelText: 'আবেদনকারীর নাম *',
                            hintText: 'সাহায্যপ্রার্থী ব্যক্তির পূর্ণ নাম',
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                            prefixIcon: const Icon(Icons.person_outline),
                          ),
                          validator: (v) => (v == null || v.trim().isEmpty) ? 'নাম আবশ্যক' : null,
                        ),
                        const SizedBox(height: 14),

                        // Mobile & NID
                        Row(
                          children: [
                            Expanded(
                              child: TextFormField(
                                controller: _mobileController,
                                keyboardType: TextInputType.phone,
                                decoration: InputDecoration(
                                  labelText: 'মোবাইল নম্বর *',
                                  hintText: '01XXXXXXXXX',
                                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                                  prefixIcon: const Icon(Icons.phone_outlined),
                                ),
                                validator: (v) => (v == null || v.trim().isEmpty) ? 'মোবাইল আবশ্যক' : null,
                              ),
                            ),
                            const SizedBox(width: 10),
                            Expanded(
                              child: TextFormField(
                                controller: _nidController,
                                keyboardType: TextInputType.number,
                                decoration: InputDecoration(
                                  labelText: 'জাতীয় পরিচয়পত্র (NID)',
                                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                                  prefixIcon: const Icon(Icons.badge_outlined),
                                ),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 14),

                        // Address
                        TextFormField(
                          controller: _addressController,
                          decoration: InputDecoration(
                            labelText: 'বর্তমান ঠিকানা ও পাড়া/মহল্লা *',
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                            prefixIcon: const Icon(Icons.home_outlined),
                          ),
                          validator: (v) => (v == null || v.trim().isEmpty) ? 'ঠিকানা আবশ্যক' : null,
                        ),
                        const SizedBox(height: 14),

                        // Requested Amount
                        TextFormField(
                          controller: _amountController,
                          keyboardType: TextInputType.number,
                          decoration: InputDecoration(
                            labelText: 'প্রার্থিত অনুদানের পরিমাণ (টাকা) *',
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                            prefixIcon: const Icon(Icons.currency_exchange),
                          ),
                          validator: (v) => (v == null || v.trim().isEmpty) ? 'টাকার পরিমাণ আবশ্যক' : null,
                        ),
                        const SizedBox(height: 14),

                        // Notes & Description
                        TextFormField(
                          controller: _notesController,
                          maxLines: 2,
                          decoration: InputDecoration(
                            labelText: 'আবেদনের কারণ ও পটভূমি',
                            hintText: 'দুস্থতা বা চিকিৎসার বিস্তারিত...',
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
                    backgroundColor: AppColors.emeraldPrimary,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 14),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusButton)),
                  ),
                  child: const Text('আবেদন জমা দিন', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
