import 'package:flutter/material.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_dimens.dart';

/// Standard Islamic Elevated Card Shell with Subtle Border
class CardShell extends StatelessWidget {
  final Widget child;
  final EdgeInsetsGeometry padding;
  final EdgeInsetsGeometry? margin;
  final Color? color;
  final Border? border;
  final VoidCallback? onTap;

  const CardShell({
    super.key,
    required this.child,
    this.padding = const EdgeInsets.all(MosqueContentPadding.cardInternal),
    this.margin,
    this.color,
    this.border,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final cardColor = color ?? (isDark ? AppColors.nightSurface : AppColors.cardBg);
    final cardBorder = border ??
        Border.all(
          color: isDark ? AppColors.nightBorder : AppColors.cardBorder,
          width: 1,
        );

    Widget content = Container(
      margin: margin,
      padding: padding,
      decoration: BoxDecoration(
        color: cardColor,
        borderRadius: BorderRadius.circular(MosqueRadius.card),
        border: cardBorder,
        boxShadow: [
          BoxShadow(
            color: isDark ? Colors.black26 : Colors.black.withOpacity(0.04),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: child,
    );

    if (onTap != null) {
      return Material(
        color: Colors.transparent,
        borderRadius: BorderRadius.circular(MosqueRadius.card),
        child: InkWell(
          onTap: onTap,
          borderRadius: BorderRadius.circular(MosqueRadius.card),
          child: content,
        ),
      );
    }

    return content;
  }
}
