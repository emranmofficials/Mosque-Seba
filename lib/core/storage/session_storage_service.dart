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
  static const String _keyUserEmail = 'key_user_email';
  static const String _keyUserMobile = 'key_user_mobile';
  static const String _keyMosqueNameBn = 'key_mosque_name_bn';
  static const String _keyMosqueAddress = 'key_mosque_address';
  static const String _keyEstablishedYear = 'key_established_year';
  static const String _keyRememberedEmail = 'key_remembered_email';

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

  String get userEmail =>
      _prefs.getString(_keyUserEmail) ?? '';

  String get userMobile =>
      _prefs.getString(_keyUserMobile) ?? '';

  String get mosqueNameBn =>
      _prefs.getString(_keyMosqueNameBn) ?? '';

  String get mosqueAddress =>
      _prefs.getString(_keyMosqueAddress) ?? '';

  int? get establishedYear =>
      _prefs.getInt(_keyEstablishedYear);

  String get rememberedEmail =>
      _prefs.getString(_keyRememberedEmail) ?? '';

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
    String email = '',
    String mobile = '',
    String mosqueNameBn = '',
    String mosqueAddress = '',
    int? establishedYear,
  }) async {
    await _prefs.setBool('is_logged_in', true);
    await _prefs.setString(AppConstants.keyUserRole, roleCode);
    await _prefs.setString(AppConstants.keyMemberId, memberId);
    await _prefs.setString(AppConstants.keyMemberName, memberName);
    await _prefs.setString(AppConstants.keyCommitteeId, committeeId);
    await _prefs.setString(AppConstants.keyActiveMosqueId, mosqueId);
    await _prefs.setString(_keyUserEmail, email);
    await _prefs.setString(_keyUserMobile, mobile);
    await _prefs.setString(_keyMosqueNameBn, mosqueNameBn);
    await _prefs.setString(_keyMosqueAddress, mosqueAddress);
    if (establishedYear != null) {
      await _prefs.setInt(_keyEstablishedYear, establishedYear);
    } else {
      await _prefs.remove(_keyEstablishedYear);
    }
  }

  Future<void> updateMosqueMetadata({
    required String mosqueNameBn,
    required String mosqueAddress,
    int? establishedYear,
  }) async {
    await _prefs.setString(_keyMosqueNameBn, mosqueNameBn);
    await _prefs.setString(_keyMosqueAddress, mosqueAddress);
    if (establishedYear != null) {
      await _prefs.setInt(_keyEstablishedYear, establishedYear);
    }
  }

  Future<void> setRememberedEmail(String? email) async {
    if (email == null || email.trim().isEmpty) {
      await _prefs.remove(_keyRememberedEmail);
    } else {
      await _prefs.setString(_keyRememberedEmail, email.trim());
    }
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
    await _prefs.remove(AppConstants.keyActiveMosqueId);
    await _prefs.remove(_keyUserEmail);
    await _prefs.remove(_keyUserMobile);
    await _prefs.remove(_keyMosqueNameBn);
    await _prefs.remove(_keyMosqueAddress);
    await _prefs.remove(_keyEstablishedYear);
    await _secureStorage.deleteAll();
  }
}
