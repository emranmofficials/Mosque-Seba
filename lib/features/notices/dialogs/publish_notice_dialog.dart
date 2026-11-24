import 'package:flutter/material.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../shared/widgets/app_button.dart';
import '../../../../shared/widgets/app_text_field.dart';
import '../models/notice_model.dart';

/// Modal dialog to draft and publish a new official mosque notice
class PublishNoticeDialog extends StatefulWidget {
  final String authorName;
  final Future<bool> Function(NoticeModel notice) onSubmit;

  const PublishNoticeDialog({
    super.key,
    required this.authorName,
    required this.onSubmit,
  });

  @override
  State<PublishNoticeDialog> createState() => _PublishNoticeDialogState();
}

class _PublishNoticeDialogState extends State<PublishNoticeDialog> {
  final _formKey = GlobalKey<FormState>();

  final _titleController = TextEditingController();
  final _contentController = TextEditingController();
  String _priority = 'normal';
  bool _isPinned = false;
  bool _isLoading = false;

  final Map<String, String> _priorityMap = {
    'normal': 'সাধারণ নোটিশ',
    'important': 'গুরুত্বপূর্ণ নোটিশ',
    'urgent': 'জরুরি নোটিশ / ঘোষণা',
  };

  @override
  void dispose() {
    _titleController.dispose();
    _contentController.dispose();
    super.dispose();
  }

  Future<void> _handlePublish() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    final notice = NoticeModel(
      id: '',
      title: _titleController.text.trim(),
      content: _contentController.text.trim(),
      priority: _priority,
      isPinned: _isPinned,
      publishDate: DateTime.now().toIso8601String().split('T').first,
      createdBy: widget.authorName,
      createdAt: DateTime.now().millisecondsSinceEpoch,
    );

    final ok = await widget.onSubmit(notice);

    if (mounted) {
      setState(() => _isLoading = false);
      if (ok) {
        Navigator.of(context).pop();
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('নোটিশ সফলভাবে প্রকাশিত হয়েছে')),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('নোটিশ প্রকাশ করতে ব্যর্থ হয়েছে')),
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
                        'নতুন নোটিশ প্রকাশ',
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
                    label: 'নোটিশের শিরোনাম *',
                    hint: 'যেমন: মাহে রমজানের তারাবীহ নামাজের সূচি',
                    controller: _titleController,
                    validator: (v) => v == null || v.trim().isEmpty ? 'শিরোনাম আবশ্যক' : null,
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  Text('গুরুত্ব ও অগ্রাধিকার *', style: AppTypography.bodySmall.copyWith(fontWeight: FontWeight.w600)),
                  const SizedBox(height: 4),
                  DropdownButtonFormField<String>(
                    value: _priority,
                    decoration: InputDecoration(
                      filled: true,
                      fillColor: AppColors.surface,
                      contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusMd)),
                    ),
                    items: _priorityMap.entries.map((e) {
                      return DropdownMenuItem(value: e.key, child: Text(e.value));
                    }).toList(),
                    onChanged: (val) {
                      if (val != null) setState(() => _priority = val);
                    },
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  AppTextField(
                    label: 'নোটিশের মূল বিবরণ *',
                    hint: 'বিজ্ঞপ্তির বিস্তারিত বিবরণ লিখুন...',
                    maxLines: 4,
                    controller: _contentController,
                    validator: (v) => v == null || v.trim().isEmpty ? 'বিবরণ আবশ্যক' : null,
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  CheckboxListTile(
                    contentPadding: EdgeInsets.zero,
                    activeColor: AppColors.primary,
                    title: const Text('নোটিশ বোর্ডের শীর্ষে পিন করে রাখুন'),
                    value: _isPinned,
                    onChanged: (v) => setState(() => _isPinned = v ?? false),
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
                          text: 'প্রকাশ করুন',
                          isLoading: _isLoading,
                          onPressed: _handlePublish,
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
