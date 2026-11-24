import 'package:equatable/equatable.dart';

/// Hifz Daily Progress Record Model (হিফজুল কুরআন দৈনিক ছবক ও মূল্যায়ন মডেল)
class HifzRecordModel extends Equatable {
  final String id;
  final String studentId;
  final String studentNameBn;
  final int paraNumber; // 1 to 30
  final String surahName;
  final int pageNumber;
  final int sabaqRating; // 1 to 5 stars (ছবক)
  final int sabaqiRating; // 1 to 5 stars (ছবকী)
  final int manzilRating; // 1 to 5 stars (আমোখতা / মঞ্জিল)
  final String teacherRemarks;
  final String recordedDate; // YYYY-MM-DD
  final int createdAt;

  const HifzRecordModel({
    required this.id,
    required this.studentId,
    required this.studentNameBn,
    required this.paraNumber,
    required this.surahName,
    this.pageNumber = 1,
    this.sabaqRating = 5,
    this.sabaqiRating = 5,
    this.manzilRating = 5,
    this.teacherRemarks = '',
    required this.recordedDate,
    required this.createdAt,
  });

  factory HifzRecordModel.fromJson(Map<String, dynamic> json) {
    return HifzRecordModel(
      id: json['id'] as String? ?? '',
      studentId: json['student_id'] as String? ?? '',
      studentNameBn: json['student_name_bn'] as String? ?? '',
      paraNumber: json['para_number'] as int? ?? 1,
      surahName: json['surah_name'] as String? ?? '',
      pageNumber: json['page_number'] as int? ?? 1,
      sabaqRating: json['sabaq_rating'] as int? ?? 5,
      sabaqiRating: json['sabaqi_rating'] as int? ?? 5,
      manzilRating: json['manzil_rating'] as int? ?? 5,
      teacherRemarks: json['teacher_remarks'] as String? ?? '',
      recordedDate: json['recorded_date'] as String? ?? '',
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
      'student_id': studentId,
      'student_name_bn': studentNameBn,
      'para_number': paraNumber,
      'surah_name': surahName,
      'page_number': pageNumber,
      'sabaq_rating': sabaqRating,
      'sabaqi_rating': sabaqiRating,
      'manzil_rating': manzilRating,
      'teacher_remarks': teacherRemarks,
      'recorded_date': recordedDate,
      'created_at': createdAt,
    };
  }

  HifzRecordModel copyWith({
    String? id,
    String? studentId,
    String? studentNameBn,
    int? paraNumber,
    String? surahName,
    int? pageNumber,
    int? sabaqRating,
    int? sabaqiRating,
    int? manzilRating,
    String? teacherRemarks,
    String? recordedDate,
    int? createdAt,
  }) {
    return HifzRecordModel(
      id: id ?? this.id,
      studentId: studentId ?? this.studentId,
      studentNameBn: studentNameBn ?? this.studentNameBn,
      paraNumber: paraNumber ?? this.paraNumber,
      surahName: surahName ?? this.surahName,
      pageNumber: pageNumber ?? this.pageNumber,
      sabaqRating: sabaqRating ?? this.sabaqRating,
      sabaqiRating: sabaqiRating ?? this.sabaqiRating,
      manzilRating: manzilRating ?? this.manzilRating,
      teacherRemarks: teacherRemarks ?? this.teacherRemarks,
      recordedDate: recordedDate ?? this.recordedDate,
      createdAt: createdAt ?? this.createdAt,
    );
  }

  @override
  List<Object?> get props => [
        id,
        studentId,
        paraNumber,
        surahName,
        pageNumber,
        sabaqRating,
        sabaqiRating,
        manzilRating,
        recordedDate,
      ];
}
