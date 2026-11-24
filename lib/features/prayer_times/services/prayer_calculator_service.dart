import '../../../core/utils/bengali_number_formatter.dart';
import '../models/prayer_time_item.dart';

/// Islamic Prayer Times & Iqamah Calculation Service (PrayerCard.kt Parity)
class PrayerCalculatorService {
  /// Computes today's prayer schedule and finds the next upcoming waqt
  static ({List<PrayerTimeItem> schedule, NextPrayerSummary nextSummary})
      computeTodayPrayerSchedule({DateTime? referenceTime}) {
    final now = referenceTime ?? DateTime.now();
    final isFriday = now.weekday == DateTime.friday;
    final dhuhrLabel = isFriday ? 'জুমুআ' : 'যোহর';

    // Base prayer schedule for Bangladesh standard mosque timings
    final rawTimes = [
      ('ফজর', '০৪:৩০', '০৫:০০', 5, 0),
      (dhuhrLabel, '১২:৩০', isFriday ? '০১:৩০' : '০১:১৫', isFriday ? 13 : 13, isFriday ? 30 : 15),
      ('আসর', '০৪:৩০', '০৪:৪৫', 16, 45),
      ('মাগরিব', '০৬:১৮', '০৬:২৩', 18, 23),
      ('ইশা', '০৭:৪৫', '০৮:১৫', 20, 15),
    ];

    int nextIndex = -1;
    final List<PrayerTimeItem> schedule = [];

    for (int i = 0; i < rawTimes.length; i++) {
      final (name, azan, iqamah, hour, minute) = rawTimes[i];
      final iqamahTime = DateTime(now.year, now.month, now.day, hour, minute);
      final isUpcoming = iqamahTime.isAfter(now);

      if (isUpcoming && nextIndex == -1) {
        nextIndex = i;
      }

      schedule.add(
        PrayerTimeItem(
          nameBn: name,
          azanTime: azan,
          iqamahTime: iqamah,
          iqamahDateTime: iqamahTime,
          isNext: false,
        ),
      );
    }

    // If all today's prayers have passed, next is tomorrow's Fajr
    if (nextIndex == -1) {
      nextIndex = 0;
    }

    // Mark the next item
    final updatedSchedule = schedule.asMap().entries.map((entry) {
      final i = entry.key;
      final item = entry.value;
      return PrayerTimeItem(
        nameBn: item.nameBn,
        azanTime: item.azanTime,
        iqamahTime: item.iqamahTime,
        iqamahDateTime: item.iqamahDateTime,
        isNext: i == nextIndex,
      );
    }).toList();

    final nextItem = updatedSchedule[nextIndex];
    DateTime targetIqamah = nextItem.iqamahDateTime;
    if (targetIqamah.isBefore(now)) {
      targetIqamah = targetIqamah.add(const Duration(days: 1));
    }

    final diff = targetIqamah.difference(now);
    final remainingMinutes = diff.inMinutes;
    final hours = remainingMinutes ~/ 60;
    final mins = remainingMinutes % 60;

    String remainingTextBn = '';
    if (hours > 0) {
      remainingTextBn = '${BengaliNumberFormatter.toBengaliDigits(hours)} ঘণ্টা ${BengaliNumberFormatter.toBengaliDigits(mins)} মিনিট বাকি';
    } else {
      remainingTextBn = '${BengaliNumberFormatter.toBengaliDigits(mins)} মিনিট বাকি';
    }

    final summary = NextPrayerSummary(
      nameBn: nextItem.nameBn,
      iqamahTimeBn: nextItem.iqamahTime,
      remainingTextBn: remainingTextBn,
      remainingMinutes: remainingMinutes,
    );

    return (schedule: updatedSchedule, nextSummary: summary);
  }
}
