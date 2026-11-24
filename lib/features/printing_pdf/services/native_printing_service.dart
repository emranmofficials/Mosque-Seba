import 'dart:typed_data';
import 'package:printing/printing.dart';
import 'pdf_generator_service.dart';

/// Cross-platform Native Printing & Sharing Service
class NativePrintingService {
  final PdfGeneratorService _generator;

  NativePrintingService({PdfGeneratorService? generator})
      : _generator = generator ?? PdfGeneratorService();

  /// Direct Print Chanda Receipt
  Future<void> printReceipt({
    required String receiptNo,
    required String memberName,
    required String memberCode,
    required String month,
    required double amount,
    required String receivedBy,
    required String date,
  }) async {
    final bytes = await _generator.generateReceiptPdf(
      receiptNo: receiptNo,
      memberName: memberName,
      memberCode: memberCode,
      month: month,
      amount: amount,
      receivedBy: receivedBy,
      date: date,
    );

    await Printing.layoutPdf(
      onLayout: (_) async => bytes,
      name: 'Receipt_$receiptNo.pdf',
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
  }) async {
    final bytes = await _generator.generateVoucherPdf(
      voucherNo: voucherNo,
      category: category,
      description: description,
      amount: amount,
      approvedBy: approvedBy,
      date: date,
    );

    await Printing.layoutPdf(
      onLayout: (_) async => bytes,
      name: 'Voucher_$voucherNo.pdf',
    );
  }

  /// Share Document
  Future<void> shareDocument(Uint8List bytes, String filename) async {
    await Printing.sharePdf(bytes: bytes, filename: filename);
  }
}
