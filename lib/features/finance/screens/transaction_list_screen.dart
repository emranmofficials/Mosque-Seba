import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../shared/components/empty_state_view.dart';
import '../../../shared/components/error_state_view.dart';
import '../../../shared/widgets/adaptive_scaffold.dart';
import '../providers/finance_provider.dart';
import '../widgets/transaction_detail_sheet.dart';
import '../widgets/transaction_item_card.dart';

/// Complete General Ledger Screen (সম্পূর্ণ আর্থিক লেজার ও খতিয়ান)
class TransactionListScreen extends ConsumerWidget {
  const TransactionListScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final transactionsAsync = ref.watch(financeListProvider);
    final currentTypeFilter = ref.watch(financeTypeFilterProvider);
    final currentStatusFilter = ref.watch(financeStatusFilterProvider);

    return AdaptiveScaffold(
      title: 'সম্পূর্ণ লেনদেন লেজার',
      body: Column(
        children: [
          // Filter Tabs (সকল, আয়, ব্যয় + অনুমোদন অবস্থা)
          Container(
            color: AppColors.surface,
            padding: const EdgeInsets.symmetric(horizontal: AppDimens.spacing16, vertical: 8),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                SingleChildScrollView(
                  scrollDirection: Axis.horizontal,
                  child: Row(
                    children: [
                      _buildTypeChip(ref, 'সকল', 'all', currentTypeFilter),
                      const SizedBox(width: 8),
                      _buildTypeChip(ref, 'শুধুমাত্র আয় (+)', 'income', currentTypeFilter),
                      const SizedBox(width: 8),
                      _buildTypeChip(ref, 'শুধুমাত্র ব্যয় (-)', 'expense', currentTypeFilter),
                    ],
                  ),
                ),
                const SizedBox(height: 6),
                SingleChildScrollView(
                  scrollDirection: Axis.horizontal,
                  child: Row(
                    children: [
                      _buildStatusChip(ref, 'সব অবস্থা', 'all', currentStatusFilter),
                      const SizedBox(width: 8),
                      _buildStatusChip(ref, 'অনুমোদিত', 'approved', currentStatusFilter),
                      const SizedBox(width: 8),
                      _buildStatusChip(ref, 'অপেক্ষমাণ', 'pending', currentStatusFilter),
                      const SizedBox(width: 8),
                      _buildStatusChip(ref, 'প্রত্যাখ্যাত', 'rejected', currentStatusFilter),
                    ],
                  ),
                ),
              ],
            ),
          ),
          Expanded(
            child: transactionsAsync.when(
              loading: () => const Center(child: CircularProgressIndicator(color: AppColors.primary)),
              error: (err, _) => ErrorStateView(
                title: 'তথ্য লোড করতে সমস্যা হয়েছে',
                message: err.toString(),
                onRetry: () => ref.read(financeListProvider.notifier).loadTransactions(),
              ),
              data: (txns) {
                if (txns.isEmpty) {
                  return const EmptyStateView(
                    title: 'কোনো লেনদেন পাওয়া যায়নি',
                    message: 'এই ফিল্টারে কোনো ভাউচার লিপিবদ্ধ করা হয়নি।',
                  );
                }

                return RefreshIndicator(
                  onRefresh: () => ref.read(financeListProvider.notifier).loadTransactions(),
                  child: ListView.builder(
                    padding: const EdgeInsets.all(AppDimens.spacing16),
                    itemCount: txns.length,
                    itemBuilder: (context, index) {
                      final txn = txns[index];
                      return Padding(
                        padding: const EdgeInsets.only(bottom: AppDimens.spacing8),
                        child: TransactionItemCard(
                          transaction: txn,
                          onTap: () => showTransactionDetailSheet(context, ref, txn),
                        ),
                      );
                    },
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTypeChip(WidgetRef ref, String label, String value, String currentValue) {
    final isSelected = value == currentValue;
    return ChoiceChip(
      label: Text(label),
      selected: isSelected,
      selectedColor: AppColors.primaryContainer,
      labelStyle: TextStyle(
        color: isSelected ? AppColors.primary : AppColors.textPrimary,
        fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
        fontSize: 12,
      ),
      onSelected: (selected) {
        if (selected) {
          ref.read(financeTypeFilterProvider.notifier).state = value;
          ref.read(financeListProvider.notifier).loadTransactions();
        }
      },
    );
  }

  Widget _buildStatusChip(WidgetRef ref, String label, String value, String currentValue) {
    final isSelected = value == currentValue;
    return ChoiceChip(
      label: Text(label),
      selected: isSelected,
      selectedColor: AppColors.goldLight,
      labelStyle: TextStyle(
        color: isSelected ? AppColors.goldDark : AppColors.textSecondary,
        fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
        fontSize: 11,
      ),
      onSelected: (selected) {
        if (selected) {
          ref.read(financeStatusFilterProvider.notifier).state = value;
          ref.read(financeListProvider.notifier).loadTransactions();
        }
      },
    );
  }
}
