import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../../core/network/supabase_service.dart';
import '../models/mosque_event_model.dart';

/// Repository for Mosque Events & Mahfils (মসজিদ অনুষ্ঠান ও ওয়াজ মাহফিল রিপোজিটরি)
class EventRepository {
  final SupabaseService _supabaseService;

  EventRepository({SupabaseService? supabaseService})
      : _supabaseService = supabaseService ?? SupabaseService();

  SupabaseClient get _client => _supabaseService.client;

  static final List<MosqueEventModel> _mockEvents = [
    MosqueEventModel(
      id: 'ev-001',
      title: 'ঐতিহাসিক বাৎসরিক তাফসিরুল কুরআন ও ওয়াজ মাহফিল ২০২৬',
      eventType: 'waz_mahfil',
      eventDate: '2026-12-15',
      eventTime: 'বাদ আসর হতে মধ্যরাত পর্যন্ত',
      venue: 'মসজিদ সংলগ্ন ঈদগাহ ময়দান',
      chiefGuest: 'মুফতী মাওলানা মিজানুর রহমান',
      specialSpeakers: 'হাফেজ কারী মো. ফোরকান উল্লাহ, মুফতী উসমান গনী',
      estimatedBudget: 85000.0,
      actualExpense: 0.0,
      status: 'upcoming',
      description: 'সার্বজনীন তাফসিরুল কুরআন মাহফিল। সর্বস্তরের ধর্মপ্রাণ মুসল্লিদের সবান্ধব উপস্থিতি কামনা করা যাচ্ছে।',
      createdAt: DateTime.now().millisecondsSinceEpoch,
    ),
    MosqueEventModel(
      id: 'ev-002',
      title: 'পবিত্র মাহে রমজানের গণ-ইফতার ও দোয়া মাহফিল',
      eventType: 'iftar_party',
      eventDate: '2026-03-25',
      eventTime: 'বিকাল ৫:০০ ঘটিকা',
      venue: 'মসজিদের নিচতলা ও প্রাঙ্গণ',
      chiefGuest: 'মসজিদ কমিটির সভাপতি মহোদয়',
      specialSpeakers: 'খতিব মাওলানা নুরুল ইসলাম',
      estimatedBudget: 45000.0,
      actualExpense: 42000.0,
      status: 'completed',
      description: 'এলাকার ৫০০ জন সম্মানিত মুসল্লি ও পথচারীদের সম্মানে বিশেষ ইফতার মাহফিল।',
      createdAt: DateTime.now().subtract(const Duration(days: 200)).millisecondsSinceEpoch,
    ),
  ];

  /// Fetch all events with status filter
  Future<List<MosqueEventModel>> fetchEvents({String? statusFilter}) async {
    try {
      var query = _client.from('mosque_events').select();
      if (statusFilter != null && statusFilter.isNotEmpty && statusFilter != 'all') {
        query = query.eq('status', statusFilter);
      }

      final res = await query.order('event_date', ascending: true);
      final List<dynamic> data = res as List<dynamic>;

      if (data.isEmpty) return _mockEvents;
      return data.map((item) => MosqueEventModel.fromJson(item as Map<String, dynamic>)).toList();
    } catch (e) {
      if (statusFilter != null && statusFilter != 'all') {
        return _mockEvents.where((ev) => ev.status == statusFilter).toList();
      }
      return _mockEvents;
    }
  }

  /// Create a new event
  Future<MosqueEventModel?> createEvent(MosqueEventModel event) async {
    try {
      final payload = event.toJson();
      if (payload['id'] == null || (payload['id'] as String).isEmpty) {
        payload['id'] = 'ev-${DateTime.now().millisecondsSinceEpoch}';
      }

      final res = await _client.from('mosque_events').insert(payload).select().single();
      return MosqueEventModel.fromJson(res);
    } catch (e) {
      return event;
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
