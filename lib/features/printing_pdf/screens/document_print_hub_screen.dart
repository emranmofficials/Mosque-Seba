import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/widgets/card_shell.dart';
import '../../authentication/providers/auth_provider.dart';
import '../../examinations/models/exam_model.dart';
import '../../examinations/providers/exam_provider.dart';
import '../../finance/providers/finance_provider.dart';
import '../../madrasa/providers/madrasa_provider.dart';
import '../../staff/providers/staff_provider.dart';
import '../../subscriptions/providers/chanda_provider.dart';
import '../dialogs/thermal_printer_dialog.dart';
import '../services/native_printing_service.dart';
import '../services/pdf_generator_service.dart';

/// Screen presenting the Native Document Printing, Sharing & Thermal PDF Hub
class DocumentPrintHubScreen extends ConsumerWidget {
  const DocumentPrintHubScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final printingService = NativePrintingService();
    final pdfGenerator = PdfGeneratorService();

    final authUser = ref.watch(authNotifierProvider).valueOrNull;
    final mosqueName = (authUser?.mosqueNameBn.isNotEmpty ?? false)
        ? authUser!.mosqueNameBn
        : 'মসজিদ সেবা জামে মসজিদ';

    final chandaList = ref.watch(chandaListProvider).valueOrNull ?? [];
    final txnList = ref.watch(financeListProvider).valueOrNull ?? [];
    final financeSummary = ref.watch(financeSummaryProvider);
    final studentList = ref.watch(studentListProvider).valueOrNull ?? [];
    final examList = ref.watch(examListProvider).valueOrNull ?? [];
    final payrollList = ref.watch(payrollHistoryProvider).valueOrNull ?? [];

    final latestChanda = chandaList.isNotEmpty ? chandaList.first : null;
    final latestTxn = txnList.isNotEmpty ? txnList.first : null;
    final latestStudent = studentList.isNotEmpty ? studentList.first : null;
    final latestExam = examList.isNotEmpty ? examList.first : null;
    final latestPayroll = payrollList.isNotEmpty ? payrollList.first : null;

    return Scaffold(
      appBar: AppBar(
        title: const Text('ডকুমেন্ট ও প্রিন্ট সেন্টার'),
      ),
      body: ListView(
        padding: const EdgeInsets.all(AppDimens.spacing16),
        children: [
          const Text(
            'অফিসিয়াল ডকুমেন্টস, সিল-স্বাক্ষরযুক্ত PDF ও থার্মাল প্রিন্ট',
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 4),
          const Text(
            'লাইভ ডাটাবেস থেকে রসিদ, ভাউচার, রিপোর্ট কার্ড এবং অডিট রিপোর্ট সরাসরি WiFi/PDF বা ব্লুটুথ থার্মাল প্রিন্টারে প্রিন্ট করুন',
            style: TextStyle(fontSize: 12, color: Colors.grey),
          ),
          const SizedBox(height: 16),

          // 1. Live Chanda Receipt
          _buildPrintCard(
            title: 'চাঁদা ও দান আদায়ের রসিদ',
            subtitle: latestChanda != null
                ? 'সর্বশেষ: ${latestChanda.memberNameBn} • ${BengaliNumberFormatter.formatCurrency(latestChanda.amountPaid)} (${latestChanda.receiptCode})'
                : 'চাঁদা লেজারে কোনো রসিদ এখনো নেই — সংক্ষিপ্ত নমুনা প্রিন্ট প্রিভিউ উপলব্ধ',
            icon: Icons.receipt_long,
            color: AppColors.emeraldPrimary,
            onPrintPdf: () => printingService.printReceipt(
              receiptNo: latestChanda?.receiptCode ?? 'N/A',
              memberName: latestChanda?.memberNameBn ?? 'মসজিদ দাতা/সদস্য',
              memberCode: latestChanda?.memberId ?? '-',
              month: latestChanda?.month ?? '-',
              amount: latestChanda?.amountPaid ?? 0,
              receivedBy: latestChanda?.receivedBy ?? (authUser?.memberName ?? 'কোষাধ্যক্ষ'),
              date: latestChanda?.paidAt ?? DateTime.now().toIso8601String().split('T').first,
              qrVerificationCode: latestChanda?.qrVerificationCode,
              mosqueName: mosqueName,
            ),
            onSharePdf: () async {
              final bytes = await printingService.buildReceiptPdf(
                receiptNo: latestChanda?.receiptCode ?? 'N/A',
                memberName: latestChanda?.memberNameBn ?? 'মসজিদ দাতা/সদস্য',
                memberCode: latestChanda?.memberId ?? '-',
                month: latestChanda?.month ?? '-',
                amount: latestChanda?.amountPaid ?? 0,
                receivedBy: latestChanda?.receivedBy ?? (authUser?.memberName ?? 'কোষাধ্যক্ষ'),
                date: latestChanda?.paidAt ?? DateTime.now().toIso8601String().split('T').first,
                qrVerificationCode: latestChanda?.qrVerificationCode,
                mosqueName: mosqueName,
              );
              await printingService.shareDocument(bytes, 'Chanda_Receipt.pdf');
            },
            onThermalPrint: () => showThermalPrinterDialog(
              context,
              buildPdf: () => printingService.buildReceiptPdf(
                receiptNo: latestChanda?.receiptCode ?? 'N/A',
                memberName: latestChanda?.memberNameBn ?? 'মসজিদ দাতা/সদস্য',
                memberCode: latestChanda?.memberId ?? '-',
                month: latestChanda?.month ?? '-',
                amount: latestChanda?.amountPaid ?? 0,
                receivedBy: latestChanda?.receivedBy ?? (authUser?.memberName ?? 'কোষাধ্যক্ষ'),
                date: latestChanda?.paidAt ?? DateTime.now().toIso8601String().split('T').first,
                qrVerificationCode: latestChanda?.qrVerificationCode,
                mosqueName: mosqueName,
              ),
            ),
          ),
          const SizedBox(height: 12),

          // 2. Live Expense / Finance Voucher
          _buildPrintCard(
            title: 'আয়-ব্যয় ও ডেবিট ভাউচার',
            subtitle: latestTxn != null
                ? 'সর্বশেষ: ${latestTxn.categoryNameBn} • ${BengaliNumberFormatter.formatCurrency(latestTxn.amount)} (${latestTxn.transactionCode})'
                : 'ফাইন্যান্স লেজারে কোনো লেনদেন এখনো নেই',
            icon: Icons.assignment_outlined,
            color: AppColors.roleTreasurer,
            onPrintPdf: () => printingService.printVoucher(
              voucherNo: latestTxn?.transactionCode ?? 'N/A',
              category: latestTxn?.categoryNameBn ?? 'সাধারণ খাত',
              description: latestTxn?.description.isNotEmpty == true
                  ? latestTxn!.description
                  : 'অনুমোদিত লেনদেন ভাউচার',
              amount: latestTxn?.amount ?? 0,
              approvedBy: latestTxn?.approvedBy ?? (authUser?.memberName ?? 'সভাপতি ও কোষাধ্যক্ষ'),
              date: latestTxn?.txnDate ?? DateTime.now().toIso8601String().split('T').first,
              mosqueName: mosqueName,
            ),
            onSharePdf: () async {
              final bytes = await printingService.buildVoucherPdf(
                voucherNo: latestTxn?.transactionCode ?? 'N/A',
                category: latestTxn?.categoryNameBn ?? 'সাধারণ খাত',
                description: latestTxn?.description.isNotEmpty == true
                    ? latestTxn!.description
                    : 'অনুমোদিত লেনদেন ভাউচার',
                amount: latestTxn?.amount ?? 0,
                approvedBy: latestTxn?.approvedBy ?? (authUser?.memberName ?? 'সভাপতি ও কোষাধ্যক্ষ'),
                date: latestTxn?.txnDate ?? DateTime.now().toIso8601String().split('T').first,
                mosqueName: mosqueName,
              );
              await printingService.shareDocument(bytes, 'Finance_Voucher.pdf');
            },
            onThermalPrint: () => showThermalPrinterDialog(
              context,
              buildPdf: () => printingService.buildVoucherPdf(
                voucherNo: latestTxn?.transactionCode ?? 'N/A',
                category: latestTxn?.categoryNameBn ?? 'সাধারণ খাত',
                description: latestTxn?.description.isNotEmpty == true
                    ? latestTxn!.description
                    : 'অনুমোদিত লেনদেন ভাউচার',
                amount: latestTxn?.amount ?? 0,
                approvedBy: latestTxn?.approvedBy ?? (authUser?.memberName ?? 'সভাপতি ও কোষাধ্যক্ষ'),
                date: latestTxn?.txnDate ?? DateTime.now().toIso8601String().split('T').first,
                mosqueName: mosqueName,
              ),
            ),
          ),
          const SizedBox(height: 12),

          // 3. Madrasa Student Report Card PDF
          _buildPrintCard(
            title: 'মাদরাসা শিক্ষার্থীর ফলাফল পত্র',
            subtitle: latestStudent != null
                ? 'শিক্ষার্থী: ${latestStudent.nameBn} (${latestStudent.className}) • পরীক্ষা: ${latestExam?.examNameBn ?? 'সর্বশেষ মূল্যায়ন'}'
                : 'মাদরাসা তালিকায় শিক্ষার্থী যুক্ত থাকলে তাদের নম্বরপত্র PDF হবে',
            icon: Icons.school_outlined,
            color: Colors.purple.shade700,
            onPrintPdf: () async {
              final examRepo = ref.read(examRepositoryProvider);
              final List<ExamResultModel> results = latestStudent != null
                  ? await examRepo.fetchResults(
                      mosqueId: authUser?.mosqueId ?? '',
                      examId: latestExam?.id,
                      studentId: latestStudent.id,
                    )
                  : const <ExamResultModel>[];
              await printingService.printReportCard(
                examName: latestExam?.examNameBn ?? 'সাময়িক মূল্যায়ন',
                studentName: latestStudent?.nameBn ?? 'শিক্ষার্থী নির্ধারিত নেই',
                className: latestStudent?.className ?? 'মাদরাসা বিভাগ',
                results: results,
                mosqueName: '$mosqueName — মাদরাসা বিভাগ',
              );
            },
            onSharePdf: () async {
              final examRepo = ref.read(examRepositoryProvider);
              final List<ExamResultModel> results = latestStudent != null
                  ? await examRepo.fetchResults(
                      mosqueId: authUser?.mosqueId ?? '',
                      examId: latestExam?.id,
                      studentId: latestStudent.id,
                    )
                  : const <ExamResultModel>[];
              final bytes = await pdfGenerator.generateReportCardPdf(
                examName: latestExam?.examNameBn ?? 'সাময়িক মূল্যায়ন',
                studentName: latestStudent?.nameBn ?? 'শিক্ষার্থী নির্ধারিত নেই',
                className: latestStudent?.className ?? 'মাদরাসা বিভাগ',
                results: results,
                mosqueName: '$mosqueName — মাদরাসা বিভাগ',
              );
              await printingService.shareDocument(bytes, 'Madrasa_Report_Card.pdf');
            },
          ),
          const SizedBox(height: 12),

          // 4. Financial Audit Statement PDF
          _buildPrintCard(
            title: 'বাৎসরিক ও সামগ্রিক আয়-ব্যয় হিসাব বিবরণী',
            subtitle:
                'মোট আয়: ${BengaliNumberFormatter.formatCurrency(financeSummary.totalIncome)} • নীট তহবিল: ${BengaliNumberFormatter.formatCurrency(financeSummary.netBalance)}',
            icon: Icons.pie_chart_outline,
            color: Colors.blue.shade700,
            onPrintPdf: () => printingService.printFinancialStatement(
              summary: financeSummary,
              transactions: txnList,
              mosqueName: mosqueName,
            ),
            onSharePdf: () async {
              final bytes = await pdfGenerator.generateFinancialStatementPdf(
                summary: financeSummary,
                transactions: txnList,
                mosqueName: mosqueName,
              );
              await printingService.shareDocument(bytes, 'Financial_Statement.pdf');
            },
          ),
          const SizedBox(height: 12),

          // 5. Staff Salary Payslip PDF
          if (latestPayroll != null)
            _buildPrintCard(
              title: 'ইমাম ও স্টাফ বেতন-ভাতা পে-স্লিপ',
              subtitle:
                  '${latestPayroll.staffNameBn} • ${latestPayroll.month} (${BengaliNumberFormatter.formatCurrency(latestPayroll.netSalary)})',
              icon: Icons.payments_outlined,
              color: Colors.teal.shade700,
              onPrintPdf: () => printingService.printSalarySlip(
                staffName: latestPayroll.staffNameBn,
                month: latestPayroll.month,
                baseSalary: latestPayroll.salaryAmount,
                bonusAmount: latestPayroll.bonusAmount,
                netSalary: latestPayroll.netSalary,
                paidDate: latestPayroll.paidDate,
                mosqueName: mosqueName,
              ),
              onSharePdf: () async {
                final bytes = await pdfGenerator.generateSalarySlipPdf(
                  staffName: latestPayroll.staffNameBn,
                  month: latestPayroll.month,
                  baseSalary: latestPayroll.salaryAmount,
                  bonusAmount: latestPayroll.bonusAmount,
                  netSalary: latestPayroll.netSalary,
                  paidDate: latestPayroll.paidDate,
                  mosqueName: mosqueName,
                );
                await printingService.shareDocument(bytes, 'Salary_Slip.pdf');
              },
            ),
        ],
      ),
    );
  }

  Widget _buildPrintCard({
    required String title,
    required String subtitle,
    required IconData icon,
    required Color color,
    required VoidCallback onPrintPdf,
    required VoidCallback onSharePdf,
    VoidCallback? onThermalPrint,
  }) {
    return CardShell(
      padding: const EdgeInsets.all(AppDimens.spacing14),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: color.withOpacity(0.12),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Icon(icon, color: color, size: 22),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      subtitle,
                      style: TextStyle(fontSize: 11, color: Colors.grey.shade600),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          Wrap(
            spacing: 8,
            runSpacing: 6,
            children: [
              ElevatedButton.icon(
                onPressed: onPrintPdf,
                icon: const Icon(Icons.picture_as_pdf, size: 15),
                label: const Text('PDF প্রিন্ট', style: TextStyle(fontSize: 12)),
                style: ElevatedButton.styleFrom(
                  backgroundColor: color,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                ),
              ),
              OutlinedButton.icon(
                onPressed: onSharePdf,
                icon: const Icon(Icons.share_outlined, size: 15),
                label: const Text('শেয়ার', style: TextStyle(fontSize: 12)),
                style: OutlinedButton.styleFrom(
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                ),
              ),
              if (onThermalPrint != null)
                OutlinedButton.icon(
                  onPressed: onThermalPrint,
                  icon: const Icon(Icons.bluetooth_connected, size: 15),
                  label: const Text('থার্মাল প্রিন্ট', style: TextStyle(fontSize: 12)),
                  style: OutlinedButton.styleFrom(
                    foregroundColor: AppColors.emeraldPrimary,
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                  ),
                ),
            ],
          ),
        ],
      ),
    );
  }
}
