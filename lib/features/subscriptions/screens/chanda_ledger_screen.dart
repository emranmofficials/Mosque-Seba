import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/utils/bengali_number_formatter.dart';
import '../../../../shared/components/empty_state_view.dart';
import '../../../../shared/components/error_state_view.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../../../shared/widgets/card_shell.dart';
import '../../membership/models/member_model.dart';
import '../../membership/providers/member_provider.dart';
import '../dialogs/receive_chanda_dialog.dart';
import '../models/chanda_payment_model.dart';
import '../providers/chanda_provider.dart';
import '../widgets/chanda_status_badge.dart';
import '../widgets/qr_receipt_widget.dart';

/// Screen managing monthly Chanda subscriptions and receipt history (চাঁদা লেজার)
class ChandaLedgerScreen extends ConsumerWidget {
  final MemberModel? targetMember;

  const ChandaLedgerScreen({super.key, this.targetMember});

  void _showReceiveDialog(BuildContext context, WidgetRef ref, List<MemberModel> members) async {
    final result = await showDialog<ChandaPaymentModel>(
      context: context,
      builder: (ctx) => ReceiveChandaDialog(
        initialMember: targetMember,
        members: members,
        onReceive: ({
          required String memberId,
          required String memberNameBn,
          required String month,
          required double amount,
          required String method,
          required String receivedBy,
        }) =>
            ref.read(chandaListProvider.notifier).receivePayment(
                  memberId: memberId,
                  memberNameBn: memberNameBn,
                  month: month,
                  amount: amount,
                  method: method,
                  receivedBy: receivedBy,
                ),
      ),
    );

    if (result != null && context.mounted) {
      _showReceiptPreview(context, result);
    }
  }

  void _showReceiptPreview(BuildContext context, ChandaPaymentModel payment) {
    showDialog(
      context: context,
      builder: (ctx) => Dialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusLg)),
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 420),
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                QrReceiptWidget(payment: payment),
                Padding(
                  padding: const EdgeInsets.all(AppDimens.spacing12),
                  child: TextButton.icon(
                    icon: const Icon(Icons.close),
                    label: const Text('বন্ধ করুন'),
                    onPressed: () => Navigator.of(ctx).pop(),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final paymentsAsync = ref.watch(chandaListProvider);
    final membersAsync = ref.watch(memberListProvider);
    final members = membersAsync.asData?.value ?? [];

    return AdaptiveScaffold(
      title: 'মাসিক চাঁদা লেজার ও আদায়',
      floatingActionButton: FloatingActionButton.extended(
        backgroundColor: AppColors.primary,
        foregroundColor: AppColors.onPrimary,
        icon: const Icon(Icons.add_card),
        label: const Text('চাঁদা জমা নিন', style: TextStyle(fontWeight: FontWeight.bold)),
        onPressed: () => _showReceiveDialog(context, ref, members),
      ),
      body: paymentsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator(color: AppColors.primary)),
        error: (err, _) => ErrorStateView(
          title: 'তথ্য লোড করতে সমস্যা হয়েছে',
          message: err.toString(),
          onRetry: () => ref.read(chandaListProvider.notifier).loadPayments(),
        ),
        data: (payments) {
          final totalCollected = payments.fold<double>(0.0, (acc, p) => acc + p.amountPaid);

          return ListView(
            padding: const EdgeInsets.all(AppDimens.spacing16),
            children: [
              // Summary card
              CardShell(
                padding: const EdgeInsets.all(AppDimens.spacing16),
                child: Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(AppDimens.spacing10),
                      decoration: BoxDecoration(
                        color: AppColors.primaryContainer,
                        borderRadius: BorderRadius.circular(AppDimens.radiusMd),
                      ),
                      child: const Icon(Icons.savings_outlined, color: AppColors.primary, size: 28),
                    ),
                    const SizedBox(width: AppDimens.spacing12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'মোট আদায়কৃত চাঁদা',
                            style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
                          ),
                          const SizedBox(height: 2),
                          Text(
                            BengaliNumberFormatter.formatCurrency(totalCollected),
                            style: AppTypography.titleLarge.copyWith(
                              fontWeight: FontWeight.bold,
                              color: AppColors.primary,
                            ),
                          ),
                        ],
                      ),
                    ),
                    Text(
                      '${BengaliNumberFormatter.toBengali(payments.length)} টি রসিদ',
                      style: AppTypography.caption.copyWith(
                        fontWeight: FontWeight.bold,
                        color: AppColors.textSecondary,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: AppDimens.spacing16),
              Text(
                'সাম্প্রতিক আদায় তালিকা',
                style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: AppDimens.spacing12),
              if (payments.isEmpty)
                const EmptyStateView(
                  title: 'কোনো চাঁদা আদায়ের রেকর্ড নেই',
                  message: 'এখনও কোনো চাঁদা সংগ্রহ করা হয়নি। নিচে চাপ দিয়ে নতুন চাঁদা জমা নিন।',
                )
              else
                ...payments.map(
                  (payment) => Padding(
                    padding: const EdgeInsets.only(bottom: AppDimens.spacing12),
                    child: CardShell(
                      onTap: () => _showReceiptPreview(context, payment),
                      padding: const EdgeInsets.all(AppDimens.spacing12),
                      child: Row(
                        children: [
                          CircleAvatar(
                            radius: 20,
                            backgroundColor: AppColors.primaryContainer,
                            child: const Icon(Icons.receipt, color: AppColors.primary, size: 20),
                          ),
                          const SizedBox(width: AppDimens.spacing12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Row(
                                  children: [
                                    Text(
                                      payment.memberNameBn,
                                      style: AppTypography.bodyMedium.copyWith(fontWeight: FontWeight.bold),
                                    ),
                                    const SizedBox(width: 8),
                                    ChandaStatusBadge(status: payment.status),
                                  ],
                                ),
                                const SizedBox(height: 2),
                                Text(
                                  'মাস: ${payment.month} • রসিদ: ${BengaliNumberFormatter.toBengali(payment.receiptCode)}',
                                  style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
                                ),
                              ],
                            ),
                          ),
                          Column(
                            crossAxisAlignment: CrossAxisAlignment.end,
                            children: [
                              Text(
                                BengaliNumberFormatter.formatCurrency(payment.amountPaid),
                                style: AppTypography.titleMedium.copyWith(
                                  fontWeight: FontWeight.bold,
                                  color: AppColors.primary,
                                ),
                              ),
                              Text(
                                BengaliNumberFormatter.toBengali(payment.paidAt),
                                style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
            ],
          );
        },
      ),
    );
  }
}
