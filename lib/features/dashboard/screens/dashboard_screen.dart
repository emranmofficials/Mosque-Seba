import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/router/app_router.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../shared/widgets/adaptive_scaffold.dart';
import '../../../shared/widgets/app_header.dart';
import '../../../shared/widgets/card_shell.dart';
import '../../authentication/providers/auth_provider.dart';
import '../../finance/providers/finance_provider.dart';
import '../../hadith/widgets/hadith_board_widget.dart';
import '../../membership/providers/member_provider.dart';
import '../../notices/providers/notice_provider.dart';
import '../../notifications/providers/notification_provider.dart';
import '../../prayer_times/widgets/prayer_countdown_card.dart';
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
    final unreadCount = ref.watch(unreadNotificationCountProvider);
    final financeSummary = ref.watch(financeSummaryProvider);
    final totalMembers = ref.watch(memberListProvider).valueOrNull?.length ?? 0;
    final notices = ref.watch(noticeListProvider).valueOrNull ?? const [];
    final publishedNotices = notices.where((n) => n.status == 'published').toList();
    final pinnedNotices = publishedNotices.where((n) => n.isPinned).toList();
    final latestNotice = pinnedNotices.isNotEmpty
        ? pinnedNotices.first
        : (publishedNotices.isNotEmpty ? publishedNotices.first : null);

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
        if (index == 0) {
          setState(() => _navIndex = 0);
          return;
        }
        if (index == 1) context.push(AppRouter.members);
        if (index == 2) context.push(AppRouter.finance);
        if (index == 3) context.push(AppRouter.notices);
        if (index == 4) context.push(AppRouter.more);
      },
      destinations: destinations,
      appBar: AppHeader(
        userName: user?.memberName ?? 'মুহতারাম সভাপতি',
        designation: currentRole.titleBn,
        unreadNotifications: unreadCount,
        onNotificationTap: () => context.push(AppRouter.notifications),
        onProfileTap: () => context.push(AppRouter.profile),
      ),
      body: RefreshIndicator(
        onRefresh: () async {
          await Future.wait([
            ref.read(authNotifierProvider.notifier).checkSession(),
            ref.read(financeListProvider.notifier).loadTransactions(),
            ref.read(memberListProvider.notifier).loadMembers(),
            ref.read(noticeListProvider.notifier).loadNotices(),
            ref.read(notificationListProvider.notifier).loadNotifications(),
          ]);
        },
        child: ListView(
          padding: const EdgeInsets.all(MosqueContentPadding.screenHorizontal),
          children: [
            // 1. Mosque Identity Hero Card
            DashboardHeroCard(
              mosqueNameBn: user?.mosqueNameBn.isNotEmpty == true
                  ? user!.mosqueNameBn
                  : 'মসজিদ সেবা পোর্টাল',
              address: user?.mosqueAddress.isNotEmpty == true
                  ? user!.mosqueAddress
                  : currentRole.titleBn,
              committeeId: user?.committeeId.isNotEmpty == true ? user!.committeeId : '—',
              establishedYear: user?.establishedYear ?? DateTime.now().year,
              onProfileTap: () => context.push(AppRouter.profile),
            ),
            const SizedBox(height: MosqueSpace.md),

            // 2. Prayer Times Live Countdown Card
            PrayerCountdownCard(
              onTap: () => context.push(AppRouter.prayerTimes),
            ),
            const SizedBox(height: MosqueSpace.md),

            // 3. Smart Information Board (Quotes & Reminders)
            SmartBoardCard(
              categoryBadge: latestNotice == null
                  ? 'জুমার বিশেষ নির্দেশনা'
                  : (latestNotice.isUrgent
                      ? 'জরুরি বিজ্ঞপ্তি'
                      : (latestNotice.isPinned ? 'পিন করা নোটিশ' : 'সর্বশেষ নোটিশ')),
              title: latestNotice?.title ?? 'খুতবা শুরু হলে নীরবতা পালন করুন',
              message: latestNotice?.content ??
                  'খুতবা চলাকালীন মোবাইল সাইলেন্ট রাখুন এবং মনোযোগ সহকারে বয়ান শ্রবণ করুন।',
              onTap: () => context.push(AppRouter.notices),
            ),
            const SizedBox(height: MosqueSpace.md),

            // 4. Quick Actions Panel (4 Administrative Launchers)
            QuickActionsGrid(
              onReceiveChanda: () => context.push(AppRouter.chandaLedger),
              onAddTransaction: () => context.push(AppRouter.finance),
              onCashboxCounter: () => context.push(AppRouter.cashbox),
              onPublishNotice: () => context.push(AppRouter.notices),
            ),
            const SizedBox(height: MosqueSpace.md),

            // 5. Daily Sahih Hadith Board
            const HadithBoardWidget(),
            const SizedBox(height: MosqueSpace.md),

            // 6. Financial & Membership KPI Cards
            KpiSummaryGrid(
              totalMembers: totalMembers,
              cashBalance: financeSummary.cashInHand,
              bankBalance: financeSummary.bankBalance,
              pendingApprovals: financeSummary.pendingVouchersCount,
              onFinanceTap: () => context.push(AppRouter.finance),
              onMembersTap: () => context.push(AppRouter.members),
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
