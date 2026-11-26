import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../shared/widgets/card_shell.dart';
import '../models/waqf_asset_model.dart';

/// Card component representing a Waqf Property in the list
class WaqfAssetCard extends StatelessWidget {
  final WaqfAssetModel asset;
  final VoidCallback onTap;

  const WaqfAssetCard({
    super.key,
    required this.asset,
    required this.onTap,
  });

  IconData _getCategoryIcon(String category) {
    if (category.contains('জমি') || category.contains('প্লট')) return Icons.landscape_outlined;
    if (category.contains('দোকান') || category.contains('মার্কেট')) return Icons.storefront_outlined;
    if (category.contains('পুকুর')) return Icons.water_drop_outlined;
    if (category.contains('ভবন')) return Icons.domain_outlined;
    return Icons.account_balance_outlined;
  }

  Color _getStatusColor(String status) {
    if (status.contains('সংরক্ষিত')) return AppColors.success;
    if (status.contains('ভাড়া') || status.contains('লীজ')) return AppColors.primary;
    if (status.contains('বিতর্কিত')) return AppColors.error;
    return AppColors.warning;
  }

  @override
  Widget build(BuildContext context) {
    final statusColor = _getStatusColor(asset.status);

    return CardShell(
      onTap: onTap,
      padding: const EdgeInsets.all(AppDimens.spacing16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(AppDimens.spacing8),
                decoration: BoxDecoration(
                  color: AppColors.primaryContainer,
                  borderRadius: BorderRadius.circular(AppDimens.radiusSm),
                ),
                child: Icon(
                  _getCategoryIcon(asset.category),
                  color: AppColors.primary,
                  size: 22,
                ),
              ),
              const SizedBox(width: AppDimens.spacing12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      asset.title,
                      style: AppTypography.titleMedium.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    const SizedBox(height: 2),
                    Text(
                      asset.category,
                      style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
                    ),
                  ],
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(
                  color: statusColor.withValues(alpha: 0.12),
                  borderRadius: BorderRadius.circular(AppDimens.radiusSm),
                  border: Border.all(color: statusColor.withValues(alpha: 0.3)),
                ),
                child: Text(
                  asset.status,
                  style: AppTypography.caption.copyWith(
                    color: statusColor,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: AppDimens.spacing12),
          const Divider(),
          const SizedBox(height: AppDimens.spacing8),
          Row(
            children: [
              Expanded(
                child: _buildDetailItem(
                  Icons.square_foot,
                  'আয়তন / পরিমাণ',
                  asset.areaOrQuantity,
                ),
              ),
              Expanded(
                child: _buildDetailItem(
                  Icons.pin_drop_outlined,
                  'অবস্থান / দাগ',
                  asset.locationOrDagNo,
                ),
              ),
            ],
          ),
          if (asset.estimatedValueOrIncome.isNotEmpty) ...[
            const SizedBox(height: AppDimens.spacing8),
            _buildDetailItem(
              Icons.attach_money,
              'আনুমানিক মূল্য / আয়',
              asset.estimatedValueOrIncome,
              isHighlight: true,
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildDetailItem(IconData icon, String label, String value, {bool isHighlight = false}) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Icon(icon, size: 14, color: isHighlight ? AppColors.gold : AppColors.textSecondary),
        const SizedBox(width: 4),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                label,
                style: AppTypography.caption.copyWith(color: AppColors.textSecondary, fontSize: 10),
              ),
              Text(
                value,
                style: AppTypography.bodySmall.copyWith(
                  fontWeight: FontWeight.w600,
                  color: isHighlight ? AppColors.gold : AppColors.textPrimary,
                ),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ),
        ),
      ],
    );
  }
}
