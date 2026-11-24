import 'package:equatable/equatable.dart';

/// Madrasa Student Model (মাদরাসা শিক্ষার্থী মডেল)
class StudentModel extends Equatable {
  final String id;
  final String studentCode; // e.g. ST-2026-001
  final String nameBn;
  final String nameEn;
  final String className; // নূরানী, নাজেরা, হিফজ, কিতাব বিভাগ
  final String guardianName;
  final String guardianMobile;
  final String guardianNid;
  final double monthlyFee;
  final String admissionDate; // YYYY-MM-DD
  final String bloodGroup;
  final String status; // 'active', 'inactive', 'graduated'
  final String? photoUrl;
  final int createdAt;

  const StudentModel({
    required this.id,
    required this.studentCode,
    required this.nameBn,
    this.nameEn = '',
    required this.className,
    required this.guardianName,
    required this.guardianMobile,
    this.guardianNid = '',
    this.monthlyFee = 0.0,
    required this.admissionDate,
    this.bloodGroup = '',
    this.status = 'active',
    this.photoUrl,
    required this.createdAt,
  });

  bool get isActive => status == 'active';

  factory StudentModel.fromJson(Map<String, dynamic> json) {
    return StudentModel(
      id: json['id'] as String? ?? '',
      studentCode: json['student_code'] as String? ?? '',
      nameBn: json['name_bn'] as String? ?? '',
      nameEn: json['name_en'] as String? ?? '',
      className: json['class_name'] as String? ?? 'নূরানী',
      guardianName: json['guardian_name'] as String? ?? '',
      guardianMobile: json['guardian_mobile'] as String? ?? '',
      guardianNid: json['guardian_nid'] as String? ?? '',
      monthlyFee: (json['monthly_fee'] as num?)?.toDouble() ?? 0.0,
      admissionDate: json['admission_date'] as String? ?? '',
      bloodGroup: json['blood_group'] as String? ?? '',
      status: json['status'] as String? ?? 'active',
      photoUrl: json['photo_url'] as String?,
      createdAt: json['created_at'] != null
          ? (json['created_at'] is int
              ? json['created_at'] as int
              : DateTime.tryParse(json['created_at'].toString())?.millisecondsSinceEpoch ?? 0)
          : 0,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'student_code': studentCode,
      'name_bn': nameBn,
      'name_en': nameEn,
      'class_name': className,
      'guardian_name': guardianName,
      'guardian_mobile': guardianMobile,
      'guardian_nid': guardianNid,
      'monthly_fee': monthlyFee,
      'admission_date': admissionDate,
      'blood_group': bloodGroup,
      'status': status,
      'photo_url': photoUrl,
      'created_at': createdAt,
    };
  }

  StudentModel copyWith({
    String? id,
    String? studentCode,
    String? nameBn,
    String? nameEn,
    String? className,
    String? guardianName,
    String? guardianMobile,
    String? guardianNid,
    double? monthlyFee,
    String? admissionDate,
    String? bloodGroup,
    String? status,
    String? photoUrl,
    int? createdAt,
  }) {
    return StudentModel(
      id: id ?? this.id,
      studentCode: studentCode ?? this.studentCode,
      nameBn: nameBn ?? this.nameBn,
      nameEn: nameEn ?? this.nameEn,
      className: className ?? this.className,
      guardianName: guardianName ?? this.guardianName,
      guardianMobile: guardianMobile ?? this.guardianMobile,
      guardianNid: guardianNid ?? this.guardianNid,
      monthlyFee: monthlyFee ?? this.monthlyFee,
      admissionDate: admissionDate ?? this.admissionDate,
      bloodGroup: bloodGroup ?? this.bloodGroup,
      status: status ?? this.status,
      photoUrl: photoUrl ?? this.photoUrl,
      createdAt: createdAt ?? this.createdAt,
    );
  }

  @override
  List<Object?> get props => [
        id,
        studentCode,
        nameBn,
        nameEn,
        className,
        guardianName,
        guardianMobile,
        monthlyFee,
        admissionDate,
        status,
      ];
}
