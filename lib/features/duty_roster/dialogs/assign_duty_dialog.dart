import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../shared/widgets/app_button.dart';
import '../../../shared/widgets/app_text_field.dart';
import '../../staff/models/staff_model.dart';
import '../models/duty_roster_item.dart';

/// Modal dialog to assign or swap staff duties for a waqt on a given day of the week
class AssignDutyDialog extends StatefulWidget {
  final String dayOfWeek;
  final String waqtName;
  final List<StaffModel> staffList;
  final Future<bool> Function(DutyRosterItem item) onAssign;

  const AssignDutyDialog({
    super.key,
    required this.dayOfWeek,
    required this.waqtName,
    required this.staffList,
    required this.onAssign,
  });

  @override
  State<AssignDutyDialog> createState() => _AssignDutyDialogState();
}

class _AssignDutyDialogState extends State<AssignDutyDialog> {
  final _formKey = GlobalKey<FormState>();

  late StaffModel? _selectedStaff;
  String _selectedRoleTitle = 'ইমামতি';
  final _notesController = TextEditingController();
  bool _isLoading = false;

  final List<String> _roleTitles = [
    'ইমামতি',
    'আযান ও একামত',
    'খুতবা ও বয়ান',
    'মসজিদ পরিচালনা',
    'বদলি ইমাম',
  ];

  @override
  void initState() {
    super.initState();
    _selectedStaff = widget.staffList.isNotEmpty ? widget.staffList.first : null;
  }

  @override
  void dispose() {
    _notesController.dispose();
    super.dispose();
  }

  Future<void> _handleSave() async {
    if (!_formKey.currentState!.validate() || _selectedStaff == null) return;

    setState(() => _isLoading = true);

    final item = DutyRosterItem(
      id: '',
      staffId: _selectedStaff!.id,
      staffNameBn: _selectedStaff!.nameBn,
      dayOfWeek: widget.dayOfWeek,
      prayerName: widget.waqtName,
      roleTitle: _selectedRoleTitle,
      notes: _notesController.text.trim(),
    );

    final ok = await widget.onAssign(item);

    if (mounted) {
      setState(() => _isLoading = false);
      if (ok) {
        Navigator.of(context).pop();
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('দায়িত্ব সফলভাবে নির্ধারণ করা হয়েছে')),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('দায়িত্ব সংরক্ষণ করা যায়নি')),
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
                        'দায়িত্ব বণ্টন (${widget.dayOfWeek})',
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
                  // Waqt Header Banner
                  Container(
                    padding: const EdgeInsets.all(AppDimens.spacing10),
                    decoration: BoxDecoration(
                      color: AppColors.primaryContainer,
                      borderRadius: BorderRadius.circular(AppDimens.radiusMd),
                    ),
                    child: Row(
                      children: [
                        const Icon(Icons.access_time, color: AppColors.primary, size: 20),
                        const SizedBox(width: 8),
                        Text(
                          'ওয়াক্ত: ${widget.waqtName} • বার: ${widget.dayOfWeek}',
                          style: AppTypography.titleSmall.copyWith(
                            fontWeight: FontWeight.bold,
                            color: AppColors.primary,
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: AppDimens.spacing16),
                  Text('দায়িত্বপ্রাপ্ত কর্মকর্তা *', style: AppTypography.bodySmall.copyWith(fontWeight: FontWeight.w600)),
                  const SizedBox(height: 4),
                  DropdownButtonFormField<StaffModel>(
                    value: _selectedStaff,
                    decoration: InputDecoration(
                      filled: true,
                      fillColor: AppColors.surface,
                      contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusMd)),
                    ),
                    items: widget.staffList.map((s) {
                      return DropdownMenuItem(
                        value: s,
                        child: Text('${s.nameBn} (${s.designationBn})'),
                      );
                    }).toList(),
                    onChanged: (val) {
                      if (val != null) setState(() => _selectedStaff = val);
                    },
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  Text('দায়িত্বের ধরন *', style: AppTypography.bodySmall.copyWith(fontWeight: FontWeight.w600)),
                  const SizedBox(height: 4),
                  DropdownButtonFormField<String>(
                    value: _selectedRoleTitle,
                    decoration: InputDecoration(
                      filled: true,
                      fillColor: AppColors.surface,
                      contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusMd)),
                    ),
                    items: _roleTitles.map((r) => DropdownMenuItem(value: r, child: Text(r))).toList(),
                    onChanged: (v) {
                      if (v != null) setState(() => _selectedRoleTitle = v);
                    },
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  AppTextField(
                    label: 'মন্তব্য / বিকল্প সূচি',
                    hint: 'যেমন: ছুটি বা বদলি ইমাম',
                    controller: _notesController,
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
                          label: 'বরাদ্দ নিশ্চিত করুন',
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
