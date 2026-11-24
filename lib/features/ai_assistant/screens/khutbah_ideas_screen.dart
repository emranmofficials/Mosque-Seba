import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../shared/widgets/card_shell.dart';
import '../models/khutbah_topic_model.dart';
import '../providers/ai_assistant_provider.dart';

/// Screen presenting Gemini-powered Khutbah Ideas & Bayan Preparation Assistant
class KhutbahIdeasScreen extends ConsumerStatefulWidget {
  const KhutbahIdeasScreen({super.key});

  @override
  ConsumerState<KhutbahIdeasScreen> createState() => _KhutbahIdeasScreenState();
}

class _KhutbahIdeasScreenState extends ConsumerState<KhutbahIdeasScreen> {
  String _selectedCategory = 'character';
  final _customPromptController = TextEditingController();

  final List<Map<String, String>> _categories = [
    {'code': 'character', 'label': 'উত্তম চরিত্র ও ব্যবহার'},
    {'code': 'halal_earnings', 'label': 'হালাল জীবিকা ও ব্যবসা'},
    {'code': 'charity', 'label': 'যাকাত ও আল্লাহর পথে ব্যয়'},
    {'code': 'parents_rights', 'label': 'পিতা-মাতার হক'},
    {'code': 'salah', 'label': 'নামাজে একাগ্রতা'},
    {'code': 'taqwa', 'label': 'তাকওয়া ও আখিরাত'},
  ];

  @override
  void dispose() {
    _customPromptController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(khutbahNotifierProvider);
    final topic = state.topic;

    return Scaffold(
      appBar: AppBar(
        title: const Text('জুমার খুতবা ও বয়ান প্রস্তুতি AI'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(AppDimens.spacing16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Category Chips Row
            const Text(
              'বিষয় নির্বাচন করুন',
              style: TextStyle(fontSize: 14, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: Row(
                children: _categories.map((c) {
                  final isSelected = c['code'] == _selectedCategory;
                  return Padding(
                    padding: const EdgeInsets.only(right: 6),
                    child: ChoiceChip(
                      label: Text(c['label']!),
                      selected: isSelected,
                      selectedColor: AppColors.emeraldPrimary.withOpacity(0.15),
                      labelStyle: TextStyle(
                        color: isSelected ? AppColors.emeraldPrimary : Colors.grey.shade700,
                        fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                        fontSize: 12,
                      ),
                      onSelected: (_) {
                        setState(() => _selectedCategory = c['code']!);
                        ref.read(khutbahNotifierProvider.notifier).generateTopic(
                              category: _selectedCategory,
                              prompt: _customPromptController.text.trim(),
                            );
                      },
                    ),
                  );
                }).toList(),
              ),
            ),
            const SizedBox(height: 12),

            // Custom Prompt Input
            Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _customPromptController,
                    decoration: InputDecoration(
                      hintText: 'নির্দিষ্ট কোনো বিষয় থাকলে লিখুন...',
                      isDense: true,
                      contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
                      filled: true,
                      fillColor: Theme.of(context).cardColor,
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                ElevatedButton.icon(
                  onPressed: state.isLoading
                      ? null
                      : () {
                          ref.read(khutbahNotifierProvider.notifier).generateTopic(
                                category: _selectedCategory,
                                prompt: _customPromptController.text.trim(),
                              );
                        },
                  icon: const Icon(Icons.auto_awesome, size: 16),
                  label: const Text('জেনারেট'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.emeraldPrimary,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 18),

            // Khutbah Card
            if (state.isLoading)
              const Center(
                child: Padding(
                  padding: EdgeInsets.all(32.0),
                  child: CircularProgressIndicator(color: AppColors.emeraldPrimary),
                ),
              )
            else if (topic != null)
              _buildKhutbahCard(topic),
          ],
        ),
      ),
    );
  }

  Widget _buildKhutbahCard(KhutbahTopicModel topic) {
    return CardShell(
      padding: const EdgeInsets.all(AppDimens.spacing16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: AppColors.emeraldPrimary.withOpacity(0.12),
                  shape: BoxShape.circle,
                ),
                child: const Icon(Icons.menu_book, color: AppColors.emeraldPrimary, size: 22),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: Text(
                  topic.title,
                  style: const TextStyle(fontSize: 17, fontWeight: FontWeight.bold),
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          const Divider(height: 1),
          const SizedBox(height: 14),

          // Quran Ayat Box
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: AppColors.emeraldPrimary.withOpacity(0.06),
              borderRadius: BorderRadius.circular(8),
              border: Border.all(color: AppColors.emeraldPrimary.withOpacity(0.2)),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Row(
                  children: [
                    Icon(Icons.format_quote, size: 16, color: AppColors.emeraldPrimary),
                    SizedBox(width: 4),
                    Text('আল-কুরআনের দিকনির্দেশনা', style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: AppColors.emeraldPrimary)),
                  ],
                ),
                const SizedBox(height: 6),
                Text(
                  topic.quranicAyat,
                  style: const TextStyle(fontSize: 13, height: 1.4, fontWeight: FontWeight.w500),
                ),
              ],
            ),
          ),
          const SizedBox(height: 12),

          // Hadith Reference Box
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: AppColors.goldDeep.withOpacity(0.08),
              borderRadius: BorderRadius.circular(8),
              border: Border.all(color: AppColors.goldDeep.withOpacity(0.3)),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Row(
                  children: [
                    Icon(Icons.bookmark_border, size: 16, color: AppColors.goldDeep),
                    SizedBox(width: 4),
                    Text('সহীহ হাদিস রেফারেন্স', style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: AppColors.goldDeep)),
                  ],
                ),
                const SizedBox(height: 6),
                Text(
                  topic.hadithReference,
                  style: const TextStyle(fontSize: 13, height: 1.4, fontWeight: FontWeight.w500),
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),

          // Main Points
          const Text('বক্তব্যের মূল বিষয়সমূহ:', style: TextStyle(fontSize: 14, fontWeight: FontWeight.bold)),
          const SizedBox(height: 8),
          ...topic.mainPoints.map(
            (pt) => Padding(
              padding: const EdgeInsets.only(bottom: 6),
              child: Text(pt, style: const TextStyle(fontSize: 13, height: 1.3)),
            ),
          ),
          const SizedBox(height: 14),

          // Practical Guidance
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: Colors.grey.withOpacity(0.08),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('মুসল্লিদের জন্য করণীয়:', style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                const SizedBox(height: 4),
                Text(topic.practicalGuidance, style: TextStyle(fontSize: 12, color: Colors.grey.shade800)),
              ],
            ),
          ),
          const SizedBox(height: 16),

          // Copy / Share Button
          Align(
            alignment: Alignment.centerRight,
            child: TextButton.icon(
              onPressed: () {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('খুতবার আউটলাইন ক্লিপবোর্ডে কপি করা হয়েছে'),
                    backgroundColor: AppColors.emeraldPrimary,
                  ),
                );
              },
              icon: const Icon(Icons.copy, size: 16),
              label: const Text('কপি করুন'),
            ),
          ),
        ],
      ),
    );
  }
}
