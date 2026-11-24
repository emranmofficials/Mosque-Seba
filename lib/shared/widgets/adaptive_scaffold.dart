import 'package:flutter/material.dart';
import '../../core/constants/app_constants.dart';
import '../../core/theme/app_colors.dart';

class AdaptiveNavigationDestination {
  final IconData icon;
  final IconData? selectedIcon;
  final String label;

  const AdaptiveNavigationDestination({
    required this.icon,
    this.selectedIcon,
    required this.label,
  });
}

/// Adaptive Scaffold supporting Mobile BottomBar and Tablet NavigationRail (600dp+ breakpoint)
class AdaptiveScaffold extends StatelessWidget {
  final int currentIndex;
  final ValueChanged<int> onNavigationIndexChanged;
  final List<AdaptiveNavigationDestination> destinations;
  final Widget body;
  final PreferredSizeWidget? appBar;
  final Widget? floatingActionButton;

  const AdaptiveScaffold({
    super.key,
    required this.currentIndex,
    required this.onNavigationIndexChanged,
    required this.destinations,
    required this.body,
    this.appBar,
    this.floatingActionButton,
  });

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final isTablet = constraints.maxWidth >= AppConstants.tabletBreakpoint;
        final theme = Theme.of(context);
        final isDark = theme.brightness == Brightness.dark;

        if (isTablet) {
          return Scaffold(
            appBar: appBar,
            floatingActionButton: floatingActionButton,
            body: Row(
              children: [
                NavigationRail(
                  selectedIndex: currentIndex,
                  onDestinationSelected: onNavigationIndexChanged,
                  labelType: NavigationRailLabelType.all,
                  backgroundColor: isDark ? AppColors.nightSurface : AppColors.surface,
                  selectedIconTheme: const IconThemeData(color: AppColors.emeraldPrimary),
                  selectedLabelTextStyle: const TextStyle(
                    color: AppColors.emeraldPrimary,
                    fontWeight: FontWeight.bold,
                    fontSize: 12,
                  ),
                  unselectedLabelTextStyle: TextStyle(
                    color: isDark ? AppColors.nightTextMuted : AppColors.textMuted,
                    fontSize: 11,
                  ),
                  destinations: destinations
                      .map(
                        (d) => NavigationRailDestination(
                          icon: Icon(d.icon),
                          selectedIcon: Icon(d.selectedIcon ?? d.icon),
                          label: Text(d.label),
                        ),
                      )
                      .toList(),
                ),
                const VerticalDivider(thickness: 1, width: 1),
                Expanded(child: body),
              ],
            ),
          );
        }

        return Scaffold(
          appBar: appBar,
          body: body,
          floatingActionButton: floatingActionButton,
          bottomNavigationBar: NavigationBar(
            selectedIndex: currentIndex,
            onDestinationSelected: onNavigationIndexChanged,
            backgroundColor: isDark ? AppColors.nightSurface : AppColors.surface,
            indicatorColor: isDark
                ? AppColors.nightSecondaryContainer
                : AppColors.emeraldSoft,
            destinations: destinations
                .map(
                  (d) => NavigationDestination(
                    icon: Icon(d.icon),
                    selectedIcon: Icon(
                      d.selectedIcon ?? d.icon,
                      color: isDark ? AppColors.greenAccent : AppColors.emeraldPrimary,
                    ),
                    label: d.label,
                  ),
                )
                .toList(),
          ),
        );
      },
    );
  }
}
