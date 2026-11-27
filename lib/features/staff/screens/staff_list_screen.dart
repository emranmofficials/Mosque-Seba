import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/components/empty_state_view.dart';
import '../../../shared/components/error_state_view.dart';
import '../../../shared/widgets/adaptive_scaffold.dart';
import '../../../shared/widgets/card_shell.dart';
import '../../role_permissions/models/committee_role.dart';
import '../../role_permissions/models/permission_catalog.dart';
import '../../role_permissions/providers/role_permissions_provider.dart';
import '../../role_permissions/services/permission_engine.dart';
import '../dialogs/add_staff_dialog.dart';
import '../dialogs/disburse_salary_dialog.dart';
import '../models/staff_model.dart';
import '../providers/staff_provider.dart';
import '../widgets/staff_card.dart';

/// Screen managing Mosque Employees and Payroll (মসজিদের স্টাফ ও ইমাম-মুয়াজ্জিন পরিচালনা)
class StaffListScreen extends ConsumerWidget {
  const StaffListScreen({super.key});

  void _showAddStaffDialog(
    BuildContext context,
    WidgetRef ref, {
    StaffModel? existingStaff,
  }) {
    showDialog(
      context: context,
      builder: (ctx) => AddStaffDialog(
        existingStaff: existingStaff,
        onSubmit: (staff) {
          if (existingStaff != null) {
            return ref.read(staffListProvider.notifier).updateStaff(staff);
          }
          return ref.read(staffListProvider.notifier).addStaff(staff);
        },
      ),
    );
  }

  Future<void> _confirmDeactivate(
    BuildContext context,
    WidgetRef ref,
    StaffModel staff,
  ) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('স্টাফ অব্যাহতি নিশ্চিত করুন'),
        content: Text('${staff.nameBn} কে সক্রিয় স্টাফ তালিকা থেকে সরাতে চান? পূর্ববর্তী বেতনের রেকর্ড সুরক্ষিত থাকবে।'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('বাতিল'),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.urgentRed,
              foregroundColor: Colors.white,
            ),
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('অব্যাহতি দিন'),
          ),
        ],
      ),
    );
    if (confirmed != true) return;
    final ok = await ref.read(staffListProvider.notifier).deactivateStaff(staff.id);
    if (!context.mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(
          ok ? '${staff.nameBn} কে নিষ্ক্রিয় করা হয়েছে।' : 'প্রক্রিয়াটি ব্যর্থ হয়েছে।',
        ),
      ),
    );
  }

  void _showDisburseDialog(BuildContext context, WidgetRef ref, StaffModel staff) {
    showDialog(
      context: context,
      builder: (ctx) => DisburseSalaryDialog(
        staff: staff,
        onDisburse: ({
          required String staffId,
          required String staffNameBn,
          required String month,
          required double baseSalary,
          required double bonusAmount,
          required String disbursedBy,
        }) =>
            ref.read(payrollHistoryProvider.notifier).disburseSalary(
                  staffId: staffId,
                  staffNameBn: staffNameBn,
                  month: month,
                  baseSalary: baseSalary,
                  bonusAmount: bonusAmount,
                  disbursedBy: disbursedBy,
                ),
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final staffAsync = ref.watch(staffListProvider);
    final role = ref.watch(currentRoleProvider);
    final canManageStaff = role == CommitteeRole.president ||
        PermissionEngine.hasPermission(role, PermissionCatalog.staffManage);
    final canPaySalary = role == CommitteeRole.president ||
        PermissionEngine.hasPermission(role, PermissionCatalog.staffSalaryPay);
    final canViewPayroll = role == CommitteeRole.president ||
        PermissionEngine.hasPermission(role, PermissionCatalog.staffSalaryView);

    return AdaptiveScaffold(
      title: 'মসজিদ স্টাফ ও ইমাম-মুয়াজ্জিন',
      actions: [
        if (canViewPayroll)
          IconButton(
            icon: const Icon(Icons.history_edu),
            tooltip: 'বেতন পরিশোধ খতিয়ান',
            onPressed: () => context.push('/staff-payroll'),
          ),
      ],
      floatingActionButton: canManageStaff
          ? FloatingActionButton.extended(
              backgroundColor: AppColors.primary,
              foregroundColor: AppColors.onPrimary,
              icon: const Icon(Icons.person_add_alt_1),
              label: const Text('স্টাফ নিয়োগ', style: TextStyle(fontWeight: FontWeight.bold)),
              onPressed: () => _showAddStaffDialog(context, ref),
            )
          : null,
      body: staffAsync.when(
        loading: () => const Center(child: CircularProgressIndicator(color: AppColors.primary)),
        error: (err, _) => ErrorStateView(
          title: 'তথ্য লোড করতে সমস্যা হয়েছে',
          message: err.toString(),
          onRetry: () => ref.read(staffListProvider.notifier).loadStaff(),
        ),
        data: (staffList) {
          final totalMonthlyPayroll = staffList.fold<double>(0.0, (acc, s) => acc + s.salaryAmount);
          final mealCount = staffList.where((s) => s.mealRequired).length;

          return CustomScrollView(
            slivers: [
              SliverPadding(
                padding: const EdgeInsets.all(AppDimens.spacing16),
                sliver: SliverList(
                  delegate: SliverChildListDelegate([
              // Summary Banner
              CardShell(
                padding: const EdgeInsets.all(AppDimens.spacing16),
                child: Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(AppDimens.spacing10),
                      decoration: BoxDecoration(
                        color: AppColors.primaryContainer,
                        borderRadius: BorderRadius.circular(AppDimens.radiusMd),
                      ),
                      child: const Icon(Icons.badge_outlined, color: AppColors.primary, size: 28),
                    ),
                    const SizedBox(width: AppDimens.spacing12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('নির্ধারিত মাসিক বেতন বাজেট', style: AppTypography.caption.copyWith(color: AppColors.textSecondary)),
                          const SizedBox(height: 2),
                          Text(
                            BengaliNumberFormatter.formatCurrency(totalMonthlyPayroll),
                            style: AppTypography.titleLarge.copyWith(
                              fontWeight: FontWeight.bold,
                              color: AppColors.primary,
                            ),
                          ),
                        ],
                      ),
                    ),
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.end,
                      children: [
                        Text(
                          '${BengaliNumberFormatter.toBengali(staffList.length)} জন কর্মকর্তা',
                          style: AppTypography.caption.copyWith(fontWeight: FontWeight.bold),
                        ),
                        Text(
                          'খাবার বরাদ্দ: ${BengaliNumberFormatter.toBengali(mealCount)} জন',
                          style: AppTypography.caption.copyWith(color: AppColors.gold, fontSize: 11),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
              const SizedBox(height: AppDimens.spacing16),
              Text(
                'কর্মকর্তা ও কর্মচারীদের তালিকা',
                style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: AppDimens.spacing12),
                    ]),
                  ),
                ),
                if (staffList.isEmpty)
                  SliverToBoxAdapter(
                    child: Padding(
                      padding: const EdgeInsets.symmetric(horizontal: AppDimens.spacing16),
                      child: EmptyStateView(
                        title: 'কোনো স্টাফ নিবন্ধিত নেই',
                        message: 'মসজিদের পেশ ইমাম, মুয়াজ্জিন ও খাদেমদের তথ্য নিবন্ধন করুন।',
                        actionLabel: 'স্টাফ যোগ করুন',
                        onAction: () => _showAddStaffDialog(context, ref),
                      ),
                    ),
                  )
                else
                  SliverPadding(
                    padding: const EdgeInsets.fromLTRB(AppDimens.spacing16, 0, AppDimens.spacing16, AppDimens.spacing16),
                    sliver: SliverList.builder(
                      itemCount: staffList.length,
                      itemBuilder: (context, index) {
                        final staff = staffList[index];
                        return Padding(
                          padding: const EdgeInsets.only(bottom: AppDimens.spacing12),
                          child: StaffCard(
                            staff: staff,
                            canPaySalary: canPaySalary,
                            onPaySalary: () => _showDisburseDialog(context, ref, staff),
                            onEdit: canManageStaff
                                ? () => _showAddStaffDialog(context, ref, existingStaff: staff)
                                : null,
                            onDeactivate: canManageStaff
                                ? () => _confirmDeactivate(context, ref, staff)
                                : null,
                          ),
                        );
                      },
                    ),
                  ),
              ],
            );
        },
      ),
    );
  }
}
