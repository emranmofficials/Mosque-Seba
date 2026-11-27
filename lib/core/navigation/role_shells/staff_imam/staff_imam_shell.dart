import 'package:flutter/material.dart';

import '../../../../features/role_permissions/models/committee_role.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../../router/app_router.dart';
import '../shared/role_shell_base.dart';

/// Dedicated interface shell for Operational Imam (ইমাম স্টাফ).
class StaffImamShell extends StatelessWidget {
  const StaffImamShell({super.key});

  @override
  Widget build(BuildContext context) {
    return const RoleShellScaffold(
      expectedRole: CommitteeRole.staffImam,
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
        RoleNavTarget(
          destination: AdaptiveNavigationDestination(
            icon: Icons.restaurant_outlined,
            selectedIcon: Icons.restaurant,
            label: 'খাবার',
          ),
          route: AppRouter.mealSchedule,
        ),
      ],
      sections: [
        RoleActionSection(
          title: 'দৈনিক ইমামতি ও খুতবা',
          items: [
            RoleActionItem(
              icon: Icons.schedule_outlined,
              title: 'আমার ডিউটি রোস্টার',
              subtitle: '৫ ওয়াক্ত ও জুমার নামাজের দায়িত্ব',
              route: AppRouter.dutyRoster,
            ),
            RoleActionItem(
              icon: Icons.auto_awesome_outlined,
              title: 'খুতবা ও বয়ান সহকারী (AI)',
              subtitle: 'জুমার খুতবার পয়েন্ট ও হাদিস',
              route: AppRouter.khutbahIdeas,
            ),
            RoleActionItem(
              icon: Icons.restaurant_outlined,
              title: 'দৈনিক খাবার রোস্টার',
              subtitle: 'মেহমানদারি ও খাবারের পালা',
              route: AppRouter.mealSchedule,
            ),
            RoleActionItem(
              icon: Icons.notifications_active_outlined,
              title: 'জানাজা ও রমজান হাব',
              subtitle: 'জানাজা সময় ও তারাবিহ-খতমে কোরআন',
              route: AppRouter.janazaAlerts,
            ),
          ],
        ),
      ],
    );
  }
}
