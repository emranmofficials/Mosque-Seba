import 'package:flutter/material.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/utils/bengali_number_formatter.dart';
import '../../../../shared/widgets/card_shell.dart';
import '../models/welfare_case_model.dart';

/// Card component presenting a Welfare or Zakat Case
class WelfareCaseCard extends StatelessWidget {
  final WelfareCaseModel welfareCase;
  final VoidCallback? onTap;
  final VoidCallback? onApprove;

  const WelfareCaseCard({
    super.key,
    required this.welfareCase,
    this.onTap,
    this.onApprove,
  });

  Color _getStatusColor(String status) {
    switch (status) {
      case 'disbursed':
        return AppColors.emeraldPrimary;
      case 'approved':
        return Colors.blue.shade700;
      case 'pending':
        return AppColors.roleTreasurer;
      default:
        return Colors.grey;
    }
  }

  String _getStatusLabel(String status) {
    switch (status) {
      case 'disbursed':
        return 'বিতরণ সম্পন্ন';
      case 'approved':
        return 'মঞ্জুরকৃত';
      case 'pending':
        return 'প্রক্রিয়াধীন';
      default:
        return status;
    }
  }

  @override
  Widget build(BuildContext context) {
    final statusColor = _getStatusColor(welfareCase.status);

    return CardShell(
      onTap: onTap,
      padding: const EdgeInsets.all(AppDimens.spacing14),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: statusColor.withOpacity(0.12),
                  shape: BoxShape.circle,
                ),
                child: Icon(Icons.volunteer_activism, color: statusColor, size: 22),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          welfareCase.applicantName,
                          style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                        ),
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                          decoration: BoxDecoration(
                            color: statusColor.withOpacity(0.12),
                            borderRadius: BorderRadius.circular(6),
                          ),
                          child: Text(
                            _getStatusLabel(welfareCase.status),
                            style: TextStyle(
                              fontSize: 11,
                              fontWeight: FontWeight.bold,
                              color: statusColor,
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 2),
                    Text(
                      '${welfareCase.categoryTitleBn} • ${welfareCase.fundTypeTitleBn}',
                      style: TextStyle(fontSize: 12, color: Colors.grey.shade600),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          const Divider(height: 1),
          const SizedBox(height: 8),

          // Location & Contact
          Row(
            children: [
              const Icon(Icons.location_on_outlined, size: 14, color: Colors.grey),
              const SizedBox(width: 4),
              Expanded(
                child: Text(
                  welfareCase.address,
                  style: TextStyle(fontSize: 12, color: Colors.grey.shade700),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            ],
          ),
          const SizedBox(height: 2),
          Row(
            children: [
              const Icon(Icons.phone_outlined, size: 14, color: Colors.grey),
              const SizedBox(width: 4),
              Text(
                BengaliNumberFormatter.toBengali(welfareCase.applicantMobile),
                style: TextStyle(fontSize: 12, color: Colors.grey.shade700),
              ),
            ],
          ),

          if (welfareCase.notes.isNotEmpty) ...[
            const SizedBox(height: 6),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: Colors.grey.withOpacity(0.06),
                borderRadius: BorderRadius.circular(6),
              ),
              child: Text(
                welfareCase.notes,
                style: TextStyle(fontSize: 11, color: Colors.grey.shade800),
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
            ),
          ],
          const SizedBox(height: 8),

          // Financial Row & Action
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'প্রার্থিত: ৳ ${BengaliNumberFormatter.toBengali(welfareCase.requestedAmount.toInt().toString())}',
                    style: TextStyle(fontSize: 12, color: Colors.grey.shade600),
                  ),
                  if (welfareCase.approvedAmount > 0)
                    Text(
                      'মঞ্জুরকৃত: ৳ ${BengaliNumberFormatter.toBengali(welfareCase.approvedAmount.toInt().toString())}',
                      style: const TextStyle(fontSize: 13, fontWeight: FontWeight.bold, color: AppColors.emeraldPrimary),
                    ),
                ],
              ),
              if (welfareCase.isPending && onApprove != null)
                ElevatedButton.icon(
                  onPressed: onApprove,
                  icon: const Icon(Icons.check, size: 14),
                  label: const Text('অনুমোদন করুন', style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.roleTreasurer,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(6)),
                  ),
                ),
            ],
          ),
        ],
      ),
    );
  }
}
