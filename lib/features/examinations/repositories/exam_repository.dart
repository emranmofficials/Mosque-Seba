import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../../core/network/supabase_service.dart';
import '../models/exam_model.dart';

/// Repository for Madrasa Exams & Results (মাদরাসা পরীক্ষা ও ফলাফল রিপোজিটরি)
class ExamRepository {
  final SupabaseService _supabaseService;

  ExamRepository({SupabaseService? supabaseService})
      : _supabaseService = supabaseService ?? SupabaseService();

  SupabaseClient get _client => _supabaseService.client;

  static final List<ExamModel> _mockExams = [
    ExamModel(
      id: 'ex-001',
      examNameBn: 'বার্ষিক মূল্যায়ন পরীক্ষা ২০২৬',
      examType: 'annual',
      academicYear: '২০২৬',
      startDate: '2026-11-15',
      status: 'published',
      createdAt: DateTime.now().millisecondsSinceEpoch,
    ),
    ExamModel(
      id: 'ex-002',
      examNameBn: 'অর্ধবার্ষিক মূল্যায়ন পরীক্ষা ২০২৬',
      examType: 'half_yearly',
      academicYear: '২০২৬',
      startDate: '2026-06-10',
      status: 'completed',
      createdAt: DateTime.now().subtract(const Duration(days: 150)).millisecondsSinceEpoch,
    ),
  ];

  static final List<ExamResultModel> _mockResults = [
    ExamResultModel(
      id: 'res-001',
      examId: 'ex-001',
      studentId: 'std-001',
      studentNameBn: 'মুহাম্মদ তালহা জুবায়ের',
      className: 'হিফজুল কুরআন',
      subjectName: 'হিফজুল কুরআন ও তারতীল',
      marksObtained: 96.0,
      totalMarks: 100.0,
      grade: 'মুমতাজ (A+)',
      remarks: 'অনবদ্য হিফজ ও মাখরাজ।',
      createdAt: DateTime.now().millisecondsSinceEpoch,
    ),
    ExamResultModel(
      id: 'res-002',
      examId: 'ex-001',
      studentId: 'std-001',
      studentNameBn: 'মুহাম্মদ তালহা জুবায়ের',
      className: 'হিফজুল কুরআন',
      subjectName: 'তাজবীদ ও কাওয়ায়েদ',
      marksObtained: 92.0,
      totalMarks: 100.0,
      grade: 'মুমতাজ (A+)',
      remarks: 'প্রশংসনীয় ফলাফল।',
      createdAt: DateTime.now().millisecondsSinceEpoch,
    ),
    ExamResultModel(
      id: 'res-003',
      examId: 'ex-001',
      studentId: 'std-001',
      studentNameBn: 'মুহাম্মদ তালহা জুবায়ের',
      className: 'হিফজুল কুরআন',
      subjectName: 'ইসলামিয়াত ও আখলাক',
      marksObtained: 90.0,
      totalMarks: 100.0,
      grade: 'মুমতাজ (A+)',
      remarks: 'উত্তম ব্যবহার।',
      createdAt: DateTime.now().millisecondsSinceEpoch,
    ),
    ExamResultModel(
      id: 'res-004',
      examId: 'ex-001',
      studentId: 'std-002',
      studentNameBn: 'আব্দুল্লাহ আল নোমান',
      className: 'হিফজুল কুরআন',
      subjectName: 'হিফজুল কুরআন ও তারতীল',
      marksObtained: 85.0,
      totalMarks: 100.0,
      grade: 'মুমতাজ (A+)',
      remarks: 'ভালো উন্নতি হয়েছে।',
      createdAt: DateTime.now().millisecondsSinceEpoch,
    ),
  ];

  /// Fetch all exams
  Future<List<ExamModel>> fetchExams() async {
    try {
      final res = await _client.from('examinations').select().order('start_date', ascending: false);
      final List<dynamic> data = res as List<dynamic>;

      if (data.isEmpty) return _mockExams;
      return data.map((item) => ExamModel.fromJson(item as Map<String, dynamic>)).toList();
    } catch (e) {
      return _mockExams;
    }
  }

  /// Create a new exam
  Future<ExamModel?> createExam(ExamModel exam) async {
    try {
      final payload = exam.toJson();
      if (payload['id'] == null || (payload['id'] as String).isEmpty) {
        payload['id'] = 'ex-${DateTime.now().millisecondsSinceEpoch}';
      }

      final res = await _client.from('examinations').insert(payload).select().single();
      return ExamModel.fromJson(res);
    } catch (e) {
      return exam;
    }
  }

  /// Fetch results for an exam or a student
  Future<List<ExamResultModel>> fetchResults({
    String? examId,
    String? studentId,
  }) async {
    try {
      var query = _client.from('exam_results').select();
      if (examId != null && examId.isNotEmpty) {
        query = query.eq('exam_id', examId);
      }
      if (studentId != null && studentId.isNotEmpty) {
        query = query.eq('student_id', studentId);
      }

      final res = await query;
      final List<dynamic> data = res as List<dynamic>;

      if (data.isEmpty) {
        if (studentId != null && studentId.isNotEmpty) {
          return _mockResults.where((r) => r.studentId == studentId).toList();
        }
        return _mockResults;
      }

      return data.map((item) => ExamResultModel.fromJson(item as Map<String, dynamic>)).toList();
    } catch (e) {
      if (studentId != null && studentId.isNotEmpty) {
        return _mockResults.where((r) => r.studentId == studentId).toList();
      }
      return _mockResults;
    }
  }

  /// Add student mark entry
  Future<ExamResultModel?> recordResult(ExamResultModel result) async {
    try {
      final payload = result.toJson();
      if (payload['id'] == null || (payload['id'] as String).isEmpty) {
        payload['id'] = 'res-${DateTime.now().millisecondsSinceEpoch}';
      }

      final res = await _client.from('exam_results').insert(payload).select().single();
      return ExamResultModel.fromJson(res);
    } catch (e) {
      return result;
    }
  }
}
