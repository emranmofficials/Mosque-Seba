import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/widgets/card_shell.dart';

/// KPI Summary Grid displaying Financial & Membership status (KpiCards.kt Parity)
class KpiSummaryGrid extends StatelessWidget {
  final int totalMembers;
  final double cashBalance;
  final double bankBalance;
  final int pendingApprovals;
  final VoidCallback? onFinanceTap;
  final VoidCallback? onMembersTap;

  const KpiSummaryGrid({
    super.key,
    this.totalMembers = 0,
    this.cashBalance = 0.0,
    this.bankBalance = 0.0,
    this.pendingApprovals = 0,
    this.onFinanceTap,
    this.onMembersTap,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        // Cash Balance Card
        Expanded(
          child: CardShell(
            onTap: onFinanceTap,
            padding: const EdgeInsets.all(12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text('নগদ ক্যাশ', style: TextStyle(fontSize: 11, color: AppColors.textSecondary)),
                    Container(
                      padding: const EdgeInsets.all(4),
                      decoration: const BoxDecoration(shape: BoxShape.circle, color: AppColors.emeraldSoft),
                      child: const Icon(Icons.account_balance_wallet_outlined, size: 14, color: AppColors.emeraldPrimary),
                    ),
                  ],
                ),
                const SizedBox(height: 6),
                Text(
                  BengaliNumberFormatter.formatCurrency(cashBalance),
                  style: const TextStyle(fontSize: 15, fontWeight: FontWeight.bold, color: AppColors.emeraldPrimary),
                ),
              ],
            ),
          ),
        ),

        const SizedBox(width: MosqueSpace.sm),

        // Bank Balance Card
        Expanded(
          child: CardShell(
            onTap: onFinanceTap,
            padding: const EdgeInsets.all(12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text('ব্যাংক জমা', style: TextStyle(fontSize: 11, color: AppColors.textSecondary)),
                    Container(
                      padding: const EdgeInsets.all(4),
                      decoration: const BoxDecoration(shape: BoxShape.circle, color: AppColors.goldLight),
                      child: const Icon(Icons.account_balance_outlined, size: 14, color: AppColors.goldDark),
                    ),
                  ],
                ),
                const SizedBox(height: 6),
                Text(
                  BengaliNumberFormatter.formatCurrency(bankBalance),
                  style: const TextStyle(fontSize: 15, fontWeight: FontWeight.bold, color: AppColors.goldDark),
                ),
              ],
            ),
          ),
        ),

        const SizedBox(width: MosqueSpace.sm),

        // Total Members Card
        Expanded(
          child: CardShell(
            onTap: onMembersTap,
            padding: const EdgeInsets.all(12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text('মোট সদস্য', style: TextStyle(fontSize: 11, color: AppColors.textSecondary)),
                    Container(
                      padding: const EdgeInsets.all(4),
                      decoration: BoxDecoration(shape: BoxShape.circle, color: AppColors.infoBlue.withOpacity(0.12)),
                      child: const Icon(Icons.people_outline, size: 14, color: AppColors.infoBlue),
                    ),
                  ],
                ),
                const SizedBox(height: 6),
                Text(
                  '${BengaliNumberFormatter.toBengaliDigits(totalMembers)} জন',
                  style: const TextStyle(fontSize: 15, fontWeight: FontWeight.bold),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}
