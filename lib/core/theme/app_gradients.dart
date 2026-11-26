import 'package:flutter/material.dart';

/// Premium Islamic Mehrab & Minaret Gradients
/// Preserves exact parity with Kotlin Color.kt
class AppGradients {
  static const LinearGradient emeraldGradient = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [
      Color(0xFF022C22),
      Color(0xFF064E3B),
      Color(0xFF047857),
    ],
  );

  static const LinearGradient emeraldDarkGradient = LinearGradient(
    begin: Alignment.topCenter,
    end: Alignment.bottomCenter,
    colors: [
      Color(0xFF011C15),
      Color(0xFF022C22),
      Color(0xFF064E3B),
    ],
  );

  static const LinearGradient goldGradient = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [
      Color(0xFFFDE68A),
      Color(0xFFF59E0B),
      Color(0xFFD97706),
    ],
  );

  static const LinearGradient mehrabHeaderGradient = LinearGradient(
    begin: Alignment.topCenter,
    end: Alignment.bottomCenter,
    colors: [
      Color(0xFF011812),
      Color(0xFF022C22),
      Color(0xFF064E3B),
    ],
  );

  static const LinearGradient royalGoldShimmer = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [
      Color(0xFFD97706),
      Color(0xFFF59E0B),
      Color(0xFFD97706),
    ],
  );

  static const LinearGradient royalEmeraldShimmer = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [
      Color(0xFF022C22),
      Color(0xFF044E3B),
      Color(0xFF022C22),
    ],
  );

  // Devotional Waqt Ambient Lighting (শান্ত ও মর্যাদাপূর্ণ ওয়াক্ত আবহ)
  static const LinearGradient waqtFajrSky = LinearGradient(
    begin: Alignment.topCenter,
    end: Alignment.bottomCenter,
    colors: [Color(0xFF061A23), Color(0xFF0B2E38)],
  );

  static const LinearGradient waqtDhuhrSky = LinearGradient(
    begin: Alignment.topCenter,
    end: Alignment.bottomCenter,
    colors: [Color(0xFF022C22), Color(0xFF064E3B)],
  );

  static const LinearGradient waqtAsrSky = LinearGradient(
    begin: Alignment.topCenter,
    end: Alignment.bottomCenter,
    colors: [Color(0xFF1F2937), Color(0xFF111827)],
  );

  static const LinearGradient waqtMaghribSky = LinearGradient(
    begin: Alignment.topCenter,
    end: Alignment.bottomCenter,
    colors: [Color(0xFF0F172A), Color(0xFF062E24)],
  );

  static const LinearGradient waqtIshaSky = LinearGradient(
    begin: Alignment.topCenter,
    end: Alignment.bottomCenter,
    colors: [Color(0xFF031E17), Color(0xFF082F26)],
  );

  static LinearGradient getPrayerSkyGradient(String prayerNameBn) {
    if (prayerNameBn.contains('ফজর')) return waqtFajrSky;
    if (prayerNameBn.contains('যোহর') || prayerNameBn.contains('জুমুআ')) return waqtDhuhrSky;
    if (prayerNameBn.contains('আসর')) return waqtAsrSky;
    if (prayerNameBn.contains('মাগরিব')) return waqtMaghribSky;
    if (prayerNameBn.contains('ইশা')) return waqtIshaSky;
    return emeraldDarkGradient;
  }
}
