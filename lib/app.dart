import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'bootstrap/dependency_injection.dart';
import 'core/config/app_config.dart';
import 'core/theme/app_theme.dart';

/// Root Application Widget for Mosque Seba (মসজিদ সেবা)
class MosqueSebaApp extends ConsumerWidget {
  const MosqueSebaApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isDark = ref.watch(isDarkModeProvider);
    final router = ref.watch(routerProvider);

    return MaterialApp.router(
      title: '${AppConfig.appNameBn} (${AppConfig.appNameEn})',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      darkTheme: AppTheme.darkTheme,
      themeMode: isDark ? ThemeMode.dark : ThemeMode.light,
      routerConfig: router,
    );
  }
}
