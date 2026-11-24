import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../../core/network/supabase_service.dart';
import '../models/student_model.dart';

/// Repository for Madrasa Students (মাদরাসা শিক্ষার্থী ও ভর্তি রিপোজিটরি)
class MadrasaRepository {
  final SupabaseService _supabaseService;

  MadrasaRepository({SupabaseService? supabaseService})
      : _supabaseService = supabaseService ?? SupabaseService();

  SupabaseClient get _client => _supabaseService.client;

  /// Default demo students for instant local preview and offline fallback
  static final List<StudentModel> _mockStudents = [
    StudentModel(
      id: 'std-001',
      studentCode: 'MS-N-01',
      nameBn: 'মুহাম্মদ তালহা জুবায়ের',
      nameEn: 'Muhammad Talha Zubair',
      className: 'হিফজুল কুরআন',
      guardianName: 'মাওলানা আবদুর রউফ',
      guardianMobile: '01712345671',
      monthlyFee: 1500.0,
      admissionDate: '2026-01-10',
      bloodGroup: 'A+',
      status: 'active',
      createdAt: DateTime.now().subtract(const Duration(days: 300)).millisecondsSinceEpoch,
    ),
    StudentModel(
      id: 'std-002',
      studentCode: 'MS-N-02',
      nameBn: 'আব্দুল্লাহ আল নোমান',
      nameEn: 'Abdullah Al Noman',
      className: 'হিফজুল কুরআন',
      guardianName: 'মো. রফিকুল ইসলাম',
      guardianMobile: '01819876543',
      monthlyFee: 1200.0,
      admissionDate: '2026-01-15',
      bloodGroup: 'B+',
      status: 'active',
      createdAt: DateTime.now().subtract(const Duration(days: 295)).millisecondsSinceEpoch,
    ),
    StudentModel(
      id: 'std-003',
      studentCode: 'MS-N-03',
      nameBn: 'আবু বকর সিদ্দীক',
      nameEn: 'Abu Bakr Siddique',
      className: 'নাজেরা বিভাগ',
      guardianName: 'হাফেজ মোজাম্মেল হক',
      guardianMobile: '01911223344',
      monthlyFee: 1000.0,
      admissionDate: '2026-02-01',
      bloodGroup: 'O+',
      status: 'active',
      createdAt: DateTime.now().subtract(const Duration(days: 280)).millisecondsSinceEpoch,
    ),
    StudentModel(
      id: 'std-004',
      studentCode: 'MS-N-04',
      nameBn: 'উসামা বিন যায়েদ',
      nameEn: 'Usama Bin Zayd',
      className: 'নূরানী বিভাগ',
      guardianName: 'মো. আনিসুর রহমান',
      guardianMobile: '01677889900',
      monthlyFee: 800.0,
      admissionDate: '2026-02-10',
      bloodGroup: 'AB+',
      status: 'active',
      createdAt: DateTime.now().subtract(const Duration(days: 270)).millisecondsSinceEpoch,
    ),
    StudentModel(
      id: 'std-005',
      studentCode: 'MS-N-05',
      nameBn: 'সালমান ফারসী',
      nameEn: 'Salman Farsi',
      className: 'কিতাব বিভাগ',
      guardianName: 'কারী দেলোয়ার হোসেন',
      guardianMobile: '01555667788',
      monthlyFee: 1500.0,
      admissionDate: '2026-01-05',
      bloodGroup: 'O+',
      status: 'active',
      createdAt: DateTime.now().subtract(const Duration(days: 305)).millisecondsSinceEpoch,
    ),
  ];

  /// Fetch students with optional class & status filters
  Future<List<StudentModel>> fetchStudents({
    String? className,
    String? status,
  }) async {
    try {
      var query = _client.from('madrasa_students').select();

      if (className != null && className.isNotEmpty && className != 'all') {
        query = query.eq('class_name', className);
      }
      if (status != null && status.isNotEmpty && status != 'all') {
        query = query.eq('status', status);
      }

      final response = await query.order('student_code', ascending: true);
      final List<dynamic> data = response as List<dynamic>;

      if (data.isEmpty) {
        return _mockStudents;
      }

      return data
          .map((item) => StudentModel.fromJson(item as Map<String, dynamic>))
          .toList();
    } catch (e) {
      // Graceful offline fallback
      if (className != null && className.isNotEmpty && className != 'all') {
        return _mockStudents.where((s) => s.className == className).toList();
      }
      return _mockStudents;
    }
  }

  /// Admit a new student
  Future<StudentModel?> admitStudent(StudentModel student) async {
    try {
      final payload = student.toJson();
      if (payload['id'] == null || (payload['id'] as String).isEmpty) {
        payload['id'] = 'std-${DateTime.now().millisecondsSinceEpoch}';
      }

      final res = await _client.from('madrasa_students').insert(payload).select().single();
      return StudentModel.fromJson(res);
    } catch (e) {
      // In offline mode, simulate successful admission
      return student;
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
