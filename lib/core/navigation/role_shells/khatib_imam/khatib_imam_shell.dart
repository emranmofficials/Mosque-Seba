import 'package:flutter/material.dart';

import '../../../../features/role_permissions/models/committee_role.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../../router/app_router.dart';
import '../shared/role_shell_base.dart';

/// Dedicated interface shell for Ex-officio Khatib / Imam (খতিব/ইমাম) — Tier 3.
class KhatibImamShell extends StatelessWidget {
  const KhatibImamShell({super.key});

  @override
  Widget build(BuildContext context) {
    return const RoleShellScaffold(
      expectedRole: CommitteeRole.imam,
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
            icon: Icons.auto_awesome_outlined,
            selectedIcon: Icons.auto_awesome,
            label: 'খুতবা AI',
          ),
          route: AppRouter.khutbahIdeas,
        ),
        RoleNavTarget(
          destination: AdaptiveNavigationDestination(
            icon: Icons.access_time_outlined,
            selectedIcon: Icons.access_time_filled,
            label: 'নামাজ',
          ),
          route: AppRouter.prayerTimes,
        ),
        RoleNavTarget(
          destination: AdaptiveNavigationDestination(
            icon: Icons.schedule_outlined,
            selectedIcon: Icons.schedule,
            label: 'রোস্টার',
          ),
          route: AppRouter.dutyRoster,
        ),
      ],
      sections: [
        RoleActionSection(
          title: 'ইমামতি, খুতবা ও শরিয়াহ পরামর্শ',
          items: [
            RoleActionItem(
              icon: Icons.auto_awesome_outlined,
              title: 'জুমার খুতবা ও AI সহকারী',
              subtitle: 'খুতবার বিষয়বস্তু ও কোরআন-হাদিস রেফারেন্স',
              route: AppRouter.khutbahIdeas,
            ),
            RoleActionItem(
              icon: Icons.schedule_outlined,
              title: 'ইমাম-মুয়াজ্জিন ডিউটি রোস্টার',
              subtitle: '৫ ওয়াক্ত ও জুমার দায়িত্ব সমন্বয়',
              route: AppRouter.dutyRoster,
            ),
            RoleActionItem(
              icon: Icons.nights_stay_outlined,
              title: 'রমজান ও ইসলামিক ক্যালেন্ডার',
              subtitle: 'তারাবিহ, ইতিকাফ ও চাঁদ দেখা',
              route: AppRouter.ramadanHub,
            ),
            RoleActionItem(
              icon: Icons.verified_outlined,
              title: 'মাদরাসা ও ক্যাশবক্স সাক্ষী',
              subtitle: 'দ্বীনি শিক্ষা তদারকি ও সাক্ষ্য প্রদান',
              route: AppRouter.madrasa,
            ),
            RoleActionItem(
              icon: Icons.groups_outlined,
              title: 'কমিটির সভা ও শরিয়াহ পর্যালোচনা',
              subtitle: 'পদাধিকারবলে সিদ্ধান্তে শরিয়াহ মতামত',
              route: AppRouter.meetings,
            ),
          ],
        ),
      ],
    );
  }
}
