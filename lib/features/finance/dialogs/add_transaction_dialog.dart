import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../shared/widgets/app_button.dart';
import '../../../shared/widgets/app_text_field.dart';

/// Modal dialog to record a new Income or Expense voucher
class AddTransactionDialog extends StatefulWidget {
  final String defaultType; // 'income' or 'expense'
  final Future<bool> Function({
    required String categoryNameBn,
    required String type,
    required double amount,
    required String txnDate,
    required String description,
  }) onSubmit;

  const AddTransactionDialog({
    super.key,
    this.defaultType = 'expense',
    required this.onSubmit,
  });

  @override
  State<AddTransactionDialog> createState() => _AddTransactionDialogState();
}

class _AddTransactionDialogState extends State<AddTransactionDialog> {
  final _formKey = GlobalKey<FormState>();

  late String _type;
  late String _selectedCategory;
  final _amountController = TextEditingController();
  final _descriptionController = TextEditingController();
  final _dateController = TextEditingController(
    text: DateTime.now().toIso8601String().split('T').first,
  );
  bool _isLoading = false;

  final List<String> _incomeCategories = [
    'জুমার দানবাক্স',
    'মাসিক চাঁদা',
    'ওয়াকফ সম্পত্তি ভাড়া',
    'বিশেষ এককালীন অনুদান',
    'অন্যান্য আয়',
  ];

  final List<String> _expenseCategories = [
    'ইমাম-মুয়াজ্জিন বেতনভাতা',
    'বিদ্যুৎ ও ইউটিলিটি বিল',
    'মসজিদ সংস্কার ও মেরামত',
    'পরিচ্ছন্নতা ও রক্ষণাবেক্ষণ',
    'মাহফিল ও ইসলামিক অনুষ্ঠান',
    'অন্যান্য ব্যয়',
  ];

  @override
  void initState() {
    super.initState();
    _type = widget.defaultType;
    _selectedCategory = _type == 'income' ? _incomeCategories.first : _expenseCategories.first;
  }

  @override
  void dispose() {
    _amountController.dispose();
    _descriptionController.dispose();
    _dateController.dispose();
    super.dispose();
  }

  Future<void> _handleSave() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    final amount = double.tryParse(_amountController.text.trim()) ?? 0.0;
    final ok = await widget.onSubmit(
      categoryNameBn: _selectedCategory,
      type: _type,
      amount: amount,
      txnDate: _dateController.text.trim(),
      description: _descriptionController.text.trim(),
    );

    if (mounted) {
      setState(() => _isLoading = false);
      if (ok) {
        Navigator.of(context).pop();
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('ভাউচার সফলভাবে দাখিল হয়েছে')),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('ভাউচার দাখিল করতে ব্যর্থ হয়েছে')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final categories = _type == 'income' ? _incomeCategories : _expenseCategories;

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
                        'নতুন ভাউচার এন্ট্রি',
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
                  // Income / Expense Segmented Control
                  Row(
                    children: [
                      Expanded(
                        child: ChoiceChip(
                          label: const Center(child: Text('আয়ের ভাউচার (+)')),
                          selected: _type == 'income',
                          selectedColor: AppColors.success.withOpacity(0.2),
                          labelStyle: TextStyle(
                            color: _type == 'income' ? AppColors.success : AppColors.textPrimary,
                            fontWeight: _type == 'income' ? FontWeight.bold : FontWeight.normal,
                          ),
                          onSelected: (sel) {
                            if (sel) {
                              setState(() {
                                _type = 'income';
                                _selectedCategory = _incomeCategories.first;
                              });
                            }
                          },
                        ),
                      ),
                      const SizedBox(width: AppDimens.spacing8),
                      Expanded(
                        child: ChoiceChip(
                          label: const Center(child: Text('ব্যয়ের ভাউচার (-)')),
                          selected: _type == 'expense',
                          selectedColor: AppColors.error.withOpacity(0.2),
                          labelStyle: TextStyle(
                            color: _type == 'expense' ? AppColors.error : AppColors.textPrimary,
                            fontWeight: _type == 'expense' ? FontWeight.bold : FontWeight.normal,
                          ),
                          onSelected: (sel) {
                            if (sel) {
                              setState(() {
                                _type = 'expense';
                                _selectedCategory = _expenseCategories.first;
                              });
                            }
                          },
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: AppDimens.spacing16),
                  Text('হিসাব খাত / ক্যাটাগরি *', style: AppTypography.bodySmall.copyWith(fontWeight: FontWeight.w600)),
                  const SizedBox(height: 4),
                  DropdownButtonFormField<String>(
                    value: _selectedCategory,
                    decoration: InputDecoration(
                      filled: true,
                      fillColor: AppColors.surface,
                      contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusMd)),
                    ),
                    items: categories.map((c) => DropdownMenuItem(value: c, child: Text(c))).toList(),
                    onChanged: (v) {
                      if (v != null) setState(() => _selectedCategory = v);
                    },
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  Row(
                    children: [
                      Expanded(
                        child: AppTextField(
                          label: 'টাকার পরিমাণ (৳) *',
                          hint: '0.00',
                          keyboardType: TextInputType.number,
                          controller: _amountController,
                          validator: (v) {
                            if (v == null || v.trim().isEmpty) return 'পরিমাণ আবশ্যক';
                            final n = double.tryParse(v.trim());
                            if (n == null || n <= 0) return 'সঠিক পরিমাণ দিন';
                            return null;
                          },
                        ),
                      ),
                      const SizedBox(width: AppDimens.spacing12),
                      Expanded(
                        child: AppTextField(
                          label: 'লেনদেনের তারিখ *',
                          hint: 'YYYY-MM-DD',
                          controller: _dateController,
                          validator: (v) => v == null || v.trim().isEmpty ? 'তারিখ আবশ্যক' : null,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  AppTextField(
                    label: 'বিবরণ ও উদ্দেশ্য',
                    hint: 'ভাউচারের বিস্তারিত বিবরণ লিখুন...',
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
                          label: 'ভাউচার দাখিল করুন',
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
