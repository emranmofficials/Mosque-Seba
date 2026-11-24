import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../events/models/mosque_event_model.dart';
import '../providers/poster_provider.dart';
import '../widgets/poster_preview_canvas.dart';

/// Screen for creating, generating and customizing Islamic Dawat Postcards and Mahfil Posters
class PosterDesignerScreen extends ConsumerStatefulWidget {
  final MosqueEventModel? initialEvent;

  const PosterDesignerScreen({super.key, this.initialEvent});

  @override
  ConsumerState<PosterDesignerScreen> createState() => _PosterDesignerScreenState();
}

class _PosterDesignerScreenState extends ConsumerState<PosterDesignerScreen> {
  final _promptController = TextEditingController();

  @override
  void initState() {
    super.initState();
    if (widget.initialEvent != null) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        ref.read(posterNotifierProvider.notifier).initFromEvent(widget.initialEvent!);
      });
    }
  }

  @override
  void dispose() {
    _promptController.dispose();
    super.dispose();
  }

  void _showEditSheet() {
    final design = ref.read(posterNotifierProvider).design;
    final titleController = TextEditingController(text: design.eventTitle);
    final guestController = TextEditingController(text: design.chiefGuest);
    final dateController = TextEditingController(text: design.eventDate);
    final venueController = TextEditingController(text: design.venue);

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (ctx) => Padding(
        padding: EdgeInsets.only(
          left: 20,
          right: 20,
          top: 20,
          bottom: MediaQuery.of(ctx).viewInsets.bottom + 20,
        ),
        child: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const Text('পোস্টারের লেখা সম্পাদনা করুন', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
              const SizedBox(height: 14),
              TextField(
                controller: titleController,
                decoration: const InputDecoration(labelText: 'অনুষ্ঠানের শিরোনাম'),
              ),
              const SizedBox(height: 10),
              TextField(
                controller: guestController,
                decoration: const InputDecoration(labelText: 'প্রধান বক্তা / মেহমান'),
              ),
              const SizedBox(height: 10),
              TextField(
                controller: dateController,
                decoration: const InputDecoration(labelText: 'তারিখ ও সময়'),
              ),
              const SizedBox(height: 10),
              TextField(
                controller: venueController,
                decoration: const InputDecoration(labelText: 'স্থান / ভেন্যু'),
              ),
              const SizedBox(height: 16),
              ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.emeraldPrimary,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 12),
                ),
                onPressed: () {
                  ref.read(posterNotifierProvider.notifier).updateDesign(
                        design.copyWith(
                          eventTitle: titleController.text.trim(),
                          chiefGuest: guestController.text.trim(),
                          eventDate: dateController.text.trim(),
                          venue: venueController.text.trim(),
                        ),
                      );
                  Navigator.of(ctx).pop();
                },
                child: const Text('পরিবর্তন সংরক্ষণ করুন'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final posterState = ref.watch(posterNotifierProvider);
    final design = posterState.design;

    return Scaffold(
      appBar: AppBar(
        title: const Text('ইসলামিক দাওয়াতনামা ও পোস্টার AI'),
        actions: [
          IconButton(
            icon: const Icon(Icons.edit_note),
            tooltip: 'লেখা সম্পাদনা',
            onPressed: _showEditSheet,
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(AppDimens.spacing16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // AI Prompt Assistant Card
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: AppColors.emeraldPrimary.withOpacity(0.08),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: AppColors.emeraldPrimary.withOpacity(0.2)),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      const Icon(Icons.auto_awesome, color: AppColors.emeraldPrimary, size: 18),
                      const SizedBox(width: 8),
                      const Text(
                        'Gemini 3.8 Flash দ্বারা পোস্টার জেনারেটর',
                        style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13, color: AppColors.emeraldPrimary),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      Expanded(
                        child: TextField(
                          controller: _promptController,
                          decoration: InputDecoration(
                            hintText: 'যেমন: তাফসিরুল কুরআন মাহফিল, রমজানের ইফতার...',
                            isDense: true,
                            contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
                            filled: true,
                            fillColor: Theme.of(context).cardColor,
                          ),
                        ),
                      ),
                      const SizedBox(width: 8),
                      ElevatedButton(
                        onPressed: posterState.isGenerating
                            ? null
                            : () {
                                ref.read(posterNotifierProvider.notifier).generateWithAi(
                                      prompt: _promptController.text.trim(),
                                      eventType: widget.initialEvent?.eventType,
                                      venue: widget.initialEvent?.venue,
                                      date: widget.initialEvent?.eventDate,
                                    );
                              },
                        style: ElevatedButton.styleFrom(
                          backgroundColor: AppColors.emeraldPrimary,
                          foregroundColor: Colors.white,
                          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
                        ),
                        child: posterState.isGenerating
                            ? const SizedBox(
                                width: 16,
                                height: 16,
                                child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                              )
                            : const Text('জেনারেট', style: TextStyle(fontWeight: FontWeight.bold)),
                      ),
                    ],
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),

            // Color Theme Palette Selector
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                _buildThemeChip('পান্না সবুজ (Emerald)', 'emerald_gold', const Color(0xFF0D5C46), design.themeStyle),
                const SizedBox(width: 8),
                _buildThemeChip('নীল (Royal Blue)', 'royal_blue', const Color(0xFF203A43), design.themeStyle),
                const SizedBox(width: 8),
                _buildThemeChip('মেরুন (Classic)', 'classic_maroon', const Color(0xFF7A1C28), design.themeStyle),
              ],
            ),
            const SizedBox(height: 16),

            // The High Fidelity Live Canvas
            PosterPreviewCanvas(design: design),
            const SizedBox(height: 20),

            // Action Buttons
            Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: _showEditSheet,
                    icon: const Icon(Icons.edit, size: 18),
                    label: const Text('লেখা পরিবর্তন'),
                    style: OutlinedButton.styleFrom(padding: const EdgeInsets.symmetric(vertical: 12)),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: () {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(
                          content: Text('পোস্টার উচ্চ রেজোলিউশনে রেন্ডার ও ডাউনলোডের জন্য প্রস্তুত...'),
                          backgroundColor: AppColors.emeraldPrimary,
                        ),
                      );
                    },
                    icon: const Icon(Icons.share, size: 18),
                    label: const Text('শেয়ার ও প্রিন্ট'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.emeraldPrimary,
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(vertical: 12),
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),
          ],
        ),
      ),
    );
  }

  Widget _buildThemeChip(String label, String code, Color color, String current) {
    final isSelected = code == current;
    return InkWell(
      onTap: () => ref.read(posterNotifierProvider.notifier).updateTheme(code),
      borderRadius: BorderRadius.circular(20),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
        decoration: BoxDecoration(
          color: color.withOpacity(isSelected ? 1.0 : 0.15),
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: color, width: isSelected ? 2 : 1),
        ),
        child: Text(
          label,
          style: TextStyle(
            fontSize: 11,
            fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
            color: isSelected ? Colors.white : Colors.black87,
          ),
        ),
      ),
    );
  }
}
