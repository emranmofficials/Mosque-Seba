import '../models/hadith_item.dart';

/// Authentic Bukhari & Muslim Dataset and Daily Rotation Service
class HadithService {
  static const List<HadithItem> authenticHadithDataset = [
    HadithItem(
      id: 'h_01',
      arabicText: 'إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى',
      translationBn:
          'নিশ্চয়ই সকল আমলের ফলাফল নিয়তের ওপর নির্ভরশীল। প্রত্যেক মানুষ তা-ই পাবে যা সে নিয়ত করেছে।',
      narratorBn: 'হযরত উমর ইবনুল খাত্তাব (রা.)',
      sourceBookBn: 'সহিহ বুখারি',
      hadithNumberBn: '১',
    ),
    HadithItem(
      id: 'h_02',
      arabicText: 'لاَ يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ',
      translationBn:
          'তোমাদের কেউ ততক্ষণ পর্যন্ত প্রকৃত মুমিন হতে পারবে না, যতক্ষণ না সে তার ভাইয়ের জন্য তা-ই পছন্দ করে যা সে নিজের জন্য পছন্দ করে।',
      narratorBn: 'হযরত আনাস ইবনে মালিক (রা.)',
      sourceBookBn: 'সহিহ বুখারি ও মুসলিম',
      hadithNumberBn: '১৩',
    ),
    HadithItem(
      id: 'h_03',
      arabicText: 'خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ',
      translationBn:
          'তোমাদের মধ্যে সর্বোত্তম ব্যক্তি সে, যে নিজে পবিত্র কুরআন শিক্ষা করে এবং অপরকে তা শিক্ষা দেয়।',
      narratorBn: 'হযরত উসমান ইবনে আফফান (রা.)',
      sourceBookBn: 'সহিহ বুখারি',
      hadithNumberBn: '৫০২৭',
    ),
    HadithItem(
      id: 'h_04',
      arabicText: 'الطُّهُورُ شَطْرُ الإِيمَانِ',
      translationBn:
          'পবিত্রতা ও পরিচ্ছন্নতা হচ্ছে ঈমানের অর্ধেক অংশ।',
      narratorBn: 'হযরত আবু মালিক আল-আশআরী (রা.)',
      sourceBookBn: 'সহিহ মুসলিম',
      hadithNumberBn: '২২৩',
    ),
    HadithItem(
      id: 'h_05',
      arabicText: 'مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ',
      translationBn:
          'যে ব্যক্তি আল্লাহ ও পরকালের ওপর বিশ্বাস রাখে, সে যেন ভালো ও কল্যাণকর কথা বলে অথবা চুপ থাকে।',
      narratorBn: 'হযরত আবু হুরায়রা (রা.)',
      sourceBookBn: 'সহিহ বুখারি',
      hadithNumberBn: '৬০১৮',
    ),
    HadithItem(
      id: 'h_06',
      arabicText: 'الْمُسْلِمُ أَخُو الْمُسْلِمِ لاَ يَظْلِمُهُ وَلاَ يُسْلِمُهُ',
      translationBn:
          'এক মুসলিম অপর মুসলিমের ভাই। সে তার ওপর জুলুম করে না এবং তাকে ধ্বংস বা বিপদের মুখে ফেলে দেয় না।',
      narratorBn: 'হযরত আবদুল্লাহ ইবনে উমর (রা.)',
      sourceBookBn: 'সহিহ বুখারি ও মুসলিম',
      hadithNumberBn: '২৪৪২',
    ),
  ];

  /// Returns today's automatically rotated Hadith
  static HadithItem getTodayHadith({DateTime? date}) {
    final now = date ?? DateTime.now();
    final dayOfYear = now.difference(DateTime(now.year, 1, 1)).inDays;
    final index = dayOfYear % authenticHadithDataset.length;
    return authenticHadithDataset[index];
  }
}
