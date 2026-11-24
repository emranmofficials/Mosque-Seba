import 'package:equatable/equatable.dart';

/// Mosque Official Notice & Announcement Model (মসজিদ নোটিশ ও ঘোষণা মডেল)
class NoticeModel extends Equatable {
  final String id;
  final String title;
  final String content;
  final String priority; // 'normal', 'important', 'urgent'
  final bool isPinned;
  final String publishDate;
  final String? expiryDate;
  final String status; // 'published', 'draft', 'archived'
  final String createdBy;
  final String? imageUrl;
  final int createdAt;

  const NoticeModel({
    required this.id,
    required this.title,
    required this.content,
    this.priority = 'normal',
    this.isPinned = false,
    required this.publishDate,
    this.expiryDate,
    this.status = 'published',
    required this.createdBy,
    this.imageUrl,
    required this.createdAt,
  });

  bool get isUrgent => priority == 'urgent';
  bool get isImportant => priority == 'important';

  factory NoticeModel.fromJson(Map<String, dynamic> json) {
    return NoticeModel(
      id: json['id'] as String? ?? '',
      title: json['title'] as String? ?? '',
      content: json['content'] as String? ?? '',
      priority: json['priority'] as String? ?? 'normal',
      isPinned: json['is_pinned'] as bool? ?? false,
      publishDate: json['publish_date'] as String? ?? '',
      expiryDate: json['expiry_date'] as String?,
      status: json['status'] as String? ?? 'published',
      createdBy: json['created_by'] as String? ?? '',
      imageUrl: json['image_url'] as String?,
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
      'content': content,
      'priority': priority,
      'is_pinned': isPinned,
      'publish_date': publishDate,
      'expiry_date': expiryDate,
      'status': status,
      'created_by': createdBy,
      'image_url': imageUrl,
    };
  }

  NoticeModel copyWith({
    String? id,
    String? title,
    String? content,
    String? priority,
    bool? isPinned,
    String? publishDate,
    String? expiryDate,
    String? status,
    String? createdBy,
    String? imageUrl,
    int? createdAt,
  }) {
    return NoticeModel(
      id: id ?? this.id,
      title: title ?? this.title,
      content: content ?? this.content,
      priority: priority ?? this.priority,
      isPinned: isPinned ?? this.isPinned,
      publishDate: publishDate ?? this.publishDate,
      expiryDate: expiryDate ?? this.expiryDate,
      status: status ?? this.status,
      createdBy: createdBy ?? this.createdBy,
      imageUrl: imageUrl ?? this.imageUrl,
      createdAt: createdAt ?? this.createdAt,
    );
  }

  @override
  List<Object?> get props => [
        id,
        title,
        priority,
        isPinned,
        publishDate,
        status,
      ];
}
