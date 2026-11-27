import 'package:flutter/material.dart';
import '../../../../features/role_permissions/models/committee_role.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../../router/app_router.dart';
import '../shared/role_shell_base.dart';

/// Dedicated interface shell for Executive Member (কার্যনির্বাহী সদস্য) — Tier 3.
class ExecutiveMemberShell extends StatelessWidget {
  const ExecutiveMemberShell({super.key});

  @override
  Widget build(BuildContext context) {
    return const RoleShellScaffold(
      expectedRole: CommitteeRole.executiveMember,
      showPrayerCard: true,
      showSmartBoard: true,
      showHadithBoard: true,
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
            icon: Icons.point_of_sale_outlined,
            selectedIcon: Icons.point_of_sale,
            label: 'ক্যাশবক্স',
          ),
          route: AppRouter.cashbox,
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
            icon: Icons.campaign_outlined,
            selectedIcon: Icons.campaign,
            label: 'নোটিশ',
          ),
          route: AppRouter.notices,
        ),
      ],
      sections: [
        RoleActionSection(
          title: 'সাক্ষী ও শৃঙ্খলা দায়িত্ব',
          items: [
            RoleActionItem(
              icon: Icons.verified_outlined,
              title: 'জুমার ক্যাশবক্স সাক্ষী',
              subtitle: 'নগদ গণনায় নিরপেক্ষ সাক্ষ্য ও স্বাক্ষর',
              route: AppRouter.cashbox,
            ),
            RoleActionItem(
              icon: Icons.groups_outlined,
              title: 'কমিটির সভা ও সিদ্ধান্ত',
              subtitle: 'সভার সময়সূচি ও কার্যবিবরণী',
              route: AppRouter.meetings,
            ),
            RoleActionItem(
              icon: Icons.event_outlined,
              title: 'জুমা, ঈদ ও ধর্মীয় অনুষ্ঠান',
              subtitle: 'শৃঙ্খলা রক্ষা ও স্বেচ্ছাসেবক দায়িত্ব',
              route: AppRouter.events,
            ),
          ],
        ),
      ],
    );
  }
}
