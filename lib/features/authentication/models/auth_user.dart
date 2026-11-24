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
    );
  }

  bool get isAuthenticated => uid.isNotEmpty && mosqueId.isNotEmpty;
}
