import 'package:flutter/material.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../shared/widgets/card_shell.dart';
import '../dialogs/signature_pad_dialog.dart';
import '../services/signature_storage_service.dart';

/// Screen for managing the Official Mosque Seal, Stamp, and Authorized Signatures
class SealSignatureManagementScreen extends StatefulWidget {
  const SealSignatureManagementScreen({super.key});

  @override
  State<SealSignatureManagementScreen> createState() => _SealSignatureManagementScreenState();
}

class _SealSignatureManagementScreenState extends State<SealSignatureManagementScreen> {
  final _service = SignatureStorageService();
  bool _hasPresidentSign = true;
  bool _hasTreasurerSign = true;
  bool _hasSecretarySign = false;

  void _openSignDialog(String title, String role) {
    showDialog(
      context: context,
      builder: (ctx) => SignaturePadDialog(
        title: title,
        signatoryRole: role,
        onSave: (points) {
          setState(() {
            if (role.contains('সভাপতি')) _hasPresidentSign = true;
            if (role.contains('কোষাধ্যক্ষ')) _hasTreasurerSign = true;
            if (role.contains('সম্পাদক')) _hasSecretarySign = true;
          });
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('$role-এর স্বাক্ষর সফলভাবে এনক্রিপ্ট করে সংরক্ষিত হয়েছে'),
              backgroundColor: AppColors.emeraldPrimary,
            ),
          );
        },
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('অফিসিয়াল সিল ও ডিজিটাল স্বাক্ষর'),
      ),
      body: ListView(
        padding: const EdgeInsets.all(AppDimens.spacing16),
        children: [
          // Mosque Seal Card
          CardShell(
            padding: const EdgeInsets.all(AppDimens.spacing16),
            child: Column(
              children: [
                Container(
                  width: 90,
                  height: 90,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    border: Border.all(color: AppColors.emeraldPrimary, width: 2),
                    color: AppColors.emeraldPrimary.withOpacity(0.08),
                  ),
                  child: const Center(
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(Icons.verified, color: AppColors.emeraldPrimary, size: 36),
                        Text('অফিসিয়াল সিল', style: TextStyle(fontSize: 10, fontWeight: FontWeight.bold, color: AppColors.emeraldPrimary)),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 12),
                const Text(
                  'বায়তুল মুকাররম মডেল জামে মসজিদ',
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 2),
                const Text('স্থাপিত: ১৯৯৫ খ্রি. • ওয়াকফ রেজি নং: ৪২৪৫/ঢাকা', style: TextStyle(fontSize: 12, color: Colors.grey)),
                const SizedBox(height: 14),
                OutlinedButton.icon(
                  onPressed: () {
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(
                        content: Text('মসজিদ সিল ফাইল আপলোড ডায়ালগ খোলা হচ্ছে...'),
                        backgroundColor: AppColors.emeraldPrimary,
                      ),
                    );
                  },
                  icon: const Icon(Icons.upload_file, size: 16),
                  label: const Text('নতুন সিল আপলোড করুন'),
                ),
              ],
            ),
          ),
          const SizedBox(height: 20),

          // Authorized Signatures Section
          const Text(
            'কমিটি অনুমোদিত ডিজিটাল স্বাক্ষরসমূহ',
            style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 4),
          const Text(
            'রসিদ, ভাউচার ও নোটিশে ব্যবহারের জন্য এনক্রিপ্টেড স্বাক্ষর',
            style: TextStyle(fontSize: 12, color: Colors.grey),
          ),
          const SizedBox(height: 12),

          _buildSignatoryTile(
            title: 'সভাপতি মহোদয়ের স্বাক্ষর',
            role: 'মুহতারাম সভাপতি',
            isConfigured: _hasPresidentSign,
            onTap: () => _openSignDialog('সভাপতির স্বাক্ষর', 'মুহতারাম সভাপতি'),
          ),
          const SizedBox(height: 10),

          _buildSignatoryTile(
            title: 'সাধারণ সম্পাদকের স্বাক্ষর',
            role: 'সাধারণ সম্পাদক',
            isConfigured: _hasSecretarySign,
            onTap: () => _openSignDialog('সাধারণ সম্পাদকের স্বাক্ষর', 'সাধারণ সম্পাদক'),
          ),
          const SizedBox(height: 10),

          _buildSignatoryTile(
            title: 'কোষাধ্যক্ষের স্বাক্ষর',
            role: 'অর্থ সম্পাদক / কোষাধ্যক্ষ',
            isConfigured: _hasTreasurerSign,
            onTap: () => _openSignDialog('কোষাধ্যক্ষের স্বাক্ষর', 'কোষাধ্যক্ষ'),
          ),
        ],
      ),
    );
  }

  Widget _buildSignatoryTile({
    required String title,
    required String role,
    required bool isConfigured,
    required VoidCallback onTap,
  }) {
    return CardShell(
      onTap: onTap,
      padding: const EdgeInsets.all(AppDimens.spacing14),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: (isConfigured ? AppColors.emeraldPrimary : Colors.grey).withOpacity(0.12),
              shape: BoxShape.circle,
            ),
            child: Icon(
              isConfigured ? Icons.draw : Icons.gesture,
              color: isConfigured ? AppColors.emeraldPrimary : Colors.grey,
              size: 22,
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                const SizedBox(height: 2),
                Text(role, style: TextStyle(fontSize: 11, color: Colors.grey.shade600)),
              ],
            ),
          ),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
            decoration: BoxDecoration(
              color: (isConfigured ? AppColors.emeraldPrimary : Colors.orange).withOpacity(0.12),
              borderRadius: BorderRadius.circular(6),
            ),
            child: Text(
              isConfigured ? 'সংরক্ষিত' : 'স্বাক্ষর দিন',
              style: TextStyle(
                fontSize: 11,
                fontWeight: FontWeight.bold,
                color: isConfigured ? AppColors.emeraldPrimary : Colors.orange.shade800,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
