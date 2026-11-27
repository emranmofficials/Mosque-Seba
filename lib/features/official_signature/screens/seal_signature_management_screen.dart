import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/widgets/card_shell.dart';
import '../../authentication/providers/auth_provider.dart';
import '../dialogs/signature_pad_dialog.dart';
import '../services/signature_storage_service.dart';

/// Screen for managing the Official Mosque Seal, Stamp, and Authorized Signatures
class SealSignatureManagementScreen extends ConsumerStatefulWidget {
  const SealSignatureManagementScreen({super.key});

  @override
  ConsumerState<SealSignatureManagementScreen> createState() =>
      _SealSignatureManagementScreenState();
}

class _SealSignatureManagementScreenState
    extends ConsumerState<SealSignatureManagementScreen> {
  final _service = SignatureStorageService();
  final _picker = ImagePicker();

  Uint8List? _presidentSign;
  Uint8List? _secretarySign;
  Uint8List? _treasurerSign;
  Uint8List? _sealBytes;

  @override
  void initState() {
    super.initState();
    _loadSavedAssets();
  }

  Future<void> _loadSavedAssets() async {
    final pres = await _service.getSignature('president');
    final sec = await _service.getSignature('general_secretary');
    final treas = await _service.getSignature('treasurer');
    final seal = await _service.getSeal();
    if (!mounted) return;
    setState(() {
      _presidentSign = pres;
      _secretarySign = sec;
      _treasurerSign = treas;
      _sealBytes = seal;
    });
  }

  Future<void> _pickSealImage() async {
    try {
      final picked = await _picker.pickImage(
        source: ImageSource.gallery,
        maxWidth: 800,
        maxHeight: 800,
        imageQuality: 90,
      );
      if (picked == null) return;
      final bytes = await picked.readAsBytes();
      await _service.saveSeal(bytes);
      if (!mounted) return;
      setState(() => _sealBytes = bytes);
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('অফিসিয়াল সিল সফলভাবে সংরক্ষিত হয়েছে'),
          backgroundColor: AppColors.emeraldPrimary,
        ),
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('সিল নির্বাচনে ত্রুটি হয়েছে: $e'),
          backgroundColor: Colors.red,
        ),
      );
    }
  }

  Future<void> _removeSeal() async {
    await _service.deleteSeal();
    if (!mounted) return;
    setState(() => _sealBytes = null);
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('অফিসিয়াল সিল মুছে ফেলা হয়েছে')),
    );
  }

  void _openSignDialog(String title, String role, String roleCode) {
    showDialog<void>(
      context: context,
      builder: (ctx) => SignaturePadDialog(
        title: title,
        signatoryRole: role,
        onSavePng: (pngBytes) async {
          await _service.saveSignature(
            roleCode: roleCode,
            signatureBytes: pngBytes,
          );
          if (!mounted) return;
          setState(() {
            if (roleCode == 'president') _presidentSign = pngBytes;
            if (roleCode == 'treasurer') _treasurerSign = pngBytes;
            if (roleCode == 'general_secretary') _secretarySign = pngBytes;
          });
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('$role-এর স্বাক্ষর এনক্রিপ্ট করে সংরক্ষিত হয়েছে'),
              backgroundColor: AppColors.emeraldPrimary,
            ),
          );
        },
      ),
    );
  }

  Future<void> _deleteSignature(String roleCode, String roleLabel) async {
    await _service.deleteSignature(roleCode);
    if (!mounted) return;
    setState(() {
      if (roleCode == 'president') _presidentSign = null;
      if (roleCode == 'treasurer') _treasurerSign = null;
      if (roleCode == 'general_secretary') _secretarySign = null;
    });
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('$roleLabel-এর স্বাক্ষর মুছে ফেলা হয়েছে')),
    );
  }

  @override
  Widget build(BuildContext context) {
    final authUser = ref.watch(authNotifierProvider).valueOrNull;
    final mosqueName = (authUser?.mosqueNameBn.isNotEmpty ?? false)
        ? authUser!.mosqueNameBn
        : 'মসজিদ সেবা কেন্দ্রীয় মসজিদ';
    final committeeId = (authUser?.committeeId.isNotEmpty ?? false)
        ? BengaliNumberFormatter.toBengali(authUser!.committeeId)
        : 'MS-2026';

    return Scaffold(
      appBar: AppBar(
        title: const Text('অফিসিয়াল সিল ও ডিজিটাল স্বাক্ষর'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(AppDimens.spacing16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
          // Mosque Seal Card
          CardShell(
            padding: const EdgeInsets.all(AppDimens.spacing16),
            child: Column(
              children: [
                Container(
                  width: 96,
                  height: 96,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    border: Border.all(color: AppColors.emeraldPrimary, width: 2),
                    color: AppColors.emeraldPrimary.withOpacity(0.08),
                  ),
                  clipBehavior: Clip.antiAlias,
                  child: _sealBytes != null
                      ? Image.memory(
                          _sealBytes!,
                          fit: BoxFit.cover,
                          errorBuilder: (_, __, ___) => const Icon(
                            Icons.verified,
                            color: AppColors.emeraldPrimary,
                            size: 36,
                          ),
                        )
                      : Center(
                          child: Column(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              const Icon(Icons.verified, color: AppColors.emeraldPrimary, size: 36),
                              Text(
                                'অফিসিয়াল সিল',
                                style: Theme.of(context).textTheme.labelSmall?.copyWith(
                                  fontWeight: FontWeight.bold,
                                  color: AppColors.emeraldPrimary,
                                ),
                              ),
                            ],
                          ),
                        ),
                ),
                const SizedBox(height: 12),
                Text(
                  mosqueName,
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 2),
                Text(
                  'কমিটি আইডি: $committeeId • রসিদ ও PDF-এ স্বয়ংক্রিয়ভাবে যুক্ত হবে',
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(color: Colors.grey),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 14),
                Wrap(
                  spacing: 8,
                  children: [
                    OutlinedButton.icon(
                      onPressed: _pickSealImage,
                      icon: const Icon(Icons.upload_file, size: 16),
                      label: Text(_sealBytes == null ? 'সিল আপলোড করুন' : 'সিল পরিবর্তন'),
                    ),
                    if (_sealBytes != null)
                      OutlinedButton.icon(
                        onPressed: _removeSeal,
                        icon: const Icon(Icons.delete_outline, size: 16, color: Colors.red),
                        label: const Text('মুছুন', style: TextStyle(color: Colors.red)),
                      ),
                  ],
                ),
              ],
            ),
          ),
          const SizedBox(height: 20),

          // Authorized Signatures Section
          Text(
            'কমিটি অনুমোদিত ডিজিটাল স্বাক্ষরসমূহ',
            style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 4),
          Text(
            'রসিদ, ভাউচার ও রিপোর্ট কার্ড PDF-এ স্বয়ংক্রিয়ভাবে যুক্ত হবে',
            style: Theme.of(context).textTheme.bodySmall?.copyWith(color: Colors.grey),
          ),
          const SizedBox(height: 12),

          _buildSignatoryTile(
            title: 'সভাপতি মহোদয়ের স্বাক্ষর',
            role: 'মুহতারাম সভাপতি',
            signatureBytes: _presidentSign,
            onTap: () => _openSignDialog(
              'সভাপতির স্বাক্ষর',
              'মুহতারাম সভাপতি',
              'president',
            ),
            onDelete: () => _deleteSignature('president', 'সভাপতি'),
          ),
          const SizedBox(height: 10),

          _buildSignatoryTile(
            title: 'সাধারণ সম্পাদকের স্বাক্ষর',
            role: 'সাধারণ সম্পাদক',
            signatureBytes: _secretarySign,
            onTap: () => _openSignDialog(
              'সাধারণ সম্পাদকের স্বাক্ষর',
              'সাধারণ সম্পাদক',
              'general_secretary',
            ),
            onDelete: () => _deleteSignature('general_secretary', 'সাধারণ সম্পাদক'),
          ),
          const SizedBox(height: 10),

          _buildSignatoryTile(
            title: 'কোষাধ্যক্ষের স্বাক্ষর',
            role: 'অর্থ সম্পাদক / কোষাধ্যক্ষ',
            signatureBytes: _treasurerSign,
            onTap: () => _openSignDialog(
              'কোষাধ্যক্ষের স্বাক্ষর',
              'কোষাধ্যক্ষ',
              'treasurer',
            ),
            onDelete: () => _deleteSignature('treasurer', 'কোষাধ্যক্ষ'),
          ),
        ],
      ),
    ),
  );
  }

  Widget _buildSignatoryTile({
    required String title,
    required String role,
    required Uint8List? signatureBytes,
    required VoidCallback onTap,
    required VoidCallback onDelete,
  }) {
    final isConfigured = signatureBytes != null && signatureBytes.isNotEmpty;
    final hasValidPng =
        isConfigured && SignatureStorageService.isPngBytes(signatureBytes);

    return CardShell(
      onTap: onTap,
      padding: const EdgeInsets.all(AppDimens.spacing14),
      child: Column(
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: (isConfigured ? AppColors.emeraldPrimary : Colors.grey)
                      .withOpacity(0.12),
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
                    Text(
                      title,
                      style: Theme.of(context).textTheme.titleSmall?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      role,
                      style: Theme.of(context).textTheme.labelSmall?.copyWith(color: Colors.grey.shade600),
                    ),
                  ],
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                decoration: BoxDecoration(
                  color: (isConfigured ? AppColors.emeraldPrimary : Colors.orange)
                      .withOpacity(0.12),
                  borderRadius: BorderRadius.circular(6),
                ),
                child: Text(
                  isConfigured ? 'সংরক্ষিত' : 'স্বাক্ষর দিন',
                  style: Theme.of(context).textTheme.labelSmall?.copyWith(
                    fontWeight: FontWeight.bold,
                    color: isConfigured
                        ? AppColors.emeraldPrimary
                        : Colors.orange.shade800,
                  ),
                ),
              ),
              if (isConfigured)
                IconButton(
                  icon: const Icon(Icons.delete_outline, size: 18, color: Colors.red),
                  onPressed: onDelete,
                  tooltip: 'স্বাক্ষর মুছুন',
                ),
            ],
          ),
          if (hasValidPng) ...[
            const SizedBox(height: 10),
            Container(
              height: 54,
              width: double.infinity,
              padding: const EdgeInsets.all(6),
              decoration: BoxDecoration(
                color: Colors.grey.shade50,
                borderRadius: BorderRadius.circular(8),
                border: Border.all(color: Colors.grey.shade200),
              ),
              child: Image.memory(signatureBytes, fit: BoxFit.contain),
            ),
          ] else if (isConfigured) ...[
            const SizedBox(height: 6),
            Align(
              alignment: Alignment.centerLeft,
              child: Text(
                'প্রিভিউ দেখতে অনুগ্রহ করে পুনরায় স্বাক্ষর আঁকুন',
                style: Theme.of(context).textTheme.labelSmall?.copyWith(color: Colors.orange),
              ),
            ),
          ],
        ],
      ),
    );
  }
}
