import 'package:drift/drift.dart' show Value;
import 'package:flutter/foundation.dart';
import 'package:supabase_flutter/supabase_flutter.dart' hide AuthUser;
import 'package:uuid/uuid.dart';
import '../../../core/database/app_database.dart';
import '../../../core/database/offline_cache_service.dart';
import '../../../core/network/supabase_service.dart';
import '../../../core/storage/session_storage_service.dart';
import '../models/auth_user.dart';

/// Centralized Authentication Service
/// Handles Supabase Auth, local credential caching, and tenant database isolation
class AuthService {
  final SessionStorageService _storage;
  final AppDatabase _database;

  AuthService(this._storage, this._database);

  int? _toInt(dynamic value) {
    if (value == null) return null;
    if (value is int) return value;
    return int.tryParse(value.toString().trim());
  }

  Future<Map<String, dynamic>?> _fetchAndCacheMosque(String mosqueId) async {
    try {
      if (SupabaseService.isInitialized) {
        final mosqueRes = await SupabaseService.client
            .from('mosques')
            .select()
            .eq('id', mosqueId)
            .maybeSingle();

        if (mosqueRes != null) {
          final map = Map<String, dynamic>.from(mosqueRes);
          final nameBn = map['name_bn']?.toString() ?? '';
          final address = map['address']?.toString() ?? '';
          final committeeId = map['committee_id']?.toString() ?? '';
          final districtVal = map['district']?.toString();
          final upazilaVal = map['upazila']?.toString();
          final estYearVal = _toInt(map['established_year']);
          final startYearVal = _toInt(map['committee_start_year']);
          final endYearVal = _toInt(map['committee_end_year']);
          final bankInfoVal = map['bank_account_info']?.toString();
          if (nameBn.isNotEmpty) {
            await _database.into(_database.mosquesTable).insertOnConflictUpdate(
              MosquesTableCompanion.insert(
                id: mosqueId,
                committeeId: committeeId,
                nameBn: nameBn,
                address: address,
                district: districtVal != null ? Value(districtVal) : const Value.absent(),
                upazila: upazilaVal != null ? Value(upazilaVal) : const Value.absent(),
                establishedYear: estYearVal != null ? Value(estYearVal) : const Value.absent(),
                committeeStartYear: startYearVal != null ? Value(startYearVal) : const Value.absent(),
                committeeEndYear: endYearVal != null ? Value(endYearVal) : const Value.absent(),
                bankAccountInfo: bankInfoVal != null ? Value(bankInfoVal) : const Value.absent(),
              ),
            );
          }
          return map;
        }
      }
    } catch (e) {
      debugPrint('Mosque metadata fetch fallback: $e');
    }

    try {
      final localRow = await (_database.select(_database.mosquesTable)
            ..where((t) => t.id.equals(mosqueId)))
          .getSingleOrNull();
      if (localRow != null) {
        return {
          'id': localRow.id,
          'committee_id': localRow.committeeId,
          'name_bn': localRow.nameBn,
          'address': localRow.address,
          'district': localRow.district,
          'upazila': localRow.upazila,
          'established_year': localRow.establishedYear,
          'committee_start_year': localRow.committeeStartYear,
          'committee_end_year': localRow.committeeEndYear,
          'bank_account_info': localRow.bankAccountInfo,
        };
      }
    } catch (_) {}

    return null;
  }

  /// Restores user session from encrypted local storage
  Future<AuthUser?> restoreSession() async {
    if (!_storage.isLoggedIn) return null;

    final mosqueId = _storage.activeMosqueId;
    if (mosqueId.isEmpty) return null;

    var mosqueNameBn = _storage.mosqueNameBn;
    var mosqueAddress = _storage.mosqueAddress;
    var committeeId = _storage.committeeId;
    var establishedYear = _storage.establishedYear;

    if (mosqueNameBn.isEmpty || mosqueAddress.isEmpty || committeeId.isEmpty) {
      try {
        final localRow = await (_database.select(_database.mosquesTable)
              ..where((t) => t.id.equals(mosqueId)))
            .getSingleOrNull();
        if (localRow != null) {
          if (mosqueNameBn.isEmpty) mosqueNameBn = localRow.nameBn;
          if (mosqueAddress.isEmpty) mosqueAddress = localRow.address;
          if (committeeId.isEmpty) committeeId = localRow.committeeId;
          establishedYear ??= localRow.establishedYear;
        }
      } catch (_) {}
    }

    return AuthUser(
      uid: SupabaseService.currentUserId ?? 'local_user',
      email: _storage.userEmail,
      mobile: _storage.userMobile,
      memberId: _storage.memberId,
      memberName: _storage.memberName,
      committeeId: committeeId,
      roleCode: _storage.userRoleCode,
      mosqueId: mosqueId,
      mosqueNameBn: mosqueNameBn,
      mosqueAddress: mosqueAddress,
      establishedYear: establishedYear,
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
          .select('id, mosque_id, full_name_bn, phone')
          .eq('id', user.id)
          .maybeSingle();

      final mosqueId = profileRes?['mosque_id']?.toString() ?? '';
      if (mosqueId.isEmpty) {
        throw Exception('এই অ্যাকাউন্টের সাথে কোনো মসজিদ প্রোফাইল যুক্ত নেই।');
      }

      // Resolve actual committee role from members table
      Map<String, dynamic>? memberRes;
      try {
        memberRes = await SupabaseService.client
            .from('members')
            .select('id, role_code, name_bn, mobile')
            .eq('mosque_id', mosqueId)
            .eq('id', user.id)
            .maybeSingle();
        memberRes ??= await SupabaseService.client
            .from('members')
            .select('id, role_code, name_bn, mobile')
            .eq('mosque_id', mosqueId)
            .eq('email', cleanEmail)
            .maybeSingle();
      } catch (_) {}

      final roleStr = memberRes?['role_code']?.toString().trim() ?? '';
      final roleCode = roleStr.isNotEmpty ? roleStr : 'president';
      final memberId = memberRes?['id']?.toString() ?? user.id;
      final memberName = memberRes?['name_bn']?.toString().trim().isNotEmpty == true
          ? memberRes!['name_bn'].toString()
          : (profileRes?['full_name_bn']?.toString() ?? 'সভাপতি');
      final mobile = memberRes?['mobile']?.toString() ??
          profileRes?['phone']?.toString() ??
          (user.phone ?? '');

      final mosqueData = await _fetchAndCacheMosque(mosqueId);
      final mosqueNameBn = mosqueData?['name_bn']?.toString() ?? '';
      final mosqueAddress = mosqueData?['address']?.toString() ?? '';
      final committeeId = mosqueData?['committee_id']?.toString() ?? '';
      final establishedYear = _toInt(mosqueData?['established_year']);

      await _storage.saveSession(
        roleCode: roleCode,
        memberId: memberId,
        memberName: memberName,
        committeeId: committeeId,
        mosqueId: mosqueId,
        email: cleanEmail,
        mobile: mobile,
        mosqueNameBn: mosqueNameBn,
        mosqueAddress: mosqueAddress,
        establishedYear: establishedYear,
      );

      return AuthUser(
        uid: user.id,
        email: cleanEmail,
        mobile: mobile,
        memberId: memberId,
        memberName: memberName,
        committeeId: committeeId,
        roleCode: roleCode,
        mosqueId: mosqueId,
        mosqueNameBn: mosqueNameBn,
        mosqueAddress: mosqueAddress,
        establishedYear: establishedYear,
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
      final memberId = memberRes?['id']?.toString() ?? user.id;
      final mosqueId = memberRes?['mosque_id']?.toString() ?? '';
      if (mosqueId.isEmpty) {
        throw Exception('সদস্যের সাথে কোনো মসজিদ প্রোফাইল যুক্ত পাওয়া যায়নি।');
      }

      final mosqueData = await _fetchAndCacheMosque(mosqueId);
      final mosqueCommitteeId =
          (mosqueData?['committee_id']?.toString() ?? '').trim().toUpperCase();

      if (cleanCommitteeId.isNotEmpty &&
          mosqueCommitteeId.isNotEmpty &&
          cleanCommitteeId != mosqueCommitteeId) {
        try {
          await SupabaseService.client.auth.signOut();
        } catch (_) {}
        throw Exception('প্রদত্ত কমিটি কোড ($cleanCommitteeId) এই মসজিদের সাথে মিলছে না। সঠিক কমিটি কোড দিন।');
      }

      final resolvedCommitteeId =
          mosqueCommitteeId.isNotEmpty ? mosqueCommitteeId : cleanCommitteeId;
      final mosqueNameBn = mosqueData?['name_bn']?.toString() ?? '';
      final mosqueAddress = mosqueData?['address']?.toString() ?? '';
      final establishedYear = _toInt(mosqueData?['established_year']);

      await _storage.saveSession(
        roleCode: roleCode,
        memberId: memberId,
        memberName: memberName,
        committeeId: resolvedCommitteeId,
        mosqueId: mosqueId,
        email: syntheticEmail,
        mobile: cleanMobile,
        mosqueNameBn: mosqueNameBn,
        mosqueAddress: mosqueAddress,
        establishedYear: establishedYear,
      );

      return AuthUser(
        uid: user.id,
        email: syntheticEmail,
        mobile: cleanMobile,
        memberId: memberId,
        memberName: memberName,
        committeeId: resolvedCommitteeId,
        roleCode: roleCode,
        mosqueId: mosqueId,
        mosqueNameBn: mosqueNameBn,
        mosqueAddress: mosqueAddress,
        establishedYear: establishedYear,
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
      email: presidentEmail.trim().toLowerCase(),
      mobile: presidentMobile,
      mosqueNameBn: mosqueNameBn,
      mosqueAddress: '$upazila, $district',
      establishedYear: establishedYear,
    );

    return generatedCommitteeId;
  }

  /// Sends a Supabase password reset email
  Future<void> sendPasswordResetEmail(String email) async {
    final cleanEmail = email.trim().toLowerCase();
    if (cleanEmail.isEmpty || !cleanEmail.contains('@')) {
      throw Exception('সঠিক ইমেইল ঠিকানা প্রদান করুন।');
    }
    if (!SupabaseService.isInitialized) {
      throw Exception('ক্লাউড সার্ভারের সাথে সংযোগ স্থাপন করা যায়নি।');
    }
    await SupabaseService.client.auth.resetPasswordForEmail(cleanEmail);
  }

  /// Updates mosque metadata on Supabase + Drift + session storage
  Future<void> updateMosqueProfile({
    required String mosqueId,
    required String nameBn,
    required String address,
    int? establishedYear,
  }) async {
    final cleanName = nameBn.trim();
    final cleanAddress = address.trim();
    final payload = <String, dynamic>{
      'name_bn': cleanName,
      'address': cleanAddress,
      if (establishedYear != null) 'established_year': establishedYear,
    };

    try {
      if (SupabaseService.isInitialized) {
        await SupabaseService.client
            .from('mosques')
            .update(payload)
            .eq('id', mosqueId);
      } else {
        await OfflineCacheService.instance.updateRecord(
          tableName: 'mosques',
          mosqueId: mosqueId,
          id: mosqueId,
          updates: payload,
        );
      }
    } catch (e) {
      debugPrint('Mosque profile cloud update queued: $e');
      await OfflineCacheService.instance.updateRecord(
        tableName: 'mosques',
        mosqueId: mosqueId,
        id: mosqueId,
        updates: payload,
      );
    }

    try {
      await (_database.update(_database.mosquesTable)
            ..where((t) => t.id.equals(mosqueId)))
          .write(
        MosquesTableCompanion(
          nameBn: Value(cleanName),
          address: Value(cleanAddress),
          establishedYear:
              establishedYear != null ? Value(establishedYear) : const Value.absent(),
        ),
      );
    } catch (_) {}

    await _storage.updateMosqueMetadata(
      mosqueNameBn: cleanName,
      mosqueAddress: cleanAddress,
      establishedYear: establishedYear,
    );
  }

  /// Logout and atomic tenant data wipe
  Future<void> logout() async {
    try {
      if (SupabaseService.isInitialized) {
        await SupabaseService.client.auth.signOut();
      }
    } catch (_) {}

    OfflineCacheService.instance.clearMemoryCache();
    await _database.clearAllTenantData();
    await _storage.clearSession();
  }
}
