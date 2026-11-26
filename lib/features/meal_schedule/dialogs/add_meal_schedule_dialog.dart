import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../shared/widgets/app_button.dart';
import '../../../shared/widgets/app_text_field.dart';
import '../../membership/models/member_model.dart';
import '../models/meal_schedule_item.dart';

/// Modal dialog to schedule a staff meal provider for lunch or dinner
class AddMealScheduleDialog extends StatefulWidget {
  final String dateStr;
  final String defaultMealType;
  final MealScheduleItem? existingItem;
  final List<MemberModel> volunteers;
  final Future<bool> Function(MealScheduleItem item) onSubmit;

  const AddMealScheduleDialog({
    super.key,
    required this.dateStr,
    this.defaultMealType = 'দুপুর',
    this.existingItem,
    required this.volunteers,
    required this.onSubmit,
  });

  @override
  State<AddMealScheduleDialog> createState() => _AddMealScheduleDialogState();
}

class _AddMealScheduleDialogState extends State<AddMealScheduleDialog> {
  final _formKey = GlobalKey<FormState>();

  late String _mealType;
  final _providerNameController = TextEditingController();
  final _providerMobileController = TextEditingController();
  final _receiverStaffController = TextEditingController(text: 'পেশ ইমাম ও খাদেম');
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _mealType = widget.existingItem?.mealType ?? widget.defaultMealType;
    if (widget.existingItem != null) {
      _providerNameController.text = widget.existingItem!.providerName;
      _providerMobileController.text = widget.existingItem!.providerMobile;
      _receiverStaffController.text = widget.existingItem!.receiverStaffName;
    }
  }

  @override
  void dispose() {
    _providerNameController.dispose();
    _providerMobileController.dispose();
    _receiverStaffController.dispose();
    super.dispose();
  }

  void _selectVolunteer(MemberModel member) {
    setState(() {
      _providerNameController.text = member.nameBn;
      _providerMobileController.text = member.mobile;
    });
  }

  Future<void> _handleSave() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    final item = MealScheduleItem(
      id: widget.existingItem?.id ?? '',
      dateStr: widget.dateStr,
      mealType: _mealType,
      providerName: _providerNameController.text.trim(),
      providerMobile: _providerMobileController.text.trim(),
      receiverStaffName: _receiverStaffController.text.trim(),
      status: widget.existingItem?.status ?? 'scheduled',
    );

    final ok = await widget.onSubmit(item);

    if (mounted) {
      setState(() => _isLoading = false);
      if (ok) {
        Navigator.of(context).pop();
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('খাবার সূচি সফলভাবে নির্ধারিত হয়েছে')),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('সূচি সংরক্ষণ করতে ব্যর্থ হয়েছে')),
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
                        'খাবার জোগান নির্ধারণ',
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
                  // Meal Type Toggle (দুপুর / রাত)
                  Row(
                    children: [
                      Expanded(
                        child: ChoiceChip(
                          label: const Center(child: Text('দুপুরের খাবার')),
                          selected: _mealType == 'দুপুর',
                          selectedColor: AppColors.primaryContainer,
                          labelStyle: TextStyle(
                            color: _mealType == 'দুপুর' ? AppColors.primary : AppColors.textPrimary,
                            fontWeight: _mealType == 'দুপুর' ? FontWeight.bold : FontWeight.normal,
                          ),
                          onSelected: (sel) {
                            if (sel) setState(() => _mealType = 'দুপুর');
                          },
                        ),
                      ),
                      const SizedBox(width: AppDimens.spacing8),
                      Expanded(
                        child: ChoiceChip(
                          label: const Center(child: Text('রাতের খাবার')),
                          selected: _mealType == 'রাত',
                          selectedColor: AppColors.primaryContainer,
                          labelStyle: TextStyle(
                            color: _mealType == 'রাত' ? AppColors.primary : AppColors.textPrimary,
                            fontWeight: _mealType == 'রাত' ? FontWeight.bold : FontWeight.normal,
                          ),
                          onSelected: (sel) {
                            if (sel) setState(() => _mealType = 'রাত');
                          },
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: AppDimens.spacing16),
                  // Volunteer Quick Pick Strip
                  if (widget.volunteers.isNotEmpty) ...[
                    Text(
                      'নিবন্ধিত খাবার জোগানদাতা সদস্য:',
                      style: AppTypography.caption.copyWith(color: AppColors.textSecondary, fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 6),
                    SizedBox(
                      height: 34,
                      child: ListView.separated(
                        scrollDirection: Axis.horizontal,
                        itemCount: widget.volunteers.length,
                        separatorBuilder: (_, __) => const SizedBox(width: 6),
                        itemBuilder: (ctx, idx) {
                          final v = widget.volunteers[idx];
                          return ActionChip(
                            label: Text(v.nameBn, style: const TextStyle(fontSize: 12)),
                            avatar: const Icon(Icons.person, size: 14),
                            onPressed: () => _selectVolunteer(v),
                          );
                        },
                      ),
                    ),
                    const SizedBox(height: AppDimens.spacing12),
                  ],
                  AppTextField(
                    label: 'জোগানদাতার নাম *',
                    hint: 'যেমন: হাজী মো: রফিকুল ইসলাম',
                    controller: _providerNameController,
                    validator: (v) => v == null || v.trim().isEmpty ? 'নাম আবশ্যক' : null,
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  AppTextField(
                    label: 'মোবাইল নম্বর *',
                    hint: '০১৭১XXXXXXXX',
                    keyboardType: TextInputType.phone,
                    controller: _providerMobileController,
                    validator: (v) => v == null || v.trim().isEmpty ? 'মোবাইল আবশ্যক' : null,
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  AppTextField(
                    label: 'প্রাপক স্টাফ সদস্য *',
                    hint: 'যেমন: পেশ ইমাম ও খাদেম',
                    controller: _receiverStaffController,
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
                          label: 'সূচি নিশ্চিত করুন',
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
