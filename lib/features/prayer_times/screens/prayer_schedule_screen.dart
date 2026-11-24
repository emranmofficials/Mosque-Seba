import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../shared/widgets/card_shell.dart';
import '../providers/prayer_times_provider.dart';

/// Full Prayer Times & Jamaat Schedule Screen
class PrayerScheduleScreen extends ConsumerWidget {
  const PrayerScheduleScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final prayerDataAsync = ref.watch(prayerTimeStreamProvider);
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Scaffold(
      appBar: AppBar(
        title: const Text('নামাজের স্থায়ী সময়সূচি'),
      ),
      body: prayerDataAsync.when(
        data: (data) {
          final schedule = data.schedule;

          return ListView(
            padding: const EdgeInsets.all(MosqueContentPadding.screenHorizontal),
            children: [
              CardShell(
                color: isDark ? AppColors.nightSurface : AppColors.emeraldSoft,
                border: Border.all(color: AppColors.emeraldPrimary.withOpacity(0.3)),
                child: Row(
                  children: [
                    const Icon(Icons.info_outline, color: AppColors.emeraldPrimary),
                    const SizedBox(width: MosqueSpace.md),
                    Expanded(
                      child: Text(
                        'মসজিদের পরিচালনা কমিটি কর্তৃক নির্ধারিত আজান ও জামাতের সময়সূচি।',
                        style: TextStyle(
                          fontSize: 12,
                          color: isDark ? AppColors.nightTextPrimary : AppColors.textPrimary,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: MosqueSpace.lg),

              ...schedule.map((item) {
                return Padding(
                  padding: const EdgeInsets.only(bottom: MosqueSpace.md),
                  child: CardShell(
                    border: item.isNext
                        ? Border.all(color: AppColors.gold, width: 1.5)
                        : null,
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Row(
                          children: [
                            Container(
                              width: 36,
                              height: 36,
                              decoration: BoxDecoration(
                                shape: BoxShape.circle,
                                color: item.isNext
                                    ? AppColors.goldLight
                                    : (isDark ? AppColors.nightSurfaceVariant : AppColors.bgLight),
                              ),
                              child: Icon(
                                Icons.access_time,
                                size: 18,
                                color: item.isNext ? AppColors.goldDark : AppColors.emeraldPrimary,
                              ),
                            ),
                            const SizedBox(width: MosqueSpace.md),
                            Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  item.nameBn,
                                  style: const TextStyle(
                                    fontSize: 15,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                                Text(
                                  'আজান: ${item.azanTime}',
                                  style: TextStyle(
                                    fontSize: 12,
                                    color: isDark ? AppColors.nightTextMuted : AppColors.textSecondary,
                                  ),
                                ),
                              ],
                            ),
                          ],
                        ),

                        Column(
                          crossAxisAlignment: CrossAxisAlignment.end,
                          children: [
                            Text(
                              'ইকামত',
                              style: TextStyle(
                                fontSize: 11,
                                color: isDark ? AppColors.nightTextMuted : AppColors.textSecondary,
                              ),
                            ),
                            Text(
                              item.iqamahTime,
                              style: const TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.bold,
                                color: AppColors.emeraldPrimary,
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                );
              }),
            ],
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (err, _) => Center(child: Text('ত্রুটি: $err')),
      ),
    );
  }
}
