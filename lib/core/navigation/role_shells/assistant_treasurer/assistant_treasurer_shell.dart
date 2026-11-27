import 'package:flutter/material.dart';
import '../../../../features/role_permissions/models/committee_role.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../../router/app_router.dart';
import '../shared/role_shell_base.dart';

/// Dedicated interface shell for Assistant Treasurer (সহ-অর্থ সম্পাদক) — Tier 1.
class AssistantTreasurerShell extends StatelessWidget {
  const AssistantTreasurerShell({super.key});

  @override
  Widget build(BuildContext context) {
    return const RoleShellScaffold(
      expectedRole: CommitteeRole.assistantTreasurer,
      showKpiSummary: true,
      showQuickActions: true,
      showPrayerCard: true,
      showSmartBoard: false,
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
            icon: Icons.point_of_sale_outlined,
            selectedIcon: Icons.point_of_sale,
            label: 'ক্যাশবক্স',
          ),
          route: AppRouter.cashbox,
        ),
        RoleNavTarget(
          destination: AdaptiveNavigationDestination(
            icon: Icons.receipt_long_outlined,
            selectedIcon: Icons.receipt_long,
            label: 'চাঁদা',
          ),
          route: AppRouter.chandaLedger,
        ),
      ],
      sections: [
        RoleActionSection(
          title: 'আর্থিক সহায়তা ও আদায়',
          items: [
            RoleActionItem(
              icon: Icons.receipt_long_outlined,
              title: 'মাসিক চাঁদা ও দান রশিদ',
              subtitle: 'QR রশিদ ইস্যু ও সদস্য লেজার',
              route: AppRouter.chandaLedger,
            ),
            RoleActionItem(
              icon: Icons.point_of_sale_outlined,
              title: 'ক্যাশবক্স গণনায় সহায়তা',
              subtitle: 'জুমার দানবক্স গণনা ও ভাউচার পোস্টিং',
              route: AppRouter.cashbox,
            ),
            RoleActionItem(
              icon: Icons.school_outlined,
              title: 'মাদরাসা বেতন ও ফি',
              subtitle: 'শিক্ষার্থী ফি আদায় ও রশিদ',
              route: AppRouter.madrasaFees,
            ),
          ],
        ),
      ],
    );
  }
}
