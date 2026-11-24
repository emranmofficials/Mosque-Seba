import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../../core/network/supabase_service.dart';
import '../models/hifz_record_model.dart';

/// Repository for Quran & Hifz Progress Tracking (হিফজুল কুরআন রিপোজিটরি)
class HifzRepository {
  final SupabaseService _supabaseService;

  HifzRepository({SupabaseService? supabaseService})
      : _supabaseService = supabaseService ?? SupabaseService();

  SupabaseClient get _client => _supabaseService.client;

  static final List<HifzRecordModel> _mockRecords = [
    HifzRecordModel(
      id: 'hifz-001',
      studentId: 'std-001',
      studentNameBn: 'মুহাম্মদ তালহা জুবায়ের',
      paraNumber: 15,
      surahName: 'সূরা বনী ইসরাঈল',
      pageNumber: 285,
      sabaqRating: 5,
      sabaqiRating: 5,
      manzilRating: 4,
      teacherRemarks: 'মাশাআল্লাহ, উচ্চারণ ও তারতীল চমৎকার।',
      recordedDate: '2026-11-24',
      createdAt: DateTime.now().millisecondsSinceEpoch,
    ),
    HifzRecordModel(
      id: 'hifz-002',
      studentId: 'std-002',
      studentNameBn: 'আব্দুল্লাহ আল নোমান',
      paraNumber: 8,
      surahName: 'সূরা আল-আনাম',
      pageNumber: 142,
      sabaqRating: 4,
      sabaqiRating: 4,
      manzilRating: 3,
      teacherRemarks: 'ছবকী আরও বারবার পড়তে হবে।',
      recordedDate: '2026-11-24',
      createdAt: DateTime.now().subtract(const Duration(hours: 2)).millisecondsSinceEpoch,
    ),
    HifzRecordModel(
      id: 'hifz-003',
      studentId: 'std-001',
      studentNameBn: 'মুহাম্মদ তালহা জুবায়ের',
      paraNumber: 15,
      surahName: 'সূরা বনী ইসরাঈল',
      pageNumber: 284,
      sabaqRating: 5,
      sabaqiRating: 5,
      manzilRating: 5,
      teacherRemarks: 'সম্পূর্ণ নির্ভুল পাঠ।',
      recordedDate: '2026-11-23',
      createdAt: DateTime.now().subtract(const Duration(days: 1)).millisecondsSinceEpoch,
    ),
  ];

  /// Fetch recent Hifz session logs
  Future<List<HifzRecordModel>> fetchRecentLogs({String? studentId}) async {
    try {
      var query = _client.from('hifz_progress').select();
      if (studentId != null && studentId.isNotEmpty) {
        query = query.eq('student_id', studentId);
      }

      final res = await query.order('created_at', ascending: false).limit(50);
      final List<dynamic> data = res as List<dynamic>;

      if (data.isEmpty) {
        if (studentId != null && studentId.isNotEmpty) {
          return _mockRecords.where((r) => r.studentId == studentId).toList();
        }
        return _mockRecords;
      }

      return data
          .map((item) => HifzRecordModel.fromJson(item as Map<String, dynamic>))
          .toList();
    } catch (e) {
      if (studentId != null && studentId.isNotEmpty) {
        return _mockRecords.where((r) => r.studentId == studentId).toList();
      }
      return _mockRecords;
    }
  }

  /// Log a new Hifz session
  Future<HifzRecordModel?> logSession(HifzRecordModel record) async {
    try {
      final payload = record.toJson();
      if (payload['id'] == null || (payload['id'] as String).isEmpty) {
        payload['id'] = 'hifz-${DateTime.now().millisecondsSinceEpoch}';
      }

      final res = await _client.from('hifz_progress').insert(payload).select().single();
      return HifzRecordModel.fromJson(res);
    } catch (e) {
      return record;
    }
  }
}
