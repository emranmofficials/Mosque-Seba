import 'package:flutter/foundation.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:timezone/data/latest.dart' as tzdata;
import 'package:timezone/timezone.dart' as tz;

/// Manages local notifications, prayer Azan alarms, and urgent mosque alerts.
class NotificationService {
  static final NotificationService instance = NotificationService();

  static const String _azanPrefsPrefix = 'key_azan_alarm_';
  static const String _azanChannelId = 'mosque_seba_azan_channel';
  static const String _alertChannelId = 'mosque_seba_alerts_channel';

  final FlutterLocalNotificationsPlugin _plugin =
      FlutterLocalNotificationsPlugin();

  bool _initialized = false;
  void Function(String? payload)? _onTapPayload;

  /// Fixed notification IDs reserved for the 5 daily waqt alarms
  static const Map<String, int> waqtAlarmIds = {
    'ফজর': 1001,
    'যোহর': 1002,
    'জুমুআ': 1002,
    'আসর': 1003,
    'মাগরিব': 1004,
    'ইশা': 1005,
  };

  Future<void> initialize({
    void Function(String? payload)? onTapPayload,
  }) async {
    _onTapPayload = onTapPayload ?? _onTapPayload;
    if (_initialized || kIsWeb) return;

    try {
      tzdata.initializeTimeZones();
      tz.setLocalLocation(tz.getLocation('Asia/Dhaka'));

      const androidSettings = AndroidInitializationSettings('@mipmap/ic_launcher');
      const darwinSettings = DarwinInitializationSettings(
        requestAlertPermission: true,
        requestBadgePermission: true,
        requestSoundPermission: true,
      );

      const settings = InitializationSettings(
        android: androidSettings,
        iOS: darwinSettings,
        macOS: darwinSettings,
      );

      await _plugin.initialize(
        settings,
        onDidReceiveNotificationResponse: (NotificationResponse response) {
          _onTapPayload?.call(response.payload);
        },
      );

      final androidImpl = _plugin.resolvePlatformSpecificImplementation<
          AndroidFlutterLocalNotificationsPlugin>();
      if (androidImpl != null) {
        await androidImpl.createNotificationChannel(
          const AndroidNotificationChannel(
            _azanChannelId,
            'নামাজ ও আজান অ্যালার্ম',
            description: 'দৈনিক ৫ ওয়াক্ত নামাজের সময় ও আজানের রিমাইন্ডার',
            importance: Importance.max,
          ),
        );
        await androidImpl.createNotificationChannel(
          const AndroidNotificationChannel(
            _alertChannelId,
            'মসজিদ জরুরি বিজ্ঞপ্তি ও জানাজা',
            description: 'জরুরি নোটিশ, জানাজা অ্যালার্ট এবং প্রশাসনিক বার্তা',
            importance: Importance.high,
          ),
        );
      }

      _initialized = true;
    } catch (e) {
      debugPrint('NotificationService init skipped: $e');
    }
  }

  /// Requests OS notification and exact-alarm permissions where supported.
  Future<bool> requestPermissions() async {
    await initialize();
    if (!_initialized) return false;
    try {
      final androidImpl = _plugin.resolvePlatformSpecificImplementation<
          AndroidFlutterLocalNotificationsPlugin>();
      if (androidImpl != null) {
        final notifGranted =
            await androidImpl.requestNotificationsPermission() ?? true;
        await androidImpl.requestExactAlarmsPermission();
        return notifGranted;
      }
      final iosImpl = _plugin.resolvePlatformSpecificImplementation<
          IOSFlutterLocalNotificationsPlugin>();
      if (iosImpl != null) {
        return await iosImpl.requestPermissions(
              alert: true,
              badge: true,
              sound: true,
            ) ??
            false;
      }
      return true;
    } catch (e) {
      debugPrint('Notification permission error: $e');
      return false;
    }
  }

  /// Shows an immediate local push notification for urgent notices/Janaza/etc.
  Future<void> showInstantAlert({
    required String title,
    required String body,
    String? payload,
    bool isUrgent = false,
  }) async {
    await initialize();
    if (!_initialized) return;

    try {
      final id = DateTime.now().millisecondsSinceEpoch % 100000;
      final details = NotificationDetails(
        android: AndroidNotificationDetails(
          _alertChannelId,
          'মসজিদ জরুরি বিজ্ঞপ্তি ও জানাজা',
          channelDescription: 'জরুরি নোটিশ, জানাজা অ্যালার্ট এবং প্রশাসনিক বার্তা',
          importance: isUrgent ? Importance.max : Importance.high,
          priority: isUrgent ? Priority.max : Priority.high,
          icon: '@mipmap/ic_launcher',
        ),
        iOS: const DarwinNotificationDetails(),
      );

      await _plugin.show(id, title, body, details, payload: payload);
    } catch (e) {
      debugPrint('showInstantAlert error: $e');
    }
  }

  /// Reads whether a daily waqt alarm toggle is enabled.
  Future<bool> isWaqtAlarmEnabled(String waqtKey) async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getBool('$_azanPrefsPrefix$waqtKey') ?? false;
  }

  /// Reads all 5 daily waqt alarm preferences.
  Future<Map<String, bool>> getAllWaqtAlarmStates() async {
    final prefs = await SharedPreferences.getInstance();
    return {
      for (final key in const ['ফজর', 'যোহর', 'আসর', 'মাগরিব', 'ইশা'])
        key: prefs.getBool('$_azanPrefsPrefix$key') ?? false,
    };
  }

  /// Enables or disables a recurring daily prayer Azan notification.
  Future<bool> setWaqtAlarm({
    required String waqtKey,
    required bool enabled,
    required int hour24,
    required int minute,
    String? displayTime,
  }) async {
    final normalizedKey = waqtKey == 'জুমুআ' ? 'যোহর' : waqtKey;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('$_azanPrefsPrefix$normalizedKey', enabled);

    final id = waqtAlarmIds[normalizedKey] ?? 1099;
    await initialize();
    if (!_initialized) return enabled;

    if (!enabled) {
      await _plugin.cancel(id);
      return false;
    }

    await requestPermissions();

    try {
      final now = tz.TZDateTime.now(tz.local);
      var scheduled = tz.TZDateTime(
        tz.local,
        now.year,
        now.month,
        now.day,
        hour24,
        minute,
      );
      if (scheduled.isBefore(now)) {
        scheduled = scheduled.add(const Duration(days: 1));
      }

      const details = NotificationDetails(
        android: AndroidNotificationDetails(
          _azanChannelId,
          'নামাজ ও আজান অ্যালার্ম',
          channelDescription: 'দৈনিক ৫ ওয়াক্ত নামাজের সময় ও আজানের রিমাইন্ডার',
          importance: Importance.max,
          priority: Priority.max,
          icon: '@mipmap/ic_launcher',
        ),
        iOS: DarwinNotificationDetails(),
      );

      final timeLabel = displayTime ??
          '${hour24.toString().padLeft(2, '0')}:${minute.toString().padLeft(2, '0')}';

      try {
        await _plugin.zonedSchedule(
          id,
          '🕌 $normalizedKey ওয়াক্তের আজান',
          '$normalizedKey নামাজের সময় হয়েছে ($timeLabel)। মসজিদে জামাতের প্রস্তুতি নিন।',
          scheduled,
          details,
          uiLocalNotificationDateInterpretation:
              UILocalNotificationDateInterpretation.absoluteTime,
          androidScheduleMode: AndroidScheduleMode.exactAllowWhileIdle,
          matchDateTimeComponents: DateTimeComponents.time,
          payload: '/prayer-times',
        );
      } catch (_) {
        await _plugin.zonedSchedule(
          id,
          '🕌 $normalizedKey ওয়াক্তের আজান',
          '$normalizedKey নামাজের সময় হয়েছে ($timeLabel)। মসজিদে জামাতের প্রস্তুতি নিন।',
          scheduled,
          details,
          uiLocalNotificationDateInterpretation:
              UILocalNotificationDateInterpretation.absoluteTime,
          androidScheduleMode: AndroidScheduleMode.inexactAllowWhileIdle,
          matchDateTimeComponents: DateTimeComponents.time,
          payload: '/prayer-times',
        );
      }
      return true;
    } catch (e) {
      debugPrint('schedulePrayerAlarm failed: $e');
      return false;
    }
  }
}
