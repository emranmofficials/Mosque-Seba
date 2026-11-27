import 'package:flutter/material.dart';
import '../../../../features/role_permissions/models/committee_role.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../../router/app_router.dart';
import '../shared/role_shell_base.dart';

/// Dedicated interface shell for Vice President (সহ-সভাপতি) — Tier 1 Executive.
class VicePresidentShell extends StatelessWidget {
  const VicePresidentShell({super.key});

  @override
  Widget build(BuildContext context) {
    return const RoleShellScaffold(
      expectedRole: CommitteeRole.vicePresident,
      showKpiSummary: true,
      showQuickActions: true,
      showPrayerCard: true,
      showSmartBoard: true,
      navTargets: [
        RoleNavTarget(
          destination: AdaptiveNavigationDestination(
            icon: Icons.dashboard_outlined,
            selectedIcon: Icons.dashboard,
            label: 'ড্যাশবোর্ড',
          ),
          route: AppRouter.dashboard,
        ),
        RoleNavTarget(
          destination: AdaptiveNavigationDestination(
            icon: Icons.account_balance_wallet_outlined,
            selectedIcon: Icons.account_balance_wallet,
            label: 'ফাইন্যান্স',
          ),
          route: AppRouter.finance,
        ),
        RoleNavTarget(
          destination: AdaptiveNavigationDestination(
            icon: Icons.people_outline,
            selectedIcon: Icons.people,
            label: 'সদস্য',
          ),
          route: AppRouter.members,
        ),
        RoleNavTarget(
          destination: AdaptiveNavigationDestination(
            icon: Icons.groups_outlined,
            selectedIcon: Icons.groups,
            label: 'সভা',
          ),
          route: AppRouter.meetings,
        ),
        RoleNavTarget(
          destination: AdaptiveNavigationDestination(
            icon: Icons.grid_view_outlined,
            selectedIcon: Icons.grid_view,
            label: 'অন্যান্য',
          ),
          route: AppRouter.more,
        ),
      ],
      sections: [
        RoleActionSection(
          title: 'উপ-কমিটি ও আর্থিক তদারকি',
          items: [
            RoleActionItem(
              icon: Icons.approval_outlined,
              title: 'ভাউচার অনুমোদন ও ক্যাশবক্স',
              subtitle: 'আর্থিক অনুমোদন ও সাক্ষী তদারকি',
              route: AppRouter.finance,
            ),
            RoleActionItem(
              icon: Icons.badge_outlined,
              title: 'স্টাফ ও ডিউটি রোস্টার',
              subtitle: 'ইমাম-মুয়াজ্জিন-খাদেম কার্যক্রম তদারকি',
              route: AppRouter.staff,
            ),
            RoleActionItem(
              icon: Icons.event_available_outlined,
              title: 'অনুষ্ঠান ও উন্নয়ন সমন্বয়',
              subtitle: 'ওয়াজ মাহফিল ও উপ-কমিটি ইভেন্ট',
              route: AppRouter.events,
            ),
          ],
        ),
      ],
    );
  }
}
