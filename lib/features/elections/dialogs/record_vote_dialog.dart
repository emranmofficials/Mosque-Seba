import 'package:flutter/material.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../shared/widgets/app_button.dart';
import '../../../../shared/widgets/app_text_field.dart';
import '../models/election_candidate_model.dart';

/// Modal dialog to record vote count and victory declaration for a candidate
class RecordVoteDialog extends StatefulWidget {
  final ElectionCandidateModel candidate;
  final Future<bool> Function(String candidateId, int votes, bool isElected) onSubmit;

  const RecordVoteDialog({
    super.key,
    required this.candidate,
    required this.onSubmit,
  });

  @override
  State<RecordVoteDialog> createState() => _RecordVoteDialogState();
}

class _RecordVoteDialogState extends State<RecordVoteDialog> {
  final _formKey = GlobalKey<FormState>();

  late TextEditingController _votesController;
  late bool _isElected;
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _votesController = TextEditingController(text: widget.candidate.votesReceived.toString());
    _isElected = widget.candidate.isElected;
  }

  @override
  void dispose() {
    _votesController.dispose();
    super.dispose();
  }

  Future<void> _handleSave() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    final votes = int.tryParse(_votesController.text.trim()) ?? 0;
    final ok = await widget.onSubmit(widget.candidate.id, votes, _isElected);

    if (mounted) {
      setState(() => _isLoading = false);
      if (ok) {
        Navigator.of(context).pop();
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('ভোট ও ফলাফল সফলভাবে সংরক্ষিত হয়েছে')),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('সংরক্ষণ করতে ব্যর্থ হয়েছে')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusLg)),
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 420),
        child: Padding(
          padding: const EdgeInsets.all(AppDimens.spacing20),
          child: Form(
            key: _formKey,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      'ভোট ও ফলাফল ঘোষণা',
                      style: AppTypography.titleLarge.copyWith(fontWeight: FontWeight.bold),
                    ),
                    IconButton(
                      icon: const Icon(Icons.close),
                      onPressed: () => Navigator.of(context).pop(),
                    ),
                  ],
                ),
                const Divider(),
                const SizedBox(height: AppDimens.spacing8),
                Text(
                  widget.candidate.candidateName,
                  style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold),
                ),
                Text(
                  'পদ: ${widget.candidate.targetRoleBn} • প্রতীক: ${widget.candidate.symbolOrPanel}',
                  style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
                ),
                const SizedBox(height: AppDimens.spacing16),
                AppTextField(
                  label: 'প্রাপ্ত মোট ভোট সংখ্যা *',
                  keyboardType: TextInputType.number,
                  controller: _votesController,
                  validator: (v) => v == null || v.trim().isEmpty ? 'ভোট সংখ্যা আবশ্যক' : null,
                ),
                const SizedBox(height: AppDimens.spacing12),
                CheckboxListTile(
                  contentPadding: EdgeInsets.zero,
                  activeColor: AppColors.success,
                  title: const Text('বিজয়ী প্রার্থী হিসেবে ঘোষণা করুন (Elected)'),
                  value: _isElected,
                  onChanged: (v) => setState(() => _isElected = v ?? false),
                ),
                const SizedBox(height: AppDimens.spacing20),
                Row(
                  children: [
                    Expanded(
                      child: AppButton(
                        text: 'বাতিল',
                        variant: AppButtonVariant.outline,
                        onPressed: () => Navigator.of(context).pop(),
                      ),
                    ),
                    const SizedBox(width: AppDimens.spacing12),
                    Expanded(
                      child: AppButton(
                        text: 'ফলাফল সংরক্ষণ',
                        isLoading: _isLoading,
                        onPressed: _handleSave,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
