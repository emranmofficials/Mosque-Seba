import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';

enum IslamicEventCategory {
  obligatory(labelBn: 'ফরজ ও ওয়াজিব', color: AppColors.urgentRed),
  eid(labelBn: 'পবিত্র ঈদ ও আনন্দ', color: AppColors.emeraldPrimary),
  blessedNight(labelBn: 'বরকতময় রজনী', color: AppColors.goldDeep),
  sunnahFast(labelBn: 'সুন্নাত ও নফল সিয়াম', color: AppColors.successGreen),
  historic(labelBn: 'ঐতিহাসিক তাৎপর্য', color: AppColors.infoBlue);

  final String labelBn;
  final Color color;

  const IslamicEventCategory({
    required this.labelBn,
    required this.color,
  });
}

/// 13 Annual Islamic Occasions Model (IslamicCalendarModels.kt Parity)
class IslamicEvent {
  final String id;
  final String titleBn;
  final String titleEn;
  final int hijriMonth; // 1 = Muharram .. 12 = Dhul-Hijjah
  final int hijriDay;
  final int? hijriEndDay;
  final IslamicEventCategory category;
  final String summaryBn;
  final String defaultSignificanceBn;
  final List<String> defaultAmalsBn;
  final List<String> defaultKhutbahPoints;

  const IslamicEvent({
    required this.id,
    required this.titleBn,
    required this.titleEn,
    required this.hijriMonth,
    required this.hijriDay,
    this.hijriEndDay,
    required this.category,
    required this.summaryBn,
    required this.defaultSignificanceBn,
    required this.defaultAmalsBn,
    required this.defaultKhutbahPoints,
  });

  static const List<IslamicEvent> annualEvents = [
    IslamicEvent(
      id: 'evt_ashura',
      titleBn: 'পবিত্র আশুরা',
      titleEn: 'Ashura',
      hijriMonth: 1,
      hijriDay: 10,
      category: IslamicEventCategory.sunnahFast,
      summaryBn: '১০ই মহররম কারবালার শোকাবহ স্মৃতি ও ঐতিহাসিক অলৌকিক মুক্তির দিন।',
      defaultSignificanceBn: 'হযরত মুসা (আ.) ও তাঁর অনুসারীদের ফেরাউনের কবল থেকে অলৌকিক মুক্তি এবং কারবালার শাহাদাতের চিরন্তন শিক্ষা।',
      defaultAmalsBn: ['৯ ও ১০ অথবা ১০ ও ১১ই মহররম রোজা রাখা।', 'তওবা-ইস্তিগফার ও সদকা বৃদ্ধি করা।'],
      defaultKhutbahPoints: ['আশুরার ঐতিহাসিক শিক্ষা ও সত্যের ওপর অবিচল থাকা।', 'অন্যায় ও জুলুমের বিরুদ্ধে আপসহীন চেতনা।'],
    ),
    IslamicEvent(
      id: 'evt_seerat',
      titleBn: 'ঈদে মিলাদুন্নবী (সা.) / সীরাতুন্নবী',
      titleEn: 'Seerat-un-Nabi',
      hijriMonth: 3,
      hijriDay: 12,
      category: IslamicEventCategory.historic,
      summaryBn: 'বিশ্বনবী হযরত মুহাম্মদ (সা.)-এর পবিত্র বেলাদত ও আদর্শময় জীবনচরিত।',
      defaultSignificanceBn: 'সমগ্র মানবজাতির রহমত স্বরূপ মহানবী (সা.)-এর শুভাগমন ও সুন্নাহ অনুসরণের অঙ্গীকার।',
      defaultAmalsBn: ['রাসূল (সা.)-এর ওপর বেশি বেশি দরুদ পাঠ।', 'সীরাত আলোচনা মাহফিল আয়োজন।'],
      defaultKhutbahPoints: ['মহানবী (সা.)-এর চারিত্রিক মাধুর্য ও সমাজ সংস্কার।', 'পারিবারিক ও ব্যবসায়িক জীবনে সুন্নাহর বাস্তবায়ন।'],
    ),
    IslamicEvent(
      id: 'evt_meraj',
      titleBn: 'পবিত্র শবে মেরাজ',
      titleEn: 'Shab-e-Meraj',
      hijriMonth: 7,
      hijriDay: 27,
      category: IslamicEventCategory.blessedNight,
      summaryBn: 'মহানবী (সা.)-এর ঊর্ধ্বাকাশ ভ্রমণ ও পাঁচ ওয়াক্ত নামাজের তোহফা প্রাপ্তির রাত।',
      defaultSignificanceBn: 'মহিমান্বিত ইসরা ও মেরাজের অলৌকিক যাত্রা এবং উম্মতে মুহাম্মদীর জন্য সর্বশ্রেষ্ঠ উপহার পাঁচ ওয়াক্ত সালাত।',
      defaultAmalsBn: ['নফল সালাত, কুরআন তেলাওয়াত ও জিকির।', 'পরের দিন নফল রোজা রাখা।'],
      defaultKhutbahPoints: ['নামাজের গুরুত্ব ও মেরাজের অলৌকিক নিদর্শন।', 'দৈনন্দিন জীবনে সালাতের পবিত্র হেফাজত।'],
    ),
    IslamicEvent(
      id: 'evt_barat',
      titleBn: 'পবিত্র শবে বরাত (লাইলাতুল বরাত)',
      titleEn: 'Shab-e-Barat',
      hijriMonth: 8,
      hijriDay: 15,
      category: IslamicEventCategory.blessedNight,
      summaryBn: 'মধ্য শাবানের মহিমান্বিত ক্ষমার রাত ও রহমতের ফয়সালা।',
      defaultSignificanceBn: 'আল্লাহ তাআলা বান্দাদের জন্য রহমত ও ক্ষমার দরজা উন্মুক্ত করে দেন।',
      defaultAmalsBn: ['রাত জেগে ইবাদত ও পূর্বপুরুষদের জন্য দোয়া।', '১৫ই শাবান নফল রোজা রাখা।'],
      defaultKhutbahPoints: ['গুনাহ থেকে তাওবা এবং পারস্পরিক হিংসা-বিদ্বেষ পরিহার।', 'রমজানের প্রস্তুতি হিসেবে শাবানের তাৎপর্য।'],
    ),
    IslamicEvent(
      id: 'evt_ramadan',
      titleBn: 'পবিত্র রমজানুল মুবারক',
      titleEn: 'Ramadan Fasting',
      hijriMonth: 9,
      hijriDay: 1,
      hijriEndDay: 30,
      category: IslamicEventCategory.obligatory,
      summaryBn: 'রহমত, মাগফিরাত ও নাজাতের মাস এবং সিয়াম সাধনার ফরজ বিধান।',
      defaultSignificanceBn: 'পবিত্র কুরআন অবতীর্ণের মাস এবং তাকওয়া অর্জনের সর্বোত্তম সুবর্ণ সুযোগ।',
      defaultAmalsBn: ['ফরজ সিয়াম পালন।', 'তারাবির নামাজ ও খতমে কুরআন।', 'সদকাতুল ফিতর ও যাকাত আদায়।'],
      defaultKhutbahPoints: ['সিয়ামের আধ্যাত্মিক ও সামাজিক শিক্ষা।', 'পরোপকার ও আত্মার পরিশুদ্ধি।'],
    ),
    IslamicEvent(
      id: 'evt_qadr',
      titleBn: 'পবিত্র শবে কদর (লাইলাতুল কদর)',
      titleEn: 'Laylatul Qadr',
      hijriMonth: 9,
      hijriDay: 27,
      category: IslamicEventCategory.blessedNight,
      summaryBn: 'হাজার মাসের চেয়েও উত্তম বরকতময় ভাগ্যরজনী।',
      defaultSignificanceBn: 'পবিত্র কুরআন নাজিলের রাত এবং ফেরেশতাদের শান্তির বার্তা অবতরণ।',
      defaultAmalsBn: ['সারারাত জেগে তাহাজ্জুদ, কুরআন তেলাওয়াত ও বিশেষ দোয়া।'],
      defaultKhutbahPoints: ['লাইলাতুল কদরের মর্যাদা ও শেষ দশকের বিজোড় রাতের অনুসন্ধান।'],
    ),
    IslamicEvent(
      id: 'evt_fitr',
      titleBn: 'পবিত্র ঈদুল ফিতর',
      titleEn: 'Eid-ul-Fitr',
      hijriMonth: 10,
      hijriDay: 1,
      category: IslamicEventCategory.eid,
      summaryBn: 'এক মাস সিয়াম সাধনা শেষে আনন্দ ও পুরস্কারের মহোৎসব।',
      defaultSignificanceBn: 'গরিব-দুখীদের ফিতরা প্রদান এবং ভ্রাতৃত্বের মেলবন্ধন।',
      defaultAmalsBn: ['সদকাতুল ফিতরা ঈদের নামাজের পূর্বে আদায়।', 'ঈদের নামাজ জামাতে আদায়।'],
      defaultKhutbahPoints: ['তাকওয়া অব্যাহত রাখা এবং সমাজের অবহেলিতদের মুখে হাসি ফোটানো।'],
    ),
    IslamicEvent(
      id: 'evt_arafah',
      titleBn: 'পবিত্র আরাফাহ দিবস',
      titleEn: 'Day of Arafah',
      hijriMonth: 12,
      hijriDay: 9,
      category: IslamicEventCategory.sunnahFast,
      summaryBn: 'হজের মূল দিন এবং দুই বছরের গুনাহ মাফের সুন্নাত রোজা।',
      defaultSignificanceBn: 'আরাফাতের ময়দানে লাব্বাইক ধ্বনি ও আল্লাহর ক্ষমার মহাপ্লাবন।',
      defaultAmalsBn: ['আরাফাতের দিনের নফল রোজা রাখা।', 'তাকবীরে তাশরিক পাঠ।'],
      defaultKhutbahPoints: ['বিদায় হজের ঐতিহাসিক মানবাধিকার সনদ ও কুরবানির মূল চেতনা।'],
    ),
    IslamicEvent(
      id: 'evt_adha',
      titleBn: 'পবিত্র ঈদুল আজহা',
      titleEn: 'Eid-ul-Adha',
      hijriMonth: 12,
      hijriDay: 10,
      hijriEndDay: 12,
      category: IslamicEventCategory.eid,
      summaryBn: 'হযরত ইব্রাহিম (আ.) ও ইসমাইল (আ.)-এর ত্যাগ ও পশু কুরবানির মহাপর্ব।',
      defaultSignificanceBn: 'আল্লাহর সন্তুষ্টির জন্য আত্মত্যাগের চিরন্তন মহিমা।',
      defaultAmalsBn: ['ওয়াজিব কুরবানি আদায় ও গোশত সমবণ্টন।', 'আইয়ামে তাশরিকের তাকবীর পাঠ।'],
      defaultKhutbahPoints: ['কুরবানির অন্তর্নিহিত তাকওয়া ও অহংকার বর্জন।'],
    ),
  ];
}
