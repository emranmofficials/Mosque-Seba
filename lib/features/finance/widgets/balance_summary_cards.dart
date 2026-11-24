import 'package:flutter/material.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/utils/bengali_number_formatter.dart';
import '../../../../shared/widgets/card_shell.dart';
import '../repositories/finance_repository.dart';

/// Presentation card displaying Net Balance, Cash Fund, and Bank Balance
class BalanceSummaryCards extends StatelessWidget {
  final FinancialSummary summary;

  const BalanceSummaryCards({super.key, required this.summary});

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        // Net Balance Hero Card
        CardShell(
          padding: const EdgeInsets.all(AppDimens.spacing20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    'মসজিদ তহবিলের মোট স্থিতি',
                    style: AppTypography.bodyMedium.copyWith(color: AppColors.textSecondary),
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                    decoration: BoxDecoration(
                      color: AppColors.primaryContainer,
                      borderRadius: BorderRadius.circular(AppDimens.radiusSm),
                    ),
                    child: Text(
                      'হালনাগাদ হিসাব',
                      style: AppTypography.caption.copyWith(
                        color: AppColors.primary,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: AppDimens.spacing8),
              Text(
                BengaliNumberFormatter.formatCurrency(summary.netBalance),
                style: AppTypography.headlineMedium.copyWith(
                  fontWeight: FontWeight.bold,
                  color: summary.netBalance >= 0 ? AppColors.primary : AppColors.error,
                ),
              ),
              const SizedBox(height: AppDimens.spacing16),
              const Divider(),
              const SizedBox(height: AppDimens.spacing12),
              // Cash and Bank Split
              Row(
                children: [
                  Expanded(
                    child: _buildSplitFund(
                      Icons.account_balance_wallet_outlined,
                      'নগদ ক্যাশ ফান্ড',
                      summary.cashInHand,
                      AppColors.primary,
                    ),
                  ),
                  Container(height: 36, width: 1, color: AppColors.outline),
                  Expanded(
                    child: _buildSplitFund(
                      Icons.account_balance_outlined,
                      'ব্যাংক ব্যালেন্স',
                      summary.bankBalance,
                      AppColors.gold,
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
        const SizedBox(height: AppDimens.spacing12),
        // Income vs Expense Metrics Row
        Row(
          children: [
            Expanded(
              child: CardShell(
                padding: const EdgeInsets.all(AppDimens.spacing12),
                child: Row(
                  children: [
                    CircleAvatar(
                      radius: 18,
                      backgroundColor: AppColors.success.withValues(alpha: 0.15),
                      child: const Icon(Icons.arrow_downward, color: AppColors.success, size: 18),
                    ),
                    const SizedBox(width: AppDimens.spacing8),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('মোট আয়', style: AppTypography.caption.copyWith(color: AppColors.textSecondary)),
                          Text(
                            BengaliNumberFormatter.formatCurrency(summary.totalIncome),
                            style: AppTypography.titleSmall.copyWith(
                              fontWeight: FontWeight.bold,
                              color: AppColors.success,
                            ),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(width: AppDimens.spacing8),
            Expanded(
              child: CardShell(
                padding: const EdgeInsets.all(AppDimens.spacing12),
                child: Row(
                  children: [
                    CircleAvatar(
                      radius: 18,
                      backgroundColor: AppColors.error.withValues(alpha: 0.15),
                      child: const Icon(Icons.arrow_upward, color: AppColors.error, size: 18),
                    ),
                    const SizedBox(width: AppDimens.spacing8),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('মোট ব্যয়', style: AppTypography.caption.copyWith(color: AppColors.textSecondary)),
                          Text(
                            BengaliNumberFormatter.formatCurrency(summary.totalExpense),
                            style: AppTypography.titleSmall.copyWith(
                              fontWeight: FontWeight.bold,
                              color: AppColors.error,
                            ),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildSplitFund(IconData icon, String title, double amount, Color iconColor) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 8),
      child: Row(
        children: [
          Icon(icon, color: iconColor, size: 22),
          const SizedBox(width: 8),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: AppTypography.caption.copyWith(color: AppColors.textSecondary)),
                Text(
                  BengaliNumberFormatter.formatCurrency(amount),
                  style: AppTypography.bodyMedium.copyWith(fontWeight: FontWeight.bold),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
