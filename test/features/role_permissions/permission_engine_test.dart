import 'package:flutter_test/flutter_test.dart';
import 'package:mosqueseba/features/role_permissions/models/committee_role.dart';
import 'package:mosqueseba/features/role_permissions/models/permission_catalog.dart';
import 'package:mosqueseba/features/role_permissions/services/permission_engine.dart';

void main() {
  group('PermissionEngine & CommitteeRole', () {
    test('president holds all administrative permissions', () {
      expect(
        PermissionEngine.hasPermission(
          CommitteeRole.president,
          PermissionCatalog.financeApprove,
        ),
        isTrue,
      );
      expect(
        PermissionEngine.hasPermission(
          CommitteeRole.president,
          PermissionCatalog.staffSalaryPay,
        ),
        isTrue,
      );
    });

    test('general member is restricted to transparent finance summary only', () {
      expect(
        PermissionEngine.hasPermission(
          CommitteeRole.generalMember,
          PermissionCatalog.financeView,
        ),
        isTrue,
      );
      expect(
        PermissionEngine.hasPermission(
          CommitteeRole.generalMember,
          PermissionCatalog.financeApprove,
        ),
        isFalse,
      );
      expect(
        PermissionEngine.hasPermission(
          CommitteeRole.generalMember,
          PermissionCatalog.cashboxManage,
        ),
        isFalse,
      );
      expect(
        PermissionEngine.hasPermission(
          CommitteeRole.generalMember,
          PermissionCatalog.staffSalaryView,
        ),
        isFalse,
      );
    });

    test('treasurer can manage finance and pay staff salaries but cannot approve finance', () {
      expect(
        PermissionEngine.hasPermission(
          CommitteeRole.treasurer,
          PermissionCatalog.financeCreate,
        ),
        isTrue,
      );
      expect(
        PermissionEngine.hasPermission(
          CommitteeRole.treasurer,
          PermissionCatalog.staffSalaryPay,
        ),
        isTrue,
      );
      expect(
        PermissionEngine.hasPermission(
          CommitteeRole.treasurer,
          PermissionCatalog.financeApprove,
        ),
        isFalse,
      );
    });

    test('resolves UI profiles accurately', () {
      expect(
        PermissionEngine.resolveUiProfile(CommitteeRole.president),
        UiProfile.ui01President,
      );
      expect(
        PermissionEngine.resolveUiProfile(CommitteeRole.imam),
        UiProfile.ui04Staff,
      );
      expect(
        PermissionEngine.resolveUiProfile(CommitteeRole.generalMember),
        UiProfile.ui03Musalli,
      );
    });

    test('evaluates expanded catalog permissions across specialist roles', () {
      expect(
        PermissionEngine.hasPermission(
          CommitteeRole.imam,
          PermissionCatalog.madrasaManage,
        ),
        isTrue,
      );
      expect(
        PermissionEngine.hasPermission(
          CommitteeRole.imam,
          PermissionCatalog.dutyManage,
        ),
        isTrue,
      );
      expect(
        PermissionEngine.hasPermission(
          CommitteeRole.muazzin,
          PermissionCatalog.prayerManage,
        ),
        isTrue,
      );
      expect(
        PermissionEngine.hasPermission(
          CommitteeRole.officeSecretary,
          PermissionCatalog.feeReceive,
        ),
        isTrue,
      );
      expect(
        PermissionEngine.hasPermission(
          CommitteeRole.generalSecretary,
          PermissionCatalog.memberArchive,
        ),
        isTrue,
      );
      expect(
        PermissionEngine.hasPermission(
          CommitteeRole.generalMember,
          PermissionCatalog.madrasaManage,
        ),
        isFalse,
      );
      expect(
        PermissionEngine.hasPermission(
          CommitteeRole.generalMember,
          PermissionCatalog.staffManage,
        ),
        isFalse,
      );
    });
  });
}
