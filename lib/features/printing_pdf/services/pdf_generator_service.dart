import 'dart:typed_data';
import 'package:pdf/pdf.dart';
import 'package:pdf/widgets.dart' as pw;

/// PDF Generator for Mosque Receipts, Vouchers, and Salary Slips
class PdfGeneratorService {
  /// Generate a formal donation / Chanda receipt PDF
  Future<Uint8List> generateReceiptPdf({
    required String receiptNo,
    required String memberName,
    required String memberCode,
    required String month,
    required double amount,
    required String receivedBy,
    required String date,
    String mosqueName = 'বায়তুল মুকাররম মডেল জামে মসজিদ',
  }) async {
    final pdf = pw.Document();

    pdf.addPage(
      pw.Page(
        pageFormat: PdfPageFormat.a5,
        build: (pw.Context context) {
          return pw.Container(
            padding: const pw.EdgeInsets.all(20),
            decoration: pw.BoxDecoration(
              border: pw.Border.all(color: PdfColors.green800, width: 2),
            ),
            child: pw.Column(
              crossAxisAlignment: pw.CrossAxisAlignment.center,
              children: [
                pw.Text(
                  mosqueName,
                  style: pw.TextStyle(fontSize: 18, fontWeight: pw.FontWeight.bold, color: PdfColors.green900),
                ),
                pw.SizedBox(height: 4),
                pw.Text('চাঁদা ও অনুদান আদায়ের রসিদ', style: pw.TextStyle(fontSize: 13, fontWeight: pw.FontWeight.bold)),
                pw.SizedBox(height: 2),
                pw.Text('রসিদ নং: $receiptNo  |  তারিখ: $date', style: const pw.TextStyle(fontSize: 10, color: PdfColors.grey700)),
                pw.Divider(thickness: 1, color: PdfColors.green800),
                pw.SizedBox(height: 10),

                pw.Row(
                  mainAxisAlignment: pw.MainAxisAlignment.spaceBetween,
                  children: [
                    pw.Text('সদস্যের নাম: $memberName', style: pw.TextStyle(fontWeight: pw.FontWeight.bold, fontSize: 12)),
                    pw.Text('সদস্য আইডি: $memberCode', style: const pw.TextStyle(fontSize: 11)),
                  ],
                ),
                pw.SizedBox(height: 8),
                pw.Row(
                  mainAxisAlignment: pw.MainAxisAlignment.spaceBetween,
                  children: [
                    pw.Text('চাঁদার মাস: $month', style: const pw.TextStyle(fontSize: 11)),
                    pw.Text('পরিশোধের মাধ্যম: নগদ (Cash)', style: const pw.TextStyle(fontSize: 11)),
                  ],
                ),
                pw.SizedBox(height: 14),

                pw.Container(
                  padding: const pw.EdgeInsets.symmetric(vertical: 8, horizontal: 12),
                  color: PdfColors.green50,
                  child: pw.Row(
                    mainAxisAlignment: pw.MainAxisAlignment.spaceBetween,
                    children: [
                      pw.Text('আদায়কৃত অর্থের পরিমাণ:', style: pw.TextStyle(fontWeight: pw.FontWeight.bold, fontSize: 13)),
                      pw.Text('BDT ${amount.toStringAsFixed(2)}', style: pw.TextStyle(fontWeight: pw.FontWeight.bold, fontSize: 14, color: PdfColors.green900)),
                    ],
                  ),
                ),
                pw.SizedBox(height: 30),

                pw.Row(
                  mainAxisAlignment: pw.MainAxisAlignment.spaceBetween,
                  children: [
                    pw.Column(
                      children: [
                        pw.Container(width: 80, height: 1, color: PdfColors.grey700),
                        pw.SizedBox(height: 4),
                        pw.Text('আদায়কারী ($receivedBy)', style: const pw.TextStyle(fontSize: 9)),
                      ],
                    ),
                    pw.Column(
                      children: [
                        pw.Container(width: 80, height: 1, color: PdfColors.grey700),
                        pw.SizedBox(height: 4),
                        pw.Text('কোষাধ্যক্ষ / সভাপতি', style: const pw.TextStyle(fontSize: 9)),
                      ],
                    ),
                  ],
                ),
              ],
            ),
          );
        },
      ),
    );

    return pdf.save();
  }

  /// Generate financial expenditure voucher PDF
  Future<Uint8List> generateVoucherPdf({
    required String voucherNo,
    required String category,
    required String description,
    required double amount,
    required String approvedBy,
    required String date,
    String mosqueName = 'বায়তুল মুকাররম মডেল জামে মসজিদ',
  }) async {
    final pdf = pw.Document();

    pdf.addPage(
      pw.Page(
        pageFormat: PdfPageFormat.a5,
        build: (pw.Context context) {
          return pw.Container(
            padding: const pw.EdgeInsets.all(20),
            decoration: pw.BoxDecoration(
              border: pw.Border.all(color: PdfColors.grey700, width: 1.5),
            ),
            child: pw.Column(
              crossAxisAlignment: pw.CrossAxisAlignment.center,
              children: [
                pw.Text(
                  mosqueName,
                  style: pw.TextStyle(fontSize: 16, fontWeight: pw.FontWeight.bold),
                ),
                pw.SizedBox(height: 2),
                pw.Text('অফিসিয়াল ব্যয় ও ডেবিট ভাউচার', style: pw.TextStyle(fontSize: 12, fontWeight: pw.FontWeight.bold)),
                pw.Text('ভাউচার নং: $voucherNo  |  তারিখ: $date', style: const pw.TextStyle(fontSize: 10)),
                pw.Divider(thickness: 1),
                pw.SizedBox(height: 8),

                pw.Row(
                  mainAxisAlignment: pw.MainAxisAlignment.spaceBetween,
                  children: [
                    pw.Text('ব্যয়ের খাত: $category', style: pw.TextStyle(fontWeight: pw.FontWeight.bold, fontSize: 11)),
                  ],
                ),
                pw.SizedBox(height: 6),
                pw.Text('বিবরণ: $description', style: const pw.TextStyle(fontSize: 11)),
                pw.SizedBox(height: 12),

                pw.Container(
                  padding: const pw.EdgeInsets.all(8),
                  color: PdfColors.grey200,
                  child: pw.Row(
                    mainAxisAlignment: pw.MainAxisAlignment.spaceBetween,
                    children: [
                      pw.Text('মোট ব্যয়ের পরিমাণ:', style: pw.TextStyle(fontWeight: pw.FontWeight.bold, fontSize: 12)),
                      pw.Text('BDT ${amount.toStringAsFixed(2)}', style: pw.TextStyle(fontWeight: pw.FontWeight.bold, fontSize: 13)),
                    ],
                  ),
                ),
                pw.SizedBox(height: 35),

                pw.Row(
                  mainAxisAlignment: pw.MainAxisAlignment.spaceBetween,
                  children: [
                    pw.Column(
                      children: [
                        pw.Container(width: 70, height: 1, color: PdfColors.black),
                        pw.SizedBox(height: 2),
                        pw.Text('গ্রহীতার স্বাক্ষর', style: const pw.TextStyle(fontSize: 8)),
                      ],
                    ),
                    pw.Column(
                      children: [
                        pw.Container(width: 70, height: 1, color: PdfColors.black),
                        pw.SizedBox(height: 2),
                        pw.Text('অনুমোদনকারী ($approvedBy)', style: const pw.TextStyle(fontSize: 8)),
                      ],
                    ),
                  ],
                ),
              ],
            ),
          );
        },
      ),
    );

    return pdf.save();
  }
}
