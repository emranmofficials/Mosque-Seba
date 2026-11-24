import 'package:flutter/material.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/utils/bengali_number_formatter.dart';
import '../models/welfare_case_model.dart';

/// Modal dialog for Approving and Disbursing a Welfare / Zakat Grant
class ApproveWelfareGrantDialog extends StatefulWidget {
  final WelfareCaseModel welfareCase;
  final Function(double approvedAmount, String approverName, bool disburseNow) onApprove;

  const ApproveWelfareGrantDialog({
    super.key,
    required this.welfareCase,
    required this.onApprove,
  });

  @override
  State<ApproveWelfareGrantDialog> createState() => _ApproveWelfareGrantDialogState();
}

class _ApproveWelfareGrantDialogState extends State<ApproveWelfareGrantDialog> {
  late final TextEditingController _amountController;
  final _approverController = TextEditingController(text: 'মুহতারাম সভাপতি');
  bool _disburseNow = true;

  @override
  void initState() {
    super.initState();
    _amountController = TextEditingController(
      text: widget.welfareCase.requestedAmount.toInt().toString(),
    );
  }

  @override
  void dispose() {
    _amountController.dispose();
    _approverController.dispose();
    super.dispose();
  }

  void _submit() {
    final amount = double.tryParse(_amountController.text.trim()) ?? 0.0;
    if (amount <= 0) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('অনুগ্রহ করে সঠিক অনুমোদিত টাকার পরিমাণ লিখুন')),
      );
      return;
    }

    widget.onApprove(amount, _approverController.text.trim(), _disburseNow);
    Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusCard)),
      child: Padding(
        padding: const EdgeInsets.all(AppDimens.spacing20),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: AppColors.roleTreasurer.withOpacity(0.12),
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(Icons.check_circle_outline, color: AppColors.roleTreasurer, size: 24),
                ),
                const SizedBox(width: 10),
                const Text(
                  'অনুদান অনুমোদন ও বিতরণ',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
              ],
            ),
            const Divider(height: 20),

            Text(
              'আবেদনকারী: ${widget.welfareCase.applicantName}',
              style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15),
            ),
            const SizedBox(height: 2),
            Text(
              'খাত: ${widget.welfareCase.categoryTitleBn} (${widget.welfareCase.fundTypeTitleBn})',
              style: TextStyle(fontSize: 12, color: Colors.grey.shade600),
            ),
            const SizedBox(height: 4),
            Text(
              'প্রার্থিত অনুদান: ৳ ${BengaliNumberFormatter.toBengali(widget.welfareCase.requestedAmount.toInt().toString())}',
              style: const TextStyle(fontSize: 13, color: AppColors.emeraldPrimary, fontWeight: FontWeight.w600),
            ),
            const SizedBox(height: 14),

            // Approved Amount
            TextField(
              controller: _amountController,
              keyboardType: TextInputType.number,
              decoration: InputDecoration(
                labelText: 'অনুমোদিত টাকার পরিমাণ *',
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                prefixIcon: const Icon(Icons.currency_exchange),
              ),
            ),
            const SizedBox(height: 12),

            // Approver Title
            TextField(
              controller: _approverController,
              decoration: InputDecoration(
                labelText: 'অনুমোদনকারীর পদবী / নাম',
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                prefixIcon: const Icon(Icons.verified_user_outlined),
              ),
            ),
            const SizedBox(height: 12),

            // Checkbox: Disburse now and post to Finance
            CheckboxListTile(
              contentPadding: EdgeInsets.zero,
              title: const Text('অনুদানের অর্থ তাৎক্ষণিক বিতরণ করুন', style: TextStyle(fontSize: 13, fontWeight: FontWeight.bold)),
              subtitle: const Text('মসজিদ ফাইন্যান্স হিসাবের ব্যয় লেজারে স্বয়ংক্রিয়ভাবে রেকর্ড হবে', style: TextStyle(fontSize: 11)),
              value: _disburseNow,
              activeColor: AppColors.emeraldPrimary,
              onChanged: (val) => setState(() => _disburseNow = val ?? true),
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
              child: Text(
                _disburseNow ? 'অনুমোদন ও অর্থ বিতরণ নিশ্চিত করুন' : 'অনুমোদন সংরক্ষণ করুন',
                style: const TextStyle(fontSize: 15, fontWeight: FontWeight.bold),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
