import '../../../core/database/offline_cache_service.dart';
import '../models/waqf_asset_model.dart';

/// Repository for Waqf Properties (ওয়াকফ সম্পত্তি রিপোজিটরি)
class WaqfRepository {
  final OfflineCacheService _cache;

  WaqfRepository({OfflineCacheService? cache})
      : _cache = cache ?? OfflineCacheService.instance;

  /// Fetch all waqf properties for a mosque
  Future<List<WaqfAssetModel>> fetchProperties({
    required String mosqueId,
    String? categoryFilter,
  }) async {
    final equals = <String, dynamic>{};
    if (categoryFilter != null && categoryFilter.isNotEmpty && categoryFilter != 'সকল') {
      equals['category'] = categoryFilter;
    }

    final rows = await _cache.fetchList(
      tableName: 'mosque_properties',
      mosqueId: mosqueId,
      orderBy: 'created_at',
      ascending: false,
      equalsFilters: equals,
    );

    return rows.map(WaqfAssetModel.fromJson).toList();
  }

  /// Create a new waqf property
  Future<WaqfAssetModel?> createProperty({
    required String mosqueId,
    required WaqfAssetModel property,
  }) async {
    try {
      final payload = property.toJson();
      if ((payload['id']?.toString().trim() ?? '').isEmpty) {
        payload.remove('id');
      }
      final res = await _cache.insertRecord(
        tableName: 'mosque_properties',
        mosqueId: mosqueId,
        payload: payload,
      );
      return WaqfAssetModel.fromJson(res);
    } catch (_) {
      return null;
    }
  }

  /// Update an existing property
  Future<bool> updateProperty(WaqfAssetModel property, {String? mosqueId}) async {
    try {
      return await _cache.updateRecord(
        tableName: 'mosque_properties',
        id: property.id,
        mosqueId: mosqueId,
        updates: property.toJson(),
      );
    } catch (_) {
      return false;
    }
  }

  /// Delete a waqf property
  Future<bool> deleteProperty(String id) async {
    try {
      return await _cache.deleteRecord(tableName: 'mosque_properties', id: id);
    } catch (_) {
      return false;
    }
  }
}
