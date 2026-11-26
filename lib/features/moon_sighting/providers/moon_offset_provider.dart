import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../bootstrap/dependency_injection.dart';

class MoonOffsetNotifier extends StateNotifier<int> {
  final Ref _ref;

  MoonOffsetNotifier(this._ref) : super(0) {
    _loadInitial();
  }

  void _loadInitial() {
    final storage = _ref.read(sessionStorageServiceProvider);
    state = storage.hijriDayOffset;
  }

  Future<void> setOffset(int offset) async {
    if (offset < -1 || offset > 1) return;
    final storage = _ref.read(sessionStorageServiceProvider);
    await storage.setHijriOffset(offset);
    if (!mounted) return;
    state = offset;
  }
}

final moonOffsetProvider = StateNotifierProvider<MoonOffsetNotifier, int>((ref) {
  return MoonOffsetNotifier(ref);
});
