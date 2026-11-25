import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../../core/network/supabase_service.dart';
import '../models/waqf_asset_model.dart';

/// Repository for Waqf Properties (ওয়াকফ সম্পত্তি রিপোজিটরি)
class WaqfRepository {
  final SupabaseService _supabaseService;

  WaqfRepository({SupabaseService? supabaseService})
      : _supabaseService = supabaseService ?? SupabaseService();

  SupabaseClient get _client => SupabaseService.client;

  /// Fetch all waqf properties for a mosque
  Future<List<WaqfAssetModel>> fetchProperties({
    required String mosqueId,
    String? categoryFilter,
  }) async {
    try {
      var query = _client.from('mosque_properties').select();

      if (categoryFilter != null && categoryFilter.isNotEmpty && categoryFilter != 'সকল') {
        query = query.eq('category', categoryFilter);
      }

      final response = await query.order('created_at', ascending: false);
      final List<dynamic> data = response as List<dynamic>;

      return data
          .map((item) => WaqfAssetModel.fromJson(item as Map<String, dynamic>))
          .toList();
    } catch (e) {
      return [];
    }
  }

  /// Create a new waqf property
  Future<WaqfAssetModel?> createProperty(WaqfAssetModel property) async {
    try {
      final payload = property.toJson();
      if (payload['id'] == null || (payload['id'] as String).isEmpty) {
        payload['id'] = 'prop-${DateTime.now().millisecondsSinceEpoch}';
      }

      final res = await _client
          .from('mosque_properties')
          .insert(payload)
          .select()
          .single();
      return WaqfAssetModel.fromJson(res);
    } catch (e) {
      return null;
    }
  }

  /// Update an existing property
  Future<bool> updateProperty(WaqfAssetModel property) async {
    try {
      await _client
          .from('mosque_properties')
          .update(property.toJson())
          .eq('id', property.id);
      return true;
    } catch (e) {
      return false;
    }
  }
}
