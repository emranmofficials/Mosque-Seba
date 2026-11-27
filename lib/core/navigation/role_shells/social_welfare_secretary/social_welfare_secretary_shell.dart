import 'package:flutter/material.dart';
import '../../../../features/role_permissions/models/committee_role.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../../router/app_router.dart';
import '../shared/role_shell_base.dart';

/// Dedicated interface shell for Social Welfare & Religious Education Secretary — Tier 2.
class SocialWelfareSecretaryShell extends StatelessWidget {
  const SocialWelfareSecretaryShell({super.key});

  @override
  Widget build(BuildContext context) {
    return const RoleShellScaffold(
      expectedRole: CommitteeRole.welfareEducationSecretary,
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
            icon: Icons.volunteer_activism_outlined,
            selectedIcon: Icons.volunteer_activism,
            label: 'যাকাত ও কল্যাণ',
          ),
          route: AppRouter.welfare,
        ),
        RoleNavTarget(
          destination: AdaptiveNavigationDestination(
            icon: Icons.school_outlined,
            selectedIcon: Icons.school,
            label: 'মাদরাসা',
          ),
          route: AppRouter.madrasa,
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
          title: 'মক্তব, হিফজ ও সমাজকল্যাণ',
          items: [
            RoleActionItem(
              icon: Icons.volunteer_activism_outlined,
              title: 'যাকাত ও কল্যাণ তহবিল',
              subtitle: 'চিকিৎসা, বিধবা-এতিম ও শিক্ষা অনুদান',
              route: AppRouter.welfare,
            ),
            RoleActionItem(
              icon: Icons.school_outlined,
              title: 'মাদরাসা ও মক্তব তদারকি',
              subtitle: 'ভর্তি, হাজিরা, হিফজ ও পরীক্ষার ফলাফল',
              route: AppRouter.madrasa,
            ),
            RoleActionItem(
              icon: Icons.payments_outlined,
              title: 'মাদরাসা ফি ও রশিদ',
              subtitle: 'শিক্ষার্থী ফি ও বৃত্তি সমন্বয়',
              route: AppRouter.madrasaFees,
            ),
            RoleActionItem(
              icon: Icons.notifications_active_outlined,
              title: 'জানাজা ও মানবিক সহায়তা',
              subtitle: 'জরুরি জানাজা বার্তা ও ত্রাণ কার্যক্রম',
              route: AppRouter.janazaAlerts,
            ),
          ],
        ),
      ],
    );
  }
}
