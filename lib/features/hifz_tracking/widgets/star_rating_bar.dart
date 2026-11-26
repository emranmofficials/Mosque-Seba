import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';

/// Interactive Star Rating Bar (1 to 5 Stars)
class StarRatingBar extends StatelessWidget {
  final int rating;
  final int maxRating;
  final ValueChanged<int>? onRatingChanged;
  final double size;
  final Color activeColor;

  const StarRatingBar({
    super.key,
    required this.rating,
    this.maxRating = 5,
    this.onRatingChanged,
    this.size = 20,
    this.activeColor = AppColors.goldDeep,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: List.generate(maxRating, (index) {
        final starIndex = index + 1;
        final isFilled = starIndex <= rating;

        return InkWell(
          onTap: onRatingChanged != null ? () => onRatingChanged!(starIndex) : null,
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 2),
            child: Icon(
              isFilled ? Icons.star : Icons.star_border,
              color: isFilled ? activeColor : Colors.grey.shade400,
              size: size,
            ),
          ),
        );
      }),
    );
  }
}
