import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/theme/app_typography.dart';
import '../../../shared/widgets/app_button.dart';

/// Bottom sheet for filtering members by role and volunteer status
class MemberFilterSheet extends StatefulWidget {
  final String currentRole;
  final bool mealVolunteerOnly;
  final Function(String role, bool mealVolunteer) onApply;

  const MemberFilterSheet({
    super.key,
    required this.currentRole,
    required this.mealVolunteerOnly,
    required this.onApply,
  });

  @override
  State<MemberFilterSheet> createState() => _MemberFilterSheetState();
}

class _MemberFilterSheetState extends State<MemberFilterSheet> {
  late String _selectedRole;
  late bool _mealVolunteer;

  final Map<String, String> _roles = {
    'all': 'সকল সদস্য',
    'general_member': 'সাধারণ সদস্য',
    'life_member': 'আজীবন সদস্য',
    'advisor': 'উপদেষ্টা',
    'executive_member': 'কার্যনির্বাহী সদস্য',
  };

  @override
  void initState() {
    super.initState();
    _selectedRole = widget.currentRole;
    _mealVolunteer = widget.mealVolunteerOnly;
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(AppDimens.spacing20),
      decoration: const BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.vertical(top: Radius.circular(AppDimens.radiusXl)),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'সদস্য ফিল্টার',
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
          Text(
            'সদস্যের ধরন নির্বাচন করুন',
            style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.w600),
          ),
          const SizedBox(height: AppDimens.spacing8),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: _roles.entries.map((entry) {
              final isSelected = _selectedRole == entry.key;
              return ChoiceChip(
                label: Text(entry.value),
                selected: isSelected,
                selectedColor: AppColors.primaryContainer,
                labelStyle: TextStyle(
                  color: isSelected ? AppColors.primary : AppColors.textPrimary,
                  fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                ),
                onSelected: (selected) {
                  if (selected) {
                    setState(() => _selectedRole = entry.key);
                  }
                },
              );
            }).toList(),
          ),
          const SizedBox(height: AppDimens.spacing16),
          SwitchListTile(
            contentPadding: EdgeInsets.zero,
            activeColor: AppColors.primary,
            title: Text(
              'শুধুমাত্র খাবার জোগানদাতা সদস্য',
              style: AppTypography.bodyMedium.copyWith(fontWeight: FontWeight.w500),
            ),
            subtitle: Text(
              'যারা ইমাম-মুয়াজ্জিনের খাবারের দায়িত্ব নিয়েছেন',
              style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
            ),
            value: _mealVolunteer,
            onChanged: (val) => setState(() => _mealVolunteer = val),
          ),
          const SizedBox(height: AppDimens.spacing24),
          Row(
            children: [
              Expanded(
                child: AppButton(
                  label: 'রিসেট',
                  variant: AppButtonVariant.outline,
                  onPressed: () {
                    setState(() {
                      _selectedRole = 'all';
                      _mealVolunteer = false;
                    });
                  },
                ),
              ),
              const SizedBox(width: AppDimens.spacing12),
              Expanded(
                child: AppButton(
                  label: 'প্রয়োগ করুন',
                  onPressed: () {
                    widget.onApply(_selectedRole, _mealVolunteer);
                    Navigator.of(context).pop();
                  },
                ),
              ),
            ],
          ),
          const SizedBox(height: AppDimens.spacing12),
        ],
      ),
    );
  }
}
