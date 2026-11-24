import 'package:flutter/material.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/utils/bengali_number_formatter.dart';
import '../../../../shared/widgets/card_shell.dart';
import '../models/finance_transaction_model.dart';

/// Single item representation in the general ledger list
class TransactionItemCard extends StatelessWidget {
  final FinanceTransactionModel transaction;
  final VoidCallback? onTap;

  const TransactionItemCard({
    super.key,
    required this.transaction,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final isIncome = transaction.isIncome;
    final amountColor = isIncome ? AppColors.success : AppColors.error;

    return CardShell(
      onTap: onTap,
      padding: const EdgeInsets.all(AppDimens.spacing12),
      child: Row(
        children: [
          CircleAvatar(
            radius: 20,
            backgroundColor: amountColor.withValues(alpha: 0.12),
            child: Icon(
              isIncome ? Icons.arrow_downward : Icons.arrow_upward,
              color: amountColor,
              size: 20,
            ),
          ),
          const SizedBox(width: AppDimens.spacing12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Flexible(
                      child: Text(
                        transaction.categoryNameBn,
                        style: AppTypography.bodyMedium.copyWith(fontWeight: FontWeight.bold),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                    if (transaction.isReversal) ...[
                      const SizedBox(width: 6),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 5, vertical: 1),
                        decoration: BoxDecoration(
                          color: AppColors.error.withValues(alpha: 0.12),
                          borderRadius: BorderRadius.circular(3),
                        ),
                        child: Text(
                          'সমন্বয়',
                          style: AppTypography.caption.copyWith(color: AppColors.error, fontSize: 10),
                        ),
                      ),
                    ],
                  ],
                ),
                const SizedBox(height: 2),
                Text(
                  '${BengaliNumberFormatter.toBengali(transaction.txnDate)} • ${BengaliNumberFormatter.toBengali(transaction.transactionCode)}',
                  style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
                ),
              ],
            ),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Text(
                '${isIncome ? '+' : '-'} ${BengaliNumberFormatter.formatCurrency(transaction.amount)}',
                style: AppTypography.titleMedium.copyWith(
                  fontWeight: FontWeight.bold,
                  color: amountColor,
                ),
              ),
              if (transaction.isPending)
                Text(
                  'অপেক্ষমাণ',
                  style: AppTypography.caption.copyWith(color: AppColors.warning, fontWeight: FontWeight.bold),
                )
              else
                Text(
                  transaction.approvalStatus == 'rejected' ? 'প্রত্যাখ্যাত' : 'অনুমোদিত',
                  style: AppTypography.caption.copyWith(
                    color: transaction.approvalStatus == 'rejected' ? AppColors.error : AppColors.textSecondary,
                  ),
                ),
            ],
          ),
        ],
      ),
    );
  }
}
