import '../../../core/database/offline_cache_service.dart';
import '../../../core/services/notification_service.dart';
import '../models/janaza_alert_model.dart';

/// Repository for Emergency Janaza Alerts (জরুরি জানাজা সম্প্রচার রিপোজিটরি)
class JanazaRepository {
  final OfflineCacheService _cache;

  JanazaRepository({OfflineCacheService? cache})
      : _cache = cache ?? OfflineCacheService.instance;

  /// Fetch active and historical Janaza notices while reconstructing metadata lines
  Future<List<JanazaAlertModel>> fetchJanazaAlerts({
    required String mosqueId,
  }) async {
    final rows = await _cache.fetchList(
      tableName: 'notices',
      mosqueId: mosqueId,
      orderBy: 'created_at',
      ascending: false,
      equalsFilters: {'priority': 'urgent'},
    );

    return rows.where((item) {
      final title = (item['title'] as String? ?? '').toLowerCase();
      return title.contains('জানাজা') || title.contains('শোক');
    }).map((item) {
      final rawTitle = item['title'] as String? ?? '';
      final content = item['content'] as String? ?? '';
      final deceased = rawTitle.replaceFirst('🚨 জরুরি জানাজা: ', '').trim();
      final lines = content.split('\n').map((l) => l.trim()).toList();

      String extractAfter(String prefix, {String fallback = ''}) {
        for (final line in lines) {
          if (line.startsWith(prefix)) {
            return line.substring(prefix.length).trim();
          }
        }
        return fallback;
      }

      String relation = 'পিতা';
      String guardian = '';
      String age = '';
      String village = '';
      String timeOfDeath = '';

      for (final line in lines) {
        if (line.startsWith('(') && line.endsWith(')') && line.contains(':')) {
          final inner = line.substring(1, line.length - 1);
          final idx = inner.indexOf(':');
          relation = inner.substring(0, idx).trim();
          guardian = inner.substring(idx + 1).trim();
        } else if (line.startsWith('বয়স:') && line.contains('| ঠিকানা:')) {
          final parts = line.split('|');
          age = parts.first.replaceAll('বয়স:', '').replaceAll('বছর', '').trim();
          village = parts.last.replaceAll('ঠিকানা:', '').trim();
        } else if (line.startsWith('তিনি ') && line.contains('ইন্তেকাল')) {
          timeOfDeath =
              line.replaceFirst('তিনি ', '').replaceAll('ইন্তেকাল করেছেন।', '').trim();
        }
      }

      return JanazaAlertModel(
        id: item['id'] as String? ?? '',
        deceasedName: deceased.isNotEmpty ? deceased : rawTitle,
        relativeRelation: relation,
        guardianName: guardian,
        age: age,
        villageArea: village,
        timeOfDeath: timeOfDeath,
        janazaVenue: extractAfter('• স্থান:', fallback: 'মসজিদ প্রাঙ্গণ'),
        janazaTime: extractAfter(
          '• সময়:',
          fallback: (item['publish_date'] as String?) ??
              (item['publish_at']?.toString().split('T').first ?? ''),
        ),
        graveyardVenue: extractAfter('• দাফন:'),
        contactPhone: extractAfter('• যোগাযোগ:'),
        extraNotes: content,
        broadcastAt: (item['publish_date'] as String?) ??
            (item['publish_at']?.toString().split('T').first ?? ''),
      );
    }).toList();
  }

  /// Broadcast an emergency Janaza alert, save offline-first, add in-app notification & push alert
  Future<JanazaAlertModel?> broadcastAlert({
    required String mosqueId,
    required String mosqueName,
    required JanazaAlertModel alert,
    required String broadcastBy,
  }) async {
    try {
      final fullBroadcast = alert.toBroadcastMessage(mosqueName);
      final publishDate = DateTime.now().toIso8601String().split('T').first;

      final noticePayload = {
        'mosque_id': mosqueId,
        'title': '🚨 জরুরি জানাজা: ${alert.deceasedName}',
        'content': fullBroadcast,
        'priority': 'urgent',
        'is_pinned': true,
        'publish_date': publishDate,
        'created_by': broadcastBy,
      };

      final savedNotice = await _cache.insertRecord(
        tableName: 'notices',
        mosqueId: mosqueId,
        payload: noticePayload,
      );

      final notificationTitle = '🚨 জরুরি জানাজা সংবাদ: ${alert.deceasedName}';
      final notificationBody =
          'স্থান: ${alert.janazaVenue} • সময়: ${alert.janazaTime}';

      await _cache.insertRecord(
        tableName: 'notifications',
        mosqueId: mosqueId,
        payload: {
          'mosque_id': mosqueId,
          'title': notificationTitle,
          'body': notificationBody,
          'priority': 'urgent',
          'entity_type': 'janaza',
          'entity_id': savedNotice['id']?.toString() ?? '',
          'is_read': false,
        },
      );

      await NotificationService.instance.showInstantAlert(
        title: notificationTitle,
        body: notificationBody,
        payload: '/janaza-alerts',
        isUrgent: true,
      );

      return JanazaAlertModel(
        id: savedNotice['id']?.toString() ?? alert.id,
        deceasedName: alert.deceasedName,
        relativeRelation: alert.relativeRelation,
        guardianName: alert.guardianName,
        age: alert.age,
        villageArea: alert.villageArea,
        timeOfDeath: alert.timeOfDeath,
        janazaVenue: alert.janazaVenue,
        janazaTime: alert.janazaTime,
        graveyardVenue: alert.graveyardVenue,
        contactPhone: alert.contactPhone,
        extraNotes: alert.extraNotes,
        broadcastAt: publishDate,
      );
    } catch (_) {
      return null;
    }
  }
}
