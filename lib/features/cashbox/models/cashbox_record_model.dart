import 'package:equatable/equatable.dart';

/// Jumu'ah Cashbox Record Model (জুমার দানবাক্স গণনা রেকর্ড)
class CashboxRecordModel extends Equatable {
  final String id;
  final String waqtDate; // YYYY-MM-DD
  final int note1000;
  final int note500;
  final int note200;
  final int note100;
  final int note50;
  final int note20;
  final int note10;
  final int note5;
  final int note2;
  final int note1;
  final double coinsAmount;
  final double totalAmount;
  final String witness1Name;
  final String witness2Name;
  final String witness3Name;
  final String? witness1SignatureUrl;
  final String? witness2SignatureUrl;
  final String? witness3SignatureUrl;
  final String? voucherCode;
  final String notes;
  final int countedAt;

  const CashboxRecordModel({
    required this.id,
    required this.waqtDate,
    this.note1000 = 0,
    this.note500 = 0,
    this.note200 = 0,
    this.note100 = 0,
    this.note50 = 0,
    this.note20 = 0,
    this.note10 = 0,
    this.note5 = 0,
    this.note2 = 0,
    this.note1 = 0,
    this.coinsAmount = 0.0,
    required this.totalAmount,
    required this.witness1Name,
    required this.witness2Name,
    this.witness3Name = '',
    this.witness1SignatureUrl,
    this.witness2SignatureUrl,
    this.witness3SignatureUrl,
    this.voucherCode,
    this.notes = '',
    required this.countedAt,
  });

  factory CashboxRecordModel.fromJson(Map<String, dynamic> json) {
    final rawWaqtDate = json['waqt_date'] as String? ?? '';
    final rawCountedAt = json['counted_at'] ?? json['created_at'];
    return CashboxRecordModel(
      id: json['id'] as String? ?? '',
      waqtDate: rawWaqtDate.isNotEmpty
          ? rawWaqtDate
          : (json['collection_date'] as String? ?? ''),
      note1000: json['note_1000'] as int? ?? 0,
      note500: json['note_500'] as int? ?? 0,
      note200: json['note_200'] as int? ?? 0,
      note100: json['note_100'] as int? ?? 0,
      note50: json['note_50'] as int? ?? 0,
      note20: json['note_20'] as int? ?? 0,
      note10: json['note_10'] as int? ?? 0,
      note5: json['note_5'] as int? ?? 0,
      note2: json['note_2'] as int? ?? 0,
      note1: json['note_1'] as int? ?? 0,
      coinsAmount: (json['coins_amount'] as num?)?.toDouble() ?? 0.0,
      totalAmount: (json['total_amount'] as num?)?.toDouble() ?? 0.0,
      witness1Name: json['witness_1_name'] as String? ?? '',
      witness2Name: json['witness_2_name'] as String? ?? '',
      witness3Name: json['witness_3_name'] as String? ?? '',
      witness1SignatureUrl: json['witness_1_signature_url'] as String?,
      witness2SignatureUrl: json['witness_2_signature_url'] as String?,
      witness3SignatureUrl: json['witness_3_signature_url'] as String?,
      voucherCode: json['voucher_code'] as String?,
      notes: json['notes'] as String? ?? '',
      countedAt: rawCountedAt != null
          ? (rawCountedAt is int
              ? rawCountedAt
              : DateTime.tryParse(rawCountedAt.toString())?.millisecondsSinceEpoch ?? 0)
          : 0,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'waqt_date': waqtDate,
      'note_1000': note1000,
      'note_500': note500,
      'note_200': note200,
      'note_100': note100,
      'note_50': note50,
      'note_20': note20,
      'note_10': note10,
      'note_5': note5,
      'note_2': note2,
      'note_1': note1,
      'coins_amount': coinsAmount,
      'total_amount': totalAmount,
      'witness_1_name': witness1Name,
      'witness_2_name': witness2Name,
      'witness_3_name': witness3Name,
      'witness_1_signature_url': witness1SignatureUrl,
      'witness_2_signature_url': witness2SignatureUrl,
      'witness_3_signature_url': witness3SignatureUrl,
      'voucher_code': voucherCode,
      'notes': notes,
    };
  }

  @override
  List<Object?> get props => [
        id,
        waqtDate,
        totalAmount,
        witness1Name,
        witness2Name,
        voucherCode,
      ];
}
