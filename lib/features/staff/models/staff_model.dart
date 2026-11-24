import 'package:equatable/equatable.dart';

/// Mosque Operational Staff Model (মসজিদের বেতনভুক্ত স্টাফ)
class StaffModel extends Equatable {
  final String id;
  final String nameBn;
  final String roleCode; // 'imam', 'muazzin', 'khadem', 'teacher', 'other'
  final String designationBn; // 'পেশ ইমাম', 'মুয়াজ্জিন', 'খাদেম', 'হিফজ শিক্ষক'
  final String employmentType; // 'full_time', 'part_time', 'contract'
  final String joinDate;
  final double salaryAmount;
  final String salaryFrequency; // 'monthly'
  final bool mealRequired;
  final String mobile;
  final bool isActive;
  final String? signatureUrl;
  final String? stampUrl;

  const StaffModel({
    required this.id,
    required this.nameBn,
    required this.roleCode,
    required this.designationBn,
    this.employmentType = 'full_time',
    this.joinDate = '',
    required this.salaryAmount,
    this.salaryFrequency = 'monthly',
    this.mealRequired = true,
    required this.mobile,
    this.isActive = true,
    this.signatureUrl,
    this.stampUrl,
  });

  factory StaffModel.fromJson(Map<String, dynamic> json) {
    return StaffModel(
      id: json['id'] as String? ?? '',
      nameBn: json['name_bn'] as String? ?? '',
      roleCode: json['role_code'] as String? ?? 'other',
      designationBn: json['designation_bn'] as String? ?? 'স্টাফ',
      employmentType: json['employment_type'] as String? ?? 'full_time',
      joinDate: json['join_date'] as String? ?? '',
      salaryAmount: (json['salary_amount'] as num?)?.toDouble() ?? 0.0,
      salaryFrequency: json['salary_frequency'] as String? ?? 'monthly',
      mealRequired: json['meal_required'] as bool? ?? true,
      mobile: json['mobile'] as String? ?? '',
      isActive: json['is_active'] as bool? ?? true,
      signatureUrl: json['signature_url'] as String?,
      stampUrl: json['stamp_url'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name_bn': nameBn,
      'role_code': roleCode,
      'designation_bn': designationBn,
      'employment_type': employmentType,
      'join_date': joinDate,
      'salary_amount': salaryAmount,
      'salary_frequency': salaryFrequency,
      'meal_required': mealRequired,
      'mobile': mobile,
      'is_active': isActive,
      'signature_url': signatureUrl,
      'stamp_url': stampUrl,
    };
  }

  StaffModel copyWith({
    String? id,
    String? nameBn,
    String? roleCode,
    String? designationBn,
    String? employmentType,
    String? joinDate,
    double? salaryAmount,
    String? salaryFrequency,
    bool? mealRequired,
    String? mobile,
    bool? isActive,
    String? signatureUrl,
    String? stampUrl,
  }) {
    return StaffModel(
      id: id ?? this.id,
      nameBn: nameBn ?? this.nameBn,
      roleCode: roleCode ?? this.roleCode,
      designationBn: designationBn ?? this.designationBn,
      employmentType: employmentType ?? this.employmentType,
      joinDate: joinDate ?? this.joinDate,
      salaryAmount: salaryAmount ?? this.salaryAmount,
      salaryFrequency: salaryFrequency ?? this.salaryFrequency,
      mealRequired: mealRequired ?? this.mealRequired,
      mobile: mobile ?? this.mobile,
      isActive: isActive ?? this.isActive,
      signatureUrl: signatureUrl ?? this.signatureUrl,
      stampUrl: stampUrl ?? this.stampUrl,
    );
  }

  @override
  List<Object?> get props => [
        id,
        nameBn,
        roleCode,
        designationBn,
        salaryAmount,
        mobile,
        isActive,
      ];
}
