import 'bengali_number_formatter.dart';

/// Islamic Hijri Date Calculator with Moon-Sighting Offset Support
class HijriCalendarHelper {
  static const List<String> hijriMonthsBn = [
    'মুহররম',
    'সফর',
    'রবিউল আউয়াল',
    'রবিউস সানি',
    'জমাদিউল আউয়াল',
    'জমাদিউস সানি',
    'রজব',
    'শাবান',
    'রমজান',
    'শাওয়াল',
    'জিলকদ',
    'জিলহজ',
  ];

  /// Computes Hijri (year, month [1..12], day [1..30]) from Gregorian date.
  static ({int year, int month, int day}) computeHijriParts(
    DateTime gregorianDate, {
    int moonOffsetDays = 0,
  }) {
    final adjustedDate = gregorianDate.add(Duration(days: moonOffsetDays));

    int y = adjustedDate.year;
    int m = adjustedDate.month;
    final int d = adjustedDate.day;

    if (m < 3) {
      y -= 1;
      m += 12;
    }

    final int a = (y / 100).floor();
    final int b = 2 - a + (a / 4).floor();
    final double jd = (365.25 * (y + 4716)).floor() +
        (30.6001 * (m + 1)).floor() +
        d +
        b -
        1524.5;

    final double z = jd - 1948438.5;
    final int l = (z / 10631).floor();
    final double rem = z - 10631 * l;
    final int j = ((rem + 0.5) / 354.36667).floor();
    final int hijriYear = 30 * l + j + 1;

    final double dayOfYear = rem - (j * 354.36667) + 0.5;
    int hijriMonth = (dayOfYear / 29.530588).floor() + 1;
    if (hijriMonth > 12) hijriMonth = 12;
    if (hijriMonth < 1) hijriMonth = 1;

    int hijriDay = (dayOfYear - ((hijriMonth - 1) * 29.530588)).floor() + 1;
    if (hijriDay > 30) hijriDay = 30;
    if (hijriDay < 1) hijriDay = 1;

    return (year: hijriYear, month: hijriMonth, day: hijriDay);
  }

  /// Approximates Hijri date from Gregorian date with moon offset (-1, 0, +1)
  static String formatHijriDate(DateTime gregorianDate, {int moonOffsetDays = 0}) {
    final parts = computeHijriParts(gregorianDate, moonOffsetDays: moonOffsetDays);
    final monthName = hijriMonthsBn[parts.month - 1];
    final dayBn = BengaliNumberFormatter.toBengaliDigits(parts.day);
    final yearBn = BengaliNumberFormatter.toBengaliDigits(parts.year);

    return '$dayBn $monthName $yearBn হিজরি';
  }
}
