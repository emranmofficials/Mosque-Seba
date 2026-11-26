import '../models/committee_role.dart';
import '../models/permission_catalog.dart';

enum UiProfile {
  ui01President,
  ui02Executive,
  ui03Musalli,
  ui04Staff,
  ui05Teacher,
  ui06Guardian,
  ui07SuperAdmin,
}

/// Centralized Role-Based Access Control (RBAC) Engine
class PermissionEngine {
  /// Resolves the UI Interface Profile for a given role (UIUX §2)
  static UiProfile resolveUiProfile(CommitteeRole role) {
    switch (role) {
      case CommitteeRole.president:
        return UiProfile.ui01President;
      case CommitteeRole.vicePresident:
      case CommitteeRole.generalSecretary:
      case CommitteeRole.assistantSecretary:
      case CommitteeRole.treasurer:
      case CommitteeRole.assistantTreasurer:
      case CommitteeRole.organizingSecretary:
      case CommitteeRole.publicitySecretary:
      case CommitteeRole.officeSecretary:
      case CommitteeRole.welfareEducationSecretary:
      case CommitteeRole.executiveMember:
      case CommitteeRole.advisoryBoard:
        return UiProfile.ui02Executive;
      case CommitteeRole.imam:
      case CommitteeRole.muazzin:
      case CommitteeRole.khadem:
        return UiProfile.ui04Staff;
      case CommitteeRole.generalMember:
        return UiProfile.ui03Musalli;
    }
  }

  /// Evaluates if the current role holds a specific granular permission
  static bool hasPermission(CommitteeRole role, String permission) {
    // President holds all administrative permissions
    if (role == CommitteeRole.president) return true;

    switch (permission) {
      case PermissionCatalog.financeView:
      case PermissionCatalog.memberView:
      case PermissionCatalog.chandaView:
      case PermissionCatalog.chandaPay:
      case PermissionCatalog.staffView:
      case PermissionCatalog.madrasaView:
      case PermissionCatalog.studentView:
      case PermissionCatalog.attendanceView:
      case PermissionCatalog.hifzView:
      case PermissionCatalog.noticeView:
      case PermissionCatalog.meetingView:
        return true; // Transparent community read access
      case PermissionCatalog.financeCreate:
        return role.canManageFinance();
      case PermissionCatalog.financeApprove:
        return role.canApproveFinance();
      case PermissionCatalog.financeReport:
        return role.canManageFinance() ||
            role.canApproveFinance() ||
            role == CommitteeRole.generalSecretary ||
            role == CommitteeRole.advisoryBoard;
      case PermissionCatalog.staffManage:
        return role == CommitteeRole.president ||
            role == CommitteeRole.vicePresident ||
            role == CommitteeRole.generalSecretary;
      case PermissionCatalog.staffSalaryView:
        return role == CommitteeRole.president ||
            role == CommitteeRole.vicePresident ||
            role == CommitteeRole.treasurer ||
            role == CommitteeRole.generalSecretary;
      case PermissionCatalog.staffSalaryPay:
        return role == CommitteeRole.president || role == CommitteeRole.treasurer;
      case PermissionCatalog.memberCreate:
      case PermissionCatalog.memberUpdate:
        return role.canManageMembers();
      case PermissionCatalog.memberArchive:
        return role == CommitteeRole.president || role == CommitteeRole.generalSecretary;
      case PermissionCatalog.chandaReceive:
        return role == CommitteeRole.president ||
            role == CommitteeRole.treasurer ||
            role == CommitteeRole.assistantTreasurer ||
            role == CommitteeRole.officeSecretary;
      case PermissionCatalog.cashboxManage:
        return role.canManageCashbox();
      case PermissionCatalog.cashboxWitness:
        return role.canWitnessCashbox();
      case PermissionCatalog.madrasaManage:
      case PermissionCatalog.studentCreate:
      case PermissionCatalog.attendanceManage:
      case PermissionCatalog.hifzManage:
      case PermissionCatalog.examPublish:
        return role.canManageWelfareEducation() || role == CommitteeRole.assistantSecretary;
      case PermissionCatalog.feeReceive:
        return role.canManageFinance() ||
            role.canManageWelfareEducation() ||
            role == CommitteeRole.officeSecretary;
      case PermissionCatalog.noticeManage:
        return role.canPublishNotices();
      case PermissionCatalog.meetingManage:
        return role.canScheduleMeetings();
      case PermissionCatalog.prayerManage:
        return role == CommitteeRole.president ||
            role == CommitteeRole.generalSecretary ||
            role == CommitteeRole.imam ||
            role == CommitteeRole.muazzin ||
            role == CommitteeRole.publicitySecretary;
      case PermissionCatalog.dutyManage:
        return role == CommitteeRole.president ||
            role == CommitteeRole.vicePresident ||
            role == CommitteeRole.generalSecretary ||
            role == CommitteeRole.imam;
      case PermissionCatalog.welfareManage:
        return role.canManageWelfareEducation();
      default:
        return false;
    }
  }
}
