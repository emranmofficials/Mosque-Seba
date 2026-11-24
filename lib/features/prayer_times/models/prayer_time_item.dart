/// Individual Prayer Time Entry (PrayerCard.kt Parity)
class PrayerTimeItem {
  final String nameBn;
  final String azanTime;
  final String iqamahTime;
  final DateTime iqamahDateTime;
  final bool isNext;

  const PrayerTimeItem({
    required this.nameBn,
    required this.azanTime,
    required this.iqamahTime,
    required this.iqamahDateTime,
    this.isNext = false,
  });
}

/// Summary of the upcoming prayer waqt with remaining countdown
class NextPrayerSummary {
  final String nameBn;
  final String iqamahTimeBn;
  final String remainingTextBn;
  final int remainingMinutes;

  const NextPrayerSummary({
    required this.nameBn,
    required this.iqamahTimeBn,
    required this.remainingTextBn,
    required this.remainingMinutes,
  });
}
