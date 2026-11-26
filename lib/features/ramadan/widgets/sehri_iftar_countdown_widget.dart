import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/widgets/card_shell.dart';
import '../../prayer_times/providers/prayer_times_provider.dart';
import '../../prayer_times/services/prayer_calculator_service.dart';
import '../models/ramadan_ashra.dart';

/// Live Solar Sehri & Iftar Countdown with Active Ashra Dua
class SehriIftarCountdownWidget extends ConsumerStatefulWidget {
  final int currentRamadanDay;

  const SehriIftarCountdownWidget({
    super.key,
    this.currentRamadanDay = 1,
  });

  @override
  ConsumerState<SehriIftarCountdownWidget> createState() =>
      _SehriIftarCountdownWidgetState();
}

class _SehriIftarCountdownWidgetState
    extends ConsumerState<SehriIftarCountdownWidget> {
  Timer? _timer;
  DateTime _now = DateTime.now();

  @override
  void initState() {
    super.initState();
    _timer = Timer.periodic(const Duration(seconds: 1), (_) {
      if (mounted) setState(() => _now = DateTime.now());
    });
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final settings = ref.watch(prayerSettingsProvider);
    final times = PrayerCalculatorService.computeSehriIftarTimes(
      referenceTime: _now,
      latitude: settings.latitude,
      longitude: settings.longitude,
    );

    final ashra = RamadanAshra.fromDay(widget.currentRamadanDay);
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    String countdownLabel;
    Duration remaining;
    if (_now.isBefore(times.sehriEnd)) {
      remaining = times.sehriEnd.difference(_now);
      countdownLabel = 'সেহরি শেষ';
    } else if (_now.isBefore(times.iftarStart)) {
      remaining = times.iftarStart.difference(_now);
      countdownLabel = 'ইফতার বাকি';
    } else {
      final tomorrow = PrayerCalculatorService.computeSehriIftarTimes(
        referenceTime: _now.add(const Duration(days: 1)),
        latitude: settings.latitude,
        longitude: settings.longitude,
      );
      remaining = tomorrow.sehriEnd.difference(_now);
      countdownLabel = 'সেহরি বাকি';
    }

    final hrs = BengaliNumberFormatter.toBengaliDigits(remaining.inHours);
    final mins = BengaliNumberFormatter.toBengaliDigits(
      (remaining.inMinutes % 60).toString().padLeft(2, '0'),
    );
    final secs = BengaliNumberFormatter.toBengaliDigits(
      (remaining.inSeconds % 60).toString().padLeft(2, '0'),
    );

    final sehriStr = PrayerCalculatorService.formatTimeBn12Hour(times.sehriEnd);
    final iftarStr = PrayerCalculatorService.formatTimeBn12Hour(times.iftarStart);

    return CardShell(
      border: Border.all(color: AppColors.gold, width: 1.2),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Expanded(
                child: Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(6),
                      decoration: BoxDecoration(
                        color: isDark
                            ? AppColors.nightSecondaryContainer
                            : AppColors.goldLight,
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(
                        Icons.wb_sunny_outlined,
                        size: 16,
                        color: AppColors.goldDark,
                      ),
                    ),
                    const SizedBox(width: MosqueSpace.sm),
                    Expanded(
                      child: Text(
                        '${settings.districtNameBn} — ${ashra.titleBn}',
                        style: const TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 13,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(
                  color: AppColors.emeraldSoft,
                  borderRadius: BorderRadius.circular(MosqueRadius.pill),
                ),
                child: Text(
                  '$countdownLabel: $hrs:$mins:$secs',
                  style: const TextStyle(
                    color: AppColors.emeraldPrimary,
                    fontSize: 11,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: MosqueSpace.md),

          // Live Daily Sehri & Iftar Times
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
                      Text(
                        'আজকের সেহরি শেষ',
                        style: TextStyle(
                          fontSize: 11,
                          color: isDark ? AppColors.nightTextMuted : AppColors.textSecondary,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        '$sehriStr ভোর',
                        style: const TextStyle(
                          fontSize: 15,
                          fontWeight: FontWeight.bold,
                          color: AppColors.emeraldPrimary,
                        ),
                      ),
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
                      Text(
                        'আজকের ইফতার শুরু',
                        style: TextStyle(
                          fontSize: 11,
                          color: isDark ? AppColors.nightTextMuted : AppColors.textSecondary,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        '$iftarStr সন্ধ্যা',
                        style: const TextStyle(
                          fontSize: 15,
                          fontWeight: FontWeight.bold,
                          color: AppColors.goldDark,
                        ),
                      ),
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
                  style: const TextStyle(
                    fontSize: 11,
                    fontWeight: FontWeight.bold,
                    color: AppColors.goldDark,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  ashra.duaArabic,
                  textAlign: TextAlign.right,
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.bold,
                    color: AppColors.emeraldPrimary,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  ashra.duaTranslation,
                  style: TextStyle(
                    fontSize: 11,
                    color: isDark ? AppColors.nightTextSecondary : AppColors.textSecondary,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
