import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../core/network/supabase_service.dart';
import '../models/mosque_event_model.dart';

/// Repository for Mosque Events & Mahfils (মসজিদ অনুষ্ঠান ও ওয়াজ মাহফিল রিপোজিটরি)
class EventRepository {
  SupabaseClient get _client => SupabaseService.client;

  /// Fetch all events for a mosque with optional status filter
  Future<List<MosqueEventModel>> fetchEvents({
    required String mosqueId,
    String? statusFilter,
  }) async {
    try {
      var query = _client.from('mosque_events').select().eq('mosque_id', mosqueId);
      if (statusFilter != null && statusFilter.isNotEmpty && statusFilter != 'all') {
        query = query.eq('status', statusFilter);
      }

      final res = await query.order('event_date', ascending: true);
      final List<dynamic> data = res as List<dynamic>;

      return data
          .map((item) => MosqueEventModel.fromJson(item as Map<String, dynamic>))
          .toList();
    } catch (e) {
      return [];
    }
  }

  /// Create a new event
  Future<MosqueEventModel?> createEvent({
    required String mosqueId,
    required MosqueEventModel event,
  }) async {
    try {
      final payload = event.toJson();
      payload['mosque_id'] = mosqueId;
      if (payload['id'] == null || (payload['id'] as String).isEmpty) {
        payload['id'] = 'ev-${DateTime.now().millisecondsSinceEpoch}';
      }

      final res = await _client.from('mosque_events').insert(payload).select().single();
      return MosqueEventModel.fromJson(res);
    } catch (e) {
      return null;
    }
  }

  /// Update event
  Future<bool> updateEvent(MosqueEventModel event) async {
    try {
      await _client.from('mosque_events').update(event.toJson()).eq('id', event.id);
      return true;
    } catch (e) {
      return false;
    }
  }
}
