import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../security/encryption_helper.dart';

/// Secure Token and Credentials Manager
class SecureTokenManager {
  final FlutterSecureStorage _storage;

  SecureTokenManager({FlutterSecureStorage? storage})
      : _storage = storage ?? const FlutterSecureStorage();

  static const String _keySessionToken = 'secure_session_token';
  static const String _keyCommitteePinHash = 'secure_committee_pin_hash';

  /// Store encrypted session token
  Future<void> saveSessionToken(String token) async {
    await _storage.write(key: _keySessionToken, value: token);
  }

  /// Read session token
  Future<String?> getSessionToken() async {
    return _storage.read(key: _keySessionToken);
  }

  /// Clear session
  Future<void> clearSession() async {
    await _storage.delete(key: _keySessionToken);
  }

  /// Store Committee App PIN
  Future<void> setCommitteePin(String pin) async {
    final hash = EncryptionHelper.hashSha256(pin);
    await _storage.write(key: _keyCommitteePinHash, value: hash);
  }

  /// Verify Committee PIN
  Future<bool> verifyCommitteePin(String enteredPin) async {
    final storedHash = await _storage.read(key: _keyCommitteePinHash);
    if (storedHash == null) return true; // first time setup
    return storedHash == EncryptionHelper.hashSha256(enteredPin);
  }
}
