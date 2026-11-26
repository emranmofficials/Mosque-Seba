import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:local_auth/local_auth.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../features/app_security/services/secure_token_manager.dart';

/// Manages Biometric (Fingerprint/Face) & Committee PIN App Lock.
class BiometricAuthService {
  static final BiometricAuthService instance = BiometricAuthService();

  static const String _keyBiometricEnabled = 'key_biometric_lock_enabled';
  static const String _keyPinEnabled = 'key_pin_lock_enabled';

  final LocalAuthentication _localAuth = LocalAuthentication();
  final SecureTokenManager _tokenManager = SecureTokenManager();

  Future<bool> isBiometricHardwareAvailable() async {
    if (kIsWeb) return false;
    try {
      final supported = await _localAuth.isDeviceSupported();
      final canCheck = await _localAuth.canCheckBiometrics;
      return supported || canCheck;
    } on PlatformException catch (e) {
      debugPrint('Biometric check error: $e');
      return false;
    } catch (_) {
      return false;
    }
  }

  Future<bool> authenticateWithBiometrics({
    String reason = 'মসজিদ সেবা আনলক করতে বায়োমেট্রিক যাচাই করুন',
  }) async {
    if (kIsWeb) return false;
    try {
      final available = await isBiometricHardwareAvailable();
      if (!available) return false;
      return await _localAuth.authenticate(
        localizedReason: reason,
        options: const AuthenticationOptions(
          stickyAuth: true,
          biometricOnly: false,
        ),
      );
    } on PlatformException catch (e) {
      debugPrint('Biometric auth error: $e');
      return false;
    } catch (_) {
      return false;
    }
  }

  Future<bool> isBiometricLockEnabled() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getBool(_keyBiometricEnabled) ?? false;
  }

  Future<void> setBiometricLockEnabled(bool enabled) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keyBiometricEnabled, enabled);
  }

  Future<bool> isPinLockEnabled() async {
    final prefs = await SharedPreferences.getInstance();
    final enabled = prefs.getBool(_keyPinEnabled) ?? false;
    if (!enabled) return false;
    return _tokenManager.hasCommitteePin();
  }

  Future<void> enableCommitteePin(String pin) async {
    await _tokenManager.setCommitteePin(pin);
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keyPinEnabled, true);
  }

  Future<void> disableCommitteePin() async {
    await _tokenManager.clearCommitteePin();
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keyPinEnabled, false);
  }

  Future<bool> verifyPin(String pin) => _tokenManager.verifyCommitteePin(pin);

  Future<bool> shouldLockOnLaunch() async {
    final bio = await isBiometricLockEnabled();
    final pin = await isPinLockEnabled();
    return bio || pin;
  }
}
