import 'package:flutter/material.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../../../shared/widgets/card_shell.dart';
import '../models/waqf_asset_model.dart';

/// Screen displaying detailed view of a Waqf Property and its deed/registration info
class WaqfAssetDetailScreen extends StatelessWidget {
  final WaqfAssetModel asset;

  const WaqfAssetDetailScreen({super.key, required this.asset});

  @override
  Widget build(BuildContext context) {
    return AdaptiveScaffold(
      title: 'ওয়াকফ সম্পত্তির বিবরণ',
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(AppDimens.spacing16),
        child: Column(
          children: [
            CardShell(
              padding: const EdgeInsets.all(AppDimens.spacing20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(AppDimens.spacing10),
                        decoration: BoxDecoration(
                          color: AppColors.primaryContainer,
                          borderRadius: BorderRadius.circular(AppDimens.radiusMd),
                        ),
                        child: const Icon(
                          Icons.account_balance,
                          color: AppColors.primary,
                          size: 32,
                        ),
                      ),
                      const SizedBox(width: AppDimens.spacing12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              asset.title,
                              style: AppTypography.titleLarge.copyWith(fontWeight: FontWeight.bold),
                            ),
                            const SizedBox(height: 4),
                            Text(
                              asset.category,
                              style: AppTypography.bodySmall.copyWith(color: AppColors.primary),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: AppDimens.spacing16),
                  const Divider(),
                  const SizedBox(height: AppDimens.spacing12),
                  _buildDetailRow('অবস্থা (Status)', asset.status, isTag: true),
                  _buildDetailRow('আয়তন / পরিমাণ', asset.areaOrQuantity),
                  _buildDetailRow('অবস্থান ও দাগ নম্বর', asset.locationOrDagNo),
                  _buildDetailRow('দলিল / রেজিষ্ট্রেশন নম্বর', asset.deedOrRegNo.isNotEmpty ? asset.deedOrRegNo : 'সংরক্ষিত নেই'),
                  _buildDetailRow('আনুমানিক বাজারমূল্য / মাসিক আয়', asset.estimatedValueOrIncome.isNotEmpty ? asset.estimatedValueOrIncome : 'উল্লেখ নেই'),
                  _buildDetailRow('নিবন্ধনের তারিখ', asset.recordedDate.isNotEmpty ? asset.recordedDate : 'তথ্য নেই'),
                  if (asset.description.isNotEmpty) ...[
                    const SizedBox(height: AppDimens.spacing12),
                    Text(
                      'অতিরিক্ত বিবরণ ও শর্তাবলী:',
                      style: AppTypography.bodySmall.copyWith(
                        fontWeight: FontWeight.bold,
                        color: AppColors.textSecondary,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      asset.description,
                      style: AppTypography.bodyMedium,
                    ),
                  ],
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildDetailRow(String label, String value, {bool isTag = false}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            label,
            style: AppTypography.bodyMedium.copyWith(color: AppColors.textSecondary),
          ),
          const Spacer(),
          Flexible(
            child: isTag
                ? Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                    decoration: BoxDecoration(
                      color: AppColors.primaryContainer,
                      borderRadius: BorderRadius.circular(4),
                    ),
                    child: Text(
                      value,
                      style: AppTypography.caption.copyWith(
                        color: AppColors.primary,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  )
                : Text(
                    value,
                    style: AppTypography.bodyMedium.copyWith(fontWeight: FontWeight.w600),
                    textAlign: TextAlign.right,
                  ),
          ),
        ],
      ),
    );
  }
}
