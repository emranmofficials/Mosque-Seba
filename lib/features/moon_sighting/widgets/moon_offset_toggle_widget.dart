import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../providers/moon_offset_provider.dart';

/// Moon-Sighting Offset Selector (-1, 0, +1 day) (ChandaMonthUtils.kt Parity)
class MoonOffsetToggleWidget extends ConsumerWidget {
  const MoonOffsetToggleWidget({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final currentOffset = ref.watch(moonOffsetProvider);
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        color: isDark ? AppColors.nightSurface : AppColors.cardBg,
        borderRadius: BorderRadius.circular(MosqueRadius.medium),
        border: Border.all(
          color: isDark ? AppColors.nightBorder : AppColors.cardBorder,
        ),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            children: [
              const Icon(Icons.nightlight_round, color: AppColors.gold, size: 20),
              const SizedBox(width: MosqueSpace.sm),
              Text(
                'চাঁদ দেখা সমন্বয়:',
                style: TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.bold,
                  color: isDark ? AppColors.nightTextPrimary : AppColors.textPrimary,
                ),
              ),
            ],
          ),

          Row(
            children: [-1, 0, 1].map((offset) {
              final isSelected = currentOffset == offset;
              final label = offset == -1
                  ? '-১ দিন'
                  : offset == 1
                      ? '+১ দিন'
                      : 'স্বাভাবিক';

              return Padding(
                padding: const EdgeInsets.only(left: 4),
                child: GestureDetector(
                  onTap: () => ref.read(moonOffsetProvider.notifier).setOffset(offset),
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: isSelected
                          ? AppColors.emeraldPrimary
                          : (isDark ? AppColors.nightSurfaceVariant : AppColors.bgLight),
                      borderRadius: BorderRadius.circular(MosqueRadius.sm),
                      border: isSelected
                          ? Border.all(color: AppColors.gold, width: 1)
                          : null,
                    ),
                    child: Text(
                      label,
                      style: TextStyle(
                        fontSize: 11,
                        fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                        color: isSelected
                            ? Colors.white
                            : (isDark ? AppColors.nightTextMuted : AppColors.textSecondary),
                      ),
                    ),
                  ),
                ),
              );
            }).toList(),
          ),
        ],
      ),
    );
  }
}
