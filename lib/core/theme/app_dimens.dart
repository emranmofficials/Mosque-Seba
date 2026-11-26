/// 8-Point & 4-Point Islamic Design Grid Scale
/// Preserves exact parity with Kotlin Dimens.kt
class MosqueSpace {
  static const double none = 0.0;
  static const double xxs = 2.0;
  static const double xs = 4.0;
  static const double sm = 8.0;
  static const double md = 12.0;
  static const double lg = 16.0;
  static const double xl = 24.0;
  static const double xxl = 32.0;
  static const double xxxl = 48.0;
}

class MosqueRadius {
  static const double none = 0.0;
  static const double xs = 4.0;
  static const double sm = 6.0;
  static const double md = 10.0;
  static const double medium = 12.0;
  static const double lg = 14.0;
  static const double large = 16.0;
  static const double card = 16.0;
  static const double xl = 20.0;
  static const double pill = 999.0;
}

class MosqueElevation {
  static const double none = 0.0;
  static const double subtle = 0.5;
  static const double level1 = 1.0;
  static const double raised = 2.0;
  static const double level2 = 2.0;
  static const double level3 = 4.0;
  static const double overlay = 6.0;
  static const double level4 = 8.0;
}

class MosqueTouchTarget {
  static const double minimum = 48.0;
  static const double compact = 44.0;
}

class MosqueContentPadding {
  static const double screenHorizontal = 16.0;
  static const double screenTop = 12.0;
  static const double screenBottom = 100.0;
  static const double cardInternal = 16.0;
  static const double cardCompact = 12.0;
}

/// Semantic spacing and radius aliases mapped to the core design tokens
class AppDimens {
  static const double spacing6 = 6.0;
  static const double spacing8 = MosqueSpace.sm;
  static const double spacing10 = 10.0;
  static const double spacing12 = MosqueSpace.md;
  static const double spacing14 = 14.0;
  static const double spacing16 = MosqueSpace.lg;
  static const double spacing20 = 20.0;
  static const double spacing24 = MosqueSpace.xl;

  static const double radiusXs = MosqueRadius.xs;
  static const double radiusSm = MosqueRadius.sm;
  static const double radiusSmall = MosqueRadius.sm;
  static const double radiusMd = MosqueRadius.md;
  static const double radiusLg = MosqueRadius.lg;
  static const double radiusCard = MosqueRadius.card;
  static const double radiusButton = MosqueRadius.medium;
  static const double radiusXl = MosqueRadius.xl;
}
