import 'package:flutter/material.dart';
import '../../../core/config/app_config.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_gradients.dart';

/// Islamic Mihrab Arch Banner for Authentication Screens
class AuthHeaderBanner extends StatelessWidget {
  final String title;
  final String subtitle;

  const AuthHeaderBanner({
    super.key,
    required this.title,
    required this.subtitle,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.fromLTRB(24, 48, 24, 28),
      decoration: const BoxDecoration(
        gradient: AppGradients.emeraldDarkGradient,
        borderRadius: BorderRadius.only(
          bottomLeft: Radius.circular(32),
          bottomRight: Radius.circular(32),
        ),
      ),
      child: Column(
        children: [
          Container(
            width: 64,
            height: 64,
            padding: const EdgeInsets.all(8),
            decoration: const BoxDecoration(
              shape: BoxShape.circle,
              color: AppColors.surface,
            ),
            child: Image.asset(
              AppConfig.logoAsset,
              fit: BoxFit.contain,
              errorBuilder: (_, __, ___) => const Icon(
                Icons.mosque,
                size: 36,
                color: AppColors.emeraldPrimary,
              ),
            ),
          ),
          const SizedBox(height: 14),
          Text(
            title,
            style: const TextStyle(
              fontFamily: AppConfig.fontHindSiliguri,
              color: AppColors.surface,
              fontSize: 22,
              fontWeight: FontWeight.bold,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 4),
          Text(
            subtitle,
            style: const TextStyle(
              fontFamily: AppConfig.fontHindSiliguri,
              color: AppColors.goldLight,
              fontSize: 12,
            ),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }
}
