import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_gradients.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../core/utils/hijri_calendar_helper.dart';
import '../../moon_sighting/providers/moon_offset_provider.dart';

/// Mosque Dashboard Hero Card (HeroCard.kt Parity)
/// Displays Mosque Name, Committee ID with 1-tap copy, and Hijri Date
class DashboardHeroCard extends ConsumerWidget {
  final String mosqueNameBn;
  final String address;
  final String committeeId;
  final int establishedYear;
  final VoidCallback? onProfileTap;

  const DashboardHeroCard({
    super.key,
    required this.mosqueNameBn,
    required this.address,
    required this.committeeId,
    this.establishedYear = 1985,
    this.onProfileTap,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final moonOffset = ref.watch(moonOffsetProvider);
    final hijriDateBn = HijriCalendarHelper.formatHijriDate(
      DateTime.now(),
      moonOffsetDays: moonOffset,
    );
    final establishedBn = BengaliNumberFormatter.toBengaliDigits(establishedYear);

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: AppGradients.emeraldDarkGradient,
        borderRadius: BorderRadius.circular(MosqueRadius.card),
        border: Border.all(color: AppColors.gold.withOpacity(0.4), width: 1.2),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.12),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Top Row: Established Year Badge & Committee ID Copy Tag
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.12),
                  borderRadius: BorderRadius.circular(MosqueRadius.pill),
                  border: Border.all(color: Colors.white.withOpacity(0.2)),
                ),
                child: Text(
                  'স্থাপিত: $establishedBn খ্রি.',
                  style: const TextStyle(
                    color: AppColors.goldLight,
                    fontSize: 11,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ),

              GestureDetector(
                onTap: () {
                  if (committeeId.isNotEmpty) {
                    Clipboard.setData(ClipboardData(text: committeeId));
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text('কমিটি কোড $committeeId কপি করা হয়েছে।')),
                    );
                  }
                },
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                  decoration: BoxDecoration(
                    color: AppColors.goldLight,
                    borderRadius: BorderRadius.circular(MosqueRadius.pill),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Icon(Icons.copy, size: 12, color: AppColors.goldDark),
                      const SizedBox(width: 4),
                      Text(
                        committeeId.isNotEmpty ? committeeId : 'MS-2026',
                        style: const TextStyle(
                          color: AppColors.goldDark,
                          fontSize: 11,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),

          const SizedBox(height: MosqueSpace.md),

          // Mosque Title in Bengali
          Text(
            mosqueNameBn.isNotEmpty ? mosqueNameBn : 'বায়তুল মুকাররম জামে মসজিদ',
            style: const TextStyle(
              color: Colors.white,
              fontSize: 20,
              fontWeight: FontWeight.w800,
              letterSpacing: 0.2,
            ),
          ),

          const SizedBox(height: 4),

          // Mosque Address
          Row(
            children: [
              const Icon(Icons.location_on_outlined, size: 14, color: AppColors.goldLight),
              const SizedBox(width: 4),
              Expanded(
                child: Text(
                  address.isNotEmpty ? address : 'মিরপুর, ঢাকা-১২১৬',
                  style: TextStyle(
                    color: Colors.white.withOpacity(0.85),
                    fontSize: 12,
                  ),
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            ],
          ),

          const SizedBox(height: MosqueSpace.md),
          const Divider(color: Colors.white24, height: 1),
          const SizedBox(height: MosqueSpace.sm),

          // Hijri Date Banner
          Row(
            children: [
              const Icon(Icons.calendar_today_outlined, size: 13, color: AppColors.goldLight),
              const SizedBox(width: 6),
              Text(
                hijriDateBn,
                style: const TextStyle(
                  color: AppColors.goldLight,
                  fontSize: 12,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
