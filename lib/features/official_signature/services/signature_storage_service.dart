import 'dart:convert';
import 'dart:typed_data';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// Storage Service for Official Mosque Seal & Digital Signatures (PNG Base64).
class SignatureStorageService {
  final FlutterSecureStorage _storage;

  SignatureStorageService({FlutterSecureStorage? storage})
      : _storage = storage ?? const FlutterSecureStorage();

  static const String _keyPresidentSignature = 'official_president_signature';
  static const String _keySecretarySignature = 'official_secretary_signature';
  static const String _keyTreasurerSignature = 'official_treasurer_signature';
  static const String _keyOfficialSeal = 'official_mosque_seal';

  /// Check if raw bytes represent a PNG image header.
  static bool isPngBytes(Uint8List bytes) {
    return bytes.length >= 4 &&
        bytes[0] == 0x89 &&
        bytes[1] == 0x50 &&
        bytes[2] == 0x4E &&
        bytes[3] == 0x47;
  }

  /// Save signature image bytes as Base64 encoded string in secure storage.
  Future<void> saveSignature({
    required String roleCode, // 'president', 'general_secretary', 'treasurer'
    required Uint8List signatureBytes,
  }) async {
    final base64String = base64Encode(signatureBytes);
    final key = _getKeyForRole(roleCode);
    await _storage.write(key: key, value: base64String);
  }

  /// Retrieve signature bytes.
  Future<Uint8List?> getSignature(String roleCode) async {
    final key = _getKeyForRole(roleCode);
    final base64String = await _storage.read(key: key);
    if (base64String != null && base64String.isNotEmpty) {
      try {
        return base64Decode(base64String);
      } catch (_) {
        return null;
      }
    }
    return null;
  }

  /// Delete saved signature for a role.
  Future<void> deleteSignature(String roleCode) async {
    await _storage.delete(key: _getKeyForRole(roleCode));
  }

  /// Save official mosque seal image bytes.
  Future<void> saveSeal(Uint8List sealBytes) async {
    final base64String = base64Encode(sealBytes);
    await _storage.write(key: _keyOfficialSeal, value: base64String);
  }

  /// Retrieve official mosque seal bytes.
  Future<Uint8List?> getSeal() async {
    final base64String = await _storage.read(key: _keyOfficialSeal);
    if (base64String != null && base64String.isNotEmpty) {
      try {
        return base64Decode(base64String);
      } catch (_) {
        return null;
      }
    }
    return null;
  }

  /// Delete official mosque seal.
  Future<void> deleteSeal() async {
    await _storage.delete(key: _keyOfficialSeal);
  }

  String _getKeyForRole(String role) {
    if (role.contains('treasurer')) return _keyTreasurerSignature;
    if (role.contains('secretary')) return _keySecretarySignature;
    return _keyPresidentSignature;
  }
}
