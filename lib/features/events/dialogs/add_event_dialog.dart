import 'package:flutter/material.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/utils/bengali_number_formatter.dart';
import '../models/mosque_event_model.dart';

/// Modal dialog for scheduling a new Mosque Event or Mahfil
class AddEventDialog extends StatefulWidget {
  final Function(MosqueEventModel) onSubmit;

  const AddEventDialog({super.key, required this.onSubmit});

  @override
  State<AddEventDialog> createState() => _AddEventDialogState();
}

class _AddEventDialogState extends State<AddEventDialog> {
  final _formKey = GlobalKey<FormState>();

  final _titleController = TextEditingController();
  final _venueController = TextEditingController(text: 'মসজিদ প্রাঙ্গণ');
  final _chiefGuestController = TextEditingController();
  final _specialSpeakersController = TextEditingController();
  final _budgetController = TextEditingController(text: '50000');
  final _descriptionController = TextEditingController();

  String _eventType = 'waz_mahfil';
  String _eventTime = 'বাদ আসর';
  DateTime _eventDate = DateTime.now().add(const Duration(days: 15));

  final List<Map<String, String>> _types = [
    {'code': 'waz_mahfil', 'label': 'ওয়াজ ও দোয়ার মাহফিল'},
    {'code': 'shab_e_qadr', 'label': 'পবিত্র শবে কদর ও কিয়ামুল লাইল'},
    {'code': 'iftar_party', 'label': 'গণ-ইফতার ও দুস্থ বিতরণ'},
    {'code': 'milad_doa', 'label': 'মিলাদ ও বিশেষ দোয়া'},
    {'code': 'eid_gathering', 'label': 'ঈদ জামাত ও সমাবেশ'},
  ];

  @override
  void dispose() {
    _titleController.dispose();
    _venueController.dispose();
    _chiefGuestController.dispose();
    _specialSpeakersController.dispose();
    _budgetController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  void _submit() {
    if (!_formKey.currentState!.validate()) return;

    final dateStr =
        '${_eventDate.year}-${_eventDate.month.toString().padLeft(2, '0')}-${_eventDate.day.toString().padLeft(2, '0')}';

    final event = MosqueEventModel(
      id: 'ev-${DateTime.now().millisecondsSinceEpoch}',
      title: _titleController.text.trim(),
      eventType: _eventType,
      eventDate: dateStr,
      eventTime: _eventTime,
      venue: _venueController.text.trim(),
      chiefGuest: _chiefGuestController.text.trim(),
      specialSpeakers: _specialSpeakersController.text.trim(),
      estimatedBudget: double.tryParse(_budgetController.text.trim()) ?? 0.0,
      status: 'upcoming',
      description: _descriptionController.text.trim(),
      createdAt: DateTime.now().millisecondsSinceEpoch,
    );

    widget.onSubmit(event);
    Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusCard)),
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 500, maxHeight: 660),
        child: Padding(
          padding: const EdgeInsets.all(AppDimens.spacing20),
          child: Form(
            key: _formKey,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Row(
                      children: [
                        Container(
                          padding: const EdgeInsets.all(8),
                          decoration: BoxDecoration(
                            color: AppColors.goldDeep.withOpacity(0.12),
                            shape: BoxShape.circle,
                          ),
                          child: const Icon(Icons.event_available, color: AppColors.goldDeep, size: 22),
                        ),
                        const SizedBox(width: 10),
                        const Text(
                          'নতুন অনুষ্ঠান বা মাহফিল শিডিউল',
                          style: TextStyle(fontSize: 17, fontWeight: FontWeight.bold),
                        ),
                      ],
                    ),
                    IconButton(
                      icon: const Icon(Icons.close),
                      onPressed: () => Navigator.of(context).pop(),
                    ),
                  ],
                ),
                const Divider(height: 20),

                Expanded(
                  child: SingleChildScrollView(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        // Event Type
                        const Text('অনুষ্ঠানের ধরন *', style: TextStyle(fontWeight: FontWeight.w600)),
                        const SizedBox(height: 6),
                        DropdownButtonFormField<String>(
                          value: _eventType,
                          decoration: InputDecoration(
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                            contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                          ),
                          items: _types
                              .map((t) => DropdownMenuItem(value: t['code']!, child: Text(t['label']!)))
                              .toList(),
                          onChanged: (val) {
                            if (val != null) setState(() => _eventType = val);
                          },
                        ),
                        const SizedBox(height: 14),

                        // Title
                        TextFormField(
                          controller: _titleController,
                          decoration: InputDecoration(
                            labelText: 'অনুষ্ঠানের পূর্ণ শিরোনাম *',
                            hintText: 'যেমন: বাৎসরিক তাফসিরুল কুরআন ও ওয়াজ মাহফিল',
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                            prefixIcon: const Icon(Icons.title),
                          ),
                          validator: (v) => (v == null || v.trim().isEmpty) ? 'শিরোনাম আবশ্যক' : null,
                        ),
                        const SizedBox(height: 14),

                        // Date & Time Row
                        Row(
                          children: [
                            Expanded(
                              child: InkWell(
                                onTap: () async {
                                  final picked = await showDatePicker(
                                    context: context,
                                    initialDate: _eventDate,
                                    firstDate: DateTime.now(),
                                    lastDate: DateTime(2030),
                                  );
                                  if (picked != null) {
                                    setState(() => _eventDate = picked);
                                  }
                                },
                                child: Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 14),
                                  decoration: BoxDecoration(
                                    border: Border.all(color: Colors.grey.shade400),
                                    borderRadius: BorderRadius.circular(AppDimens.radiusSmall),
                                  ),
                                  child: Row(
                                    children: [
                                      const Icon(Icons.calendar_month, color: AppColors.goldDeep, size: 20),
                                      const SizedBox(width: 8),
                                      Text(
                                        BengaliNumberFormatter.toBengali('${_eventDate.day}/${_eventDate.month}/${_eventDate.year}'),
                                        style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                                      ),
                                    ],
                                  ),
                                ),
                              ),
                            ),
                            const SizedBox(width: 10),
                            Expanded(
                              child: TextFormField(
                                initialValue: _eventTime,
                                decoration: InputDecoration(
                                  labelText: 'সময়সূচি',
                                  hintText: 'যেমন: বাদ আসর',
                                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                                  prefixIcon: const Icon(Icons.access_time),
                                ),
                                onChanged: (v) => _eventTime = v,
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 14),

                        // Venue
                        TextFormField(
                          controller: _venueController,
                          decoration: InputDecoration(
                            labelText: 'স্থান / ভেন্যু *',
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                            prefixIcon: const Icon(Icons.location_on_outlined),
                          ),
                        ),
                        const SizedBox(height: 14),

                        // Chief Guest
                        TextFormField(
                          controller: _chiefGuestController,
                          decoration: InputDecoration(
                            labelText: 'প্রধান মেহমান / প্রধান বক্তা',
                            hintText: 'মাওলানা / মুফতী সাহেবের নাম',
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                            prefixIcon: const Icon(Icons.person_pin),
                          ),
                        ),
                        const SizedBox(height: 14),

                        // Special Speakers
                        TextFormField(
                          controller: _specialSpeakersController,
                          decoration: InputDecoration(
                            labelText: 'বিশেষ আলোচক ও কারীগণ',
                            hintText: 'কমা দিয়ে একাধিক নাম লিখুন',
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                            prefixIcon: const Icon(Icons.record_voice_over_outlined),
                          ),
                        ),
                        const SizedBox(height: 14),

                        // Budget
                        TextFormField(
                          controller: _budgetController,
                          keyboardType: TextInputType.number,
                          decoration: InputDecoration(
                            labelText: 'আনুমানিক বাজেট (টাকা)',
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                            prefixIcon: const Icon(Icons.account_balance_wallet_outlined),
                          ),
                        ),
                        const SizedBox(height: 14),

                        // Description
                        TextFormField(
                          controller: _descriptionController,
                          maxLines: 2,
                          decoration: InputDecoration(
                            labelText: 'বিস্তারিত বিবরণ ও নির্দেশনাবলী',
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 16),

                ElevatedButton(
                  onPressed: _submit,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.goldDeep,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 14),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusButton)),
                  ),
                  child: const Text('অনুষ্ঠান সূচি চূড়ান্ত করুন', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
