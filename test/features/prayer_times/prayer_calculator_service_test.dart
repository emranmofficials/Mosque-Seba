import 'package:flutter_test/flutter_test.dart';
import 'package:mosqueseba/features/prayer_times/services/prayer_calculator_service.dart';

void main() {
  group('PrayerCalculatorService', () {
    test('uses জুমুআ on Friday and যোহর on other days', () {
      final fridayResult = PrayerCalculatorService.computeTodayPrayerSchedule(
        referenceTime: DateTime(2026, 11, 27, 10, 0), // Friday
      );
      final thursdayResult = PrayerCalculatorService.computeTodayPrayerSchedule(
        referenceTime: DateTime(2026, 11, 26, 10, 0), // Thursday
      );

      expect(fridayResult.schedule.length, 5);
      expect(fridayResult.nextSummary.nameBn, 'জুমুআ');
      expect(thursdayResult.nextSummary.nameBn, 'যোহর');
    });

    test('rolls over to tomorrow Fajr after Isha', () {
      final lateNight = PrayerCalculatorService.computeTodayPrayerSchedule(
        referenceTime: DateTime(2026, 11, 26, 22, 0),
      );
      expect(lateNight.nextSummary.nameBn, 'ফজর');
      expect(lateNight.nextSummary.remainingMinutes, greaterThan(0));
    });
  });
}
