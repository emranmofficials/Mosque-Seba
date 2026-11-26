import 'dart:typed_data';
import 'package:printing/printing.dart';
import '../../examinations/models/exam_model.dart';
import '../../finance/models/finance_transaction_model.dart';
import '../../finance/repositories/finance_repository.dart';
import 'pdf_generator_service.dart';

/// Cross-platform Native Printing & Sharing Service
class NativePrintingService {
  final PdfGeneratorService _generator;

  NativePrintingService({PdfGeneratorService? generator})
      : _generator = generator ?? PdfGeneratorService();

  Future<Uint8List> buildReceiptPdf({
    required String receiptNo,
    required String memberName,
    required String memberCode,
    required String month,
    required double amount,
    required String receivedBy,
    required String date,
    String titleBn = 'চাঁদা ও অনুদান আদায়ের রসিদ',
    String paymentMethod = 'নগদ (Cash)',
    String? qrVerificationCode,
    String mosqueName = 'বায়তুল মুকাররম মডেল জামে মসজিদ',
  }) {
    return _generator.generateReceiptPdf(
      receiptNo: receiptNo,
      memberName: memberName,
      memberCode: memberCode,
      month: month,
      amount: amount,
      receivedBy: receivedBy,
      date: date,
      titleBn: titleBn,
      paymentMethod: paymentMethod,
      qrVerificationCode: qrVerificationCode,
      mosqueName: mosqueName,
    );
  }

  /// Direct Print Chanda / Fee Receipt
  Future<void> printReceipt({
    required String receiptNo,
    required String memberName,
    required String memberCode,
    required String month,
    required double amount,
    required String receivedBy,
    required String date,
    String titleBn = 'চাঁদা ও অনুদান আদায়ের রসিদ',
    String paymentMethod = 'নগদ (Cash)',
    String? qrVerificationCode,
    String mosqueName = 'বায়তুল মুকাররম মডেল জামে মসজিদ',
  }) async {
    final bytes = await buildReceiptPdf(
      receiptNo: receiptNo,
      memberName: memberName,
      memberCode: memberCode,
      month: month,
      amount: amount,
      receivedBy: receivedBy,
      date: date,
      titleBn: titleBn,
      paymentMethod: paymentMethod,
      qrVerificationCode: qrVerificationCode,
      mosqueName: mosqueName,
    );

    await Printing.layoutPdf(
      onLayout: (_) async => bytes,
      name: 'Receipt_$receiptNo.pdf',
    );
  }

  Future<Uint8List> buildVoucherPdf({
    required String voucherNo,
    required String category,
    required String description,
    required double amount,
    required String approvedBy,
    required String date,
    String mosqueName = 'বায়তুল মুকাররম মডেল জামে মসজিদ',
  }) {
    return _generator.generateVoucherPdf(
      voucherNo: voucherNo,
      category: category,
      description: description,
      amount: amount,
      approvedBy: approvedBy,
      date: date,
      mosqueName: mosqueName,
    );
  }

  /// Direct Print Debit Voucher
  Future<void> printVoucher({
    required String voucherNo,
    required String category,
    required String description,
    required double amount,
    required String approvedBy,
    required String date,
    String mosqueName = 'বায়তুল মুকাররম মডেল জামে মসজিদ',
  }) async {
    final bytes = await buildVoucherPdf(
      voucherNo: voucherNo,
      category: category,
      description: description,
      amount: amount,
      approvedBy: approvedBy,
      date: date,
      mosqueName: mosqueName,
    );

    await Printing.layoutPdf(
      onLayout: (_) async => bytes,
      name: 'Voucher_$voucherNo.pdf',
    );
  }

  /// Direct Print Madrasa Report Card
  Future<void> printReportCard({
    required String examName,
    required String studentName,
    required String className,
    required List<ExamResultModel> results,
    String mosqueName = 'মসজিদ সেবা মাদরাসা ও মক্তব',
  }) async {
    final bytes = await _generator.generateReportCardPdf(
      examName: examName,
      studentName: studentName,
      className: className,
      results: results,
      mosqueName: mosqueName,
    );

    await Printing.layoutPdf(
      onLayout: (_) async => bytes,
      name: 'ReportCard_$studentName.pdf',
    );
  }

  /// Direct Print Financial Audit Statement
  Future<void> printFinancialStatement({
    required FinancialSummary summary,
    required List<FinanceTransactionModel> transactions,
    required String mosqueName,
  }) async {
    final bytes = await _generator.generateFinancialStatementPdf(
      summary: summary,
      transactions: transactions,
      mosqueName: mosqueName,
    );

    await Printing.layoutPdf(
      onLayout: (_) async => bytes,
      name: 'Financial_Statement.pdf',
    );
  }

  /// Direct Print Staff Salary Payslip
  Future<void> printSalarySlip({
    required String staffName,
    required String month,
    required double baseSalary,
    required double bonusAmount,
    required double netSalary,
    required String paidDate,
    String mosqueName = 'বায়তুল মুকাররম মডেল জামে মসজিদ',
  }) async {
    final bytes = await _generator.generateSalarySlipPdf(
      staffName: staffName,
      month: month,
      baseSalary: baseSalary,
      bonusAmount: bonusAmount,
      netSalary: netSalary,
      paidDate: paidDate,
      mosqueName: mosqueName,
    );

    await Printing.layoutPdf(
      onLayout: (_) async => bytes,
      name: 'SalarySlip_$staffName.pdf',
    );
  }

  /// Share PDF Document via Native OS Share Sheet
  Future<void> shareDocument(Uint8List bytes, String filename) async {
    await Printing.sharePdf(bytes: bytes, filename: filename);
  }
}
