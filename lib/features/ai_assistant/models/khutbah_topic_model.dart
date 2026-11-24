import 'package:equatable/equatable.dart';

/// Khutbah Topic & Outline Model (জুমার খুতবা ও বয়ান মডেল)
class KhutbahTopicModel extends Equatable {
  final String id;
  final String title;
  final String category; // 'character', 'halal_earnings', 'parents_rights', 'charity', 'salah', 'taqwa'
  final String quranicAyat;
  final String hadithReference;
  final List<String> mainPoints;
  final String practicalGuidance;

  const KhutbahTopicModel({
    required this.id,
    required this.title,
    required this.category,
    required this.quranicAyat,
    required this.hadithReference,
    required this.mainPoints,
    required this.practicalGuidance,
  });

  @override
  List<Object?> get props => [id, title, category, quranicAyat, hadithReference];
}
