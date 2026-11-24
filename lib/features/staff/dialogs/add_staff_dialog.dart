import 'package:flutter/material.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../shared/widgets/app_button.dart';
import '../../../../shared/widgets/app_text_field.dart';
import '../models/staff_model.dart';

/// Modal dialog to recruit or register a new mosque staff member
class AddStaffDialog extends StatefulWidget {
  final Future<bool> Function(StaffModel staff) onSubmit;

  const AddStaffDialog({super.key, required this.onSubmit});

  @override
  State<AddStaffDialog> createState() => _AddStaffDialogState();
}

class _AddStaffDialogState extends State<AddStaffDialog> {
  final _formKey = GlobalKey<FormState>();

  final _nameBnController = TextEditingController();
  final _mobileController = TextEditingController();
  final _salaryController = TextEditingController();
  String _selectedRoleCode = 'imam';
  String _selectedDesignation = 'পেশ ইমাম';
  bool _mealRequired = true;
  bool _isLoading = false;

  final Map<String, String> _designationMap = {
    'imam': 'পেশ ইমাম',
    'muazzin': 'মুয়াজ্জিন',
    'khadem': 'খাদেম',
    'teacher': 'হিফজ শিক্ষক',
    'other': 'অফিস সহকারী',
  };

  @override
  void dispose() {
    _nameBnController.dispose();
    _mobileController.dispose();
    _salaryController.dispose();
    super.dispose();
  }

  Future<void> _handleSave() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    final salary = double.tryParse(_salaryController.text.trim()) ?? 0.0;
    final staff = StaffModel(
      id: '',
      nameBn: _nameBnController.text.trim(),
      roleCode: _selectedRoleCode,
      designationBn: _selectedDesignation,
      salaryAmount: salary,
      mobile: _mobileController.text.trim(),
      mealRequired: _mealRequired,
      joinDate: DateTime.now().toIso8601String().split('T').first,
    );

    final ok = await widget.onSubmit(staff);

    if (mounted) {
      setState(() => _isLoading = false);
      if (ok) {
        Navigator.of(context).pop();
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('নতুন স্টাফ সফলভাবে নিযুক্ত হয়েছেন')),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('স্টাফ সংরক্ষণ করতে ব্যর্থ হয়েছে')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusLg)),
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 480),
        child: Padding(
          padding: const EdgeInsets.all(AppDimens.spacing20),
          child: SingleChildScrollView(
            child: Form(
              key: _formKey,
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        'নতুন স্টাফ নিয়োগ ও নিবন্ধন',
                        style: AppTypography.titleLarge.copyWith(fontWeight: FontWeight.bold),
                      ),
                      IconButton(
                        icon: const Icon(Icons.close),
                        onPressed: () => Navigator.of(context).pop(),
                      ),
                    ],
                  ),
                  const Divider(),
                  const SizedBox(height: AppDimens.spacing12),
                  AppTextField(
                    label: 'স্টাফের পূর্ণ নাম (বাংলায়) *',
                    hint: 'যেমন: মাওলানা আব্দুল্লাহ',
                    controller: _nameBnController,
                    validator: (v) => v == null || v.trim().isEmpty ? 'নাম আবশ্যক' : null,
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  Text('দায়িত্ব ও পদবি *', style: AppTypography.bodySmall.copyWith(fontWeight: FontWeight.w600)),
                  const SizedBox(height: 4),
                  DropdownButtonFormField<String>(
                    value: _selectedRoleCode,
                    decoration: InputDecoration(
                      filled: true,
                      fillColor: AppColors.surface,
                      contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusMd)),
                    ),
                    items: _designationMap.entries.map((e) {
                      return DropdownMenuItem(value: e.key, child: Text(e.value));
                    }).toList(),
                    onChanged: (val) {
                      if (val != null) {
                        setState(() {
                          _selectedRoleCode = val;
                          _selectedDesignation = _designationMap[val] ?? 'স্টাফ';
                        });
                      }
                    },
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  Row(
                    children: [
                      Expanded(
                        child: AppTextField(
                          label: 'মোবাইল নম্বর *',
                          hint: '০১৭১XXXXXXXX',
                          keyboardType: TextInputType.phone,
                          controller: _mobileController,
                          validator: (v) => v == null || v.trim().isEmpty ? 'মোবাইল আবশ্যক' : null,
                        ),
                      ),
                      const SizedBox(width: AppDimens.spacing12),
                      Expanded(
                        child: AppTextField(
                          label: 'মাসিক নির্ধারিত বেতন (৳) *',
                          hint: '15000',
                          keyboardType: TextInputType.number,
                          controller: _salaryController,
                          validator: (v) => v == null || v.trim().isEmpty ? 'বেতন আবশ্যক' : null,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  CheckboxListTile(
                    contentPadding: EdgeInsets.zero,
                    activeColor: AppColors.primary,
                    title: const Text('মসজিদ হতে খাবার ব্যবস্থা প্রযোজ্য'),
                    value: _mealRequired,
                    onChanged: (v) => setState(() => _mealRequired = v ?? true),
                  ),
                  const SizedBox(height: AppDimens.spacing20),
                  Row(
                    children: [
                      Expanded(
                        child: AppButton(
                          text: 'বাতিল',
                          variant: AppButtonVariant.outline,
                          onPressed: () => Navigator.of(context).pop(),
                        ),
                      ),
                      const SizedBox(width: AppDimens.spacing12),
                      Expanded(
                        child: AppButton(
                          text: 'নিয়োগ সংরক্ষণ',
                          isLoading: _isLoading,
                          onPressed: _handleSave,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
