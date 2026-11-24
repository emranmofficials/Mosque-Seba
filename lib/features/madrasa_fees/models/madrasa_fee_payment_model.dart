import 'package:equatable/equatable.dart';

/// Madrasa Student Fee Payment Model (মাদরাসা ফি আদায়ের রশিদ মডেল)
class MadrasaFeePaymentModel extends Equatable {
  final String id;
  final String studentId;
  final String studentNameBn;
  final String className;
  final String month; // যেমন: ২০২৬-১১
  final double amountDue;
  final double amountPaid;
  final double discountAmount;
  final String paymentMethod; // 'cash', 'bkash', 'nagad', 'bank'
  final String receiptCode; // e.g. MFR-2026-1101
  final String status; // 'paid', 'partial', 'due'
  final String receivedBy;
  final String paidAt; // YYYY-MM-DD
  final int createdAt;

  const MadrasaFeePaymentModel({
    required this.id,
    required this.studentId,
    required this.studentNameBn,
    required this.className,
    required this.month,
    this.amountDue = 0.0,
    required this.amountPaid,
    this.discountAmount = 0.0,
    this.paymentMethod = 'cash',
    required this.receiptCode,
    this.status = 'paid',
    required this.receivedBy,
    required this.paidAt,
    required this.createdAt,
  });

  bool get isFullyPaid => status == 'paid';

  factory MadrasaFeePaymentModel.fromJson(Map<String, dynamic> json) {
    return MadrasaFeePaymentModel(
      id: json['id'] as String? ?? '',
      studentId: json['student_id'] as String? ?? '',
      studentNameBn: json['student_name_bn'] as String? ?? '',
      className: json['class_name'] as String? ?? '',
      month: json['month'] as String? ?? '',
      amountDue: (json['amount_due'] as num?)?.toDouble() ?? 0.0,
      amountPaid: (json['amount_paid'] as num?)?.toDouble() ?? 0.0,
      discountAmount: (json['discount_amount'] as num?)?.toDouble() ?? 0.0,
      paymentMethod: json['payment_method'] as String? ?? 'cash',
      receiptCode: json['receipt_code'] as String? ?? '',
      status: json['status'] as String? ?? 'paid',
      receivedBy: json['received_by'] as String? ?? 'কোষাধ্যক্ষ',
      paidAt: json['paid_at'] as String? ?? '',
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
      'student_id': studentId,
      'student_name_bn': studentNameBn,
      'class_name': className,
      'month': month,
      'amount_due': amountDue,
      'amount_paid': amountPaid,
      'discount_amount': discountAmount,
      'payment_method': paymentMethod,
      'receipt_code': receiptCode,
      'status': status,
      'received_by': receivedBy,
      'paid_at': paidAt,
      'created_at': createdAt,
    };
  }

  @override
  List<Object?> get props => [
        id,
        studentId,
        studentNameBn,
        month,
        amountPaid,
        receiptCode,
        status,
      ];
}
