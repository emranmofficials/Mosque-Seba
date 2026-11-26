/// Authenticated User Model (UserSession.kt Parity)
class AuthUser {
  final String uid;
  final String email;
  final String mobile;
  final String memberId;
  final String memberName;
  final String committeeId;
  final String roleCode;
  final String mosqueId;
  final String mosqueNameBn;
  final String mosqueAddress;
  final int? establishedYear;

  const AuthUser({
    required this.uid,
    required this.email,
    required this.mobile,
    required this.memberId,
    required this.memberName,
    required this.committeeId,
    required this.roleCode,
    required this.mosqueId,
    required this.mosqueNameBn,
    this.mosqueAddress = '',
    this.establishedYear,
  });

  factory AuthUser.empty() {
    return const AuthUser(
      uid: '',
      email: '',
      mobile: '',
      memberId: '',
      memberName: '',
      committeeId: '',
      roleCode: 'general_member',
      mosqueId: '',
      mosqueNameBn: '',
      mosqueAddress: '',
      establishedYear: null,
    );
  }

  AuthUser copyWith({
    String? uid,
    String? email,
    String? mobile,
    String? memberId,
    String? memberName,
    String? committeeId,
    String? roleCode,
    String? mosqueId,
    String? mosqueNameBn,
    String? mosqueAddress,
    int? establishedYear,
  }) {
    return AuthUser(
      uid: uid ?? this.uid,
      email: email ?? this.email,
      mobile: mobile ?? this.mobile,
      memberId: memberId ?? this.memberId,
      memberName: memberName ?? this.memberName,
      committeeId: committeeId ?? this.committeeId,
      roleCode: roleCode ?? this.roleCode,
      mosqueId: mosqueId ?? this.mosqueId,
      mosqueNameBn: mosqueNameBn ?? this.mosqueNameBn,
      mosqueAddress: mosqueAddress ?? this.mosqueAddress,
      establishedYear: establishedYear ?? this.establishedYear,
    );
  }

  bool get isAuthenticated => uid.isNotEmpty && mosqueId.isNotEmpty;
}
