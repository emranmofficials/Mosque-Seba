import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../core/utils/hijri_calendar_helper.dart';
import '../../../shared/widgets/card_shell.dart';
import '../../moon_sighting/providers/moon_offset_provider.dart';
import '../widgets/sehri_iftar_countdown_widget.dart';

/// Ramadan Special Devotional Hub Screen with Persistent Trackers & Live Countdown
class RamadanHubScreen extends ConsumerStatefulWidget {
  const RamadanHubScreen({super.key});

  @override
  ConsumerState<RamadanHubScreen> createState() => _RamadanHubScreenState();
}

class _RamadanHubScreenState extends ConsumerState<RamadanHubScreen> {
  static const String _keyTaraweeh = 'key_ramadan_taraweeh_days';
  static const String _keyKhatam = 'key_ramadan_khatam_paras';

  int _taraweehCompletedDays = 0;
  int _khatamParasRead = 0;

  @override
  void initState() {
    super.initState();
    _loadProgress();
  }

  Future<void> _loadProgress() async {
    final prefs = await SharedPreferences.getInstance();
    if (!mounted) return;
    setState(() {
      _taraweehCompletedDays = prefs.getInt(_keyTaraweeh) ?? 0;
      _khatamParasRead = prefs.getInt(_keyKhatam) ?? 0;
    });
  }

  Future<void> _updateTaraweeh(int newValue) async {
    final clamped = newValue.clamp(0, 30);
    setState(() => _taraweehCompletedDays = clamped);
    final prefs = await SharedPreferences.getInstance();
    await prefs.setInt(_keyTaraweeh, clamped);
  }

  Future<void> _updateKhatam(int newValue) async {
    final clamped = newValue.clamp(0, 30);
    setState(() => _khatamParasRead = clamped);
    final prefs = await SharedPreferences.getInstance();
    await prefs.setInt(_keyKhatam, clamped);
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final moonOffset = ref.watch(moonOffsetProvider);
    final hijri = HijriCalendarHelper.computeHijriParts(
      DateTime.now(),
      moonOffsetDays: moonOffset,
    );
    // Ramadan is Hijri month 9; otherwise fallback to current Hijri day for Ashra display
    final ramadanDay = hijri.month == 9 ? hijri.day : hijri.day.clamp(1, 30);

    return Scaffold(
      appBar: AppBar(
        title: const Text('পবিত্র মাহে রমজান স্পেশাল হাব'),
      ),
      body: ListView(
        padding: const EdgeInsets.all(MosqueContentPadding.screenHorizontal),
        children: [
          SehriIftarCountdownWidget(currentRamadanDay: ramadanDay),
          const SizedBox(height: MosqueSpace.lg),

          // Taraweeh Tracker Card
          CardShell(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text(
                      '২০ রাকাত তারাবিহ ট্র্যাকার',
                      style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                    ),
                    Text(
                      'সম্পন্ন: ${BengaliNumberFormatter.toBengali(_taraweehCompletedDays)} / ৩০ দিন',
                      style: const TextStyle(
                        fontWeight: FontWeight.bold,
                        color: AppColors.emeraldPrimary,
                        fontSize: 12,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: MosqueSpace.sm),
                LinearProgressIndicator(
                  value: _taraweehCompletedDays / 30,
                  backgroundColor: isDark
                      ? AppColors.nightSurfaceVariant
                      : AppColors.cardBorderSubtle,
                  valueColor: const AlwaysStoppedAnimation<Color>(
                    AppColors.emeraldPrimary,
                  ),
                  minHeight: 8,
                  borderRadius: BorderRadius.circular(MosqueRadius.pill),
                ),
                const SizedBox(height: MosqueSpace.md),
                Wrap(
                  spacing: 8,
                  children: [
                    OutlinedButton.icon(
                      icon: const Icon(Icons.remove, size: 16),
                      label: const Text('-১ দিন', style: TextStyle(fontSize: 12)),
                      onPressed: _taraweehCompletedDays > 0
                          ? () => _updateTaraweeh(_taraweehCompletedDays - 1)
                          : null,
                    ),
                    ElevatedButton.icon(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.emeraldPrimary,
                        foregroundColor: Colors.white,
                      ),
                      icon: const Icon(Icons.check_circle_outline, size: 16),
                      label: const Text(
                        '+১ দিন সম্পন্ন',
                        style: TextStyle(fontSize: 12),
                      ),
                      onPressed: _taraweehCompletedDays < 30
                          ? () => _updateTaraweeh(_taraweehCompletedDays + 1)
                          : null,
                    ),
                    TextButton(
                      onPressed: _taraweehCompletedDays > 0
                          ? () => _updateTaraweeh(0)
                          : null,
                      child: const Text('রিসেট', style: TextStyle(fontSize: 12)),
                    ),
                  ],
                ),
              ],
            ),
          ),

          const SizedBox(height: MosqueSpace.md),

          // Khatm-e-Quran Progress Card
          CardShell(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text(
                      'খতমে কুরআন অগ্রগতি (পারাভিত্তিক)',
                      style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                    ),
                    Text(
                      'পঠিত: ${BengaliNumberFormatter.toBengali(_khatamParasRead)} / ৩০ পারা',
                      style: const TextStyle(
                        fontWeight: FontWeight.bold,
                        color: AppColors.goldDark,
                        fontSize: 12,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: MosqueSpace.sm),
                LinearProgressIndicator(
                  value: _khatamParasRead / 30,
                  backgroundColor: isDark
                      ? AppColors.nightSurfaceVariant
                      : AppColors.cardBorderSubtle,
                  valueColor: const AlwaysStoppedAnimation<Color>(AppColors.gold),
                  minHeight: 8,
                  borderRadius: BorderRadius.circular(MosqueRadius.pill),
                ),
                const SizedBox(height: MosqueSpace.md),
                Wrap(
                  spacing: 8,
                  children: [
                    OutlinedButton.icon(
                      icon: const Icon(Icons.remove, size: 16),
                      label: const Text('-১ পারা', style: TextStyle(fontSize: 12)),
                      onPressed: _khatamParasRead > 0
                          ? () => _updateKhatam(_khatamParasRead - 1)
                          : null,
                    ),
                    ElevatedButton.icon(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.goldDark,
                        foregroundColor: Colors.white,
                      ),
                      icon: const Icon(Icons.menu_book, size: 16),
                      label: const Text(
                        '+১ পারা তেলাওয়াত',
                        style: TextStyle(fontSize: 12),
                      ),
                      onPressed: _khatamParasRead < 30
                          ? () => _updateKhatam(_khatamParasRead + 1)
                          : null,
                    ),
                    TextButton(
                      onPressed: _khatamParasRead > 0
                          ? () => _updateKhatam(0)
                          : null,
                      child: const Text('রিসেট', style: TextStyle(fontSize: 12)),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
