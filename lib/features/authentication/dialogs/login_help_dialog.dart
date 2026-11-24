import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../shared/widgets/app_button.dart';

/// Help Dialog explaining Committee vs Musalli login flows
class LoginHelpDialog extends StatelessWidget {
  const LoginHelpDialog({super.key});

  @override
  Widget build(BuildContext context) {
    return Dialog(
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(MosqueRadius.xl),
      ),
      child: Padding(
        padding: const EdgeInsets.all(MosqueSpace.xl),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Row(
              children: [
                Icon(Icons.help_outline, color: AppColors.goldDeep, size: 24),
                SizedBox(width: MosqueSpace.sm),
                Text(
                  'কীভাবে লগইন করবেন?',
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                ),
              ],
            ),
            const SizedBox(height: MosqueSpace.md),
            const Text(
              '১. সভাপতি ও সাধারণ সম্পাদক:\nরেজিস্ট্রেশনের সময় প্রদান করা ইমেইল ও পাসওয়ার্ড দিয়ে প্রথম ট্যাবে লগইন করুন।',
              style: TextStyle(fontSize: 13, height: 1.4),
            ),
            const SizedBox(height: MosqueSpace.sm),
            const Text(
              '২. কার্যনির্বাহী কমিটি ও সাধারণ মুসল্লি:\nমসজিদের কমিটি কোড (যেমন: MS-2026), সদস্য তালিকায় নিবন্ধিত মোবাইল নম্বর ও পাসওয়ার্ড দিয়ে দ্বিতীয় ট্যাবে প্রবেশ করুন।',
              style: TextStyle(fontSize: 13, height: 1.4),
            ),
            const SizedBox(height: MosqueSpace.lg),
            Align(
              alignment: Alignment.centerRight,
              child: AppButton(
                label: 'ঠিক আছে',
                width: 100,
                onPressed: () => Navigator.of(context).pop(),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
