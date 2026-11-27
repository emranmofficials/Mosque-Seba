import 'package:flutter/material.dart';
import '../../../../features/role_permissions/models/committee_role.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../../router/app_router.dart';
import '../shared/role_shell_base.dart';

/// Dedicated interface shell for President (সভাপতি) — Tier 1 Executive.
class PresidentShell extends StatelessWidget {
  const PresidentShell({super.key});

  @override
  Widget build(BuildContext context) {
    return const RoleShellScaffold(
      expectedRole: CommitteeRole.president,
      showKpiSummary: true,
      showQuickActions: true,
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
          title: 'চূড়ান্ত অনুমোদন ও আর্থিক তদারকি',
          items: [
            RoleActionItem(
              icon: Icons.approval,
              title: 'ভাউচার ও হিসাব অনুমোদন',
              subtitle: 'আয়-ব্যয় অনুমোদন ও আর্থিক প্রতিবেদন',
              route: AppRouter.finance,
            ),
            RoleActionItem(
              icon: Icons.point_of_sale,
              title: 'জুমার ক্যাশবক্স গণনা',
              subtitle: '৩ সাক্ষীভিত্তিক নগদ টাকা গণনা',
              route: AppRouter.cashbox,
            ),
            RoleActionItem(
              icon: Icons.payments_outlined,
              title: 'স্টাফ বেতন ও বোনাস',
              subtitle: 'মাসিক বেতন শিট ও ঈদ বোনাস অনুমোদন',
              route: AppRouter.staffPayroll,
            ),
          ],
        ),
        RoleActionSection(
          title: 'প্রশাসন, শাসন ও নিরাপত্তা',
          items: [
            RoleActionItem(
              icon: Icons.how_to_vote_outlined,
              title: 'নির্বাচন ও গেজেট',
              subtitle: 'কমিটি নির্বাচন, মনোনয়ন ও ফলাফল',
              route: AppRouter.elections,
            ),
            RoleActionItem(
              icon: Icons.account_balance_outlined,
              title: 'ওয়াকফ সম্পত্তি রেজিস্টার',
              subtitle: 'স্থাবর-অস্থাবর সম্পত্তি ও দলিল',
              route: AppRouter.waqfAssets,
            ),
            RoleActionItem(
              icon: Icons.draw_outlined,
              title: 'অফিসিয়াল সিল ও স্বাক্ষর',
              subtitle: 'রশিদ ও ভাউচারের জন্য সিল-স্বাক্ষর',
              route: AppRouter.sealSignature,
            ),
            RoleActionItem(
              icon: Icons.security_outlined,
              title: 'অ্যাপ নিরাপত্তা ও ক্লাউড সিঙ্ক',
              subtitle: 'সুরক্ষা ড্যাশবোর্ড ও ব্যাকআপ সিঙ্ক',
              route: AppRouter.appSecurity,
            ),
          ],
        ),
      ],
    );
  }
}
