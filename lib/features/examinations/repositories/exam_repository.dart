import '../../../core/database/offline_cache_service.dart';
import '../models/exam_model.dart';

/// Repository for Madrasa Exams & Results (মাদরাসা পরীক্ষা ও ফলাফল রিপোজিটরি)
class ExamRepository {
  final OfflineCacheService _cache;

  ExamRepository({OfflineCacheService? cache})
      : _cache = cache ?? OfflineCacheService.instance;

  /// Fetch all exams for a mosque
  Future<List<ExamModel>> fetchExams({required String mosqueId}) async {
    final rows = await _cache.fetchList(
      tableName: 'examinations',
      mosqueId: mosqueId,
      orderBy: 'start_date',
      ascending: false,
    );

    return rows.map(ExamModel.fromJson).toList();
  }

  /// Create a new exam
  Future<ExamModel?> createExam({
    required String mosqueId,
    required ExamModel exam,
  }) async {
    try {
      final payload = exam.toJson();
      if ((payload['id']?.toString().trim() ?? '').isEmpty) {
        payload.remove('id');
      }
      if (payload['created_at'] == 0) payload.remove('created_at');

      final res = await _cache.insertRecord(
        tableName: 'examinations',
        mosqueId: mosqueId,
        payload: payload,
      );
      return ExamModel.fromJson(res);
    } catch (_) {
      return null;
    }
  }

  /// Fetch results for an exam or a student
  Future<List<ExamResultModel>> fetchResults({
    required String mosqueId,
    String? examId,
    String? studentId,
  }) async {
    final equals = <String, dynamic>{};
    if (examId != null && examId.isNotEmpty) {
      equals['exam_id'] = examId;
    }
    if (studentId != null && studentId.isNotEmpty) {
      equals['student_id'] = studentId;
    }

    final rows = await _cache.fetchList(
      tableName: 'exam_results',
      mosqueId: mosqueId,
      equalsFilters: equals,
    );

    return rows.map(ExamResultModel.fromJson).toList();
  }

  /// Add student mark entry
  Future<ExamResultModel?> recordResult({
    required String mosqueId,
    required ExamResultModel result,
  }) async {
    try {
      final payload = result.toJson();
      if ((payload['id']?.toString().trim() ?? '').isEmpty) {
        payload.remove('id');
      }
      if (payload['created_at'] == 0) payload.remove('created_at');

      final res = await _cache.insertRecord(
        tableName: 'exam_results',
        mosqueId: mosqueId,
        payload: payload,
      );
      return ExamResultModel.fromJson(res);
    } catch (_) {
      return null;
    }
  }
}
