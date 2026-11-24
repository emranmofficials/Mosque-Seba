import 'package:flutter/material.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_dimens.dart';

enum AppButtonVariant { primary, secondary, outline, text }

/// Reusable Islamic Themed Button Component
class AppButton extends StatelessWidget {
  final String label;
  final VoidCallback? onPressed;
  final AppButtonVariant variant;
  final IconData? icon;
  final bool isLoading;
  final double? width;
  final double height;

  const AppButton({
    super.key,
    required this.label,
    required this.onPressed,
    this.variant = AppButtonVariant.primary,
    this.icon,
    this.isLoading = false,
    this.width,
    this.height = MosqueTouchTarget.minimum,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    Widget buttonContent = isLoading
        ? SizedBox(
            height: 20,
            width: 20,
            child: CircularProgressIndicator(
              strokeWidth: 2,
              valueColor: AlwaysStoppedAnimation<Color>(
                variant == AppButtonVariant.primary
                    ? AppColors.surface
                    : AppColors.emeraldPrimary,
              ),
            ),
          )
        : Row(
            mainAxisSize: MainAxisSize.min,
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              if (icon != null) ...[
                Icon(icon, size: 18),
                const SizedBox(width: MosqueSpace.sm),
              ],
              Text(
                label,
                style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14),
              ),
            ],
          );

    switch (variant) {
      case AppButtonVariant.primary:
        return SizedBox(
          width: width,
          height: height,
          child: ElevatedButton(
            onPressed: isLoading ? null : onPressed,
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.emeraldPrimary,
              foregroundColor: AppColors.surface,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(MosqueRadius.medium),
              ),
            ),
            child: buttonContent,
          ),
        );
      case AppButtonVariant.secondary:
        return SizedBox(
          width: width,
          height: height,
          child: ElevatedButton(
            onPressed: isLoading ? null : onPressed,
            style: ElevatedButton.styleFrom(
              backgroundColor: isDark ? AppColors.nightSecondaryContainer : AppColors.goldLight,
              foregroundColor: isDark ? AppColors.goldLight : AppColors.goldDark,
              elevation: MosqueElevation.none,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(MosqueRadius.medium),
                side: const BorderSide(color: AppColors.gold, width: 1),
              ),
            ),
            child: buttonContent,
          ),
        );
      case AppButtonVariant.outline:
        return SizedBox(
          width: width,
          height: height,
          child: OutlinedButton(
            onPressed: isLoading ? null : onPressed,
            style: OutlinedButton.styleFrom(
              foregroundColor: isDark ? AppColors.greenAccent : AppColors.emeraldPrimary,
              side: BorderSide(
                color: isDark ? AppColors.greenAccent : AppColors.emeraldPrimary,
              ),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(MosqueRadius.medium),
              ),
            ),
            child: buttonContent,
          ),
        );
      case AppButtonVariant.text:
        return SizedBox(
          width: width,
          height: height,
          child: TextButton(
            onPressed: isLoading ? null : onPressed,
            style: TextButton.styleFrom(
              foregroundColor: isDark ? AppColors.greenAccent : AppColors.emeraldPrimary,
            ),
            child: buttonContent,
          ),
        );
    }
  }
}
