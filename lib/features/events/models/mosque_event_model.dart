import 'package:equatable/equatable.dart';

/// Mosque Event & Program Model (মসজিদ মাহফিল ও অনুষ্ঠান মডেল)
class MosqueEventModel extends Equatable {
  final String id;
  final String title;
  final String eventType; // 'waz_mahfil', 'shab_e_qadr', 'iftar_party', 'milad_doa', 'eid_gathering'
  final String eventDate; // YYYY-MM-DD
  final String eventTime; // e.g. 'বাদ আসর', 'সকাল ৯:০০'
  final String venue;
  final String chiefGuest;
  final String specialSpeakers;
  final double estimatedBudget;
  final double actualExpense;
  final String status; // 'upcoming', 'ongoing', 'completed', 'cancelled'
  final String description;
  final int createdAt;

  const MosqueEventModel({
    required this.id,
    required this.title,
    this.eventType = 'waz_mahfil',
    required this.eventDate,
    this.eventTime = 'বাদ আসর',
    this.venue = 'মসজিদ প্রাঙ্গণ',
    this.chiefGuest = '',
    this.specialSpeakers = '',
    this.estimatedBudget = 0.0,
    this.actualExpense = 0.0,
    this.status = 'upcoming',
    this.description = '',
    required this.createdAt,
  });

  bool get isUpcoming => status == 'upcoming';
  bool get isCompleted => status == 'completed';

  String get eventTypeTitleBn {
    switch (eventType) {
      case 'waz_mahfil':
        return 'ওয়াজ ও দোয়ার মাহফিল';
      case 'shab_e_qadr':
        return 'পবিত্র শবে কদর ও কিয়ামুল লাইল';
      case 'iftar_party':
        return 'গণ-ইফতার ও দুস্থ বিতরণ';
      case 'milad_doa':
        return 'মিলাদ ও বিশেষ দোয়া';
      case 'eid_gathering':
        return 'ঈদুল ফিতর / আজহা জামাত';
      default:
        return 'ইসলামিক সম্মেলন';
    }
  }

  factory MosqueEventModel.fromJson(Map<String, dynamic> json) {
    return MosqueEventModel(
      id: json['id'] as String? ?? '',
      title: json['title'] as String? ?? '',
      eventType: json['event_type'] as String? ?? 'waz_mahfil',
      eventDate: json['event_date'] as String? ?? '',
      eventTime: json['event_time'] as String? ?? 'বাদ আসর',
      venue: json['venue'] as String? ?? 'মসজিদ প্রাঙ্গণ',
      chiefGuest: json['chief_guest'] as String? ?? '',
      specialSpeakers: json['special_speakers'] as String? ?? '',
      estimatedBudget: (json['estimated_budget'] as num?)?.toDouble() ?? 0.0,
      actualExpense: (json['actual_expense'] as num?)?.toDouble() ?? 0.0,
      status: json['status'] as String? ?? 'upcoming',
      description: json['description'] as String? ?? '',
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
      'event_type': eventType,
      'event_date': eventDate,
      'event_time': eventTime,
      'venue': venue,
      'chief_guest': chiefGuest,
      'special_speakers': specialSpeakers,
      'estimated_budget': estimatedBudget,
      'actual_expense': actualExpense,
      'status': status,
      'description': description,
      'created_at': createdAt,
    };
  }

  MosqueEventModel copyWith({
    String? id,
    String? title,
    String? eventType,
    String? eventDate,
    String? eventTime,
    String? venue,
    String? chiefGuest,
    String? specialSpeakers,
    double? estimatedBudget,
    double? actualExpense,
    String? status,
    String? description,
    int? createdAt,
  }) {
    return MosqueEventModel(
      id: id ?? this.id,
      title: title ?? this.title,
      eventType: eventType ?? this.eventType,
      eventDate: eventDate ?? this.eventDate,
      eventTime: eventTime ?? this.eventTime,
      venue: venue ?? this.venue,
      chiefGuest: chiefGuest ?? this.chiefGuest,
      specialSpeakers: specialSpeakers ?? this.specialSpeakers,
      estimatedBudget: estimatedBudget ?? this.estimatedBudget,
      actualExpense: actualExpense ?? this.actualExpense,
      status: status ?? this.status,
      description: description ?? this.description,
      createdAt: createdAt ?? this.createdAt,
    );
  }

  @override
  List<Object?> get props => [
        id,
        title,
        eventType,
        eventDate,
        venue,
        chiefGuest,
        estimatedBudget,
        status,
      ];
}
