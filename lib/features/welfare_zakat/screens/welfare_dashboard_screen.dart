import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/components/empty_state_view.dart';
import '../../../shared/widgets/card_shell.dart';
import '../dialogs/add_welfare_case_dialog.dart';
import '../dialogs/approve_welfare_grant_dialog.dart';
import '../models/welfare_case_model.dart';
import '../providers/welfare_provider.dart';
import '../widgets/welfare_case_card.dart';

/// Screen presenting the Welfare & Zakat Assistance Dashboard
class WelfareDashboardScreen extends ConsumerWidget {
  const WelfareDashboardScreen({super.key});

  void _showAddDialog(BuildContext context, WidgetRef ref) {
    showDialog(
      context: context,
      builder: (ctx) => AddWelfareCaseDialog(
        onSubmit: (welfareCase) => ref.read(welfareListProvider.notifier).submitCase(welfareCase),
      ),
    );
  }

  void _showApproveDialog(
    BuildContext context,
    WidgetRef ref,
    WelfareCaseModel welfareCase,
  ) {
    showDialog<void>(
      context: context,
      builder: (ctx) => ApproveWelfareGrantDialog(
        welfareCase: welfareCase,
        onApprove: (amount, approver, disburseNow) async {
          final ok = await ref.read(welfareListProvider.notifier).approveCase(
                caseId: welfareCase.id,
                approvedAmount: amount,
                approverName: approver,
                disburseNow: disburseNow,
              );
          if (!context.mounted) return;
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(
                ok
                    ? (disburseNow
                        ? 'অনুদানের অর্থ সফলভাবে বিতরণ ও হিসাবভুক্ত হয়েছে'
                        : 'অনুদান মঞ্জুর হয়েছে')
                    : 'সংরক্ষণ করতে ব্যর্থ হয়েছে',
              ),
              backgroundColor: ok ? AppColors.emeraldPrimary : Colors.red,
            ),
          );
        },
      ),
    );
  }

  Future<void> _disburseApprovedCase(
    BuildContext context,
    WidgetRef ref,
    WelfareCaseModel welfareCase,
  ) async {
    final ok = await ref.read(welfareListProvider.notifier).approveCase(
          caseId: welfareCase.id,
          approvedAmount: welfareCase.approvedAmount > 0
              ? welfareCase.approvedAmount
              : welfareCase.requestedAmount,
          approverName: welfareCase.approvedBy ?? 'সভাপতি',
          disburseNow: true,
        );
    if (!context.mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(
          ok
              ? 'অনুদানের অর্থ বিতরণ সম্পন্ন ও ফাইন্যান্স লেজারে যুক্ত হয়েছে'
              : 'বিতরণ সম্পন্ন করা যায়নি',
        ),
        backgroundColor: ok ? AppColors.emeraldPrimary : Colors.red,
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final casesAsync = ref.watch(welfareListProvider);
    final filteredCases = ref.watch(filteredWelfareCasesProvider);
    final currentStatusFilter = ref.watch(welfareStatusFilterProvider);

    final allCases = casesAsync.valueOrNull ?? [];
    final totalDisbursed = allCases
        .where((c) => c.status == 'disbursed')
        .fold<double>(0.0, (acc, c) => acc + c.approvedAmount);
    final pendingCount = allCases.where((c) => c.status == 'pending').length;

    return Scaffold(
      appBar: AppBar(
        title: const Text('যাকাত ও সমাজকল্যাণ তহবিল'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () => ref.read(welfareListProvider.notifier).loadCases(),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        backgroundColor: AppColors.emeraldPrimary,
        foregroundColor: Colors.white,
        icon: const Icon(Icons.volunteer_activism),
        label: const Text('নতুন আবেদন', style: TextStyle(fontWeight: FontWeight.bold)),
        onPressed: () => _showAddDialog(context, ref),
      ),
      body: Column(
        children: [
          // KPI Metric Header
          Container(
            padding: const EdgeInsets.all(AppDimens.spacing14),
            color: Theme.of(context).cardColor,
            child: Row(
              children: [
                Expanded(
                  child: CardShell(
                    padding: const EdgeInsets.all(10),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text('মোট বিতরণকৃত যাকাত', style: TextStyle(fontSize: 11, color: Colors.grey)),
                        const SizedBox(height: 4),
                        Text(
                          '৳ ${BengaliNumberFormatter.toBengali(totalDisbursed.toInt().toString())}',
                          style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: AppColors.emeraldPrimary),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: CardShell(
                    padding: const EdgeInsets.all(10),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text('প্রক্রিয়াধীন আবেদন', style: TextStyle(fontSize: 11, color: Colors.grey)),
                        const SizedBox(height: 4),
                        Text(
                          '${BengaliNumberFormatter.toBengali(pendingCount.toString())} টি',
                          style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: AppColors.roleTreasurer),
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),

          // Status Filter Tabs
          Container(
            padding: const EdgeInsets.symmetric(horizontal: AppDimens.spacing14, vertical: 8),
            child: SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: Row(
                children: [
                  _buildFilterChip(ref, 'সকল আবেদন', 'all', currentStatusFilter),
                  const SizedBox(width: 6),
                  _buildFilterChip(ref, 'প্রক্রিয়াধীন', 'pending', currentStatusFilter),
                  const SizedBox(width: 6),
                  _buildFilterChip(ref, 'মঞ্জুরকৃত', 'approved', currentStatusFilter),
                  const SizedBox(width: 6),
                  _buildFilterChip(ref, 'বিতরণ সম্পন্ন', 'disbursed', currentStatusFilter),
                ],
              ),
            ),
          ),

          // Welfare Cases List
          Expanded(
            child: casesAsync.when(
              loading: () => const Center(child: CircularProgressIndicator(color: AppColors.emeraldPrimary)),
              error: (err, _) => Center(child: Text('ত্রুটি: $err')),
              data: (_) {
                if (filteredCases.isEmpty) {
                  return EmptyStateView(
                    title: 'কোনো আবেদন পাওয়া যায়নি',
                    message: 'নতুন যাকাত বা সমাজকল্যাণ আবেদন যুক্ত করতে নিচে চাপ দিন।',
                    actionLabel: 'আবেদন তৈরি করুন',
                    onAction: () => _showAddDialog(context, ref),
                  );
                }

                return ListView.builder(
                  padding: const EdgeInsets.all(AppDimens.spacing14),
                  itemCount: filteredCases.length,
                  itemBuilder: (context, index) {
                    final welfareCase = filteredCases[index];
                    return Padding(
                      padding: const EdgeInsets.only(bottom: AppDimens.spacing12),
                      child: WelfareCaseCard(
                        welfareCase: welfareCase,
                        onApprove: () => _showApproveDialog(context, ref, welfareCase),
                        onDisburse: () =>
                            _disburseApprovedCase(context, ref, welfareCase),
                      ),
                    );
                  },
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFilterChip(WidgetRef ref, String label, String value, String current) {
    final isSelected = current == value;
    return ChoiceChip(
      label: Text(label),
      selected: isSelected,
      selectedColor: AppColors.emeraldPrimary.withOpacity(0.15),
      labelStyle: TextStyle(
        color: isSelected ? AppColors.emeraldPrimary : Colors.grey.shade700,
        fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
        fontSize: 12,
      ),
      onSelected: (_) {
        ref.read(welfareStatusFilterProvider.notifier).state = value;
      },
    );
  }
}
