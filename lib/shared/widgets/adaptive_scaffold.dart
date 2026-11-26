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

/// Adaptive Scaffold supporting Mobile BottomBar, Tablet NavigationRail (600dp+),
/// and standard detail screens with an AppBar title/actions
class AdaptiveScaffold extends StatelessWidget {
  final int? currentIndex;
  final ValueChanged<int>? onNavigationIndexChanged;
  final List<AdaptiveNavigationDestination>? destinations;
  final Widget body;
  final PreferredSizeWidget? appBar;
  final String? title;
  final List<Widget>? actions;
  final Widget? floatingActionButton;

  const AdaptiveScaffold({
    super.key,
    this.currentIndex,
    this.onNavigationIndexChanged,
    this.destinations,
    required this.body,
    this.appBar,
    this.title,
    this.actions,
    this.floatingActionButton,
  });

  @override
  Widget build(BuildContext context) {
    final navDestinations = destinations;
    final selectedIndex = currentIndex;
    final onNavChanged = onNavigationIndexChanged;

    if (navDestinations == null ||
        navDestinations.isEmpty ||
        selectedIndex == null ||
        onNavChanged == null) {
      return Scaffold(
        appBar: appBar ??
            (title != null
                ? AppBar(
                    title: Text(title!),
                    actions: actions,
                  )
                : null),
        body: body,
        floatingActionButton: floatingActionButton,
      );
    }

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
                  selectedIndex: selectedIndex,
                  onDestinationSelected: onNavChanged,
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
                  destinations: navDestinations
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
            selectedIndex: selectedIndex,
            onDestinationSelected: onNavChanged,
            backgroundColor: isDark ? AppColors.nightSurface : AppColors.surface,
            indicatorColor: isDark
                ? AppColors.nightSecondaryContainer
                : AppColors.emeraldSoft,
            destinations: navDestinations
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
