import 'package:flutter/material.dart';
import '../../../core/theme/app_dimens.dart';
import '../../moon_sighting/widgets/moon_offset_toggle_widget.dart';
import '../models/islamic_event.dart';
import '../widgets/islamic_event_card.dart';

/// Islamic Calendar & Annual Occasion Hub
class IslamicCalendarScreen extends StatelessWidget {
  const IslamicCalendarScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final events = IslamicEvent.annualEvents;

    return Scaffold(
      appBar: AppBar(
        title: const Text('ইসলামিক ক্যালেন্ডার ও বিশেষ দিবস'),
      ),
      body: ListView(
        padding: const EdgeInsets.all(MosqueContentPadding.screenHorizontal),
        children: [
          const MoonOffsetToggleWidget(),
          const SizedBox(height: MosqueSpace.lg),

          const Text(
            'বাৎসরিক গুরুত্বপূর্ণ ইসলামিক দিবস ও ফজিলত',
            style: TextStyle(fontSize: 14, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: MosqueSpace.sm),

          ...events.map(
            (e) => Padding(
              padding: const EdgeInsets.only(bottom: MosqueSpace.md),
              child: IslamicEventCard(
                event: e,
                onTap: () {
                  showModalBottomSheet(
                    context: context,
                    isScrollControlled: true,
                    shape: const RoundedRectangleBorder(
                      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
                    ),
                    builder: (ctx) => DraggableScrollableSheet(
                      initialChildSize: 0.6,
                      maxChildSize: 0.9,
                      minChildSize: 0.4,
                      expand: false,
                      builder: (_, scrollController) => ListView(
                        controller: scrollController,
                        padding: const EdgeInsets.all(20),
                        children: [
                          Text(e.titleBn, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                          const SizedBox(height: 8),
                          Text(e.defaultSignificanceBn, style: const TextStyle(fontSize: 13, height: 1.4)),
                          const SizedBox(height: 16),
                          const Text('✨ গুরুত্বপূর্ণ নফল আমল:', style: TextStyle(fontWeight: FontWeight.bold)),
                          const SizedBox(height: 4),
                          ...e.defaultAmalsBn.map((a) => Padding(
                                padding: const EdgeInsets.symmetric(vertical: 2),
                                child: Text('• $a', style: const TextStyle(fontSize: 12)),
                              )),
                          const SizedBox(height: 16),
                          const Text('🕌 খুতবা ও বয়ান পয়েন্ট:', style: TextStyle(fontWeight: FontWeight.bold)),
                          const SizedBox(height: 4),
                          ...e.defaultKhutbahPoints.map((k) => Padding(
                                padding: const EdgeInsets.symmetric(vertical: 2),
                                child: Text('• $k', style: const TextStyle(fontSize: 12)),
                              )),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
          ),
        ],
      ),
    );
  }
}
