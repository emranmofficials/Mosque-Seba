import '../../../core/database/offline_cache_service.dart';
import '../models/election_candidate_model.dart';
import '../models/election_schedule_model.dart';

/// Repository for Mosque Committee Elections & Gazette (নির্বাচন ও গেজেট রিপোজিটরি)
class ElectionRepository {
  final OfflineCacheService _cache;

  ElectionRepository({OfflineCacheService? cache})
      : _cache = cache ?? OfflineCacheService.instance;

  /// Fetch active and historical election schedules
  Future<List<ElectionScheduleModel>> fetchSchedules({
    required String mosqueId,
  }) async {
    final rows = await _cache.fetchList(
      tableName: 'election_schedules',
      mosqueId: mosqueId,
      orderBy: 'created_at',
      ascending: false,
    );

    return rows.map(ElectionScheduleModel.fromJson).toList();
  }

  /// Announce a new election schedule
  Future<ElectionScheduleModel?> createSchedule({
    required String mosqueId,
    required ElectionScheduleModel schedule,
  }) async {
    try {
      final payload = schedule.toJson();
      if ((payload['id']?.toString().trim() ?? '').isEmpty) {
        payload.remove('id');
      }
      final res = await _cache.insertRecord(
        tableName: 'election_schedules',
        mosqueId: mosqueId,
        payload: payload,
      );
      return ElectionScheduleModel.fromJson(res);
    } catch (_) {
      return null;
    }
  }

  /// Fetch all candidates for a specific election
  Future<List<ElectionCandidateModel>> fetchCandidates(String scheduleId) async {
    final rows = await _cache.fetchList(
      tableName: 'election_candidates',
      mosqueId: scheduleId,
      filterByMosqueId: false,
      orderBy: 'votes_received',
      ascending: false,
      equalsFilters: {'schedule_id': scheduleId},
    );

    final list = rows.map(ElectionCandidateModel.fromJson).toList();
    list.sort((a, b) => b.votesReceived.compareTo(a.votesReceived));
    return list;
  }

  /// Nominate candidate
  Future<ElectionCandidateModel?> nominateCandidate(
    ElectionCandidateModel candidate,
  ) async {
    try {
      final payload = candidate.toJson();
      if ((payload['id']?.toString().trim() ?? '').isEmpty) {
        payload.remove('id');
      }
      final res = await _cache.insertRecord(
        tableName: 'election_candidates',
        mosqueId: candidate.scheduleId,
        attachMosqueId: false,
        payload: payload,
      );
      return ElectionCandidateModel.fromJson(res);
    } catch (_) {
      return null;
    }
  }

  /// Record vote count or declaration
  Future<bool> updateVoteCount(
    String candidateId,
    int votes,
    bool isElected,
  ) async {
    try {
      return await _cache.updateRecord(
        tableName: 'election_candidates',
        id: candidateId,
        updates: {
          'votes_received': votes,
          'is_elected': isElected,
        },
      );
    } catch (_) {
      return false;
    }
  }
}
