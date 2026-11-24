import 'package:equatable/equatable.dart';

/// Chanda / Subscription Payment Model (চাঁদা পরিশোধ মডেল)
class ChandaPaymentModel extends Equatable {
  final String id;
  final String memberId;
  final String memberNameBn;
  final String month; // 'জানুয়ারি ২০২৬' or '2026-01'
  final double amountDue;
  final double amountPaid;
  final String method; // 'cash', 'bkash', 'nagad', 'bank'
  final String status; // 'paid', 'partial', 'due'
  final String receiptCode; // 'RCP-2026-0001'
  final String qrVerificationCode;
  final String receivedBy;
  final String paidAt;
  final int createdAt;

  const ChandaPaymentModel({
    required this.id,
    required this.memberId,
    required this.memberNameBn,
    required this.month,
    this.amountDue = 0.0,
    required this.amountPaid,
    this.method = 'cash',
    this.status = 'paid',
    required this.receiptCode,
    this.qrVerificationCode = '',
    required this.receivedBy,
    required this.paidAt,
    required this.createdAt,
  });

  factory ChandaPaymentModel.fromJson(Map<String, dynamic> json) {
    return ChandaPaymentModel(
      id: json['id'] as String? ?? '',
      memberId: json['member_id'] as String? ?? '',
      memberNameBn: json['member_name_bn'] as String? ?? '',
      month: json['month'] as String? ?? '',
      amountDue: (json['amount_due'] as num?)?.toDouble() ?? 0.0,
      amountPaid: (json['amount_paid'] as num?)?.toDouble() ?? 0.0,
      method: json['method'] as String? ?? 'cash',
      status: json['status'] as String? ?? 'paid',
      receiptCode: json['receipt_code'] as String? ?? '',
      qrVerificationCode: json['qr_verification_code'] as String? ?? '',
      receivedBy: json['received_by'] as String? ?? '',
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
      'member_id': memberId,
      'member_name_bn': memberNameBn,
      'month': month,
      'amount_due': amountDue,
      'amount_paid': amountPaid,
      'method': method,
      'status': status,
      'receipt_code': receiptCode,
      'qr_verification_code': qrVerificationCode,
      'received_by': receivedBy,
      'paid_at': paidAt,
    };
  }

  @override
  List<Object?> get props => [
        id,
        memberId,
        month,
        amountPaid,
        receiptCode,
        status,
      ];
}
