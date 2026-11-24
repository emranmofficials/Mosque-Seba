import 'package:equatable/equatable.dart';

/// Islamic Poster Content & Styling Configuration Model
class PosterDesignModel extends Equatable {
  final String bismillahHeader;
  final String quranAyat;
  final String eventTitle;
  final String subtitle;
  final String chiefGuest;
  final String chiefGuestTitle;
  final String specialGuest;
  final String mainSpeaker;
  final String quranReciter;
  final String chairPerson;
  final String eventDate;
  final String hijriDate;
  final String eventTime;
  final String venue;
  final String organizers;
  final String themeStyle; // 'emerald_gold', 'royal_blue', 'classic_maroon'

  const PosterDesignModel({
    this.bismillahHeader = 'بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ',
    this.quranAyat = '“তোমরা সৎকাজে পরস্পরকে সাহায্য করো...” — (আল-কুরআন)',
    required this.eventTitle,
    this.subtitle = 'ঐতিহাসিক বাৎসরিক তাফসিরুল কুরআন ও ওয়াজ মাহফিল',
    this.chiefGuest = 'মুফতী মাওলানা মিজানুর রহমান',
    this.chiefGuestTitle = 'আন্তর্জাতিক খ্যাতিসম্পন্ন মুফাসসিরে কুরআন',
    this.specialGuest = 'হাফেজ কারী মো. ফোরকান উল্লাহ',
    this.mainSpeaker = 'মাওলানা নুরুল ইসলাম ফারুকী',
    this.quranReciter = 'আন্তর্জাতিক ক্বারী মাওলানা আব্দুল্লাহ',
    this.chairPerson = 'মুহতারাম সভাপতি, মসজিদ পরিচালনা কমিটি',
    required this.eventDate,
    this.hijriDate = 'পবিত্র রবিউস সানি ১৪৪৮ হিজরি',
    this.eventTime = 'বাদ আসর হতে মধ্যরাত পর্যন্ত',
    this.venue = 'মসজিদ প্রাঙ্গণ ও সংলগ্ন ঈদগাহ ময়দান',
    this.organizers = 'সার্বিক ব্যবস্থাপনায়: মসজিদ পরিচালনা কমিটি ও সর্বস্তরের ধর্মপ্রাণ মুসল্লিবৃন্দ',
    this.themeStyle = 'emerald_gold',
  });

  PosterDesignModel copyWith({
    String? bismillahHeader,
    String? quranAyat,
    String? eventTitle,
    String? subtitle,
    String? chiefGuest,
    String? chiefGuestTitle,
    String? specialGuest,
    String? mainSpeaker,
    String? quranReciter,
    String? chairPerson,
    String? eventDate,
    String? hijriDate,
    String? eventTime,
    String? venue,
    String? organizers,
    String? themeStyle,
  }) {
    return PosterDesignModel(
      bismillahHeader: bismillahHeader ?? this.bismillahHeader,
      quranAyat: quranAyat ?? this.quranAyat,
      eventTitle: eventTitle ?? this.eventTitle,
      subtitle: subtitle ?? this.subtitle,
      chiefGuest: chiefGuest ?? this.chiefGuest,
      chiefGuestTitle: chiefGuestTitle ?? this.chiefGuestTitle,
      specialGuest: specialGuest ?? this.specialGuest,
      mainSpeaker: mainSpeaker ?? this.mainSpeaker,
      quranReciter: quranReciter ?? this.quranReciter,
      chairPerson: chairPerson ?? this.chairPerson,
      eventDate: eventDate ?? this.eventDate,
      hijriDate: hijriDate ?? this.hijriDate,
      eventTime: eventTime ?? this.eventTime,
      venue: venue ?? this.venue,
      organizers: organizers ?? this.organizers,
      themeStyle: themeStyle ?? this.themeStyle,
    );
  }

  @override
  List<Object?> get props => [
        eventTitle,
        chiefGuest,
        specialGuest,
        eventDate,
        venue,
        themeStyle,
      ];
}
