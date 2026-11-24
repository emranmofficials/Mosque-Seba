import '../models/poster_design_model.dart';

/// Service for generating authentic Bengali Islamic Poster Copy with Gemini AI
class PosterAiService {
  /// Generate or optimize authentic Islamic Dawat poster copy
  Future<PosterDesignModel> generatePosterCopy({
    required String prompt,
    String? eventType,
    String? venue,
    String? date,
  }) async {
    // Simulating prompt-based AI synthesis using Gemini 3.8 Flash template
    await Future.delayed(const Duration(milliseconds: 600));

    if (eventType == 'iftar_party') {
      return PosterDesignModel(
        bismillahHeader: 'بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ',
        quranAyat: '“রোজাদারের জন্য রয়েছে দুটি আনন্দ...” — (আল-হাদিস)',
        eventTitle: 'পবিত্র মাহে রমজানের গণ-ইফতার ও দোয়া মাহফিল',
        subtitle: 'রহমত, মাগফিরাত ও নাজাতের সওগাত নিয়ে আয়োজিত বিশেষ ইফতার সমাবেশ',
        chiefGuest: 'সম্মানিত খতিব ও ওলামায়ে কেরাম',
        chiefGuestTitle: 'বিশিষ্ট ইসলামিক চিন্তাবিদ ও গবেষক',
        specialGuest: 'মসজিদ কমিটির সকল সম্মানিত উপদেষ্টাবৃন্দ',
        mainSpeaker: 'মাওলানা মো. মাহমুদুল হাসান',
        quranReciter: 'হাফেজ কারী আহমেদ উল্লাহ',
        chairPerson: 'সভাপতি, মসজিদ পরিচালনা কমিটি',
        eventDate: date ?? '২৫ রমজানুল মুবারক',
        hijriDate: 'পবিত্র রমজান ১৪৪৮ হিজরি',
        eventTime: 'বিকাল ৫:০০ ঘটিকা (ইফতারের পূর্ব মুহূর্ত)',
        venue: venue ?? 'মসজিদের নীচতলা ও প্রাঙ্গণ',
        organizers: 'উদ্যোগে: মসজিদ কমিটি ও এলাকার ধর্মপ্রাণ যুবসমাজ',
        themeStyle: 'emerald_gold',
      );
    }

    if (eventType == 'shab_e_qadr') {
      return PosterDesignModel(
        bismillahHeader: 'بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ',
        quranAyat: '“নিশ্চয়ই আমি এটি নাযিল করেছি মহিমান্বিত রজনীতে...” — (সূরা আল-ক্বদর)',
        eventTitle: 'পবিত্র লাইলাতুল ক্বদর ও বিশেষ কিয়ামুল লাইল',
        subtitle: 'হাজার মাসের চেয়ে শ্রেষ্ঠ রজনীর বিশেষ নফল ইবাদত, খতমে কুরআন ও রোনাজারি',
        chiefGuest: 'মুফতী মাওলানা তাজুল ইসলাম',
        chiefGuestTitle: 'মুহাদ্দিস ও শায়খুল হাদিস',
        specialGuest: 'স্থানীয় সম্মানিত ওলামায়ে কেরাম',
        mainSpeaker: 'মাওলানা বশির উল্লাহ',
        quranReciter: 'আন্তর্জাতিক পুরস্কারপ্রাপ্ত ক্বারী হাফেজ জুনায়েদ',
        chairPerson: 'সভাপতি ও সাধারণ সম্পাদক, মসজিদ কমিটি',
        eventDate: date ?? '২৭শে রমজান দিবাগত রাত',
        hijriDate: 'পবিত্র রমজান ১৪৪৮ হিজরি',
        eventTime: 'তারাবীহ পরবর্তী সারারাত ব্যাপী',
        venue: venue ?? 'মসজিদ কমপ্লেক্স',
        organizers: 'পরিচালনায়: মসজিদ পরিচালনা পরিষদ',
        themeStyle: 'royal_blue',
      );
    }

    // Default Waz Mahfil Template
    return PosterDesignModel(
      bismillahHeader: 'بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ',
      quranAyat: '“তোমরা সৎকাজে পরস্পরকে সাহায্য করো এবং অসৎকাজে নয়...” — (সূরা মায়িদাহ: ২)',
      eventTitle: prompt.isNotEmpty ? prompt : 'ঐতিহাসিক বাৎসরিক তাফসিরুল কুরআন ও ওয়াজ মাহফিল',
      subtitle: 'ইসলাম, শান্তি ও মানবতার কল্যাণে এক বিশাল ঐতিহাসিক তাফসির সমাবেশ',
      chiefGuest: 'মুফতী মাওলানা মিজানুর রহমান কাসেমী',
      chiefGuestTitle: 'আন্তর্জাতিক খ্যাতিসম্পন্ন মুফাসসিরে কুরআন ও মিডিয়া ব্যক্তিত্ব',
      specialGuest: 'হাফেজ কারী মো. ফোরকান উল্লাহ (পীর সাহেব)',
      mainSpeaker: 'মাওলানা মুফতী উসমান গনী',
      quranReciter: 'আন্তর্জাতিক ক্বারী মাওলানা আব্দুল্লাহ',
      chairPerson: 'মুহতারাম সভাপতি, মসজিদ পরিচালনা কমিটি',
      eventDate: date ?? '১৫ই ডিসেম্বর ২০২৬',
      hijriDate: 'পবিত্র রবিউস সানি ১৪৪৮ হিজরি',
      eventTime: 'বাদ আসর হতে মধ্যরাত পর্যন্ত',
      venue: venue ?? 'মসজিদ সংলগ্ন ঈদগাহ ময়দান',
      organizers: 'সার্বিক ব্যবস্থাপনায়: মসজিদ কমিটি ও সর্বস্তরের ধর্মপ্রাণ মুসল্লিবৃন্দ',
      themeStyle: 'emerald_gold',
    );
  }
}
