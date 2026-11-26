import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../shared/widgets/app_button.dart';
import '../../../shared/widgets/app_text_field.dart';
import '../models/election_candidate_model.dart';

/// Modal dialog to submit a candidate nomination
class NominateCandidateDialog extends StatefulWidget {
  final String scheduleId;
  final Future<bool> Function(ElectionCandidateModel candidate) onSubmit;

  const NominateCandidateDialog({
    super.key,
    required this.scheduleId,
    required this.onSubmit,
  });

  @override
  State<NominateCandidateDialog> createState() => _NominateCandidateDialogState();
}

class _NominateCandidateDialogState extends State<NominateCandidateDialog> {
  final _formKey = GlobalKey<FormState>();

  final _candidateNameController = TextEditingController();
  final _symbolController = TextEditingController(text: 'ছাতা');
  final _proposerController = TextEditingController();
  final _seconderController = TextEditingController();

  String _selectedRole = 'president';
  String _selectedRoleBn = 'সভাপতি';
  bool _isLoading = false;

  final Map<String, String> _rolesMap = {
    'president': 'সভাপতি',
    'vice_president': 'সহ-সভাপতি',
    'general_secretary': 'সাধারণ সম্পাদক',
    'assistant_secretary': 'সহ-সম্পাদক',
    'treasurer': 'কোষাধ্যক্ষ',
    'assistant_treasurer': 'সহ-কোষাধ্যক্ষ',
    'executive_member': 'কার্যনির্বাহী সদস্য',
  };

  @override
  void dispose() {
    _candidateNameController.dispose();
    _symbolController.dispose();
    _proposerController.dispose();
    _seconderController.dispose();
    super.dispose();
  }

  Future<void> _handleSave() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    final candidate = ElectionCandidateModel(
      id: '',
      scheduleId: widget.scheduleId,
      targetRole: _selectedRole,
      targetRoleBn: _selectedRoleBn,
      candidateName: _candidateNameController.text.trim(),
      symbolOrPanel: _symbolController.text.trim(),
      proposerName: _proposerController.text.trim(),
      seconderName: _seconderController.text.trim(),
      votesReceived: 0,
      isElected: false,
    );

    final ok = await widget.onSubmit(candidate);

    if (mounted) {
      setState(() => _isLoading = false);
      if (ok) {
        Navigator.of(context).pop();
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('মনোনয়ন সফলভাবে দাখিল হয়েছে')),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('মনোনয়ন সংরক্ষণ করতে ব্যর্থ হয়েছে')),
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
                        'প্রার্থী মনোনয়ন দাখিল',
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
                  Text('প্রার্থিতার পদবি *', style: AppTypography.bodySmall.copyWith(fontWeight: FontWeight.w600)),
                  const SizedBox(height: 4),
                  DropdownButtonFormField<String>(
                    value: _selectedRole,
                    decoration: InputDecoration(
                      filled: true,
                      fillColor: AppColors.surface,
                      contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusMd)),
                    ),
                    items: _rolesMap.entries.map((e) => DropdownMenuItem(value: e.key, child: Text(e.value))).toList(),
                    onChanged: (val) {
                      if (val != null) {
                        setState(() {
                          _selectedRole = val;
                          _selectedRoleBn = _rolesMap[val] ?? 'সদস্য';
                        });
                      }
                    },
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  AppTextField(
                    label: 'প্রার্থীর নাম *',
                    hint: 'যেমন: হাজী মো: রফিকুল ইসলাম',
                    controller: _candidateNameController,
                    validator: (v) => v == null || v.trim().isEmpty ? 'প্রার্থীর নাম আবশ্যক' : null,
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  AppTextField(
                    label: 'নির্বাচনী প্রতীক / প্যানেল *',
                    hint: 'যেমন: ছাতা, বই, দেয়াল ঘড়ি',
                    controller: _symbolController,
                    validator: (v) => v == null || v.trim().isEmpty ? 'প্রতীক আবশ্যক' : null,
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  Row(
                    children: [
                      Expanded(
                        child: AppTextField(
                          label: 'প্রস্তাবকের নাম *',
                          controller: _proposerController,
                          validator: (v) => v == null || v.trim().isEmpty ? 'প্রস্তাবক আবশ্যক' : null,
                        ),
                      ),
                      const SizedBox(width: AppDimens.spacing12),
                      Expanded(
                        child: AppTextField(
                          label: 'সমর্থকের নাম *',
                          controller: _seconderController,
                          validator: (v) => v == null || v.trim().isEmpty ? 'সমর্থক আবশ্যক' : null,
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
                          label: 'মনোনয়ন জমা দিন',
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
