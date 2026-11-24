import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../authentication/providers/auth_provider.dart';
import '../models/notice_model.dart';
import '../repositories/notice_repository.dart';

final noticeRepositoryProvider = Provider<NoticeRepository>((ref) {
  return NoticeRepository();
});

final noticePriorityFilterProvider = StateProvider<String>((ref) => 'all');
final noticePinnedOnlyFilterProvider = StateProvider<bool>((ref) => false);
final noticeSearchQueryProvider = StateProvider<String>((ref) => '');

class NoticeListNotifier extends StateNotifier<AsyncValue<List<NoticeModel>>> {
  final NoticeRepository _repository;
  final String _mosqueId;
  final Ref _ref;

  NoticeListNotifier(this._repository, this._mosqueId, this._ref)
      : super(const AsyncValue.loading()) {
    loadNotices();
  }

  Future<void> loadNotices() async {
    state = const AsyncValue.loading();
    try {
      final priority = _ref.read(noticePriorityFilterProvider);
      final pinnedOnly = _ref.read(noticePinnedOnlyFilterProvider);
      final search = _ref.read(noticeSearchQueryProvider).trim().toLowerCase();

      var list = await _repository.fetchNotices(
        mosqueId: _mosqueId,
        priorityFilter: priority,
        pinnedOnly: pinnedOnly,
      );

      if (search.isNotEmpty) {
        list = list.where((n) {
          return n.title.toLowerCase().contains(search) ||
              n.content.toLowerCase().contains(search) ||
              n.createdBy.toLowerCase().contains(search);
        }).toList();
      }

      state = AsyncValue.data(list);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  Future<bool> publishNotice(NoticeModel notice) async {
    final result = await _repository.publishNotice(
      mosqueId: _mosqueId,
      notice: notice,
    );
    if (result != null) {
      await loadNotices();
      return true;
    }
    return false;
  }

  Future<bool> togglePin(String noticeId, bool isPinned) async {
    final ok = await _repository.togglePin(noticeId, isPinned);
    if (ok) {
      await loadNotices();
      return true;
    }
    return false;
  }

  Future<bool> deleteNotice(String noticeId) async {
    final ok = await _repository.deleteNotice(noticeId);
    if (ok) {
      await loadNotices();
      return true;
    }
    return false;
  }
}

final noticeListProvider = StateNotifierProvider<NoticeListNotifier, AsyncValue<List<NoticeModel>>>((ref) {
  final repo = ref.watch(noticeRepositoryProvider);
  final authState = ref.watch(authProvider);
  final mosqueId = authState.user?.mosqueId ?? 'mosque-default';
  return NoticeListNotifier(repo, mosqueId, ref);
});
