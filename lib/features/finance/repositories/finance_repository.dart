import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../core/network/supabase_service.dart';
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
  SupabaseClient get _client => SupabaseService.client;

  /// Fetch all transactions for a mosque
  Future<List<FinanceTransactionModel>> fetchTransactions({
    required String mosqueId,
    String? typeFilter,
    String? statusFilter,
  }) async {
    try {
      var query = _client.from('transactions').select().eq('mosque_id', mosqueId);

      if (typeFilter != null && typeFilter.isNotEmpty && typeFilter != 'all') {
        query = query.eq('type', typeFilter.toLowerCase());
      }

      if (statusFilter != null && statusFilter.isNotEmpty && statusFilter != 'all') {
        query = query.eq('approval_status', statusFilter.toLowerCase());
      }

      final response = await query.order('created_at', ascending: false);
      final List<dynamic> data = response as List<dynamic>;

      return data
          .map((item) => FinanceTransactionModel.fromJson(item as Map<String, dynamic>))
          .toList();
    } catch (e) {
      return [];
    }
  }

  /// Calculate live summary metrics
  FinancialSummary calculateSummary(List<FinanceTransactionModel> transactions) {
    double income = 0.0;
    double expense = 0.0;
    int pendingCount = 0;

    for (final txn in transactions) {
      if (txn.isPending) {
        pendingCount++;
        continue;
      }
      if (txn.approvalStatus == 'rejected' || txn.isReversal) continue;

      if (txn.isIncome) {
        income += txn.amount;
      } else {
        expense += txn.amount;
      }
    }

    final net = income - expense;
    // Approximating 65% cash in hand and 35% bank deposit as per legacy distribution
    final cash = net > 0 ? net * 0.65 : 0.0;
    final bank = net > 0 ? net * 0.35 : 0.0;

    return FinancialSummary(
      totalIncome: income,
      totalExpense: expense,
      netBalance: net,
      cashInHand: cash,
      bankBalance: bank,
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
      // Threshold rules:
      // Amount <= 5,000: Auto approved if submitted by Treasurer or President
      // Amount > 5,000: Pending approval
      final isAutoApproved = amount <= 5000.0 &&
          (userRole == 'treasurer' || userRole == 'president');

      final status = isAutoApproved ? 'approved' : 'pending';
      final approvedBy = isAutoApproved ? createdBy : null;

      final txnCode = 'TXN-${DateTime.now().year}-${DateTime.now().millisecondsSinceEpoch % 1000000}';

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

      final res = await _client.from('transactions').insert(payload).select().single();
      return FinanceTransactionModel.fromJson(res);
    } catch (e) {
      return null;
    }
  }

  /// Approve or reject transaction voucher with strict Self-Approval Prevention
  Future<bool> reviewTransaction({
    required String transactionId,
    required String reviewerName,
    required String creatorName,
    required bool isApproved,
  }) async {
    // Non-negotiable invariant: Creator cannot approve their own voucher!
    if (reviewerName.trim().toLowerCase() == creatorName.trim().toLowerCase()) {
      throw Exception('অনুমোদন নীতি লঙ্ঘন: ভাউচার তৈরিকারী নিজের ভাউচার অনুমোদন করতে পারবেন না!');
    }

    try {
      await _client.from('transactions').update({
        'approval_status': isApproved ? 'approved' : 'rejected',
        'approved_by': reviewerName,
      }).eq('id', transactionId);
      return true;
    } catch (e) {
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
      // 1. Mark original as reversed
      await _client
          .from('transactions')
          .update({'is_reversal': true})
          .eq('id', originalTxn.id)
          .eq('mosque_id', mosqueId);

      // 2. Insert offsetting reversal entry
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

      await _client.from('transactions').insert(payload);
      return true;
    } catch (e) {
      return false;
    }
  }
}
