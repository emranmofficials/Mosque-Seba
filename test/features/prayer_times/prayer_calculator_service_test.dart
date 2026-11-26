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

    test('computes valid Sehri and Iftar solar times and district list', () {
      final times = PrayerCalculatorService.computeSehriIftarTimes(
        referenceTime: DateTime(2026, 3, 15, 12, 0),
      );

      expect(times.sehriEnd.isBefore(times.iftarStart), isTrue);
      expect(times.sehriEnd.hour, inInclusiveRange(4, 6));
      expect(times.iftarStart.hour, inInclusiveRange(17, 19));

      expect(PrayerCalculatorService.bangladeshDistricts.length, 14);
      final ctg = PrayerCalculatorService.findDistrict('চট্টগ্রাম');
      expect(ctg.nameBn, 'চট্টগ্রাম');
      expect(ctg.latitude, inInclusiveRange(20.0, 27.0));
      expect(ctg.longitude, inInclusiveRange(88.0, 93.0));

      final fallback = PrayerCalculatorService.findDistrict('অজানা শহর');
      expect(fallback.nameBn, 'ঢাকা');
    });
  });
}
