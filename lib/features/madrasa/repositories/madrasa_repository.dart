import '../../../core/database/offline_cache_service.dart';
import '../models/student_model.dart';

/// Repository for Madrasa Students (মাদরাসা শিক্ষার্থী ও ভর্তি রিপোজিটরি)
class MadrasaRepository {
  final OfflineCacheService _cache;

  MadrasaRepository({OfflineCacheService? cache})
      : _cache = cache ?? OfflineCacheService.instance;

  /// Fetch students for a mosque with optional class & status filters
  Future<List<StudentModel>> fetchStudents({
    required String mosqueId,
    String? className,
    String? status,
  }) async {
    final equals = <String, dynamic>{};
    if (className != null && className.isNotEmpty && className != 'all') {
      equals['class_name'] = className;
    }
    if (status != null && status.isNotEmpty && status != 'all') {
      equals['status'] = status;
    }

    final rows = await _cache.fetchList(
      tableName: 'madrasa_students',
      mosqueId: mosqueId,
      orderBy: 'student_code',
      ascending: true,
      equalsFilters: equals,
    );

    final list = rows.map(StudentModel.fromJson).toList();
    list.sort((a, b) => a.studentCode.compareTo(b.studentCode));
    return list;
  }

  /// Admit a new student
  Future<StudentModel?> admitStudent({
    required String mosqueId,
    required StudentModel student,
  }) async {
    try {
      final payload = student.toJson();
      if ((payload['id']?.toString().trim() ?? '').isEmpty) {
        payload.remove('id');
      }
      if (payload['created_at'] == 0) payload.remove('created_at');
      final res = await _cache.insertRecord(
        tableName: 'madrasa_students',
        mosqueId: mosqueId,
        payload: payload,
      );
      return StudentModel.fromJson(res);
    } catch (_) {
      return null;
    }
  }

  /// Update an existing student record
  Future<bool> updateStudent(StudentModel student, {String? mosqueId}) async {
    try {
      return await _cache.updateRecord(
        tableName: 'madrasa_students',
        id: student.id,
        mosqueId: mosqueId,
        updates: student.toJson(),
      );
    } catch (_) {
      return false;
    }
  }

  /// Remove student
  Future<bool> deleteStudent(String studentId) async {
    try {
      return await _cache.deleteRecord(
        tableName: 'madrasa_students',
        id: studentId,
      );
    } catch (_) {
      return false;
    }
  }
}
