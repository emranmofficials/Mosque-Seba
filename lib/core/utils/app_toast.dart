import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_dimens.dart';

/// Notification Toast Types
enum ToastType { success, error, warning, info }

/// Global App Toast Notification Utility
/// Displays floating, beautifully animated, brand-aligned toasts across the app
class AppToast {
  static void show(
    BuildContext context, {
    required String message,
    ToastType type = ToastType.info,
    Duration duration = const Duration(seconds: 3),
    SnackBarAction? action,
  }) {
    Color bgColor;
    const Color textColor = Colors.white;
    IconData icon;

    switch (type) {
      case ToastType.success:
        bgColor = AppColors.emeraldPrimary;
        icon = Icons.check_circle_outline;
        break;
      case ToastType.error:
        bgColor = AppColors.urgentRed;
        icon = Icons.error_outline;
        break;
      case ToastType.warning:
        bgColor = const Color(0xFFD97706); // Amber 600
        icon = Icons.warning_amber_rounded;
        break;
      case ToastType.info:
        bgColor = AppColors.infoBlue;
        icon = Icons.info_outline;
        break;
    }

    final messenger = ScaffoldMessenger.of(context);
    messenger.hideCurrentSnackBar();
    messenger.showSnackBar(
      SnackBar(
        content: Row(
          children: [
            Icon(icon, color: textColor, size: 20),
            const SizedBox(width: MosqueSpace.sm),
            Expanded(
              child: Text(
                message,
                style: const TextStyle(
                  color: textColor,
                  fontSize: 13,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ),
          ],
        ),
        backgroundColor: bgColor,
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(MosqueRadius.medium),
        ),
        margin: const EdgeInsets.symmetric(horizontal: MosqueSpace.md, vertical: MosqueSpace.sm),
        duration: duration,
        action: action,
      ),
    );
  }

  /// Show Success Toast (Green)
  static void success(BuildContext context, String message) =>
      show(context, message: message, type: ToastType.success);

  /// Show Error Toast (Red)
  static void error(BuildContext context, String message) =>
      show(context, message: message, type: ToastType.error);

  /// Show Warning Toast (Amber)
  static void warning(BuildContext context, String message) =>
      show(context, message: message, type: ToastType.warning);

  /// Show Info Toast (Emerald/Blue)
  static void info(BuildContext context, String message) =>
      show(context, message: message, type: ToastType.info);
}
