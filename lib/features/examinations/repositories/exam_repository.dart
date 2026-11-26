import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../core/network/supabase_service.dart';
import '../models/exam_model.dart';

/// Repository for Madrasa Exams & Results (মাদরাসা পরীক্ষা ও ফলাফল রিপোজিটরি)
class ExamRepository {
  SupabaseClient get _client => SupabaseService.client;

  /// Fetch all exams for a mosque
  Future<List<ExamModel>> fetchExams({required String mosqueId}) async {
    try {
      final res = await _client
          .from('examinations')
          .select()
          .eq('mosque_id', mosqueId)
          .order('start_date', ascending: false);
      final List<dynamic> data = res as List<dynamic>;

      return data
          .map((item) => ExamModel.fromJson(item as Map<String, dynamic>))
          .toList();
    } catch (e) {
      return [];
    }
  }

  /// Create a new exam
  Future<ExamModel?> createExam({
    required String mosqueId,
    required ExamModel exam,
  }) async {
    try {
      final payload = exam.toJson();
      payload['mosque_id'] = mosqueId;
      if (payload['id'] == null || (payload['id'] as String).isEmpty) {
        payload['id'] = 'ex-${DateTime.now().millisecondsSinceEpoch}';
      }

      final res = await _client.from('examinations').insert(payload).select().single();
      return ExamModel.fromJson(res);
    } catch (e) {
      return null;
    }
  }

  /// Fetch results for an exam or a student
  Future<List<ExamResultModel>> fetchResults({
    required String mosqueId,
    String? examId,
    String? studentId,
  }) async {
    try {
      var query = _client.from('exam_results').select().eq('mosque_id', mosqueId);
      if (examId != null && examId.isNotEmpty) {
        query = query.eq('exam_id', examId);
      }
      if (studentId != null && studentId.isNotEmpty) {
        query = query.eq('student_id', studentId);
      }

      final res = await query;
      final List<dynamic> data = res as List<dynamic>;

      return data
          .map((item) => ExamResultModel.fromJson(item as Map<String, dynamic>))
          .toList();
    } catch (e) {
      return [];
    }
  }

  /// Add student mark entry
  Future<ExamResultModel?> recordResult({
    required String mosqueId,
    required ExamResultModel result,
  }) async {
    try {
      final payload = result.toJson();
      payload['mosque_id'] = mosqueId;
      if (payload['id'] == null || (payload['id'] as String).isEmpty) {
        payload['id'] = 'res-${DateTime.now().millisecondsSinceEpoch}';
      }

      final res = await _client.from('exam_results').insert(payload).select().single();
      return ExamResultModel.fromJson(res);
    } catch (e) {
      return null;
    }
  }
}
