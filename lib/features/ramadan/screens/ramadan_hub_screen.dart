import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../shared/widgets/card_shell.dart';
import '../widgets/sehri_iftar_countdown_widget.dart';

/// Ramadan Special Devotional Hub Screen (RamadanCard.kt Parity)
class RamadanHubScreen extends StatefulWidget {
  const RamadanHubScreen({super.key});

  @override
  State<RamadanHubScreen> createState() => _RamadanHubScreenState();
}

class _RamadanHubScreenState extends State<RamadanHubScreen> {
  int _taraweehCompletedDays = 12;
  int _khatamParasRead = 15;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Scaffold(
      appBar: AppBar(
        title: const Text('পবিত্র মাহে রমজান স্পেশাল হাব'),
      ),
      body: ListView(
        padding: const EdgeInsets.all(MosqueContentPadding.screenHorizontal),
        children: [
          const SehriIftarCountdownWidget(currentRamadanDay: 12),
          const SizedBox(height: MosqueSpace.lg),

          // Taraweeh Tracker Card
          CardShell(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text('২০ রাকাত তারাবিহ ট্র্যাকার', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                    Text('সম্পন্ন: $_taraweehCompletedDays / ৩০ দিন', style: const TextStyle(fontWeight: FontWeight.bold, color: AppColors.emeraldPrimary, fontSize: 12)),
                  ],
                ),
                const SizedBox(height: MosqueSpace.sm),
                LinearProgressIndicator(
                  value: _taraweehCompletedDays / 30,
                  backgroundColor: isDark ? AppColors.nightSurfaceVariant : AppColors.cardBorderSubtle,
                  valueColor: const AlwaysStoppedAnimation<Color>(AppColors.emeraldPrimary),
                  minHeight: 8,
                  borderRadius: BorderRadius.circular(MosqueRadius.pill),
                ),
                const SizedBox(height: MosqueSpace.md),
                Row(
                  mainAxisAlignment: MainAxisAlignment.end,
                  children: [
                    TextButton.icon(
                      icon: const Icon(Icons.check_circle_outline, size: 18),
                      label: const Text('আজকের তারাবিহ সম্পন্ন চিহ্নিত করুন', style: TextStyle(fontSize: 12)),
                      onPressed: () {
                        if (_taraweehCompletedDays < 30) {
                          setState(() => _taraweehCompletedDays++);
                        }
                      },
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
                    const Text('খতমে কুরআন অগ্রগতি (পারাভিত্তিক)', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                    Text('পঠিত: $_khatamParasRead / ৩০ পারা', style: const TextStyle(fontWeight: FontWeight.bold, color: AppColors.goldDark, fontSize: 12)),
                  ],
                ),
                const SizedBox(height: MosqueSpace.sm),
                LinearProgressIndicator(
                  value: _khatamParasRead / 30,
                  backgroundColor: isDark ? AppColors.nightSurfaceVariant : AppColors.cardBorderSubtle,
                  valueColor: const AlwaysStoppedAnimation<Color>(AppColors.gold),
                  minHeight: 8,
                  borderRadius: BorderRadius.circular(MosqueRadius.pill),
                ),
                const SizedBox(height: MosqueSpace.md),
                Row(
                  mainAxisAlignment: MainAxisAlignment.end,
                  children: [
                    TextButton.icon(
                      icon: const Icon(Icons.menu_book, size: 18),
                      label: const Text('+১ পারা তেলাওয়াত যোগ করুন', style: TextStyle(fontSize: 12)),
                      onPressed: () {
                        if (_khatamParasRead < 30) {
                          setState(() => _khatamParasRead++);
                        }
                      },
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
