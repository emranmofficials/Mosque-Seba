import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../shared/widgets/card_shell.dart';
import '../providers/prayer_times_provider.dart';

/// Interactive Prayer Time Card with Real-time Waqt Countdown (PrayerCard.kt Parity)
class PrayerCountdownCard extends ConsumerWidget {
  final VoidCallback? onTap;

  const PrayerCountdownCard({super.key, this.onTap});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final prayerDataAsync = ref.watch(prayerTimeStreamProvider);
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return prayerDataAsync.when(
      loading: () => const CardShell(
        child: SizedBox(
          height: 100,
          child: Center(child: CircularProgressIndicator(strokeWidth: 2)),
        ),
      ),
      error: (_, __) => const SizedBox.shrink(),
      data: (data) {
        final next = data.nextSummary;
        final schedule = data.schedule;

        return CardShell(
          onTap: onTap,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Header: Next Prayer Title & Remaining Countdown
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
                          Icons.access_time_filled,
                          size: 16,
                          color: AppColors.goldDark,
                        ),
                      ),
                      const SizedBox(width: MosqueSpace.sm),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'পরবর্তী ওয়াক্ত: ${next.nameBn}',
                            style: const TextStyle(
                              fontSize: 14,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          Text(
                            'ইকামত: ${next.iqamahTimeBn}',
                            style: TextStyle(
                              fontSize: 12,
                              color: isDark ? AppColors.nightTextMuted : AppColors.textSecondary,
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),

                  // Countdown Pill Badge
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: AppColors.emeraldPrimary,
                      borderRadius: BorderRadius.circular(MosqueRadius.pill),
                    ),
                    child: Text(
                      next.remainingTextBn,
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 11,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ],
              ),

              const SizedBox(height: MosqueSpace.md),
              const Divider(height: 1, thickness: 0.5),
              const SizedBox(height: MosqueSpace.sm),

              // 5 Daily Waqts Horizontal Grid
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: schedule.map((item) {
                  final isNext = item.isNext;

                  return Container(
                    padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 6),
                    decoration: BoxDecoration(
                      color: isNext
                          ? (isDark ? AppColors.nightSecondaryContainer : AppColors.goldLight)
                          : Colors.transparent,
                      borderRadius: BorderRadius.circular(MosqueRadius.sm),
                      border: isNext
                          ? Border.all(color: AppColors.gold, width: 1)
                          : null,
                    ),
                    child: Column(
                      children: [
                        Text(
                          item.nameBn,
                          style: TextStyle(
                            fontSize: 11,
                            fontWeight: isNext ? FontWeight.bold : FontWeight.w500,
                            color: isNext
                                ? AppColors.goldDark
                                : (isDark ? AppColors.nightTextPrimary : AppColors.textPrimary),
                          ),
                        ),
                        const SizedBox(height: 2),
                        Text(
                          item.iqamahTime,
                          style: TextStyle(
                            fontSize: 11,
                            fontWeight: isNext ? FontWeight.bold : FontWeight.normal,
                            color: isNext
                                ? (isDark ? AppColors.goldLight : AppColors.emeraldPrimary)
                                : (isDark ? AppColors.nightTextMuted : AppColors.textSecondary),
                          ),
                        ),
                      ],
                    ),
                  );
                }).toList(),
              ),
            ],
          ),
        );
      },
    );
  }
}
