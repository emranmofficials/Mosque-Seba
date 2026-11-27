import 'package:flutter/material.dart';

import '../../../../features/role_permissions/models/committee_role.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../../router/app_router.dart';
import '../shared/role_shell_base.dart';

/// Dedicated interface shell for Treasurer (কোষাধ্যক্ষ / অর্থ সম্পাদক) — Tier 1.
class TreasurerShell extends StatelessWidget {
  const TreasurerShell({super.key});

  @override
  Widget build(BuildContext context) {
    return const RoleShellScaffold(
      expectedRole: CommitteeRole.treasurer,
      showKpiSummary: true,
      showQuickActions: false,
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
          title: 'তহবিল, হিসাব ও ক্যাশবক্স',
          items: [
            RoleActionItem(
              icon: Icons.account_balance_wallet_outlined,
              title: 'আয়-ব্যয় লেজার ও ব্যাংক',
              subtitle: 'ভাউচার তৈরি, নগদ ও ব্যাংক ব্যালেন্স',
              route: AppRouter.finance,
            ),
            RoleActionItem(
              icon: Icons.point_of_sale_outlined,
              title: 'জুমার ক্যাশবক্স গণনা',
              subtitle: 'নোট গণনা ও ৩ সাক্ষীর স্বাক্ষর গ্রহণ',
              route: AppRouter.cashbox,
            ),
            RoleActionItem(
              icon: Icons.receipt_long_outlined,
              title: 'চাঁদা ও দান সংগ্রহ',
              subtitle: 'QR রশিদ ও স্বয়ংক্রিয় ডাবল-এন্ট্রি',
              route: AppRouter.chandaLedger,
            ),
          ],
        ),
        RoleActionSection(
          title: 'বেতন, মাদরাসা ফি ও প্রিন্ট',
          items: [
            RoleActionItem(
              icon: Icons.payments_outlined,
              title: 'স্টাফ বেতন ও ঈদ বোনাস',
              subtitle: 'মাসিক বেতন প্রদান ও খরচ পোস্টিং',
              route: AppRouter.staffPayroll,
            ),
            RoleActionItem(
              icon: Icons.school_outlined,
              title: 'মাদরাসা ফি সংগ্রহ',
              subtitle: 'ছাত্র বেতন রশিদ ও আয় সমন্বয়',
              route: AppRouter.madrasaFees,
            ),
            RoleActionItem(
              icon: Icons.print_outlined,
              title: 'আর্থিক রিপোর্ট ও রশিদ প্রিন্ট',
              subtitle: 'সিল-স্বাক্ষরসহ পিডিএফ ও প্রিন্ট',
              route: AppRouter.printHub,
            ),
            RoleActionItem(
              icon: Icons.draw_outlined,
              title: 'অফিসিয়াল সিল ও স্বাক্ষর',
              subtitle: 'রশিদ ও ভাউচারের জন্য ডিজিটাল স্বাক্ষর',
              route: AppRouter.sealSignature,
            ),
          ],
        ),
      ],
    );
  }
}
