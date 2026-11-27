import 'package:flutter/material.dart';

import '../../../../features/role_permissions/models/committee_role.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../../router/app_router.dart';
import '../shared/role_shell_base.dart';

/// Dedicated interface shell for Khadem (খাদেম).
class StaffKhademShell extends StatelessWidget {
  const StaffKhademShell({super.key});

  @override
  Widget build(BuildContext context) {
    return const RoleShellScaffold(
      expectedRole: CommitteeRole.khadem,
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
          title: 'মসজিদ পরিচর্যা ও দৈনিক দায়িত্ব',
          items: [
            RoleActionItem(
              icon: Icons.schedule_outlined,
              title: 'ডিউটি ও পরিচ্ছন্নতা রোস্টার',
              subtitle: 'নামাজ-পূর্ব প্রস্তুতি ও দৈনিক দায়িত্ব',
              route: AppRouter.dutyRoster,
            ),
            RoleActionItem(
              icon: Icons.restaurant_outlined,
              title: 'খাবারের পালা তালিকা',
              subtitle: 'পরিবারভিত্তিক দুপুর ও রাতের খাবার',
              route: AppRouter.mealSchedule,
            ),
            RoleActionItem(
              icon: Icons.campaign_outlined,
              title: 'কমিটির নির্দেশনা ও নোটিশ',
              subtitle: 'মসজিদ ব্যবস্থাপনার জরুরি বিজ্ঞপ্তি',
              route: AppRouter.notices,
            ),
          ],
        ),
      ],
    );
  }
}
