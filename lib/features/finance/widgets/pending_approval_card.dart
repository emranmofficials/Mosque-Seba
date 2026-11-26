import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/widgets/card_shell.dart';
import '../models/finance_transaction_model.dart';

/// Card alerting committee executives about pending transaction vouchers awaiting review
class PendingApprovalCard extends StatelessWidget {
  final FinanceTransactionModel transaction;
  final VoidCallback onReview;

  const PendingApprovalCard({
    super.key,
    required this.transaction,
    required this.onReview,
  });

  @override
  Widget build(BuildContext context) {
    return CardShell(
      padding: const EdgeInsets.all(AppDimens.spacing16),
      border: Border.all(color: AppColors.warning.withValues(alpha: 0.5), width: 1.2),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(
                  color: AppColors.warning.withValues(alpha: 0.15),
                  borderRadius: BorderRadius.circular(4),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Icon(Icons.pending_actions, size: 14, color: AppColors.warning),
                    const SizedBox(width: 4),
                    Text(
                      'অনুমোদন অপেক্ষমাণ',
                      style: AppTypography.caption.copyWith(
                        color: AppColors.warning,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
              ),
              const Spacer(),
              Text(
                BengaliNumberFormatter.toBengali(transaction.txnDate),
                style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
              ),
            ],
          ),
          const SizedBox(height: AppDimens.spacing8),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Expanded(
                child: Text(
                  transaction.categoryNameBn,
                  style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold),
                ),
              ),
              Text(
                BengaliNumberFormatter.formatCurrency(transaction.amount),
                style: AppTypography.titleLarge.copyWith(
                  fontWeight: FontWeight.bold,
                  color: transaction.isIncome ? AppColors.success : AppColors.error,
                ),
              ),
            ],
          ),
          if (transaction.description.isNotEmpty) ...[
            const SizedBox(height: 4),
            Text(
              transaction.description,
              style: AppTypography.bodySmall.copyWith(color: AppColors.textSecondary),
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
            ),
          ],
          const SizedBox(height: AppDimens.spacing8),
          Text(
            'দাখিলকারী: ${transaction.createdBy}',
            style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
          ),
          const SizedBox(height: AppDimens.spacing12),
          Align(
            alignment: Alignment.centerRight,
            child: ElevatedButton.icon(
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.primary,
                foregroundColor: AppColors.onPrimary,
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSm)),
              ),
              icon: const Icon(Icons.check_circle_outline, size: 16),
              label: const Text('যাচাই ও সিদ্ধান্ত দিন'),
              onPressed: onReview,
            ),
          ),
        ],
      ),
    );
  }
}
