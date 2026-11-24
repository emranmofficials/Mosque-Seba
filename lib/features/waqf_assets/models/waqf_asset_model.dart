import 'package:equatable/equatable.dart';

/// Mosque Waqf Asset / Property Model (ওয়াকফ ও মসজিদ সম্পত্তি)
class WaqfAssetModel extends Equatable {
  final String id;
  final String title;
  final String category; // 'জমি / প্লট', 'দোকান / মার্কেট', 'পুকুর / জলাশয়', 'ভবন / স্থাপনা', 'অন্যান্য'
  final String areaOrQuantity;
  final String locationOrDagNo;
  final String deedOrRegNo;
  final String estimatedValueOrIncome;
  final String? photoUrl;
  final String status; // 'সংরক্ষিত', 'ভাড়ায় চালিত', 'লীজকৃত', 'বিতর্কিত'
  final String description;
  final String recordedDate;
  final DateTime? createdAt;

  const WaqfAssetModel({
    required this.id,
    required this.title,
    required this.category,
    required this.areaOrQuantity,
    required this.locationOrDagNo,
    this.deedOrRegNo = '',
    this.estimatedValueOrIncome = '',
    this.photoUrl,
    this.status = 'সংরক্ষিত',
    this.description = '',
    this.recordedDate = '',
    this.createdAt,
  });

  factory WaqfAssetModel.fromJson(Map<String, dynamic> json) {
    return WaqfAssetModel(
      id: json['id'] as String? ?? '',
      title: json['title'] as String? ?? '',
      category: json['category'] as String? ?? 'জমি / প্লট',
      areaOrQuantity: json['area_or_quantity'] as String? ?? '',
      locationOrDagNo: json['location_or_dag_no'] as String? ?? '',
      deedOrRegNo: json['deed_or_reg_no'] as String? ?? '',
      estimatedValueOrIncome: json['estimated_value_or_income'] as String? ?? '',
      photoUrl: json['photo_url'] as String?,
      status: json['status'] as String? ?? 'সংরক্ষিত',
      description: json['description'] as String? ?? '',
      recordedDate: json['recorded_date'] as String? ?? '',
      createdAt: json['created_at'] != null ? DateTime.tryParse(json['created_at'].toString()) : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
      'category': category,
      'area_or_quantity': areaOrQuantity,
      'location_or_dag_no': locationOrDagNo,
      'deed_or_reg_no': deedOrRegNo,
      'estimated_value_or_income': estimatedValueOrIncome,
      'photo_url': photoUrl,
      'status': status,
      'description': description,
      'recorded_date': recordedDate,
    };
  }

  WaqfAssetModel copyWith({
    String? id,
    String? title,
    String? category,
    String? areaOrQuantity,
    String? locationOrDagNo,
    String? deedOrRegNo,
    String? estimatedValueOrIncome,
    String? photoUrl,
    String? status,
    String? description,
    String? recordedDate,
    DateTime? createdAt,
  }) {
    return WaqfAssetModel(
      id: id ?? this.id,
      title: title ?? this.title,
      category: category ?? this.category,
      areaOrQuantity: areaOrQuantity ?? this.areaOrQuantity,
      locationOrDagNo: locationOrDagNo ?? this.locationOrDagNo,
      deedOrRegNo: deedOrRegNo ?? this.deedOrRegNo,
      estimatedValueOrIncome: estimatedValueOrIncome ?? this.estimatedValueOrIncome,
      photoUrl: photoUrl ?? this.photoUrl,
      status: status ?? this.status,
      description: description ?? this.description,
      recordedDate: recordedDate ?? this.recordedDate,
      createdAt: createdAt ?? this.createdAt,
    );
  }

  @override
  List<Object?> get props => [
        id,
        title,
        category,
        areaOrQuantity,
        locationOrDagNo,
        deedOrRegNo,
        status,
      ];
}
