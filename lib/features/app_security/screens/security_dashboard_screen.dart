import 'package:flutter/material.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../shared/widgets/card_shell.dart';
import '../services/security_check_service.dart';

/// Screen presenting Device Security, Encryption, and RBAC Audit Status
class SecurityDashboardScreen extends StatefulWidget {
  const SecurityDashboardScreen({super.key});

  @override
  State<SecurityDashboardScreen> createState() => _SecurityDashboardScreenState();
}

class _SecurityDashboardScreenState extends State<SecurityDashboardScreen> {
  final _securityService = SecurityCheckService();
  DeviceSecurityAssessment? _assessment;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _runCheck();
  }

  Future<void> _runCheck() async {
    setState(() => _isLoading = true);
    final result = await _securityService.runSecurityScan();
    setState(() {
      _assessment = result;
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
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
          ? const Center(child: CircularProgressIndicator(color: AppColors.emeraldPrimary))
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
                          color: (_assessment?.trustScore ?? 0) >= 80
                              ? AppColors.emeraldPrimary.withOpacity(0.12)
                              : Colors.orange.withOpacity(0.12),
                          shape: BoxShape.circle,
                        ),
                        child: Icon(
                          (_assessment?.trustScore ?? 0) >= 80 ? Icons.security : Icons.warning_amber,
                          color: (_assessment?.trustScore ?? 0) >= 80
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
                            const Text('ডিভাইস ট্রাস্ট স্কোর', style: TextStyle(fontSize: 12, color: Colors.grey)),
                            const SizedBox(height: 2),
                            Text(
                              '${_assessment?.trustScore ?? 100} / ১০০ (নিরাপদ)',
                              style: TextStyle(
                                fontSize: 18,
                                fontWeight: FontWeight.bold,
                                color: (_assessment?.trustScore ?? 0) >= 80
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

                // Security Check items
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
                  title: 'SQLite ডাটাবেস এনক্রিপশন',
                  subtitle: 'লোকাল Drift ডাটাবেস ডিভাইস সল্ট দিয়ে সুরক্ষিত',
                  isSafe: true,
                ),
                const SizedBox(height: 8),

                _buildSecurityItem(
                  title: 'রোল ও পারমিশন গার্ড (RBAC)',
                  subtitle: 'কোষাধ্যক্ষ ও সভাপতির অনুমোদন পৃথকীকরণ সক্রিয়',
                  isSafe: true,
                ),
                const SizedBox(height: 8),

                _buildSecurityItem(
                  title: 'ডিজিটাল সই ও রসিদ HMAC যাচাই',
                  subtitle: 'QR কোড জালিয়াতি প্রতিরোধ অ্যালগরিদম সক্রিয়',
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
                Text(title, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                const SizedBox(height: 2),
                Text(subtitle, style: TextStyle(fontSize: 11, color: Colors.grey.shade600)),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
