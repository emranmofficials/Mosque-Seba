import 'dart:convert';
import 'dart:typed_data';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// Storage Service for Official Mosque Seal & Signatures
class SignatureStorageService {
  final FlutterSecureStorage _storage;

  SignatureStorageService({FlutterSecureStorage? storage})
      : _storage = storage ?? const FlutterSecureStorage();

  static const String _keyPresidentSignature = 'official_president_signature';
  static const String _keySecretarySignature = 'official_secretary_signature';
  static const String _keyTreasurerSignature = 'official_treasurer_signature';
  static const String _keyOfficialSeal = 'official_mosque_seal';

  /// Save signature as Base64 encoded string
  Future<void> saveSignature({
    required String roleCode, // 'president', 'general_secretary', 'treasurer'
    required Uint8List signatureBytes,
  }) async {
    final base64String = base64Encode(signatureBytes);
    final key = _getKeyForRole(roleCode);
    await _storage.write(key: key, value: base64String);
  }

  /// Retrieve signature bytes
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

  /// Save official seal
  Future<void> saveSeal(Uint8List sealBytes) async {
    final base64String = base64Encode(sealBytes);
    await _storage.write(key: _keyOfficialSeal, value: base64String);
  }

  /// Get official seal
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

  String _getKeyForRole(String role) {
    if (role.contains('treasurer')) return _keyTreasurerSignature;
    if (role.contains('secretary')) return _keySecretarySignature;
    return _keyPresidentSignature;
  }
}
