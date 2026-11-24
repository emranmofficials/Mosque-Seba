import 'package:flutter/material.dart';
import '../../core/config/app_config.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_dimens.dart';

/// Global Application Header (GlobalHeader.kt Parity)
/// Displays official logo, user name, committee designation, and notification badge
class AppHeader extends StatelessWidget implements PreferredSizeWidget {
  final String userName;
  final String designation;
  final int unreadNotifications;
  final VoidCallback? onNotificationTap;
  final VoidCallback? onProfileTap;

  const AppHeader({
    super.key,
    required this.userName,
    required this.designation,
    this.unreadNotifications = 0,
    this.onNotificationTap,
    this.onProfileTap,
  });

  @override
  Size get preferredSize => const Size.fromHeight(64);

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: MosqueContentPadding.screenHorizontal,
        vertical: MosqueSpace.sm,
      ),
      decoration: const BoxDecoration(
        color: AppColors.emeraldDark,
      ),
      child: SafeArea(
        bottom: false,
        child: Row(
          children: [
            // Official App Logo
            ClipRRect(
              borderRadius: BorderRadius.circular(MosqueRadius.sm),
              child: Image.asset(
                AppConfig.logoAsset,
                width: 36,
                height: 36,
                fit: BoxFit.contain,
                errorBuilder: (_, __, ___) => const Icon(
                  Icons.mosque,
                  color: AppColors.gold,
                  size: 28,
                ),
              ),
            ),
            const SizedBox(width: MosqueSpace.md),

            // User Name and Committee Designation
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    userName.isNotEmpty ? userName : 'মুহতারাম মুসল্লি',
                    style: const TextStyle(
                      color: AppColors.surface,
                      fontSize: 14,
                      fontWeight: FontWeight.bold,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                  Text(
                    designation.isNotEmpty ? designation : 'সাধারণ সদস্য',
                    style: const TextStyle(
                      color: AppColors.goldLight,
                      fontSize: 11,
                      fontWeight: FontWeight.w500,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                ],
              ),
            ),

            // Notification Bell with Badge
            IconButton(
              icon: Stack(
                children: [
                  const Icon(
                    Icons.notifications_outlined,
                    color: AppColors.surface,
                    size: 24,
                  ),
                  if (unreadNotifications > 0)
                    Positioned(
                      right: 0,
                      top: 0,
                      child: Container(
                        padding: const EdgeInsets.all(2),
                        decoration: const BoxDecoration(
                          color: AppColors.urgentRed,
                          shape: BoxShape.circle,
                        ),
                        constraints: const BoxConstraints(
                          minWidth: 14,
                          minHeight: 14,
                        ),
                        child: Text(
                          '$unreadNotifications',
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 9,
                            fontWeight: FontWeight.bold,
                          ),
                          textAlign: TextAlign.center,
                        ),
                      ),
                    ),
                ],
              ),
              onPressed: onNotificationTap,
            ),

            // Profile / Menu Action
            IconButton(
              icon: const Icon(
                Icons.account_circle_outlined,
                color: AppColors.surface,
                size: 24,
              ),
              onPressed: onProfileTap,
            ),
          ],
        ),
      ),
    );
  }
}
