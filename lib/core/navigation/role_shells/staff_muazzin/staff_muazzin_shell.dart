import 'package:flutter/material.dart';

import '../../../../features/role_permissions/models/committee_role.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../../router/app_router.dart';
import '../shared/role_shell_base.dart';

/// Dedicated interface shell for Muazzin (মুয়াজ্জিন).
class StaffMuazzinShell extends StatelessWidget {
  const StaffMuazzinShell({super.key});

  @override
  Widget build(BuildContext context) {
    return const RoleShellScaffold(
      expectedRole: CommitteeRole.muazzin,
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
            label: 'আজান ও নামাজ',
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
          title: 'আজান, ইকামত ও ওয়াক্ত পরিচালনা',
          items: [
            RoleActionItem(
              icon: Icons.access_time_outlined,
              title: '৫ ওয়াক্ত আজান ও জামাত সময়',
              subtitle: 'আজান-ইকামত সময়সূচি ও অ্যালার্ম',
              route: AppRouter.prayerTimes,
            ),
            RoleActionItem(
              icon: Icons.schedule_outlined,
              title: 'ডিউটি রোস্টার',
              subtitle: 'দৈনিক ও সাপ্তাহিক মুয়াজ্জিন পালা',
              route: AppRouter.dutyRoster,
            ),
            RoleActionItem(
              icon: Icons.restaurant_outlined,
              title: 'খাবারের সময়সূচি',
              subtitle: 'দৈনিক দুপুর ও রাতের খাবারের পালা',
              route: AppRouter.mealSchedule,
            ),
            RoleActionItem(
              icon: Icons.notifications_active_outlined,
              title: 'জরুরি জানাজা ঘোষণা',
              subtitle: 'জানাজার সময় ও স্থান তথ্য',
              route: AppRouter.janazaAlerts,
            ),
          ],
        ),
      ],
    );
  }
}
