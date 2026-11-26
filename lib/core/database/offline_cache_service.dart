import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:uuid/uuid.dart';
import '../../features/cloud_sync/services/offline_queue_manager.dart';
import '../network/supabase_service.dart';
import 'app_database.dart';

/// Shared offline-first cache & mutation queue bridge backed by Drift SQLite.
class OfflineCacheService {
  static final OfflineCacheService instance = OfflineCacheService();

  static const Uuid _uuid = Uuid();

  AppDatabase? _database;
  final OfflineQueueManager _queueManager;
  final Map<String, Map<String, Map<String, dynamic>>> _memoryFallback = {};

  OfflineCacheService({
    AppDatabase? database,
    OfflineQueueManager? queueManager,
  })  : _database = database,
        _queueManager = queueManager ?? OfflineQueueManager();

  void attachDatabase(AppDatabase database) {
    _database = database;
  }

  void clearMemoryCache() {
    _memoryFallback.clear();
  }

  String generateId() => _uuid.v4();

  /// Normalizes model payloads for Supabase PostgreSQL compatibility
  /// (e.g. converts integer epoch ms timestamps on TIMESTAMPTZ columns to ISO-8601 UTC).
  static Map<String, dynamic> normalizeCloudPayload(Map<String, dynamic> raw) {
    final normalized = Map<String, dynamic>.from(raw);
    for (final key in const ['created_at', 'counted_at']) {
      if (!normalized.containsKey(key)) continue;
      final val = normalized[key];
      if (val is int) {
        if (val <= 0) {
          normalized.remove(key);
        } else {
          normalized[key] =
              DateTime.fromMillisecondsSinceEpoch(val, isUtc: true).toIso8601String();
        }
      } else if (val is String && val.trim().isEmpty) {
        normalized.remove(key);
      }
    }
    return normalized;
  }

  SupabaseClient? get _safeClient {
    if (!SupabaseService.isInitialized) return null;
    try {
      return SupabaseService.client;
    } catch (_) {
      return null;
    }
  }

  Future<void> _saveLocal({
    required String tableName,
    required String mosqueId,
    required Map<String, dynamic> payload,
  }) async {
    final id = (payload['id'] as String?)?.trim() ?? '';
    if (id.isEmpty) return;

    final copy = Map<String, dynamic>.from(payload);
    copy['mosque_id'] ??= mosqueId;
    final tableMap = _memoryFallback.putIfAbsent(tableName, () => {});
    tableMap[id] = copy;

    final db = _database;
    if (db != null) {
      await db.upsertLocalRecord(
        entityTable: tableName,
        recordId: id,
        mosqueId: mosqueId,
        payloadJson: jsonEncode(copy),
      );
    }
  }

  Future<void> _deleteLocal({
    required String tableName,
    required String id,
  }) async {
    _memoryFallback[tableName]?.remove(id);
    final db = _database;
    if (db != null) {
      await db.deleteLocalRecord(entityTable: tableName, recordId: id);
    }
  }

  Future<List<Map<String, dynamic>>> readLocalList({
    required String tableName,
    required String mosqueId,
  }) async {
    final db = _database;
    if (db != null) {
      try {
        final rawList = await db.readLocalPayloads(
          entityTable: tableName,
          mosqueId: mosqueId,
        );
        return rawList
            .map((raw) => jsonDecode(raw) as Map<String, dynamic>)
            .toList();
      } catch (e) {
        debugPrint('Local DB read error for $tableName: $e');
      }
    }

    final tableMap = _memoryFallback[tableName];
    if (tableMap == null) return [];
    return tableMap.values
        .where((row) => (row['mosque_id'] as String?) == mosqueId || mosqueId.isEmpty)
        .map((row) => Map<String, dynamic>.from(row))
        .toList();
  }

  Future<Map<String, dynamic>?> readLocalById({
    required String tableName,
    required String id,
  }) async {
    final db = _database;
    if (db != null) {
      try {
        final raw = await db.readLocalPayloadById(
          entityTable: tableName,
          recordId: id,
        );
        if (raw != null) {
          return jsonDecode(raw) as Map<String, dynamic>;
        }
      } catch (e) {
        debugPrint('Local DB readById error for $tableName/$id: $e');
      }
    }
    final mem = _memoryFallback[tableName]?[id];
    return mem != null ? Map<String, dynamic>.from(mem) : null;
  }

  /// Fetches rows from Supabase and syncs local cache, falling back to SQLite offline.
  Future<List<Map<String, dynamic>>> fetchList({
    required String tableName,
    required String mosqueId,
    String? orderBy,
    bool ascending = false,
    int? limit,
    bool filterByMosqueId = true,
    Map<String, dynamic> equalsFilters = const {},
  }) async {
    final client = _safeClient;
    if (client != null) {
      try {
        dynamic query = client.from(tableName).select();
        if (filterByMosqueId) {
          query = query.eq('mosque_id', mosqueId);
        }
        for (final entry in equalsFilters.entries) {
          query = query.eq(entry.key, entry.value as Object);
        }
        if (orderBy != null) {
          query = query.order(orderBy, ascending: ascending);
        }
        if (limit != null) {
          query = query.limit(limit);
        }

        final dynamic response = await query;
        final serverRows = (response as List<dynamic>)
            .map((e) => Map<String, dynamic>.from(e as Map))
            .toList();

        // Preserve any locally created/updated/deleted rows not yet synced
        final pendingQueue = await _queueManager.getPendingQueue();
        final tableOutbox =
            pendingQueue.where((item) => item.tableName == tableName).toList();
        final preserveIds = <String>{};
        final deletedIds = <String>{};
        final pendingOverlays = <String, Map<String, dynamic>>{};

        for (final item in tableOutbox) {
          final id = item.payload['id']?.toString() ?? '';
          if (id.isEmpty) continue;
          preserveIds.add(id);
          if (item.operation == 'delete') {
            deletedIds.add(id);
          } else {
            pendingOverlays[id] = Map<String, dynamic>.from(item.payload);
          }
        }

        if (equalsFilters.isEmpty && limit == null) {
          final cacheMap = <String, String>{};
          for (final row in serverRows) {
            final id = row['id']?.toString() ?? '';
            if (id.isEmpty || deletedIds.contains(id)) continue;
            cacheMap[id] = jsonEncode(row);
          }
          final db = _database;
          if (db != null) {
            await db.replaceLocalTableCache(
              entityTable: tableName,
              mosqueId: mosqueId,
              recordPayloads: cacheMap,
              preserveRecordIds: preserveIds,
            );
          } else {
            for (final row in serverRows) {
              await _saveLocal(tableName: tableName, mosqueId: mosqueId, payload: row);
            }
          }
        } else {
          for (final row in serverRows) {
            final id = row['id']?.toString() ?? '';
            if (id.isNotEmpty && !preserveIds.contains(id)) {
              await _saveLocal(tableName: tableName, mosqueId: mosqueId, payload: row);
            }
          }
        }

        if (tableOutbox.isEmpty) {
          return serverRows;
        }

        // Merge server rows with pending un-synced outbox edits/inserts
        final mergedById = <String, Map<String, dynamic>>{};
        for (final row in serverRows) {
          final id = row['id']?.toString() ?? '';
          if (id.isNotEmpty && !deletedIds.contains(id)) {
            mergedById[id] = row;
          }
        }
        for (final entry in pendingOverlays.entries) {
          final existing = mergedById[entry.key];
          mergedById[entry.key] = existing != null
              ? {...existing, ...entry.value}
              : entry.value;
        }
        return mergedById.values.toList();
      } catch (e) {
        debugPrint('Supabase fetch failed for $tableName, reading SQLite cache: $e');
      }
    }

    var localRows = await readLocalList(tableName: tableName, mosqueId: mosqueId);
    if (equalsFilters.isNotEmpty) {
      localRows = localRows.where((row) {
        for (final entry in equalsFilters.entries) {
          if (row[entry.key] != entry.value) return false;
        }
        return true;
      }).toList();
    }
    return localRows;
  }

  /// Inserts a record locally first, then pushes online or queues in outbox.
  Future<Map<String, dynamic>> insertRecord({
    required String tableName,
    required String mosqueId,
    required Map<String, dynamic> payload,
    bool upsert = false,
    bool attachMosqueId = true,
  }) async {
    final serverPayload = normalizeCloudPayload(payload);
    final rawId = serverPayload['id']?.toString().trim() ?? '';
    final recordId = rawId.isEmpty ? generateId() : rawId;
    serverPayload['id'] = recordId;
    if (attachMosqueId) {
      serverPayload['mosque_id'] = mosqueId;
    }

    final localPayload = Map<String, dynamic>.from(serverPayload);
    localPayload['created_at'] ??= DateTime.now().toUtc().toIso8601String();

    await _saveLocal(
      tableName: tableName,
      mosqueId: mosqueId,
      payload: localPayload,
    );

    final client = _safeClient;
    if (client != null) {
      try {
        final dynamic res = upsert
            ? await client.from(tableName).upsert(serverPayload).select().single()
            : await client.from(tableName).insert(serverPayload).select().single();
        final serverRow = Map<String, dynamic>.from(res as Map);
        await _saveLocal(
          tableName: tableName,
          mosqueId: mosqueId,
          payload: serverRow,
        );
        return serverRow;
      } catch (e) {
        debugPrint('Supabase insert failed for $tableName, queueing outbox: $e');
      }
    }

    await _queueManager.enqueue(
      tableName: tableName,
      operation: 'insert',
      payload: serverPayload,
    );
    return localPayload;
  }

  /// Upserts a batch of records locally first, then pushes online or queues individually.
  Future<bool> upsertBatch({
    required String tableName,
    required String mosqueId,
    required List<Map<String, dynamic>> records,
  }) async {
    if (records.isEmpty) return true;

    final prepared = <Map<String, dynamic>>[];
    for (final raw in records) {
      final serverMap = normalizeCloudPayload(raw);
      final rawId = serverMap['id']?.toString().trim() ?? '';
      serverMap['id'] = rawId.isEmpty ? generateId() : rawId;
      serverMap['mosque_id'] = mosqueId;
      prepared.add(serverMap);

      final localMap = Map<String, dynamic>.from(serverMap);
      localMap['created_at'] ??= DateTime.now().toUtc().toIso8601String();
      await _saveLocal(
        tableName: tableName,
        mosqueId: mosqueId,
        payload: localMap,
      );
    }

    final client = _safeClient;
    if (client != null) {
      try {
        await client.from(tableName).upsert(prepared);
        return true;
      } catch (e) {
        debugPrint('Supabase batch upsert failed for $tableName, queueing: $e');
      }
    }

    for (final item in prepared) {
      await _queueManager.enqueue(
        tableName: tableName,
        operation: 'insert',
        payload: item,
      );
    }
    return true;
  }

  /// Updates a record in SQLite first, then pushes online or queues in outbox.
  Future<bool> updateRecord({
    required String tableName,
    required String id,
    required Map<String, dynamic> updates,
    String? mosqueId,
  }) async {
    final cleanUpdates = normalizeCloudPayload(updates)..remove('id');
    final existing = await readLocalById(tableName: tableName, id: id);
    final effectiveMosqueId =
        mosqueId ?? existing?['mosque_id']?.toString() ?? '';
    final merged = {
      if (existing != null) ...existing,
      ...cleanUpdates,
      'id': id,
      if (effectiveMosqueId.isNotEmpty) 'mosque_id': effectiveMosqueId,
    };

    if (effectiveMosqueId.isNotEmpty) {
      await _saveLocal(
        tableName: tableName,
        mosqueId: effectiveMosqueId,
        payload: merged,
      );
    }

    final client = _safeClient;
    if (client != null) {
      try {
        await client.from(tableName).update(cleanUpdates).eq('id', id);
        return true;
      } catch (e) {
        debugPrint('Supabase update failed for $tableName/$id, queueing outbox: $e');
      }
    }

    await _queueManager.enqueue(
      tableName: tableName,
      operation: 'update',
      payload: {'id': id, ...cleanUpdates},
    );
    return true;
  }

  /// Deletes a record from SQLite first, then pushes online or queues in outbox.
  Future<bool> deleteRecord({
    required String tableName,
    required String id,
  }) async {
    await _deleteLocal(tableName: tableName, id: id);

    final client = _safeClient;
    if (client != null) {
      try {
        await client.from(tableName).delete().eq('id', id);
        return true;
      } catch (e) {
        debugPrint('Supabase delete failed for $tableName/$id, queueing outbox: $e');
      }
    }

    await _queueManager.enqueue(
      tableName: tableName,
      operation: 'delete',
      payload: {'id': id},
    );
    return true;
  }

  /// Reads cached row counts grouped by table for diagnostics UI.
  Future<Map<String, int>> countCachedByTable(String mosqueId) async {
    final db = _database;
    if (db != null) {
      return db.countLocalRecordsByTable(mosqueId);
    }
    final result = <String, int>{};
    for (final entry in _memoryFallback.entries) {
      final count = entry.value.values
          .where((r) => (r['mosque_id']?.toString() ?? '') == mosqueId)
          .length;
      if (count > 0) result[entry.key] = count;
    }
    return result;
  }
}
