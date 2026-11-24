import 'package:equatable/equatable.dart';

/// Madrasa Examination Model (মাদরাসা পরীক্ষা মডেল)
class ExamModel extends Equatable {
  final String id;
  final String examNameBn; // e.g. ১ম সাময়িক পরীক্ষা ২০২৬
  final String examType; // 'quarterly', 'half_yearly', 'annual', 'hifz_eval'
  final String academicYear; // '২০২৬'
  final String startDate; // YYYY-MM-DD
  final String status; // 'scheduled', 'ongoing', 'completed', 'published'
  final int createdAt;

  const ExamModel({
    required this.id,
    required this.examNameBn,
    this.examType = 'half_yearly',
    this.academicYear = '২০২৬',
    required this.startDate,
    this.status = 'published',
    required this.createdAt,
  });

  bool get isPublished => status == 'published';

  factory ExamModel.fromJson(Map<String, dynamic> json) {
    return ExamModel(
      id: json['id'] as String? ?? '',
      examNameBn: json['exam_name_bn'] as String? ?? '',
      examType: json['exam_type'] as String? ?? 'half_yearly',
      academicYear: json['academic_year'] as String? ?? '২০২৬',
      startDate: json['start_date'] as String? ?? '',
      status: json['status'] as String? ?? 'published',
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
      'exam_name_bn': examNameBn,
      'exam_type': examType,
      'academic_year': academicYear,
      'start_date': startDate,
      'status': status,
      'created_at': createdAt,
    };
  }

  @override
  List<Object?> get props => [id, examNameBn, examType, academicYear, startDate, status];
}

/// Student Subject Result Model (শিক্ষার্থীর বিষয়ভিত্তিক ফলাফল মডেল)
class ExamResultModel extends Equatable {
  final String id;
  final String examId;
  final String studentId;
  final String studentNameBn;
  final String className;
  final String subjectName; // যেমন: কুরআন তিলাওয়াত ও তাজবীদ
  final double marksObtained;
  final double totalMarks;
  final String grade; // মুমতাজ, জাইয়িদ জিদ্দান, জাইয়িদ, মাকবুল, রাসিব
  final String remarks;
  final int createdAt;

  const ExamResultModel({
    required this.id,
    required this.examId,
    required this.studentId,
    required this.studentNameBn,
    required this.className,
    required this.subjectName,
    required this.marksObtained,
    this.totalMarks = 100.0,
    required this.grade,
    this.remarks = '',
    required this.createdAt,
  });

  static String calculateIslamicGrade(double obtained, double total) {
    final percent = (obtained / total) * 100;
    if (percent >= 80) return 'মুমতাজ (A+)';
    if (percent >= 70) return 'জাইয়িদ জিদ্দান (A)';
    if (percent >= 60) return 'জাইয়িদ (B)';
    if (percent >= 50) return 'মাকবুল (C)';
    return 'রাসিব (F)';
  }

  factory ExamResultModel.fromJson(Map<String, dynamic> json) {
    return ExamResultModel(
      id: json['id'] as String? ?? '',
      examId: json['exam_id'] as String? ?? '',
      studentId: json['student_id'] as String? ?? '',
      studentNameBn: json['student_name_bn'] as String? ?? '',
      className: json['class_name'] as String? ?? '',
      subjectName: json['subject_name'] as String? ?? '',
      marksObtained: (json['marks_obtained'] as num?)?.toDouble() ?? 0.0,
      totalMarks: (json['total_marks'] as num?)?.toDouble() ?? 100.0,
      grade: json['grade'] as String? ?? 'মুমতাজ (A+)',
      remarks: json['remarks'] as String? ?? '',
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
      'exam_id': examId,
      'student_id': studentId,
      'student_name_bn': studentNameBn,
      'class_name': className,
      'subject_name': subjectName,
      'marks_obtained': marksObtained,
      'total_marks': totalMarks,
      'grade': grade,
      'remarks': remarks,
      'created_at': createdAt,
    };
  }

  @override
  List<Object?> get props => [
        id,
        examId,
        studentId,
        studentNameBn,
        subjectName,
        marksObtained,
        grade,
      ];
}
