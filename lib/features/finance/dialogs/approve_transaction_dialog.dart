import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/widgets/app_button.dart';
import '../models/finance_transaction_model.dart';

/// Dialog to approve or reject a pending transaction voucher
class ApproveTransactionDialog extends StatefulWidget {
  final FinanceTransactionModel transaction;
  final String currentUserName;
  final Future<bool> Function({
    required String transactionId,
    required String reviewerName,
    required String creatorName,
    required bool isApproved,
  }) onReview;

  const ApproveTransactionDialog({
    super.key,
    required this.transaction,
    required this.currentUserName,
    required this.onReview,
  });

  @override
  State<ApproveTransactionDialog> createState() => _ApproveTransactionDialogState();
}

class _ApproveTransactionDialogState extends State<ApproveTransactionDialog> {
  bool _isLoading = false;

  bool get _isSelfApproval {
    return widget.currentUserName.trim().toLowerCase() ==
        widget.transaction.createdBy.trim().toLowerCase();
  }

  Future<void> _handleDecision(bool isApproved) async {
    if (_isSelfApproval) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          backgroundColor: AppColors.error,
          content: Text('অনুমোদন নীতি লঙ্ঘন: ভাউচার তৈরিকারী নিজে অনুমোদন দিতে পারবেন না!'),
        ),
      );
      return;
    }

    setState(() => _isLoading = true);

    final ok = await widget.onReview(
      transactionId: widget.transaction.id,
      reviewerName: widget.currentUserName,
      creatorName: widget.transaction.createdBy,
      isApproved: isApproved,
    );

    if (mounted) {
      setState(() => _isLoading = false);
      if (ok) {
        Navigator.of(context).pop();
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(isApproved ? 'ভাউচার অনুমোদিত হয়েছে' : 'ভাউচার বাতিল করা হয়েছে'),
          ),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('সিদ্ধান্ত সংরক্ষণ করা যায়নি')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final txn = widget.transaction;

    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusLg)),
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 440),
        child: Padding(
          padding: const EdgeInsets.all(AppDimens.spacing20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    'ভাউচার যাচাই ও অনুমোদন',
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
              if (_isSelfApproval)
                Container(
                  margin: const EdgeInsets.only(bottom: AppDimens.spacing12),
                  padding: const EdgeInsets.all(AppDimens.spacing10),
                  decoration: BoxDecoration(
                    color: AppColors.error.withValues(alpha: 0.1),
                    borderRadius: BorderRadius.circular(AppDimens.radiusSm),
                    border: Border.all(color: AppColors.error.withValues(alpha: 0.4)),
                  ),
                  child: Row(
                    children: [
                      const Icon(Icons.warning_amber_rounded, color: AppColors.error, size: 20),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          'সতর্কবার্তা: আপনি এই ভাউচারের দাখিলকারী। অডিট নিয়ম অনুযায়ী অন্য একজন অনুমোদিত কর্মকর্তার স্বাক্ষর আবশ্যক।',
                          style: AppTypography.caption.copyWith(color: AppColors.error),
                        ),
                      ),
                    ],
                  ),
                ),
              _buildRow('ভাউচার কোড', BengaliNumberFormatter.toBengali(txn.transactionCode)),
              _buildRow('খাত / ক্যাটাগরি', txn.categoryNameBn),
              _buildRow('লেনদেনের ধরন', txn.isIncome ? 'আয় (+)' : 'ব্যয় (-)'),
              _buildRow('তারিখ', BengaliNumberFormatter.toBengali(txn.txnDate)),
              _buildRow('টাকার পরিমাণ', BengaliNumberFormatter.formatCurrency(txn.amount), isBold: true),
              _buildRow('দাখিলকারী', txn.createdBy),
              if (txn.description.isNotEmpty) _buildRow('বিবরণ', txn.description),
              const SizedBox(height: AppDimens.spacing20),
              Row(
                children: [
                  Expanded(
                    child: AppButton(
                      label: 'বাতিল (Reject)',
                      variant: AppButtonVariant.outline,
                      isLoading: _isLoading,
                      onPressed: _isSelfApproval ? null : () => _handleDecision(false),
                    ),
                  ),
                  const SizedBox(width: AppDimens.spacing12),
                  Expanded(
                    child: AppButton(
                      label: 'অনুমোদন (Approve)',
                      isLoading: _isLoading,
                      onPressed: _isSelfApproval ? null : () => _handleDecision(true),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildRow(String label, String value, {bool isBold = false}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: AppTypography.bodySmall.copyWith(color: AppColors.textSecondary)),
          const Spacer(),
          Flexible(
            child: Text(
              value,
              style: AppTypography.bodySmall.copyWith(
                fontWeight: isBold ? FontWeight.bold : FontWeight.w500,
                color: isBold ? AppColors.primary : AppColors.textPrimary,
              ),
              textAlign: TextAlign.right,
            ),
          ),
        ],
      ),
    );
  }
}
