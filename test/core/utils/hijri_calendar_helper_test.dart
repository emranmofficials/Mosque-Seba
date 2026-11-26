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

    test('computeHijriParts returns valid Hijri year, month, day and applies offset', () {
      final base = HijriCalendarHelper.computeHijriParts(DateTime(2026, 11, 26));
      final shifted = HijriCalendarHelper.computeHijriParts(
        DateTime(2026, 11, 26),
        moonOffsetDays: 1,
      );

      expect(base.year, greaterThanOrEqualTo(1445));
      expect(base.month, inInclusiveRange(1, 12));
      expect(base.day, inInclusiveRange(1, 30));
      expect(shifted.day != base.day || shifted.month != base.month, isTrue);
    });
  });
}
