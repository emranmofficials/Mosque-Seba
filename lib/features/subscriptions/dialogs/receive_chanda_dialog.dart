import 'package:flutter/material.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../shared/widgets/app_button.dart';
import '../../../../shared/widgets/app_text_field.dart';
import '../../membership/models/member_model.dart';
import '../models/chanda_payment_model.dart';

/// Dialog to receive Chanda payment from a member (চাঁদা আদায় ও রসিদ তৈরি)
class ReceiveChandaDialog extends StatefulWidget {
  final MemberModel? initialMember;
  final List<MemberModel> members;
  final Future<ChandaPaymentModel?> Function({
    required String memberId,
    required String memberNameBn,
    required String month,
    required double amount,
    required String method,
    required String receivedBy,
  }) onReceive;

  const ReceiveChandaDialog({
    super.key,
    this.initialMember,
    required this.members,
    required this.onReceive,
  });

  @override
  State<ReceiveChandaDialog> createState() => _ReceiveChandaDialogState();
}

class _ReceiveChandaDialogState extends State<ReceiveChandaDialog> {
  final _formKey = GlobalKey<FormState>();

  late MemberModel? _selectedMember;
  late TextEditingController _amountController;
  late TextEditingController _receivedByController;

  String _selectedMonth = 'মার্চ ২০২৬';
  String _selectedMethod = 'cash';
  bool _isLoading = false;

  final List<String> _months = [
    'জানুয়ারি ২০২৬',
    'ফেব্রুয়ারি ২০২৬',
    'মার্চ ২০২৬',
    'এপ্রিল ২০২৬',
    'মে ২০২৬',
    'জুন ২০২৬',
    'জুলাই ২০২৬',
    'আগস্ট ২০২৬',
    'সেপ্টেম্বর ২০২৬',
    'অক্টোবর ২০২৬',
    'নভেম্বর ২০২৬',
    'ডিসেম্বর ২০২৬',
  ];

  final Map<String, String> _methods = {
    'cash': 'নগদ ক্যাশ',
    'bkash': 'বিকাশ (bKash)',
    'nagad': 'নগদ (Nagad)',
    'bank': 'ব্যাংক হিসাব',
  };

  @override
  void initState() {
    super.initState();
    _selectedMember = widget.initialMember ?? (widget.members.isNotEmpty ? widget.members.first : null);
    _amountController = TextEditingController(
      text: _selectedMember != null && _selectedMember!.monthlyChanda > 0
          ? _selectedMember!.monthlyChanda.toStringAsFixed(0)
          : '100',
    );
    _receivedByController = TextEditingController(text: 'কোষাধ্যক্ষ');
  }

  @override
  void dispose() {
    _amountController.dispose();
    _receivedByController.dispose();
    super.dispose();
  }

  Future<void> _handleCollect() async {
    if (!_formKey.currentState!.validate() || _selectedMember == null) return;

    setState(() => _isLoading = true);

    final amount = double.tryParse(_amountController.text.trim()) ?? 0.0;

    final payment = await widget.onReceive(
      memberId: _selectedMember!.id,
      memberNameBn: _selectedMember!.nameBn,
      month: _selectedMonth,
      amount: amount,
      method: _selectedMethod,
      receivedBy: _receivedByController.text.trim(),
    );

    if (mounted) {
      setState(() => _isLoading = false);
      if (payment != null) {
        Navigator.of(context).pop(payment);
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('চাঁদা গ্রহণ সম্পন্ন করা যায়নি')),
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
                        'চাঁদা গ্রহণ ও রসিদ তৈরি',
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
                  // Member Dropdown / Selection
                  Text('সদস্য নির্বাচন *', style: AppTypography.bodySmall.copyWith(fontWeight: FontWeight.w600)),
                  const SizedBox(height: 4),
                  if (widget.initialMember != null)
                    Container(
                      padding: const EdgeInsets.all(AppDimens.spacing12),
                      decoration: BoxDecoration(
                        color: AppColors.primaryContainer,
                        borderRadius: BorderRadius.circular(AppDimens.radiusMd),
                      ),
                      child: Row(
                        children: [
                          const Icon(Icons.person, color: AppColors.primary),
                          const SizedBox(width: AppDimens.spacing8),
                          Text(
                            widget.initialMember!.nameBn,
                            style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold),
                          ),
                        ],
                      ),
                    )
                  else
                    DropdownButtonFormField<MemberModel>(
                      value: _selectedMember,
                      decoration: InputDecoration(
                        filled: true,
                        fillColor: AppColors.surface,
                        contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                        border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusMd)),
                      ),
                      items: widget.members.map((m) {
                        return DropdownMenuItem(
                          value: m,
                          child: Text('${m.nameBn} (${m.memberCode})'),
                        );
                      }).toList(),
                      onChanged: (val) {
                        if (val != null) {
                          setState(() {
                            _selectedMember = val;
                            if (val.monthlyChanda > 0) {
                              _amountController.text = val.monthlyChanda.toStringAsFixed(0);
                            }
                          });
                        }
                      },
                    ),
                  const SizedBox(height: AppDimens.spacing12),
                  // Month selection
                  Text('পরিশোধিত মাস *', style: AppTypography.bodySmall.copyWith(fontWeight: FontWeight.w600)),
                  const SizedBox(height: 4),
                  DropdownButtonFormField<String>(
                    value: _selectedMonth,
                    decoration: InputDecoration(
                      filled: true,
                      fillColor: AppColors.surface,
                      contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusMd)),
                    ),
                    items: _months.map((m) => DropdownMenuItem(value: m, child: Text(m))).toList(),
                    onChanged: (v) {
                      if (v != null) setState(() => _selectedMonth = v);
                    },
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  Row(
                    children: [
                      Expanded(
                        child: AppTextField(
                          label: 'জমা পরিমাণ (৳) *',
                          keyboardType: TextInputType.number,
                          controller: _amountController,
                          validator: (v) => v == null || v.trim().isEmpty ? 'টাকার পরিমাণ আবশ্যক' : null,
                        ),
                      ),
                      const SizedBox(width: AppDimens.spacing12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text('পরিশোধের মাধ্যম *', style: AppTypography.bodySmall.copyWith(fontWeight: FontWeight.w600)),
                            const SizedBox(height: 4),
                            DropdownButtonFormField<String>(
                              value: _selectedMethod,
                              decoration: InputDecoration(
                                filled: true,
                                fillColor: AppColors.surface,
                                contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                                border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusMd)),
                              ),
                              items: _methods.entries.map((e) => DropdownMenuItem(value: e.key, child: Text(e.value))).toList(),
                              onChanged: (v) {
                                if (v != null) setState(() => _selectedMethod = v);
                              },
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  AppTextField(
                    label: 'আদায়কারীর নাম / পদবি',
                    controller: _receivedByController,
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
                          text: 'জমা নিন ও রসিদ দিন',
                          isLoading: _isLoading,
                          onPressed: _handleCollect,
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
