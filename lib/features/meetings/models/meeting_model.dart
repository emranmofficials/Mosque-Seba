import 'package:equatable/equatable.dart';

/// Mosque Executive Meeting Model (মসজিদ কমিটির সভা মডেল)
class MeetingModel extends Equatable {
  final String id;
  final String title;
  final String meetingDate; // YYYY-MM-DD or formatted date-time
  final String location;
  final String agenda;
  final String status; // 'upcoming', 'in_progress', 'completed', 'cancelled'
  final String resolution;
  final int createdAt;

  const MeetingModel({
    required this.id,
    required this.title,
    required this.meetingDate,
    required this.location,
    required this.agenda,
    this.status = 'upcoming',
    this.resolution = '',
    required this.createdAt,
  });

  bool get isCompleted => status == 'completed';

  factory MeetingModel.fromJson(Map<String, dynamic> json) {
    return MeetingModel(
      id: json['id'] as String? ?? '',
      title: json['title'] as String? ?? '',
      meetingDate: json['meeting_date'] as String? ?? '',
      location: json['location'] as String? ?? 'মসজিদ কার্যালয়',
      agenda: json['agenda'] as String? ?? '',
      status: json['status'] as String? ?? 'upcoming',
      resolution: json['resolution'] as String? ?? '',
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
      'title': title,
      'meeting_date': meetingDate,
      'location': location,
      'agenda': agenda,
      'status': status,
      'resolution': resolution,
    };
  }

  MeetingModel copyWith({
    String? id,
    String? title,
    String? meetingDate,
    String? location,
    String? agenda,
    String? status,
    String? resolution,
    int? createdAt,
  }) {
    return MeetingModel(
      id: id ?? this.id,
      title: title ?? this.title,
      meetingDate: meetingDate ?? this.meetingDate,
      location: location ?? this.location,
      agenda: agenda ?? this.agenda,
      status: status ?? this.status,
      resolution: resolution ?? this.resolution,
      createdAt: createdAt ?? this.createdAt,
    );
  }

  @override
  List<Object?> get props => [
        id,
        title,
        meetingDate,
        location,
        status,
        resolution,
      ];
}
