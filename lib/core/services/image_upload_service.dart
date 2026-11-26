import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:image_picker/image_picker.dart';
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart' as p;
import '../network/supabase_service.dart';

/// Service to handle Image Picking and Cloud/Local Uploads for Profiles & Logos.
class ImageUploadService {
  final ImagePicker _picker;

  ImageUploadService({ImagePicker? picker}) : _picker = picker ?? ImagePicker();

  /// Picks an image from Gallery or Camera.
  Future<XFile?> pickImage({ImageSource source = ImageSource.gallery}) async {
    try {
      final file = await _picker.pickImage(
        source: source,
        maxWidth: 1200,
        maxHeight: 1200,
        imageQuality: 85,
      );
      return file;
    } catch (e) {
      debugPrint('Error picking image: $e');
      return null;
    }
  }

  /// Uploads image to Supabase Storage (bucket: 'mosque-assets').
  /// Falls back to local disk storage if offline or upload fails.
  Future<String?> uploadImage({
    required XFile file,
    required String folder, // e.g. 'profiles', 'logos', 'members'
    String? customFileName,
  }) async {
    final ext = p.extension(file.path).replaceAll('.', '').toLowerCase();
    final cleanExt = ext.isEmpty ? 'jpg' : ext;
    final fileName = customFileName ??
        '${DateTime.now().millisecondsSinceEpoch}_${(file.name.hashCode & 0x7FFFFFFF)}.$cleanExt';
    final storagePath = '$folder/$fileName';

    // 1. Try uploading to Supabase Storage if initialized
    if (SupabaseService.isInitialized) {
      try {
        final bytes = await file.readAsBytes();
        await SupabaseService.client.storage
            .from('mosque-assets')
            .uploadBinary(
              storagePath,
              bytes,
            );

        final publicUrl = SupabaseService.client.storage
            .from('mosque-assets')
            .getPublicUrl(storagePath);

        if (publicUrl.isNotEmpty) {
          return publicUrl;
        }
      } catch (e) {
        debugPrint('Supabase storage upload failed, saving locally: $e');
      }
    }

    // 2. Local offline fallback: Copy to app documents directory
    try {
      final appDir = await getApplicationDocumentsDirectory();
      final localFolder = Directory(p.join(appDir.path, folder));
      if (!await localFolder.exists()) {
        await localFolder.create(recursive: true);
      }
      final targetPath = p.join(localFolder.path, fileName);
      final targetFile = await File(file.path).copy(targetPath);
      return targetFile.path;
    } catch (e) {
      debugPrint('Failed to save image locally: $e');
      return file.path;
    }
  }
}
