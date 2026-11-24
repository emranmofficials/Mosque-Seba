import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../shared/widgets/card_shell.dart';
import '../models/ramadan_ashra.dart';

/// Sehri & Iftar Live Countdown with Active Ashra Dua
class SehriIftarCountdownWidget extends StatelessWidget {
  final int currentRamadanDay;

  const SehriIftarCountdownWidget({
    super.key,
    this.currentRamadanDay = 1,
  });

  @override
  Widget build(BuildContext context) {
    final ashra = RamadanAshra.fromDay(currentRamadanDay);
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return CardShell(
      border: Border.all(color: AppColors.gold, width: 1.2),
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
                    child: const Icon(Icons.wb_sunny_outlined, size: 16, color: AppColors.goldDark),
                  ),
                  const SizedBox(width: MosqueSpace.sm),
                  Text(
                    'রমজান মুবারক — ${ashra.titleBn}',
                    style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                  ),
                ],
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(
                  color: AppColors.emeraldSoft,
                  borderRadius: BorderRadius.circular(MosqueRadius.pill),
                ),
                child: const Text(
                  'ইফতার বাকি: ০৩ ঘণ্টা ১২ মি.',
                  style: TextStyle(
                    color: AppColors.emeraldPrimary,
                    fontSize: 11,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: MosqueSpace.md),

          // Daily Sehri & Iftar Times
          Row(
            children: [
              Expanded(
                child: Container(
                  padding: const EdgeInsets.all(10),
                  decoration: BoxDecoration(
                    color: isDark ? AppColors.nightSurfaceVariant : AppColors.bgLight,
                    borderRadius: BorderRadius.circular(MosqueRadius.md),
                  ),
                  child: Column(
                    children: [
                      Text('আজকের সেহরি শেষ', style: TextStyle(fontSize: 11, color: isDark ? AppColors.nightTextMuted : AppColors.textSecondary)),
                      const SizedBox(height: 2),
                      const Text('০৪:২৫ ভোর', style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold, color: AppColors.emeraldPrimary)),
                    ],
                  ),
                ),
              ),
              const SizedBox(width: MosqueSpace.md),
              Expanded(
                child: Container(
                  padding: const EdgeInsets.all(10),
                  decoration: BoxDecoration(
                    color: isDark ? AppColors.nightSurfaceVariant : AppColors.bgLight,
                    borderRadius: BorderRadius.circular(MosqueRadius.md),
                  ),
                  child: Column(
                    children: [
                      Text('আজকের ইফতার শুরু', style: TextStyle(fontSize: 11, color: isDark ? AppColors.nightTextMuted : AppColors.textSecondary)),
                      const SizedBox(height: 2),
                      const Text('০৬:১৮ সন্ধ্যা', style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold, color: AppColors.goldDark)),
                    ],
                  ),
                ),
              ),
            ],
          ),

          const SizedBox(height: MosqueSpace.md),

          // Ashra Dua
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: isDark ? AppColors.nightSurfaceVariant : AppColors.goldSoft,
              borderRadius: BorderRadius.circular(MosqueRadius.md),
              border: Border.all(color: AppColors.cardBorderGold),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'দশকের বিশেষ দোয়া (${ashra.subtitleBn}):',
                  style: const TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: AppColors.goldDark),
                ),
                const SizedBox(height: 4),
                Text(
                  ashra.duaArabic,
                  textAlign: TextAlign.right,
                  style: const TextStyle(fontSize: 14, fontWeight: FontWeight.bold, color: AppColors.emeraldPrimary),
                ),
                const SizedBox(height: 4),
                Text(
                  ashra.duaTranslation,
                  style: TextStyle(fontSize: 11, color: isDark ? AppColors.nightTextSecondary : AppColors.textSecondary),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
