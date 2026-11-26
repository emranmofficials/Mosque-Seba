import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/widgets/card_shell.dart';
import '../models/mosque_event_model.dart';

/// Card component presenting a Mosque Event or Waz Mahfil
class EventCard extends StatelessWidget {
  final MosqueEventModel event;
  final VoidCallback? onTap;
  final VoidCallback? onDesignPoster;

  const EventCard({
    super.key,
    required this.event,
    this.onTap,
    this.onDesignPoster,
  });

  Color _getBadgeColor(String type) {
    switch (type) {
      case 'waz_mahfil':
        return AppColors.goldDeep;
      case 'shab_e_qadr':
        return AppColors.emeraldPrimary;
      case 'iftar_party':
        return Colors.orange.shade700;
      case 'eid_gathering':
        return Colors.purple.shade700;
      default:
        return AppColors.primary;
    }
  }

  @override
  Widget build(BuildContext context) {
    final badgeColor = _getBadgeColor(event.eventType);

    return CardShell(
      onTap: onTap,
      padding: const EdgeInsets.all(AppDimens.spacing14),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: badgeColor.withOpacity(0.12),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Icon(Icons.event, color: badgeColor, size: 24),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                      decoration: BoxDecoration(
                        color: badgeColor.withOpacity(0.1),
                        borderRadius: BorderRadius.circular(6),
                      ),
                      child: Text(
                        event.eventTypeTitleBn,
                        style: TextStyle(
                          fontSize: 10,
                          fontWeight: FontWeight.bold,
                          color: badgeColor,
                        ),
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      event.title,
                      style: const TextStyle(fontSize: 15, fontWeight: FontWeight.bold),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          const Divider(height: 1),
          const SizedBox(height: 10),

          // Date, Time & Venue
          Row(
            children: [
              const Icon(Icons.calendar_today, size: 14, color: Colors.grey),
              const SizedBox(width: 6),
              Text(
                '${BengaliNumberFormatter.toBengali(event.eventDate)} (${event.eventTime})',
                style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w600),
              ),
            ],
          ),
          const SizedBox(height: 4),
          Row(
            children: [
              const Icon(Icons.location_on_outlined, size: 14, color: Colors.grey),
              const SizedBox(width: 6),
              Text(
                event.venue,
                style: TextStyle(fontSize: 12, color: Colors.grey.shade700),
              ),
            ],
          ),

          if (event.chiefGuest.isNotEmpty) ...[
            const SizedBox(height: 4),
            Row(
              children: [
                const Icon(Icons.mic, size: 14, color: AppColors.goldDeep),
                const SizedBox(width: 6),
                Expanded(
                  child: Text(
                    'প্রধান মেহমান: ${event.chiefGuest}',
                    style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w500),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
              ],
            ),
          ],

          const SizedBox(height: 10),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'বাজেট: ৳ ${BengaliNumberFormatter.toBengali(event.estimatedBudget.toInt().toString())}',
                style: TextStyle(fontSize: 12, color: Colors.grey.shade600, fontWeight: FontWeight.w500),
              ),
              if (onDesignPoster != null)
                TextButton.icon(
                  onPressed: onDesignPoster,
                  icon: const Icon(Icons.auto_awesome, size: 14),
                  label: const Text('পোস্টার ডিজাইন (AI)', style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold)),
                  style: TextButton.styleFrom(
                    foregroundColor: AppColors.emeraldPrimary,
                    padding: const EdgeInsets.symmetric(horizontal: 8),
                  ),
                ),
            ],
          ),
        ],
      ),
    );
  }
}
