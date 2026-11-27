import 'package:flutter/material.dart';

import '../../../../features/role_permissions/models/committee_role.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../../router/app_router.dart';
import '../shared/role_shell_base.dart';

/// Dedicated interface shell for Publicity Secretary (প্রচার ও প্রকাশনা সম্পাদক) — Tier 2.
class PublicitySecretaryShell extends StatelessWidget {
  const PublicitySecretaryShell({super.key});

  @override
  Widget build(BuildContext context) {
    return const RoleShellScaffold(
      expectedRole: CommitteeRole.publicitySecretary,
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
            icon: Icons.campaign_outlined,
            selectedIcon: Icons.campaign,
            label: 'নোটিশ',
          ),
          route: AppRouter.notices,
        ),
        RoleNavTarget(
          destination: AdaptiveNavigationDestination(
            icon: Icons.auto_awesome_outlined,
            selectedIcon: Icons.auto_awesome,
            label: 'পোস্টার AI',
          ),
          route: AppRouter.posterAi,
        ),
        RoleNavTarget(
          destination: AdaptiveNavigationDestination(
            icon: Icons.event_outlined,
            selectedIcon: Icons.event,
            label: 'ইভেন্ট',
          ),
          route: AppRouter.events,
        ),
      ],
      sections: [
        RoleActionSection(
          title: 'প্রচার, স্মার্ট বোর্ড ও ধর্মীয় দিবস',
          items: [
            RoleActionItem(
              icon: Icons.auto_awesome_outlined,
              title: 'ইসলামিক দাওয়াত পোস্টার AI',
              subtitle: 'ওয়াজ মাহফিল ও শবে বরাত-কদর পোস্টার',
              route: AppRouter.posterAi,
            ),
            RoleActionItem(
              icon: Icons.campaign_outlined,
              title: 'ডিজিটাল নোটিশ ও স্মার্ট বোর্ড',
              subtitle: 'পিন করা নোটিশ ও AI খসড়া তৈরি',
              route: AppRouter.notices,
            ),
            RoleActionItem(
              icon: Icons.calendar_month_outlined,
              title: 'ইসলামিক ক্যালেন্ডার ও রমজান হাব',
              subtitle: '১৩টি বিশেষ দিবস ও চাঁদ দেখা সমন্বয়',
              route: AppRouter.islamicCalendar,
            ),
            RoleActionItem(
              icon: Icons.access_time_outlined,
              title: 'নামাজের সময়সূচি প্রচার',
              subtitle: '৫ ওয়াক্ত ও জুমার জামাত সময়',
              route: AppRouter.prayerTimes,
            ),
          ],
        ),
      ],
    );
  }
}
