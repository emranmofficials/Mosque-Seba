import 'package:drift/drift.dart' show Value;
import 'package:flutter/foundation.dart';
import 'package:supabase_flutter/supabase_flutter.dart' hide AuthUser;
import 'package:uuid/uuid.dart';
import '../../../core/database/app_database.dart';
import '../../../core/network/supabase_service.dart';
import '../../../core/storage/session_storage_service.dart';
import '../models/auth_user.dart';

/// Centralized Authentication Service
/// Handles Supabase Auth, local credential caching, and tenant database isolation
class AuthService {
  final SessionStorageService _storage;
  final AppDatabase _database;

  AuthService(this._storage, this._database);

  /// Restores user session from encrypted local storage
  Future<AuthUser?> restoreSession() async {
    if (!_storage.isLoggedIn) return null;

    final mosqueId = _storage.activeMosqueId;
    if (mosqueId.isEmpty) return null;

    return AuthUser(
      uid: SupabaseService.currentUserId ?? 'local_user',
      email: '',
      mobile: '',
      memberId: _storage.memberId,
      memberName: _storage.memberName,
      committeeId: _storage.committeeId,
      roleCode: _storage.userRoleCode,
      mosqueId: mosqueId,
      mosqueNameBn: '',
    );
  }

  /// Committee President Login with Email & Password
  Future<AuthUser> loginWithEmail({
    required String email,
    required String password,
  }) async {
    final cleanEmail = email.trim().toLowerCase();

    if (!SupabaseService.isInitialized) {
      throw Exception('ক্লাউড সার্ভারের সাথে সংযোগ স্থাপন করা যায়নি। ইন্টারনেট সংযোগ পরীক্ষা করুন।');
    }

    try {
      final res = await SupabaseService.client.auth.signInWithPassword(
        email: cleanEmail,
        password: password,
      );

      final user = res.user;
      if (user == null) {
        throw Exception('ভুল ইমেইল অথবা পাসওয়ার্ড প্রদান করা হয়েছে।');
      }

      final session = res.session;
      if (session != null) {
        await _storage.saveAuthTokens(
          accessToken: session.accessToken,
          refreshToken: session.refreshToken ?? '',
        );
      }

      // Fetch linked profile & mosque
      final profileRes = await SupabaseService.client
          .from('profiles')
          .select('id, mosque_id, full_name_bn')
          .eq('id', user.id)
          .maybeSingle();

      final mosqueId = profileRes?['mosque_id']?.toString() ?? '';
      if (mosqueId.isEmpty) {
        throw Exception('এই অ্যাকাউন্টের সাথে কোনো মসজিদ প্রোফাইল যুক্ত নেই।');
      }
      final memberName = profileRes?['full_name_bn']?.toString() ?? 'সভাপতি';

      await _storage.saveSession(
        roleCode: 'president',
        memberId: user.id,
        memberName: memberName,
        committeeId: '',
        mosqueId: mosqueId,
      );

      return AuthUser(
        uid: user.id,
        email: cleanEmail,
        mobile: user.phone ?? '',
        memberId: user.id,
        memberName: memberName,
        committeeId: '',
        roleCode: 'president',
        mosqueId: mosqueId,
        mosqueNameBn: '',
      );
    } on AuthException {
      throw Exception('ভুল ইমেইল অথবা পাসওয়ার্ড প্রদান করা হয়েছে।');
    } catch (e) {
      debugPrint('Supabase email login error: $e');
      rethrow;
    }
  }

  /// General Musalli / Committee Member Login with Mobile
  Future<AuthUser> loginWithMobile({
    required String committeeId,
    required String mobile,
    required String password,
  }) async {
    final cleanMobile = mobile.replaceAll('-', '').replaceAll(' ', '').trim();
    final cleanCommitteeId = committeeId.trim().toUpperCase();

    if (cleanMobile.length < 11) {
      throw Exception('সঠিক ১১ ডিজিটের মোবাইল নম্বর দিন।');
    }

    if (!SupabaseService.isInitialized) {
      throw Exception('ক্লাউড সার্ভারের সাথে সংযোগ স্থাপন করা যায়নি। ইন্টারনেট সংযোগ পরীক্ষা করুন।');
    }

    // Derived synthetic email for mobile authentication
    final syntheticEmail = 'm$cleanMobile@mosqueseba.app';

    try {
      final res = await SupabaseService.client.auth.signInWithPassword(
        email: syntheticEmail,
        password: password,
      );

      final user = res.user;
      if (user == null) {
        throw Exception('মোবাইল নম্বর অথবা পাসওয়ার্ড সঠিক নয়।');
      }

      final session = res.session;
      if (session != null) {
        await _storage.saveAuthTokens(
          accessToken: session.accessToken,
          refreshToken: session.refreshToken ?? '',
        );
      }

      final memberRes = await SupabaseService.client
          .from('members')
          .select('id, name_bn, role_code, mosque_id')
          .eq('mobile', cleanMobile)
          .maybeSingle();

      final roleCode = memberRes?['role_code']?.toString() ?? 'general_member';
      final memberName = memberRes?['name_bn']?.toString() ?? 'সম্মানিত মুসল্লি';
      final mosqueId = memberRes?['mosque_id']?.toString() ?? '';
      if (mosqueId.isEmpty) {
        throw Exception('সদস্যের সাথে কোনো মসজিদ প্রোফাইল যুক্ত পাওয়া যায়নি।');
      }

      await _storage.saveSession(
        roleCode: roleCode,
        memberId: user.id,
        memberName: memberName,
        committeeId: cleanCommitteeId,
        mosqueId: mosqueId,
      );

      return AuthUser(
        uid: user.id,
        email: syntheticEmail,
        mobile: cleanMobile,
        memberId: user.id,
        memberName: memberName,
        committeeId: cleanCommitteeId,
        roleCode: roleCode,
        mosqueId: mosqueId,
        mosqueNameBn: '',
      );
    } on AuthException {
      throw Exception('মোবাইল নম্বর অথবা পাসওয়ার্ড সঠিক নয়।');
    } catch (e) {
      debugPrint('Supabase mobile login error: $e');
      rethrow;
    }
  }

  /// Multi-Step Registration of Mosque and President
  Future<String> registerPresidentAndMosque({
    required String presidentNameBn,
    required String presidentNameEn,
    required String presidentMobile,
    required String presidentEmail,
    required String password,
    required String mosqueNameBn,
    required String mosqueNameEn,
    required String district,
    required String upazila,
    required int establishedYear,
    required int committeeStartYear,
    required int committeeEndYear,
    required String bankAccountInfo,
  }) async {
    final generatedCommitteeId = 'MS-${DateTime.now().year % 100}${1000 + (DateTime.now().millisecond % 9000)}';
    final mosqueId = const Uuid().v4();
    String effectiveUserId = const Uuid().v4();

    // 1. Cloud registration
    if (!SupabaseService.isInitialized) {
      throw Exception('ক্লাউড সার্ভারের সাথে সংযোগ স্থাপন করা যায়নি। ইন্টারনেট সংযোগ পরীক্ষা করুন।');
    }

    final authRes = await SupabaseService.client.auth.signUp(
      email: presidentEmail.trim().toLowerCase(),
      password: password,
    );

    final uid = authRes.user?.id;
    if (uid == null) {
      throw Exception('নিবন্ধন সম্পন্ন করা যায়নি। পুনরায় চেষ্টা করুন।');
    }
    effectiveUserId = uid;

    final session = authRes.session;
    if (session != null) {
      await _storage.saveAuthTokens(
        accessToken: session.accessToken,
        refreshToken: session.refreshToken ?? '',
      );
    }

    // Insert Mosque
    await SupabaseService.client.from('mosques').insert({
      'id': mosqueId,
      'committee_id': generatedCommitteeId,
      'name_bn': mosqueNameBn,
      'name_en': mosqueNameEn,
      'address': '$upazila, $district',
      'established_year': establishedYear,
      'committee_start_year': committeeStartYear,
      'committee_end_year': committeeEndYear,
      'status': 'active',
      'district': district,
      'upazila': upazila,
      'bank_account_info': bankAccountInfo,
    });

    // Insert President Profile
    await SupabaseService.client.from('profiles').insert({
      'id': effectiveUserId,
      'mosque_id': mosqueId,
      'full_name_bn': presidentNameBn,
      'full_name_en': presidentNameEn,
      'phone': presidentMobile,
      'is_active': true,
    });

    // Insert President Member record
    await SupabaseService.client.from('members').insert({
      'id': effectiveUserId,
      'mosque_id': mosqueId,
      'member_code': 'MEM-001',
      'name_bn': presidentNameBn,
      'name_en': presidentNameEn,
      'mobile': presidentMobile,
      'email': presidentEmail,
      'role_code': 'president',
      'status': 'active',
    });

    // 2. Local persistence in Drift SQLite
    await _database.into(_database.mosquesTable).insertOnConflictUpdate(
      MosquesTableCompanion.insert(
        id: mosqueId,
        committeeId: generatedCommitteeId,
        nameBn: mosqueNameBn,
        address: '$upazila, $district',
        district: Value(district),
        upazila: Value(upazila),
        establishedYear: Value(establishedYear),
        committeeStartYear: Value(committeeStartYear),
        committeeEndYear: Value(committeeEndYear),
        bankAccountInfo: Value(bankAccountInfo),
      ),
    );

    // Save active session
    await _storage.saveSession(
      roleCode: 'president',
      memberId: effectiveUserId,
      memberName: presidentNameBn,
      committeeId: generatedCommitteeId,
      mosqueId: mosqueId,
    );

    return generatedCommitteeId;
  }

  /// Logout and atomic tenant data wipe
  Future<void> logout() async {
    try {
      if (SupabaseService.isInitialized) {
        await SupabaseService.client.auth.signOut();
      }
    } catch (_) {}

    await _database.clearAllTenantData();
    await _storage.clearSession();
  }
}
