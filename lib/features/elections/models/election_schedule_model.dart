import 'package:equatable/equatable.dart';

/// Election Schedule Model for mosque committee formation (কমিটি নির্বাচন তফসিল মডেল)
class ElectionScheduleModel extends Equatable {
  final String id;
  final String termTitle; // e.g. "২০২৬–২০২৮ সেশন"
  final String commissionerName;
  final String nominationStartDate;
  final String nominationEndDate;
  final String votingDate;
  final String status; // 'draft', 'scheduled', 'in_progress', 'completed'
  final String gazetteNotes;
  final int createdAt;

  const ElectionScheduleModel({
    required this.id,
    required this.termTitle,
    required this.commissionerName,
    required this.nominationStartDate,
    required this.nominationEndDate,
    required this.votingDate,
    this.status = 'scheduled',
    this.gazetteNotes = '',
    required this.createdAt,
  });

  bool get isCompleted => status == 'completed';

  factory ElectionScheduleModel.fromJson(Map<String, dynamic> json) {
    return ElectionScheduleModel(
      id: json['id'] as String? ?? '',
      termTitle: json['term_title'] as String? ?? '',
      commissionerName: json['commissioner_name'] as String? ?? '',
      nominationStartDate: json['nomination_start_date'] as String? ?? '',
      nominationEndDate: json['nomination_end_date'] as String? ?? '',
      votingDate: json['voting_date'] as String? ?? '',
      status: json['status'] as String? ?? 'scheduled',
      gazetteNotes: json['gazette_notes'] as String? ?? '',
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
      'term_title': termTitle,
      'commissioner_name': commissionerName,
      'nomination_start_date': nominationStartDate,
      'nomination_end_date': nominationEndDate,
      'voting_date': votingDate,
      'status': status,
      'gazette_notes': gazetteNotes,
    };
  }

  @override
  List<Object?> get props => [id, termTitle, commissionerName, votingDate, status];
}
