import 'package:flutter/material.dart';

/// Islamic Modern Design System Palette (মেহরাব ও অ্যারাবেস্ক কালার প্যালেট)
/// Preserves exact parity with Kotlin Color.kt
class AppColors {
  // Primary Islamic Emerald Palette
  static const Color emeraldPrimary = Color(0xFF0D5C3A); // Primary Islamic Emerald
  static const Color emeraldDark = Color(0xFF022C22);    // Deep Sanctuary Green
  static const Color emeraldLight = Color(0xFF047857);   // Vibrant Prayer Green
  static const Color emeraldSoft = Color(0xFFE6F4EA);    // Soft Tinted Emerald
  static const Color greenAccent = Color(0xFF10B981);

  // Royal Islamic Gold Leaf & Bronze Accents
  static const Color gold = Color(0xFFD4AF37);           // Royal Islamic Gold
  static const Color goldDeep = Color(0xFFD97706);       // Antique Bronze / Minaret Gold
  static const Color goldDark = Color(0xFFB45309);       // Deep Royal Ochre
  static const Color goldLight = Color(0xFFFEF3C7);      // Warm Gold Shimmer
  static const Color goldSoft = Color(0xFFFFFBEB);       // Soft Creamy Ivory Gold

  // Islamic Clean Surfaces & Warm Modern Canvas
  static const Color bgLight = Color(0xFFF6F8F6);        // Warm parchment canvas
  static const Color surface = Color(0xFFFFFFFF);
  static const Color cardBg = Color(0xFFFFFFFF);
  static const Color cardBorder = Color(0xFFDDE8E1);
  static const Color cardBorderSubtle = Color(0xFFE7EFEA);
  static const Color cardBorderGold = Color(0xFFF3E8D0);

  // Typography & Ink Colors
  static const Color textPrimary = Color(0xFF0F172A);
  static const Color textSecondary = Color(0xFF475569);
  static const Color textMuted = Color(0xFF94A3B8);

  // Eye-Comfort Night / Dark Theme Palette (ফজর ও এশার উপযোগী নাইট মোড)
  static const Color nightBg = Color(0xFF071511);
  static const Color nightSurface = Color(0xFF0E231D);
  static const Color nightSurfaceVariant = Color(0xFF143129);
  static const Color nightBorder = Color(0xFF234E41);
  static const Color nightInputBorder = Color(0xFF387162);
  static const Color nightTextPrimary = Color(0xFFECFDF5);
  static const Color nightTextSecondary = Color(0xFFCBD5E1);
  static const Color nightTextMuted = Color(0xFF94A3B8);
  static const Color nightSecondaryContainer = Color(0xFF3A2D0B);

  // Status & Alert Colors
  static const Color urgentRed = Color(0xFFDC2626);
  static const Color warningAmber = Color(0xFFD97706);
  static const Color successGreen = Color(0xFF16A34A);
  static const Color infoBlue = Color(0xFF0284C7);

  // Semantic Theme Aliases
  static const Color primary = emeraldPrimary;
  static const Color onPrimary = surface;
  static const Color primaryContainer = emeraldSoft;
  static const Color outline = cardBorder;
  static const Color background = bgLight;
  static const Color success = successGreen;
  static const Color error = urgentRed;
  static const Color warning = warningAmber;

  // Role-Specific Identity Accents
  static const Color rolePresident = Color(0xFFD97706);     // Royal Gold
  static const Color roleTreasurer = Color(0xFF059669);     // Ledger Emerald
  static const Color roleSecretary = Color(0xFF2563EB);     // Secretariat Blue
  static const Color roleImam = Color(0xFF047857);          // Mihrab Green
  static const Color roleMusalli = Color(0xFF0D9488);       // Devotional Teal
  static const Color rolePublicity = Color(0xFF7C3AED);     // Publicity Violet
  static const Color roleOffice = Color(0xFF0369A1);        // Registry Sky Blue
  static const Color roleWelfare = Color(0xFF15803D);       // Forest Green
  static const Color roleAdvisory = Color(0xFFB45309);      // Advisory Bronze
}
