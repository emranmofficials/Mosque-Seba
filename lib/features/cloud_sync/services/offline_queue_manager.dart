import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';

class OutboxItem {
  final String id;
  final String tableName;
  final String operation; // 'insert', 'update', 'delete'
  final Map<String, dynamic> payload;
  final int timestamp;
  final int retryCount;

  const OutboxItem({
    required this.id,
    required this.tableName,
    required this.operation,
    required this.payload,
    required this.timestamp,
    this.retryCount = 0,
  });

  Map<String, dynamic> toJson() => {
        'id': id,
        'table_name': tableName,
        'operation': operation,
        'payload': payload,
        'timestamp': timestamp,
        'retry_count': retryCount,
      };

  OutboxItem copyWith({int? retryCount}) => OutboxItem(
        id: id,
        tableName: tableName,
        operation: operation,
        payload: payload,
        timestamp: timestamp,
        retryCount: retryCount ?? this.retryCount,
      );

  factory OutboxItem.fromJson(Map<String, dynamic> json) => OutboxItem(
        id: json['id'] as String? ?? '',
        tableName: json['table_name'] as String? ?? '',
        operation: json['operation'] as String? ?? 'insert',
        payload: (json['payload'] as Map<String, dynamic>?) ?? {},
        timestamp: json['timestamp'] as int? ?? 0,
        retryCount: json['retry_count'] as int? ?? 0,
      );
}

/// Offline Mutation Outbox Queue Manager
class OfflineQueueManager {
  static const String _outboxKey = 'mosque_seba_offline_outbox';

  Future<List<OutboxItem>> getPendingQueue() async {
    final prefs = await SharedPreferences.getInstance();
    final list = prefs.getStringList(_outboxKey) ?? [];
    return list.map((itemStr) {
      final jsonMap = jsonDecode(itemStr) as Map<String, dynamic>;
      return OutboxItem.fromJson(jsonMap);
    }).toList();
  }

  Future<void> enqueue({
    required String tableName,
    required String operation,
    required Map<String, dynamic> payload,
  }) async {
    final prefs = await SharedPreferences.getInstance();
    final queue = await getPendingQueue();
    final newItem = OutboxItem(
      id: 'outbox-${DateTime.now().millisecondsSinceEpoch}',
      tableName: tableName,
      operation: operation,
      payload: payload,
      timestamp: DateTime.now().millisecondsSinceEpoch,
    );
    queue.add(newItem);

    final encodedList = queue.map((i) => jsonEncode(i.toJson())).toList();
    await prefs.setStringList(_outboxKey, encodedList);
  }

  Future<void> replaceQueue(List<OutboxItem> items) async {
    final prefs = await SharedPreferences.getInstance();
    if (items.isEmpty) {
      await prefs.remove(_outboxKey);
      return;
    }
    final encodedList = items.map((i) => jsonEncode(i.toJson())).toList();
    await prefs.setStringList(_outboxKey, encodedList);
  }

  Future<void> clearQueue() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_outboxKey);
  }
}
