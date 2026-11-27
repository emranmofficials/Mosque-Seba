import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:go_router/go_router.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:supabase_flutter/supabase_flutter.dart' hide AuthUser;
import '../core/database/app_database.dart';
import '../core/network/supabase_service.dart';
import '../core/router/app_router.dart';
import '../core/storage/session_storage_service.dart';
import '../features/authentication/models/auth_user.dart';
import '../features/authentication/providers/auth_provider.dart';

// Foundation Providers (Overridden during Bootstrap)
final sharedPreferencesProvider = Provider<SharedPreferences>((ref) {
  throw UnimplementedError('sharedPreferencesProvider must be overridden in bootstrap');
});

final secureStorageProvider = Provider<FlutterSecureStorage>((ref) {
  return const FlutterSecureStorage();
});

final sessionStorageServiceProvider = Provider<SessionStorageService>((ref) {
  final prefs = ref.watch(sharedPreferencesProvider);
  final secure = ref.watch(secureStorageProvider);
  return SessionStorageService(prefs, secure);
});

final appDatabaseProvider = Provider<AppDatabase>((ref) {
  final db = AppDatabase();
  ref.onDispose(() => db.close());
  return db;
});

final supabaseClientProvider = Provider<SupabaseClient>((ref) {
  return SupabaseService.client;
});

final isDarkModeProvider = StateProvider<bool>((ref) {
  final storage = ref.watch(sessionStorageServiceProvider);
  return storage.isDarkMode;
});

final routerProvider = Provider<GoRouter>((ref) {
  final refreshNotifier = _RouterRefreshNotifier();
  ref.listen<AsyncValue<AuthUser?>>(
    authNotifierProvider,
    (_, __) => refreshNotifier.trigger(),
  );

  final router = AppRouter.createRouter(
    getAuthUser: () => ref.read(authNotifierProvider).valueOrNull,
    refreshListenable: refreshNotifier,
  );

  ref.onDispose(() {
    router.dispose();
    refreshNotifier.dispose();
  });

  return router;
});

class _RouterRefreshNotifier extends ChangeNotifier {
  void trigger() => notifyListeners();
}

