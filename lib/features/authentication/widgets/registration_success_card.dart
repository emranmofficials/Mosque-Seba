import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../shared/widgets/app_button.dart';
import '../../../shared/widgets/card_shell.dart';

/// Success Card displayed upon successful Mosque & Committee Registration
class RegistrationSuccessCard extends StatelessWidget {
  final String committeeId;
  final VoidCallback onEnterDashboard;

  const RegistrationSuccessCard({
    super.key,
    required this.committeeId,
    required this.onEnterDashboard,
  });

  @override
  Widget build(BuildContext context) {
    return CardShell(
      child: Column(
        children: [
          Container(
            padding: const EdgeInsets.all(16),
            decoration: const BoxDecoration(
              shape: BoxShape.circle,
              color: AppColors.emeraldSoft,
            ),
            child: const Icon(
              Icons.check_circle_outline,
              size: 56,
              color: AppColors.emeraldPrimary,
            ),
          ),
          const SizedBox(height: MosqueSpace.md),
          const Text(
            'অভিনন্দন! আপনার মসজিদ সফলভাবে নিবন্ধিত হয়েছে।',
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: MosqueSpace.sm),
          const Text(
            'নিচের ইউনিক কমিটি আইডিটি সংরক্ষণ করুন। কমিটির অন্য সদস্য ও মুসল্লিগণ এই আইডি দিয়ে লগইন করবেন।',
            style: TextStyle(fontSize: 13, color: AppColors.textSecondary),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: MosqueSpace.lg),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
            decoration: BoxDecoration(
              color: AppColors.goldLight,
              borderRadius: BorderRadius.circular(MosqueRadius.medium),
              border: Border.all(color: AppColors.gold, width: 1.5),
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  committeeId,
                  style: const TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                    letterSpacing: 2,
                    color: AppColors.goldDark,
                  ),
                ),
                const SizedBox(width: 12),
                IconButton(
                  icon: const Icon(Icons.copy, color: AppColors.goldDark, size: 20),
                  onPressed: () {
                    Clipboard.setData(ClipboardData(text: committeeId));
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('কমিটি আইডি কপি করা হয়েছে।')),
                    );
                  },
                ),
              ],
            ),
          ),
          const SizedBox(height: MosqueSpace.xl),
          AppButton(
            label: 'ড্যাশবোর্ডে প্রবেশ করুন',
            icon: Icons.dashboard,
            onPressed: onEnterDashboard,
          ),
        ],
      ),
    );
  }
}
