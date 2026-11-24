import 'package:flutter/material.dart';
import '../config/app_config.dart';
import 'app_colors.dart';

/// Islamic Typography Scale with Bengali Hind Siliguri Typeface
/// Preserves exact parity with Kotlin Type.kt
class AppTypography {
  static const String fontFamily = AppConfig.fontHindSiliguri;

  static const TextTheme lightTextTheme = TextTheme(
    headlineLarge: TextStyle(
      fontFamily: fontFamily,
      fontWeight: FontWeight.w800,
      fontSize: 28,
      height: 38 / 28,
      color: AppColors.textPrimary,
    ),
    headlineMedium: TextStyle(
      fontFamily: fontFamily,
      fontWeight: FontWeight.w700,
      fontSize: 22,
      height: 30 / 22,
      color: AppColors.textPrimary,
    ),
    headlineSmall: TextStyle(
      fontFamily: fontFamily,
      fontWeight: FontWeight.w700,
      fontSize: 18,
      height: 26 / 18,
      color: AppColors.textPrimary,
    ),
    titleLarge: TextStyle(
      fontFamily: fontFamily,
      fontWeight: FontWeight.w700,
      fontSize: 18,
      height: 26 / 18,
      color: AppColors.textPrimary,
    ),
    titleMedium: TextStyle(
      fontFamily: fontFamily,
      fontWeight: FontWeight.w600,
      fontSize: 16,
      height: 24 / 16,
      color: AppColors.textPrimary,
    ),
    titleSmall: TextStyle(
      fontFamily: fontFamily,
      fontWeight: FontWeight.w500,
      fontSize: 14,
      height: 22 / 14,
      color: AppColors.textSecondary,
    ),
    bodyLarge: TextStyle(
      fontFamily: fontFamily,
      fontWeight: FontWeight.w400,
      fontSize: 15,
      height: 24 / 15,
      color: AppColors.textPrimary,
    ),
    bodyMedium: TextStyle(
      fontFamily: fontFamily,
      fontWeight: FontWeight.w400,
      fontSize: 13,
      height: 20 / 13,
      color: AppColors.textSecondary,
    ),
    bodySmall: TextStyle(
      fontFamily: fontFamily,
      fontWeight: FontWeight.w400,
      fontSize: 12,
      height: 18 / 12,
      color: AppColors.textMuted,
    ),
    labelLarge: TextStyle(
      fontFamily: fontFamily,
      fontWeight: FontWeight.w600,
      fontSize: 14,
      height: 20 / 14,
      color: AppColors.textPrimary,
    ),
    labelMedium: TextStyle(
      fontFamily: fontFamily,
      fontWeight: FontWeight.w500,
      fontSize: 12,
      height: 18 / 12,
      color: AppColors.textSecondary,
    ),
    labelSmall: TextStyle(
      fontFamily: fontFamily,
      fontWeight: FontWeight.w500,
      fontSize: 11,
      height: 16 / 11,
      color: AppColors.textMuted,
    ),
  );

  static const TextTheme darkTextTheme = TextTheme(
    headlineLarge: TextStyle(
      fontFamily: fontFamily,
      fontWeight: FontWeight.w800,
      fontSize: 28,
      height: 38 / 28,
      color: AppColors.nightTextPrimary,
    ),
    headlineMedium: TextStyle(
      fontFamily: fontFamily,
      fontWeight: FontWeight.w700,
      fontSize: 22,
      height: 30 / 22,
      color: AppColors.nightTextPrimary,
    ),
    headlineSmall: TextStyle(
      fontFamily: fontFamily,
      fontWeight: FontWeight.w700,
      fontSize: 18,
      height: 26 / 18,
      color: AppColors.nightTextPrimary,
    ),
    titleLarge: TextStyle(
      fontFamily: fontFamily,
      fontWeight: FontWeight.w700,
      fontSize: 18,
      height: 26 / 18,
      color: AppColors.nightTextPrimary,
    ),
    titleMedium: TextStyle(
      fontFamily: fontFamily,
      fontWeight: FontWeight.w600,
      fontSize: 16,
      height: 24 / 16,
      color: AppColors.nightTextPrimary,
    ),
    titleSmall: TextStyle(
      fontFamily: fontFamily,
      fontWeight: FontWeight.w500,
      fontSize: 14,
      height: 22 / 14,
      color: AppColors.nightTextSecondary,
    ),
    bodyLarge: TextStyle(
      fontFamily: fontFamily,
      fontWeight: FontWeight.w400,
      fontSize: 15,
      height: 24 / 15,
      color: AppColors.nightTextPrimary,
    ),
    bodyMedium: TextStyle(
      fontFamily: fontFamily,
      fontWeight: FontWeight.w400,
      fontSize: 13,
      height: 20 / 13,
      color: AppColors.nightTextSecondary,
    ),
    bodySmall: TextStyle(
      fontFamily: fontFamily,
      fontWeight: FontWeight.w400,
      fontSize: 12,
      height: 18 / 12,
      color: AppColors.nightTextMuted,
    ),
    labelLarge: TextStyle(
      fontFamily: fontFamily,
      fontWeight: FontWeight.w600,
      fontSize: 14,
      height: 20 / 14,
      color: AppColors.nightTextPrimary,
    ),
    labelMedium: TextStyle(
      fontFamily: fontFamily,
      fontWeight: FontWeight.w500,
      fontSize: 12,
      height: 18 / 12,
      color: AppColors.nightTextSecondary,
    ),
    labelSmall: TextStyle(
      fontFamily: fontFamily,
      fontWeight: FontWeight.w500,
      fontSize: 11,
      height: 16 / 11,
      color: AppColors.nightTextMuted,
    ),
  );
}
