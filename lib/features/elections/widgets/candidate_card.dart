import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/widgets/card_shell.dart';
import '../models/election_candidate_model.dart';

/// Presentation card displaying candidate info, election symbol, and vote count
class CandidateCard extends StatelessWidget {
  final ElectionCandidateModel candidate;
  final VoidCallback onRecordVote;

  const CandidateCard({
    super.key,
    required this.candidate,
    required this.onRecordVote,
  });

  @override
  Widget build(BuildContext context) {
    return CardShell(
      padding: const EdgeInsets.all(AppDimens.spacing12),
      border: candidate.isElected
          ? Border.all(color: AppColors.success.withOpacity(0.6), width: 1.5)
          : null,
      child: Row(
        children: [
          CircleAvatar(
            radius: 22,
            backgroundColor: candidate.isElected
                ? AppColors.success.withOpacity(0.15)
                : AppColors.primaryContainer,
            child: Text(
              candidate.symbolOrPanel.characters.first,
              style: AppTypography.titleMedium.copyWith(
                fontWeight: FontWeight.bold,
                color: candidate.isElected ? AppColors.success : AppColors.primary,
              ),
            ),
          ),
          const SizedBox(width: AppDimens.spacing12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Flexible(
                      child: Text(
                        candidate.candidateName,
                        style: AppTypography.titleSmall.copyWith(fontWeight: FontWeight.bold),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                    if (candidate.isElected) ...[
                      const SizedBox(width: 6),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 1),
                        decoration: BoxDecoration(
                          color: AppColors.success.withOpacity(0.15),
                          borderRadius: BorderRadius.circular(3),
                        ),
                        child: Text(
                          'নির্বাচিত',
                          style: AppTypography.caption.copyWith(
                            color: AppColors.success,
                            fontSize: 10,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                    ],
                  ],
                ),
                const SizedBox(height: 2),
                Text(
                  'পদ: ${candidate.targetRoleBn} • প্রতীক/প্যানেল: ${candidate.symbolOrPanel}',
                  style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
                ),
                if (candidate.proposerName.isNotEmpty)
                  Text(
                    'প্রস্তাবক: ${candidate.proposerName} | সমর্থক: ${candidate.seconderName}',
                    style: AppTypography.caption.copyWith(color: AppColors.textSecondary, fontSize: 10),
                  ),
              ],
            ),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Text(
                '${BengaliNumberFormatter.toBengali(candidate.votesReceived.toString())} ভোট',
                style: AppTypography.titleMedium.copyWith(
                  fontWeight: FontWeight.bold,
                  color: candidate.isElected ? AppColors.success : AppColors.primary,
                ),
              ),
              const SizedBox(height: 4),
              InkWell(
                onTap: onRecordVote,
                child: Padding(
                  padding: const EdgeInsets.all(4),
                  child: Text(
                    'ভোট লিপিবদ্ধ',
                    style: AppTypography.caption.copyWith(
                      color: AppColors.primary,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
