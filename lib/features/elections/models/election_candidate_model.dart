import 'package:equatable/equatable.dart';

/// Election Candidate Model (কমিটি নির্বাচন প্রার্থী মডেল)
class ElectionCandidateModel extends Equatable {
  final String id;
  final String scheduleId;
  final String targetRole; // 'president', 'general_secretary', 'treasurer'
  final String targetRoleBn;
  final String candidateName;
  final String symbolOrPanel; // 'ছাতা', 'বই', 'হারিকেন', 'ঐক্য পরিষদ'
  final String proposerName;
  final String seconderName;
  final int votesReceived;
  final bool isElected;

  const ElectionCandidateModel({
    required this.id,
    required this.scheduleId,
    required this.targetRole,
    required this.targetRoleBn,
    required this.candidateName,
    this.symbolOrPanel = 'সাধারণ',
    this.proposerName = '',
    this.seconderName = '',
    this.votesReceived = 0,
    this.isElected = false,
  });

  factory ElectionCandidateModel.fromJson(Map<String, dynamic> json) {
    final rawScheduleId = json['schedule_id'] as String? ?? '';
    final rawRole = json['target_role'] as String? ?? '';
    final rawRoleBn = json['target_role_bn'] as String? ?? '';
    final rawCand = json['candidate_name'] as String? ?? '';
    final rawProposer = json['proposer_name'] as String? ?? '';
    final rawSeconder = json['seconder_name'] as String? ?? '';

    return ElectionCandidateModel(
      id: json['id'] as String? ?? '',
      scheduleId: rawScheduleId.isNotEmpty
          ? rawScheduleId
          : (json['election_id'] as String? ?? ''),
      targetRole: rawRole.isNotEmpty
          ? rawRole
          : (json['target_role_code'] as String? ?? 'general_member'),
      targetRoleBn: rawRoleBn.isNotEmpty
          ? rawRoleBn
          : (json['target_role_title_bn'] as String? ?? 'সদস্য'),
      candidateName: rawCand.isNotEmpty
          ? rawCand
          : (json['candidate_name_bn'] as String? ?? ''),
      symbolOrPanel: json['symbol_or_panel'] as String? ?? 'সাধারণ',
      proposerName: rawProposer.isNotEmpty
          ? rawProposer
          : (json['proposer_name_bn'] as String? ?? ''),
      seconderName: rawSeconder.isNotEmpty
          ? rawSeconder
          : (json['seconder_name_bn'] as String? ?? ''),
      votesReceived: json['votes_received'] as int? ?? 0,
      isElected: json['is_elected'] as bool? ?? false,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'schedule_id': scheduleId,
      'target_role': targetRole,
      'target_role_bn': targetRoleBn,
      'candidate_name': candidateName,
      'symbol_or_panel': symbolOrPanel,
      'proposer_name': proposerName,
      'seconder_name': seconderName,
      'votes_received': votesReceived,
      'is_elected': isElected,
    };
  }

  ElectionCandidateModel copyWith({
    String? id,
    String? scheduleId,
    String? targetRole,
    String? targetRoleBn,
    String? candidateName,
    String? symbolOrPanel,
    String? proposerName,
    String? seconderName,
    int? votesReceived,
    bool? isElected,
  }) {
    return ElectionCandidateModel(
      id: id ?? this.id,
      scheduleId: scheduleId ?? this.scheduleId,
      targetRole: targetRole ?? this.targetRole,
      targetRoleBn: targetRoleBn ?? this.targetRoleBn,
      candidateName: candidateName ?? this.candidateName,
      symbolOrPanel: symbolOrPanel ?? this.symbolOrPanel,
      proposerName: proposerName ?? this.proposerName,
      seconderName: seconderName ?? this.seconderName,
      votesReceived: votesReceived ?? this.votesReceived,
      isElected: isElected ?? this.isElected,
    );
  }

  @override
  List<Object?> get props => [
        id,
        scheduleId,
        targetRole,
        candidateName,
        votesReceived,
        isElected,
      ];
}
