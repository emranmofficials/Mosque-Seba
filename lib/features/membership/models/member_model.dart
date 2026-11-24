import 'package:equatable/equatable.dart';

/// Mosque Member Model (মসজিদ সদস্য)
class MemberModel extends Equatable {
  final String id;
  final String memberCode;
  final String nameBn;
  final String nameEn;
  final String mobile;
  final String email;
  final String nid;
  final String address;
  final String villageArea;
  final String roleCode;
  final double monthlyChanda;
  final String joinDate;
  final String? photoUrl;
  final String? signatureUrl;
  final String? stampUrl;
  final String? designationBn;
  final String status; // 'active', 'inactive', 'deceased'
  final bool willingToProvideStaffMeals;

  const MemberModel({
    required this.id,
    required this.memberCode,
    required this.nameBn,
    this.nameEn = '',
    required this.mobile,
    this.email = '',
    this.nid = '',
    this.address = '',
    this.villageArea = '',
    this.roleCode = 'general_member',
    this.monthlyChanda = 0.0,
    this.joinDate = '',
    this.photoUrl,
    this.signatureUrl,
    this.stampUrl,
    this.designationBn,
    this.status = 'active',
    this.willingToProvideStaffMeals = false,
  });

  bool get isActive => status == 'active';

  factory MemberModel.fromJson(Map<String, dynamic> json) {
    return MemberModel(
      id: json['id'] as String? ?? '',
      memberCode: json['member_code'] as String? ?? '',
      nameBn: json['name_bn'] as String? ?? '',
      nameEn: json['name_en'] as String? ?? '',
      mobile: json['mobile'] as String? ?? '',
      email: json['email'] as String? ?? '',
      nid: json['nid'] as String? ?? '',
      address: json['address'] as String? ?? '',
      villageArea: json['village_area'] as String? ?? '',
      roleCode: json['role_code'] as String? ?? 'general_member',
      monthlyChanda: (json['monthly_chanda'] as num?)?.toDouble() ?? 0.0,
      joinDate: json['join_date'] as String? ?? '',
      photoUrl: json['photo_url'] as String?,
      signatureUrl: json['signature_url'] as String?,
      stampUrl: json['stamp_url'] as String?,
      designationBn: json['designation_bn'] as String?,
      status: json['status'] as String? ?? 'active',
      willingToProvideStaffMeals: json['willing_to_provide_staff_meals'] as bool? ?? false,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'member_code': memberCode,
      'name_bn': nameBn,
      'name_en': nameEn,
      'mobile': mobile,
      'email': email,
      'nid': nid,
      'address': address,
      'village_area': villageArea,
      'role_code': roleCode,
      'monthly_chanda': monthlyChanda,
      'join_date': joinDate,
      'photo_url': photoUrl,
      'signature_url': signatureUrl,
      'stamp_url': stampUrl,
      'designation_bn': designationBn,
      'status': status,
      'willing_to_provide_staff_meals': willingToProvideStaffMeals,
    };
  }

  MemberModel copyWith({
    String? id,
    String? memberCode,
    String? nameBn,
    String? nameEn,
    String? mobile,
    String? email,
    String? nid,
    String? address,
    String? villageArea,
    String? roleCode,
    double? monthlyChanda,
    String? joinDate,
    String? photoUrl,
    String? signatureUrl,
    String? stampUrl,
    String? designationBn,
    String? status,
    bool? willingToProvideStaffMeals,
  }) {
    return MemberModel(
      id: id ?? this.id,
      memberCode: memberCode ?? this.memberCode,
      nameBn: nameBn ?? this.nameBn,
      nameEn: nameEn ?? this.nameEn,
      mobile: mobile ?? this.mobile,
      email: email ?? this.email,
      nid: nid ?? this.nid,
      address: address ?? this.address,
      villageArea: villageArea ?? this.villageArea,
      roleCode: roleCode ?? this.roleCode,
      monthlyChanda: monthlyChanda ?? this.monthlyChanda,
      joinDate: joinDate ?? this.joinDate,
      photoUrl: photoUrl ?? this.photoUrl,
      signatureUrl: signatureUrl ?? this.signatureUrl,
      stampUrl: stampUrl ?? this.stampUrl,
      designationBn: designationBn ?? this.designationBn,
      status: status ?? this.status,
      willingToProvideStaffMeals: willingToProvideStaffMeals ?? this.willingToProvideStaffMeals,
    );
  }

  @override
  List<Object?> get props => [
        id,
        memberCode,
        nameBn,
        mobile,
        roleCode,
        monthlyChanda,
        status,
        willingToProvideStaffMeals,
      ];
}
