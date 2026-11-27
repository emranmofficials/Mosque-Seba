import 'package:flutter/material.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_dimens.dart';

/// Standard Islamic Elevated Card Shell with Material 3 Ripple and Adaptive Shadow
class CardShell extends StatelessWidget {
  final Widget child;
  final EdgeInsetsGeometry padding;
  final EdgeInsetsGeometry? margin;
  final Color? color;
  final Gradient? gradient;
  final Border? border;
  final double? borderRadius;
  final VoidCallback? onTap;

  const CardShell({
    super.key,
    required this.child,
    this.padding = const EdgeInsets.all(MosqueContentPadding.cardInternal),
    this.margin,
    this.color,
    this.gradient,
    this.border,
    this.borderRadius,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final cardColor = color ?? (isDark ? AppColors.nightSurface : AppColors.cardBg);
    final radius = BorderRadius.circular(borderRadius ?? MosqueRadius.card);
    final cardBorder = border ??
        Border.all(
          color: isDark ? AppColors.nightBorder : AppColors.cardBorder,
          width: 1,
        );

    final decoration = BoxDecoration(
      color: gradient == null ? cardColor : null,
      gradient: gradient,
      borderRadius: radius,
      border: cardBorder,
      boxShadow: [
        BoxShadow(
          color: isDark ? Colors.black.withOpacity(0.3) : const Color(0x0A0D5C3A),
          blurRadius: 10,
          spreadRadius: 0,
          offset: const Offset(0, 3),
        ),
      ],
    );

    if (onTap != null) {
      return Container(
        margin: margin,
        decoration: decoration,
        child: Material(
          color: Colors.transparent,
          borderRadius: radius,
          child: InkWell(
            onTap: onTap,
            borderRadius: radius,
            splashColor: AppColors.emeraldPrimary.withOpacity(0.12),
            highlightColor: AppColors.emeraldPrimary.withOpacity(0.06),
            child: Padding(
              padding: padding,
              child: child,
            ),
          ),
        ),
      );
    }

    return Container(
      margin: margin,
      padding: padding,
      decoration: decoration,
      child: child,
    );
  }
}
