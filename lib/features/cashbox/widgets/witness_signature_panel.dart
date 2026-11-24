import 'package:flutter/material.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../shared/widgets/app_text_field.dart';

/// Panel holding inputs for the 3 distinct executive witnesses required for Friday cashbox opening
class WitnessSignaturePanel extends StatelessWidget {
  final TextEditingController witness1Controller;
  final TextEditingController witness2Controller;
  final TextEditingController witness3Controller;

  const WitnessSignaturePanel({
    super.key,
    required this.witness1Controller,
    required this.witness2Controller,
    required this.witness3Controller,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(AppDimens.spacing12),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(AppDimens.radiusMd),
        border: Border.all(color: AppColors.outline),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              const Icon(Icons.verified_user_outlined, color: AppColors.primary, size: 18),
              const SizedBox(width: 6),
              Text(
                '৩ জন প্রত্যক্ষ সাক্ষীর স্বাক্ষর ও প্রত্যয়ন',
                style: AppTypography.titleSmall.copyWith(
                  fontWeight: FontWeight.bold,
                  color: AppColors.primary,
                ),
              ),
            ],
          ),
          const SizedBox(height: 4),
          Text(
            'স্বচ্ছতার নীতি অনুযায়ী পেশ ইমাম, কোষাধ্যক্ষ ও একজন সাধারণ মুসল্লির নাম লিপিবদ্ধ আবশ্যক।',
            style: AppTypography.caption.copyWith(color: AppColors.textSecondary, fontSize: 11),
          ),
          const SizedBox(height: AppDimens.spacing12),
          AppTextField(
            label: '১ম সাক্ষী (পেশ ইমাম / খতিব) *',
            hint: 'যেমন: মাওলানা আব্দুল্লাহ',
            controller: witness1Controller,
          ),
          const SizedBox(height: AppDimens.spacing8),
          AppTextField(
            label: '২য় সাক্ষী (কোষাধ্যক্ষ / সম্পাদক) *',
            hint: 'যেমন: হাজী মো: রফিকুল ইসলাম',
            controller: witness2Controller,
          ),
          const SizedBox(height: AppDimens.spacing8),
          AppTextField(
            label: '৩য় সাক্ষী (উপস্থিত সাধারণ মুসল্লি) *',
            hint: 'যেমন: মোঃ তারেক রহমান',
            controller: witness3Controller,
          ),
        ],
      ),
    );
  }
}
