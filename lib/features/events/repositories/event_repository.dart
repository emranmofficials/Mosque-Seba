import '../../../core/database/offline_cache_service.dart';
import '../models/mosque_event_model.dart';

/// Repository for Mosque Events & Mahfils (মসজিদ অনুষ্ঠান ও ওয়াজ মাহফিল রিপোজিটরি)
class EventRepository {
  final OfflineCacheService _cache;

  EventRepository({OfflineCacheService? cache})
      : _cache = cache ?? OfflineCacheService.instance;

  /// Fetch all events for a mosque with optional status filter
  Future<List<MosqueEventModel>> fetchEvents({
    required String mosqueId,
    String? statusFilter,
  }) async {
    final equals = <String, dynamic>{};
    if (statusFilter != null && statusFilter.isNotEmpty && statusFilter != 'all') {
      equals['status'] = statusFilter;
    }

    final rows = await _cache.fetchList(
      tableName: 'mosque_events',
      mosqueId: mosqueId,
      orderBy: 'event_date',
      ascending: true,
      equalsFilters: equals,
    );

    return rows.map(MosqueEventModel.fromJson).toList();
  }

  /// Create a new event
  Future<MosqueEventModel?> createEvent({
    required String mosqueId,
    required MosqueEventModel event,
  }) async {
    try {
      final payload = event.toJson();
      if ((payload['id']?.toString().trim() ?? '').isEmpty) {
        payload.remove('id');
      }
      final res = await _cache.insertRecord(
        tableName: 'mosque_events',
        mosqueId: mosqueId,
        payload: payload,
      );
      return MosqueEventModel.fromJson(res);
    } catch (_) {
      return null;
    }
  }

  /// Update event details or actual expense
  Future<bool> updateEvent(MosqueEventModel event, {String? mosqueId}) async {
    try {
      return await _cache.updateRecord(
        tableName: 'mosque_events',
        id: event.id,
        mosqueId: mosqueId,
        updates: event.toJson(),
      );
    } catch (_) {
      return false;
    }
  }

  /// Delete an event
  Future<bool> deleteEvent(String eventId) async {
    try {
      return await _cache.deleteRecord(tableName: 'mosque_events', id: eventId);
    } catch (_) {
      return false;
    }
  }
}
