import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/exam_model.dart';
import '../repositories/exam_repository.dart';

final examRepositoryProvider = Provider<ExamRepository>((ref) {
  return ExamRepository();
});

class ExamListNotifier extends StateNotifier<AsyncValue<List<ExamModel>>> {
  final ExamRepository _repository;

  ExamListNotifier(this._repository) : super(const AsyncValue.loading()) {
    loadExams();
  }

  Future<void> loadExams() async {
    try {
      state = const AsyncValue.loading();
      final exams = await _repository.fetchExams();
      state = AsyncValue.data(exams);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  Future<bool> createExam(ExamModel exam) async {
    try {
      final created = await _repository.createExam(exam);
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
}

final examListProvider =
    StateNotifierProvider<ExamListNotifier, AsyncValue<List<ExamModel>>>((ref) {
  final repo = ref.watch(examRepositoryProvider);
  return ExamListNotifier(repo);
});

final selectedExamProvider = StateProvider<ExamModel?>((ref) => null);

final examResultsProvider =
    FutureProvider.family<List<ExamResultModel>, String>((ref, examId) async {
  final repo = ref.watch(examRepositoryProvider);
  return repo.fetchResults(examId: examId);
});

final studentReportCardProvider =
    FutureProvider.family<List<ExamResultModel>, String>((ref, studentId) async {
  final repo = ref.watch(examRepositoryProvider);
  return repo.fetchResults(studentId: studentId);
});
