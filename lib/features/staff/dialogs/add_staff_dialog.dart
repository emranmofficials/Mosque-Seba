import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/widgets/app_button.dart';
import '../../../shared/widgets/app_text_field.dart';
import '../models/staff_model.dart';

/// Modal dialog to recruit, register, or edit a mosque staff member
class AddStaffDialog extends StatefulWidget {
  final StaffModel? existingStaff;
  final Future<bool> Function(StaffModel staff) onSubmit;

  const AddStaffDialog({
    super.key,
    this.existingStaff,
    required this.onSubmit,
  });

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
  void initState() {
    super.initState();
    final existing = widget.existingStaff;
    if (existing != null) {
      _nameBnController.text = existing.nameBn;
      _mobileController.text = existing.mobile;
      _salaryController.text = existing.salaryAmount.toInt().toString();
      if (_designationMap.containsKey(existing.roleCode)) {
        _selectedRoleCode = existing.roleCode;
        _selectedDesignation = _designationMap[existing.roleCode]!;
      } else {
        _selectedRoleCode = 'other';
        _selectedDesignation = existing.designationBn;
      }
      _mealRequired = existing.mealRequired;
    }
  }

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

    final existing = widget.existingStaff;
    final salary = double.tryParse(
          BengaliNumberFormatter.toEnglishDigits(_salaryController.text.trim()),
        ) ??
        0.0;
    final staff = StaffModel(
      id: existing?.id ?? '',
      nameBn: _nameBnController.text.trim(),
      roleCode: _selectedRoleCode,
      designationBn: _selectedDesignation,
      employmentType: existing?.employmentType ?? 'full_time',
      salaryAmount: salary,
      mobile: _mobileController.text.trim(),
      mealRequired: _mealRequired,
      isActive: existing?.isActive ?? true,
      joinDate: existing?.joinDate.isNotEmpty == true
          ? existing!.joinDate
          : DateTime.now().toIso8601String().split('T').first,
    );

    final ok = await widget.onSubmit(staff);

    if (mounted) {
      setState(() => _isLoading = false);
      if (ok) {
        Navigator.of(context).pop();
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              existing != null
                  ? 'স্টাফের তথ্য সফলভাবে হালনাগাদ হয়েছে'
                  : 'নতুন স্টাফ সফলভাবে নিযুক্ত হয়েছেন',
            ),
          ),
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
                        widget.existingStaff != null
                            ? 'স্টাফের তথ্য সম্পাদনা'
                            : 'নতুন স্টাফ নিয়োগ ও নিবন্ধন',
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
                          label: 'বাতিল',
                          variant: AppButtonVariant.outline,
                          onPressed: () => Navigator.of(context).pop(),
                        ),
                      ),
                      const SizedBox(width: AppDimens.spacing12),
                      Expanded(
                        child: AppButton(
                          label: widget.existingStaff != null
                              ? 'হালনাগাদ করুন'
                              : 'নিয়োগ সংরক্ষণ',
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
