import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../shared/widgets/card_shell.dart';
import '../services/native_printing_service.dart';

/// Screen presenting the Native Document Printing & PDF Hub
class DocumentPrintHubScreen extends StatelessWidget {
  const DocumentPrintHubScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final printingService = NativePrintingService();

    return Scaffold(
      appBar: AppBar(
        title: const Text('ডকুমেন্ট ও প্রিন্ট সেন্টার'),
      ),
      body: ListView(
        padding: const EdgeInsets.all(AppDimens.spacing16),
        children: [
          const Text(
            'অফিসিয়াল ডকুমেন্টস ও প্রিন্টযোগ্য রসিদসমূহ',
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 4),
          const Text(
            'সরাসরি ওয়াইফাই বা ব্লুটুথ প্রিন্টারে প্রিন্ট করুন অথবা পিডিএফ ডাউনলোড করুন',
            style: TextStyle(fontSize: 12, color: Colors.grey),
          ),
          const SizedBox(height: 16),

          // 1. Chanda Receipt
          _buildPrintCard(
            title: 'চাঁদা ও দান আদায়ের রসিদ',
            subtitle: 'সদস্যের মাসিক চাঁদা ও সাধারণ দানের জন্য অফিসিয়াল রসিদ',
            icon: Icons.receipt_long,
            color: AppColors.emeraldPrimary,
            onPrint: () {
              printingService.printReceipt(
                receiptNo: 'REC-2026-0891',
                memberName: 'হাজী মো. শামসুল হক',
                memberCode: 'MS-2026-042',
                month: 'নভেম্বর ২০২৬',
                amount: 1500.0,
                receivedBy: 'মুহতারাম কোষাধ্যক্ষ',
                date: '২৪/১১/২০২৬',
              );
            },
          ),
          const SizedBox(height: 12),

          // 2. Expense Voucher
          _buildPrintCard(
            title: 'ব্যয় ও ডেবিট ভাউচার',
            subtitle: 'মসজিদের বিদ্যুৎ বিল, ইমাম-মুয়াজ্জিন সম্মানী বা মেরামত খরচ',
            icon: Icons.assignment_outlined,
            color: AppColors.roleTreasurer,
            onPrint: () {
              printingService.printVoucher(
                voucherNo: 'VCH-2026-0128',
                category: 'মসজিদ বিদ্যুৎ ও ইউটিলিটি বিল',
                description: 'অক্টোবর ২০২৬ মাসের ডেসকো বিদ্যুৎ বিল পরিশোধ',
                amount: 8450.0,
                approvedBy: 'সভাপতি ও অর্থ সম্পাদক',
                date: '২৪/১১/২০২৬',
              );
            },
          ),
          const SizedBox(height: 12),

          // 3. Madrasa Report Card Print
          _buildPrintCard(
            title: 'মাদরাসা শিক্ষার্থীর ফলাফল পত্র',
            subtitle: 'হিফজ ও নূরানী বিভাগের বার্ষিক মূল্যায়ন রিপোর্ট কার্ড',
            icon: Icons.school_outlined,
            color: Colors.purple.shade700,
            onPrint: () {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                  content: Text('মাদরাসা ফলাফল পত্রের হাই-রেজোলিউশন PDF প্রস্তুত হচ্ছে...'),
                  backgroundColor: AppColors.emeraldPrimary,
                ),
              );
            },
          ),
          const SizedBox(height: 12),

          // 4. Annual Financial Statement
          _buildPrintCard(
            title: 'বাৎসরিক আয়-ব্যয় হিসাব বিবরণী',
            subtitle: 'জুমার নামাজে পঠিতব্য পূর্ণাঙ্গ অডিট ও অডিটোরিয়াম রিপোর্ট',
            icon: Icons.pie_chart_outline,
            color: Colors.blue.shade700,
            onPrint: () {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                  content: Text('বাৎসরিক অডিট রিপোর্ট প্রিন্ট প্রস্তুত হচ্ছে...'),
                  backgroundColor: AppColors.emeraldPrimary,
                ),
              );
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
    required VoidCallback onPrint,
  }) {
    return CardShell(
      padding: const EdgeInsets.all(AppDimens.spacing14),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: color.withOpacity(0.12),
              borderRadius: BorderRadius.circular(10),
            ),
            child: Icon(icon, color: color, size: 24),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                const SizedBox(height: 2),
                Text(subtitle, style: TextStyle(fontSize: 11, color: Colors.grey.shade600)),
              ],
            ),
          ),
          ElevatedButton.icon(
            onPressed: onPrint,
            icon: const Icon(Icons.print, size: 16),
            label: const Text('প্রিন্ট'),
            style: ElevatedButton.styleFrom(
              backgroundColor: color,
              foregroundColor: Colors.white,
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
            ),
          ),
        ],
      ),
    );
  }
}
