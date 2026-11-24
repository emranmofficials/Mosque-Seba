import 'package:flutter/material.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/utils/bengali_number_formatter.dart';
import '../../../../shared/widgets/app_button.dart';
import '../models/cashbox_record_model.dart';
import '../widgets/denomination_counter_row.dart';
import '../widgets/witness_signature_panel.dart';

/// Dialog with interactive currency breakdown and 3-witness verification for Friday Cashbox
class CashboxCounterDialog extends StatefulWidget {
  final Future<CashboxRecordModel?> Function(CashboxRecordModel record) onSubmit;

  const CashboxCounterDialog({super.key, required this.onSubmit});

  @override
  State<CashboxCounterDialog> createState() => _CashboxCounterDialogState();
}

class _CashboxCounterDialogState extends State<CashboxCounterDialog> {
  int _n1000 = 0;
  int _n500 = 0;
  int _n200 = 0;
  int _n100 = 0;
  int _n50 = 0;
  int _n20 = 0;
  int _n10 = 0;
  int _n5 = 0;
  double _coins = 0.0;

  final _witness1Controller = TextEditingController(text: 'মাওলানা মো: ইউসুফ (পেশ ইমাম)');
  final _witness2Controller = TextEditingController(text: 'হাজী মো: আনোয়ার (কোষাধ্যক্ষ)');
  final _witness3Controller = TextEditingController(text: 'সাধারণ মুসল্লি প্রতিনিধি');
  final _coinsController = TextEditingController(text: '0');

  bool _isLoading = false;

  @override
  void dispose() {
    _witness1Controller.dispose();
    _witness2Controller.dispose();
    _witness3Controller.dispose();
    _coinsController.dispose();
    super.dispose();
  }

  double get _totalAmount {
    return (_n1000 * 1000) +
        (_n500 * 500) +
        (_n200 * 200) +
        (_n100 * 100) +
        (_n50 * 50) +
        (_n20 * 20) +
        (_n10 * 10) +
        (_n5 * 5) +
        _coins;
  }

  Future<void> _handleConfirm() async {
    if (_totalAmount <= 0) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('অন্তত একটি নোট বা মুদ্রার সংখ্যা গণনা করুন')),
      );
      return;
    }

    if (_witness1Controller.text.trim().isEmpty ||
        _witness2Controller.text.trim().isEmpty ||
        _witness3Controller.text.trim().isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('৩ জন সাক্ষীর নাম প্রদান বাধ্যতামূলক')),
      );
      return;
    }

    setState(() => _isLoading = true);

    final record = CashboxRecordModel(
      id: '',
      waqtDate: DateTime.now().toIso8601String().split('T').first,
      note1000: _n1000,
      note500: _n500,
      note200: _n200,
      note100: _n100,
      note50: _n50,
      note20: _n20,
      note10: _n10,
      note5: _n5,
      coinsAmount: _coins,
      totalAmount: _totalAmount,
      witness1Name: _witness1Controller.text.trim(),
      witness2Name: _witness2Controller.text.trim(),
      witness3Name: _witness3Controller.text.trim(),
      countedAt: DateTime.now().millisecondsSinceEpoch,
    );

    final saved = await widget.onSubmit(record);

    if (mounted) {
      setState(() => _isLoading = false);
      if (saved != null) {
        Navigator.of(context).pop(saved);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('দানবাক্স গণনা ও তহবিলে অটো-পোস্টিং সম্পন্ন হয়েছে')),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('গণনা সংরক্ষণ করতে ব্যর্থ হয়েছে')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusLg)),
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 520),
        child: Padding(
          padding: const EdgeInsets.all(AppDimens.spacing20),
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      'পবিত্র জুমার দানবাক্স গণনা',
                      style: AppTypography.titleLarge.copyWith(fontWeight: FontWeight.bold),
                    ),
                    IconButton(
                      icon: const Icon(Icons.close),
                      onPressed: () => Navigator.of(context).pop(),
                    ),
                  ],
                ),
                const Divider(),
                const SizedBox(height: AppDimens.spacing8),
                // Realtime Grand Total Box
                Container(
                  padding: const EdgeInsets.all(AppDimens.spacing16),
                  decoration: BoxDecoration(
                    color: AppColors.primaryContainer,
                    borderRadius: BorderRadius.circular(AppDimens.radiusMd),
                    border: Border.all(color: AppColors.primary.withValues(alpha: 0.3)),
                  ),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        'মোট গণনাকৃত টাকা:',
                        style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold),
                      ),
                      Text(
                        BengaliNumberFormatter.formatCurrency(_totalAmount),
                        style: AppTypography.headlineSmall.copyWith(
                          fontWeight: FontWeight.bold,
                          color: AppColors.primary,
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: AppDimens.spacing16),
                Text('নোটের সংখ্যা নির্ধারণ করুন', style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold)),
                const SizedBox(height: AppDimens.spacing8),
                DenominationCounterRow(denomination: 1000, count: _n1000, onCountChanged: (v) => setState(() => _n1000 = v)),
                DenominationCounterRow(denomination: 500, count: _n500, onCountChanged: (v) => setState(() => _n500 = v)),
                DenominationCounterRow(denomination: 200, count: _n200, onCountChanged: (v) => setState(() => _n200 = v)),
                DenominationCounterRow(denomination: 100, count: _n100, onCountChanged: (v) => setState(() => _n100 = v)),
                DenominationCounterRow(denomination: 50, count: _n50, onCountChanged: (v) => setState(() => _n50 = v)),
                DenominationCounterRow(denomination: 20, count: _n20, onCountChanged: (v) => setState(() => _n20 = v)),
                DenominationCounterRow(denomination: 10, count: _n10, onCountChanged: (v) => setState(() => _n10 = v)),
                DenominationCounterRow(denomination: 5, count: _n5, onCountChanged: (v) => setState(() => _n5 = v)),
                const SizedBox(height: AppDimens.spacing12),
                Row(
                  children: [
                    Expanded(
                      child: Text(
                        'খুচরা মুদ্রা / কয়েন (টাকা):',
                        style: AppTypography.bodyMedium.copyWith(fontWeight: FontWeight.w600),
                      ),
                    ),
                    SizedBox(
                      width: 100,
                      child: TextField(
                        keyboardType: TextInputType.number,
                        controller: _coinsController,
                        decoration: const InputDecoration(
                          hintText: '0',
                          contentPadding: EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                          border: OutlineInputBorder(),
                        ),
                        onChanged: (v) {
                          setState(() {
                            _coins = double.tryParse(v.trim()) ?? 0.0;
                          });
                        },
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: AppDimens.spacing16),
                // Witness Panel
                WitnessSignaturePanel(
                  witness1Controller: _witness1Controller,
                  witness2Controller: _witness2Controller,
                  witness3Controller: _witness3Controller,
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
                        text: 'নিশ্চিত ও জমা করুন',
                        isLoading: _isLoading,
                        onPressed: _handleConfirm,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
