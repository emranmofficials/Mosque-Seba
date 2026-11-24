import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../shared/widgets/card_shell.dart';
import '../models/islamic_event.dart';

/// Presentation Card for Annual Islamic Events (IslamicEventCard.kt Parity)
class IslamicEventCard extends StatelessWidget {
  final IslamicEvent event;
  final VoidCallback? onTap;

  const IslamicEventCard({
    super.key,
    required this.event,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return CardShell(
      onTap: onTap,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(
                  color: event.category.color.withOpacity(0.12),
                  borderRadius: BorderRadius.circular(MosqueRadius.pill),
                  border: Border.all(color: event.category.color.withOpacity(0.4)),
                ),
                child: Text(
                  event.category.labelBn,
                  style: TextStyle(
                    color: event.category.color,
                    fontSize: 10,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                decoration: BoxDecoration(
                  color: isDark ? AppColors.nightSecondaryContainer : AppColors.goldLight,
                  borderRadius: BorderRadius.circular(MosqueRadius.sm),
                ),
                child: Text(
                  'হিজরি ${event.hijriDay} তারিখ',
                  style: const TextStyle(
                    color: AppColors.goldDark,
                    fontSize: 11,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: MosqueSpace.sm),

          Text(
            event.titleBn,
            style: const TextStyle(
              fontSize: 15,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 4),

          Text(
            event.summaryBn,
            style: TextStyle(
              fontSize: 12,
              color: isDark ? AppColors.nightTextMuted : AppColors.textSecondary,
              height: 1.4,
            ),
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
          ),
        ],
      ),
    );
  }
}
