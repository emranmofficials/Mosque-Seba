import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import '../../core/services/image_upload_service.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_dimens.dart';

/// Reusable UI Field to Pick and Preview Profile Photos and Mosque Logos.
class ImagePickerField extends StatefulWidget {
  final String label;
  final String? subtitle;
  final String? initialUrl;
  final String folder; // 'profiles', 'logos', 'members'
  final bool isCircle;
  final ValueChanged<String> onImageUploaded;
  final VoidCallback? onImageRemoved;

  const ImagePickerField({
    super.key,
    required this.label,
    this.subtitle,
    this.initialUrl,
    required this.folder,
    this.isCircle = true,
    required this.onImageUploaded,
    this.onImageRemoved,
  });

  @override
  State<ImagePickerField> createState() => _ImagePickerFieldState();
}

class _ImagePickerFieldState extends State<ImagePickerField> {
  final _uploadService = ImageUploadService();
  String? _currentPathOrUrl;
  bool _isUploading = false;

  @override
  void initState() {
    super.initState();
    _currentPathOrUrl = widget.initialUrl;
  }

  @override
  void didUpdateWidget(covariant ImagePickerField oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.initialUrl != oldWidget.initialUrl) {
      _currentPathOrUrl = widget.initialUrl;
    }
  }

  Future<void> _pickImage(ImageSource source) async {
    Navigator.of(context).pop();
    setState(() => _isUploading = true);

    try {
      final xFile = await _uploadService.pickImage(source: source);
      if (xFile != null) {
        final result = await _uploadService.uploadImage(
          file: xFile,
          folder: widget.folder,
        );

        if (result != null && mounted) {
          setState(() {
            _currentPathOrUrl = result;
          });
          widget.onImageUploaded(result);
        }
      }
    } finally {
      if (mounted) {
        setState(() => _isUploading = false);
      }
    }
  }

  void _showSourceSheet() {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(AppDimens.radiusLg)),
      ),
      builder: (ctx) => SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(vertical: MosqueSpace.md),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                widget.label,
                style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: MosqueSpace.sm),
              ListTile(
                leading: const Icon(Icons.photo_library_outlined, color: AppColors.primary),
                title: const Text('গ্যালারি থেকে বেছে নিন'),
                onTap: () => _pickImage(ImageSource.gallery),
              ),
              ListTile(
                leading: const Icon(Icons.camera_alt_outlined, color: AppColors.primary),
                title: const Text('ক্যামেরা দিয়ে ছবি তুলুন'),
                onTap: () => _pickImage(ImageSource.camera),
              ),
              if (_currentPathOrUrl != null && _currentPathOrUrl!.isNotEmpty)
                ListTile(
                  leading: const Icon(Icons.delete_outline, color: AppColors.urgentRed),
                  title: const Text('ছবি মুছে ফেলুন', style: TextStyle(color: AppColors.urgentRed)),
                  onTap: () {
                    Navigator.of(ctx).pop();
                    setState(() => _currentPathOrUrl = null);
                    widget.onImageRemoved?.call();
                  },
                ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildImageWidget() {
    if (_isUploading) {
      return const Center(
        child: CircularProgressIndicator(strokeWidth: 2, color: AppColors.primary),
      );
    }

    if (_currentPathOrUrl != null && _currentPathOrUrl!.isNotEmpty) {
      final isNetwork = _currentPathOrUrl!.startsWith('http://') ||
          _currentPathOrUrl!.startsWith('https://');

      if (isNetwork) {
        return Image.network(
          _currentPathOrUrl!,
          fit: BoxFit.cover,
          width: double.infinity,
          height: double.infinity,
          errorBuilder: (_, __, ___) => _buildPlaceholder(),
        );
      } else {
        return Image.file(
          File(_currentPathOrUrl!),
          fit: BoxFit.cover,
          width: double.infinity,
          height: double.infinity,
          errorBuilder: (_, __, ___) => _buildPlaceholder(),
        );
      }
    }

    return _buildPlaceholder();
  }

  Widget _buildPlaceholder() {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Icon(
          widget.isCircle ? Icons.add_a_photo_outlined : Icons.add_photo_alternate_outlined,
          size: widget.isCircle ? 28 : 34,
          color: AppColors.primary,
        ),
        const SizedBox(height: 4),
        Text(
          'ছবি যুক্ত করুন',
          style: Theme.of(context).textTheme.labelSmall?.copyWith(
            color: AppColors.textSecondary.withOpacity(0.7),
          ),
        ),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    final size = widget.isCircle ? 96.0 : 110.0;
    final width = widget.isCircle ? 96.0 : 160.0;

    return Center(
      child: Column(
        children: [
          GestureDetector(
            onTap: _isUploading ? null : _showSourceSheet,
            child: Stack(
              children: [
                Container(
                  width: width,
                  height: size,
                  decoration: BoxDecoration(
                    color: AppColors.primary.withOpacity(0.06),
                    shape: widget.isCircle ? BoxShape.circle : BoxShape.rectangle,
                    borderRadius: widget.isCircle
                        ? null
                        : BorderRadius.circular(MosqueRadius.medium),
                    border: Border.all(
                      color: AppColors.primary.withOpacity(0.3),
                      width: 1.5,
                    ),
                  ),
                  clipBehavior: Clip.antiAlias,
                  child: _buildImageWidget(),
                ),
                Positioned(
                  bottom: 0,
                  right: widget.isCircle ? 0 : 4,
                  child: Container(
                    padding: const EdgeInsets.all(4),
                    decoration: BoxDecoration(
                      color: AppColors.primary,
                      shape: BoxShape.circle,
                      border: Border.all(color: Colors.white, width: 2),
                    ),
                    child: const Icon(
                      Icons.camera_alt,
                      size: 14,
                      color: Colors.white,
                    ),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: MosqueSpace.xs),
          Text(
            widget.label,
            style: Theme.of(context).textTheme.bodySmall?.copyWith(fontWeight: FontWeight.w600),
          ),
          if (widget.subtitle != null) ...[
            const SizedBox(height: 2),
            Text(
              widget.subtitle!,
              style: Theme.of(context).textTheme.labelSmall?.copyWith(color: AppColors.textSecondary.withOpacity(0.6)),
            ),
          ],
        ],
      ),
    );
  }
}
