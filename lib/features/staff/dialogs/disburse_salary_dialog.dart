import 'package:flutter/material.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/utils/bengali_number_formatter.dart';
import '../../../../shared/widgets/app_button.dart';
import '../../../../shared/widgets/app_text_field.dart';
import '../models/staff_model.dart';
import '../models/staff_payroll_model.dart';

/// Modal dialog to calculate and disburse monthly staff salary with auto General Ledger expense posting
class DisburseSalaryDialog extends StatefulWidget {
  final StaffModel staff;
  final Future<StaffPayrollModel?> Function({
    required String staffId,
    required String staffNameBn,
    required String month,
    required double baseSalary,
    required double bonusAmount,
    required String disbursedBy,
  }) onDisburse;

  const DisburseSalaryDialog({
    super.key,
    required this.staff,
    required this.onDisburse,
  });

  @override
  State<DisburseSalaryDialog> createState() => _DisburseSalaryDialogState();
}

class _DisburseSalaryDialogState extends State<DisburseSalaryDialog> {
  final _formKey = GlobalKey<FormState>();

  late TextEditingController _baseSalaryController;
  final _bonusController = TextEditingController(text: '0');
  final _disbursedByController = TextEditingController(text: 'কোষাধ্যক্ষ');

  String _selectedMonth = 'মার্চ ২০২৬';
  bool _isLoading = false;

  final List<String> _months = [
    'জানুয়ারি ২০২৬',
    'ফেব্রুয়ারি ২০২৬',
    'মার্চ ২০২৬',
    'এপ্রিল ২০২৬',
    'মে ২০২৬',
    'জুন ২০২৬',
    'জুলাই ২০২৬',
    'আগস্ট ২০২৬',
    'সেপ্টেম্বর ২০২৬',
    'অক্টোবর ২০২৬',
    'নভেম্বর ২০২৬',
    'ডিসেম্বর ২০২৬',
  ];

  @override
  void initState() {
    super.initState();
    _baseSalaryController = TextEditingController(text: widget.staff.salaryAmount.toStringAsFixed(0));
  }

  @override
  void dispose() {
    _baseSalaryController.dispose();
    _bonusController.dispose();
    _disbursedByController.dispose();
    super.dispose();
  }

  double get _netSalary {
    final base = double.tryParse(_baseSalaryController.text.trim()) ?? 0.0;
    final bonus = double.tryParse(_bonusController.text.trim()) ?? 0.0;
    return base + bonus;
  }

  Future<void> _handleDisburse() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    final base = double.tryParse(_baseSalaryController.text.trim()) ?? 0.0;
    final bonus = double.tryParse(_bonusController.text.trim()) ?? 0.0;

    final payroll = await widget.onDisburse(
      staffId: widget.staff.id,
      staffNameBn: widget.staff.nameBn,
      month: _selectedMonth,
      baseSalary: base,
      bonusAmount: bonus,
      disbursedBy: _disbursedByController.text.trim(),
    );

    if (mounted) {
      setState(() => _isLoading = false);
      if (payroll != null) {
        Navigator.of(context).pop(payroll);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('বেতনভাতা সফলভাবে পরিশোধ ও তহবিলের ব্যয় লিপিবদ্ধ হয়েছে')),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('বেতন পরিশোধ করতে ব্যর্থ হয়েছে')),
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
                        'বেতন ও ভাতা পরিশোধ',
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
                  // Staff Info Card
                  Container(
                    padding: const EdgeInsets.all(AppDimens.spacing12),
                    decoration: BoxDecoration(
                      color: AppColors.primaryContainer,
                      borderRadius: BorderRadius.circular(AppDimens.radiusMd),
                    ),
                    child: Row(
                      children: [
                        const Icon(Icons.person, color: AppColors.primary),
                        const SizedBox(width: AppDimens.spacing8),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                widget.staff.nameBn,
                                style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold),
                              ),
                              Text(
                                widget.staff.designationBn,
                                style: AppTypography.caption.copyWith(color: AppColors.primary),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  Text('বেতনের মাস *', style: AppTypography.bodySmall.copyWith(fontWeight: FontWeight.w600)),
                  const SizedBox(height: 4),
                  DropdownButtonFormField<String>(
                    value: _selectedMonth,
                    decoration: InputDecoration(
                      filled: true,
                      fillColor: AppColors.surface,
                      contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusMd)),
                    ),
                    items: _months.map((m) => DropdownMenuItem(value: m, child: Text(m))).toList(),
                    onChanged: (v) {
                      if (v != null) setState(() => _selectedMonth = v);
                    },
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  Row(
                    children: [
                      Expanded(
                        child: AppTextField(
                          label: 'মূল বেতন (৳) *',
                          keyboardType: TextInputType.number,
                          controller: _baseSalaryController,
                          onChanged: (_) => setState(() {}),
                          validator: (v) => v == null || v.trim().isEmpty ? 'মূল বেতন আবশ্যক' : null,
                        ),
                      ),
                      const SizedBox(width: AppDimens.spacing12),
                      Expanded(
                        child: AppTextField(
                          label: 'ঈদ বোনাস / অতিরিক্ত (৳)',
                          keyboardType: TextInputType.number,
                          controller: _bonusController,
                          onChanged: (_) => setState(() {}),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  AppTextField(
                    label: 'বিতরণকারী কর্মকর্তা',
                    controller: _disbursedByController,
                  ),
                  const SizedBox(height: AppDimens.spacing16),
                  // Total Payable Box
                  Container(
                    padding: const EdgeInsets.all(AppDimens.spacing12),
                    decoration: BoxDecoration(
                      color: AppColors.surface,
                      borderRadius: BorderRadius.circular(AppDimens.radiusMd),
                      border: Border.all(color: AppColors.primary.withValues(alpha: 0.4)),
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          'মোট প্রদেয় বেতনভাতা:',
                          style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold),
                        ),
                        Text(
                          BengaliNumberFormatter.formatCurrency(_netSalary),
                          style: AppTypography.titleLarge.copyWith(
                            fontWeight: FontWeight.bold,
                            color: AppColors.primary,
                          ),
                        ),
                      ],
                    ),
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
                          text: 'বেতন পরিশোধ নিশ্চিত করুন',
                          isLoading: _isLoading,
                          onPressed: _handleDisburse,
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
