import 'package:flutter/foundation.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import '../config/app_config.dart';

/// Centralized Supabase Client Service
class SupabaseService {
  static bool _isInitialized = false;

  static bool get isInitialized => _isInitialized;

  static SupabaseClient get client {
    if (!_isInitialized) {
      throw StateError('SupabaseService has not been initialized. Call initialize() first.');
    }
    return Supabase.instance.client;
  }

  /// Initializes the Supabase client with production configuration
  static Future<void> initialize({String? customUrl, String? customAnonKey}) async {
    final url = (customUrl != null && customUrl.isNotEmpty)
        ? customUrl
        : AppConfig.defaultSupabaseUrl;
    final anonKey = (customAnonKey != null && customAnonKey.isNotEmpty)
        ? customAnonKey
        : AppConfig.defaultSupabaseAnonKey;

    try {
      await Supabase.initialize(
        url: url,
        publishableKey: anonKey,
        authOptions: const FlutterAuthClientOptions(
          authFlowType: AuthFlowType.pkce,
        ),
        realtimeClientOptions: const RealtimeClientOptions(
          eventsPerSecond: 10,
        ),
      );
      _isInitialized = true;
      debugPrint('Supabase initialized successfully for $url');
    } catch (e) {
      debugPrint('Supabase initialization warning (running in offline fallback): $e');
    }
  }

  /// Checks if an active authenticated user session exists
  static bool get hasActiveSession =>
      _isInitialized && client.auth.currentSession != null;

  /// Returns current user ID or null
  static String? get currentUserId =>
      _isInitialized ? client.auth.currentUser?.id : null;
}
