import 'package:flutter/material.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_dimens.dart';
import '../widgets/app_button.dart';

/// Clean Islamic Empty State Placeholder
class EmptyStateView extends StatelessWidget {
  final String title;
  final String? subtitle;
  final String? message;
  final IconData icon;
  final Widget? action;
  final String? actionLabel;
  final VoidCallback? onAction;

  const EmptyStateView({
    super.key,
    required this.title,
    this.subtitle,
    this.message,
    this.icon = Icons.inbox_outlined,
    this.action,
    this.actionLabel,
    this.onAction,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final effectiveSubtitle = message ?? subtitle;
    final effectiveAction = action ??
        (actionLabel != null && onAction != null
            ? AppButton(
                label: actionLabel!,
                onPressed: onAction,
              )
            : null);

    return Center(
      child: Padding(
        padding: const EdgeInsets.all(MosqueSpace.xl),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(MosqueSpace.lg),
              decoration: BoxDecoration(
                color: isDark
                    ? AppColors.nightSurfaceVariant
                    : AppColors.emeraldSoft,
                shape: BoxShape.circle,
              ),
              child: Icon(
                icon,
                size: 48,
                color: isDark ? AppColors.greenAccent : AppColors.emeraldPrimary,
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
              textAlign: TextAlign.center,
            ),
            if (effectiveSubtitle != null) ...[
              const SizedBox(height: MosqueSpace.xs),
              Text(
                effectiveSubtitle,
                style: TextStyle(
                  fontSize: 13,
                  color: isDark ? AppColors.nightTextMuted : AppColors.textMuted,
                ),
                textAlign: TextAlign.center,
              ),
            ],
            if (effectiveAction != null) ...[
              const SizedBox(height: MosqueSpace.lg),
              effectiveAction,
            ],
          ],
        ),
      ),
    );
  }
}
