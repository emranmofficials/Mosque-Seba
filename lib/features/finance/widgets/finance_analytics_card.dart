import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/widgets/card_shell.dart';
import '../models/finance_transaction_model.dart';
import '../repositories/finance_repository.dart';

/// Visual Financial Analytics & Ratio Breakdown Card (আয়-ব্যয় বিশ্লেষণ ও চার্ট)
class FinanceAnalyticsCard extends StatelessWidget {
  final FinancialSummary summary;
  final List<FinanceTransactionModel> transactions;

  const FinanceAnalyticsCard({
    super.key,
    required this.summary,
    required this.transactions,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    final double totalFlow = summary.totalIncome + summary.totalExpense;
    final double incomePercent = totalFlow > 0 ? (summary.totalIncome / totalFlow) * 100 : 50.0;
    final double expensePercent = totalFlow > 0 ? (summary.totalExpense / totalFlow) * 100 : 50.0;

    // Aggregate category totals
    final Map<String, double> incomeCategories = {};
    final Map<String, double> expenseCategories = {};

    for (final t in transactions) {
      if (t.isIncome) {
        incomeCategories[t.categoryNameBn] = (incomeCategories[t.categoryNameBn] ?? 0.0) + t.amount;
      } else {
        expenseCategories[t.categoryNameBn] = (expenseCategories[t.categoryNameBn] ?? 0.0) + t.amount;
      }
    }

    final sortedIncome = incomeCategories.entries.toList()
      ..sort((a, b) => b.value.compareTo(a.value));
    final sortedExpense = expenseCategories.entries.toList()
      ..sort((a, b) => b.value.compareTo(a.value));

    return CardShell(
      padding: const EdgeInsets.all(AppDimens.spacing16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(6),
                    decoration: BoxDecoration(
                      color: AppColors.emeraldSoft,
                      shape: BoxShape.circle,
                    ),
                    child: const Icon(
                      Icons.pie_chart_outline,
                      size: 18,
                      color: AppColors.emeraldPrimary,
                    ),
                  ),
                  const SizedBox(width: 8),
                  Text(
                    'আর্থিক অনুপাত ও খাতওয়ারি বিশ্লেষণ',
                    style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold),
                  ),
                ],
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                decoration: BoxDecoration(
                  color: isDark ? AppColors.nightSurfaceVariant : AppColors.bgLight,
                  borderRadius: BorderRadius.circular(AppDimens.radiusSm),
                  border: Border.all(
                    color: isDark ? AppColors.nightBorder : AppColors.cardBorder,
                  ),
                ),
                child: Text(
                  'চলতি মাস',
                  style: TextStyle(
                    fontSize: 11,
                    fontWeight: FontWeight.w600,
                    color: isDark ? AppColors.nightTextSecondary : AppColors.textSecondary,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),

          // Income vs Expense Visual Comparison Bar
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Row(
                    children: [
                      Container(width: 8, height: 8, decoration: const BoxDecoration(shape: BoxShape.circle, color: AppColors.success)),
                      const SizedBox(width: 4),
                      Text(
                        'আয়: ${BengaliNumberFormatter.toBengaliDigits(incomePercent.toStringAsFixed(0))}%',
                        style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: AppColors.success),
                      ),
                    ],
                  ),
                  Row(
                    children: [
                      Container(width: 8, height: 8, decoration: const BoxDecoration(shape: BoxShape.circle, color: AppColors.urgentRed)),
                      const SizedBox(width: 4),
                      Text(
                        'ব্যয়: ${BengaliNumberFormatter.toBengaliDigits(expensePercent.toStringAsFixed(0))}%',
                        style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: AppColors.urgentRed),
                      ),
                    ],
                  ),
                ],
              ),
              const SizedBox(height: 6),
              ClipRRect(
                borderRadius: BorderRadius.circular(6),
                child: SizedBox(
                  height: 12,
                  child: Row(
                    children: [
                      Expanded(
                        flex: totalFlow > 0 ? (incomePercent * 10).round().clamp(1, 1000).toInt() : 1,
                        child: Container(color: AppColors.success),
                      ),
                      const SizedBox(width: 2),
                      Expanded(
                        flex: totalFlow > 0 ? (expensePercent * 10).round().clamp(1, 1000).toInt() : 1,
                        child: Container(color: AppColors.urgentRed),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          const Divider(),
          const SizedBox(height: 12),

          // Top Income / Expense Category Highlights
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Top Incomes
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'শীর্ষ আয়ের খাত',
                      style: TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.bold,
                        color: isDark ? AppColors.nightTextPrimary : AppColors.textPrimary,
                      ),
                    ),
                    const SizedBox(height: 8),
                    if (sortedIncome.isEmpty)
                      const Text('কোনো আয়ের এন্ট্রি নেই', style: TextStyle(fontSize: 11, color: Colors.grey))
                    else
                      ...sortedIncome.take(3).map((e) {
                        final double share = summary.totalIncome > 0 ? (e.value / summary.totalIncome) : 0;
                        return Padding(
                          padding: const EdgeInsets.only(bottom: 8),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                children: [
                                  Flexible(
                                    child: Text(
                                      e.key,
                                      style: const TextStyle(fontSize: 11),
                                      maxLines: 1,
                                      overflow: TextOverflow.ellipsis,
                                    ),
                                  ),
                                  Text(
                                    BengaliNumberFormatter.formatCurrency(e.value),
                                    style: const TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: AppColors.success),
                                  ),
                                ],
                              ),
                              const SizedBox(height: 3),
                              LinearProgressIndicator(
                                value: share.clamp(0.0, 1.0).toDouble(),
                                backgroundColor: isDark ? AppColors.nightSurfaceVariant : Colors.grey.shade200,
                                valueColor: const AlwaysStoppedAnimation<Color>(AppColors.success),
                                minHeight: 4,
                                borderRadius: BorderRadius.circular(2),
                              ),
                            ],
                          ),
                        );
                      }),
                  ],
                ),
              ),
              const SizedBox(width: 16),
              // Top Expenses
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'শীর্ষ ব্যয়ের খাত',
                      style: TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.bold,
                        color: isDark ? AppColors.nightTextPrimary : AppColors.textPrimary,
                      ),
                    ),
                    const SizedBox(height: 8),
                    if (sortedExpense.isEmpty)
                      const Text('কোনো ব্যয়ের এন্ট্রি নেই', style: TextStyle(fontSize: 11, color: Colors.grey))
                    else
                      ...sortedExpense.take(3).map((e) {
                        final double share = summary.totalExpense > 0 ? (e.value / summary.totalExpense) : 0;
                        return Padding(
                          padding: const EdgeInsets.only(bottom: 8),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                children: [
                                  Flexible(
                                    child: Text(
                                      e.key,
                                      style: const TextStyle(fontSize: 11),
                                      maxLines: 1,
                                      overflow: TextOverflow.ellipsis,
                                    ),
                                  ),
                                  Text(
                                    BengaliNumberFormatter.formatCurrency(e.value),
                                    style: const TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: AppColors.urgentRed),
                                  ),
                                ],
                              ),
                              const SizedBox(height: 3),
                              LinearProgressIndicator(
                                value: share.clamp(0.0, 1.0).toDouble(),
                                backgroundColor: isDark ? AppColors.nightSurfaceVariant : Colors.grey.shade200,
                                valueColor: const AlwaysStoppedAnimation<Color>(AppColors.urgentRed),
                                minHeight: 4,
                                borderRadius: BorderRadius.circular(2),
                              ),
                            ],
                          ),
                        );
                      }),
                  ],
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
