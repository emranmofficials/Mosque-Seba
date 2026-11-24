import 'package:equatable/equatable.dart';

/// Meal Schedule Item Model for staff food rotation (ইমাম-মুয়াজ্জিনদের খাবার সূচি মডেল)
class MealScheduleItem extends Equatable {
  final String id;
  final String dateStr; // YYYY-MM-DD
  final String mealType; // 'দুপুর' (Lunch), 'রাত' (Dinner)
  final String providerName;
  final String providerMobile;
  final String receiverStaffName;
  final String status; // 'scheduled', 'delivered', 'missed', 'cancelled'
  final DateTime? createdAt;

  const MealScheduleItem({
    required this.id,
    required this.dateStr,
    required this.mealType,
    required this.providerName,
    required this.providerMobile,
    required this.receiverStaffName,
    this.status = 'scheduled',
    this.createdAt,
  });

  bool get isDelivered => status == 'delivered';

  factory MealScheduleItem.fromJson(Map<String, dynamic> json) {
    return MealScheduleItem(
      id: json['id'] as String? ?? '',
      dateStr: json['date_str'] as String? ?? '',
      mealType: json['meal_type'] as String? ?? 'দুপুর',
      providerName: json['provider_name'] as String? ?? '',
      providerMobile: json['provider_mobile'] as String? ?? '',
      receiverStaffName: json['receiver_staff_name'] as String? ?? '',
      status: json['status'] as String? ?? 'scheduled',
      createdAt: json['created_at'] != null ? DateTime.tryParse(json['created_at'].toString()) : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'date_str': dateStr,
      'meal_type': mealType,
      'provider_name': providerName,
      'provider_mobile': providerMobile,
      'receiver_staff_name': receiverStaffName,
      'status': status,
    };
  }

  MealScheduleItem copyWith({
    String? id,
    String? dateStr,
    String? mealType,
    String? providerName,
    String? providerMobile,
    String? receiverStaffName,
    String? status,
    DateTime? createdAt,
  }) {
    return MealScheduleItem(
      id: id ?? this.id,
      dateStr: dateStr ?? this.dateStr,
      mealType: mealType ?? this.mealType,
      providerName: providerName ?? this.providerName,
      providerMobile: providerMobile ?? this.providerMobile,
      receiverStaffName: receiverStaffName ?? this.receiverStaffName,
      status: status ?? this.status,
      createdAt: createdAt ?? this.createdAt,
    );
  }

  @override
  List<Object?> get props => [
        id,
        dateStr,
        mealType,
        providerName,
        status,
      ];
}
