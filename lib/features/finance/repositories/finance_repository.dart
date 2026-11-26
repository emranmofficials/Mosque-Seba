import '../../../core/database/offline_cache_service.dart';
import '../models/finance_transaction_model.dart';

/// Financial Summary metrics (মোট হিসাবের সারাংশ)
class FinancialSummary {
  final double totalIncome;
  final double totalExpense;
  final double netBalance;
  final double cashInHand;
  final double bankBalance;
  final int pendingVouchersCount;

  const FinancialSummary({
    required this.totalIncome,
    required this.totalExpense,
    required this.netBalance,
    required this.cashInHand,
    required this.bankBalance,
    required this.pendingVouchersCount,
  });

  factory FinancialSummary.empty() => const FinancialSummary(
        totalIncome: 0.0,
        totalExpense: 0.0,
        netBalance: 0.0,
        cashInHand: 0.0,
        bankBalance: 0.0,
        pendingVouchersCount: 0,
      );
}

/// Repository for Financial Ledger and Approvals (আর্থিক খতিয়ান ও অনুমোদন রিপোজিটরি)
class FinanceRepository {
  final OfflineCacheService _cache;

  FinanceRepository({OfflineCacheService? cache})
      : _cache = cache ?? OfflineCacheService.instance;

  /// Fetch all transactions for a mosque (Supabase online with Drift SQLite fallback)
  Future<List<FinanceTransactionModel>> fetchTransactions({
    required String mosqueId,
    String? typeFilter,
    String? statusFilter,
  }) async {
    final equals = <String, dynamic>{};
    if (typeFilter != null && typeFilter.isNotEmpty && typeFilter != 'all') {
      equals['type'] = typeFilter.toLowerCase();
    }
    if (statusFilter != null && statusFilter.isNotEmpty && statusFilter != 'all') {
      equals['approval_status'] = statusFilter.toLowerCase();
    }

    final rows = await _cache.fetchList(
      tableName: 'transactions',
      mosqueId: mosqueId,
      orderBy: 'created_at',
      ascending: false,
      equalsFilters: equals,
    );

    final list = rows.map(FinanceTransactionModel.fromJson).toList();
    list.sort((a, b) => b.createdAt.compareTo(a.createdAt));
    return list;
  }

  /// Calculate live summary metrics directly from approved ledger entries
  FinancialSummary calculateSummary(List<FinanceTransactionModel> transactions) {
    double income = 0.0;
    double expense = 0.0;
    double bankNet = 0.0;
    double cashNet = 0.0;
    int pendingCount = 0;

    for (final txn in transactions) {
      if (txn.isPending) {
        pendingCount++;
        continue;
      }
      if (txn.approvalStatus == 'rejected' || txn.isReversal) continue;

      final descLower = txn.description.toLowerCase();
      final catLower = txn.categoryNameBn.toLowerCase();
      final isBankTxn = catLower.contains('ব্যাংক') ||
          catLower.contains('bank') ||
          descLower.contains('ব্যাংক') ||
          descLower.contains('bank') ||
          descLower.contains('বিকাশ') ||
          descLower.contains('bkash') ||
          descLower.contains('nagad') ||
          descLower.contains('নগদ ওয়ালেট') ||
          descLower.contains('রকেট') ||
          descLower.contains('চেক');

      if (txn.isIncome) {
        income += txn.amount;
        if (isBankTxn) {
          bankNet += txn.amount;
        } else {
          cashNet += txn.amount;
        }
      } else {
        expense += txn.amount;
        if (isBankTxn) {
          bankNet -= txn.amount;
        } else {
          cashNet -= txn.amount;
        }
      }
    }

    final net = income - expense;

    return FinancialSummary(
      totalIncome: income,
      totalExpense: expense,
      netBalance: net,
      cashInHand: cashNet,
      bankBalance: bankNet,
      pendingVouchersCount: pendingCount,
    );
  }

  /// Submit a new transaction voucher (with multi-tier threshold check)
  Future<FinanceTransactionModel?> submitTransaction({
    required String mosqueId,
    required String categoryNameBn,
    required String type,
    required double amount,
    required String txnDate,
    required String description,
    required String createdBy,
    required String userRole,
    String? sourceId,
    String sourceType = 'manual',
  }) async {
    try {
      final isAutoApproved = amount <= 5000.0 &&
          (userRole == 'treasurer' || userRole == 'president');

      final status = isAutoApproved ? 'approved' : 'pending';
      final approvedBy = isAutoApproved ? createdBy : null;
      final txnCode =
          'TXN-${DateTime.now().year}-${DateTime.now().millisecondsSinceEpoch % 1000000}';

      final payload = {
        'mosque_id': mosqueId,
        'transaction_code': txnCode,
        'category_name_bn': categoryNameBn,
        'type': type.toLowerCase(),
        'amount': amount,
        'txn_date': txnDate,
        'description': description,
        'source_type': sourceType,
        'source_id': sourceId,
        'created_by': createdBy,
        'approved_by': approvedBy,
        'approval_status': status,
        'is_reversal': false,
      };

      final res = await _cache.insertRecord(
        tableName: 'transactions',
        mosqueId: mosqueId,
        payload: payload,
      );
      return FinanceTransactionModel.fromJson(res);
    } catch (_) {
      return null;
    }
  }

  /// Approve or reject transaction voucher with strict Self-Approval Prevention
  Future<bool> reviewTransaction({
    required String transactionId,
    required String reviewerName,
    required String creatorName,
    required bool isApproved,
    String? mosqueId,
  }) async {
    if (reviewerName.trim().toLowerCase() == creatorName.trim().toLowerCase()) {
      throw Exception('অনুমোদন নীতি লঙ্ঘন: ভাউচার তৈরিকারী নিজের ভাউচার অনুমোদন করতে পারবেন না!');
    }

    try {
      return await _cache.updateRecord(
        tableName: 'transactions',
        id: transactionId,
        mosqueId: mosqueId,
        updates: {
          'approval_status': isApproved ? 'approved' : 'rejected',
          'approved_by': reviewerName,
        },
      );
    } catch (_) {
      return false;
    }
  }

  /// Create a cashless reversal transaction preserving historical audit trail
  Future<bool> reverseTransaction({
    required String mosqueId,
    required FinanceTransactionModel originalTxn,
    required String reason,
    required String reversedBy,
  }) async {
    try {
      await _cache.updateRecord(
        tableName: 'transactions',
        id: originalTxn.id,
        mosqueId: mosqueId,
        updates: {'is_reversal': true},
      );

      final reversalType = originalTxn.isIncome ? 'expense' : 'income';
      final payload = {
        'mosque_id': mosqueId,
        'transaction_code': 'REV-${originalTxn.transactionCode}',
        'category_name_bn': 'ভাউচার সমন্বয় / প্রত্যাহার (${originalTxn.categoryNameBn})',
        'type': reversalType,
        'amount': originalTxn.amount,
        'txn_date': DateTime.now().toIso8601String().split('T').first,
        'description': 'ভাউচার #${originalTxn.transactionCode} এর সমন্বয় এন্ট্রি। কারণ: $reason',
        'source_type': 'reversal',
        'source_id': originalTxn.id,
        'created_by': reversedBy,
        'approved_by': reversedBy,
        'approval_status': 'approved',
        'is_reversal': true,
      };

      await _cache.insertRecord(
        tableName: 'transactions',
        mosqueId: mosqueId,
        payload: payload,
      );
      return true;
    } catch (_) {
      return false;
    }
  }
}
