import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../authentication/providers/auth_provider.dart';
import '../models/waqf_asset_model.dart';
import '../repositories/waqf_repository.dart';

final waqfRepositoryProvider = Provider<WaqfRepository>((ref) {
  return WaqfRepository();
});

final waqfCategoryFilterProvider = StateProvider<String>((ref) => 'সকল');

class WaqfListNotifier extends StateNotifier<AsyncValue<List<WaqfAssetModel>>> {
  final WaqfRepository _repository;
  final String _mosqueId;
  final Ref _ref;

  WaqfListNotifier(this._repository, this._mosqueId, this._ref)
      : super(const AsyncValue.loading()) {
    loadProperties();
  }

  Future<void> loadProperties() async {
    state = const AsyncValue.loading();
    try {
      final category = _ref.read(waqfCategoryFilterProvider);
      final list = await _repository.fetchProperties(
        mosqueId: _mosqueId,
        categoryFilter: category,
      );
      if (!mounted) return;
      state = AsyncValue.data(list);
    } catch (e, st) {
      if (!mounted) return;
      state = AsyncValue.error(e, st);
    }
  }

  Future<bool> addProperty(WaqfAssetModel property) async {
    final created = await _repository.createProperty(
      mosqueId: _mosqueId,
      property: property,
    );
    if (created != null) {
      await loadProperties();
      return true;
    }
    return false;
  }

  Future<bool> updateProperty(WaqfAssetModel property) async {
    final ok = await _repository.updateProperty(property, mosqueId: _mosqueId);
    if (ok) {
      await loadProperties();
      return true;
    }
    return false;
  }

  Future<bool> deleteProperty(String propertyId) async {
    final ok = await _repository.deleteProperty(propertyId);
    if (ok) {
      await loadProperties();
      return true;
    }
    return false;
  }
}

final waqfListProvider = StateNotifierProvider<WaqfListNotifier, AsyncValue<List<WaqfAssetModel>>>((ref) {
  final repo = ref.watch(waqfRepositoryProvider);
  final authState = ref.watch(authNotifierProvider);
  final mosqueId = authState.valueOrNull?.mosqueId ?? 'mosque-default';
  return WaqfListNotifier(repo, mosqueId, ref);
});
