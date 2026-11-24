/// Three Ashras of the Blessed Month of Ramadan (RamadanModels.kt Parity)
enum RamadanAshra {
  rahmah(
    titleBn: 'রহমতের দশক',
    subtitleBn: '১ থেকে ১০ রমজান',
    descriptionBn: 'আল্লাহ তাআলার অসীম দয়া, অনুগ্রহ ও রহমত বর্ষণের বরকতময় দিনসমূহ।',
    duaArabic: 'رَبِّ اغْفِرْ وَارْحَمْ وَأَنْتَ خَيْرُ الرَّاحِمِينَ',
    duaTransliteration: 'রব্বিগফির ওয়ারহাম ওয়া আনতা খইরুর রহিমীন।',
    duaTranslation: 'হে আমার পালনকর্তা! আমাকে ক্ষমা করুন ও আমার ওপর রহম করুন; আর আপনিই সর্বশ্রেষ্ঠ দয়ালু। (সূরা আল-মুমিনূন: ১১৮)',
  ),
  maghfirah(
    titleBn: 'মাগফিরাতের দশক',
    subtitleBn: '১১ থেকে ২০ রমজান',
    descriptionBn: 'অতীত জীবনের সকল গুনাহখাতা মাফ ও আল্লাহর কাছে ক্ষমা প্রার্থনার দশক।',
    duaArabic: 'أَسْتَغْفِرُ اللهَ رَبِّي مِنْ كُلِّ ذَنْبٍ وَأَتُوبُ إِلَيْهِ',
    duaTransliteration: 'আস্তাগফিরুল্লাহা রব্বী মিন কুল্লি যাম্বিওঁ ওয়া আতূবু ইলাইহি।',
    duaTranslation: 'আমি আল্লাহর নিকট ক্ষমা প্রার্থনা করছি যিনি আমার রব, এবং তাঁরই কাছে তওবা করে ফিরে আসছি।',
  ),
  najat(
    titleBn: 'নাজাতের দশক',
    subtitleBn: '২১ থেকে ৩০ রমজান',
    descriptionBn: 'জাহান্নামের আগুন থেকে চূড়ান্ত মুক্তি, ইতিকাফ ও পবিত্র শবে কদরের মহিমান্বিত দশক।',
    duaArabic: 'اللَّهُمَّ أَجِرْنَا مِنَ النَّارِ ، اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي',
    duaTransliteration: 'আল্লাহুম্মা আজিরনা মিনান নার। আল্লাহুম্মা ইন্নাকা আফুউন তুহিব্বুল আফওয়া ফা\'ফু আন্নী।',
    duaTranslation: 'হে আল্লাহ! আমাদের জাহান্নামের আগুন থেকে মুক্তি দিন। হে আল্লাহ! আপনি ক্ষমাশীল, ক্ষমা করা পছন্দ করেন, অতএব আমাকে ক্ষমা করে দিন।',
  );

  final String titleBn;
  final String subtitleBn;
  final String descriptionBn;
  final String duaArabic;
  final String duaTransliteration;
  final String duaTranslation;

  const RamadanAshra({
    required this.titleBn,
    required this.subtitleBn,
    required this.descriptionBn,
    required this.duaArabic,
    required this.duaTransliteration,
    required this.duaTranslation,
  });

  static RamadanAshra fromDay(int day) {
    if (day <= 10) return rahmah;
    if (day <= 20) return maghfirah;
    return najat;
  }
}
