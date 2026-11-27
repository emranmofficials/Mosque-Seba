import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../../features/authentication/models/auth_user.dart';
import '../../features/authentication/screens/login_screen.dart';
import '../../features/authentication/screens/registration_screen.dart';
import '../../features/authentication/screens/splash_screen.dart';
import '../../features/cashbox/screens/cashbox_history_screen.dart';
import '../navigation/role_shells/role_shell_resolver.dart';
import '../../features/duty_roster/screens/duty_roster_screen.dart';
import '../../features/elections/screens/election_screen.dart';
import '../../features/finance/screens/finance_dashboard_screen.dart';
import '../../features/finance/screens/transaction_list_screen.dart';
import '../../features/islamic_calendar/screens/islamic_calendar_screen.dart';
import '../../features/janaza_alerts/screens/janaza_alert_screen.dart';
import '../../features/ai_assistant/screens/khutbah_ideas_screen.dart';
import '../../features/app_security/screens/security_dashboard_screen.dart';
import '../../features/cloud_sync/screens/cloud_sync_screen.dart';
import '../../features/official_signature/screens/seal_signature_management_screen.dart';
import '../../features/printing_pdf/screens/document_print_hub_screen.dart';
import '../../features/events/models/mosque_event_model.dart';
import '../../features/events/screens/event_list_screen.dart';
import '../../features/examinations/screens/exam_list_screen.dart';
import '../../features/hifz_tracking/screens/hifz_tracking_screen.dart';
import '../../features/madrasa/screens/madrasa_dashboard_screen.dart';
import '../../features/madrasa/screens/student_directory_screen.dart';
import '../../features/madrasa_attendance/screens/madrasa_attendance_screen.dart';
import '../../features/madrasa_fees/screens/fee_collection_screen.dart';
import '../../features/more/screens/more_menu_screen.dart';
import '../../features/poster_ai/screens/poster_designer_screen.dart';
import '../../features/welfare_zakat/screens/welfare_dashboard_screen.dart';
import '../../features/meal_schedule/screens/meal_schedule_screen.dart';
import '../../features/meetings/screens/meeting_list_screen.dart';
import '../../features/membership/models/member_model.dart';
import '../../features/membership/screens/member_list_screen.dart';
import '../../features/membership/screens/member_profile_screen.dart';
import '../../features/notices/screens/notice_list_screen.dart';
import '../../features/notifications/screens/notification_center_screen.dart';
import '../../features/prayer_times/screens/prayer_schedule_screen.dart';
import '../../features/profile/screens/mosque_profile_screen.dart';
import '../../features/ramadan/screens/ramadan_hub_screen.dart';
import '../../features/role_permissions/models/committee_role.dart';
import '../../features/role_permissions/models/permission_catalog.dart';
import '../../features/role_permissions/services/permission_engine.dart';
import '../../features/staff/screens/payroll_history_screen.dart';
import '../../features/staff/screens/staff_list_screen.dart';
import '../../features/subscriptions/screens/chanda_ledger_screen.dart';
import '../../features/waqf_assets/models/waqf_asset_model.dart';
import '../../features/waqf_assets/screens/waqf_asset_detail_screen.dart';
import '../../features/waqf_assets/screens/waqf_asset_list_screen.dart';

/// Centralized Declarative Router with Role-Based Access Control (RBAC) Route Guards
class AppRouter {
  static const String splash = '/';
  static const String login = '/login';
  static const String register = '/register';
  static const String dashboard = '/dashboard';
  static const String members = '/members';
  static const String memberProfile = '/member-profile';
  static const String waqfAssets = '/waqf-assets';
  static const String waqfAssetsDetail = '/waqf-assets-detail';
  static const String chandaLedger = '/chanda-ledger';
  static const String finance = '/finance';
  static const String financeTransactions = '/finance-transactions';
  static const String cashbox = '/cashbox';
  static const String staff = '/staff';
  static const String staffPayroll = '/staff-payroll';
  static const String dutyRoster = '/duty-roster';
  static const String mealSchedule = '/meal-schedule';
  static const String notices = '/notices';
  static const String notifications = '/notifications';
  static const String meetings = '/meetings';
  static const String janazaAlerts = '/janaza-alerts';
  static const String elections = '/elections';
  static const String more = '/more';
  static const String madrasa = '/madrasa';
  static const String madrasaStudents = '/madrasa/students';
  static const String madrasaAttendance = '/madrasa/attendance';
  static const String madrasaHifz = '/madrasa/hifz';
  static const String madrasaExams = '/madrasa/exams';
  static const String madrasaFees = '/madrasa/fees';
  static const String events = '/events';
  static const String posterAi = '/poster-ai';
  static const String welfare = '/welfare';
  static const String khutbahIdeas = '/khutbah-ideas';
  static const String sealSignature = '/seal-signature';
  static const String printHub = '/print-hub';
  static const String appSecurity = '/app-security';
  static const String cloudSync = '/cloud-sync';
  static const String profile = '/profile';
  static const String prayerTimes = '/prayer-times';
  static const String islamicCalendar = '/islamic-calendar';
  static const String ramadanHub = '/ramadan-hub';

  static final GlobalKey<NavigatorState> rootNavigatorKey =
      GlobalKey<NavigatorState>(debugLabel: 'root');

  static bool _isRouteAuthorized(CommitteeRole role, String location) {
    if (role == CommitteeRole.president) return true;

    switch (location) {
      case appSecurity:
      case cloudSync:
        return role.canEditMosqueProfile();
      case sealSignature:
        return role.canManageOfficeDocs() || role == CommitteeRole.treasurer;
      case staffPayroll:
        return PermissionEngine.hasPermission(role, PermissionCatalog.staffSalaryView);
      case cashbox:
        return PermissionEngine.hasPermission(role, PermissionCatalog.cashboxManage) ||
            PermissionEngine.hasPermission(role, PermissionCatalog.cashboxWitness);
      case madrasaAttendance:
      case madrasaHifz:
        return PermissionEngine.hasPermission(role, PermissionCatalog.madrasaManage);
      case madrasaFees:
        return PermissionEngine.hasPermission(role, PermissionCatalog.feeReceive);
      case posterAi:
        return role.canPublishNotices() || role.canManageSmartBoard();
      default:
        return true;
    }
  }

  static GoRouter createRouter({
    required AuthUser? Function() getAuthUser,
    Listenable? refreshListenable,
    String initialLocation = splash,
  }) {
    return GoRouter(
      navigatorKey: rootNavigatorKey,
      initialLocation: initialLocation,
      refreshListenable: refreshListenable,
      redirect: (context, state) {
        final authUser = getAuthUser();
        final isAuth = authUser != null && authUser.isAuthenticated;
        final isAuthRoute = state.matchedLocation == login || state.matchedLocation == register;
        final isSplash = state.matchedLocation == splash;

        if (isSplash) return null;

        if (!isAuth && !isAuthRoute) {
          return login;
        }

        if (isAuth && isAuthRoute) {
          return dashboard;
        }

        if (isAuth) {
          final role = CommitteeRole.fromCode(authUser.roleCode);
          if (!_isRouteAuthorized(role, state.matchedLocation)) {
            return dashboard;
          }
        }

        return null;
      },
      routes: [
        GoRoute(
          path: splash,
          name: 'splash',
          builder: (context, state) => const SplashScreen(),
        ),
        GoRoute(
          path: login,
          name: 'login',
          builder: (context, state) => const LoginScreen(),
        ),
        GoRoute(
          path: register,
          name: 'register',
          builder: (context, state) => const RegistrationScreen(),
        ),
        GoRoute(
          path: dashboard,
          name: 'dashboard',
          builder: (context, state) => const RoleShellResolver(),
        ),
        GoRoute(
          path: prayerTimes,
          name: 'prayerTimes',
          builder: (context, state) => const PrayerScheduleScreen(),
        ),
        GoRoute(
          path: islamicCalendar,
          name: 'islamicCalendar',
          builder: (context, state) => const IslamicCalendarScreen(),
        ),
        GoRoute(
          path: ramadanHub,
          name: 'ramadanHub',
          builder: (context, state) => const RamadanHubScreen(),
        ),
        GoRoute(
          path: members,
          name: 'members',
          builder: (context, state) => const MemberListScreen(),
        ),
        GoRoute(
          path: memberProfile,
          name: 'memberProfile',
          builder: (context, state) {
            final member = state.extra as MemberModel?;
            if (member == null) return const MemberListScreen();
            return MemberProfileScreen(member: member);
          },
        ),
        GoRoute(
          path: waqfAssets,
          name: 'waqfAssets',
          builder: (context, state) => const WaqfAssetListScreen(),
        ),
        GoRoute(
          path: waqfAssetsDetail,
          name: 'waqfAssetsDetail',
          builder: (context, state) {
            final asset = state.extra as WaqfAssetModel?;
            if (asset == null) return const WaqfAssetListScreen();
            return WaqfAssetDetailScreen(asset: asset);
          },
        ),
        GoRoute(
          path: chandaLedger,
          name: 'chandaLedger',
          builder: (context, state) {
            final targetMember = state.extra as MemberModel?;
            return ChandaLedgerScreen(targetMember: targetMember);
          },
        ),
        GoRoute(
          path: finance,
          name: 'finance',
          builder: (context, state) => const FinanceDashboardScreen(),
        ),
        GoRoute(
          path: financeTransactions,
          name: 'financeTransactions',
          builder: (context, state) => const TransactionListScreen(),
        ),
        GoRoute(
          path: cashbox,
          name: 'cashbox',
          builder: (context, state) => const CashboxHistoryScreen(),
        ),
        GoRoute(
          path: staff,
          name: 'staff',
          builder: (context, state) => const StaffListScreen(),
        ),
        GoRoute(
          path: staffPayroll,
          name: 'staffPayroll',
          builder: (context, state) => const PayrollHistoryScreen(),
        ),
        GoRoute(
          path: dutyRoster,
          name: 'dutyRoster',
          builder: (context, state) => const DutyRosterScreen(),
        ),
        GoRoute(
          path: mealSchedule,
          name: 'mealSchedule',
          builder: (context, state) => const MealScheduleScreen(),
        ),
        GoRoute(
          path: notices,
          name: 'notices',
          builder: (context, state) => const NoticeListScreen(),
        ),
        GoRoute(
          path: notifications,
          name: 'notifications',
          builder: (context, state) => const NotificationCenterScreen(),
        ),
        GoRoute(
          path: meetings,
          name: 'meetings',
          builder: (context, state) => const MeetingListScreen(),
        ),
        GoRoute(
          path: janazaAlerts,
          name: 'janazaAlerts',
          builder: (context, state) => const JanazaAlertScreen(),
        ),
        GoRoute(
          path: elections,
          name: 'elections',
          builder: (context, state) => const ElectionScreen(),
        ),
        GoRoute(
          path: madrasa,
          name: 'madrasa',
          builder: (context, state) => const MadrasaDashboardScreen(),
        ),
        GoRoute(
          path: madrasaStudents,
          name: 'madrasaStudents',
          builder: (context, state) => const StudentDirectoryScreen(),
        ),
        GoRoute(
          path: madrasaAttendance,
          name: 'madrasaAttendance',
          builder: (context, state) => const MadrasaAttendanceScreen(),
        ),
        GoRoute(
          path: madrasaHifz,
          name: 'madrasaHifz',
          builder: (context, state) => const HifzTrackingScreen(),
        ),
        GoRoute(
          path: madrasaExams,
          name: 'madrasaExams',
          builder: (context, state) => const ExamListScreen(),
        ),
        GoRoute(
          path: madrasaFees,
          name: 'madrasaFees',
          builder: (context, state) => const FeeCollectionScreen(),
        ),
        GoRoute(
          path: events,
          name: 'events',
          builder: (context, state) => const EventListScreen(),
        ),
        GoRoute(
          path: posterAi,
          name: 'posterAi',
          builder: (context, state) {
            final event = state.extra as MosqueEventModel?;
            return PosterDesignerScreen(initialEvent: event);
          },
        ),
        GoRoute(
          path: welfare,
          name: 'welfare',
          builder: (context, state) => const WelfareDashboardScreen(),
        ),
        GoRoute(
          path: khutbahIdeas,
          name: 'khutbahIdeas',
          builder: (context, state) => const KhutbahIdeasScreen(),
        ),
        GoRoute(
          path: sealSignature,
          name: 'sealSignature',
          builder: (context, state) => const SealSignatureManagementScreen(),
        ),
        GoRoute(
          path: printHub,
          name: 'printHub',
          builder: (context, state) => const DocumentPrintHubScreen(),
        ),
        GoRoute(
          path: appSecurity,
          name: 'appSecurity',
          builder: (context, state) => const SecurityDashboardScreen(),
        ),
        GoRoute(
          path: cloudSync,
          name: 'cloudSync',
          builder: (context, state) => const CloudSyncScreen(),
        ),
        GoRoute(
          path: more,
          name: 'more',
          builder: (context, state) => const MoreMenuScreen(),
        ),
        GoRoute(
          path: profile,
          name: 'profile',
          builder: (context, state) => const MosqueProfileScreen(),
        ),
      ],
    );
  }
}
