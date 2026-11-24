import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/utils/bengali_number_formatter.dart';
import '../../../../shared/components/empty_state_view.dart';
import '../../../../shared/components/error_state_view.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../../../shared/widgets/card_shell.dart';
import '../dialogs/add_waqf_asset_dialog.dart';
import '../models/waqf_asset_model.dart';
import '../providers/waqf_provider.dart';
import '../widgets/waqf_asset_card.dart';

/// Screen displaying the directory of Waqf Assets & Mosque Properties (ওয়াকফ ও মসজিদ সম্পদ তালিকা)
class WaqfAssetListScreen extends ConsumerWidget {
  const WaqfAssetListScreen({super.key});

  final List<String> _categoryTabs = const [
    'সকল',
    'জমি / প্লট',
    'দোকান / মার্কেট',
    'পুকুর / জলাশয়',
    'ভবন / স্থাপনা',
  ];

  void _showAddDialog(BuildContext context, WidgetRef ref) {
    showDialog(
      context: context,
      builder: (ctx) => AddWaqfAssetDialog(
        onSubmit: (newAsset) => ref.read(waqfListProvider.notifier).addProperty(newAsset),
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final propertiesAsync = ref.watch(waqfListProvider);
    final selectedCategory = ref.watch(waqfCategoryFilterProvider);

    return AdaptiveScaffold(
      title: 'ওয়াকফ ও সম্পত্তি ডিরেক্টরি',
      floatingActionButton: FloatingActionButton.extended(
        backgroundColor: AppColors.primary,
        foregroundColor: AppColors.onPrimary,
        icon: const Icon(Icons.add_location_alt_outlined),
        label: const Text('সম্পত্তি যোগ', style: TextStyle(fontWeight: FontWeight.bold)),
        onPressed: () => _showAddDialog(context, ref),
      ),
      body: Column(
        children: [
          // Category filter bar
          Container(
            height: 48,
            color: AppColors.surface,
            child: ListView.separated(
              padding: const EdgeInsets.symmetric(horizontal: AppDimens.spacing16, vertical: 6),
              scrollDirection: Axis.horizontal,
              itemCount: _categoryTabs.length,
              separatorBuilder: (_, __) => const SizedBox(width: 8),
              itemBuilder: (context, index) {
                final tab = _categoryTabs[index];
                final isSelected = tab == selectedCategory;
                return ChoiceChip(
                  label: Text(tab),
                  selected: isSelected,
                  selectedColor: AppColors.primaryContainer,
                  labelStyle: TextStyle(
                    color: isSelected ? AppColors.primary : AppColors.textPrimary,
                    fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                    fontSize: 13,
                  ),
                  onSelected: (selected) {
                    if (selected) {
                      ref.read(waqfCategoryFilterProvider.notifier).state = tab;
                      ref.read(waqfListProvider.notifier).loadProperties();
                    }
                  },
                );
              },
            ),
          ),
          // Properties list
          Expanded(
            child: propertiesAsync.when(
              loading: () => const Center(
                child: CircularProgressIndicator(color: AppColors.primary),
              ),
              error: (err, _) => ErrorStateView(
                title: 'তথ্য লোড করতে সমস্যা হয়েছে',
                message: err.toString(),
                onRetry: () => ref.read(waqfListProvider.notifier).loadProperties(),
              ),
              data: (properties) {
                if (properties.isEmpty) {
                  return EmptyStateView(
                    title: 'কোনো ওয়াকফ সম্পত্তি তালিকাভুক্ত নেই',
                    message: selectedCategory != 'সকল'
                        ? 'এই ক্যাটাগরিতে কোনো সম্পত্তি সংরক্ষিত নেই'
                        : 'মসজিদের জমি, দোকান বা ওয়াকফকৃত সম্পদ ডিজিটাল রেজিস্টারে যুক্ত করুন।',
                    actionLabel: 'সম্পত্তি নিবন্ধন করুন',
                    onAction: () => _showAddDialog(context, ref),
                  );
                }

                return ListView(
                  padding: const EdgeInsets.all(AppDimens.spacing16),
                  children: [
                    // Overview Summary Banner
                    CardShell(
                      padding: const EdgeInsets.all(AppDimens.spacing12),
                      child: Row(
                        children: [
                          const Icon(Icons.verified_outlined, color: AppColors.primary, size: 28),
                          const SizedBox(width: AppDimens.spacing12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  'মোট নথিভুক্ত ওয়াকফ সম্পত্তি',
                                  style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
                                ),
                                const SizedBox(height: 2),
                                Text(
                                  '${BengaliNumberFormatter.toBengali(properties.length)} টি সম্পত্তি তালিকাভুক্ত',
                                  style: AppTypography.titleMedium.copyWith(
                                    fontWeight: FontWeight.bold,
                                    color: AppColors.primary,
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: AppDimens.spacing16),
                    ...properties.map(
                      (property) => Padding(
                        padding: const EdgeInsets.only(bottom: AppDimens.spacing12),
                        child: WaqfAssetCard(
                          asset: property,
                          onTap: () {
                            context.push('/waqf-assets-detail', extra: property);
                          },
                        ),
                      ),
                    ),
                  ],
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
