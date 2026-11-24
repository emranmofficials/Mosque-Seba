import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../shared/widgets/card_shell.dart';
import '../providers/hadith_provider.dart';

/// Devotional Sahih Hadith Digital Board Card (SmartBoardCard.kt Parity)
class HadithBoardWidget extends ConsumerWidget {
  const HadithBoardWidget({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final hadith = ref.watch(todayHadithProvider);
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return CardShell(
      border: Border.all(
        color: isDark ? AppColors.gold.withOpacity(0.3) : AppColors.cardBorderGold,
        width: 1.2,
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(6),
                    decoration: BoxDecoration(
                      color: isDark ? AppColors.nightSecondaryContainer : AppColors.goldLight,
                      shape: BoxShape.circle,
                    ),
                    child: const Icon(
                      Icons.format_quote,
                      size: 16,
                      color: AppColors.goldDark,
                    ),
                  ),
                  const SizedBox(width: MosqueSpace.sm),
                  const Text(
                    'আজকের সহিহ হাদিস',
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.bold,
                      color: AppColors.goldDark,
                    ),
                  ),
                ],
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(
                  color: isDark ? AppColors.nightSurfaceVariant : AppColors.bgLight,
                  borderRadius: BorderRadius.circular(MosqueRadius.pill),
                  border: Border.all(color: AppColors.cardBorderSubtle),
                ),
                child: Text(
                  '${hadith.sourceBookBn} : ${hadith.hadithNumberBn}',
                  style: TextStyle(
                    fontSize: 10,
                    fontWeight: FontWeight.bold,
                    color: isDark ? AppColors.nightTextMuted : AppColors.textSecondary,
                  ),
                ),
              ),
            ],
          ),

          if (hadith.arabicText != null) ...[
            const SizedBox(height: MosqueSpace.md),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              decoration: BoxDecoration(
                color: isDark ? AppColors.nightSurfaceVariant : AppColors.bgLight,
                borderRadius: BorderRadius.circular(MosqueRadius.sm),
              ),
              child: Text(
                hadith.arabicText!,
                textAlign: TextAlign.right,
                style: const TextStyle(
                  fontSize: 15,
                  fontWeight: FontWeight.bold,
                  height: 1.6,
                  color: AppColors.emeraldPrimary,
                ),
              ),
            ),
          ],

          const SizedBox(height: MosqueSpace.md),

          // Bengali Translation
          Text(
            '“${hadith.translationBn}”',
            style: TextStyle(
              fontSize: 13,
              height: 1.5,
              fontWeight: FontWeight.w500,
              color: isDark ? AppColors.nightTextPrimary : AppColors.textPrimary,
            ),
          ),

          const SizedBox(height: MosqueSpace.sm),

          // Narrator Citation
          Align(
            alignment: Alignment.centerRight,
            child: Text(
              '— ${hadith.narratorBn}',
              style: TextStyle(
                fontSize: 11,
                fontStyle: FontStyle.italic,
                color: isDark ? AppColors.nightTextMuted : AppColors.textMuted,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
