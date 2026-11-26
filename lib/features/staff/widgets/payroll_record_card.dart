import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/widgets/card_shell.dart';
import '../models/staff_payroll_model.dart';

/// Card presenting a disbursed staff payroll record
class PayrollRecordCard extends StatelessWidget {
  final StaffPayrollModel payroll;

  const PayrollRecordCard({super.key, required this.payroll});

  @override
  Widget build(BuildContext context) {
    return CardShell(
      padding: const EdgeInsets.all(AppDimens.spacing12),
      child: Row(
        children: [
          CircleAvatar(
            radius: 20,
            backgroundColor: AppColors.success.withOpacity(0.12),
            child: const Icon(Icons.check_circle_outline, color: AppColors.success, size: 22),
          ),
          const SizedBox(width: AppDimens.spacing12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  payroll.staffNameBn,
                  style: AppTypography.titleSmall.copyWith(fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 2),
                Text(
                  'মাস: ${payroll.month} • পরিশোধ: ${payroll.paidDate != null ? BengaliNumberFormatter.toBengali(payroll.paidDate!) : "তথ্য নেই"}',
                  style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
                ),
              ],
            ),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Text(
                BengaliNumberFormatter.formatCurrency(payroll.netSalary),
                style: AppTypography.titleMedium.copyWith(
                  fontWeight: FontWeight.bold,
                  color: AppColors.success,
                ),
              ),
              if (payroll.bonusAmount > 0)
                Text(
                  '+ বোনাস ${BengaliNumberFormatter.formatCurrency(payroll.bonusAmount)}',
                  style: AppTypography.caption.copyWith(color: AppColors.gold, fontSize: 10),
                ),
            ],
          ),
        ],
      ),
    );
  }
}
