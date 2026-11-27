import 'package:flutter/material.dart';
import '../../../../features/role_permissions/models/committee_role.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../../router/app_router.dart';
import '../shared/role_shell_base.dart';

/// Dedicated interface shell for Organizational Secretary (সাংগঠনিক সম্পাদক) — Tier 2.
class OrganizationalSecretaryShell extends StatelessWidget {
  const OrganizationalSecretaryShell({super.key});

  @override
  Widget build(BuildContext context) {
    return const RoleShellScaffold(
      expectedRole: CommitteeRole.organizingSecretary,
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
            icon: Icons.event_outlined,
            selectedIcon: Icons.event,
            label: 'ইভেন্ট',
          ),
          route: AppRouter.events,
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
          title: 'সাংগঠনিক শৃঙ্খলা ও মুসল্লি সংযোগ',
          items: [
            RoleActionItem(
              icon: Icons.people_outline,
              title: 'সদস্য ও মুসল্লি ডিরেক্টরি',
              subtitle: 'সদস্য নিবন্ধন, প্রোফাইল ও যোগাযোগ',
              route: AppRouter.members,
            ),
            RoleActionItem(
              icon: Icons.event_available_outlined,
              title: 'ওয়াজ মাহফিল ও অনুষ্ঠান সমন্বয়',
              subtitle: 'স্বেচ্ছাসেবক, বাজেট ও প্রোগ্রাম',
              route: AppRouter.events,
            ),
            RoleActionItem(
              icon: Icons.groups_outlined,
              title: 'কমিটির সভা ও আমন্ত্রণ',
              subtitle: 'সভা আহ্বান ও সাংগঠনিক সিদ্ধান্ত',
              route: AppRouter.meetings,
            ),
            RoleActionItem(
              icon: Icons.notifications_active_outlined,
              title: 'জরুরি জানাজা বার্তা',
              subtitle: 'এলাকায় জানাজা ও দাফন তথ্য প্রচার',
              route: AppRouter.janazaAlerts,
            ),
          ],
        ),
      ],
    );
  }
}
