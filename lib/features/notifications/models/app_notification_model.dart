import 'package:equatable/equatable.dart';

/// Notification Center Item Model (অ্যাপ বিজ্ঞপ্তি মডেল)
class AppNotificationModel extends Equatable {
  final String id;
  final String title;
  final String body;
  final String entityType; // 'chanda', 'payroll', 'meeting', 'janaza', 'system'
  final String? entityId;
  final String priority; // 'normal', 'urgent'
  final bool isRead;
  final int createdAt;

  const AppNotificationModel({
    required this.id,
    required this.title,
    required this.body,
    this.entityType = 'system',
    this.entityId,
    this.priority = 'normal',
    this.isRead = false,
    required this.createdAt,
  });

  factory AppNotificationModel.fromJson(Map<String, dynamic> json) {
    return AppNotificationModel(
      id: json['id'] as String? ?? '',
      title: json['title'] as String? ?? '',
      body: json['body'] as String? ?? '',
      entityType: json['entity_type'] as String? ?? 'system',
      entityId: json['entity_id'] as String?,
      priority: json['priority'] as String? ?? 'normal',
      isRead: json['is_read'] as bool? ?? false,
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
      'body': body,
      'entity_type': entityType,
      'entity_id': entityId,
      'priority': priority,
      'is_read': isRead,
    };
  }

  @override
  List<Object?> get props => [id, title, isRead, createdAt];
}
