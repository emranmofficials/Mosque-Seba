import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../shared/widgets/card_shell.dart';

/// Digital Smart Board Notice & Instruction Card (SmartBoardCard.kt Parity)
class SmartBoardCard extends StatelessWidget {
  final String categoryBadge;
  final String title;
  final String message;
  final VoidCallback? onTap;

  const SmartBoardCard({
    super.key,
    this.categoryBadge = 'জুমার বিশেষ নির্দেশনা',
    this.title = 'খুতবা শুরু হলে নীরবতা পালন করুন',
    this.message = 'খুতবা চলাকালীন মোবাইল সাইলেন্ট রাখুন এবং মনোযোগ সহকারে বয়ান শ্রবণ করুন।',
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return CardShell(
      onTap: onTap,
      border: Border.all(
        color: isDark ? AppColors.gold.withOpacity(0.4) : AppColors.cardBorderGold,
        width: 1.2,
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: isDark ? AppColors.nightSecondaryContainer : AppColors.goldLight,
              shape: BoxShape.circle,
            ),
            child: const Icon(Icons.campaign, color: AppColors.goldDark, size: 20),
          ),
          const SizedBox(width: MosqueSpace.md),

          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                      decoration: BoxDecoration(
                        color: AppColors.goldLight,
                        borderRadius: BorderRadius.circular(MosqueRadius.xs),
                      ),
                      child: Text(
                        categoryBadge,
                        style: const TextStyle(
                          fontSize: 10,
                          fontWeight: FontWeight.bold,
                          color: AppColors.goldDark,
                        ),
                      ),
                    ),
                    const Icon(Icons.arrow_forward_ios, size: 10, color: AppColors.textMuted),
                  ],
                ),
                const SizedBox(height: 4),
                Text(
                  title,
                  style: const TextStyle(fontSize: 13, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 2),
                Text(
                  message,
                  style: TextStyle(
                    fontSize: 11,
                    color: isDark ? AppColors.nightTextMuted : AppColors.textSecondary,
                  ),
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
