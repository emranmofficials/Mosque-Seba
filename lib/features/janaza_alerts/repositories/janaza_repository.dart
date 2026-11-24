import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../../core/network/supabase_service.dart';
import '../models/janaza_alert_model.dart';

/// Repository for Emergency Janaza Alerts (জরুরি জানাজা সম্প্রচার রিপোজিটরি)
class JanazaRepository {
  final SupabaseService _supabaseService;

  JanazaRepository({SupabaseService? supabaseService})
      : _supabaseService = supabaseService ?? SupabaseService();

  SupabaseClient get _client => _supabaseService.client;

  /// Fetch active and historical Janaza notices
  Future<List<JanazaAlertModel>> fetchJanazaAlerts({required String mosqueId}) async {
    try {
      final response = await _client
          .from('notices')
          .select()
          .eq('mosque_id', mosqueId)
          .eq('priority', 'urgent')
          .order('created_at', ascending: false);

      final List<dynamic> data = response as List<dynamic>;

      return data.where((item) {
        final title = (item['title'] as String? ?? '').toLowerCase();
        return title.contains('জানাজা') || title.contains('শোক');
      }).map((item) {
        return JanazaAlertModel(
          id: item['id'] as String? ?? '',
          deceasedName: item['title'] as String? ?? '',
          timeOfDeath: '',
          janazaVenue: 'মসজিদ প্রাঙ্গণ',
          janazaTime: item['publish_date'] as String? ?? '',
          extraNotes: item['content'] as String? ?? '',
          broadcastAt: item['publish_date'] as String? ?? '',
        );
      }).toList();
    } catch (e) {
      return [];
    }
  }

  /// Broadcast an emergency Janaza alert and cross-post to Notices Board
  Future<JanazaAlertModel?> broadcastAlert({
    required String mosqueId,
    required String mosqueName,
    required JanazaAlertModel alert,
    required String broadcastBy,
  }) async {
    try {
      final fullBroadcast = alert.toBroadcastMessage(mosqueName);

      // Auto-post to Notices as URGENT Pinned Notice
      final noticePayload = {
        'mosque_id': mosqueId,
        'title': '🚨 জরুরি জানাজা: ${alert.deceasedName}',
        'content': fullBroadcast,
        'priority': 'urgent',
        'is_pinned': true,
        'publish_date': DateTime.now().toIso8601String().split('T').first,
        'created_by': broadcastBy,
      };

      await _client.from('notices').insert(noticePayload);

      return alert;
    } catch (e) {
      return null;
    }
  }
}
