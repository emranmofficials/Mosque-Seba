import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/widgets/card_shell.dart';
import '../models/meeting_model.dart';

/// Card component presenting a mosque executive committee meeting
class MeetingCard extends StatelessWidget {
  final MeetingModel meeting;
  final VoidCallback onRecordResolution;

  const MeetingCard({
    super.key,
    required this.meeting,
    required this.onRecordResolution,
  });

  @override
  Widget build(BuildContext context) {
    final isCompleted = meeting.isCompleted;

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
                  color: isCompleted
                      ? AppColors.success.withOpacity(0.15)
                      : AppColors.primaryContainer,
                  borderRadius: BorderRadius.circular(AppDimens.radiusSm),
                ),
                child: Icon(
                  isCompleted ? Icons.check_circle_outline : Icons.groups_outlined,
                  color: isCompleted ? AppColors.success : AppColors.primary,
                  size: 24,
                ),
              ),
              const SizedBox(width: AppDimens.spacing12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      meeting.title,
                      style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    const SizedBox(height: 2),
                    Row(
                      children: [
                        const Icon(Icons.location_on_outlined, size: 13, color: AppColors.textSecondary),
                        const SizedBox(width: 3),
                        Text(
                          meeting.location,
                          style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(
                  color: isCompleted
                      ? AppColors.success.withOpacity(0.12)
                      : AppColors.primaryContainer,
                  borderRadius: BorderRadius.circular(AppDimens.radiusSm),
                ),
                child: Text(
                  isCompleted ? 'সম্পন্ন' : 'আসন্ন সভা',
                  style: AppTypography.caption.copyWith(
                    color: isCompleted ? AppColors.success : AppColors.primary,
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
              const Icon(Icons.event_outlined, size: 14, color: AppColors.textSecondary),
              const SizedBox(width: 4),
              Text(
                'তারিখ ও সময়: ${BengaliNumberFormatter.toBengali(meeting.meetingDate)}',
                style: AppTypography.bodySmall.copyWith(color: AppColors.textSecondary),
              ),
            ],
          ),
          if (meeting.agenda.isNotEmpty) ...[
            const SizedBox(height: 6),
            Text(
              'আলোচ্যসূচি: ${meeting.agenda}',
              style: AppTypography.bodySmall.copyWith(color: AppColors.textPrimary),
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
            ),
          ],
          if (meeting.resolution.isNotEmpty) ...[
            const SizedBox(height: AppDimens.spacing8),
            Container(
              padding: const EdgeInsets.all(AppDimens.spacing10),
              decoration: BoxDecoration(
                color: AppColors.success.withOpacity(0.08),
                borderRadius: BorderRadius.circular(AppDimens.radiusSm),
                border: Border.all(color: AppColors.success.withOpacity(0.2)),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'গৃহীত সিদ্ধান্তসমূহ (Resolutions):',
                    style: AppTypography.caption.copyWith(
                      fontWeight: FontWeight.bold,
                      color: AppColors.success,
                    ),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    meeting.resolution,
                    style: AppTypography.bodySmall,
                  ),
                ],
              ),
            ),
          ],
          const SizedBox(height: AppDimens.spacing12),
          Align(
            alignment: Alignment.centerRight,
            child: OutlinedButton.icon(
              icon: Icon(
                isCompleted ? Icons.edit_note : Icons.playlist_add_check,
                size: 16,
              ),
              label: Text(isCompleted ? 'সিদ্ধান্ত হালনাগাদ' : 'কার্যবিবরণী ও সিদ্ধান্ত লিপিবদ্ধ'),
              onPressed: onRecordResolution,
            ),
          ),
        ],
      ),
    );
  }
}
