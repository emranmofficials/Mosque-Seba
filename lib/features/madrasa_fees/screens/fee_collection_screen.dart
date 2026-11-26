import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/components/empty_state_view.dart';
import '../../../shared/widgets/card_shell.dart';
import '../../authentication/providers/auth_provider.dart';
import '../../printing_pdf/dialogs/thermal_printer_dialog.dart';
import '../../printing_pdf/services/native_printing_service.dart';
import '../dialogs/collect_student_fee_dialog.dart';
import '../models/madrasa_fee_payment_model.dart';
import '../providers/madrasa_fee_provider.dart';

/// Screen listing Student Fee Collections with live PDF/Thermal Receipt Printing
class FeeCollectionScreen extends ConsumerWidget {
  const FeeCollectionScreen({super.key});

  void _showCollectFeeDialog(BuildContext context, WidgetRef ref) {
    showDialog<void>(
      context: context,
      builder: (ctx) => CollectStudentFeeDialog(
        onSubmit: (payment) =>
            ref.read(madrasaFeeListProvider.notifier).collectFee(payment),
      ),
    );
  }

  void _showReceiptDialog(
    BuildContext context,
    WidgetRef ref,
    MadrasaFeePaymentModel payment,
  ) {
    final printingService = NativePrintingService();
    final mosqueName =
        ref.read(authNotifierProvider).valueOrNull?.mosqueNameBn ?? '';
    final resolvedMosque = mosqueName.isNotEmpty
        ? '$mosqueName — মাদরাসা বিভাগ'
        : 'মসজিদ সেবা মাদরাসা ও মক্তব';

    showDialog<void>(
      context: context,
      builder: (ctx) => Dialog(
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppDimens.radiusCard),
        ),
        child: Padding(
          padding: const EdgeInsets.all(AppDimens.spacing20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(
                Icons.check_circle,
                color: AppColors.emeraldPrimary,
                size: 48,
              ),
              const SizedBox(height: 8),
              const Text(
                'মাদরাসা ফি আদায়ের ডিজিটাল রশিদ',
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 4),
              Text(
                'রশিদ নম্বর: ${payment.receiptCode}',
                style: TextStyle(fontSize: 12, color: Colors.grey.shade600),
              ),
              const Divider(height: 24),
              _buildReceiptRow('শিক্ষার্থীর নাম', payment.studentNameBn),
              _buildReceiptRow('বিভাগ/শ্রেণী', payment.className),
              _buildReceiptRow('মাসের নাম', payment.month),
              _buildReceiptRow(
                'পরিশোধিত অর্থ',
                '৳ ${BengaliNumberFormatter.toBengali(payment.amountPaid.toInt().toString())}',
              ),
              _buildReceiptRow(
                'পেমেন্ট মাধ্যম',
                payment.paymentMethod == 'cash' ? 'নগদ' : payment.paymentMethod,
              ),
              _buildReceiptRow('আদায়কারী', payment.receivedBy),
              _buildReceiptRow(
                'তারিখ',
                BengaliNumberFormatter.toBengali(payment.paidAt),
              ),
              const Divider(height: 24),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                alignment: WrapAlignment.end,
                children: [
                  TextButton(
                    onPressed: () => Navigator.of(ctx).pop(),
                    child: const Text('বন্ধ করুন'),
                  ),
                  OutlinedButton.icon(
                    onPressed: () async {
                      final bytes = await printingService.buildReceiptPdf(
                        receiptNo: payment.receiptCode,
                        memberName: payment.studentNameBn,
                        memberCode: payment.className,
                        month: payment.month,
                        amount: payment.amountPaid,
                        receivedBy: payment.receivedBy,
                        date: payment.paidAt,
                        titleBn: 'মাদরাসা শিক্ষার্থী ফি আদায়ের রসিদ',
                        paymentMethod: payment.paymentMethod,
                        mosqueName: resolvedMosque,
                      );
                      await printingService.shareDocument(
                        bytes,
                        'Madrasa_Fee_${payment.receiptCode}.pdf',
                      );
                    },
                    icon: const Icon(Icons.share_outlined, size: 16),
                    label: const Text('শেয়ার'),
                  ),
                  OutlinedButton.icon(
                    onPressed: () => showThermalPrinterDialog(
                      context,
                      buildPdf: () => printingService.buildReceiptPdf(
                        receiptNo: payment.receiptCode,
                        memberName: payment.studentNameBn,
                        memberCode: payment.className,
                        month: payment.month,
                        amount: payment.amountPaid,
                        receivedBy: payment.receivedBy,
                        date: payment.paidAt,
                        titleBn: 'মাদরাসা শিক্ষার্থী ফি রসিদ',
                        paymentMethod: payment.paymentMethod,
                        mosqueName: resolvedMosque,
                      ),
                    ),
                    icon: const Icon(Icons.bluetooth_connected, size: 16),
                    label: const Text('থার্মাল'),
                  ),
                  ElevatedButton.icon(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.emeraldPrimary,
                    ),
                    onPressed: () {
                      Navigator.of(ctx).pop();
                      printingService.printReceipt(
                        receiptNo: payment.receiptCode,
                        memberName: payment.studentNameBn,
                        memberCode: payment.className,
                        month: payment.month,
                        amount: payment.amountPaid,
                        receivedBy: payment.receivedBy,
                        date: payment.paidAt,
                        titleBn: 'মাদরাসা শিক্ষার্থী ফি আদায়ের রসিদ',
                        paymentMethod: payment.paymentMethod,
                        mosqueName: resolvedMosque,
                      );
                    },
                    icon: const Icon(Icons.print, color: Colors.white, size: 16),
                    label: const Text(
                      'প্রিন্ট PDF',
                      style: TextStyle(color: Colors.white),
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

  Widget _buildReceiptRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: const TextStyle(fontSize: 13, color: Colors.grey)),
          Text(value, style: const TextStyle(fontSize: 13, fontWeight: FontWeight.bold)),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final feesAsync = ref.watch(madrasaFeeListProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('মাদরাসা ফি আদায় ও হিসাব'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () => ref.read(madrasaFeeListProvider.notifier).loadFeePayments(),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        backgroundColor: AppColors.roleTreasurer,
        foregroundColor: Colors.white,
        icon: const Icon(Icons.add_shopping_cart),
        label: const Text('ফি আদায় করুন', style: TextStyle(fontWeight: FontWeight.bold)),
        onPressed: () => _showCollectFeeDialog(context, ref),
      ),
      body: feesAsync.when(
        loading: () => const Center(child: CircularProgressIndicator(color: AppColors.roleTreasurer)),
        error: (err, _) => Center(child: Text('ত্রুটি: $err')),
        data: (payments) {
          if (payments.isEmpty) {
            return EmptyStateView(
              title: 'কোনো ফি আদায়ের রেকর্ড নেই',
              message: 'শিক্ষার্থীদের মাসিক বেতন ও ফি সংগ্রহ করতে চাপ দিন।',
              actionLabel: 'ফি আদায় করুন',
              onAction: () => _showCollectFeeDialog(context, ref),
            );
          }

          final totalCollected = payments.fold<double>(0.0, (acc, p) => acc + p.amountPaid);

          return Column(
            children: [
              // Total Collected Banner
              Container(
                margin: const EdgeInsets.all(AppDimens.spacing14),
                padding: const EdgeInsets.all(AppDimens.spacing14),
                decoration: BoxDecoration(
                  color: AppColors.roleTreasurer.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(AppDimens.radiusCard),
                  border: Border.all(color: AppColors.roleTreasurer.withOpacity(0.3)),
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text('মোট আদায়কৃত ফি', style: TextStyle(fontSize: 12, color: Colors.grey)),
                        Text(
                          '৳ ${BengaliNumberFormatter.toBengali(totalCollected.toInt().toString())}',
                          style: const TextStyle(
                            fontSize: 20,
                            fontWeight: FontWeight.bold,
                            color: AppColors.roleTreasurer,
                          ),
                        ),
                      ],
                    ),
                    const Icon(Icons.account_balance_wallet, color: AppColors.roleTreasurer, size: 36),
                  ],
                ),
              ),

              // Payments List
              Expanded(
                child: ListView.builder(
                  padding: const EdgeInsets.symmetric(horizontal: AppDimens.spacing14),
                  itemCount: payments.length,
                  itemBuilder: (context, index) {
                    final payment = payments[index];
                    return Padding(
                      padding: const EdgeInsets.only(bottom: AppDimens.spacing10),
                      child: CardShell(
                        onTap: () => _showReceiptDialog(context, ref, payment),
                        padding: const EdgeInsets.all(AppDimens.spacing14),
                        child: Row(
                          children: [
                            Container(
                              padding: const EdgeInsets.all(10),
                              decoration: BoxDecoration(
                                color: AppColors.emeraldPrimary.withOpacity(0.12),
                                shape: BoxShape.circle,
                              ),
                              child: const Icon(Icons.receipt, color: AppColors.emeraldPrimary, size: 22),
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    payment.studentNameBn,
                                    style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15),
                                  ),
                                  const SizedBox(height: 2),
                                  Text(
                                    '${payment.className} • ${payment.month}',
                                    style: TextStyle(fontSize: 12, color: Colors.grey.shade600),
                                  ),
                                ],
                              ),
                            ),
                            Column(
                              crossAxisAlignment: CrossAxisAlignment.end,
                              children: [
                                Text(
                                  '৳ ${BengaliNumberFormatter.toBengali(payment.amountPaid.toInt().toString())}',
                                  style: const TextStyle(
                                    fontSize: 16,
                                    fontWeight: FontWeight.bold,
                                    color: AppColors.emeraldPrimary,
                                  ),
                                ),
                                const SizedBox(height: 2),
                                Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                  decoration: BoxDecoration(
                                    color: AppColors.emeraldPrimary.withOpacity(0.12),
                                    borderRadius: BorderRadius.circular(4),
                                  ),
                                  child: const Text(
                                    'পরিশোধিত',
                                    style: TextStyle(
                                      fontSize: 10,
                                      fontWeight: FontWeight.bold,
                                      color: AppColors.emeraldPrimary,
                                    ),
                                  ),
                                ),
                              ],
                            ),
                          ],
                        ),
                      ),
                    );
                  },
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}
