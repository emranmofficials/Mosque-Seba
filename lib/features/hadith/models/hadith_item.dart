/// Sahih Hadith Entry Model
class HadithItem {
  final String id;
  final String? arabicText;
  final String translationBn;
  final String narratorBn;
  final String sourceBookBn;
  final String hadithNumberBn;

  const HadithItem({
    required this.id,
    this.arabicText,
    required this.translationBn,
    required this.narratorBn,
    required this.sourceBookBn,
    required this.hadithNumberBn,
  });
}
