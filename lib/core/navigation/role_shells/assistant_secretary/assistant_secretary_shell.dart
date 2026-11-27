import 'package:flutter/material.dart';

import '../../../../features/role_permissions/models/committee_role.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../../router/app_router.dart';
import '../shared/role_shell_base.dart';

/// Dedicated interface shell for Assistant Secretary (সহ-সাধারণ সম্পাদক) — Tier 1.
class AssistantSecretaryShell extends StatelessWidget {
  const AssistantSecretaryShell({super.key});

  @override
  Widget build(BuildContext context) {
    return const RoleShellScaffold(
      expectedRole: CommitteeRole.assistantSecretary,
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
            icon: Icons.school_outlined,
            selectedIcon: Icons.school,
            label: 'মাদরাসা',
          ),
          route: AppRouter.madrasa,
        ),
      ],
      sections: [
        RoleActionSection(
          title: 'দাপ্তরিক সহায়তা ও সমন্বয়',
          items: [
            RoleActionItem(
              icon: Icons.campaign_outlined,
              title: 'বিজ্ঞপ্তি ও ঘোষণা',
              subtitle: 'কমিটির নোটিশ প্রকাশ ও সমন্বয়',
              route: AppRouter.notices,
            ),
            RoleActionItem(
              icon: Icons.schedule_outlined,
              title: 'ডিউটি ও খাবার রোস্টার',
              subtitle: 'দৈনিক দায়িত্ব ও মেহমানদারি তালিকা',
              route: AppRouter.dutyRoster,
            ),
            RoleActionItem(
              icon: Icons.school_outlined,
              title: 'মাদরাসা ও মক্তব সহায়তা',
              subtitle: 'শিক্ষার্থী, হাজিরা ও পরীক্ষা সমন্বয়',
              route: AppRouter.madrasa,
            ),
            RoleActionItem(
              icon: Icons.notifications_active_outlined,
              title: 'জরুরি জানাজা বার্তা',
              subtitle: 'সাধারণ সম্পাদকের অনুপস্থিতিতে জানাজা অ্যালার্ট',
              route: AppRouter.janazaAlerts,
            ),
          ],
        ),
      ],
    );
  }
}
