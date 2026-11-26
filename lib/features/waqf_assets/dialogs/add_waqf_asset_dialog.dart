import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../shared/widgets/app_button.dart';
import '../../../shared/widgets/app_text_field.dart';
import '../models/waqf_asset_model.dart';

/// Dialog to record or edit a Waqf Property (ওয়াকফ সম্পত্তি নিবন্ধন ও সম্পাদনা)
class AddWaqfAssetDialog extends StatefulWidget {
  final WaqfAssetModel? existingAsset;
  final Future<bool> Function(WaqfAssetModel asset) onSubmit;

  const AddWaqfAssetDialog({
    super.key,
    this.existingAsset,
    required this.onSubmit,
  });

  @override
  State<AddWaqfAssetDialog> createState() => _AddWaqfAssetDialogState();
}

class _AddWaqfAssetDialogState extends State<AddWaqfAssetDialog> {
  final _formKey = GlobalKey<FormState>();

  final _titleController = TextEditingController();
  final _areaController = TextEditingController();
  final _locationController = TextEditingController();
  final _deedNoController = TextEditingController();
  final _valuationController = TextEditingController();
  final _descriptionController = TextEditingController();

  String _selectedCategory = 'জমি / প্লট';
  String _selectedStatus = 'সংরক্ষিত';
  bool _isLoading = false;

  final List<String> _categories = [
    'জমি / প্লট',
    'দোকান / মার্কেট',
    'পুকুর / জলাশয়',
    'ভবন / স্থাপনা',
    'অন্যান্য',
  ];

  final List<String> _statuses = [
    'সংরক্ষিত',
    'ভাড়ায় চালিত',
    'লীজকৃত',
    'বিতর্কিত',
  ];

  @override
  void initState() {
    super.initState();
    final existing = widget.existingAsset;
    if (existing != null) {
      _titleController.text = existing.title;
      _areaController.text = existing.areaOrQuantity;
      _locationController.text = existing.locationOrDagNo;
      _deedNoController.text = existing.deedOrRegNo;
      _valuationController.text = existing.estimatedValueOrIncome;
      _descriptionController.text = existing.description;
      if (_categories.contains(existing.category)) {
        _selectedCategory = existing.category;
      }
      if (_statuses.contains(existing.status)) {
        _selectedStatus = existing.status;
      }
    }
  }

  @override
  void dispose() {
    _titleController.dispose();
    _areaController.dispose();
    _locationController.dispose();
    _deedNoController.dispose();
    _valuationController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  Future<void> _handleSave() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    final existing = widget.existingAsset;
    final asset = WaqfAssetModel(
      id: existing?.id ?? '',
      title: _titleController.text.trim(),
      category: _selectedCategory,
      areaOrQuantity: _areaController.text.trim(),
      locationOrDagNo: _locationController.text.trim(),
      deedOrRegNo: _deedNoController.text.trim(),
      estimatedValueOrIncome: _valuationController.text.trim(),
      status: _selectedStatus,
      description: _descriptionController.text.trim(),
      recordedDate:
          existing?.recordedDate.isNotEmpty == true
              ? existing!.recordedDate
              : DateTime.now().toIso8601String().split('T').first,
    );

    final ok = await widget.onSubmit(asset);

    if (mounted) {
      setState(() => _isLoading = false);
      if (ok) {
        Navigator.of(context).pop();
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              existing != null
                  ? 'ওয়াকফ সম্পত্তির তথ্য হালনাগাদ হয়েছে'
                  : 'ওয়াকফ সম্পত্তি সফলভাবে নিবন্ধিত হয়েছে',
            ),
          ),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('সম্পত্তি সংরক্ষণ করতে ব্যর্থ হয়েছে')),
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
                        widget.existingAsset != null
                            ? 'ওয়াকফ সম্পত্তি সম্পাদনা'
                            : 'ওয়াকফ সম্পত্তি নিবন্ধন',
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
                    label: 'সম্পত্তির নাম / শিরোনাম *',
                    hint: 'যেমন: মসজিদের মূল মাঠ ও কবরস্থান',
                    controller: _titleController,
                    validator: (v) => v == null || v.trim().isEmpty ? 'শিরোনাম আবশ্যক' : null,
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  Text(
                    'সম্পত্তির ধরন *',
                    style: AppTypography.bodySmall.copyWith(fontWeight: FontWeight.w600),
                  ),
                  const SizedBox(height: 4),
                  DropdownButtonFormField<String>(
                    value: _selectedCategory,
                    decoration: InputDecoration(
                      filled: true,
                      fillColor: AppColors.surface,
                      contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusMd)),
                    ),
                    items: _categories.map((c) => DropdownMenuItem(value: c, child: Text(c))).toList(),
                    onChanged: (v) {
                      if (v != null) setState(() => _selectedCategory = v);
                    },
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  Row(
                    children: [
                      Expanded(
                        child: AppTextField(
                          label: 'আয়তন / পরিমাণ *',
                          hint: 'যেমন: ১৫ শতক',
                          controller: _areaController,
                          validator: (v) => v == null || v.trim().isEmpty ? 'আয়তন আবশ্যক' : null,
                        ),
                      ),
                      const SizedBox(width: AppDimens.spacing12),
                      Expanded(
                        child: AppTextField(
                          label: 'দাগ / খতিয়ান নং *',
                          hint: 'দাগ: ১২৩৪, খতিয়ান: ৫৬',
                          controller: _locationController,
                          validator: (v) => v == null || v.trim().isEmpty ? 'দাগ নং আবশ্যক' : null,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  Row(
                    children: [
                      Expanded(
                        child: AppTextField(
                          label: 'দলিল / রেজিস্ট্রি নং',
                          hint: 'ঐচ্ছিক',
                          controller: _deedNoController,
                        ),
                      ),
                      const SizedBox(width: AppDimens.spacing12),
                      Expanded(
                        child: AppTextField(
                          label: 'আনুমানিক মূল্য / আয়',
                          hint: '৫০,০০,০০০ ৳',
                          controller: _valuationController,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  Text(
                    'বর্তমান অবস্থা',
                    style: AppTypography.bodySmall.copyWith(fontWeight: FontWeight.w600),
                  ),
                  const SizedBox(height: 4),
                  DropdownButtonFormField<String>(
                    value: _selectedStatus,
                    decoration: InputDecoration(
                      filled: true,
                      fillColor: AppColors.surface,
                      contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusMd)),
                    ),
                    items: _statuses.map((s) => DropdownMenuItem(value: s, child: Text(s))).toList(),
                    onChanged: (v) {
                      if (v != null) setState(() => _selectedStatus = v);
                    },
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  AppTextField(
                    label: 'বিবরণ ও বিবরণী',
                    hint: 'সম্পত্তি সংক্রান্ত অতিরিক্ত তথ্য...',
                    maxLines: 2,
                    controller: _descriptionController,
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
                          label: widget.existingAsset != null
                              ? 'হালনাগাদ করুন'
                              : 'নিবন্ধন করুন',
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
