import 'package:flutter/material.dart';
import '../../../../features/role_permissions/models/committee_role.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../../router/app_router.dart';
import '../shared/role_shell_base.dart';

/// Dedicated interface shell for General Member / Musalli (সাধারণ সদস্য / মুসল্লি).
class GeneralMemberShell extends StatelessWidget {
  const GeneralMemberShell({super.key});

  @override
  Widget build(BuildContext context) {
    return const RoleShellScaffold(
      expectedRole: CommitteeRole.generalMember,
      showPrayerCard: true,
      showSmartBoard: true,
      showHadithBoard: true,
      navTargets: [
        RoleNavTarget(
          destination: AdaptiveNavigationDestination(
            icon: Icons.dashboard_outlined,
            selectedIcon: Icons.dashboard,
            label: 'হোম',
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
            icon: Icons.receipt_long_outlined,
            selectedIcon: Icons.receipt_long,
            label: 'চাঁদা ও দান',
          ),
          route: AppRouter.chandaLedger,
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
          title: 'মুসল্লি সেবা ও অংশগ্রহণ',
          items: [
            RoleActionItem(
              icon: Icons.receipt_long_outlined,
              title: 'আমার চাঁদা ও দান',
              subtitle: 'মাসিক চাঁদার হিসাব ও ডিজিটাল রশিদ',
              route: AppRouter.chandaLedger,
            ),
            RoleActionItem(
              icon: Icons.nights_stay_outlined,
              title: 'রমজান ও ইসলামিক ক্যালেন্ডার',
              subtitle: 'সেহরি-ইফতার কাউন্টডাউন ও ১৩টি দিবস',
              route: AppRouter.ramadanHub,
            ),
            RoleActionItem(
              icon: Icons.event_outlined,
              title: 'ওয়াজ মাহফিল ও অনুষ্ঠান',
              subtitle: 'আসন্ন ধর্মীয় প্রোগ্রাম ও জানাজা তথ্য',
              route: AppRouter.events,
            ),
            RoleActionItem(
              icon: Icons.how_to_vote_outlined,
              title: 'কমিটি নির্বাচন ও গেজেট',
              subtitle: 'প্রার্থী তালিকা, ভোটদান ও ফলাফল',
              route: AppRouter.elections,
            ),
          ],
        ),
      ],
    );
  }
}
