import 'package:equatable/equatable.dart';

/// Duty Roster Item Model for daily waqt assignments (ওয়াক্তভিত্তিক দায়িত্ব রোস্টার মডেল)
class DutyRosterItem extends Equatable {
  final String id;
  final String staffId;
  final String staffNameBn;
  final String dayOfWeek; // 'শনিবার', 'রবিবার', 'সোমবার', 'মঙ্গলবার', 'বুধবার', 'বৃহস্পতিবার', 'শুক্রবার'
  final String prayerName; // 'ফজর', 'যোহর', 'আসর', 'মাগরিব', 'ইশা', 'জুমা'
  final String roleTitle; // 'ইমামতি', 'আযান ও একামত', 'মসজিদ রক্ষণাবেক্ষণ'
  final String notes;
  final DateTime? createdAt;

  const DutyRosterItem({
    required this.id,
    required this.staffId,
    required this.staffNameBn,
    required this.dayOfWeek,
    required this.prayerName,
    required this.roleTitle,
    this.notes = '',
    this.createdAt,
  });

  factory DutyRosterItem.fromJson(Map<String, dynamic> json) {
    return DutyRosterItem(
      id: json['id'] as String? ?? '',
      staffId: json['staff_id'] as String? ?? '',
      staffNameBn: json['staff_name_bn'] as String? ?? '',
      dayOfWeek: json['day_of_week'] as String? ?? 'শুক্রবার',
      prayerName: json['prayer_name'] as String? ?? 'ফজর',
      roleTitle: json['role_title'] as String? ?? 'ইমামতি',
      notes: json['notes'] as String? ?? '',
      createdAt: json['created_at'] != null ? DateTime.tryParse(json['created_at'].toString()) : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'staff_id': staffId,
      'staff_name_bn': staffNameBn,
      'day_of_week': dayOfWeek,
      'prayer_name': prayerName,
      'role_title': roleTitle,
      'notes': notes,
    };
  }

  DutyRosterItem copyWith({
    String? id,
    String? staffId,
    String? staffNameBn,
    String? dayOfWeek,
    String? prayerName,
    String? roleTitle,
    String? notes,
    DateTime? createdAt,
  }) {
    return DutyRosterItem(
      id: id ?? this.id,
      staffId: staffId ?? this.staffId,
      staffNameBn: staffNameBn ?? this.staffNameBn,
      dayOfWeek: dayOfWeek ?? this.dayOfWeek,
      prayerName: prayerName ?? this.prayerName,
      roleTitle: roleTitle ?? this.roleTitle,
      notes: notes ?? this.notes,
      createdAt: createdAt ?? this.createdAt,
    );
  }

  @override
  List<Object?> get props => [
        id,
        staffId,
        dayOfWeek,
        prayerName,
        roleTitle,
      ];
}
