import 'package:flutter/material.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';

/// Interactive Touchscreen Signature Canvas Pad Dialog
class SignaturePadDialog extends StatefulWidget {
  final String title;
  final String signatoryRole;
  final Function(List<Offset?> points) onSave;

  const SignaturePadDialog({
    super.key,
    this.title = 'অনুমোদিত ডিজিটাল স্বাক্ষর',
    this.signatoryRole = 'মুহতারাম সভাপতি',
    required this.onSave,
  });

  @override
  State<SignaturePadDialog> createState() => _SignaturePadDialogState();
}

class _SignaturePadDialogState extends State<SignaturePadDialog> {
  final List<Offset?> _points = [];

  void _clear() {
    setState(() => _points.clear());
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusCard)),
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
                        child: const Icon(Icons.draw, color: AppColors.emeraldPrimary, size: 22),
                      ),
                      const SizedBox(width: 10),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(widget.title, style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                          Text(widget.signatoryRole, style: TextStyle(fontSize: 12, color: Colors.grey.shade600)),
                        ],
                      ),
                    ],
                  ),
                  IconButton(
                    icon: const Icon(Icons.close),
                    onPressed: () => Navigator.of(context).pop(),
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
                      // Guide watermark
                      const Center(
                        child: Text(
                          'এখানে আঙুল দিয়ে স্বাক্ষর করুন',
                          style: TextStyle(color: Colors.black26, fontSize: 13, fontStyle: FontStyle.italic),
                        ),
                      ),
                      GestureDetector(
                        onPanUpdate: (details) {
                          final renderBox = context.findRenderObject() as RenderBox?;
                          if (renderBox != null) {
                            setState(() {
                              _points.add(details.localPosition);
                            });
                          }
                        },
                        onPanEnd: (_) => setState(() => _points.add(null)),
                        child: CustomPaint(
                          painter: _SignaturePainter(points: _points),
                          size: Size.infinite,
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
                    onPressed: _clear,
                    icon: const Icon(Icons.refresh, size: 16),
                    label: const Text('স্বাক্ষর মুছুন'),
                    style: TextButton.styleFrom(foregroundColor: Colors.red),
                  ),
                  ElevatedButton.icon(
                    onPressed: _points.isEmpty
                        ? null
                        : () {
                            widget.onSave(_points);
                            Navigator.of(context).pop();
                          },
                    icon: const Icon(Icons.check, size: 16),
                    label: const Text('স্বাক্ষর সংরক্ষণ'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.emeraldPrimary,
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
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
      ..color = const Color(0xFF0F2027)
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
