import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../madrasa/models/student_model.dart';
import '../../madrasa/providers/madrasa_provider.dart';
import '../models/madrasa_attendance_model.dart';
import '../repositories/madrasa_attendance_repository.dart';

final madrasaAttendanceRepositoryProvider = Provider<MadrasaAttendanceRepository>((ref) {
  return MadrasaAttendanceRepository();
});

final selectedAttendanceDateProvider = StateProvider<DateTime>((ref) => DateTime.now());
final attendanceClassFilterProvider = StateProvider<String>((ref) => 'all');

class AttendanceState {
  final bool isLoading;
  final String date;
  final List<MadrasaAttendanceModel> records;
  final String? errorMessage;

  const AttendanceState({
    this.isLoading = false,
    required this.date,
    this.records = const [],
    this.errorMessage,
  });

  int get totalCount => records.length;
  int get presentCount => records.where((r) => r.status == 'present').length;
  int get absentCount => records.where((r) => r.status == 'absent').length;
  int get lateCount => records.where((r) => r.status == 'late').length;
  int get leaveCount => records.where((r) => r.status == 'leave').length;

  double get attendancePercentage =>
      totalCount > 0 ? (presentCount + lateCount) / totalCount * 100 : 0.0;
}

class AttendanceNotifier extends StateNotifier<AttendanceState> {
  final MadrasaAttendanceRepository _repository;
  final Ref _ref;

  AttendanceNotifier(this._repository, this._ref)
      : super(AttendanceState(date: _formatDate(DateTime.now()))) {
    loadAttendance();
  }

  static String _formatDate(DateTime dt) {
    return '${dt.year}-${dt.month.toString().padLeft(2, '0')}-${dt.day.toString().padLeft(2, '0')}';
  }

  Future<void> loadAttendance() async {
    final dt = _ref.read(selectedAttendanceDateProvider);
    final dateStr = _formatDate(dt);
    final classFilter = _ref.read(attendanceClassFilterProvider);

    state = AttendanceState(isLoading: true, date: dateStr);

    try {
      final existingRecords = await _repository.fetchAttendance(
        date: dateStr,
        className: classFilter,
      );

      // If no attendance exists for this date, build default from students list
      final studentsAsync = _ref.read(studentListProvider);
      final students = studentsAsync.valueOrNull ?? [];

      final filteredStudents = classFilter == 'all'
          ? students
          : students.where((s) => s.className == classFilter).toList();

      final Map<String, MadrasaAttendanceModel> existingMap = {
        for (var r in existingRecords) r.studentId: r
      };

      final List<MadrasaAttendanceModel> merged = filteredStudents.map((student) {
        if (existingMap.containsKey(student.id)) {
          return existingMap[student.id]!;
        }
        return MadrasaAttendanceModel(
          id: 'att-${student.id}-$dateStr',
          studentId: student.id,
          studentNameBn: student.nameBn,
          className: student.className,
          attendanceDate: dateStr,
          status: 'present',
          recordedBy: 'শিক্ষক',
          createdAt: DateTime.now().millisecondsSinceEpoch,
        );
      }).toList();

      state = AttendanceState(
        isLoading: false,
        date: dateStr,
        records: merged,
      );
    } catch (e) {
      state = AttendanceState(
        isLoading: false,
        date: dateStr,
        errorMessage: e.toString(),
      );
    }
  }

  void updateStatus(String studentId, String newStatus) {
    final updated = state.records.map((r) {
      if (r.studentId == studentId) {
        return r.copyWith(status: newStatus);
      }
      return r;
    }).toList();

    state = AttendanceState(
      isLoading: false,
      date: state.date,
      records: updated,
    );
  }

  void markAllPresent() {
    final updated = state.records.map((r) => r.copyWith(status: 'present')).toList();
    state = AttendanceState(
      isLoading: false,
      date: state.date,
      records: updated,
    );
  }

  Future<bool> saveAttendance() async {
    state = AttendanceState(
      isLoading: true,
      date: state.date,
      records: state.records,
    );

    final success = await _repository.saveAttendanceBatch(state.records);

    state = AttendanceState(
      isLoading: false,
      date: state.date,
      records: state.records,
    );
    return success;
  }
}

final attendanceNotifierProvider =
    StateNotifierProvider<AttendanceNotifier, AttendanceState>((ref) {
  final repo = ref.watch(madrasaAttendanceRepositoryProvider);
  return AttendanceNotifier(repo, ref);
});
