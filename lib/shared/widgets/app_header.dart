import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../bootstrap/dependency_injection.dart';
import '../../core/config/app_config.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_dimens.dart';

/// Global Application Header (GlobalHeader.kt Parity)
/// Displays official logo, user name, committee designation, theme toggle, and notification badge
class AppHeader extends ConsumerWidget implements PreferredSizeWidget {
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
  Widget build(BuildContext context, WidgetRef ref) {
    final isDark = ref.watch(isDarkModeProvider);

    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: MosqueContentPadding.screenHorizontal,
        vertical: MosqueSpace.sm,
      ),
      decoration: BoxDecoration(
        color: isDark ? AppColors.nightSurface : AppColors.emeraldDark,
        border: isDark ? const Border(bottom: BorderSide(color: AppColors.nightBorder, width: 0.8)) : null,
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
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                      color: AppColors.surface,
                      fontWeight: FontWeight.bold,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                  Text(
                    designation.isNotEmpty ? designation : 'সাধারণ সদস্য',
                    style: Theme.of(context).textTheme.labelSmall?.copyWith(
                      color: AppColors.goldLight,
                      fontWeight: FontWeight.w500,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                ],
              ),
            ),

            // 1-Tap Eye-Comfort Islamic Night Mode Toggle
            IconButton(
              icon: Icon(
                isDark ? Icons.light_mode : Icons.dark_mode_outlined,
                color: isDark ? AppColors.gold : AppColors.surface,
                size: 22,
              ),
              tooltip: isDark ? 'স্বাভাবিক মোড চালু করুন' : 'ইসলামিক নাইট মোড চালু করুন',
              onPressed: () {
                ref.read(isDarkModeProvider.notifier).state = !isDark;
              },
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
                          style: Theme.of(context).textTheme.labelSmall?.copyWith(
                            color: Colors.white,
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
