import 'package:flutter/material.dart';
import '../models/poster_design_model.dart';

/// Ornate Islamic Dawatnama Poster Preview Canvas
class PosterPreviewCanvas extends StatelessWidget {
  final PosterDesignModel design;

  const PosterPreviewCanvas({super.key, required this.design});

  LinearGradient _getThemeGradient() {
    switch (design.themeStyle) {
      case 'royal_blue':
        return const LinearGradient(
          colors: [Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)],
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
        );
      case 'classic_maroon':
        return const LinearGradient(
          colors: [Color(0xFF4A0E17), Color(0xFF7A1C28), Color(0xFF2B050B)],
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
        );
      default: // emerald_gold
        return const LinearGradient(
          colors: [Color(0xFF063326), Color(0xFF0D5C46), Color(0xFF04241B)],
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
        );
    }
  }

  Color _getGoldColor() => const Color(0xFFE5C158);

  @override
  Widget build(BuildContext context) {
    final gold = _getGoldColor();

    return Container(
      width: double.infinity,
      decoration: BoxDecoration(
        gradient: _getThemeGradient(),
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.3),
            blurRadius: 15,
            offset: const Offset(0, 8),
          ),
        ],
        border: Border.all(color: gold.withOpacity(0.6), width: 2),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Container(
          decoration: BoxDecoration(
            border: Border.all(color: gold.withOpacity(0.4), width: 1),
            borderRadius: BorderRadius.circular(10),
          ),
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 16),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              // 1. Calligraphic Bismillah
              Text(
                design.bismillahHeader,
                style: TextStyle(
                  color: gold,
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                  letterSpacing: 1.2,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 6),

              // 2. Quranic Ayat / Hadith Banner
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: gold.withOpacity(0.12),
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Text(
                  design.quranAyat,
                  style: const TextStyle(
                    color: Colors.white70,
                    fontSize: 10,
                    fontStyle: FontStyle.italic,
                  ),
                  textAlign: TextAlign.center,
                ),
              ),
              const SizedBox(height: 12),

              // 3. Subtitle / Invitation Call
              Text(
                design.subtitle,
                style: TextStyle(
                  color: gold,
                  fontSize: 12,
                  fontWeight: FontWeight.w600,
                  letterSpacing: 0.5,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 6),

              // 4. Main Event Title
              Text(
                design.eventTitle,
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  height: 1.25,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 14),

              // Divider with Gold Star
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Container(width: 40, height: 1, color: gold.withOpacity(0.5)),
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 8),
                    child: Icon(Icons.star, size: 14, color: gold),
                  ),
                  Container(width: 40, height: 1, color: gold.withOpacity(0.5)),
                ],
              ),
              const SizedBox(height: 14),

              // 5. Chief Guest Card
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.08),
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: gold.withOpacity(0.3)),
                ),
                child: Column(
                  children: [
                    Text(
                      '— প্রধান মেহমান / প্রধান বক্তা —',
                      style: TextStyle(color: gold, fontSize: 11, fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      design.chiefGuest,
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                      ),
                      textAlign: TextAlign.center,
                    ),
                    Text(
                      design.chiefGuestTitle,
                      style: const TextStyle(color: Colors.white70, fontSize: 11),
                      textAlign: TextAlign.center,
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 10),

              // 6. Speakers & Reciter Row
              Row(
                children: [
                  Expanded(
                    child: _buildSpeakerBox(
                      title: 'বিশেষ বক্তা',
                      name: design.specialGuest,
                      gold: gold,
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: _buildSpeakerBox(
                      title: 'প্রধান ক্বারী',
                      name: design.quranReciter,
                      gold: gold,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 14),

              // 7. Date, Time & Venue Gold Strip
              Container(
                padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 10),
                decoration: BoxDecoration(
                  color: gold.withOpacity(0.18),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Column(
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(Icons.calendar_month, color: gold, size: 14),
                        const SizedBox(width: 6),
                        Text(
                          '${design.eventDate} (${design.eventTime})',
                          style: const TextStyle(color: Colors.white, fontSize: 12, fontWeight: FontWeight.bold),
                        ),
                      ],
                    ),
                    const SizedBox(height: 2),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(Icons.location_on, color: gold, size: 14),
                        const SizedBox(width: 4),
                        Text(
                          design.venue,
                          style: const TextStyle(color: Colors.white70, fontSize: 11),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 12),

              // 8. Chairperson & Organizers
              Text(
                'সভাপতিত্ব করবেন: ${design.chairPerson}',
                style: const TextStyle(color: Colors.white70, fontSize: 11, fontWeight: FontWeight.w500),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 4),
              Text(
                design.organizers,
                style: TextStyle(color: gold.withOpacity(0.85), fontSize: 10),
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSpeakerBox({
    required String title,
    required String name,
    required Color gold,
  }) {
    return Container(
      padding: const EdgeInsets.all(8),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.05),
        borderRadius: BorderRadius.circular(6),
        border: Border.all(color: Colors.white.withOpacity(0.1)),
      ),
      child: Column(
        children: [
          Text(title, style: TextStyle(color: gold, fontSize: 10, fontWeight: FontWeight.bold)),
          const SizedBox(height: 2),
          Text(
            name,
            style: const TextStyle(color: Colors.white, fontSize: 12, fontWeight: FontWeight.bold),
            textAlign: TextAlign.center,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
          ),
        ],
      ),
    );
  }
}
