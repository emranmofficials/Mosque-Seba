import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../core/network/supabase_service.dart';
import '../models/student_model.dart';

/// Repository for Madrasa Students (মাদরাসা শিক্ষার্থী ও ভর্তি রিপোজিটরি)
class MadrasaRepository {
  SupabaseClient get _client => SupabaseService.client;

  /// Fetch students for a mosque with optional class & status filters
  Future<List<StudentModel>> fetchStudents({
    required String mosqueId,
    String? className,
    String? status,
  }) async {
    try {
      var query = _client.from('madrasa_students').select().eq('mosque_id', mosqueId);

      if (className != null && className.isNotEmpty && className != 'all') {
        query = query.eq('class_name', className);
      }
      if (status != null && status.isNotEmpty && status != 'all') {
        query = query.eq('status', status);
      }

      final response = await query.order('student_code', ascending: true);
      final List<dynamic> data = response as List<dynamic>;

      return data
          .map((item) => StudentModel.fromJson(item as Map<String, dynamic>))
          .toList();
    } catch (e) {
      return [];
    }
  }

  /// Admit a new student
  Future<StudentModel?> admitStudent({
    required String mosqueId,
    required StudentModel student,
  }) async {
    try {
      final payload = student.toJson();
      payload['mosque_id'] = mosqueId;
      if (payload['id'] == null || (payload['id'] as String).isEmpty) {
        payload['id'] = 'std-${DateTime.now().millisecondsSinceEpoch}';
      }

      final res = await _client.from('madrasa_students').insert(payload).select().single();
      return StudentModel.fromJson(res);
    } catch (e) {
      return null;
    }
  }

  /// Update an existing student record
  Future<bool> updateStudent(StudentModel student) async {
    try {
      await _client
          .from('madrasa_students')
          .update(student.toJson())
          .eq('id', student.id);
      return true;
    } catch (e) {
      return false;
    }
  }

  /// Remove student
  Future<bool> deleteStudent(String studentId) async {
    try {
      await _client.from('madrasa_students').delete().eq('id', studentId);
      return true;
    } catch (e) {
      return false;
    }
  }
}
