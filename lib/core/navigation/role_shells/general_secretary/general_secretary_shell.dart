import 'package:flutter/material.dart';

import '../../../../features/role_permissions/models/committee_role.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../../router/app_router.dart';
import '../shared/role_shell_base.dart';

/// Dedicated interface shell for General Secretary (সাধারণ সম্পাদক) — Tier 1 Executive.
class GeneralSecretaryShell extends StatelessWidget {
  const GeneralSecretaryShell({super.key});

  @override
  Widget build(BuildContext context) {
    return const RoleShellScaffold(
      expectedRole: CommitteeRole.generalSecretary,
      showKpiSummary: true,
      showQuickActions: false,
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
            icon: Icons.badge_outlined,
            selectedIcon: Icons.badge,
            label: 'স্টাফ',
          ),
          route: AppRouter.staff,
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
          title: 'দৈনন্দিন প্রশাসন ও সভা ব্যবস্থাপনা',
          items: [
            RoleActionItem(
              icon: Icons.edit_calendar_outlined,
              title: 'কমিটির সভা ও রেজুলেশন',
              subtitle: 'এজেন্ডা, আমন্ত্রণ ও স্থায়ী কার্যবিবরণী',
              route: AppRouter.meetings,
            ),
            RoleActionItem(
              icon: Icons.campaign_outlined,
              title: 'নোটিশ ও AI খসড়া',
              subtitle: 'বিজ্ঞপ্তি প্রকাশ ও স্মার্ট বোর্ড নিয়ন্ত্রণ',
              route: AppRouter.notices,
            ),
            RoleActionItem(
              icon: Icons.notifications_active_outlined,
              title: 'জরুরি জানাজা বার্তা',
              subtitle: 'তাৎক্ষণিক এসএমএস ও হোয়াটসঅ্যাপ অ্যালার্ট',
              route: AppRouter.janazaAlerts,
            ),
          ],
        ),
        RoleActionSection(
          title: 'স্টাফ ও নথিপত্র তদারকি',
          items: [
            RoleActionItem(
              icon: Icons.schedule_outlined,
              title: 'ইমাম-মুয়াজ্জিন ডিউটি রোস্টার',
              subtitle: 'পাঁচ ওয়াক্ত ও জুমার দায়িত্ব বণ্টন',
              route: AppRouter.dutyRoster,
            ),
            RoleActionItem(
              icon: Icons.print_outlined,
              title: 'প্রিন্ট ও পিডিএফ হাব',
              subtitle: 'রেজুলেশন, নোটিশ ও দাপ্তরিক কপি',
              route: AppRouter.printHub,
            ),
          ],
        ),
      ],
    );
  }
}
