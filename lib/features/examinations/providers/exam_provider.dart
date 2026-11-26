import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../authentication/providers/auth_provider.dart';
import '../models/exam_model.dart';
import '../repositories/exam_repository.dart';

final examRepositoryProvider = Provider<ExamRepository>((ref) {
  return ExamRepository();
});

class ExamListNotifier extends StateNotifier<AsyncValue<List<ExamModel>>> {
  final ExamRepository _repository;
  final String _mosqueId;

  ExamListNotifier(this._repository, this._mosqueId)
      : super(const AsyncValue.loading()) {
    loadExams();
  }

  Future<void> loadExams() async {
    try {
      state = const AsyncValue.loading();
      final exams = await _repository.fetchExams(mosqueId: _mosqueId);
      if (!mounted) return;
      state = AsyncValue.data(exams);
    } catch (e, st) {
      if (!mounted) return;
      state = AsyncValue.error(e, st);
    }
  }

  Future<bool> createExam(ExamModel exam) async {
    try {
      final created = await _repository.createExam(
        mosqueId: _mosqueId,
        exam: exam,
      );
      if (!mounted) return created != null;
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
  final authState = ref.watch(authNotifierProvider);
  final mosqueId = authState.valueOrNull?.mosqueId ?? 'mosque-default';
  return ExamListNotifier(repo, mosqueId);
});

final selectedExamProvider = StateProvider<ExamModel?>((ref) => null);

final examResultsProvider =
    FutureProvider.family<List<ExamResultModel>, String>((ref, examId) async {
  final repo = ref.watch(examRepositoryProvider);
  final mosqueId = ref.watch(authNotifierProvider).valueOrNull?.mosqueId ?? 'mosque-default';
  return repo.fetchResults(mosqueId: mosqueId, examId: examId);
});

final studentReportCardProvider =
    FutureProvider.family<List<ExamResultModel>, String>((ref, studentId) async {
  final repo = ref.watch(examRepositoryProvider);
  final mosqueId = ref.watch(authNotifierProvider).valueOrNull?.mosqueId ?? 'mosque-default';
  return repo.fetchResults(mosqueId: mosqueId, studentId: studentId);
});
