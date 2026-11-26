import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../authentication/providers/auth_provider.dart';
import '../../printing_pdf/services/native_printing_service.dart';
import '../../role_permissions/models/committee_role.dart';
import '../../role_permissions/providers/role_permissions_provider.dart';
import '../models/finance_transaction_model.dart';
import '../providers/finance_provider.dart';

/// Opens a detailed inspection bottom sheet for a voucher/transaction
/// Supports native PDF voucher printing and audited cashless reversal
void showTransactionDetailSheet(
  BuildContext context,
  WidgetRef ref,
  FinanceTransactionModel txn,
) {
  final role = ref.read(currentRoleProvider);
  final user = ref.read(authNotifierProvider).valueOrNull;
  final mosqueName = user?.mosqueNameBn.isNotEmpty == true
      ? user!.mosqueNameBn
      : 'মসজিদ সেবা পোর্টাল';
  final canReverse = !txn.isReversal &&
      txn.approvalStatus != 'rejected' &&
      (role == CommitteeRole.president ||
          role.canApproveFinance() ||
          role.canManageFinance());

  final statusLabel = txn.isReversal
      ? 'সমন্বয়কৃত'
      : txn.isPending
          ? 'অপেক্ষমাণ'
          : (txn.approvalStatus == 'rejected' ? 'প্রত্যাখ্যাত' : 'অনুমোদিত');

  showModalBottomSheet<void>(
    context: context,
    isScrollControlled: true,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (sheetCtx) => Padding(
      padding: EdgeInsets.fromLTRB(
        20,
        16,
        20,
        MediaQuery.of(sheetCtx).viewInsets.bottom + 24,
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Center(
            child: Container(
              width: 40,
              height: 4,
              decoration: BoxDecoration(
                color: Colors.grey.shade400,
                borderRadius: BorderRadius.circular(2),
              ),
            ),
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              CircleAvatar(
                backgroundColor:
                    (txn.isIncome ? AppColors.success : AppColors.error)
                        .withOpacity(0.12),
                child: Icon(
                  txn.isIncome ? Icons.arrow_downward : Icons.arrow_upward,
                  color: txn.isIncome ? AppColors.success : AppColors.error,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      txn.categoryNameBn,
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    Text(
                      'কোড: ${BengaliNumberFormatter.toBengali(txn.transactionCode)}',
                      style: const TextStyle(
                        fontSize: 12,
                        color: AppColors.textSecondary,
                      ),
                    ),
                  ],
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: AppColors.emeraldSoft,
                  borderRadius: BorderRadius.circular(AppDimens.radiusSm),
                ),
                child: Text(
                  statusLabel,
                  style: const TextStyle(
                    fontSize: 11,
                    fontWeight: FontWeight.bold,
                    color: AppColors.emeraldPrimary,
                  ),
                ),
              ),
            ],
          ),
          const Divider(height: 24),
          _detailRow('ধরন', txn.isIncome ? 'আয় (Credit)' : 'ব্যয় (Debit)'),
          _detailRow('পরিমাণ', BengaliNumberFormatter.formatCurrency(txn.amount)),
          _detailRow('তারিখ', BengaliNumberFormatter.toBengali(txn.txnDate)),
          _detailRow('প্রস্তুতকারী', txn.createdBy.isNotEmpty ? txn.createdBy : '—'),
          if (txn.approvedBy != null && txn.approvedBy!.isNotEmpty)
            _detailRow('অনুমোদনকারী', txn.approvedBy!),
          if (txn.description.isNotEmpty) _detailRow('বিবরণ', txn.description),
          const SizedBox(height: 18),
          Row(
            children: [
              Expanded(
                child: OutlinedButton.icon(
                  icon: const Icon(Icons.picture_as_pdf_outlined, size: 18),
                  label: const Text('ভাউচার PDF প্রিন্ট'),
                  onPressed: () async {
                    await NativePrintingService().printVoucher(
                      voucherNo: txn.transactionCode,
                      category: txn.categoryNameBn,
                      description: txn.description.isNotEmpty
                          ? txn.description
                          : txn.categoryNameBn,
                      amount: txn.amount,
                      approvedBy: txn.approvedBy?.isNotEmpty == true
                          ? txn.approvedBy!
                          : txn.createdBy,
                      date: txn.txnDate,
                      mosqueName: mosqueName,
                    );
                  },
                ),
              ),
              if (canReverse) ...[
                const SizedBox(width: 10),
                Expanded(
                  child: ElevatedButton.icon(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.urgentRed,
                      foregroundColor: Colors.white,
                    ),
                    icon: const Icon(Icons.undo, size: 18),
                    label: const Text('সমন্বয় / বাতিল'),
                    onPressed: () {
                      Navigator.pop(sheetCtx);
                      _confirmReverseDialog(
                        context,
                        ref,
                        txn,
                        user?.memberName.isNotEmpty == true
                            ? user!.memberName
                            : role.titleBn,
                      );
                    },
                  ),
                ),
              ],
            ],
          ),
        ],
      ),
    ),
  );
}

Widget _detailRow(String label, String value) {
  return Padding(
    padding: const EdgeInsets.symmetric(vertical: 4),
    child: Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SizedBox(
          width: 110,
          child: Text(
            label,
            style: const TextStyle(fontSize: 12, color: AppColors.textSecondary),
          ),
        ),
        Expanded(
          child: Text(
            value,
            style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
          ),
        ),
      ],
    ),
  );
}

Future<void> _confirmReverseDialog(
  BuildContext context,
  WidgetRef ref,
  FinanceTransactionModel txn,
  String actorName,
) async {
  final reasonCtrl = TextEditingController();
  bool submitting = false;

  await showDialog<void>(
    context: context,
    builder: (dialogCtx) => StatefulBuilder(
      builder: (dialogCtx, setDialogState) => AlertDialog(
        title: const Text('ভাউচার সমন্বয় নিশ্চিত করুন'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'মূল এন্ট্রি মুছে ফেলা হবে না; বরং অডিট ট্রেইল রক্ষার্থে বিপরীত সমন্বয় এন্ট্রি যোগ করা হবে।',
              style: TextStyle(fontSize: 12),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: reasonCtrl,
              maxLines: 2,
              decoration: const InputDecoration(
                labelText: 'সমন্বয়ের কারণ *',
                hintText: 'যেমন: ভুল অংক এন্ট্রি সংশোধন',
                border: OutlineInputBorder(),
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: submitting ? null : () => Navigator.pop(dialogCtx),
            child: const Text('বাতিল'),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.urgentRed,
              foregroundColor: Colors.white,
            ),
            onPressed: submitting
                ? null
                : () async {
                    final reason = reasonCtrl.text.trim();
                    if (reason.isEmpty) {
                      ScaffoldMessenger.of(dialogCtx).showSnackBar(
                        const SnackBar(content: Text('সমন্বয়ের কারণ লিখুন।')),
                      );
                      return;
                    }
                    setDialogState(() => submitting = true);
                    final ok = await ref
                        .read(financeListProvider.notifier)
                        .reverseVoucher(
                          originalTxn: txn,
                          reason: reason,
                          reversedBy: actorName,
                        );
                    if (!dialogCtx.mounted) return;
                    Navigator.pop(dialogCtx);
                    if (!context.mounted) return;
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(
                        content: Text(
                          ok
                              ? 'ভাউচার সফলভাবে সমন্বয় করা হয়েছে।'
                              : 'ভাউচার সমন্বয় করা যায়নি।',
                        ),
                        backgroundColor:
                            ok ? AppColors.emeraldPrimary : AppColors.urgentRed,
                      ),
                    );
                  },
            child: const Text('সমন্বয় করুন'),
          ),
        ],
      ),
    ),
  );

  reasonCtrl.dispose();
}
