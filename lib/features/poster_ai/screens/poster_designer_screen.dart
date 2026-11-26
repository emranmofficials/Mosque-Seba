import 'dart:io';
import 'dart:typed_data';
import 'dart:ui' as ui;
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:path_provider/path_provider.dart';
import 'package:pdf/pdf.dart';
import 'package:pdf/widgets.dart' as pw;
import 'package:printing/printing.dart';
import 'package:share_plus/share_plus.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../events/models/mosque_event_model.dart';
import '../models/poster_design_model.dart';
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
  final GlobalKey _posterBoundaryKey = GlobalKey();
  bool _isExporting = false;

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

  Future<Uint8List?> _capturePosterPng() async {
    final boundary = _posterBoundaryKey.currentContext?.findRenderObject()
        as RenderRepaintBoundary?;
    if (boundary == null) return null;
    final image = await boundary.toImage(pixelRatio: 3.0);
    final byteData = await image.toByteData(format: ui.ImageByteFormat.png);
    return byteData?.buffer.asUint8List();
  }

  Future<void> _sharePosterPng(PosterDesignModel design) async {
    if (_isExporting) return;
    setState(() => _isExporting = true);
    try {
      final pngBytes = await _capturePosterPng();
      if (pngBytes == null) return;
      final dir = await getTemporaryDirectory();
      final file = File(
        '${dir.path}/mosque_poster_${DateTime.now().millisecondsSinceEpoch}.png',
      );
      await file.writeAsBytes(pngBytes);
      await Share.shareXFiles(
        [XFile(file.path)],
        text: '${design.eventTitle}\n${design.eventDate} • ${design.venue}',
      );
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('পোস্টার এক্সপোর্টে ত্রুটি হয়েছে: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } finally {
      if (mounted) setState(() => _isExporting = false);
    }
  }

  Future<void> _printPosterPdf(PosterDesignModel design) async {
    if (_isExporting) return;
    setState(() => _isExporting = true);
    try {
      final pngBytes = await _capturePosterPng();
      if (pngBytes == null) return;
      final doc = pw.Document();
      final pdfImg = pw.MemoryImage(pngBytes);
      doc.addPage(
        pw.Page(
          pageFormat: PdfPageFormat.a4,
          build: (_) => pw.Center(child: pw.Image(pdfImg, fit: pw.BoxFit.contain)),
        ),
      );
      final bytes = await doc.save();
      await Printing.layoutPdf(
        onLayout: (_) async => bytes,
        name: 'Mosque_Poster.pdf',
      );
    } finally {
      if (mounted) setState(() => _isExporting = false);
    }
  }

  void _showEditSheet() {
    final design = ref.read(posterNotifierProvider).design;
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (ctx) => _EditPosterSheet(
        design: design,
        onSave: (updated) =>
            ref.read(posterNotifierProvider.notifier).updateDesign(updated),
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
                  const Row(
                    children: [
                      Icon(Icons.auto_awesome, color: AppColors.emeraldPrimary, size: 18),
                      SizedBox(width: 8),
                      Text(
                        'Gemini AI দ্বারা পোস্টার জেনারেটর',
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

            // The High Fidelity Live Canvas wrapped in RepaintBoundary for PNG/PDF export
            RepaintBoundary(
              key: _posterBoundaryKey,
              child: PosterPreviewCanvas(design: design),
            ),
            const SizedBox(height: 20),

            // Action Buttons
            Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: _showEditSheet,
                    icon: const Icon(Icons.edit, size: 18),
                    label: const Text('সম্পাদনা'),
                    style: OutlinedButton.styleFrom(padding: const EdgeInsets.symmetric(vertical: 12)),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: _isExporting ? null : () => _printPosterPdf(design),
                    icon: const Icon(Icons.print_outlined, size: 18),
                    label: const Text('প্রিন্ট PDF'),
                    style: OutlinedButton.styleFrom(padding: const EdgeInsets.symmetric(vertical: 12)),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: _isExporting ? null : () => _sharePosterPng(design),
                    icon: _isExporting
                        ? const SizedBox(
                            width: 16,
                            height: 16,
                            child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                          )
                        : const Icon(Icons.share, size: 18),
                    label: const Text('ছবি শেয়ার'),
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

class _EditPosterSheet extends StatefulWidget {
  final PosterDesignModel design;
  final ValueChanged<PosterDesignModel> onSave;

  const _EditPosterSheet({
    required this.design,
    required this.onSave,
  });

  @override
  State<_EditPosterSheet> createState() => _EditPosterSheetState();
}

class _EditPosterSheetState extends State<_EditPosterSheet> {
  late final TextEditingController _titleController;
  late final TextEditingController _guestController;
  late final TextEditingController _dateController;
  late final TextEditingController _venueController;

  @override
  void initState() {
    super.initState();
    _titleController = TextEditingController(text: widget.design.eventTitle);
    _guestController = TextEditingController(text: widget.design.chiefGuest);
    _dateController = TextEditingController(text: widget.design.eventDate);
    _venueController = TextEditingController(text: widget.design.venue);
  }

  @override
  void dispose() {
    _titleController.dispose();
    _guestController.dispose();
    _dateController.dispose();
    _venueController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(
        left: 20,
        right: 20,
        top: 20,
        bottom: MediaQuery.of(context).viewInsets.bottom + 20,
      ),
      child: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Text('পোস্টারের লেখা সম্পাদনা করুন', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
            const SizedBox(height: 14),
            TextField(
              controller: _titleController,
              decoration: const InputDecoration(labelText: 'অনুষ্ঠানের শিরোনাম'),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: _guestController,
              decoration: const InputDecoration(labelText: 'প্রধান বক্তা / মেহমান'),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: _dateController,
              decoration: const InputDecoration(labelText: 'তারিখ ও সময়'),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: _venueController,
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
                widget.onSave(
                  widget.design.copyWith(
                    eventTitle: _titleController.text.trim(),
                    chiefGuest: _guestController.text.trim(),
                    eventDate: _dateController.text.trim(),
                    venue: _venueController.text.trim(),
                  ),
                );
                Navigator.of(context).pop();
              },
              child: const Text('পরিবর্তন সংরক্ষণ করুন'),
            ),
          ],
        ),
      ),
    );
  }
}
