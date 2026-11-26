import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/components/empty_state_view.dart';
import '../../../shared/components/error_state_view.dart';
import '../../../shared/widgets/adaptive_scaffold.dart';
import '../../../shared/widgets/card_shell.dart';
import '../../authentication/providers/auth_provider.dart';
import '../../printing_pdf/services/native_printing_service.dart';
import '../providers/staff_provider.dart';
import '../widgets/payroll_record_card.dart';

/// Screen presenting the history of all disbursed staff salary vouchers (বেতন পরিশোধ রেজিস্টার)
class PayrollHistoryScreen extends ConsumerWidget {
  const PayrollHistoryScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final payrollAsync = ref.watch(payrollHistoryProvider);
    final selectedMonth = ref.watch(payrollMonthFilterProvider);
    final mosqueName = ref.watch(authNotifierProvider).valueOrNull?.mosqueNameBn ?? '';

    return AdaptiveScaffold(
      title: 'বেতন পরিশোধ খতিয়ান',
      body: payrollAsync.when(
        loading: () => const Center(child: CircularProgressIndicator(color: AppColors.primary)),
        error: (err, _) => ErrorStateView(
          title: 'তথ্য লোড করতে সমস্যা হয়েছে',
          message: err.toString(),
          onRetry: () => ref.read(payrollHistoryProvider.notifier).loadPayroll(monthFilter: 'সকল'),
        ),
        data: (payrollList) {
          final distinctMonths = <String>{
            'সকল',
            ...payrollList.map((p) => p.month).where((m) => m.trim().isNotEmpty),
          }.toList();
          final effectiveMonth = distinctMonths.contains(selectedMonth) ? selectedMonth : 'সকল';
          final filteredList = effectiveMonth == 'সকল'
              ? payrollList
              : payrollList.where((p) => p.month == effectiveMonth).toList();
          final totalPaid = filteredList.fold<double>(0.0, (acc, p) => acc + p.netSalary);

          return RefreshIndicator(
            onRefresh: () =>
                ref.read(payrollHistoryProvider.notifier).loadPayroll(monthFilter: 'সকল'),
            child: ListView(
              padding: const EdgeInsets.all(AppDimens.spacing16),
              children: [
                if (distinctMonths.length > 1) ...[
                  SingleChildScrollView(
                    scrollDirection: Axis.horizontal,
                    child: Row(
                      children: distinctMonths.map((month) {
                        final isSelected = month == effectiveMonth;
                        return Padding(
                          padding: const EdgeInsets.only(right: 8),
                          child: ChoiceChip(
                            label: Text(month),
                            selected: isSelected,
                            selectedColor: AppColors.primaryContainer,
                            labelStyle: TextStyle(
                              fontSize: 12,
                              color: isSelected ? AppColors.primary : AppColors.textPrimary,
                              fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                            ),
                            onSelected: (selected) {
                              if (selected) {
                                ref.read(payrollMonthFilterProvider.notifier).state = month;
                              }
                            },
                          ),
                        );
                      }).toList(),
                    ),
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                ],
                CardShell(
                  padding: const EdgeInsets.all(AppDimens.spacing16),
                  child: Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(AppDimens.spacing10),
                        decoration: BoxDecoration(
                          color: AppColors.success.withOpacity(0.15),
                          borderRadius: BorderRadius.circular(AppDimens.radiusMd),
                        ),
                        child: const Icon(Icons.verified, color: AppColors.success, size: 28),
                      ),
                      const SizedBox(width: AppDimens.spacing12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text('মোট পরিশোধিত বেতনভাতা', style: AppTypography.caption.copyWith(color: AppColors.textSecondary)),
                            const SizedBox(height: 2),
                            Text(
                              BengaliNumberFormatter.formatCurrency(totalPaid),
                              style: AppTypography.titleLarge.copyWith(
                                fontWeight: FontWeight.bold,
                                color: AppColors.success,
                              ),
                            ),
                          ],
                        ),
                      ),
                      Text(
                        '${BengaliNumberFormatter.toBengali(filteredList.length)} টি ভাউচার',
                        style: AppTypography.caption.copyWith(fontWeight: FontWeight.bold),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: AppDimens.spacing16),
                Text(
                  'পরিশোধিত বেতন ভাউচারসমূহ',
                  style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: AppDimens.spacing12),
                if (filteredList.isEmpty)
                  const EmptyStateView(
                    title: 'কোনো বেতন পরিশোধের রেকর্ড নেই',
                    message: 'স্টাফ তালিকা থেকে মাসিক বেতন পরিশোধ সম্পন্ন করুন।',
                  )
                else
                  ...filteredList.map(
                    (payroll) => Padding(
                      padding: const EdgeInsets.only(bottom: AppDimens.spacing8),
                      child: PayrollRecordCard(
                        payroll: payroll,
                        onPrintSlip: () => NativePrintingService().printSalarySlip(
                          staffName: payroll.staffNameBn,
                          month: payroll.month,
                          baseSalary: payroll.salaryAmount,
                          bonusAmount: payroll.bonusAmount,
                          netSalary: payroll.netSalary,
                          paidDate: payroll.paidDate ??
                              DateTime.now().toIso8601String().split('T').first,
                          mosqueName: mosqueName.isNotEmpty
                              ? mosqueName
                              : 'মসজিদ সেবা পোর্টাল',
                        ),
                      ),
                    ),
                  ),
              ],
            ),
          );
        },
      ),
    );
  }
}
