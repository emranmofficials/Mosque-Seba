import 'package:flutter/material.dart';
import 'app_colors.dart';
import 'app_dimens.dart';
import 'app_typography.dart';

/// Islamic Modern Theme System for Mosque Seba
/// Provides Light & Eye-Comfort Night Theme
class AppTheme {
  static ThemeData get lightTheme {
    final colorScheme = ColorScheme(
      brightness: Brightness.light,
      primary: AppColors.emeraldPrimary,
      onPrimary: AppColors.surface,
      primaryContainer: AppColors.emeraldLight,
      onPrimaryContainer: AppColors.surface,
      secondary: AppColors.gold,
      onSecondary: AppColors.textPrimary,
      secondaryContainer: AppColors.goldLight,
      onSecondaryContainer: AppColors.textPrimary,
      tertiary: AppColors.greenAccent,
      onTertiary: AppColors.surface,
      error: AppColors.urgentRed,
      onError: AppColors.surface,
      surface: AppColors.surface,
      onSurface: AppColors.textPrimary,
      surfaceContainerHighest: AppColors.cardBg,
      onSurfaceVariant: AppColors.textSecondary,
      outline: AppColors.cardBorder,
    );

    return ThemeData(
      useMaterial3: true,
      colorScheme: colorScheme,
      scaffoldBackgroundColor: AppColors.bgLight,
      textTheme: AppTypography.lightTextTheme,
      fontFamily: AppTypography.fontFamily,
      appBarTheme: const AppBarTheme(
        backgroundColor: AppColors.emeraldDark,
        foregroundColor: AppColors.surface,
        elevation: MosqueElevation.none,
        centerTitle: false,
      ),
      cardTheme: CardTheme(
        color: AppColors.surface,
        elevation: MosqueElevation.level1,
        shape: RoundedCornerShape(MosqueRadius.card),
        margin: EdgeInsets.zero,
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.emeraldPrimary,
          foregroundColor: AppColors.surface,
          elevation: MosqueElevation.raised,
          minimumSize: const Size(double.infinity, MosqueTouchTarget.minimum),
          shape: RoundedCornerShape(MosqueRadius.medium),
          textStyle: AppTypography.lightTextTheme.labelLarge,
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: AppColors.surface,
        contentPadding: const EdgeInsets.symmetric(
          horizontal: MosqueSpace.lg,
          vertical: MosqueSpace.md,
        ),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(MosqueRadius.medium),
          borderSide: const BorderSide(color: AppColors.cardBorder),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(MosqueRadius.medium),
          borderSide: const BorderSide(color: AppColors.cardBorder),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(MosqueRadius.medium),
          borderSide: const BorderSide(color: AppColors.emeraldPrimary, width: 2),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(MosqueRadius.medium),
          borderSide: const BorderSide(color: AppColors.urgentRed),
        ),
      ),
    );
  }

  static ThemeData get darkTheme {
    final colorScheme = ColorScheme(
      brightness: Brightness.dark,
      primary: AppColors.greenAccent,
      onPrimary: AppColors.emeraldDark,
      primaryContainer: AppColors.emeraldPrimary,
      onPrimaryContainer: AppColors.nightTextPrimary,
      secondary: AppColors.gold,
      onSecondary: AppColors.emeraldDark,
      secondaryContainer: AppColors.nightSecondaryContainer,
      onSecondaryContainer: AppColors.goldLight,
      tertiary: AppColors.greenAccent,
      onTertiary: AppColors.emeraldDark,
      error: AppColors.urgentRed,
      onError: AppColors.nightTextPrimary,
      surface: AppColors.nightSurface,
      onSurface: AppColors.nightTextPrimary,
      surfaceContainerHighest: AppColors.nightSurfaceVariant,
      onSurfaceVariant: AppColors.nightTextSecondary,
      outline: AppColors.nightBorder,
    );

    return ThemeData(
      useMaterial3: true,
      colorScheme: colorScheme,
      scaffoldBackgroundColor: AppColors.nightBg,
      textTheme: AppTypography.darkTextTheme,
      fontFamily: AppTypography.fontFamily,
      appBarTheme: const AppBarTheme(
        backgroundColor: AppColors.nightSurface,
        foregroundColor: AppColors.nightTextPrimary,
        elevation: MosqueElevation.none,
        centerTitle: false,
      ),
      cardTheme: CardTheme(
        color: AppColors.nightSurface,
        elevation: MosqueElevation.level1,
        shape: RoundedCornerShape(MosqueRadius.card),
        margin: EdgeInsets.zero,
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.emeraldPrimary,
          foregroundColor: AppColors.nightTextPrimary,
          elevation: MosqueElevation.raised,
          minimumSize: const Size(double.infinity, MosqueTouchTarget.minimum),
          shape: RoundedCornerShape(MosqueRadius.medium),
          textStyle: AppTypography.darkTextTheme.labelLarge,
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: AppColors.nightSurfaceVariant,
        contentPadding: const EdgeInsets.symmetric(
          horizontal: MosqueSpace.lg,
          vertical: MosqueSpace.md,
        ),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(MosqueRadius.medium),
          borderSide: const BorderSide(color: AppColors.nightBorder),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(MosqueRadius.medium),
          borderSide: const BorderSide(color: AppColors.nightBorder),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(MosqueRadius.medium),
          borderSide: const BorderSide(color: AppColors.greenAccent, width: 2),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(MosqueRadius.medium),
          borderSide: const BorderSide(color: AppColors.urgentRed),
        ),
      ),
    );
  }
}

class RoundedCornerShape extends RoundedRectangleBorder {
  RoundedCornerShape(double radius) : super(borderRadius: BorderRadius.circular(radius));
}
