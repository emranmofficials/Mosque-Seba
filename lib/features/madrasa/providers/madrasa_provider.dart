import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/student_model.dart';
import '../repositories/madrasa_repository.dart';

final madrasaRepositoryProvider = Provider<MadrasaRepository>((ref) {
  return MadrasaRepository();
});

final studentClassFilterProvider = StateProvider<String>((ref) => 'all');
final studentSearchQueryProvider = StateProvider<String>((ref) => '');

class StudentListNotifier extends StateNotifier<AsyncValue<List<StudentModel>>> {
  final MadrasaRepository _repository;

  StudentListNotifier(this._repository) : super(const AsyncValue.loading()) {
    loadStudents();
  }

  Future<void> loadStudents() async {
    try {
      state = const AsyncValue.loading();
      final students = await _repository.fetchStudents();
      state = AsyncValue.data(students);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  Future<bool> admitStudent(StudentModel student) async {
    try {
      final created = await _repository.admitStudent(student);
      if (created != null) {
        final current = state.valueOrNull ?? [];
        state = AsyncValue.data([created, ...current]);
        return true;
      }
      return false;
    } catch (e) {
      return false;
    }
  }

  Future<bool> updateStudent(StudentModel student) async {
    try {
      final success = await _repository.updateStudent(student);
      if (success) {
        final current = state.valueOrNull ?? [];
        state = AsyncValue.data(
          current.map((s) => s.id == student.id ? student : s).toList(),
        );
        return true;
      }
      return false;
    } catch (e) {
      return false;
    }
  }
}

final studentListProvider =
    StateNotifierProvider<StudentListNotifier, AsyncValue<List<StudentModel>>>((ref) {
  final repo = ref.watch(madrasaRepositoryProvider);
  return StudentListNotifier(repo);
});

final filteredStudentsProvider = Provider<List<StudentModel>>((ref) {
  final studentsAsync = ref.watch(studentListProvider);
  final classFilter = ref.watch(studentClassFilterProvider);
  final searchQuery = ref.watch(studentSearchQueryProvider).trim().toLowerCase();

  final students = studentsAsync.valueOrNull ?? [];

  return students.where((student) {
    final matchesClass = classFilter == 'all' || student.className == classFilter;
    final matchesSearch = searchQuery.isEmpty ||
        student.nameBn.toLowerCase().contains(searchQuery) ||
        student.nameEn.toLowerCase().contains(searchQuery) ||
        student.studentCode.toLowerCase().contains(searchQuery) ||
        student.guardianName.toLowerCase().contains(searchQuery) ||
        student.guardianMobile.contains(searchQuery);

    return matchesClass && matchesSearch;
  }).toList();
});
