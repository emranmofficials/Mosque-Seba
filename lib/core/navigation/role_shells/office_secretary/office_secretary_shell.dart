import 'package:flutter/material.dart';
import '../../../../features/role_permissions/models/committee_role.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../../router/app_router.dart';
import '../shared/role_shell_base.dart';

/// Dedicated interface shell for Office Secretary (দপ্তর সম্পাদক) — Tier 2.
class OfficeSecretaryShell extends StatelessWidget {
  const OfficeSecretaryShell({super.key});

  @override
  Widget build(BuildContext context) {
    return const RoleShellScaffold(
      expectedRole: CommitteeRole.officeSecretary,
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
            icon: Icons.account_balance_outlined,
            selectedIcon: Icons.account_balance,
            label: 'ওয়াকফ',
          ),
          route: AppRouter.waqfAssets,
        ),
        RoleNavTarget(
          destination: AdaptiveNavigationDestination(
            icon: Icons.print_outlined,
            selectedIcon: Icons.print,
            label: 'প্রিন্ট হাব',
          ),
          route: AppRouter.printHub,
        ),
        RoleNavTarget(
          destination: AdaptiveNavigationDestination(
            icon: Icons.draw_outlined,
            selectedIcon: Icons.draw,
            label: 'সিল ও স্বাক্ষর',
          ),
          route: AppRouter.sealSignature,
        ),
      ],
      sections: [
        RoleActionSection(
          title: 'দাপ্তরিক নথি, দলিল ও রশিদ',
          items: [
            RoleActionItem(
              icon: Icons.account_balance_outlined,
              title: 'ওয়াকফ সম্পত্তি ও দলিলপত্র',
              subtitle: 'স্থাবর-অস্থাবর সম্পত্তির ডিজিটাল রেজিস্টার',
              route: AppRouter.waqfAssets,
            ),
            RoleActionItem(
              icon: Icons.draw_outlined,
              title: 'অফিসিয়াল সিল ও স্বাক্ষর',
              subtitle: 'ডিজিটাল সিল ও স্বাক্ষর সংরক্ষণ',
              route: AppRouter.sealSignature,
            ),
            RoleActionItem(
              icon: Icons.print_outlined,
              title: '১-ক্লিক প্রিন্ট ও পিডিএফ',
              subtitle: 'রশিদ, ভাউচার, বেতন স্লিপ ও নোটিশ',
              route: AppRouter.printHub,
            ),
            RoleActionItem(
              icon: Icons.receipt_long_outlined,
              title: 'চাঁদা ও মাদরাসা ফি রশিদ',
              subtitle: 'অফিসিয়াল রশিদ ইস্যু ও নথিভুক্তি',
              route: AppRouter.chandaLedger,
            ),
          ],
        ),
      ],
    );
  }
}
