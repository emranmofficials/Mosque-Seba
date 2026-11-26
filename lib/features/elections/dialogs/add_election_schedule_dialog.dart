import 'package:flutter/material.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../shared/widgets/app_button.dart';
import '../../../shared/widgets/app_text_field.dart';
import '../models/election_schedule_model.dart';

/// Modal dialog to announce a new committee election schedule
class AddElectionScheduleDialog extends StatefulWidget {
  final Future<bool> Function(ElectionScheduleModel schedule) onSubmit;

  const AddElectionScheduleDialog({super.key, required this.onSubmit});

  @override
  State<AddElectionScheduleDialog> createState() => _AddElectionScheduleDialogState();
}

class _AddElectionScheduleDialogState extends State<AddElectionScheduleDialog> {
  final _formKey = GlobalKey<FormState>();

  final _termTitleController = TextEditingController(text: '২০২৬–২০২৮ সেশন পরিচালনা কমিটি নির্বাচন');
  final _commissionerController = TextEditingController(text: 'মাওলানা মোঃ আব্দুল লতিফ (প্রধান নির্বাচন কমিশনার)');
  final _nominationStartController = TextEditingController(text: '2026-04-01');
  final _nominationEndController = TextEditingController(text: '2026-04-10');
  final _votingDateController = TextEditingController(text: '2026-04-20');
  final _gazetteNotesController = TextEditingController(text: 'মসজিদ সংবিধির অনুচ্ছেদ ১২ অনুযায়ী নির্বাচন পরিচালিত হবে।');
  bool _isLoading = false;

  @override
  void dispose() {
    _termTitleController.dispose();
    _commissionerController.dispose();
    _nominationStartController.dispose();
    _nominationEndController.dispose();
    _votingDateController.dispose();
    _gazetteNotesController.dispose();
    super.dispose();
  }

  Future<void> _handleSave() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    final schedule = ElectionScheduleModel(
      id: '',
      termTitle: _termTitleController.text.trim(),
      commissionerName: _commissionerController.text.trim(),
      nominationStartDate: _nominationStartController.text.trim(),
      nominationEndDate: _nominationEndController.text.trim(),
      votingDate: _votingDateController.text.trim(),
      gazetteNotes: _gazetteNotesController.text.trim(),
      createdAt: DateTime.now().millisecondsSinceEpoch,
    );

    final ok = await widget.onSubmit(schedule);

    if (mounted) {
      setState(() => _isLoading = false);
      if (ok) {
        Navigator.of(context).pop();
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('নির্বাচনী তফসিল সফলভাবে ঘোষিত হয়েছে')),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('তফসিল ঘোষণা করতে ব্যর্থ হয়েছে')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusLg)),
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 480),
        child: Padding(
          padding: const EdgeInsets.all(AppDimens.spacing20),
          child: SingleChildScrollView(
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
                        'নির্বাচনী তফসিল ঘোষণা',
                        style: AppTypography.titleLarge.copyWith(fontWeight: FontWeight.bold),
                      ),
                      IconButton(
                        icon: const Icon(Icons.close),
                        onPressed: () => Navigator.of(context).pop(),
                      ),
                    ],
                  ),
                  const Divider(),
                  const SizedBox(height: AppDimens.spacing12),
                  AppTextField(
                    label: 'নির্বাচনী সেশন ও শিরোনাম *',
                    controller: _termTitleController,
                    validator: (v) => v == null || v.trim().isEmpty ? 'শিরোনাম আবশ্যক' : null,
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  AppTextField(
                    label: 'প্রধান নির্বাচন কমিশনার *',
                    controller: _commissionerController,
                    validator: (v) => v == null || v.trim().isEmpty ? 'কমিশনার আবশ্যক' : null,
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  Row(
                    children: [
                      Expanded(
                        child: AppTextField(
                          label: 'মনোনয়ন শেষ তারিখ *',
                          controller: _nominationEndController,
                          validator: (v) => v == null || v.trim().isEmpty ? 'তারিখ আবশ্যক' : null,
                        ),
                      ),
                      const SizedBox(width: AppDimens.spacing12),
                      Expanded(
                        child: AppTextField(
                          label: 'ভোটগ্রহণের তারিখ *',
                          controller: _votingDateController,
                          validator: (v) => v == null || v.trim().isEmpty ? 'তারিখ আবশ্যক' : null,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  AppTextField(
                    label: 'গেজেট ও নির্বাচনী বিধিমালা',
                    maxLines: 3,
                    controller: _gazetteNotesController,
                  ),
                  const SizedBox(height: AppDimens.spacing20),
                  Row(
                    children: [
                      Expanded(
                        child: AppButton(
                          label: 'বাতিল',
                          variant: AppButtonVariant.outline,
                          onPressed: () => Navigator.of(context).pop(),
                        ),
                      ),
                      const SizedBox(width: AppDimens.spacing12),
                      Expanded(
                        child: AppButton(
                          label: 'তফসিল ঘোষণা করুন',
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
      ),
    );
  }
}
