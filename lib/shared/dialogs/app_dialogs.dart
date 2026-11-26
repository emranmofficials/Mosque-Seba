import 'package:flutter/material.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_dimens.dart';
import '../../core/theme/app_typography.dart';

/// Reusable Confirmation and Warning Dialogs across the application
class AppDialogs {
  /// General confirmation dialog
  static Future<bool?> confirm(
    BuildContext context, {
    required String title,
    required String message,
    String confirmText = 'নিশ্চিত করুন',
    String cancelText = 'বাতিল',
    bool isDestructive = false,
    IconData? icon,
  }) {
    return showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppDimens.radiusLg),
        ),
        titlePadding: const EdgeInsets.fromLTRB(24, 20, 24, 10),
        contentPadding: const EdgeInsets.fromLTRB(24, 0, 24, 20),
        actionsPadding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
        title: Row(
          children: [
            if (icon != null || isDestructive) ...[
              Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: isDestructive
                      ? AppColors.urgentRed.withOpacity(0.12)
                      : AppColors.primaryContainer,
                  shape: BoxShape.circle,
                ),
                child: Icon(
                  icon ?? (isDestructive ? Icons.warning_amber_rounded : Icons.help_outline),
                  color: isDestructive ? AppColors.urgentRed : AppColors.primary,
                  size: 22,
                ),
              ),
              const SizedBox(width: 12),
            ],
            Expanded(
              child: Text(
                title,
                style: AppTypography.titleLarge.copyWith(fontWeight: FontWeight.bold),
              ),
            ),
          ],
        ),
        content: Text(
          message,
          style: AppTypography.bodyMedium.copyWith(color: AppColors.textSecondary),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(false),
            child: Text(
              cancelText,
              style: const TextStyle(color: AppColors.textSecondary),
            ),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: isDestructive ? AppColors.urgentRed : AppColors.emeraldPrimary,
              foregroundColor: Colors.white,
              elevation: 0,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(AppDimens.radiusMd),
              ),
            ),
            onPressed: () => Navigator.of(ctx).pop(true),
            child: Text(confirmText),
          ),
        ],
      ),
    );
  }

  /// Warning / Delete confirmation dialog
  static Future<bool?> confirmDelete(
    BuildContext context, {
    required String itemName,
    String? customMessage,
  }) {
    return confirm(
      context,
      title: 'মুছে ফেলার সতর্কতা',
      message: customMessage ??
          'আপনি কি নিশ্চিত যে আপনি "$itemName" মুছে ফেলতে চান? এই তথ্যটি পুনরায় ফিরিয়ে আনা যাবে না।',
      confirmText: 'মুছে ফেলুন',
      cancelText: 'বাতিল',
      isDestructive: true,
      icon: Icons.delete_forever_outlined,
    );
  }

  /// Alert warning dialog
  static Future<void> showWarning(
    BuildContext context, {
    required String title,
    required String message,
    String buttonText = 'ঠিক আছে',
  }) {
    return showDialog<void>(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppDimens.radiusLg),
        ),
        titlePadding: const EdgeInsets.fromLTRB(24, 20, 24, 10),
        contentPadding: const EdgeInsets.fromLTRB(24, 0, 24, 20),
        actionsPadding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
        title: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: Colors.amber.withOpacity(0.12),
                shape: BoxShape.circle,
              ),
              child: const Icon(Icons.warning_amber_rounded, color: Colors.amber, size: 24),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                title,
                style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold),
              ),
            ),
          ],
        ),
        content: Text(
          message,
          style: AppTypography.bodyMedium.copyWith(color: AppColors.textSecondary),
        ),
        actions: [
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.emeraldPrimary,
              foregroundColor: Colors.white,
              elevation: 0,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(AppDimens.radiusMd),
              ),
            ),
            onPressed: () => Navigator.of(ctx).pop(),
            child: Text(buttonText),
          ),
        ],
      ),
    );
  }

  /// Logout confirmation dialog
  static Future<bool?> confirmLogout(BuildContext context) {
    return confirm(
      context,
      title: 'লগআউট নিশ্চিতকরণ',
      message: 'আপনি কি নিশ্চিত যে আপনি অ্যাকাউন্ট থেকে লগআউট করতে চান?',
      confirmText: 'লগআউট',
      cancelText: 'থাকুন',
      isDestructive: true,
      icon: Icons.logout,
    );
  }
}
