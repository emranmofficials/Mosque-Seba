import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../core/database/offline_cache_service.dart';
import '../core/network/supabase_service.dart';
import '../core/services/notification_service.dart';
import '../core/theme/app_colors.dart';
import 'dependency_injection.dart';

/// Application Bootstrap Runner
/// Initializes system services, native overlays, cloud backend and local persistence
class AppBootstrap {
  static Future<ProviderContainer> initialize() async {
    WidgetsFlutterBinding.ensureInitialized();

    // Configure System UI Status & Navigation Bar Styling
    SystemChrome.setSystemUIOverlayStyle(
      const SystemUiOverlayStyle(
        statusBarColor: AppColors.emeraldDark,
        statusBarIconBrightness: Brightness.light,
        statusBarBrightness: Brightness.dark,
        systemNavigationBarColor: AppColors.surface,
        systemNavigationBarIconBrightness: Brightness.dark,
      ),
    );

    // Initialize SharedPreferences
    final sharedPreferences = await SharedPreferences.getInstance();

    // Initialize Supabase Client
    try {
      await SupabaseService.initialize();
    } catch (e) {
      debugPrint('Bootstrap Supabase initialization note: $e');
    }

    // Create Riverpod Container with Overridden Singletons
    final container = ProviderContainer(
      overrides: [
        sharedPreferencesProvider.overrideWithValue(sharedPreferences),
      ],
    );

    // Attach Drift SQLite DB to shared OfflineCacheService & init notifications
    final db = container.read(appDatabaseProvider);
    OfflineCacheService.instance.attachDatabase(db);

    await NotificationService.instance.initialize(
      onTapPayload: (routePayload) {
        if (routePayload != null && routePayload.startsWith('/')) {
          container.read(routerProvider).push(routePayload);
        }
      },
    );

    return container;
  }
}
