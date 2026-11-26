import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../madrasa/models/student_model.dart';
import '../../madrasa/providers/madrasa_provider.dart';
import '../models/madrasa_fee_payment_model.dart';

/// Modal Dialog for Collecting Madrasa Student Tuition Fee
class CollectStudentFeeDialog extends ConsumerStatefulWidget {
  final StudentModel? preselectedStudent;
  final Function(MadrasaFeePaymentModel) onSubmit;

  const CollectStudentFeeDialog({
    super.key,
    this.preselectedStudent,
    required this.onSubmit,
  });

  @override
  ConsumerState<CollectStudentFeeDialog> createState() => _CollectStudentFeeDialogState();
}

class _CollectStudentFeeDialogState extends ConsumerState<CollectStudentFeeDialog> {
  final _formKey = GlobalKey<FormState>();

  StudentModel? _selectedStudent;
  final _amountPaidController = TextEditingController(text: '1200');
  final _discountController = TextEditingController(text: '0');

  String _selectedMonth = 'নভেম্বর ২০২৬';
  String _paymentMethod = 'cash';

  final List<String> _months = [
    'জানুয়ারি ২০২৬',
    'ফেব্রুয়ারি ২০২৬',
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
    _selectedStudent = widget.preselectedStudent;
    if (_selectedStudent != null) {
      _amountPaidController.text = _selectedStudent!.monthlyFee.toInt().toString();
    }
  }

  @override
  void dispose() {
    _amountPaidController.dispose();
    _discountController.dispose();
    super.dispose();
  }

  void _submit() {
    if (_selectedStudent == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('অনুগ্রহ করে একজন শিক্ষার্থী নির্বাচন করুন')),
      );
      return;
    }

    if (!_formKey.currentState!.validate()) return;

    final now = DateTime.now();
    final dateStr =
        '${now.year}-${now.month.toString().padLeft(2, '0')}-${now.day.toString().padLeft(2, '0')}';
    final receipt = 'MFR-${now.year}-${now.millisecondsSinceEpoch % 10000}';

    final payment = MadrasaFeePaymentModel(
      id: 'mfp-${DateTime.now().millisecondsSinceEpoch}',
      studentId: _selectedStudent!.id,
      studentNameBn: _selectedStudent!.nameBn,
      className: _selectedStudent!.className,
      month: _selectedMonth,
      amountPaid: double.tryParse(_amountPaidController.text.trim()) ?? 0.0,
      discountAmount: double.tryParse(_discountController.text.trim()) ?? 0.0,
      paymentMethod: _paymentMethod,
      receiptCode: receipt,
      status: 'paid',
      receivedBy: 'কোষাধ্যক্ষ',
      paidAt: dateStr,
      createdAt: DateTime.now().millisecondsSinceEpoch,
    );

    widget.onSubmit(payment);
    Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    final studentsAsync = ref.watch(studentListProvider);
    final allStudents = studentsAsync.valueOrNull ?? [];

    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusCard)),
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 480, maxHeight: 600),
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
                            color: AppColors.roleTreasurer.withOpacity(0.12),
                            shape: BoxShape.circle,
                          ),
                          child: const Icon(Icons.receipt_long, color: AppColors.roleTreasurer, size: 22),
                        ),
                        const SizedBox(width: 10),
                        const Text(
                          'মাদরাসা ফি আদায় রশিদ',
                          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
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
                        // Student Selector
                        const Text('শিক্ষার্থী নির্বাচন *', style: TextStyle(fontWeight: FontWeight.w600)),
                        const SizedBox(height: 6),
                        DropdownButtonFormField<StudentModel>(
                          value: _selectedStudent,
                          decoration: InputDecoration(
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                            contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                          ),
                          hint: const Text('শিক্ষার্থী বেছে নিন'),
                          items: allStudents.map((s) {
                            return DropdownMenuItem(
                              value: s,
                              child: Text('${s.nameBn} (${s.className})'),
                            );
                          }).toList(),
                          onChanged: (val) {
                            if (val != null) {
                              setState(() {
                                _selectedStudent = val;
                                _amountPaidController.text = val.monthlyFee.toInt().toString();
                              });
                            }
                          },
                        ),
                        const SizedBox(height: 14),

                        // Month & Method Row
                        Row(
                          children: [
                            Expanded(
                              flex: 3,
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  const Text('ফি আদায়ের মাস *', style: TextStyle(fontWeight: FontWeight.w600)),
                                  const SizedBox(height: 6),
                                  DropdownButtonFormField<String>(
                                    value: _selectedMonth,
                                    decoration: InputDecoration(
                                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                                      contentPadding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
                                    ),
                                    items: _months.map((m) => DropdownMenuItem(value: m, child: Text(m))).toList(),
                                    onChanged: (val) {
                                      if (val != null) setState(() => _selectedMonth = val);
                                    },
                                  ),
                                ],
                              ),
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              flex: 2,
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  const Text('মাধ্যম', style: TextStyle(fontWeight: FontWeight.w600)),
                                  const SizedBox(height: 6),
                                  DropdownButtonFormField<String>(
                                    value: _paymentMethod,
                                    decoration: InputDecoration(
                                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                                      contentPadding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
                                    ),
                                    items: const [
                                      DropdownMenuItem(value: 'cash', child: Text('নগদ (Cash)')),
                                      DropdownMenuItem(value: 'bkash', child: Text('বিকাশ')),
                                      DropdownMenuItem(value: 'nagad', child: Text('নগদ')),
                                      DropdownMenuItem(value: 'bank', child: Text('ব্যাংক')),
                                    ],
                                    onChanged: (val) {
                                      if (val != null) setState(() => _paymentMethod = val);
                                    },
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 14),

                        // Amount Paid
                        TextFormField(
                          controller: _amountPaidController,
                          keyboardType: TextInputType.number,
                          decoration: InputDecoration(
                            labelText: 'আদায়কৃত টাকার পরিমাণ *',
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                            prefixIcon: const Icon(Icons.currency_exchange),
                          ),
                          validator: (v) {
                            if (v == null || v.trim().isEmpty) return 'টাকার পরিমাণ দিন';
                            if (double.tryParse(v.trim()) == null) return 'সঠিক সংখ্যা লিখুন';
                            return null;
                          },
                        ),
                        const SizedBox(height: 14),

                        // Discount Amount
                        TextFormField(
                          controller: _discountController,
                          keyboardType: TextInputType.number,
                          decoration: InputDecoration(
                            labelText: 'বিশেষ ছাড় / মওকুফ (যদি থাকে)',
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                            prefixIcon: const Icon(Icons.discount_outlined),
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
                    backgroundColor: AppColors.roleTreasurer,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 14),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusButton)),
                  ),
                  child: const Text('ফি আদায় ও রসিদ তৈরি করুন', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
