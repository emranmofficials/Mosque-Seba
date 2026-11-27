import 'package:flutter/material.dart';

import '../../../../features/role_permissions/models/committee_role.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../../router/app_router.dart';
import '../shared/role_shell_base.dart';

/// Dedicated interface shell for Madrasa Teacher (মাদরাসা ওস্তাদ).
class MadrasaTeacherShell extends StatelessWidget {
  const MadrasaTeacherShell({super.key});

  @override
  Widget build(BuildContext context) {
    return const RoleShellScaffold(
      expectedRole: CommitteeRole.madrasaTeacher,
      showPrayerCard: true,
      showSmartBoard: false,
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
            icon: Icons.school_outlined,
            selectedIcon: Icons.school,
            label: 'মাদরাসা',
          ),
          route: AppRouter.madrasa,
        ),
        RoleNavTarget(
          destination: AdaptiveNavigationDestination(
            icon: Icons.fact_check_outlined,
            selectedIcon: Icons.fact_check,
            label: 'হাজিরা',
          ),
          route: AppRouter.madrasaAttendance,
        ),
        RoleNavTarget(
          destination: AdaptiveNavigationDestination(
            icon: Icons.menu_book_outlined,
            selectedIcon: Icons.menu_book,
            label: 'হিফজ',
          ),
          route: AppRouter.madrasaHifz,
        ),
      ],
      sections: [
        RoleActionSection(
          title: 'মক্তব, হিফজ ও পরীক্ষা পরিচালনা',
          items: [
            RoleActionItem(
              icon: Icons.people_outline,
              title: 'শিক্ষার্থী ডিরেক্টরি',
              subtitle: 'ভর্তি, অভিভাবক যোগাযোগ ও তালিকা',
              route: AppRouter.madrasaStudents,
            ),
            RoleActionItem(
              icon: Icons.fact_check_outlined,
              title: 'দৈনিক হাজিরা গ্রহণ',
              subtitle: 'উপস্থিতি রেকর্ড ও পূর্বের ইতিহাস',
              route: AppRouter.madrasaAttendance,
            ),
            RoleActionItem(
              icon: Icons.menu_book_outlined,
              title: 'কোরআন ও হিফজ ট্র্যাকিং',
              subtitle: 'সবক, পারা-পৃষ্ঠা ও স্টার মূল্যায়ন',
              route: AppRouter.madrasaHifz,
            ),
            RoleActionItem(
              icon: Icons.grading_outlined,
              title: 'পরীক্ষা ও ৩৬০° রিপোর্ট কার্ড',
              subtitle: 'নম্বর এন্ট্রি ও প্রগ্রেস মূল্যায়ন',
              route: AppRouter.madrasaExams,
            ),
          ],
        ),
      ],
    );
  }
}
