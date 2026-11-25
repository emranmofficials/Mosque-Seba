import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../../core/network/supabase_service.dart';
import '../models/election_candidate_model.dart';
import '../models/election_schedule_model.dart';

/// Repository for Mosque Committee Elections & Gazette (নির্বাচন ও গেজেট রিপোজিটরি)
class ElectionRepository {
  final SupabaseService _supabaseService;

  ElectionRepository({SupabaseService? supabaseService})
      : _supabaseService = supabaseService ?? SupabaseService();

  SupabaseClient get _client => SupabaseService.client;

  /// Fetch active and historical election schedules
  Future<List<ElectionScheduleModel>> fetchSchedules({required String mosqueId}) async {
    try {
      final response = await _client
          .from('election_schedules')
          .select()
          .order('created_at', ascending: false);

      final List<dynamic> data = response as List<dynamic>;
      return data
          .map((item) => ElectionScheduleModel.fromJson(item as Map<String, dynamic>))
          .toList();
    } catch (e) {
      return [];
    }
  }

  /// Announce a new election schedule
  Future<ElectionScheduleModel?> createSchedule(ElectionScheduleModel schedule) async {
    try {
      final payload = schedule.toJson();
      if (payload['id'] == null || (payload['id'] as String).isEmpty) {
        payload['id'] = 'elec-${DateTime.now().millisecondsSinceEpoch}';
      }

      final res = await _client.from('election_schedules').insert(payload).select().single();
      return ElectionScheduleModel.fromJson(res);
    } catch (e) {
      return null;
    }
  }

  /// Fetch all candidates for a specific election
  Future<List<ElectionCandidateModel>> fetchCandidates(String scheduleId) async {
    try {
      final response = await _client
          .from('election_candidates')
          .select()
          .eq('schedule_id', scheduleId)
          .order('votes_received', ascending: false);

      final List<dynamic> data = response as List<dynamic>;
      return data
          .map((item) => ElectionCandidateModel.fromJson(item as Map<String, dynamic>))
          .toList();
    } catch (e) {
      return [];
    }
  }

  /// Nominate candidate
  Future<ElectionCandidateModel?> nominateCandidate(ElectionCandidateModel candidate) async {
    try {
      final payload = candidate.toJson();
      if (payload['id'] == null || (payload['id'] as String).isEmpty) {
        payload['id'] = 'cand-${DateTime.now().millisecondsSinceEpoch}';
      }

      final res = await _client.from('election_candidates').insert(payload).select().single();
      return ElectionCandidateModel.fromJson(res);
    } catch (e) {
      return null;
    }
  }

  /// Record vote count or declaration
  Future<bool> updateVoteCount(String candidateId, int votes, bool isElected) async {
    try {
      await _client.from('election_candidates').update({
        'votes_received': votes,
        'is_elected': isElected,
      }).eq('id', candidateId);
      return true;
    } catch (e) {
      return false;
    }
  }
}
