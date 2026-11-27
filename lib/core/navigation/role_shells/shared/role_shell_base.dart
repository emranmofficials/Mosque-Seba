import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../../features/authentication/providers/auth_provider.dart';
import '../../../../features/dashboard/widgets/dashboard_hero_card.dart';
import '../../../../features/dashboard/widgets/kpi_summary_grid.dart';
import '../../../../features/dashboard/widgets/quick_actions_grid.dart';
import '../../../../features/dashboard/widgets/smart_board_card.dart';
import '../../../../features/finance/providers/finance_provider.dart';
import '../../../../features/hadith/widgets/hadith_board_widget.dart';
import '../../../../features/membership/providers/member_provider.dart';
import '../../../../features/notices/providers/notice_provider.dart';
import '../../../../features/notifications/providers/notification_provider.dart';
import '../../../../features/prayer_times/widgets/prayer_countdown_card.dart';
import '../../../../features/role_permissions/models/committee_role.dart';
import '../../../../features/role_permissions/providers/role_permissions_provider.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../../../shared/widgets/app_header.dart';
import '../../../../shared/widgets/card_shell.dart';
import '../../../constants/app_constants.dart';
import '../../../router/app_router.dart';
import '../../../theme/app_dimens.dart';

/// A navigation tab item bound to a target route for a specific role shell.
class RoleNavTarget {
  final AdaptiveNavigationDestination destination;
  final String route;

  const RoleNavTarget({
    required this.destination,
    required this.route,
  });
}

/// An actionable module shortcut tailored to a role's duties.
class RoleActionItem {
  final IconData icon;
  final String title;
  final String subtitle;
  final String route;

  const RoleActionItem({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.route,
  });
}

/// A grouped section of duty-specific actions shown in a role shell.
class RoleActionSection {
  final String title;
  final List<RoleActionItem> items;

  const RoleActionSection({
    required this.title,
    required this.items,
  });
}

/// Shared layout shell composed by each of the 18 role-specific shells.
class RoleShellScaffold extends ConsumerStatefulWidget {
  final CommitteeRole expectedRole;
  final List<RoleNavTarget> navTargets;
  final List<RoleActionSection> sections;
  final bool showKpiSummary;
  final bool showQuickActions;
  final bool showPrayerCard;
  final bool showSmartBoard;
  final bool showHadithBoard;

  const RoleShellScaffold({
    super.key,
    required this.expectedRole,
    required this.navTargets,
    required this.sections,
    this.showKpiSummary = false,
    this.showQuickActions = false,
    this.showPrayerCard = true,
    this.showSmartBoard = true,
    this.showHadithBoard = false,
  });

  @override
  ConsumerState<RoleShellScaffold> createState() => _RoleShellScaffoldState();
}

class _RoleShellScaffoldState extends ConsumerState<RoleShellScaffold> {
  int _navIndex = 0;

  @override
  Widget build(BuildContext context) {
    final user = ref.watch(authNotifierProvider).valueOrNull;
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

    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;
    final textTheme = theme.textTheme;

    return AdaptiveScaffold(
      currentIndex: _navIndex,
      onNavigationIndexChanged: (index) {
        if (index == 0) {
          setState(() => _navIndex = 0);
          return;
        }
        if (index > 0 && index < widget.navTargets.length) {
          context.push(widget.navTargets[index].route);
        }
      },
      destinations: widget.navTargets.map((t) => t.destination).toList(),
      appBar: AppHeader(
        userName: user?.memberName ?? currentRole.titleBn,
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
        child: LayoutBuilder(
          builder: (context, constraints) {
            final isTablet = constraints.maxWidth >= AppConstants.tabletBreakpoint;

            final heroCard = DashboardHeroCard(
              mosqueNameBn: user?.mosqueNameBn.isNotEmpty == true
                  ? user!.mosqueNameBn
                  : 'মসজিদ সেবা পোর্টাল',
              address: user?.mosqueAddress.isNotEmpty == true
                  ? user!.mosqueAddress
                  : currentRole.titleBn,
              committeeId:
                  user?.committeeId.isNotEmpty == true ? user!.committeeId : '—',
              establishedYear: user?.establishedYear ?? DateTime.now().year,
              onProfileTap: () => context.push(AppRouter.profile),
            );

            final roleDutyBanner = CardShell(
              padding: const EdgeInsets.symmetric(
                horizontal: MosqueSpace.md,
                vertical: MosqueSpace.sm,
              ),
              child: Row(
                children: [
                  CircleAvatar(
                    backgroundColor: colorScheme.primaryContainer,
                    foregroundColor: colorScheme.onPrimaryContainer,
                    child: const Icon(Icons.verified_user_outlined),
                  ),
                  const SizedBox(width: MosqueSpace.sm),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          widget.expectedRole.titleBn,
                          style: textTheme.titleSmall?.copyWith(
                            fontWeight: FontWeight.bold,
                            color: colorScheme.primary,
                          ),
                        ),
                        const SizedBox(height: 2),
                        Text(
                          widget.expectedRole.shortSummaryBn,
                          style: textTheme.bodySmall?.copyWith(
                            color: colorScheme.onSurfaceVariant,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            );

            final kpiGrid = widget.showKpiSummary
                ? KpiSummaryGrid(
                    totalMembers: totalMembers,
                    cashBalance: financeSummary.cashInHand,
                    bankBalance: financeSummary.bankBalance,
                    pendingApprovals: financeSummary.pendingVouchersCount,
                    onFinanceTap: () => context.push(AppRouter.finance),
                    onMembersTap: () => context.push(AppRouter.members),
                  )
                : null;

            final prayerCard = widget.showPrayerCard
                ? PrayerCountdownCard(
                    onTap: () => context.push(AppRouter.prayerTimes),
                  )
                : null;

            final smartBoard = widget.showSmartBoard
                ? SmartBoardCard(
                    categoryBadge: latestNotice == null
                        ? 'জুমার বিশেষ নির্দেশনা'
                        : (latestNotice.isUrgent
                            ? 'জরুরি বিজ্ঞপ্তি'
                            : (latestNotice.isPinned
                                ? 'পিন করা নোটিশ'
                                : 'সর্বশেষ নোটিশ')),
                    title:
                        latestNotice?.title ?? 'খুতবা শুরু হলে নীরবতা পালন করুন',
                    message: latestNotice?.content ??
                        'খুতবা চলাকালীন মোবাইল সাইলেন্ট রাখুন এবং মনোযোগ সহকারে বয়ান শ্রবণ করুন।',
                    onTap: () => context.push(AppRouter.notices),
                  )
                : null;

            final quickActions = widget.showQuickActions
                ? QuickActionsGrid(
                    onReceiveChanda: () => context.push(AppRouter.chandaLedger),
                    onAddTransaction: () => context.push(AppRouter.finance),
                    onCashboxCounter: () => context.push(AppRouter.cashbox),
                    onPublishNotice: () => context.push(AppRouter.notices),
                  )
                : null;

            final sectionWidgets = widget.sections
                .map((section) => _RoleSectionCard(section: section))
                .toList();

            return Center(
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 1150),
                child: isTablet
                    ? SingleChildScrollView(
                        padding: const EdgeInsets.all(
                          MosqueContentPadding.screenHorizontal,
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.stretch,
                          children: [
                            if (kpiGrid != null) ...[
                              kpiGrid,
                              const SizedBox(height: MosqueSpace.md),
                            ],
                            Row(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Expanded(
                                  flex: 6,
                                  child: Column(
                                    children: [
                                      heroCard,
                                      const SizedBox(height: MosqueSpace.md),
                                      roleDutyBanner,
                                      if (prayerCard != null) ...[
                                        const SizedBox(height: MosqueSpace.md),
                                        prayerCard,
                                      ],
                                      if (smartBoard != null) ...[
                                        const SizedBox(height: MosqueSpace.md),
                                        smartBoard,
                                      ],
                                    ],
                                  ),
                                ),
                                const SizedBox(width: MosqueSpace.md),
                                Expanded(
                                  flex: 5,
                                  child: Column(
                                    children: [
                                      if (quickActions != null) ...[
                                        quickActions,
                                        const SizedBox(height: MosqueSpace.md),
                                      ],
                                      ...sectionWidgets.expand(
                                        (w) => [
                                          w,
                                          const SizedBox(height: MosqueSpace.md),
                                        ],
                                      ),
                                      if (widget.showHadithBoard)
                                        const HadithBoardWidget(),
                                    ],
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: MosqueSpace.xxl),
                          ],
                        ),
                      )
                    : SingleChildScrollView(
                        padding: const EdgeInsets.all(
                          MosqueContentPadding.screenHorizontal,
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.stretch,
                          children: [
                            heroCard,
                            const SizedBox(height: MosqueSpace.md),
                            roleDutyBanner,
                            if (kpiGrid != null) ...[
                              const SizedBox(height: MosqueSpace.md),
                              kpiGrid,
                            ],
                            if (prayerCard != null) ...[
                              const SizedBox(height: MosqueSpace.md),
                              prayerCard,
                            ],
                            if (quickActions != null) ...[
                              const SizedBox(height: MosqueSpace.md),
                              quickActions,
                            ],
                            ...sectionWidgets.expand(
                              (w) => [
                                const SizedBox(height: MosqueSpace.md),
                                w,
                              ],
                            ),
                            if (smartBoard != null) ...[
                              const SizedBox(height: MosqueSpace.md),
                              smartBoard,
                            ],
                            if (widget.showHadithBoard) ...[
                              const SizedBox(height: MosqueSpace.md),
                              const HadithBoardWidget(),
                            ],
                            const SizedBox(height: MosqueSpace.xxl),
                          ],
                        ),
                      ),
              ),
            );
          },
        ),
      ),
    );
  }
}

class _RoleSectionCard extends StatelessWidget {
  final RoleActionSection section;

  const _RoleSectionCard({required this.section});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;
    final textTheme = theme.textTheme;

    return CardShell(
      padding: const EdgeInsets.all(MosqueSpace.md),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            section.title,
            style: textTheme.titleSmall?.copyWith(
              fontWeight: FontWeight.bold,
              color: colorScheme.primary,
            ),
          ),
          const SizedBox(height: MosqueSpace.sm),
          ...section.items.map(
            (item) => ListTile(
              contentPadding: EdgeInsets.zero,
              leading: CircleAvatar(
                backgroundColor: colorScheme.secondaryContainer,
                foregroundColor: colorScheme.onSecondaryContainer,
                child: Icon(item.icon, size: 20),
              ),
              title: Text(
                item.title,
                style: textTheme.bodyMedium?.copyWith(fontWeight: FontWeight.w600),
              ),
              subtitle: Text(
                item.subtitle,
                style: textTheme.bodySmall?.copyWith(
                  color: colorScheme.onSurfaceVariant,
                ),
              ),
              trailing: const Icon(Icons.chevron_right),
              onTap: () => context.push(item.route),
            ),
          ),
        ],
      ),
    );
  }
}
