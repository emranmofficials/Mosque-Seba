import 'package:flutter/material.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_dimens.dart';
import '../widgets/app_button.dart';

/// Error State Placeholder with Retry Action
class ErrorStateView extends StatelessWidget {
  final String title;
  final String? errorMessage;
  final String? message;
  final VoidCallback? onRetry;

  const ErrorStateView({
    super.key,
    this.title = 'একটি ত্রুটি ঘটেছে',
    this.errorMessage,
    this.message,
    this.onRetry,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final displayMessage = message ?? errorMessage ?? '';

    return Center(
      child: Padding(
        padding: const EdgeInsets.all(MosqueSpace.xl),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(MosqueSpace.lg),
              decoration: BoxDecoration(
                color: AppColors.urgentRed.withOpacity(0.1),
                shape: BoxShape.circle,
              ),
              child: const Icon(
                Icons.error_outline_rounded,
                size: 48,
                color: AppColors.urgentRed,
              ),
            ),
            const SizedBox(height: MosqueSpace.md),
            Text(
              title,
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: isDark ? AppColors.nightTextPrimary : AppColors.textPrimary,
              ),
            ),
            const SizedBox(height: MosqueSpace.xs),
            Text(
              displayMessage,
              style: TextStyle(
                fontSize: 13,
                color: isDark ? AppColors.nightTextMuted : AppColors.textMuted,
              ),
              textAlign: TextAlign.center,
            ),
            if (onRetry != null) ...[
              const SizedBox(height: MosqueSpace.lg),
              AppButton(
                label: 'পুনরায় চেষ্টা করুন',
                variant: AppButtonVariant.outline,
                icon: Icons.refresh,
                onPressed: onRetry,
              ),
            ],
          ],
        ),
      ),
    );
  }
}
