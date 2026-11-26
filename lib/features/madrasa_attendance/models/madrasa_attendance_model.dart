import 'package:equatable/equatable.dart';

/// Madrasa Student Attendance Record Model (মাদরাসা শিক্ষার্থীর হাজিরা রেকর্ড মডেল)
class MadrasaAttendanceModel extends Equatable {
  final String id;
  final String studentId;
  final String studentNameBn;
  final String className;
  final String attendanceDate; // YYYY-MM-DD
  final String status; // 'present', 'absent', 'late', 'leave'
  final String remarks;
  final String recordedBy;
  final int createdAt;

  const MadrasaAttendanceModel({
    required this.id,
    required this.studentId,
    required this.studentNameBn,
    required this.className,
    required this.attendanceDate,
    this.status = 'present',
    this.remarks = '',
    required this.recordedBy,
    required this.createdAt,
  });

  bool get isPresent => status == 'present';
  bool get isAbsent => status == 'absent';
  bool get isLate => status == 'late';
  bool get isLeave => status == 'leave';

  factory MadrasaAttendanceModel.fromJson(Map<String, dynamic> json) {
    final rawAttDate = json['attendance_date'] as String? ?? '';
    return MadrasaAttendanceModel(
      id: json['id'] as String? ?? '',
      studentId: json['student_id'] as String? ?? '',
      studentNameBn: json['student_name_bn'] as String? ?? '',
      className: json['class_name'] as String? ?? '',
      attendanceDate: rawAttDate.isNotEmpty
          ? rawAttDate
          : (json['date'] as String? ?? ''),
      status: json['status'] as String? ?? 'present',
      remarks: json['remarks'] as String? ?? '',
      recordedBy: json['recorded_by'] as String? ?? 'শিক্ষক',
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
      'class_name': className,
      'attendance_date': attendanceDate,
      'status': status,
      'remarks': remarks,
      'recorded_by': recordedBy,
      'created_at': createdAt,
    };
  }

  MadrasaAttendanceModel copyWith({
    String? id,
    String? studentId,
    String? studentNameBn,
    String? className,
    String? attendanceDate,
    String? status,
    String? remarks,
    String? recordedBy,
    int? createdAt,
  }) {
    return MadrasaAttendanceModel(
      id: id ?? this.id,
      studentId: studentId ?? this.studentId,
      studentNameBn: studentNameBn ?? this.studentNameBn,
      className: className ?? this.className,
      attendanceDate: attendanceDate ?? this.attendanceDate,
      status: status ?? this.status,
      remarks: remarks ?? this.remarks,
      recordedBy: recordedBy ?? this.recordedBy,
      createdAt: createdAt ?? this.createdAt,
    );
  }

  @override
  List<Object?> get props => [
        id,
        studentId,
        studentNameBn,
        attendanceDate,
        status,
        remarks,
      ];
}
