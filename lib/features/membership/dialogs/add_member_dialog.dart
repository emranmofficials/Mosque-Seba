import 'dart:math';
import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/widgets/app_button.dart';
import '../../../shared/widgets/app_text_field.dart';
import '../../../shared/widgets/image_picker_field.dart';
import '../../role_permissions/models/committee_role.dart';
import '../models/member_model.dart';

/// Modal dialog to register a new mosque member
class AddMemberDialog extends StatefulWidget {
  final Future<bool> Function(MemberModel member) onSubmit;

  const AddMemberDialog({super.key, required this.onSubmit});

  @override
  State<AddMemberDialog> createState() => _AddMemberDialogState();
}

class _AddMemberDialogState extends State<AddMemberDialog> {
  final _formKey = GlobalKey<FormState>();

  final _nameBnController = TextEditingController();
  final _mobileController = TextEditingController();
  final _villageAreaController = TextEditingController();
  final _monthlyChandaController = TextEditingController(text: '100');
  final _nidController = TextEditingController();

  String _selectedRole = CommitteeRole.generalMember.code;
  bool _willingToProvideMeal = false;
  bool _isLoading = false;
  String? _photoUrl;

  @override
  void dispose() {
    _nameBnController.dispose();
    _mobileController.dispose();
    _villageAreaController.dispose();
    _monthlyChandaController.dispose();
    _nidController.dispose();
    super.dispose();
  }

  Future<void> _handleSave() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    final random = Random();
    // 8-digit random numeric member code (e.g. 84521903)
    final autoCode = (10000000 + random.nextInt(90000000)).toString();
    final chanda = double.tryParse(
          BengaliNumberFormatter.toEnglishDigits(_monthlyChandaController.text.trim()),
        ) ??
        0.0;

    final newMember = MemberModel(
      id: '',
      memberCode: autoCode,
      nameBn: _nameBnController.text.trim(),
      mobile: _mobileController.text.trim(),
      villageArea: _villageAreaController.text.trim(),
      monthlyChanda: chanda,
      nid: _nidController.text.trim(),
      roleCode: _selectedRole,
      willingToProvideStaffMeals: _willingToProvideMeal,
      photoUrl: _photoUrl,
      joinDate: DateTime.now().toIso8601String().split('T').first,
    );

    final success = await widget.onSubmit(newMember);

    if (mounted) {
      setState(() => _isLoading = false);
      if (success) {
        Navigator.of(context).pop();
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('নতুন সদস্য সফলভাবে যুক্ত হয়েছে')),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('সদস্য সংরক্ষণ করতে ব্যর্থ হয়েছে')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(AppDimens.radiusLg),
      ),
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
                        'নতুন সদস্য নিবন্ধন',
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
                  ImagePickerField(
                    label: 'সদস্যের ছবি (ঐচ্ছিক)',
                    subtitle: 'সদস্য কার্ডের জন্য ছবি নির্বাচন করুন',
                    folder: 'members',
                    isCircle: true,
                    initialUrl: _photoUrl,
                    onImageUploaded: (url) => setState(() => _photoUrl = url),
                    onImageRemoved: () => setState(() => _photoUrl = null),
                  ),
                  const SizedBox(height: AppDimens.spacing16),
                  AppTextField(
                    label: 'পূর্ণ নাম (বাংলায়) *',
                    hint: 'যেমন: হাজী মো: রফিকুল ইসলাম',
                    controller: _nameBnController,
                    validator: (v) => v == null || v.trim().isEmpty ? 'নাম আবশ্যক' : null,
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  AppTextField(
                    label: 'মোবাইল নম্বর *',
                    hint: '০১৭১XXXXXXXX',
                    keyboardType: TextInputType.phone,
                    controller: _mobileController,
                    validator: (v) {
                      if (v == null || v.trim().isEmpty) return 'মোবাইল নম্বর আবশ্যক';
                      if (v.trim().length < 11) return 'সঠিক ১১ ডিজিটের মোবাইল নম্বর দিন';
                      return null;
                    },
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  Row(
                    children: [
                      Expanded(
                        child: AppTextField(
                          label: 'গ্রাম / পাড়া',
                          hint: 'যেমন: উত্তরপাড়া',
                          controller: _villageAreaController,
                        ),
                      ),
                      const SizedBox(width: AppDimens.spacing12),
                      Expanded(
                        child: AppTextField(
                          label: 'মাসিক চাঁদা (৳)',
                          hint: '100',
                          keyboardType: TextInputType.number,
                          controller: _monthlyChandaController,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  AppTextField(
                    label: 'জাতীয় পরিচয়পত্র (NID) নম্বর',
                    hint: 'ঐচ্ছিক',
                    keyboardType: TextInputType.number,
                    controller: _nidController,
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  DropdownButtonFormField<String>(
                    value: _selectedRole,
                    isExpanded: true,
                    decoration: const InputDecoration(
                      labelText: 'কমিটির পদবি / ধরন',
                      border: OutlineInputBorder(),
                    ),
                    items: CommitteeRole.values
                        .map(
                          (r) => DropdownMenuItem<String>(
                            value: r.code,
                            child: Text(r.titleBn),
                          ),
                        )
                        .toList(),
                    onChanged: (val) {
                      if (val != null) {
                        setState(() => _selectedRole = val);
                      }
                    },
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  CheckboxListTile(
                    contentPadding: EdgeInsets.zero,
                    activeColor: AppColors.primary,
                    title: const Text('ইমাম/মুয়াজ্জিনের খাবার জোগানে ইচ্ছুক'),
                    value: _willingToProvideMeal,
                    onChanged: (v) => setState(() => _willingToProvideMeal = v ?? false),
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
                          label: 'সংরক্ষণ করুন',
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
