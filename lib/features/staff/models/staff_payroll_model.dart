import 'package:equatable/equatable.dart';

/// Staff Payroll / Salary Payment Model (স্টাফ বেতনভাতা পরিশোধ মডেল)
class StaffPayrollModel extends Equatable {
  final String id;
  final String staffId;
  final String staffNameBn;
  final String month; // e.g. "মার্চ ২০২৬"
  final double salaryAmount;
  final double bonusAmount;
  final double netSalary;
  final String paymentStatus; // 'pending', 'paid'
  final String? paidDate;
  final int createdAt;

  const StaffPayrollModel({
    required this.id,
    required this.staffId,
    required this.staffNameBn,
    required this.month,
    required this.salaryAmount,
    this.bonusAmount = 0.0,
    required this.netSalary,
    this.paymentStatus = 'pending',
    this.paidDate,
    required this.createdAt,
  });

  bool get isPaid => paymentStatus == 'paid';

  factory StaffPayrollModel.fromJson(Map<String, dynamic> json) {
    return StaffPayrollModel(
      id: json['id'] as String? ?? '',
      staffId: json['staff_id'] as String? ?? '',
      staffNameBn: json['staff_name_bn'] as String? ?? '',
      month: json['month'] as String? ?? '',
      salaryAmount: (json['salary_amount'] as num?)?.toDouble() ?? 0.0,
      bonusAmount: (json['bonus_amount'] as num?)?.toDouble() ?? 0.0,
      netSalary: (json['net_salary'] as num?)?.toDouble() ?? 0.0,
      paymentStatus: json['payment_status'] as String? ?? 'pending',
      paidDate: json['paid_date'] as String?,
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
      'staff_id': staffId,
      'staff_name_bn': staffNameBn,
      'month': month,
      'salary_amount': salaryAmount,
      'bonus_amount': bonusAmount,
      'net_salary': netSalary,
      'payment_status': paymentStatus,
      'paid_date': paidDate,
    };
  }

  @override
  List<Object?> get props => [
        id,
        staffId,
        month,
        netSalary,
        paymentStatus,
        paidDate,
      ];
}
