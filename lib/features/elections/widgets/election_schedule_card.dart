import 'package:flutter/material.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/utils/bengali_number_formatter.dart';
import '../../../../shared/widgets/card_shell.dart';
import '../models/election_schedule_model.dart';

/// Presentation card displaying election term timeline and chief commissioner
class ElectionScheduleCard extends StatelessWidget {
  final ElectionScheduleModel schedule;

  const ElectionScheduleCard({super.key, required this.schedule});

  @override
  Widget build(BuildContext context) {
    return CardShell(
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
                child: const Icon(Icons.how_to_vote, color: AppColors.primary, size: 26),
              ),
              const SizedBox(width: AppDimens.spacing12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      schedule.termTitle,
                      style: AppTypography.titleLarge.copyWith(fontWeight: FontWeight.bold),
                    ),
                    Text(
                      'প্রধান নির্বাচন কমিশনার: ${schedule.commissionerName}',
                      style: AppTypography.caption.copyWith(color: AppColors.primary, fontWeight: FontWeight.bold),
                    ),
                  ],
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(
                  color: schedule.isCompleted
                      ? AppColors.success.withValues(alpha: 0.12)
                      : AppColors.gold.withValues(alpha: 0.15),
                  borderRadius: BorderRadius.circular(AppDimens.radiusSm),
                ),
                child: Text(
                  schedule.isCompleted ? 'সম্পন্ন' : 'তফসিল ঘোষিত',
                  style: AppTypography.caption.copyWith(
                    color: schedule.isCompleted ? AppColors.success : AppColors.gold,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: AppDimens.spacing16),
          const Divider(),
          const SizedBox(height: AppDimens.spacing8),
          _buildTimelineRow('মনোনয়ন জমাদানের শেষ সময়', schedule.nominationEndDate),
          _buildTimelineRow('ভোটগ্রহণ ও ফলাফল ঘোষণা', schedule.votingDate, isHighlight: true),
          if (schedule.gazetteNotes.isNotEmpty) ...[
            const SizedBox(height: 8),
            Text(
              schedule.gazetteNotes,
              style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildTimelineRow(String label, String date, {bool isHighlight = false}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 3),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: AppTypography.bodySmall.copyWith(color: AppColors.textSecondary)),
          Text(
            BengaliNumberFormatter.toBengali(date),
            style: AppTypography.bodySmall.copyWith(
              fontWeight: FontWeight.bold,
              color: isHighlight ? AppColors.primary : AppColors.textPrimary,
            ),
          ),
        ],
      ),
    );
  }
}
