import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/router/app_router.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../shared/widgets/adaptive_scaffold.dart';
import '../../../shared/widgets/app_header.dart';
import '../../authentication/providers/auth_provider.dart';
import '../../hadith/widgets/hadith_board_widget.dart';
import '../../prayer_times/widgets/prayer_countdown_card.dart';
import '../../role_permissions/models/committee_role.dart';
import '../../role_permissions/providers/role_permissions_provider.dart';
import '../widgets/dashboard_hero_card.dart';
import '../widgets/kpi_summary_grid.dart';
import '../widgets/quick_actions_grid.dart';
import '../widgets/smart_board_card.dart';

/// Centralized Smart Dashboard Screen (DashboardScreen.kt Parity)
class DashboardScreen extends ConsumerStatefulWidget {
  const DashboardScreen({super.key});

  @override
  ConsumerState<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends ConsumerState<DashboardScreen> {
  int _navIndex = 0;

  @override
  Widget build(BuildContext context) {
    final authState = ref.watch(authNotifierProvider);
    final user = authState.valueOrNull;
    final currentRole = ref.watch(currentRoleProvider);

    // 5 Primary Navigation Destinations based on UI-01 & UI-02 profiles
    final destinations = [
      const AdaptiveNavigationDestination(
        icon: Icons.dashboard_outlined,
        selectedIcon: Icons.dashboard,
        label: 'ড্যাশবোর্ড',
      ),
      const AdaptiveNavigationDestination(
        icon: Icons.people_outline,
        selectedIcon: Icons.people,
        label: 'সদস্য',
      ),
      const AdaptiveNavigationDestination(
        icon: Icons.account_balance_wallet_outlined,
        selectedIcon: Icons.account_balance_wallet,
        label: 'ফাইন্যান্স',
      ),
      const AdaptiveNavigationDestination(
        icon: Icons.campaign_outlined,
        selectedIcon: Icons.campaign,
        label: 'নোটিশ',
      ),
      const AdaptiveNavigationDestination(
        icon: Icons.grid_view_outlined,
        selectedIcon: Icons.grid_view,
        label: 'অন্যান্য',
      ),
    ];

    return AdaptiveScaffold(
      currentIndex: _navIndex,
      onNavigationIndexChanged: (index) {
        setState(() => _navIndex = index);
        if (index == 1) context.go(AppRouter.members);
        if (index == 2) context.go(AppRouter.finance);
        if (index == 3) context.go(AppRouter.notices);
        if (index == 4) context.go(AppRouter.more);
      },
      destinations: destinations,
      appBar: AppHeader(
        userName: user?.memberName ?? 'মুহতারাম সভাপতি',
        designation: currentRole.titleBn,
        unreadNotifications: 3,
        onNotificationTap: () => context.go(AppRouter.notices),
        onProfileTap: () => context.go(AppRouter.profile),
      ),
      body: RefreshIndicator(
        onRefresh: () async {
          await ref.read(authNotifierProvider.notifier).checkSession();
        },
        child: ListView(
          padding: const EdgeInsets.all(MosqueContentPadding.screenHorizontal),
          children: [
            // 1. Mosque Identity Hero Card
            DashboardHeroCard(
              mosqueNameBn: user?.mosqueNameBn.isNotEmpty == true
                  ? user!.mosqueNameBn
                  : 'বায়তুল মুকাররম মডেল জামে মসজিদ',
              address: 'মিরপুর, ঢাকা',
              committeeId: user?.committeeId ?? 'MS-2026',
              onProfileTap: () => context.go(AppRouter.profile),
            ),
            const SizedBox(height: MosqueSpace.md),

            // 2. Prayer Times Live Countdown Card
            PrayerCountdownCard(
              onTap: () {
                Navigator.of(context).push(
                  MaterialPageRoute(builder: (_) => const Scaffold(body: Center(child: Text('নামাজের বিস্তারিত')))),
                );
              },
            ),
            const SizedBox(height: MosqueSpace.md),

            // 3. Smart Information Board (Quotes & Reminders)
            SmartBoardCard(
              onTap: () => context.go(AppRouter.notices),
            ),
            const SizedBox(height: MosqueSpace.md),

            // 4. Quick Actions Panel (4 Administrative Launchers)
            QuickActionsGrid(
              onReceiveChanda: () => context.go(AppRouter.finance),
              onAddTransaction: () => context.go(AppRouter.finance),
              onCashboxCounter: () => context.go(AppRouter.cashbox),
              onPublishNotice: () => context.go(AppRouter.notices),
            ),
            const SizedBox(height: MosqueSpace.md),

            // 5. Daily Sahih Hadith Board
            const HadithBoardWidget(),
            const SizedBox(height: MosqueSpace.md),

            // 6. Financial & Membership KPI Cards
            KpiSummaryGrid(
              onFinanceTap: () => context.go(AppRouter.finance),
              onMembersTap: () => context.go(AppRouter.members),
            ),
            const SizedBox(height: MosqueSpace.md),

            // 7. Madrasa & Maktab Education Highlight Banner
            CardShell(
              onTap: () => context.push(AppRouter.madrasa),
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
              child: Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(
                      color: AppColors.emeraldPrimary.withOpacity(0.12),
                      shape: BoxShape.circle,
                    ),
                    child: const Icon(Icons.school, color: AppColors.emeraldPrimary, size: 22),
                  ),
                  const SizedBox(width: 12),
                  const Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'মাদরাসা ও মক্তব শিক্ষা হাব',
                          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                        ),
                        Text(
                          'শিক্ষার্থী, দৈনিক হাজিরা, হিফজ ট্র্যাকার ও ফি আদায়',
                          style: TextStyle(fontSize: 11, color: Colors.grey),
                        ),
                      ],
                    ),
                  ),
                  const Icon(Icons.arrow_forward_ios, size: 14, color: Colors.grey),
                ],
              ),
            ),
            const SizedBox(height: MosqueSpace.xxl),
          ],
        ),
      ),
    );
  }
}
