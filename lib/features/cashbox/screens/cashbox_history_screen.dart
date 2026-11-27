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
import '../dialogs/cashbox_counter_dialog.dart';
import '../models/cashbox_record_model.dart';
import '../providers/cashbox_provider.dart';

/// Screen displaying Friday cashbox counting history and witness vouchers (দানবাক্স গণনা খতিয়ান)
class CashboxHistoryScreen extends ConsumerWidget {
  const CashboxHistoryScreen({super.key});

  void _showCounterDialog(BuildContext context, WidgetRef ref) {
    showDialog(
      context: context,
      builder: (ctx) => CashboxCounterDialog(
        onSubmit: (record) => ref.read(cashboxListProvider.notifier).submitCount(record),
      ),
    );
  }

  void _showRecordDetails(BuildContext context, CashboxRecordModel record) {
    showDialog(
      context: context,
      builder: (ctx) => Dialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusLg)),
        child: Padding(
          padding: const EdgeInsets.all(AppDimens.spacing20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    'দানবাক্স গণনার বিস্তারিত',
                    style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold),
                  ),
                  IconButton(
                    icon: const Icon(Icons.close),
                    onPressed: () => Navigator.of(ctx).pop(),
                  ),
                ],
              ),
              const Divider(),
              const SizedBox(height: AppDimens.spacing8),
              _buildDetailItem('গণনার তারিখ', BengaliNumberFormatter.toBengali(record.waqtDate)),
              _buildDetailItem('ভাউচার কোড', BengaliNumberFormatter.toBengali(record.voucherCode ?? 'N/A')),
              _buildDetailItem('মোট আদায়', BengaliNumberFormatter.formatCurrency(record.totalAmount), isHighlight: true),
              const SizedBox(height: AppDimens.spacing12),
              Text('নোটের বিবরণ:', style: AppTypography.bodySmall.copyWith(fontWeight: FontWeight.bold)),
              const SizedBox(height: 4),
              Wrap(
                spacing: 6,
                runSpacing: 4,
                children: [
                  if (record.note1000 > 0) _buildNoteBadge('১০০০ ৳', record.note1000),
                  if (record.note500 > 0) _buildNoteBadge('৫০০ ৳', record.note500),
                  if (record.note200 > 0) _buildNoteBadge('২০০ ৳', record.note200),
                  if (record.note100 > 0) _buildNoteBadge('১০০ ৳', record.note100),
                  if (record.note50 > 0) _buildNoteBadge('৫০ ৳', record.note50),
                  if (record.note20 > 0) _buildNoteBadge('২০ ৳', record.note20),
                  if (record.note10 > 0) _buildNoteBadge('১০ ৳', record.note10),
                  if (record.note5 > 0) _buildNoteBadge('৫ ৳', record.note5),
                ],
              ),
              const SizedBox(height: AppDimens.spacing12),
              Text('সাক্ষীদের নাম:', style: AppTypography.bodySmall.copyWith(fontWeight: FontWeight.bold)),
              const SizedBox(height: 2),
              Text('১. ${record.witness1Name}', style: AppTypography.caption),
              Text('২. ${record.witness2Name}', style: AppTypography.caption),
              if (record.witness3Name.isNotEmpty) Text('৩. ${record.witness3Name}', style: AppTypography.caption),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildNoteBadge(String title, int count) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
      decoration: BoxDecoration(
        color: AppColors.surface,
        border: Border.all(color: AppColors.outline),
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text('$title x ${BengaliNumberFormatter.toBengali(count.toString())}', style: AppTypography.caption),
    );
  }

  Widget _buildDetailItem(String label, String value, {bool isHighlight = false}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: AppTypography.bodySmall.copyWith(color: AppColors.textSecondary)),
          Text(
            value,
            style: AppTypography.bodyMedium.copyWith(
              fontWeight: FontWeight.bold,
              color: isHighlight ? AppColors.primary : AppColors.textPrimary,
            ),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final recordsAsync = ref.watch(cashboxListProvider);

    return AdaptiveScaffold(
      title: 'জুমার দানবাক্স গণনা',
      floatingActionButton: FloatingActionButton.extended(
        backgroundColor: AppColors.primary,
        foregroundColor: AppColors.onPrimary,
        icon: const Icon(Icons.calculate_outlined),
        label: const Text('নতুন গণনা শুরু', style: TextStyle(fontWeight: FontWeight.bold)),
        onPressed: () => _showCounterDialog(context, ref),
      ),
      body: recordsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator(color: AppColors.primary)),
        error: (err, _) => ErrorStateView(
          title: 'তথ্য লোড করতে সমস্যা হয়েছে',
          message: err.toString(),
          onRetry: () => ref.read(cashboxListProvider.notifier).loadRecords(),
        ),
        data: (records) {
          final totalAllFridays = records.fold<double>(0.0, (acc, r) => acc + r.totalAmount);

          return CustomScrollView(
            slivers: [
              SliverPadding(
                padding: const EdgeInsets.all(AppDimens.spacing16),
                sliver: SliverList(
                  delegate: SliverChildListDelegate([
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
                      child: const Icon(Icons.monetization_on_outlined, color: AppColors.primary, size: 28),
                    ),
                    const SizedBox(width: AppDimens.spacing12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('মোট জুমার দানবাক্স আয়', style: AppTypography.caption.copyWith(color: AppColors.textSecondary)),
                          const SizedBox(height: 2),
                          Text(
                            BengaliNumberFormatter.formatCurrency(totalAllFridays),
                            style: AppTypography.titleLarge.copyWith(
                              fontWeight: FontWeight.bold,
                              color: AppColors.primary,
                            ),
                          ),
                        ],
                      ),
                    ),
                    Text(
                      '${BengaliNumberFormatter.toBengali(records.length)} টি জুমা',
                      style: AppTypography.caption.copyWith(fontWeight: FontWeight.bold),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: AppDimens.spacing16),
              Text(
                'গণনার ইতিহাস ও ভাউচার',
                style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: AppDimens.spacing12),
                    ]),
                  ),
                ),
                if (records.isEmpty)
                  const SliverToBoxAdapter(
                    child: Padding(
                      padding: EdgeInsets.symmetric(horizontal: AppDimens.spacing16),
                      child: EmptyStateView(
                        title: 'কোনো গণনার রেকর্ড নেই',
                        message: 'জুমার জামাতের পর ৩ সাক্ষীর উপস্থিতিতে নোট গণনা সম্পন্ন করুন।',
                      ),
                    ),
                  )
                else
                  SliverPadding(
                    padding: const EdgeInsets.fromLTRB(AppDimens.spacing16, 0, AppDimens.spacing16, AppDimens.spacing16),
                    sliver: SliverList.builder(
                      itemCount: records.length,
                      itemBuilder: (context, index) {
                        final record = records[index];
                        return Padding(
                          padding: const EdgeInsets.only(bottom: AppDimens.spacing12),
                          child: CardShell(
                            onTap: () => _showRecordDetails(context, record),
                            padding: const EdgeInsets.all(AppDimens.spacing12),
                            child: Row(
                              children: [
                                const CircleAvatar(
                                  radius: 20,
                                  backgroundColor: AppColors.primaryContainer,
                                  child: Icon(Icons.inventory_2_outlined, color: AppColors.primary, size: 20),
                                ),
                                const SizedBox(width: AppDimens.spacing12),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Text(
                                        'জুমার জামাত • ${BengaliNumberFormatter.toBengali(record.waqtDate)}',
                                        style: AppTypography.bodyMedium.copyWith(fontWeight: FontWeight.bold),
                                      ),
                                      const SizedBox(height: 2),
                                      Text(
                                        'সাক্ষী: ${record.witness1Name}',
                                        style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
                                        maxLines: 1,
                                        overflow: TextOverflow.ellipsis,
                                      ),
                                    ],
                                  ),
                                ),
                                Column(
                                  crossAxisAlignment: CrossAxisAlignment.end,
                                  children: [
                                    Text(
                                      BengaliNumberFormatter.formatCurrency(record.totalAmount),
                                      style: AppTypography.titleMedium.copyWith(
                                        fontWeight: FontWeight.bold,
                                        color: AppColors.primary,
                                      ),
                                    ),
                                    Text(
                                      '৩ সাক্ষী স্বাক্ষরিত',
                                      style: AppTypography.caption.copyWith(color: AppColors.success, fontSize: 10),
                                    ),
                                  ],
                                ),
                              ],
                            ),
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
