import 'dart:typed_data';
import 'dart:ui' as ui;
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';

/// Interactive Touchscreen Signature Canvas Pad Dialog exporting crisp PNG bytes.
class SignaturePadDialog extends StatefulWidget {
  final String title;
  final String signatoryRole;
  final Function(List<Offset?> points)? onSave;
  final Future<void> Function(Uint8List pngBytes)? onSavePng;

  const SignaturePadDialog({
    super.key,
    this.title = 'অনুমোদিত ডিজিটাল স্বাক্ষর',
    this.signatoryRole = 'মুহতারাম সভাপতি',
    this.onSave,
    this.onSavePng,
  });

  @override
  State<SignaturePadDialog> createState() => _SignaturePadDialogState();
}

class _SignaturePadDialogState extends State<SignaturePadDialog> {
  final List<Offset?> _points = [];
  final GlobalKey _boundaryKey = GlobalKey();
  bool _saving = false;

  void _clear() {
    setState(() => _points.clear());
  }

  Future<Uint8List?> _capturePng() async {
    final boundary = _boundaryKey.currentContext?.findRenderObject()
        as RenderRepaintBoundary?;
    if (boundary == null) return null;
    final image = await boundary.toImage(pixelRatio: 2.5);
    final byteData = await image.toByteData(format: ui.ImageByteFormat.png);
    return byteData?.buffer.asUint8List();
  }

  Future<void> _handleSave() async {
    if (_points.isEmpty || _saving) return;
    setState(() => _saving = true);

    try {
      if (widget.onSavePng != null) {
        final pngBytes = await _capturePng();
        if (pngBytes != null) {
          await widget.onSavePng!(pngBytes);
        }
      }
      widget.onSave?.call(_points);
      if (mounted) Navigator.of(context).pop();
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(AppDimens.radiusCard),
      ),
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 480, maxHeight: 450),
        child: Padding(
          padding: const EdgeInsets.all(AppDimens.spacing20),
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
                          color: AppColors.emeraldPrimary.withOpacity(0.12),
                          shape: BoxShape.circle,
                        ),
                        child: const Icon(
                          Icons.draw,
                          color: AppColors.emeraldPrimary,
                          size: 22,
                        ),
                      ),
                      const SizedBox(width: 10),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            widget.title,
                            style: Theme.of(context).textTheme.titleMedium?.copyWith(
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          Text(
                            widget.signatoryRole,
                            style: Theme.of(context).textTheme.bodySmall?.copyWith(
                              color: Colors.grey.shade600,
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                  IconButton(
                    icon: const Icon(Icons.close),
                    onPressed: _saving ? null : () => Navigator.of(context).pop(),
                  ),
                ],
              ),
              const Divider(height: 16),

              // Canvas Area
              Expanded(
                child: Container(
                  decoration: BoxDecoration(
                    color: Colors.grey.withOpacity(0.06),
                    borderRadius: BorderRadius.circular(AppDimens.radiusSmall),
                    border: Border.all(color: Colors.grey.shade300),
                  ),
                  child: Stack(
                    children: [
                      Center(
                        child: Text(
                          'এখানে আঙুল দিয়ে স্বাক্ষর করুন',
                          style: Theme.of(context).textTheme.bodySmall?.copyWith(
                            color: Colors.black26,
                            fontStyle: FontStyle.italic,
                          ),
                        ),
                      ),
                      GestureDetector(
                        onPanUpdate: (details) {
                          setState(() {
                            _points.add(details.localPosition);
                          });
                        },
                        onPanEnd: (_) => setState(() => _points.add(null)),
                        child: RepaintBoundary(
                          key: _boundaryKey,
                          child: CustomPaint(
                            painter: _SignaturePainter(points: _points),
                            size: Size.infinite,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 14),

              // Action Buttons
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  TextButton.icon(
                    onPressed: _saving ? null : _clear,
                    icon: const Icon(Icons.refresh, size: 16),
                    label: const Text('স্বাক্ষর মুছুন'),
                    style: TextButton.styleFrom(foregroundColor: Colors.red),
                  ),
                  ElevatedButton.icon(
                    onPressed: (_points.isEmpty || _saving) ? null : _handleSave,
                    icon: _saving
                        ? const SizedBox(
                            width: 14,
                            height: 14,
                            child: CircularProgressIndicator(
                              strokeWidth: 2,
                              color: Colors.white,
                            ),
                          )
                        : const Icon(Icons.check, size: 16),
                    label: const Text('স্বাক্ষর সংরক্ষণ'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.emeraldPrimary,
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(
                        horizontal: 16,
                        vertical: 10,
                      ),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _SignaturePainter extends CustomPainter {
  final List<Offset?> points;

  _SignaturePainter({required this.points});

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = AppColors.textPrimary
      ..strokeCap = StrokeCap.round
      ..strokeWidth = 3.0;

    for (int i = 0; i < points.length - 1; i++) {
      if (points[i] != null && points[i + 1] != null) {
        canvas.drawLine(points[i]!, points[i + 1]!, paint);
      }
    }
  }

  @override
  bool shouldRepaint(_SignaturePainter oldDelegate) => true;
}
