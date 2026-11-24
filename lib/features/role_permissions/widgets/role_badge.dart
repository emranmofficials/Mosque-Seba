import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../models/committee_role.dart';

/// Role Identity Badge with Culturally Appropriate Accent Color
class RoleBadge extends StatelessWidget {
  final CommitteeRole role;
  final bool isCompact;

  const RoleBadge({
    super.key,
    required this.role,
    this.isCompact = false,
  });

  Color _getRoleColor() {
    switch (role) {
      case CommitteeRole.president:
        return AppColors.rolePresident;
      case CommitteeRole.vicePresident:
        return AppColors.goldDeep;
      case CommitteeRole.treasurer:
      case CommitteeRole.assistantTreasurer:
        return AppColors.roleTreasurer;
      case CommitteeRole.generalSecretary:
      case CommitteeRole.assistantSecretary:
        return AppColors.roleSecretary;
      case CommitteeRole.organizingSecretary:
        return AppColors.rolePublicity;
      case CommitteeRole.publicitySecretary:
        return AppColors.rolePublicity;
      case CommitteeRole.officeSecretary:
        return AppColors.roleOffice;
      case CommitteeRole.welfareEducationSecretary:
        return AppColors.roleWelfare;
      case CommitteeRole.imam:
        return AppColors.roleImam;
      case CommitteeRole.advisoryBoard:
        return AppColors.roleAdvisory;
      default:
        return AppColors.roleMusalli;
    }
  }

  @override
  Widget build(BuildContext context) {
    final color = _getRoleColor();

    return Container(
      padding: EdgeInsets.symmetric(
        horizontal: isCompact ? MosqueSpace.xs : MosqueSpace.sm,
        vertical: isCompact ? 1 : MosqueSpace.xxs,
      ),
      decoration: BoxDecoration(
        color: color.withOpacity(0.12),
        borderRadius: BorderRadius.circular(MosqueRadius.pill),
        border: Border.all(color: color.withOpacity(0.4), width: 1),
      ),
      child: Text(
        role.titleBn,
        style: TextStyle(
          color: color,
          fontSize: isCompact ? 10 : 11,
          fontWeight: FontWeight.bold,
        ),
      ),
    );
  }
}
