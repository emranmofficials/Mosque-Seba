import 'dart:math' as math;
import '../../../core/utils/bengali_number_formatter.dart';
import '../models/prayer_time_item.dart';

class DistrictCoord {
  final String nameBn;
  final double latitude;
  final double longitude;

  const DistrictCoord(this.nameBn, this.latitude, this.longitude);
}

/// Solar Astronomical Prayer Times, Sehri/Iftar & Mosque Iqamah Calculator
class PrayerCalculatorService {
  static const List<DistrictCoord> bangladeshDistricts = [
    DistrictCoord('ঢাকা', 23.8103, 90.4125),
    DistrictCoord('চট্টগ্রাম', 22.3569, 91.7832),
    DistrictCoord('সিলেট', 24.8949, 91.8687),
    DistrictCoord('রাজশাহী', 24.3745, 88.6042),
    DistrictCoord('খুলনা', 22.8456, 89.5403),
    DistrictCoord('বরিশাল', 22.7010, 90.3535),
    DistrictCoord('রংপুর', 25.7439, 89.2752),
    DistrictCoord('ময়মনসিংহ', 24.7471, 90.4203),
    DistrictCoord('কুমিল্লা', 23.4607, 91.1809),
    DistrictCoord('গাজীপুর', 23.9999, 90.4203),
    DistrictCoord('নারায়ণগঞ্জ', 23.6238, 90.5000),
    DistrictCoord('বগুড়া', 24.8465, 89.3776),
    DistrictCoord('কক্সবাজার', 21.4272, 92.0058),
    DistrictCoord('যশোর', 23.1664, 89.2081),
  ];

  static DistrictCoord findDistrict(String nameBn) {
    for (final d in bangladeshDistricts) {
      if (d.nameBn == nameBn) return d;
    }
    return bangladeshDistricts.first;
  }

  static double _deg2rad(double d) => d * math.pi / 180.0;
  static double _rad2deg(double r) => r * 180.0 / math.pi;

  static double _fixAngle(double a) {
    final res = a - 360.0 * (a / 360.0).floor();
    return res < 0 ? res + 360.0 : res;
  }

  static double _fixHour(double h) {
    final res = h - 24.0 * (h / 24.0).floor();
    return res < 0 ? res + 24.0 : res;
  }

  static ({
    DateTime fajr,
    DateTime sunrise,
    DateTime dhuhr,
    DateTime asr,
    DateTime maghrib,
    DateTime isha,
  }) computeSolarTimes({
    required DateTime date,
    double latitude = 23.8103,
    double longitude = 90.4125,
    double timezoneOffsetHours = 6.0,
  }) {
    final y = date.year;
    final m = date.month;
    final day = date.day;

    var yy = y;
    var mm = m;
    if (mm <= 2) {
      yy -= 1;
      mm += 12;
    }
    final a = (yy / 100).floor();
    final b = 2 - a + (a / 4).floor();
    final jd = (365.25 * (yy + 4716)).floor() +
        (30.6001 * (mm + 1)).floor() +
        day +
        b -
        1524.5;

    final d = jd - 2451545.0;
    final g = _deg2rad(_fixAngle(357.529 + 0.98560028 * d));
    final q = _fixAngle(280.459 + 0.98564736 * d);
    final l = _deg2rad(
      _fixAngle(q + 1.915 * math.sin(g) + 0.020 * math.sin(2 * g)),
    );
    final e = _deg2rad(23.439 - 0.00000036 * d);

    final declRad = math.asin(math.sin(e) * math.sin(l));
    final raDeg = _rad2deg(math.atan2(math.cos(e) * math.sin(l), math.cos(l)));
    final raHours = _fixHour(raDeg / 15.0);
    var eqt = q / 15.0 - raHours;
    if (eqt > 12) eqt -= 24;
    if (eqt < -12) eqt += 24;

    final noon = _fixHour(12.0 + timezoneOffsetHours - longitude / 15.0 - eqt);

    double hourAngleForElevation(double angleDeg) {
      final cosH = (math.sin(_deg2rad(angleDeg)) -
              math.sin(_deg2rad(latitude)) * math.sin(declRad)) /
          (math.cos(_deg2rad(latitude)) * math.cos(declRad));
      final clamped = cosH.clamp(-0.999, 0.999);
      return _rad2deg(math.acos(clamped)) / 15.0;
    }

    final fajrHour = noon - hourAngleForElevation(-18.0);
    final sunriseHour = noon - hourAngleForElevation(-0.833);
    final dhuhrHour = noon + (2.0 / 60.0);

    // Hanafi Asr: shadow length = 2x object + noon shadow
    final diffRad = (_deg2rad(latitude) - declRad).abs();
    final asrAngleDeg = _rad2deg(math.atan(1.0 / (2.0 + math.tan(diffRad))));
    final asrHour = noon + hourAngleForElevation(asrAngleDeg);

    final maghribHour = noon + hourAngleForElevation(-0.833) + (3.0 / 60.0);
    final ishaHour = noon + hourAngleForElevation(-18.0);

    DateTime toDateTime(double fractionalHours) {
      final totalMinutes = (fractionalHours * 60).round().clamp(0, 1439);
      return DateTime(y, m, day, totalMinutes ~/ 60, totalMinutes % 60);
    }

    return (
      fajr: toDateTime(fajrHour),
      sunrise: toDateTime(sunriseHour),
      dhuhr: toDateTime(dhuhrHour),
      asr: toDateTime(asrHour),
      maghrib: toDateTime(maghribHour),
      isha: toDateTime(ishaHour),
    );
  }

  static String formatTimeBn12Hour(DateTime dt) {
    final hour12 = dt.hour % 12 == 0 ? 12 : dt.hour % 12;
    final hStr = hour12.toString().padLeft(2, '0');
    final mStr = dt.minute.toString().padLeft(2, '0');
    return BengaliNumberFormatter.toBengaliDigits('$hStr:$mStr');
  }

  /// Computes today's Sehri end (Fajr) & Iftar start (Maghrib) solar times.
  static ({DateTime sehriEnd, DateTime iftarStart}) computeSehriIftarTimes({
    DateTime? referenceTime,
    double latitude = 23.8103,
    double longitude = 90.4125,
  }) {
    final now = referenceTime ?? DateTime.now();
    final solar = computeSolarTimes(
      date: now,
      latitude: latitude,
      longitude: longitude,
    );
    return (sehriEnd: solar.fajr, iftarStart: solar.maghrib);
  }

  /// Computes today's prayer schedule and finds the next upcoming waqt
  static ({List<PrayerTimeItem> schedule, NextPrayerSummary nextSummary})
      computeTodayPrayerSchedule({
    DateTime? referenceTime,
    double latitude = 23.8103,
    double longitude = 90.4125,
    Map<String, ({int hour, int minute})>? customIqamah,
  }) {
    final now = referenceTime ?? DateTime.now();
    final isFriday = now.weekday == DateTime.friday;
    final dhuhrLabel = isFriday ? 'জুমুআ' : 'যোহর';

    final solar = computeSolarTimes(
      date: now,
      latitude: latitude,
      longitude: longitude,
    );

    DateTime resolveIqamah(String key, DateTime azanDt, int defaultDelayMin) {
      final override = customIqamah?[key];
      if (override != null) {
        return DateTime(now.year, now.month, now.day, override.hour, override.minute);
      }
      if (key == 'জুমুআ') {
        return DateTime(now.year, now.month, now.day, 13, 30);
      }
      return azanDt.add(Duration(minutes: defaultDelayMin));
    }

    final rawTimes = [
      ('ফজর', solar.fajr, resolveIqamah('ফজর', solar.fajr, 25)),
      (
        dhuhrLabel,
        solar.dhuhr,
        resolveIqamah(dhuhrLabel, solar.dhuhr, isFriday ? 45 : 35),
      ),
      ('আসর', solar.asr, resolveIqamah('আসর', solar.asr, 15)),
      ('মাগরিব', solar.maghrib, resolveIqamah('মাগরিব', solar.maghrib, 5)),
      ('ইশা', solar.isha, resolveIqamah('ইশা', solar.isha, 30)),
    ];

    int nextIndex = -1;
    final List<PrayerTimeItem> schedule = [];

    for (int i = 0; i < rawTimes.length; i++) {
      final (name, azanDt, iqamahDt) = rawTimes[i];
      if (iqamahDt.isAfter(now) && nextIndex == -1) {
        nextIndex = i;
      }

      schedule.add(
        PrayerTimeItem(
          nameBn: name,
          azanTime: formatTimeBn12Hour(azanDt),
          iqamahTime: formatTimeBn12Hour(iqamahDt),
          iqamahDateTime: iqamahDt,
          isNext: false,
        ),
      );
    }

    // If all today's prayers have passed, next is tomorrow's Fajr
    if (nextIndex == -1) {
      nextIndex = 0;
    }

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

    final String remainingTextBn = hours > 0
        ? '${BengaliNumberFormatter.toBengaliDigits(hours)} ঘণ্টা ${BengaliNumberFormatter.toBengaliDigits(mins)} মিনিট বাকি'
        : '${BengaliNumberFormatter.toBengaliDigits(mins)} মিনিট বাকি';

    final summary = NextPrayerSummary(
      nameBn: nextItem.nameBn,
      iqamahTimeBn: nextItem.iqamahTime,
      remainingTextBn: remainingTextBn,
      remainingMinutes: remainingMinutes,
    );

    return (schedule: updatedSchedule, nextSummary: summary);
  }
}
