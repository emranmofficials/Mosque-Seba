import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../shared/widgets/app_button.dart';
import '../../../shared/widgets/app_text_field.dart';
import '../models/meeting_model.dart';

/// Modal dialog to record official resolutions and meeting decisions (কার্যবিবরণী ও সিদ্ধান্ত লিপিবদ্ধ)
class RecordResolutionDialog extends StatefulWidget {
  final MeetingModel meeting;
  final Future<bool> Function(String meetingId, String resolution) onSave;

  const RecordResolutionDialog({
    super.key,
    required this.meeting,
    required this.onSave,
  });

  @override
  State<RecordResolutionDialog> createState() => _RecordResolutionDialogState();
}

class _RecordResolutionDialogState extends State<RecordResolutionDialog> {
  final _formKey = GlobalKey<FormState>();

  late TextEditingController _resolutionController;
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _resolutionController = TextEditingController(text: widget.meeting.resolution);
  }

  @override
  void dispose() {
    _resolutionController.dispose();
    super.dispose();
  }

  Future<void> _handleSave() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    final ok = await widget.onSave(
      widget.meeting.id,
      _resolutionController.text.trim(),
    );

    if (mounted) {
      setState(() => _isLoading = false);
      if (ok) {
        Navigator.of(context).pop();
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('সভার সিদ্ধান্তসমূহ সফলভাবে সংরক্ষিত হয়েছে')),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('সিদ্ধান্ত সংরক্ষণ করতে ব্যর্থ হয়েছে')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusLg)),
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 500),
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
                        'সভার সিদ্ধান্ত ও কার্যবিবরণী',
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
                    widget.meeting.title,
                    style: AppTypography.titleMedium.copyWith(
                      fontWeight: FontWeight.bold,
                      color: AppColors.primary,
                    ),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    'তারিখ: ${widget.meeting.meetingDate} • স্থান: ${widget.meeting.location}',
                    style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  AppTextField(
                    label: 'গৃহীত সিদ্ধান্তসমূহ (Resolutions) *',
                    hint: 'সভায় সর্বসম্মতিক্রমে গৃহীত প্রস্তাব ও সিদ্ধান্তসমূহ বিস্তারিত লিখুন...',
                    maxLines: 6,
                    controller: _resolutionController,
                    validator: (v) => v == null || v.trim().isEmpty ? 'সিদ্ধান্ত বিবরণ আবশ্যক' : null,
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
                          label: 'সংরক্ষণ ও সম্পন্ন করুন',
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
