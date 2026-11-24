import 'package:flutter/material.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../shared/widgets/app_button.dart';
import '../../../../shared/widgets/app_text_field.dart';
import '../models/meeting_model.dart';

/// Modal dialog to schedule a new executive committee meeting
class ScheduleMeetingDialog extends StatefulWidget {
  final Future<bool> Function(MeetingModel meeting) onSubmit;

  const ScheduleMeetingDialog({super.key, required this.onSubmit});

  @override
  State<ScheduleMeetingDialog> createState() => _ScheduleMeetingDialogState();
}

class _ScheduleMeetingDialogState extends State<ScheduleMeetingDialog> {
  final _formKey = GlobalKey<FormState>();

  final _titleController = TextEditingController(text: 'কার্যনির্বাহী কমিটির মাসিক সাধারণ সভা');
  final _dateController = TextEditingController(
    text: DateTime.now().toIso8601String().split('T').first,
  );
  final _locationController = TextEditingController(text: 'মসজিদ কমপ্লেক্স কার্যালয়');
  final _agendaController = TextEditingController(
    text: '১. বিগত সভার সিদ্ধান্ত পর্যালোচনা।\n২. মাসিক আয়-ব্যয় অনুমোদন।\n৩. মসজিদ সংস্কার ও বিবিধ।',
  );
  bool _isLoading = false;

  @override
  void dispose() {
    _titleController.dispose();
    _dateController.dispose();
    _locationController.dispose();
    _agendaController.dispose();
    super.dispose();
  }

  Future<void> _handleSave() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    final meeting = MeetingModel(
      id: '',
      title: _titleController.text.trim(),
      meetingDate: _dateController.text.trim(),
      location: _locationController.text.trim(),
      agenda: _agendaController.text.trim(),
      status: 'upcoming',
      createdAt: DateTime.now().millisecondsSinceEpoch,
    );

    final ok = await widget.onSubmit(meeting);

    if (mounted) {
      setState(() => _isLoading = false);
      if (ok) {
        Navigator.of(context).pop();
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('সভা সফলভাবে আহ্বান ও নির্ধারিত হয়েছে')),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('সভা নির্ধারণ করতে ব্যর্থ হয়েছে')),
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
                        'কমিটির সভা আহ্বান',
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
                    label: 'সভার শিরোনাম *',
                    controller: _titleController,
                    validator: (v) => v == null || v.trim().isEmpty ? 'শিরোনাম আবশ্যক' : null,
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  Row(
                    children: [
                      Expanded(
                        child: AppTextField(
                          label: 'সভার তারিখ ও সময় *',
                          hint: 'YYYY-MM-DD',
                          controller: _dateController,
                          validator: (v) => v == null || v.trim().isEmpty ? 'তারিখ আবশ্যক' : null,
                        ),
                      ),
                      const SizedBox(width: AppDimens.spacing12),
                      Expanded(
                        child: AppTextField(
                          label: 'সভার স্থান *',
                          controller: _locationController,
                          validator: (v) => v == null || v.trim().isEmpty ? 'স্থান আবশ্যক' : null,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  AppTextField(
                    label: 'আলোচ্যসূচি (Agenda) *',
                    hint: 'সভার আলোচ্য বিষয়সমূহ...',
                    maxLines: 4,
                    controller: _agendaController,
                    validator: (v) => v == null || v.trim().isEmpty ? 'আলোচ্যসূচি আবশ্যক' : null,
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
                          text: 'সভা নির্ধারণ করুন',
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
