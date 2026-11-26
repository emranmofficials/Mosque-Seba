import 'package:flutter/material.dart';
import 'package:qr_flutter/qr_flutter.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/widgets/card_shell.dart';
import '../models/chanda_payment_model.dart';

/// Digital Verified Mosque Receipt Card (ডিজিটাল চাঁদা রসিদ)
class QrReceiptWidget extends StatelessWidget {
  final ChandaPaymentModel payment;
  final String mosqueName;

  const QrReceiptWidget({
    super.key,
    required this.payment,
    this.mosqueName = 'মসজিদ সেবা জামে মসজিদ',
  });

  @override
  Widget build(BuildContext context) {
    return CardShell(
      padding: const EdgeInsets.all(AppDimens.spacing20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          // Header seal & logo area
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.mosque, color: AppColors.primary, size: 28),
              const SizedBox(width: AppDimens.spacing8),
              Flexible(
                child: Text(
                  mosqueName,
                  style: AppTypography.titleMedium.copyWith(
                    fontWeight: FontWeight.bold,
                    color: AppColors.primary,
                  ),
                  textAlign: TextAlign.center,
                ),
              ),
            ],
          ),
          const SizedBox(height: 4),
          Text(
            'অফিসিয়াল চাঁদা রসিদ (ডিজিটাল রশিদ)',
            style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
          ),
          const SizedBox(height: AppDimens.spacing12),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
            decoration: BoxDecoration(
              color: AppColors.primaryContainer,
              borderRadius: BorderRadius.circular(AppDimens.radiusSm),
            ),
            child: Text(
              'রসিদ নং: ${BengaliNumberFormatter.toBengali(payment.receiptCode)}',
              style: AppTypography.bodySmall.copyWith(
                fontWeight: FontWeight.bold,
                color: AppColors.primary,
              ),
            ),
          ),
          const SizedBox(height: AppDimens.spacing16),
          const Divider(),
          const SizedBox(height: AppDimens.spacing12),
          _buildItemRow('সদস্যের নাম', payment.memberNameBn),
          _buildItemRow('পরিশোধিত মাস', payment.month),
          _buildItemRow('পরিশোধের মাধ্যম', payment.method == 'cash' ? 'নগদ ক্যাশ' : payment.method.toUpperCase()),
          _buildItemRow('পরিশোধের তারিখ', BengaliNumberFormatter.toBengali(payment.paidAt)),
          _buildItemRow('আদায়কারীর নাম', payment.receivedBy),
          const SizedBox(height: AppDimens.spacing8),
          const Divider(),
          const SizedBox(height: AppDimens.spacing8),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'মোট পরিশোধিত টাকা:',
                style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold),
              ),
              Text(
                BengaliNumberFormatter.formatCurrency(payment.amountPaid),
                style: AppTypography.titleLarge.copyWith(
                  fontWeight: FontWeight.bold,
                  color: AppColors.primary,
                ),
              ),
            ],
          ),
          const SizedBox(height: AppDimens.spacing16),
          // QR Verification Badge
          Container(
            padding: const EdgeInsets.all(AppDimens.spacing12),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(AppDimens.radiusMd),
              border: Border.all(color: AppColors.gold.withOpacity(0.4), width: 1.2),
            ),
            child: Column(
              children: [
                QrImageView(
                  data: payment.qrVerificationCode.isNotEmpty
                      ? payment.qrVerificationCode
                      : 'MOSQUE_SEBA:RECEIPT:${payment.receiptCode}',
                  version: QrVersions.auto,
                  size: 130.0,
                ),
                const SizedBox(height: 6),
                Text(
                  'যাচাইকৃত ডিজিটাল প্রমাণ',
                  style: AppTypography.caption.copyWith(color: AppColors.textSecondary, fontSize: 10),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildItemRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: AppTypography.bodySmall.copyWith(color: AppColors.textSecondary)),
          Text(value, style: AppTypography.bodySmall.copyWith(fontWeight: FontWeight.w600)),
        ],
      ),
    );
  }
}
