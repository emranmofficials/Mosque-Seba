import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../features/role_permissions/models/committee_role.dart';
import '../../../features/role_permissions/providers/role_permissions_provider.dart';
import 'advisory_board/advisory_board_shell.dart';
import 'assistant_secretary/assistant_secretary_shell.dart';
import 'assistant_treasurer/assistant_treasurer_shell.dart';
import 'executive_member/executive_member_shell.dart';
import 'general_member/general_member_shell.dart';
import 'general_secretary/general_secretary_shell.dart';
import 'khatib_imam/khatib_imam_shell.dart';
import 'madrasa_teacher/madrasa_teacher_shell.dart';
import 'office_secretary/office_secretary_shell.dart';
import 'organizational_secretary/organizational_secretary_shell.dart';
import 'president/president_shell.dart';
import 'publicity_secretary/publicity_secretary_shell.dart';
import 'social_welfare_secretary/social_welfare_secretary_shell.dart';
import 'staff_imam/staff_imam_shell.dart';
import 'staff_khadem/staff_khadem_shell.dart';
import 'staff_muazzin/staff_muazzin_shell.dart';
import 'treasurer/treasurer_shell.dart';
import 'vice_president/vice_president_shell.dart';

/// Resolves the authenticated user's [CommitteeRole] to its dedicated shell UI.
class RoleShellResolver extends ConsumerWidget {
  const RoleShellResolver({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final role = ref.watch(currentRoleProvider);
    switch (role) {
      case CommitteeRole.president:
        return const PresidentShell();
      case CommitteeRole.vicePresident:
        return const VicePresidentShell();
      case CommitteeRole.generalSecretary:
        return const GeneralSecretaryShell();
      case CommitteeRole.assistantSecretary:
        return const AssistantSecretaryShell();
      case CommitteeRole.treasurer:
        return const TreasurerShell();
      case CommitteeRole.assistantTreasurer:
        return const AssistantTreasurerShell();
      case CommitteeRole.organizingSecretary:
        return const OrganizationalSecretaryShell();
      case CommitteeRole.publicitySecretary:
        return const PublicitySecretaryShell();
      case CommitteeRole.officeSecretary:
        return const OfficeSecretaryShell();
      case CommitteeRole.welfareEducationSecretary:
        return const SocialWelfareSecretaryShell();
      case CommitteeRole.executiveMember:
        return const ExecutiveMemberShell();
      case CommitteeRole.imam:
        return const KhatibImamShell();
      case CommitteeRole.advisoryBoard:
        return const AdvisoryBoardShell();
      case CommitteeRole.staffImam:
        return const StaffImamShell();
      case CommitteeRole.muazzin:
        return const StaffMuazzinShell();
      case CommitteeRole.khadem:
        return const StaffKhademShell();
      case CommitteeRole.madrasaTeacher:
        return const MadrasaTeacherShell();
      case CommitteeRole.generalMember:
        return const GeneralMemberShell();
    }
  }
}
