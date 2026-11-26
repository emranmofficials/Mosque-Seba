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

/// Modal dialog to edit an existing member's profile
class EditMemberDialog extends StatefulWidget {
  final MemberModel member;
  final Future<bool> Function(MemberModel member) onUpdate;

  const EditMemberDialog({
    super.key,
    required this.member,
    required this.onUpdate,
  });

  @override
  State<EditMemberDialog> createState() => _EditMemberDialogState();
}

class _EditMemberDialogState extends State<EditMemberDialog> {
  final _formKey = GlobalKey<FormState>();

  late TextEditingController _nameBnController;
  late TextEditingController _mobileController;
  late TextEditingController _villageAreaController;
  late TextEditingController _monthlyChandaController;
  late TextEditingController _nidController;
  late bool _willingToProvideMeal;
  late String _status;
  late String _selectedRole;
  bool _isLoading = false;
  String? _photoUrl;

  @override
  void initState() {
    super.initState();
    _photoUrl = widget.member.photoUrl;
    _nameBnController = TextEditingController(text: widget.member.nameBn);
    _mobileController = TextEditingController(text: widget.member.mobile);
    _villageAreaController = TextEditingController(text: widget.member.villageArea);
    _monthlyChandaController = TextEditingController(text: widget.member.monthlyChanda.toStringAsFixed(0));
    _nidController = TextEditingController(text: widget.member.nid);
    _willingToProvideMeal = widget.member.willingToProvideStaffMeals;
    _status = const ['active', 'inactive', 'deceased'].contains(widget.member.status)
        ? widget.member.status
        : 'active';
    _selectedRole = CommitteeRole.fromCode(widget.member.roleCode).code;
  }

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

    final chanda = double.tryParse(
          BengaliNumberFormatter.toEnglishDigits(_monthlyChandaController.text.trim()),
        ) ??
        0.0;
    final updated = widget.member.copyWith(
      nameBn: _nameBnController.text.trim(),
      mobile: _mobileController.text.trim(),
      villageArea: _villageAreaController.text.trim(),
      monthlyChanda: chanda,
      nid: _nidController.text.trim(),
      roleCode: _selectedRole,
      willingToProvideStaffMeals: _willingToProvideMeal,
      status: _status,
      photoUrl: _photoUrl,
    );

    final success = await widget.onUpdate(updated);

    if (mounted) {
      setState(() => _isLoading = false);
      if (success) {
        Navigator.of(context).pop();
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('সদস্যের তথ্য সফলভাবে হালনাগাদ হয়েছে')),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('হালনাগাদ করতে ব্যর্থ হয়েছে')),
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
                        'সদস্য তথ্য সম্পাদনা',
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
                    label: 'সদস্যের ছবি',
                    subtitle: 'সদস্য কার্ডের জন্য ছবি পরিবর্তন করুন',
                    folder: 'members',
                    isCircle: true,
                    initialUrl: _photoUrl,
                    onImageUploaded: (url) => setState(() => _photoUrl = url),
                    onImageRemoved: () => setState(() => _photoUrl = null),
                  ),
                  const SizedBox(height: AppDimens.spacing16),
                  AppTextField(
                    label: 'পূর্ণ নাম (বাংলায়) *',
                    controller: _nameBnController,
                    validator: (v) => v == null || v.trim().isEmpty ? 'নাম আবশ্যক' : null,
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  AppTextField(
                    label: 'মোবাইল নম্বর *',
                    keyboardType: TextInputType.phone,
                    controller: _mobileController,
                    validator: (v) => v == null || v.trim().isEmpty ? 'মোবাইল নম্বর আবশ্যক' : null,
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  Row(
                    children: [
                      Expanded(
                        child: AppTextField(
                          label: 'গ্রাম / পাড়া',
                          controller: _villageAreaController,
                        ),
                      ),
                      const SizedBox(width: AppDimens.spacing12),
                      Expanded(
                        child: AppTextField(
                          label: 'মাসিক চাঁদা (৳)',
                          keyboardType: TextInputType.number,
                          controller: _monthlyChandaController,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  AppTextField(
                    label: 'জাতীয় পরিচয়পত্র (NID)',
                    controller: _nidController,
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  DropdownButtonFormField<String>(
                    value: _selectedRole,
                    isExpanded: true,
                    decoration: const InputDecoration(
                      labelText: 'কমিটির পদবি',
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
                      if (val != null) setState(() => _selectedRole = val);
                    },
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  DropdownButtonFormField<String>(
                    value: _status,
                    isExpanded: true,
                    decoration: const InputDecoration(
                      labelText: 'সদস্যের অবস্থা',
                      border: OutlineInputBorder(),
                    ),
                    items: const [
                      DropdownMenuItem(value: 'active', child: Text('সক্রিয়')),
                      DropdownMenuItem(value: 'inactive', child: Text('নিষ্ক্রিয়')),
                      DropdownMenuItem(value: 'deceased', child: Text('মরহুম / বিগত')),
                    ],
                    onChanged: (val) {
                      if (val != null) setState(() => _status = val);
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
                          label: 'হালনাগাদ করুন',
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
