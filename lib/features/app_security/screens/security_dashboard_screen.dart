import 'package:flutter/material.dart';
import '../../../core/services/biometric_auth_service.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/widgets/card_shell.dart';
import '../security/encryption_helper.dart';
import '../services/security_check_service.dart';

/// Screen presenting Device Security, Biometrics, PIN Lock & QR HMAC Verification
class SecurityDashboardScreen extends StatefulWidget {
  const SecurityDashboardScreen({super.key});

  @override
  State<SecurityDashboardScreen> createState() =>
      _SecurityDashboardScreenState();
}

class _SecurityDashboardScreenState extends State<SecurityDashboardScreen> {
  final _securityService = SecurityCheckService();
  final _biometricAuth = BiometricAuthService.instance;
  final _qrInputController = TextEditingController();

  DeviceSecurityAssessment? _assessment;
  bool _isLoading = true;
  bool _bioAvailable = false;
  bool _bioEnabled = false;
  bool _pinEnabled = false;
  String? _qrVerifyResult;
  bool? _qrVerifyValid;

  @override
  void initState() {
    super.initState();
    _runCheck();
  }

  @override
  void dispose() {
    _qrInputController.dispose();
    super.dispose();
  }

  Future<void> _runCheck() async {
    setState(() => _isLoading = true);
    final result = await _securityService.runSecurityScan();
    final bioAvail = await _biometricAuth.isBiometricHardwareAvailable();
    final bioOn = await _biometricAuth.isBiometricLockEnabled();
    final pinOn = await _biometricAuth.isPinLockEnabled();
    if (!mounted) return;
    setState(() {
      _assessment = result;
      _bioAvailable = bioAvail;
      _bioEnabled = bioOn;
      _pinEnabled = pinOn;
      _isLoading = false;
    });
  }

  Future<void> _toggleBiometrics(bool enabled) async {
    if (enabled) {
      if (!_bioAvailable) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('এই ডিভাইসে বায়োমেট্রিক হার্ডওয়্যার সক্রিয় নেই'),
            backgroundColor: Colors.red,
          ),
        );
        return;
      }
      final verified = await _biometricAuth.authenticateWithBiometrics(
        reason: 'বায়োমেট্রিক লক চালু করতে যাচাই করুন',
      );
      if (!verified || !mounted) return;
    }

    await _biometricAuth.setBiometricLockEnabled(enabled);
    if (!mounted) return;
    setState(() => _bioEnabled = enabled);
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(
          enabled ? 'বায়োমেট্রিক লক চালু হয়েছে' : 'বায়োমেট্রিক লক বন্ধ হয়েছে',
        ),
        backgroundColor: AppColors.emeraldPrimary,
      ),
    );
  }

  Future<void> _configurePin() async {
    final pinCtrl = TextEditingController();
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(_pinEnabled ? 'কমিটি পিন পরিবর্তন' : 'কমিটি পিন সেট করুন'),
        content: TextField(
          controller: pinCtrl,
          obscureText: true,
          keyboardType: TextInputType.number,
          maxLength: 6,
          decoration: const InputDecoration(
            labelText: '৪-৬ সংখ্যার পিন লিখুন',
            border: OutlineInputBorder(),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(false),
            child: const Text('বাতিল'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.of(ctx).pop(true),
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.emeraldPrimary,
              foregroundColor: Colors.white,
            ),
            child: const Text('সংরক্ষণ'),
          ),
        ],
      ),
    );
    final pin = pinCtrl.text.trim();
    pinCtrl.dispose();

    if (confirmed == true) {
      if (pin.length < 4) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('কমপক্ষে ৪ সংখ্যার পিন দিন')),
          );
        }
        return;
      }
      await _biometricAuth.enableCommitteePin(pin);
      if (!mounted) return;
      setState(() => _pinEnabled = true);
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('কমিটি অ্যাপ পিন সফলভাবে সেট করা হয়েছে'),
          backgroundColor: AppColors.emeraldPrimary,
        ),
      );
    }
  }

  Future<void> _disablePin() async {
    await _biometricAuth.disableCommitteePin();
    if (!mounted) return;
    setState(() => _pinEnabled = false);
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('কমিটি পিন লক বন্ধ করা হয়েছে')),
    );
  }

  void _verifyQrString() {
    final raw = _qrInputController.text.trim();
    // Format: MOSQUE_SEBA:CHANDA:$receiptCode:$amount:$memberId:$sig
    final parts = raw.split(':');
    if (parts.length == 6 && parts[0] == 'MOSQUE_SEBA') {
      final receiptNo = parts[2];
      final amount = double.tryParse(parts[3]) ?? -1;
      final memberCode = parts[4];
      final sig = parts[5];
      final valid = EncryptionHelper.verifyReceiptCode(
        receiptNo: receiptNo,
        amount: amount,
        memberCode: memberCode,
        signature: sig,
      );
      setState(() {
        _qrVerifyValid = valid;
        _qrVerifyResult = valid
            ? 'বৈধ রসিদ! ($receiptNo • ৳$amount)'
            : 'সতর্কতা: রসিদের HMAC স্বাক্ষর মেলেনি (সম্ভাব্য জাল কোড)।';
      });
    } else {
      setState(() {
        _qrVerifyValid = false;
        _qrVerifyResult = 'অবৈধ বা স্বাক্ষরবিহীন QR ফরম্যাট।';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final score = _assessment?.trustScore ?? 100;
    final isSafeScore = score >= 80;

    return Scaffold(
      appBar: AppBar(
        title: const Text('অ্যাপ্লিকেশন নিরাপত্তা ও অডিট'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _runCheck,
          ),
        ],
      ),
      body: _isLoading
          ? const Center(
              child: CircularProgressIndicator(color: AppColors.emeraldPrimary),
            )
          : ListView(
              padding: const EdgeInsets.all(AppDimens.spacing16),
              children: [
                // Trust Score Banner
                CardShell(
                  padding: const EdgeInsets.all(AppDimens.spacing16),
                  child: Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: (isSafeScore
                                  ? AppColors.emeraldPrimary
                                  : Colors.orange)
                              .withOpacity(0.12),
                          shape: BoxShape.circle,
                        ),
                        child: Icon(
                          isSafeScore ? Icons.security : Icons.warning_amber,
                          color: isSafeScore
                              ? AppColors.emeraldPrimary
                              : Colors.orange.shade800,
                          size: 32,
                        ),
                      ),
                      const SizedBox(width: 14),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text(
                              'ডিভাইস ট্রাস্ট স্কোর',
                              style: TextStyle(fontSize: 12, color: Colors.grey),
                            ),
                            const SizedBox(height: 2),
                            Text(
                              '${BengaliNumberFormatter.toBengali(score)} / ১০০ (${isSafeScore ? 'নিরাপদ' : 'সতর্কতা'})',
                              style: TextStyle(
                                fontSize: 18,
                                fontWeight: FontWeight.bold,
                                color: isSafeScore
                                    ? AppColors.emeraldPrimary
                                    : Colors.orange.shade800,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 16),

                // Interactive App Lock Controls
                const Text(
                  'অ্যাপ লক ও প্রবেশ নিয়ন্ত্রণ',
                  style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 10),
                CardShell(
                  padding: const EdgeInsets.all(AppDimens.spacing14),
                  child: Column(
                    children: [
                      SwitchListTile.adaptive(
                        contentPadding: EdgeInsets.zero,
                        secondary: const Icon(Icons.fingerprint, color: AppColors.emeraldPrimary),
                        title: const Text(
                          'বায়োমেট্রিক / ফিঙ্গারপ্রিন্ট লক',
                          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                        ),
                        subtitle: Text(
                          _bioAvailable
                              ? 'অ্যাপ চালুর সময় ফিঙ্গারপ্রিন্ট বা ফেস যাচাই'
                              : 'ডিভাইসে বায়োমেট্রিক অনুপলব্ধ',
                          style: const TextStyle(fontSize: 11),
                        ),
                        value: _bioEnabled,
                        activeColor: AppColors.emeraldPrimary,
                        onChanged: _toggleBiometrics,
                      ),
                      const Divider(),
                      ListTile(
                        contentPadding: EdgeInsets.zero,
                        leading: const Icon(Icons.pin_outlined, color: AppColors.emeraldPrimary),
                        title: const Text(
                          'কমিটি সিকিউরিটি পিন (SHA-256)',
                          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                        ),
                        subtitle: Text(
                          _pinEnabled
                              ? 'সক্রিয় • এনক্রিপ্টেড কীস্টোরে সংরক্ষিত'
                              : 'পিন সেট করা নেই',
                          style: const TextStyle(fontSize: 11),
                        ),
                        trailing: Wrap(
                          spacing: 6,
                          children: [
                            ElevatedButton(
                              onPressed: _configurePin,
                              style: ElevatedButton.styleFrom(
                                backgroundColor: AppColors.emeraldPrimary,
                                foregroundColor: Colors.white,
                              ),
                              child: Text(_pinEnabled ? 'পরিবর্তন' : 'সেট করুন', style: const TextStyle(fontSize: 12)),
                            ),
                            if (_pinEnabled)
                              TextButton(
                                onPressed: _disablePin,
                                child: const Text('বন্ধ', style: TextStyle(color: Colors.red, fontSize: 12)),
                              ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 18),

                // Receipt HMAC Verifier
                const Text(
                  'রসিদ QR / HMAC সত্যতা যাচাই',
                  style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 8),
                CardShell(
                  padding: const EdgeInsets.all(AppDimens.spacing14),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      TextField(
                        controller: _qrInputController,
                        decoration: const InputDecoration(
                          labelText: 'রসিদ QR কোড স্ট্রিং লিখুন',
                          hintText: 'MOSQUE_SEBA:CHANDA:RCP-...:500.0:...',
                          border: OutlineInputBorder(),
                          isDense: true,
                        ),
                      ),
                      const SizedBox(height: 10),
                      ElevatedButton.icon(
                        onPressed: _verifyQrString,
                        icon: const Icon(Icons.verified_outlined, size: 16),
                        label: const Text('HMAC স্বাক্ষর যাচাই করুন'),
                        style: ElevatedButton.styleFrom(
                          backgroundColor: AppColors.emeraldPrimary,
                          foregroundColor: Colors.white,
                        ),
                      ),
                      if (_qrVerifyResult != null) ...[
                        const SizedBox(height: 8),
                        Text(
                          _qrVerifyResult!,
                          style: TextStyle(
                            fontSize: 12,
                            fontWeight: FontWeight.bold,
                            color: _qrVerifyValid == true
                                ? AppColors.emeraldPrimary
                                : Colors.red,
                          ),
                        ),
                      ],
                    ],
                  ),
                ),
                const SizedBox(height: 18),

                // Honest Security Audit Summary
                const Text(
                  'নিরাপত্তা যাচাইসমূহ',
                  style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 10),

                _buildSecurityItem(
                  title: 'রুট বা টেম্পারিং সুরক্ষা',
                  subtitle: _assessment?.isRootedOrCompromised == true
                      ? 'ডিভাইসে রুট প্রিভিলেজ শনাক্ত হয়েছে'
                      : 'ডিভাইস সুরক্ষিত ও আন-রুটেড',
                  isSafe: _assessment?.isRootedOrCompromised != true,
                ),
                const SizedBox(height: 8),

                _buildSecurityItem(
                  title: 'সুরক্ষিত কী-স্টোর (FlutterSecureStorage)',
                  subtitle: 'অথ টোকেন, পিন হ্যাশ ও ডিজিটাল স্বাক্ষর এনক্রিপ্টেড কী-স্টোরে সংরক্ষিত',
                  isSafe: true,
                ),
                const SizedBox(height: 8),

                _buildSecurityItem(
                  title: 'রোল ও পারমিশন গার্ড (RBAC)',
                  subtitle: '১৬টি রোল ও স্ব-অনুমোদন প্রতিরোধ নীতি সক্রিয়',
                  isSafe: true,
                ),
                const SizedBox(height: 8),

                _buildSecurityItem(
                  title: 'রসিদ HMAC-SHA256 যাচাই',
                  subtitle: 'চাঁদা ও অনুদান রসিদে ডিজিটাল HMAC স্বাক্ষর যুক্ত হচ্ছে',
                  isSafe: true,
                ),
              ],
            ),
    );
  }

  Widget _buildSecurityItem({
    required String title,
    required String subtitle,
    required bool isSafe,
  }) {
    return CardShell(
      padding: const EdgeInsets.all(AppDimens.spacing14),
      child: Row(
        children: [
          Icon(
            isSafe ? Icons.check_circle : Icons.error,
            color: isSafe ? AppColors.emeraldPrimary : Colors.red,
            size: 22,
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
    );
  }
}
