import 'package:flutter_test/flutter_test.dart';
import 'package:mosqueseba/core/utils/hijri_calendar_helper.dart';

void main() {
  group('HijriCalendarHelper', () {
    test('formats Hijri date in Bengali with moon offset', () {
      final base = HijriCalendarHelper.formatHijriDate(DateTime(2026, 11, 26));
      final plusOne = HijriCalendarHelper.formatHijriDate(
        DateTime(2026, 11, 26),
        moonOffsetDays: 1,
      );

      expect(base, endsWith('হিজরি'));
      expect(base, isNot(equals(plusOne)));
      expect(HijriCalendarHelper.hijriMonthsBn.length, 12);
    });
  });
}
