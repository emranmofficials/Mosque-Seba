import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../shared/widgets/app_button.dart';
import '../../../shared/widgets/app_text_field.dart';
import '../models/janaza_alert_model.dart';

/// Modal dialog to broadcast an emergency community Janaza announcement
class BroadcastJanazaDialog extends StatefulWidget {
  final String mosqueName;
  final Future<bool> Function(JanazaAlertModel alert) onBroadcast;

  const BroadcastJanazaDialog({
    super.key,
    required this.mosqueName,
    required this.onBroadcast,
  });

  @override
  State<BroadcastJanazaDialog> createState() => _BroadcastJanazaDialogState();
}

class _BroadcastJanazaDialogState extends State<BroadcastJanazaDialog> {
  final _formKey = GlobalKey<FormState>();

  final _deceasedNameController = TextEditingController();
  final _guardianNameController = TextEditingController();
  final _ageController = TextEditingController();
  final _villageController = TextEditingController(text: 'মসজিদ সংলগ্ন এলাকা');
  final _deathTimeController = TextEditingController(text: 'আজ আনুমানিক সকাল ১০:০০ ঘটিকা');
  late TextEditingController _venueController;
  final _janazaTimeController = TextEditingController(text: 'আজ বাদ আসর');
  final _graveyardController = TextEditingController(text: 'মসজিদ কেন্দ্রীয় কবরস্থান');
  final _phoneController = TextEditingController();
  String _relation = 'পিতা';
  bool _isLoading = false;

  final List<String> _relations = ['পিতা', 'মাতা', 'স্বামী', 'স্ত্রী', 'সন্তান', 'অন্যান্য'];

  @override
  void initState() {
    super.initState();
    _venueController = TextEditingController(text: '${widget.mosqueName} প্রাঙ্গণ');
  }

  @override
  void dispose() {
    _deceasedNameController.dispose();
    _guardianNameController.dispose();
    _ageController.dispose();
    _villageController.dispose();
    _deathTimeController.dispose();
    _venueController.dispose();
    _janazaTimeController.dispose();
    _graveyardController.dispose();
    _phoneController.dispose();
    super.dispose();
  }

  Future<void> _handleBroadcast() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    final alert = JanazaAlertModel(
      id: '',
      deceasedName: _deceasedNameController.text.trim(),
      relativeRelation: _relation,
      guardianName: _guardianNameController.text.trim(),
      age: _ageController.text.trim(),
      villageArea: _villageController.text.trim(),
      timeOfDeath: _deathTimeController.text.trim(),
      janazaVenue: _venueController.text.trim(),
      janazaTime: _janazaTimeController.text.trim(),
      graveyardVenue: _graveyardController.text.trim(),
      contactPhone: _phoneController.text.trim(),
      broadcastAt: DateTime.now().toIso8601String().split('T').first,
    );

    final ok = await widget.onBroadcast(alert);

    if (mounted) {
      setState(() => _isLoading = false);
      if (ok) {
        Navigator.of(context).pop();
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('জরুরি জানাজা সংবাদ সফলভাবে সম্প্রচারিত হয়েছে')),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('জানাজা সম্প্রচার ব্যর্থ হয়েছে')),
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
                        'জরুরি জানাজা ও শোক সংবাদ',
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
                    label: 'মরহুম / মরহুমার নাম *',
                    hint: 'যেমন: হাজী মো: আব্দুল করিম',
                    controller: _deceasedNameController,
                    validator: (v) => v == null || v.trim().isEmpty ? 'নাম আবশ্যক' : null,
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  Row(
                    children: [
                      Expanded(
                        child: DropdownButtonFormField<String>(
                          value: _relation,
                          decoration: InputDecoration(
                            labelText: 'সম্পর্ক',
                            filled: true,
                            fillColor: AppColors.surface,
                            contentPadding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusMd)),
                          ),
                          items: _relations.map((r) => DropdownMenuItem(value: r, child: Text(r))).toList(),
                          onChanged: (v) {
                            if (v != null) setState(() => _relation = v);
                          },
                        ),
                      ),
                      const SizedBox(width: AppDimens.spacing12),
                      Expanded(
                        flex: 2,
                        child: AppTextField(
                          label: 'পিতা / স্বামীর নাম',
                          controller: _guardianNameController,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  Row(
                    children: [
                      Expanded(
                        child: AppTextField(
                          label: 'বয়স (বছর)',
                          hint: 'যেমন: ৭০',
                          keyboardType: TextInputType.number,
                          controller: _ageController,
                        ),
                      ),
                      const SizedBox(width: AppDimens.spacing12),
                      Expanded(
                        flex: 2,
                        child: AppTextField(
                          label: 'ঠিকানা / গ্রাম',
                          controller: _villageController,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  AppTextField(
                    label: 'মৃত্যুর সময় *',
                    controller: _deathTimeController,
                    validator: (v) => v == null || v.trim().isEmpty ? 'মৃত্যুর সময় আবশ্যক' : null,
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  Row(
                    children: [
                      Expanded(
                        child: AppTextField(
                          label: 'জানাজার স্থান *',
                          controller: _venueController,
                          validator: (v) => v == null || v.trim().isEmpty ? 'স্থান আবশ্যক' : null,
                        ),
                      ),
                      const SizedBox(width: AppDimens.spacing12),
                      Expanded(
                        child: AppTextField(
                          label: 'জানাজার সময় *',
                          hint: 'আজ বাদ আসর বিকাল ৫:০০ মি.',
                          controller: _janazaTimeController,
                          validator: (v) => v == null || v.trim().isEmpty ? 'সময় আবশ্যক' : null,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  Row(
                    children: [
                      Expanded(
                        child: AppTextField(
                          label: 'দাফনের কবরস্থান',
                          controller: _graveyardController,
                        ),
                      ),
                      const SizedBox(width: AppDimens.spacing12),
                      Expanded(
                        child: AppTextField(
                          label: 'যোগাযোগের মোবাইল',
                          keyboardType: TextInputType.phone,
                          controller: _phoneController,
                        ),
                      ),
                    ],
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
                          label: 'সম্প্রচার করুন',
                          isLoading: _isLoading,
                          onPressed: _handleBroadcast,
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
