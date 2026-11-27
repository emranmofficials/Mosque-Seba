import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../shared/components/error_state_view.dart';
import '../../../shared/widgets/adaptive_scaffold.dart';
import '../../../shared/widgets/card_shell.dart';
import '../../authentication/providers/auth_provider.dart';
import '../../role_permissions/models/committee_role.dart';
import '../../role_permissions/providers/role_permissions_provider.dart';
import '../dialogs/add_transaction_dialog.dart';
import '../dialogs/approve_transaction_dialog.dart';
import '../models/finance_transaction_model.dart';
import '../providers/finance_provider.dart';
import '../widgets/balance_summary_cards.dart';
import '../widgets/finance_analytics_card.dart';
import '../widgets/pending_approval_card.dart';
import '../widgets/transaction_detail_sheet.dart';
import '../widgets/transaction_item_card.dart';

/// Main Financial Dashboard Screen (মসজিদ হিসাব ও আর্থিক তহবিল)
class FinanceDashboardScreen extends ConsumerWidget {
  const FinanceDashboardScreen({super.key});

  void _showAddVoucherDialog(BuildContext context, WidgetRef ref, String defaultType) {
    final authState = ref.read(authNotifierProvider);
    final userName = authState.valueOrNull?.memberName ?? 'কোষাধ্যক্ষ';
    final userRole = authState.valueOrNull?.roleCode ?? 'treasurer';

    showDialog(
      context: context,
      builder: (ctx) => AddTransactionDialog(
        defaultType: defaultType,
        onSubmit: ({
          required String categoryNameBn,
          required String type,
          required double amount,
          required String txnDate,
          required String description,
        }) =>
            ref.read(financeListProvider.notifier).submitVoucher(
                  categoryNameBn: categoryNameBn,
                  type: type,
                  amount: amount,
                  txnDate: txnDate,
                  description: description,
                  createdBy: userName,
                  userRole: userRole,
                ),
      ),
    );
  }

  void _showApproveDialog(BuildContext context, WidgetRef ref, FinanceTransactionModel txn) {
    final authState = ref.read(authNotifierProvider);
    final userName = authState.valueOrNull?.memberName ?? 'সভাপতি';

    showDialog(
      context: context,
      builder: (ctx) => ApproveTransactionDialog(
        transaction: txn,
        currentUserName: userName,
        onReview: ({
          required String transactionId,
          required String reviewerName,
          required String creatorName,
          required bool isApproved,
        }) =>
            ref.read(financeListProvider.notifier).reviewVoucher(
                  transactionId: transactionId,
                  reviewerName: reviewerName,
                  creatorName: creatorName,
                  isApproved: isApproved,
                ),
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final summary = ref.watch(financeSummaryProvider);
    final transactionsAsync = ref.watch(financeAllTransactionsProvider);
    final role = ref.watch(currentRoleProvider);
    final canManageFinance = role == CommitteeRole.president || role.canManageFinance();
    final canApproveFinance = role == CommitteeRole.president || role.canApproveFinance();

    return AdaptiveScaffold(
      title: 'মসজিদ তহবিল ও হিসাব',
      actions: [
        IconButton(
          icon: const Icon(Icons.history),
          tooltip: 'সম্পূর্ণ লেজার',
          onPressed: () => context.push('/finance-transactions'),
        ),
      ],
      body: RefreshIndicator(
        onRefresh: () => ref.read(financeListProvider.notifier).loadTransactions(),
        child: ListView(
          padding: const EdgeInsets.all(AppDimens.spacing16),
          children: [
            // Balance Summary
            BalanceSummaryCards(summary: summary),
            const SizedBox(height: AppDimens.spacing16),
            // Financial Analytics & Category Breakdown
            transactionsAsync.maybeWhen(
              data: (txns) => Column(
                children: [
                  FinanceAnalyticsCard(summary: summary, transactions: txns),
                  const SizedBox(height: AppDimens.spacing16),
                ],
              ),
              orElse: () => const SizedBox.shrink(),
            ),
            // Quick Voucher Buttons Row (Role-protected)
            if (canManageFinance) ...[
              Row(
                children: [
                  Expanded(
                    child: ElevatedButton.icon(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.success,
                        foregroundColor: Colors.white,
                        padding: const EdgeInsets.symmetric(vertical: 12),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusMd)),
                      ),
                      icon: const Icon(Icons.add_circle_outline),
                      label: const Text('আয় ভাউচার (+)', style: TextStyle(fontWeight: FontWeight.bold)),
                      onPressed: () => _showAddVoucherDialog(context, ref, 'income'),
                    ),
                  ),
                  const SizedBox(width: AppDimens.spacing12),
                  Expanded(
                    child: ElevatedButton.icon(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.error,
                        foregroundColor: Colors.white,
                        padding: const EdgeInsets.symmetric(vertical: 12),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusMd)),
                      ),
                      icon: const Icon(Icons.remove_circle_outline),
                      label: const Text('ব্যয় ভাউচার (-)', style: TextStyle(fontWeight: FontWeight.bold)),
                      onPressed: () => _showAddVoucherDialog(context, ref, 'expense'),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: AppDimens.spacing20),
            ],
            // Pending Approvals Section
            transactionsAsync.when(
              loading: () => const SizedBox.shrink(),
              error: (err, _) => ErrorStateView(
                title: 'ভাউচার লোড ব্যর্থ',
                message: err.toString(),
                onRetry: () => ref.read(financeListProvider.notifier).loadTransactions(),
              ),
              data: (txns) {
                final pendingList = txns.where((t) => t.isPending).toList();
                if (pendingList.isEmpty) return const SizedBox.shrink();

                return Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        const Icon(Icons.notification_important, color: AppColors.warning, size: 20),
                        const SizedBox(width: 8),
                        Text(
                          'অনুমোদন অপেক্ষমাণ ভাউচার (${pendingList.length} টি)',
                          style: AppTypography.titleMedium.copyWith(
                            fontWeight: FontWeight.bold,
                            color: AppColors.warning,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: AppDimens.spacing8),
                    ...pendingList.map(
                      (p) => Padding(
                        padding: const EdgeInsets.only(bottom: AppDimens.spacing12),
                        child: PendingApprovalCard(
                          transaction: p,
                          onReview: () {
                            if (!canApproveFinance) {
                              ScaffoldMessenger.of(context).showSnackBar(
                                const SnackBar(
                                  content: Text('ভাউচার অনুমোদনের ক্ষমতা কেবল সভাপতি ও সহ-সভাপতির।'),
                                ),
                              );
                              return;
                            }
                            _showApproveDialog(context, ref, p);
                          },
                        ),
                      ),
                    ),
                    const SizedBox(height: AppDimens.spacing16),
                  ],
                );
              },
            ),
            // Recent Transactions Section Header
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'সাম্প্রতিক ভাউচার ও লেনদেন',
                  style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold),
                ),
                TextButton(
                  onPressed: () => context.push('/finance-transactions'),
                  child: const Text('সব দেখুন'),
                ),
              ],
            ),
            const SizedBox(height: AppDimens.spacing8),
            transactionsAsync.when(
              loading: () => const Center(child: CircularProgressIndicator(color: AppColors.primary)),
              error: (_, __) => const SizedBox.shrink(),
              data: (txns) {
                final recent = txns.take(10).toList();
                if (recent.isEmpty) {
                  return const CardShell(
                    padding: EdgeInsets.all(AppDimens.spacing16),
                    child: Center(child: Text('কোনো আর্থিক লেনদেন রেকর্ড করা হয়নি')),
                  );
                }
                return Column(
                  children: recent
                      .map((t) => Padding(
                            padding: const EdgeInsets.only(bottom: AppDimens.spacing8),
                            child: TransactionItemCard(
                              transaction: t,
                              onTap: () => showTransactionDetailSheet(context, ref, t),
                            ),
                          ))
                      .toList(),
                );
              },
            ),
          ],
        ),
      ),
    );
  }
}
