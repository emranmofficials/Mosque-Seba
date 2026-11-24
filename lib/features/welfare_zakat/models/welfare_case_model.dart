import 'package:equatable/equatable.dart';

/// Welfare & Zakat Assistance Case Model (সমাজকল্যাণ ও যাকাত তহবিল মডেল)
class WelfareCaseModel extends Equatable {
  final String id;
  final String caseCode; // e.g. WLF-2026-001
  final String applicantName;
  final String applicantMobile;
  final String applicantNid;
  final String address;
  final String category; // 'medical', 'widow', 'orphan', 'disaster', 'debt_relief', 'education'
  final String fundType; // 'zakat', 'sadakah', 'general_welfare'
  final double requestedAmount;
  final double approvedAmount;
  final String status; // 'pending', 'verified', 'approved', 'disbursed', 'rejected'
  final String? verifiedBy;
  final String? approvedBy;
  final String notes;
  final String applicationDate; // YYYY-MM-DD
  final String? disbursedDate;
  final int createdAt;

  const WelfareCaseModel({
    required this.id,
    required this.caseCode,
    required this.applicantName,
    required this.applicantMobile,
    this.applicantNid = '',
    required this.address,
    required this.category,
    this.fundType = 'zakat',
    required this.requestedAmount,
    this.approvedAmount = 0.0,
    this.status = 'pending',
    this.verifiedBy,
    this.approvedBy,
    this.notes = '',
    required this.applicationDate,
    this.disbursedDate,
    required this.createdAt,
  });

  bool get isPending => status == 'pending';
  bool get isApproved => status == 'approved';
  bool get isDisbursed => status == 'disbursed';

  String get categoryTitleBn {
    switch (category) {
      case 'medical':
        return 'চিকিৎসা সহায়তা';
      case 'widow':
        return 'বিধবা ও দুস্থ ভাতা';
      case 'orphan':
        return 'এতিম ও শিক্ষা সহায়তা';
      case 'disaster':
        return 'প্রাকৃতিক দুর্যোগ ত্রাণ';
      case 'debt_relief':
        return 'ঋণগ্রস্তদের সহায়তা';
      default:
        return 'সাধারণ সমাজকল্যাণ';
    }
  }

  String get fundTypeTitleBn {
    switch (fundType) {
      case 'zakat':
        return 'যাকাত তহবিল';
      case 'sadakah':
        return 'সদকা তহবিল';
      default:
        return 'সাধারণ কল্যাণ তহবিল';
    }
  }

  factory WelfareCaseModel.fromJson(Map<String, dynamic> json) {
    return WelfareCaseModel(
      id: json['id'] as String? ?? '',
      caseCode: json['case_code'] as String? ?? '',
      applicantName: json['applicant_name'] as String? ?? '',
      applicantMobile: json['applicant_mobile'] as String? ?? '',
      applicantNid: json['applicant_nid'] as String? ?? '',
      address: json['address'] as String? ?? '',
      category: json['category'] as String? ?? 'medical',
      fundType: json['fund_type'] as String? ?? 'zakat',
      requestedAmount: (json['requested_amount'] as num?)?.toDouble() ?? 0.0,
      approvedAmount: (json['approved_amount'] as num?)?.toDouble() ?? 0.0,
      status: json['status'] as String? ?? 'pending',
      verifiedBy: json['verified_by'] as String?,
      approvedBy: json['approved_by'] as String?,
      notes: json['notes'] as String? ?? '',
      applicationDate: json['application_date'] as String? ?? '',
      disbursedDate: json['disbursed_date'] as String?,
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
      'case_code': caseCode,
      'applicant_name': applicantName,
      'applicant_mobile': applicantMobile,
      'applicant_nid': applicantNid,
      'address': address,
      'category': category,
      'fund_type': fundType,
      'requested_amount': requestedAmount,
      'approved_amount': approvedAmount,
      'status': status,
      'verified_by': verifiedBy,
      'approved_by': approvedBy,
      'notes': notes,
      'application_date': applicationDate,
      'disbursed_date': disbursedDate,
      'created_at': createdAt,
    };
  }

  WelfareCaseModel copyWith({
    String? id,
    String? caseCode,
    String? applicantName,
    String? applicantMobile,
    String? applicantNid,
    String? address,
    String? category,
    String? fundType,
    double? requestedAmount,
    double? approvedAmount,
    String? status,
    String? verifiedBy,
    String? approvedBy,
    String? notes,
    String? applicationDate,
    String? disbursedDate,
    int? createdAt,
  }) {
    return WelfareCaseModel(
      id: id ?? this.id,
      caseCode: caseCode ?? this.caseCode,
      applicantName: applicantName ?? this.applicantName,
      applicantMobile: applicantMobile ?? this.applicantMobile,
      applicantNid: applicantNid ?? this.applicantNid,
      address: address ?? this.address,
      category: category ?? this.category,
      fundType: fundType ?? this.fundType,
      requestedAmount: requestedAmount ?? this.requestedAmount,
      approvedAmount: approvedAmount ?? this.approvedAmount,
      status: status ?? this.status,
      verifiedBy: verifiedBy ?? this.verifiedBy,
      approvedBy: approvedBy ?? this.approvedBy,
      notes: notes ?? this.notes,
      applicationDate: applicationDate ?? this.applicationDate,
      disbursedDate: disbursedDate ?? this.disbursedDate,
      createdAt: createdAt ?? this.createdAt,
    );
  }

  @override
  List<Object?> get props => [
        id,
        caseCode,
        applicantName,
        applicantMobile,
        category,
        fundType,
        requestedAmount,
        approvedAmount,
        status,
      ];
}
