import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../constants/app_constants.dart';

/// Secure Session Storage Service
/// Manages user credentials, active tenant mosque ID, and preferences
class SessionStorageService {
  final SharedPreferences _prefs;
  final FlutterSecureStorage _secureStorage;

  static const String _keyToken = 'auth_access_token';
  static const String _keyRefreshToken = 'auth_refresh_token';

  SessionStorageService(this._prefs, this._secureStorage);

  bool get isLoggedIn => _prefs.getBool('is_logged_in') ?? false;

  String get activeMosqueId =>
      _prefs.getString(AppConstants.keyActiveMosqueId) ?? '';

  String get userRoleCode =>
      _prefs.getString(AppConstants.keyUserRole) ?? 'general_member';

  String get memberName =>
      _prefs.getString(AppConstants.keyMemberName) ?? '';

  String get memberId =>
      _prefs.getString(AppConstants.keyMemberId) ?? '';

  String get committeeId =>
      _prefs.getString(AppConstants.keyCommitteeId) ?? '';

  bool get isDarkMode =>
      _prefs.getBool(AppConstants.keyIsDarkMode) ?? false;

  int get hijriDayOffset =>
      _prefs.getInt(AppConstants.keyHijriOffset) ?? 0;

  Future<void> saveSession({
    required String roleCode,
    required String memberId,
    required String memberName,
    required String committeeId,
    required String mosqueId,
  }) async {
    await _prefs.setBool('is_logged_in', true);
    await _prefs.setString(AppConstants.keyUserRole, roleCode);
    await _prefs.setString(AppConstants.keyMemberId, memberId);
    await _prefs.setString(AppConstants.keyMemberName, memberName);
    await _prefs.setString(AppConstants.keyCommitteeId, committeeId);
    await _prefs.setString(AppConstants.keyActiveMosqueId, mosqueId);
  }

  Future<void> setDarkMode(bool isDark) async {
    await _prefs.setBool(AppConstants.keyIsDarkMode, isDark);
  }

  Future<void> setHijriOffset(int offset) async {
    await _prefs.setInt(AppConstants.keyHijriOffset, offset);
  }

  Future<void> saveAuthTokens({
    required String accessToken,
    required String refreshToken,
  }) async {
    await _secureStorage.write(key: _keyToken, value: accessToken);
    await _secureStorage.write(key: _keyRefreshToken, value: refreshToken);
  }

  Future<String?> getAccessToken() async {
    return await _secureStorage.read(key: _keyToken);
  }

  Future<void> clearSession() async {
    await _prefs.remove('is_logged_in');
    await _prefs.remove(AppConstants.keyUserRole);
    await _prefs.remove(AppConstants.keyMemberId);
    await _prefs.remove(AppConstants.keyMemberName);
    await _prefs.remove(AppConstants.keyCommitteeId);
    await _secureStorage.deleteAll();
  }
}
