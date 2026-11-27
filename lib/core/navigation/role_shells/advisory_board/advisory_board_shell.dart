import 'package:flutter/material.dart';

import '../../../../features/role_permissions/models/committee_role.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../../router/app_router.dart';
import '../shared/role_shell_base.dart';

/// Dedicated interface shell for Advisory Board (উপদেষ্টা পরিষদ) — Tier 3.
class AdvisoryBoardShell extends StatelessWidget {
  const AdvisoryBoardShell({super.key});

  @override
  Widget build(BuildContext context) {
    return const RoleShellScaffold(
      expectedRole: CommitteeRole.advisoryBoard,
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
            icon: Icons.groups_outlined,
            selectedIcon: Icons.groups,
            label: 'সভা',
          ),
          route: AppRouter.meetings,
        ),
        RoleNavTarget(
          destination: AdaptiveNavigationDestination(
            icon: Icons.assessment_outlined,
            selectedIcon: Icons.assessment,
            label: 'রিপোর্ট',
          ),
          route: AppRouter.finance,
        ),
        RoleNavTarget(
          destination: AdaptiveNavigationDestination(
            icon: Icons.how_to_vote_outlined,
            selectedIcon: Icons.how_to_vote,
            label: 'নির্বাচন',
          ),
          route: AppRouter.elections,
        ),
      ],
      sections: [
        RoleActionSection(
          title: 'কৌশলগত পরামর্শ ও তদারকি',
          items: [
            RoleActionItem(
              icon: Icons.groups_outlined,
              title: 'সভার কার্যবিবরণী ও পরামর্শ',
              subtitle: 'গুরুত্বপূর্ণ সিদ্ধান্ত ও সংকট সমাধান',
              route: AppRouter.meetings,
            ),
            RoleActionItem(
              icon: Icons.assessment_outlined,
              title: 'আর্থিক স্বচ্ছতা প্রতিবেদন',
              subtitle: 'মাসিক আয়-ব্যয় ও অডিট পর্যবেক্ষণ',
              route: AppRouter.finance,
            ),
            RoleActionItem(
              icon: Icons.account_balance_outlined,
              title: 'ওয়াকফ ও উন্নয়ন পরিকল্পনা',
              subtitle: 'মসজিদ নির্মাণ ও সম্পত্তি পর্যালোচনা',
              route: AppRouter.waqfAssets,
            ),
          ],
        ),
      ],
    );
  }
}
