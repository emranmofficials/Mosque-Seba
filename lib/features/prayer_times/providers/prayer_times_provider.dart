import 'dart:async';
import 'dart:convert';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../../bootstrap/dependency_injection.dart';
import '../../../core/services/notification_service.dart';
import '../models/prayer_time_item.dart';
import '../services/prayer_calculator_service.dart';

class PrayerSettingsState {
  final String districtNameBn;
  final double latitude;
  final double longitude;
  final Map<String, ({int hour, int minute})> customIqamah;
  final Map<String, bool> alarmEnabled;

  const PrayerSettingsState({
    this.districtNameBn = 'ঢাকা',
    this.latitude = 23.8103,
    this.longitude = 90.4125,
    this.customIqamah = const {},
    this.alarmEnabled = const {},
  });

  PrayerSettingsState copyWith({
    String? districtNameBn,
    double? latitude,
    double? longitude,
    Map<String, ({int hour, int minute})>? customIqamah,
    Map<String, bool>? alarmEnabled,
  }) {
    return PrayerSettingsState(
      districtNameBn: districtNameBn ?? this.districtNameBn,
      latitude: latitude ?? this.latitude,
      longitude: longitude ?? this.longitude,
      customIqamah: customIqamah ?? this.customIqamah,
      alarmEnabled: alarmEnabled ?? this.alarmEnabled,
    );
  }
}

class PrayerSettingsNotifier extends StateNotifier<PrayerSettingsState> {
  static const String _keyDistrict = 'key_prayer_district_bn';
  static const String _keyCustomIqamah = 'key_prayer_custom_iqamah';

  final SharedPreferences? _prefs;

  PrayerSettingsNotifier(this._prefs) : super(const PrayerSettingsState()) {
    _load();
  }

  Future<void> _load() async {
    final prefs = _prefs ?? await SharedPreferences.getInstance();
    final districtName = prefs.getString(_keyDistrict) ?? 'ঢাকা';
    final coord = PrayerCalculatorService.findDistrict(districtName);

    final rawJson = prefs.getString(_keyCustomIqamah);
    final customMap = <String, ({int hour, int minute})>{};
    if (rawJson != null && rawJson.isNotEmpty) {
      try {
        final decoded = jsonDecode(rawJson) as Map<String, dynamic>;
        for (final entry in decoded.entries) {
          final m = entry.value as Map<String, dynamic>;
          customMap[entry.key] = (
            hour: (m['h'] as num).toInt(),
            minute: (m['m'] as num).toInt(),
          );
        }
      } catch (_) {}
    }

    final alarms = await NotificationService.instance.getAllWaqtAlarmStates();
    if (!mounted) return;
    state = state.copyWith(
      districtNameBn: coord.nameBn,
      latitude: coord.latitude,
      longitude: coord.longitude,
      customIqamah: customMap,
      alarmEnabled: alarms,
    );
  }

  Future<void> setDistrict(String districtNameBn) async {
    final coord = PrayerCalculatorService.findDistrict(districtNameBn);
    final prefs = _prefs ?? await SharedPreferences.getInstance();
    await prefs.setString(_keyDistrict, coord.nameBn);
    if (!mounted) return;
    state = state.copyWith(
      districtNameBn: coord.nameBn,
      latitude: coord.latitude,
      longitude: coord.longitude,
    );
  }

  Future<void> setCustomIqamah(String waqtName, int hour, int minute) async {
    final updated = Map<String, ({int hour, int minute})>.from(state.customIqamah);
    updated[waqtName] = (hour: hour, minute: minute);
    final prefs = _prefs ?? await SharedPreferences.getInstance();
    final encoded = updated.map((k, v) => MapEntry(k, {'h': v.hour, 'm': v.minute}));
    await prefs.setString(_keyCustomIqamah, jsonEncode(encoded));
    if (!mounted) return;
    state = state.copyWith(customIqamah: updated);
  }

  Future<void> resetCustomIqamah() async {
    final prefs = _prefs ?? await SharedPreferences.getInstance();
    await prefs.remove(_keyCustomIqamah);
    if (!mounted) return;
    state = state.copyWith(customIqamah: const {});
  }

  Future<bool> toggleWaqtAlarm({
    required String waqtName,
    required bool enabled,
    required int hour24,
    required int minute,
    required String displayTime,
  }) async {
    final actual = await NotificationService.instance.setWaqtAlarm(
      waqtKey: waqtName,
      enabled: enabled,
      hour24: hour24,
      minute: minute,
      displayTime: displayTime,
    );
    final key = waqtName == 'জুমুআ' ? 'যোহর' : waqtName;
    final updated = Map<String, bool>.from(state.alarmEnabled);
    updated[key] = actual;
    if (mounted) {
      state = state.copyWith(alarmEnabled: updated);
    }
    return actual;
  }
}

final prayerSettingsProvider =
    StateNotifierProvider<PrayerSettingsNotifier, PrayerSettingsState>((ref) {
  SharedPreferences? prefs;
  try {
    prefs = ref.watch(sharedPreferencesProvider);
  } catch (_) {}
  return PrayerSettingsNotifier(prefs);
});

final prayerTimeStreamProvider = StreamProvider<
    ({List<PrayerTimeItem> schedule, NextPrayerSummary nextSummary})>((ref) {
  final settings = ref.watch(prayerSettingsProvider);
  final controller = StreamController<
      ({List<PrayerTimeItem> schedule, NextPrayerSummary nextSummary})>();

  void emitLatest() {
    if (!controller.isClosed) {
      controller.add(
        PrayerCalculatorService.computeTodayPrayerSchedule(
          latitude: settings.latitude,
          longitude: settings.longitude,
          customIqamah: settings.customIqamah,
        ),
      );
    }
  }

  emitLatest();

  final timer = Timer.periodic(const Duration(seconds: 30), (_) {
    emitLatest();
  });

  ref.onDispose(() {
    timer.cancel();
    controller.close();
  });

  return controller.stream;
});
