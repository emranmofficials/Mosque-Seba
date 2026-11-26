import 'dart:convert';
import 'dart:typed_data';
import 'package:flutter/services.dart' show rootBundle;
import 'package:pdf/pdf.dart';
import 'package:pdf/widgets.dart' as pw;
import 'package:printing/printing.dart';
import '../../examinations/models/exam_model.dart';
import '../../finance/models/finance_transaction_model.dart';
import '../../finance/repositories/finance_repository.dart';
import '../../official_signature/services/signature_storage_service.dart';

/// PDF Generator for Receipts, Vouchers, Report Cards, Statements & Salary Slips.
/// Uses native WebView HTML->PDF via Printing.convertHtml for crisp Bengali shaping
/// and embeds saved digital signatures & official mosque seal automatically.
class PdfGeneratorService {
  final SignatureStorageService _signatureService;
  pw.ThemeData? _cachedTheme;

  PdfGeneratorService({SignatureStorageService? signatureService})
      : _signatureService = signatureService ?? SignatureStorageService();

  Future<pw.ThemeData> _getBengaliTheme() async {
    if (_cachedTheme != null) return _cachedTheme!;
    final regData = await rootBundle.load('assets/fonts/hind_siliguri.ttf');
    final boldData = await rootBundle.load('assets/fonts/hind_siliguri_bold.ttf');
    final regTtf = pw.Font.ttf(regData);
    final boldTtf = pw.Font.ttf(boldData);
    _cachedTheme = pw.ThemeData.withFont(
      base: regTtf,
      bold: boldTtf,
      italic: regTtf,
      boldItalic: boldTtf,
    );
    return _cachedTheme!;
  }

  String _esc(String input) {
    return input
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
  }

  Future<Map<String, String?>> _loadAuthAssetsBase64() async {
    String? toPngDataUri(Uint8List? bytes) {
      if (bytes == null || bytes.isEmpty) return null;
      return 'data:image/png;base64,${base64Encode(bytes)}';
    }

    final seal = await _signatureService.getSeal();
    final pres = await _signatureService.getSignature('president');
    final treas = await _signatureService.getSignature('treasurer');

    return {
      'seal': toPngDataUri(seal),
      'president': (pres != null && SignatureStorageService.isPngBytes(pres))
          ? toPngDataUri(pres)
          : null,
      'treasurer': (treas != null && SignatureStorageService.isPngBytes(treas))
          ? toPngDataUri(treas)
          : null,
    };
  }

  Future<Uint8List> _convertHtmlOrFallback({
    required String bodyHtml,
    required PdfPageFormat format,
    required Future<pw.Widget> Function(pw.ThemeData theme) fallbackBuilder,
  }) async {
    final fullHtml = '''
<!DOCTYPE html>
<html lang="bn">
<head>
<meta charset="utf-8" />
<style>
  * { box-sizing: border-box; font-family: "Noto Sans Bengali", "Hind Siliguri", sans-serif; }
  body { margin: 14px; color: #162420; background: #fff; font-size: 12px; }
  .box { border: 2px solid #0B6E4F; border-radius: 8px; padding: 16px; position: relative; }
  .header { text-align: center; border-bottom: 1.5px solid #0B6E4F; padding-bottom: 8px; margin-bottom: 12px; }
  .header h1 { margin: 0; font-size: 18px; color: #084C37; }
  .header h2 { margin: 4px 0 2px; font-size: 13px; color: #1B4332; }
  .meta { font-size: 11px; color: #4A5D57; }
  .grid { width: 100%; border-collapse: collapse; margin: 10px 0; }
  .grid td, .grid th { padding: 6px 8px; border: 1px solid #D8E2DC; text-align: left; font-size: 11px; }
  .grid th { background: #E9F5F0; color: #084C37; }
  .amount-banner { background: #E9F5F0; border: 1px solid #0B6E4F; border-radius: 6px; padding: 10px 12px; display: flex; justify-content: space-between; font-weight: bold; font-size: 14px; color: #084C37; margin: 12px 0; }
  .qr-tag { font-family: monospace; font-size: 10px; background: #F4F7F5; padding: 6px 8px; border-radius: 4px; word-break: break-all; margin-top: 8px; }
  .signs { display: flex; justify-content: space-between; align-items: flex-end; margin-top: 28px; }
  .sign-col { text-align: center; width: 34%; }
  .sign-col img { max-height: 42px; max-width: 110px; display: block; margin: 0 auto 2px; }
  .sign-line { border-top: 1px solid #333; padding-top: 4px; font-size: 10px; }
  .seal-wrap { text-align: center; width: 26%; }
  .seal-wrap img { max-height: 62px; max-width: 62px; opacity: 0.9; }
</style>
</head>
<body>$bodyHtml</body>
</html>
''';

    try {
      final bytes = await Printing.convertHtml(
        html: fullHtml,
        format: format,
      );
      if (bytes.isNotEmpty) return bytes;
    } catch (_) {}

    final theme = await _getBengaliTheme();
    final pdf = pw.Document(theme: theme);
    final widget = await fallbackBuilder(theme);
    pdf.addPage(pw.Page(pageFormat: format, build: (_) => widget));
    return pdf.save();
  }

  /// 1. Generate Donation / Chanda / Madrasa Fee Receipt PDF
  Future<Uint8List> generateReceiptPdf({
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
    final assets = await _loadAuthAssetsBase64();
    final sealTag = assets['seal'] != null
        ? '<img src="${assets['seal']}" alt="seal" />'
        : '';
    final treasTag = assets['treasurer'] != null
        ? '<img src="${assets['treasurer']}" alt="treasurer" />'
        : '';
    final presTag = assets['president'] != null
        ? '<img src="${assets['president']}" alt="president" />'
        : '';

    final bodyHtml = '''
<div class="box">
  <div class="header">
    <h1>${_esc(mosqueName)}</h1>
    <h2>${_esc(titleBn)}</h2>
    <div class="meta">রসিদ নং: ${_esc(receiptNo)} &nbsp;|&nbsp; তারিখ: ${_esc(date)}</div>
  </div>
  <table class="grid">
    <tr><th>প্রদানকারীর নাম</th><td>${_esc(memberName)}</td><th>আইডি / কোড</th><td>${_esc(memberCode)}</td></tr>
    <tr><th>মাস / বিবরণ</th><td>${_esc(month)}</td><th>পরিশোধের মাধ্যম</th><td>${_esc(paymentMethod)}</td></tr>
  </table>
  <div class="amount-banner">
    <span>আদায়কৃত অর্থের পরিমাণ:</span>
    <span>৳ ${amount.toStringAsFixed(2)}</span>
  </div>
  ${qrVerificationCode != null && qrVerificationCode.isNotEmpty ? '<div class="qr-tag">ডিজিটাল যাচাই কোড (HMAC): ${_esc(qrVerificationCode)}</div>' : ''}
  <div class="signs">
    <div class="sign-col">$treasTag<div class="sign-line">আদায়কারী (${_esc(receivedBy)})</div></div>
    <div class="seal-wrap">$sealTag</div>
    <div class="sign-col">$presTag<div class="sign-line">কোষাধ্যক্ষ / সভাপতি</div></div>
  </div>
</div>
''';

    return _convertHtmlOrFallback(
      bodyHtml: bodyHtml,
      format: PdfPageFormat.a5,
      fallbackBuilder: (_) async {
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
                style: pw.TextStyle(
                  fontSize: 18,
                  fontWeight: pw.FontWeight.bold,
                  color: PdfColors.green900,
                ),
              ),
              pw.SizedBox(height: 4),
              pw.Text(
                titleBn,
                style: pw.TextStyle(fontSize: 13, fontWeight: pw.FontWeight.bold),
              ),
              pw.Text(
                'রসিদ নং: $receiptNo  |  তারিখ: $date',
                style: const pw.TextStyle(fontSize: 10, color: PdfColors.grey700),
              ),
              pw.Divider(thickness: 1, color: PdfColors.green800),
              pw.SizedBox(height: 10),
              pw.Row(
                mainAxisAlignment: pw.MainAxisAlignment.spaceBetween,
                children: [
                  pw.Text('নাম: $memberName', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
                  pw.Text('আইডি: $memberCode'),
                ],
              ),
              pw.SizedBox(height: 6),
              pw.Row(
                mainAxisAlignment: pw.MainAxisAlignment.spaceBetween,
                children: [
                  pw.Text('মাস: $month'),
                  pw.Text('মাধ্যম: $paymentMethod'),
                ],
              ),
              pw.SizedBox(height: 14),
              pw.Container(
                padding: const pw.EdgeInsets.symmetric(vertical: 8, horizontal: 12),
                color: PdfColors.green50,
                child: pw.Row(
                  mainAxisAlignment: pw.MainAxisAlignment.spaceBetween,
                  children: [
                    pw.Text('আদায়কৃত অর্থ:', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
                    pw.Text('BDT ${amount.toStringAsFixed(2)}', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
                  ],
                ),
              ),
              pw.SizedBox(height: 24),
              pw.Row(
                mainAxisAlignment: pw.MainAxisAlignment.spaceBetween,
                children: [
                  pw.Text('আদায়কারী: $receivedBy', style: const pw.TextStyle(fontSize: 9)),
                  pw.Text('কোষাধ্যক্ষ / সভাপতি', style: const pw.TextStyle(fontSize: 9)),
                ],
              ),
            ],
          ),
        );
      },
    );
  }

  /// 2. Generate financial expenditure / credit voucher PDF
  Future<Uint8List> generateVoucherPdf({
    required String voucherNo,
    required String category,
    required String description,
    required double amount,
    required String approvedBy,
    required String date,
    String voucherTitle = 'অফিসিয়াল ব্যয় ও ডেবিট ভাউচার',
    String mosqueName = 'বায়তুল মুকাররম মডেল জামে মসজিদ',
  }) async {
    final assets = await _loadAuthAssetsBase64();
    final sealTag = assets['seal'] != null
        ? '<img src="${assets['seal']}" alt="seal" />'
        : '';
    final presTag = assets['president'] != null
        ? '<img src="${assets['president']}" alt="president" />'
        : '';

    final bodyHtml = '''
<div class="box">
  <div class="header">
    <h1>${_esc(mosqueName)}</h1>
    <h2>${_esc(voucherTitle)}</h2>
    <div class="meta">ভাউচার নং: ${_esc(voucherNo)} &nbsp;|&nbsp; তারিখ: ${_esc(date)}</div>
  </div>
  <table class="grid">
    <tr><th>হিসাবের খাত</th><td>${_esc(category)}</td></tr>
    <tr><th>বিস্তারিত বিবরণ</th><td>${_esc(description)}</td></tr>
  </table>
  <div class="amount-banner">
    <span>মোট টাকার পরিমাণ:</span>
    <span>৳ ${amount.toStringAsFixed(2)}</span>
  </div>
  <div class="signs">
    <div class="sign-col"><div class="sign-line">প্রস্তুতকারী / গ্রহীতার স্বাক্ষর</div></div>
    <div class="seal-wrap">$sealTag</div>
    <div class="sign-col">$presTag<div class="sign-line">অনুমোদনকারী (${_esc(approvedBy)})</div></div>
  </div>
</div>
''';

    return _convertHtmlOrFallback(
      bodyHtml: bodyHtml,
      format: PdfPageFormat.a5,
      fallbackBuilder: (_) async {
        return pw.Container(
          padding: const pw.EdgeInsets.all(20),
          decoration: pw.BoxDecoration(
            border: pw.Border.all(color: PdfColors.grey700, width: 1.5),
          ),
          child: pw.Column(
            crossAxisAlignment: pw.CrossAxisAlignment.center,
            children: [
              pw.Text(mosqueName, style: pw.TextStyle(fontSize: 16, fontWeight: pw.FontWeight.bold)),
              pw.Text(voucherTitle, style: pw.TextStyle(fontSize: 12, fontWeight: pw.FontWeight.bold)),
              pw.Text('ভাউচার নং: $voucherNo  |  তারিখ: $date', style: const pw.TextStyle(fontSize: 10)),
              pw.Divider(),
              pw.Align(alignment: pw.Alignment.centerLeft, child: pw.Text('খাত: $category')),
              pw.SizedBox(height: 4),
              pw.Align(alignment: pw.Alignment.centerLeft, child: pw.Text('বিবরণ: $description')),
              pw.SizedBox(height: 12),
              pw.Text('মোট পরিমাণ: BDT ${amount.toStringAsFixed(2)}', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
              pw.SizedBox(height: 24),
              pw.Row(
                mainAxisAlignment: pw.MainAxisAlignment.spaceBetween,
                children: [
                  pw.Text('গ্রহীতার স্বাক্ষর', style: const pw.TextStyle(fontSize: 9)),
                  pw.Text('অনুমোদনকারী: $approvedBy', style: const pw.TextStyle(fontSize: 9)),
                ],
              ),
            ],
          ),
        );
      },
    );
  }

  /// 3. Generate Madrasa Student Exam Report Card PDF
  Future<Uint8List> generateReportCardPdf({
    required String examName,
    required String studentName,
    required String className,
    required List<ExamResultModel> results,
    String mosqueName = 'মসজিদ সেবা মাদরাসা ও মক্তব',
  }) async {
    final assets = await _loadAuthAssetsBase64();
    final sealTag = assets['seal'] != null
        ? '<img src="${assets['seal']}" alt="seal" />'
        : '';
    final presTag = assets['president'] != null
        ? '<img src="${assets['president']}" alt="president" />'
        : '';

    double obtained = 0;
    double total = 0;
    final rowsHtml = StringBuffer();
    for (final r in results) {
      obtained += r.marksObtained;
      total += r.totalMarks;
      rowsHtml.writeln(
        '<tr><td>${_esc(r.subjectName)}</td>'
        '<td>${r.marksObtained.toStringAsFixed(0)} / ${r.totalMarks.toStringAsFixed(0)}</td>'
        '<td><strong>${_esc(r.grade)}</strong></td></tr>',
      );
    }
    final pct = total > 0 ? (obtained / total * 100) : 0.0;

    final bodyHtml = '''
<div class="box">
  <div class="header">
    <h1>${_esc(mosqueName)}</h1>
    <h2>শিক্ষার্থীর পরীক্ষার ফলাফল পত্র (${_esc(examName)})</h2>
  </div>
  <table class="grid">
    <tr><th>শিক্ষার্থীর নাম</th><td>${_esc(studentName)}</td><th>বিভাগ / শ্রেণী</th><td>${_esc(className)}</td></tr>
  </table>
  <table class="grid">
    <tr><th>বিষয়</th><th>প্রাপ্ত নম্বর</th><th>গ্রেড / মূল্যায়ন</th></tr>
    ${results.isEmpty ? '<tr><td colspan="3">এই পরীক্ষার জন্য কোনো নম্বর এন্ট্রি পাওয়া যায়নি।</td></tr>' : rowsHtml.toString()}
  </table>
  <div class="amount-banner">
    <span>সর্বমোট নম্বর: ${obtained.toStringAsFixed(0)} / ${total.toStringAsFixed(0)}</span>
    <span>শতকরা হার: ${pct.toStringAsFixed(1)}%</span>
  </div>
  <div class="signs">
    <div class="sign-col"><div class="sign-line">প্রধান শিক্ষক / মুহতামিম</div></div>
    <div class="seal-wrap">$sealTag</div>
    <div class="sign-col">$presTag<div class="sign-line">মসজিদ ও মাদরাসা সভাপতি</div></div>
  </div>
</div>
''';

    return _convertHtmlOrFallback(
      bodyHtml: bodyHtml,
      format: PdfPageFormat.a4,
      fallbackBuilder: (_) async {
        return pw.Container(
          padding: const pw.EdgeInsets.all(20),
          decoration: pw.BoxDecoration(border: pw.Border.all(width: 1.5)),
          child: pw.Column(
            crossAxisAlignment: pw.CrossAxisAlignment.start,
            children: [
              pw.Center(child: pw.Text(mosqueName, style: pw.TextStyle(fontSize: 16, fontWeight: pw.FontWeight.bold))),
              pw.Center(child: pw.Text('রিপোর্ট কার্ড: $examName')),
              pw.Divider(),
              pw.Text('শিক্ষার্থী: $studentName  |  শ্রেণী: $className'),
              pw.SizedBox(height: 10),
              ...results.map(
                (r) => pw.Text('${r.subjectName}: ${r.marksObtained}/${r.totalMarks} (${r.grade})'),
              ),
              pw.SizedBox(height: 12),
              pw.Text('মোট: $obtained / $total (${pct.toStringAsFixed(1)}%)', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
            ],
          ),
        );
      },
    );
  }

  /// 4. Generate Financial Audit / Periodic Statement PDF
  Future<Uint8List> generateFinancialStatementPdf({
    required FinancialSummary summary,
    required List<FinanceTransactionModel> transactions,
    required String mosqueName,
    String periodTitle = 'মসজিদের আয়-ব্যয় হিসাব বিবরণী',
  }) async {
    final assets = await _loadAuthAssetsBase64();
    final sealTag = assets['seal'] != null
        ? '<img src="${assets['seal']}" alt="seal" />'
        : '';
    final treasTag = assets['treasurer'] != null
        ? '<img src="${assets['treasurer']}" alt="treasurer" />'
        : '';
    final presTag = assets['president'] != null
        ? '<img src="${assets['president']}" alt="president" />'
        : '';

    final approvedList = transactions
        .where((t) => !t.isPending && t.approvalStatus != 'rejected' && !t.isReversal)
        .take(40)
        .toList();

    final rowsHtml = StringBuffer();
    for (final t in approvedList) {
      final typeLabel = t.isIncome ? 'আয়' : 'ব্যয়';
      rowsHtml.writeln(
        '<tr><td>${_esc(t.txnDate)}</td><td>${_esc(t.categoryNameBn)}</td>'
        '<td>$typeLabel</td><td>৳ ${t.amount.toStringAsFixed(0)}</td></tr>',
      );
    }

    final bodyHtml = '''
<div class="box">
  <div class="header">
    <h1>${_esc(mosqueName)}</h1>
    <h2>${_esc(periodTitle)}</h2>
    <div class="meta">তারিখ: ${_esc(DateTime.now().toIso8601String().split('T').first)}</div>
  </div>
  <table class="grid">
    <tr><th>মোট অনুমোদিত আয়</th><td>৳ ${summary.totalIncome.toStringAsFixed(2)}</td><th>মোট অনুমোদিত ব্যয়</th><td>৳ ${summary.totalExpense.toStringAsFixed(2)}</td></tr>
    <tr><th>নগদ ক্যাশ ব্যালেন্স</th><td>৳ ${summary.cashInHand.toStringAsFixed(2)}</td><th>ব্যাংক / ডিজিটাল ব্যালেন্স</th><td>৳ ${summary.bankBalance.toStringAsFixed(2)}</td></tr>
  </table>
  <div class="amount-banner">
    <span>নীট বর্তমান তহবিল:</span>
    <span>৳ ${summary.netBalance.toStringAsFixed(2)}</span>
  </div>
  <table class="grid">
    <tr><th>তারিখ</th><th>খাত</th><th>ধরন</th><th>পরিমাণ</th></tr>
    ${rowsHtml.isEmpty ? '<tr><td colspan="4">কোনো অনুমোদিত লেনদেন নেই।</td></tr>' : rowsHtml.toString()}
  </table>
  <div class="signs">
    <div class="sign-col">$treasTag<div class="sign-line">অর্থ সম্পাদক / কোষাধ্যক্ষ</div></div>
    <div class="seal-wrap">$sealTag</div>
    <div class="sign-col">$presTag<div class="sign-line">মুহতারাম সভাপতি</div></div>
  </div>
</div>
''';

    return _convertHtmlOrFallback(
      bodyHtml: bodyHtml,
      format: PdfPageFormat.a4,
      fallbackBuilder: (_) async {
        return pw.Container(
          padding: const pw.EdgeInsets.all(20),
          child: pw.Column(
            crossAxisAlignment: pw.CrossAxisAlignment.start,
            children: [
              pw.Center(child: pw.Text(mosqueName, style: pw.TextStyle(fontSize: 16, fontWeight: pw.FontWeight.bold))),
              pw.Center(child: pw.Text(periodTitle)),
              pw.Divider(),
              pw.Text('মোট আয়: BDT ${summary.totalIncome.toStringAsFixed(2)}'),
              pw.Text('মোট ব্যয়: BDT ${summary.totalExpense.toStringAsFixed(2)}'),
              pw.Text('নীট ব্যালেন্স: BDT ${summary.netBalance.toStringAsFixed(2)}', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
            ],
          ),
        );
      },
    );
  }

  /// 5. Generate Staff Salary Payslip PDF
  Future<Uint8List> generateSalarySlipPdf({
    required String staffName,
    required String month,
    required double baseSalary,
    required double bonusAmount,
    required double netSalary,
    required String paidDate,
    String mosqueName = 'বায়তুল মুকাররম মডেল জামে মসজিদ',
  }) async {
    return generateVoucherPdf(
      voucherNo: 'PAY-$month',
      voucherTitle: 'ইমাম ও স্টাফ বেতন-ভাতা পে-স্লিপ ($month)',
      category: 'কর্মচারী বেতন ও ভাতা ($staffName)',
      description: 'মূল বেতন: ৳${baseSalary.toStringAsFixed(0)} + বোনাস/ভাতা: ৳${bonusAmount.toStringAsFixed(0)}',
      amount: netSalary,
      approvedBy: 'সভাপতি ও কোষাধ্যক্ষ',
      date: paidDate,
      mosqueName: mosqueName,
    );
  }
}
