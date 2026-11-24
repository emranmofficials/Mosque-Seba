import 'package:equatable/equatable.dart';

/// Finance Transaction / General Ledger Model (সাধারণ খতিয়ান লেনদেন মডেল)
class FinanceTransactionModel extends Equatable {
  final String id;
  final String transactionCode;
  final String categoryNameBn;
  final String type; // 'income' or 'expense'
  final double amount;
  final String txnDate; // YYYY-MM-DD
  final String description;
  final String sourceType; // 'manual', 'chanda_payment', 'jummah_collection', 'payroll'
  final String? sourceId;
  final String createdBy;
  final String? approvedBy;
  final String approvalStatus; // 'pending', 'approved', 'rejected'
  final bool isReversal;
  final String? attachmentUrl;
  final int createdAt;

  const FinanceTransactionModel({
    required this.id,
    required this.transactionCode,
    required this.categoryNameBn,
    required this.type,
    required this.amount,
    required this.txnDate,
    this.description = '',
    this.sourceType = 'manual',
    this.sourceId,
    required this.createdBy,
    this.approvedBy,
    this.approvalStatus = 'approved',
    this.isReversal = false,
    this.attachmentUrl,
    required this.createdAt,
  });

  bool get isIncome => type.toLowerCase() == 'income';
  bool get isExpense => type.toLowerCase() == 'expense';
  bool get isPending => approvalStatus == 'pending';
  bool get isApproved => approvalStatus == 'approved';

  factory FinanceTransactionModel.fromJson(Map<String, dynamic> json) {
    return FinanceTransactionModel(
      id: json['id'] as String? ?? '',
      transactionCode: json['transaction_code'] as String? ?? '',
      categoryNameBn: json['category_name_bn'] as String? ?? 'বিবিধ',
      type: json['type'] as String? ?? 'expense',
      amount: (json['amount'] as num?)?.toDouble() ?? 0.0,
      txnDate: json['txn_date'] as String? ?? '',
      description: json['description'] as String? ?? '',
      sourceType: json['source_type'] as String? ?? 'manual',
      sourceId: json['source_id'] as String?,
      createdBy: json['created_by'] as String? ?? '',
      approvedBy: json['approved_by'] as String?,
      approvalStatus: json['approval_status'] as String? ?? 'approved',
      isReversal: json['is_reversal'] as bool? ?? false,
      attachmentUrl: json['attachment_url'] as String?,
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
      'transaction_code': transactionCode,
      'category_name_bn': categoryNameBn,
      'type': type,
      'amount': amount,
      'txn_date': txnDate,
      'description': description,
      'source_type': sourceType,
      'source_id': sourceId,
      'created_by': createdBy,
      'approved_by': approvedBy,
      'approval_status': approvalStatus,
      'is_reversal': isReversal,
      'attachment_url': attachmentUrl,
    };
  }

  @override
  List<Object?> get props => [
        id,
        transactionCode,
        categoryNameBn,
        type,
        amount,
        txnDate,
        approvalStatus,
        isReversal,
      ];
}
