# Changelog

All notable changes to the **মসজিদ সেবা (Mosque Seba)** project will be documented in this file.

## [1.1.6] - 2026-11-17
### Major Security & Multi-Tenancy Release (সংস্করণ ১.১.৬ — কঠোর মাল্টি-টেন্যান্ট ডেটা আইসোলেশন ও ক্লাউড অথেনটিকেশন সুরক্ষা)
- **কঠোর মাল্টি-টেন্যান্ট পার্টিশন লকিং (`SupabaseSyncManager.kt`)**:
  - কোনো অবস্থাতেই এক মসজিদের তথ্য অন্য মসজিদে যাওয়ার সুযোগ নেই; প্রতিটি টেবিলের কোয়েরিতে `mosque_id=eq.$currentMosqueId` কঠোরভাবে সংযুক্ত।
  - পুশ এবং আপসার্টের প্রতিটি পে-লোডে `mosque_id` স্বয়ংক্রিয়ভাবে ইনজেক্ট করা হয়।
  - ফ্রেশ ডিভাইসে অ্যাপ ইনস্টল থাকা অবস্থায় কোনো মসজিদ সিলেক্ট বা লগইন ছাড়া কোনো র্যান্ডম মসজিদের ডেটা ডাউনলোড হওয়া বন্ধ করা হয়েছে।
- **নতুন ডিভাইসে তাৎক্ষণিক মসজিদ ও মেম্বার পুনরুদ্ধার (`fetchAndRestoreMosqueByCommitteeId`)**:
  - কমিটি আইডি দিয়ে নতুন কোনো ডিভাইসে লগইন করলে সুপাবেইজ ক্লাউড থেকে শুধুমাত্র সেই নির্দিষ্ট মসজিদের তথ্য ও সদস্যদের প্রোফাইল স্বয়ংক্রিয়ভাবে ডাউনলোড ও প্রস্তুত হয়।
- **নিরাপদ পাসওয়ার্ড হ্যাশ সিঙ্ক ও ভেরিফিকেশন (`profiles.password_hash`)**:
  - সভাপতির পাসওয়ার্ড শক্তিশালী SHA-256 ক্রিপ্টোগ্রাফিক হ্যাশ আকারে ক্লাউড প্রোফাইলে সংরক্ষিত হয়।
  - যেকোনো ডিভাইসে সভাপতির লগইন মুহূর্তে লোকাল ও ক্লাউড হ্যাশ ভেরিফিকেশনের মাধ্যমে নির্ভুল অথেনটিকেশন প্রদান করা হয়।
- **সম্পূর্ণ দ্বিমুখী ক্লাউড সিঙ্ক (Full Bidirectional Multi-Table Sync)**:
  - মিটিং, চাঁদা আদায়, মাদরাসা শিক্ষার্থী, দান বাক্স (ক্যাশবক্স), সমাজকল্যাণ কেস, নির্বাচন তফসিল ও প্রার্থী এবং মসজিদ সম্পত্তি মডিউলে লাইভ সিঙ্ক।

## [1.1.5] - 2026-11-17
### Major Cloud Real-Time & Backend Infrastructure Release (সংস্করণ ১.১.৫)
### Major Production Release (সংস্করণ ১.১.১ — পরিচ্ছন্ন হেডার, প্রফেশনাল ম্যানেজমেন্ট সিস্টেম ও ডাইনামিক টাইমলাইন)
- **হেডার ক্লিনআপ ও আধুনিকীকরণ (`GlobalHeader.kt`)**:
  - ইউজারের নামের উপর থেকে প্রতিষ্ঠানের নাম সম্পূর্ণ বন্ধ/অপসারণ করে হেডারকে আরও পরিচ্ছন্ন ও প্রফেশনাল রূপদান।
  - হেডারের ডেমো/মক ফলব্যাক নাম সম্পূর্ণ অপসারণ করে সক্রিয় সেশন ও প্রোফাইল থেকে স্বয়ংক্রিয় ডাইনামিক নাম ও পদবি প্রদর্শন।
  - পদবিবিহীন বা নতুন ব্যবহারকারীর জন্য স্মার্ট "সক্রিয় দায়িত্বশীল" ব্যাজ ইন্ডিকেটর।
- **প্রফেশনাল ম্যানেজমেন্ট সিস্টেম হার্ডেনিং**:
  - **লেনদেন অনুমোদন অডিট লগ (`MosqueViewModel.kt`)**: অনুমোদন রেকর্ডে কোনো ডেমো নাম নয়; বর্তমান সেশনে লগইনকৃত কর্মকর্তার আসল নাম ও পদবি স্বচ্ছভাবে লিপিবদ্ধ।
  - **ডাইনামিক চাঁদা ও বেতন সাইকেল (`ChandaMonthUtils.kt`, `Dialogs.kt`, `MoreDialogs.kt`)**: চাঁদা আদায় ও স্টাফদের বেতন পরিশোধে হার্ডকোডেড অতীত মাসের পরিবর্তে রিয়েল-টাইম ক্যালেন্ডার অনুযায়ী বর্তমান মাস ও বছর স্বয়ংক্রিয় নির্ধারণ (`getCurrentMonthYear()`)।
  - **অনুষ্ঠান ও মাহফিল সূচি (`MoreDialogs.kt`)**: নতুন অনুষ্ঠান ডায়ালগে ডাইনামিক বর্তমান তারিখ এবং পরিচ্ছন্ন বাজেট ও দলনেতা ইনপুট ফিল্ড।
  - **কমিটি মিটিং সূচি (`Dialogs.kt`)**: মিটিং নির্ধারণ ডায়ালগে স্বয়ংক্রিয় বর্তমান তারিখ ও সময় সংবলিত ডিফল্ট ফিল্ড।
  - **স্মার্ট কোড জেনারেটর (`MosqueViewModel.kt`)**: আর্থিক ভাউচার (`TXN-YYYY-XXX`), মাদরাসা ছাত্র কোড (`STU-YYYY-XXX`), ও ফি রসিদ (`FEE-YYYY-XXXXXX`)-এ স্বয়ংক্রিয় বর্তমান বছরের ভিত্তিতে কোড নির্ধারণ।
  - **মসজিদ ও কমিটি নিবন্ধন (`RegistrationScreen.kt`)**: মসজিদ ও পরিচালনা কমিটি নিবন্ধনে মেয়াদের সাল রিয়েল-টাইম বর্তমান সাল থেকে শুরু।
- **নতুন Supabase ক্লাউড ব্যাকএন্ড ইন্টিগ্রেশন (`SupabaseConfig.kt`, `SupabaseHttpClient.kt`)**:
  - অ্যাপের কেন্দ্রীয় ক্লাউড ব্যাকএন্ড নতুন ডেডিকেটেড একাউন্টে (`MosqueSeba` / `donglyxtrfyzwgtrskhz`) সফলভাবে স্থানান্তর ও কনফিগারেশন।
  - ২৮টি ডেটাবেজ টেবিল, মাল্টি-টেন্যান্ট RLS সিকিউরিটি পলিসি, আরপিসি স্টোর্ড প্রসিডিউর ও ডিফল্ট পদবি ক্যাটালগ স্বয়ংক্রিয় সক্রিয়করণ।
  - পোস্টগ্রেস্ট এপিআই কানেকশন টেস্ট পদ্ধতির আধুনিকীকরণ ও নির্ভরযোগ্য স্বাস্থ্য পরীক্ষা।

## [1.0.47] - 2026-11-17
### Major Feature Release (সংস্করণ ১.০.৪৭ — কর্মকর্তাদের সিলমোহর ও ডিজিটাল স্বাক্ষর ব্যবস্থাপনা এবং ডকুমেন্টে স্বয়ংক্রিয় সংযোজন)
- **কর্মকর্তাদের সিলমোহর ও ডিজিটাল স্বাক্ষর হাব (`OfficialSealSignatureDialog.kt`)**:
  - সভাপতি, সাধারণ সম্পাদক, কোষাধ্যক্ষ, সহ-সভাপতি, সহকারী সম্পাদক, সহকারী কোষাধ্যক্ষ, দপ্তর সম্পাদক এবং খতিব/ইমামদের সিল ও ডিজিটাল স্বাক্ষর ব্যবস্থাপনা।
  - মসজিদের কেন্দ্রীয় অফিসিয়াল গোল সিলমোহর (Central Round Seal) আপলোড ও ব্যবস্থাপনা।
  - ফটো গ্যালারি থেকে স্বাক্ষর ও সিল আপলোড এবং লাইভ প্রিভিউ।
  - টাচ-স্ক্রিনে সরাসরি আঙুল বা স্টাইলাস দিয়ে স্মুথ ডিজিটাল স্বাক্ষর আঁকার আধুনিক ক্যানভাস ড্রয়িং প্যাড (Interactive Canvas Drawing Pad)।
  - ৩টি অফিসিয়াল কালি রঙ (গাঢ় কালো, রয়েল নেভি ব্লু, পান্না সবুজ) এবং একাধিক স্ট্রোক থিকনেস নির্বাচন।
  - ড্রয়িং প্যাড থেকে ১-ক্লিকে পিএনজি ফরমেটে লোকাল স্টোরেজে সংরক্ষণ এবং তাৎক্ষণিক সংশ্লিষ্ট কর্মকর্তার প্রোফাইলে অ্যাসাইন।
- **রুম ডেটাবেজ মাইগ্রেশন ১০ (`MIGRATION_9_10`)**:
  - `MemberEntity`-তে `signatureUrl`, `stampUrl`, `designationBn` কলাম সংযোজন।
  - `StaffEntity`-তে `signatureUrl`, `stampUrl` কলাম সংযোজন।
  - `MosqueEntity`-তে `sealUrl` কলাম সংযোজন।
- **প্রিন্টেবল ও পিডিএফ ডকুমেন্টে স্বয়ংক্রিয় সিল-স্বাক্ষর সংযোজন (`MosquePrintHelper.kt`)**:
  - লোকাল ফাইল পাথকে স্বয়ংক্রিয়ভাবে নির্ভরযোগ্য Base64 Data URI-তে রূপান্তর করে এইচটিএমএল প্রিন্ট লেআউটে এম্বেডকরণ।
  - **চাঁদা রসিদ**: কোষাধ্যক্ষের ডিজিটাল স্বাক্ষর ও সিলমোহর, আদায়কারীর স্বাক্ষর এবং মসজিদের কেন্দ্রীয় সিলমোহর।
  - **মসজিদের নোটিশ**: সভাপতির ডিজিটাল স্বাক্ষর ও সিলমোহর এবং কেন্দ্রীয় অফিসিয়াল সিলমোহর।
  - **আয়-ব্যয় অডিট বিবরণী**: কোষাধ্যক্ষ, সাধারণ সম্পাদক ও সভাপতির ৩-স্তরীয় ডিজিটাল স্বাক্ষর ও সিলমোহর।
  - **জুমার দানবাক্স ভাউচার**: খতিব/ইমাম, কোষাধ্যক্ষ ও প্রত্যক্ষদর্শীর স্বাক্ষর এবং মসজিদের সিলমোহর।
  - **দাওয়াতনামা ও মাহফিল পোস্টার**: সভাপতির সিলমোহর ও স্বাক্ষর এবং মসজিদের কেন্দ্রীয় সিলমোহর।
- **নেভিগেশন ও প্রোফাইল ইন্টিগ্রেশন**:
  - `MoreOperationsHub` ও `MoreScreen`-এ "কর্মকর্তাদের সিলমোহর ও ডিজিটাল স্বাক্ষর" ডেডিকেটেড অপারেশন মডিউল।
  - `MainActivity`-তে গ্লোবাল ডায়ালগ হ্যান্ডলিং ও কুইক শর্টকাট।
  - `MemberProfileScreen`-এ সংশ্লিষ্ট সদস্য বা স্টাফের প্রোফাইল থেকে সরাসরি সিল-স্বাক্ষর ব্যবস্থাপনার সুযোগ।

## [1.0.46] - 2026-11-17
### Major Feature Release (সংস্করণ ১.০.৪৬ — ইসলামি দাওয়াতনামা, জুমার ক্যাশবক্স ৩-সাক্ষী কাউন্টার, জানাজা ব্রডকাস্ট ও নেটিভ প্রিন্টিং)
- **নোটিশ বোর্ডে ফটো/পোস্টার সংযুক্তি ও ফুল-স্ক্রিন ভিউয়ার (Notice Image & Poster Attachment)**:
  - মসজিদে প্রকাশিত যেকোনো নোটিশের সাথে গ্যালারি বা ক্যামেরা থেকে ফটো/পোস্টার/হাতে লেখা বিজ্ঞপ্তি সংযুক্তির পূর্ণ সুবিধা।
  - নোটিশ কার্ডে উচ্চ-রেজোলিউশন থাম্বনেইল ডিসপ্লে ও এক-ট্যাপে ফুল-স্ক্রিন জুম ভিউয়ার।
  - রুম ডেটাবেজ স্কিমা সংস্করণ ৯ এ মাইগ্রেশন (`imageUrl` কলাম সংযোজন ও স্বয়ংক্রিয় ব্যাকওয়ার্ড কম্প্যাটিবিলিটি)।
- **নেটিভ ১-ক্লিক প্রিন্টিং ও পিডিএফ জেনারেশন (`MosquePrintHelper.kt`)**:
  - তৃতীয় পক্ষের কোনো লাইব্রেরি ছাড়া অ্যান্ড্রয়েডের নেটিভ `PrintManager` ও `WebView` দিয়ে A4 এবং থার্মাল লেআউটে সরাসরি প্রিন্ট ও "Save as PDF"।
  - চাঁদা ও সদস্য রসিদ প্রিন্ট (`OfficialReceiptDialog`).
  - মসজিদের অফিসিয়াল বিজ্ঞপ্তি ও নোটিশ প্রিন্ট (`NoticesScreen`).
  - মাসিক আয়-ব্যয় ও তহবিল নিরীক্ষা বিবরণী অডিট কপি প্রিন্ট (`FinanceScreen`).
- **ইসলামিক দাওয়াতনামা ও মাহফিল পোস্টার এআই ক্রিয়েটর (`IslamicDawatnamaDialog.kt`)**:
  - বাৎসরিক ওয়াজ ও দোয়ার মাহফিল, রমজান ইফতার মাহফিল, শবে কদর, ভিত্তিপ্রস্তর স্থাপন ও এজিএম সভার আকর্ষণীয় দাওয়াতনামা তৈরি।
  - প্রধান অতিথি, প্রধান আলোচক, বিশেষ মেহমান ও সভাপতির নাম সংযোজন।
  - গুগল জেমিনি এআই দিয়ে প্রমিত ইসলামি আদব ও আন্তরিক ভাষায় দাওয়াতনামার স্বয়ংক্রিয় বর্ণনা ড্রাফট।
  - লাইভ পোস্টার প্রিভিউ, ১-ক্লিক প্রিন্ট/পিডিএফ, হোয়াটসঅ্যাপ ও সামাজিক যোগাযোগমাধ্যমে ফরম্যাটেড মেসেজ শেয়ার এবং অ্যাপের নোটিশ বোর্ডে তাৎক্ষণিক প্রকাশ।
- **জুমার দানবাক্স ৩-সাক্ষী নোট কাউন্টার ও প্রত্যয়ন ভাউচার (`JummahCashboxDialog.kt`)**:
  - বাংলাদেশি ব্যাংক নোটের মান অনুযায়ী কাউন্টার (৳১০০০, ৳৫০০, ৳২০০, ৳১০০, ৳৫০, ৳২০, ৳১০, ৳৫, ৳২, ৳১ ও খুচরা কয়েন)।
  - রিয়েল-টাইম স্বয়ংক্রিয় যোগফল ও উপ-মোট হিসাব প্রদর্শন।
  - ইমাম, কোষাধ্যক্ষ ও নিরপেক্ষ সাধারণ মুসল্লির ৩-সাক্ষী প্রত্যয়ন এবং স্বয়ংক্রিয় অনুমোদিত আয় এন্ট্রি।
  - স্বাক্ষরযুক্ত অফিশিয়াল "জুমার দানবাক্স গণনা প্রত্যয়ন ভাউচার" প্রিন্ট ও পিডিএফ সংরক্ষণ।
- **জরুরি জানাজা ও মৃত্যু সংবাদ ব্রডকাস্টার (`JanazaAlertDialog.kt`)**:
  - মহল্লাবাসীর নিকট মরহুম/মরহুমা, জানাজার স্থান-সময়, কবরস্থান ও পরিবারের জরুরি ফোন নম্বরসহ তাৎক্ষণিক জরুরি ঘোষণা।
  - কুরআনি আয়াত "ইন্না লিল্লাহি ওয়া ইন্না ইলাইহি রাজিউন" সহ তাৎক্ষণিক পিনড নোটিশ ও পুশ নোটিফিকেশন প্রকাশ।
  - মহল্লার হোয়াটসঅ্যাপ গ্রুপ ও এসএমএসে প্রেরণের জন্য প্রমিত ফরম্যাটেড ব্রডকাস্ট মেসেজ।
  - মসজিদের গেট ও নোটিশ বোর্ডে টাঙানোর জন্য ১-ক্লিক শোকবার্তা ও জানাজা পোস্টার প্রিন্ট।
- **সুপাবেইজ ক্লাউড জেমিনি এজ ফাংশন ডিপ্লয়মেন্ট (`supabase/functions/gemini-assistant`)**:
  - গুগল ক্লাউডের আধুনিক `gemini-2.5-flash` মডেলের সাথে সরাসরি সংযুক্ত সুরক্ষিত সার্ভার-সাইড এআই এজ ফাংশন।
  - সুপাবেইজ সিক্রেট এনক্রিপশনে `GEMINI_API_KEY` কনফিগার ও সক্রিয়।
  - লাইভ আর্থিক অডিট পর্যবেক্ষণ ও নোটিশ ড্রাফটার সফলভাবে ডিপ্লয়কৃত (`ACTIVE v3`)।
### Official Production Ready Release (সংস্করণ ১.০.৪৫ — সম্পূর্ণ ফ্রেশ ও প্রডাকশন রেডি)
- **১০০% ডেমো ডেটামুক্ত ফ্রেশ প্রোডাকশন ডাটাবেজ (Complete Mock Data Purge)**:
  - পূর্ববর্তী সকল কাল্পনিক ও ডেমো মসজিদ, সদস্য, ভাউচার, ক্যাশবক্স লেনদেন, মিটিং, নোটিশ, ওয়াকফ সম্পত্তি ও অডিট লগ চিরতরে অপসারিত।
  - `DatabaseInitializer.kt` এবং `MosqueDatabase.kt` এ স্বয়ংক্রিয় সেলফ-হিলিং পার্জ মেকানিজম (`purgeDemoDataIfPresent`) সংযুক্ত।
  - `AccountHubScreen.kt` এ ইউজারদের সুবিধার্থে "সকল ডেমো ডেটা মুছে ফ্রেশ শুরু করুন" ডেটাবেজ রিসেট অ্যাকশন বাটন যুক্ত।
- **লগইন স্ক্রিন ও সিকিউরিটি চূড়ান্তকরণ (`LoginScreen.kt`, `SessionManager.kt`)**:
  - প্রোটোটাইপ রোল-বাইপাস চিপস এবং প্রি-ফিল্ড কাল্পনিক তথ্য সম্পূর্ণরূপে মুছে সম্পূর্ণ সিকিউর লগইন সিস্টেম চালু।
  - ডিফল্ট ফলব্যাক আইডি দূর করে প্রতিটি মসজিদের নিজস্ব ইউনিক আইডি এবং রসিদ কিউআর কোড ডায়নামিক করা হয়েছে।
- **স্মার্ট ইসলামিক ক্যালেন্ডার ও হিজরি ইভেন্ট হাব (AI-Powered Islamic Hub - `IslamicCalendarScreen.kt`)**:
  - বাৎসরিক ১৩টি গুরুত্বপূর্ণ ইসলামিক দিবসের (রমজান, শবে কদর, ঈদুল ফিতর, ঈদুল আজহা, আশুরা ইত্যাদি) নির্ভুল দিন গণনা ও রিয়েল-টাইম স্ট্যাটাস কাউন্টডাউন।
  - স্থানীয় চাঁদ দেখার তারতম্য অনুযায়ী -১ দিন, স্বাভাবিক বা +১ দিন অ্যাডজাস্ট করার স্থায়ী অফসেট ফিচার।
  - জেমিনি এআই সহায়িকা: খতিবদের জন্য জুমার বয়ানের পয়েন্ট ও মুসল্লিদের জন্য নফল আমলের স্বয়ংক্রিয় গাইডলাইন।
- **গণতান্ত্রিক কমিটি নির্বাচন ব্যবস্থা (`ElectionScreen.kt`)**:
  - স্বচ্ছ প্রার্থী মনোনয়ন, ভোটার ব্যালট পেপার ভোটিং এবং তাৎক্ষণিক স্বচ্ছ ফলাফল গণনা ব্যবস্থা।
- **ডিজিটাল সহিহ হাদিস স্মার্ট বোর্ড (`DatabaseInitializer.kt`)**:
  - ড্যাশবোর্ডে প্রতিদিন বিশুদ্ধ হাদিসের আলোকে সহিহ বুখারি ও মুসলিম শরিফের প্রামাণ্য নসিহত ঘূর্ণায়মান প্রদর্শন (১০০% অফলাইন)।
- **অফিসিয়াল সাইনিং কি ও স্বয়ংক্রিয় সিআই/সিডি বিল্ড**:
  - স্থায়ী রিলিজ কি-স্টোর (`keystore/mosque-seba.jks`) দিয়ে স্বাক্ষরিত প্রোডাকশন APK ও AAB রিলিজ সম্পন্ন।

## [1.0.22] - 2026-11-15
### Enhanced (Professional Role & Responsibility Interface Overhaul)
- **পদ ও দায়িত্বভিত্তিক ইন্টারফেস রিডিজাইন (Role & Responsibility Tailored Workspaces - `DashboardScreen.kt`)**:
  - প্রতিটি পদের সুনির্দিষ্ট দায়িত্ব ও কর্তব্যের ভিত্তিতে স্বতন্ত্র ও পেশাদার ড্যাশবোর্ড ওয়ার্কস্পেস বাস্তবায়ন (`when (currentRole)`):
    1. **সভাপতি ও সহ-সভাপতি (President & Vice President)**: নির্বাহী অনুমোদন ডেস্ক, অপেক্ষমাণ ভাউচার নিষ্পত্তি হটলাইন, দানবাক্স সাক্ষী যাচাই, শরিয়াহ পরামর্শ ও প্রশাসনিক তদারকি।
    2. **কোষাধ্যক্ষ ও সহকারী কোষাধ্যক্ষ (Treasurer & Assistant Treasurer)**: `TreasurerTreasuryLedgerCard` — কেন্দ্রীয় বায়তুলমাল স্থিতি, নগদ বনাম ব্যাংক খতিয়ান, বকেয়া চাঁদা ও চলতি মাসের আদায় সূচক, ক্যাশবক্স জমা, স্টাফ বেতন ক্লিয়ারেন্স ও ভাউচার এন্ট্রি।
    3. **সাধারণ সম্পাদক ও সম্পাদকীয় পরিষদ (Secretariat)**: `SecretariatExecutiveHubCard` — আসন্ন কমিটি সভা ও এজেন্ডা প্রস্তুতি, জেমিনি এআই নোটিশ ড্রাফটার ও দাপ্তরিক চিঠিপত্র/নথিপত্র ট্র্যাকিং।
    4. **ইমাম (Imam)**: `ImamMehrabDutyCard` (৫ ওয়াক্ত সালাত ও জামাত কাউন্টডাউন, জুমার খুতবা ও নসিহত), `ImamMadrasaHifzDeskCard` (হিফজ ও মক্তব সবক মূল্যায়ন), এবং `StaffFoodScheduleCard` (মেহমানদার পরিবার সমন্বয়)।
    5. **মুয়াজ্জিন (Muazzin)**: `MuazzinAzanDutyCard` (আজান ও ইকামত সময় নিয়ন্ত্রণ, মাইক ও ব্যাটারি চার্জ নোটিশ), ডিউটি রোস্টার ও স্টাফ খানা সূচি।
    6. **খাদেম (Khadem)**: `KhademFacilityChecklistCard` — দৈনিক ৪-দফা পরিচ্ছন্নতা চেকলিস্ট (প্রধান নামাজ কক্ষ ও কার্পেট, অজুখানা ও ওয়াশরুম, বাতি/ফ্যান/সাউন্ড এবং বিশুদ্ধ পানি/পাম্প), রিয়েল-টাইম টগল ও প্রগ্রেস ট্র্যাকিং।
    7. **সাধারণ মুসল্লি (Musalli / General Member)**: `MusalliTransparencySummaryCard` — স্বচ্ছ বায়তুলমাল হিসাব, ব্যক্তিগত চাঁদা খতিয়ান, ভেরিফায়েড কিউআর ডিজিটাল রসিদ ও `MusalliDevotionalCard` (সহিহ হাদিস ও কুরআন নসিহত)।
    8. **সমাজকল্যাণ ও শিক্ষা সম্পাদক**: `WelfareEduDeskCard` — যাকাত তহবিল ও অসচ্ছল পরিবার সহায়তা কেস অনুমোদন এবং মক্তব শিক্ষা অগ্রগতি।
    9. **প্রচার ও প্রকাশনা সম্পাদক**: `PublicityNoticeDeskCard` — স্মার্ট ডিজিটাল বোর্ড স্লাইড ও জরুরি দেয়াল নোটিশ প্রকাশ।
    10. **উপদেষ্টা পরিষদ ও কার্যনির্বাহী সদস্য**: শরিয়াহ পর্যালোচনা নোট, দানবাক্স নিরপেক্ষ সাক্ষ্য ও কমিটি এজেন্ডা অডিট।
- **আর্থিক ও সদস্য স্ক্রিনে দায়িত্ব সচেতনতা ব্যানার (`FinanceScreen.kt`, `MembersScreen.kt`)**:
  - `FinanceScreen.kt`-এ রোলভেদে "কোষাধক্ষ্য প্যানেল: বায়তুলমাল ব্যবস্থাপনা ও অডিট নিয়ন্ত্রণ" বনাম "সার্বজনীন স্বচ্ছতা প্যানেল: উন্মুক্ত হিসাব নিরীক্ষণ ভিউ" স্ট্যাটাস হেডার সংযুক্ত।
  - `MembersScreen.kt`-এ দায়িত্ব অনুযায়ী রোল-টেইলর্ড সাবটাইটেল ও অপারেশন মোড প্রদর্শন।
- **লোগো ও ভিজ্যুয়াল সামঞ্জস্য বিধান (Logo UI Consistency)**:
  - মসজিদের অফিসিয়াল লোগো ব্যাকগ্রাউন্ড স্ট্যান্ডার্ডাইজ করে `MosqueEmeraldDark`-এ রূপান্তর।
  - ডায়ালগ, গ্লোবাল হেডার, হিরো কার্ড, স্প্ল্যাশ স্ক্রিন, লগইন ও প্রোফাইল স্ক্রিনে লোগোর স্কেলিং ও ক্লিপিং নিখুঁত করা হয়েছে।
  - অ্যান্ড্রয়েড লঞ্চার অ্যাডাপটিভ আইকন ব্যাকগ্রাউন্ড ও ড্রয়েবল আর্টওয়ার্ক আপডেট।
- **অ্যাপ সংস্করণ বৃদ্ধি**:
  - `versionCode = 22` এবং `versionName = "1.0.22"` কনফিগারেশন আপডেট।

## [1.0.0] - 2026-11-14
### Official Production Release (সংস্করণ ১.০.০ - অফিসিয়াল রিলিজ)
- **অফিসিয়াল প্রোডাকশন সংস্করণ চূড়ান্তকরণ (Official Release v1.0.0)**:
  - `versionCode = 1` এবং `versionName = "1.0.0"` কনফিগারেশন সেট।
  - সম্পূর্ণ বাগ-মুক্ত, স্ট্যাবল ও প্রোডাকশন-রেডি অ্যান্ড্রয়েড বিল্ড।
- **ট্যাবলেট ও ফোল্ডেবল ডিভাইসে অ্যাডাপটিভ নেভিগেশন (`MainActivity.kt`)**:
  - স্ক্রিনের প্রস্থ ৬০০ডিপি বা তার বেশি হলে স্বয়ংক্রিয়ভাবে `MosqueNavigationRail` সক্রিয় হয়ে প্রশস্ত স্ক্রিনের সর্বোচ্চ সদ্ব্যবহার নিশ্চিত করে।
  - স্মার্টফোন স্ক্রিনে স্বয়ংক্রিয়ভাবে স্মুথ রেসপনসিভ `NavigationBottomBar` বজায় রাখা।
- **রোল-ভিত্তিক নোটিফিকেশন সেন্টার (`NotificationsScreen.kt`)**:
  - পদমর্যাদা ভিত্তিক ফিল্টার করা নোটিফিকেশন তালিকা (অনুমোদনের অপেক্ষায় থাকা ভাউচার, জরুরি মিটিং, নতুন নোটিশ ও গুরুত্বপূর্ণ খবরাখবর)।
  - আনরিড কাউন্টার ব্যাজ ও গ্লোবাল হেডার থেকে ১-ক্লিকে নোটিফিকেশন স্ক্রিনে প্রবেশের সুবিধা।
- **সেন্ট্রাল অ্যাকাউন্ট হাব ও প্রোফাইল ব্যবস্থাপনা (`AccountHubScreen.kt`, `MemberProfileScreen.kt`, `MosqueProfileScreen.kt`)**:
  - সদস্য ও স্টাফদের বিস্তারিত ব্যক্তিগত তথ্য, যোগদানের তারিখ, মোবাইল ও এনআইডি প্রদর্শন।
  - ব্যক্তিগত মাসিক চাঁদা পরিশোধের খতিয়ান এবং ইমাম/খাদেমদের নিজস্ব বেতন ও ভাতা স্লিপ দর্শন।
  - ডার্ক মোড অন/অফ টগল, নোটিফিকেশন অগ্রাধিকার নির্ধারণ ও পাসওয়ার্ড পরিবর্তনের সুবিধা।
  - মসজিদের অফিসিয়াল তথ্য, কমিটির তালিকা, প্রতিষ্ঠা সন ও যোগাযোগ সমন্বয় স্ক্রিন।
- **রিলিজ ও CI/CD ওয়ার্কফ্লো অপ্টিমাইজেশন (`.github/workflows/release.yml`)**:
  - `google-services.json` ও সাইনিং কনফিগারেশনে ফলব্যাক ব্যবস্থাপনা যোগ যাতে অটোমেটেড রিলিজ পাইপলাইন নিরবচ্ছিন্নভাবে সম্পন্ন হয়।

## [1.0.8] - 2026-11-12
### Added & Enhanced (Dark Mode, Firebase App Check & 3D Islamic Branding)
- **সম্পূর্ণ ডার্ক মোড সাপোর্ট (Full Dark Theme Support - `Theme.kt`, `Color.kt`)**:
  - ইসলামিক এমারেল্ড-ডার্ক কালার প্যালেট (`MosqueDarkEmeraldPrimary`, `MosqueDarkSurface`, `MosqueDarkBackground`)।
  - কম আলোতে চোখ সুরক্ষিত রাখতে প্রতিটি স্ক্রিন, মেহরাব কার্ড এবং ডায়ালগে হাই-কন্ট্রাস্ট ও নান্দনিক গোল্ডেন অ্যাকসেন্ট সমন্বয়।
  - `MosqueViewModel` এ `darkModeEnabled` স্টেটফ্লো ইন্টিগ্রেশন এবং সেশন অনুসারে ডিভাইস মেমরিতে সেভ থাকার সুবিধা।
- **ফায়ারবেস অ্যাপ চেক ও ব্যাকএন্ড সিকিউরিটি গার্ড (`AppCheckHelper.kt`)**:
  - Firebase App Check এর সাথে reCAPTCHA Enterprise ইন্টিগ্রেশন।
  - অননুমোদিত API রিকোয়েস্ট ও ক্লাউড ডেটাবেজ অ্যাক্সেস সম্পূর্ণ প্রতিরোধ।
  - ডিবাগ ও প্রোডাকশন বিল্ডের জন্য আলাদা নিরাপত্তা আর্কিটেকচার।
- **অফিসিয়াল ৩ডি গোল্ড ও এমারেল্ড ব্র্যান্ডিং আর্টওয়ার্ক (`design/branding/`, `res/mipmap-*`)**:
  - ইসলামিক ভাবগাম্ভীর্যপূর্ণ ৩ডি ভেক্টর লোগো (`official_logo.png`) ও ৫১২×৫১২ হাই-রেজোলিউশন লঞ্চার আইকন (`app_icon_512.png`)।
  - গুগল প্লেস্টোরের জন্য অফিশিয়াল ফিচার ব্যানার (`banner_playstore.png`)।
  - অ্যান্ড্রয়েড ৮.০+ ডিভাইসের জন্য ভেক্টর অ্যাডাপটিভ আইকন ব্যাকগ্রাউন্ড (`ic_launcher.xml`, `ic_launcher_round.xml`)।
- **ডেটাবেজ ও পারসিসটেন্স সম্প্রসারণ (`DatabaseInitializer.kt`, `Daos.kt`, `Entities.kt`)**:
  - ক্যাশবক্স রেকর্ডস (`CashboxEntity`), অফিসিয়াল ডকুমেন্টস (`OfficeDocumentEntity`) এবং অ্যাডভাইজরি নোটস (`AdvisoryNoteEntity`) এর জন্য নতুন টেবিল ও ডাও যুক্ত।

## [1.0.7] - 2026-11-09
### Added & Redesigned (Complete Islamic Mehrab Architecture & Distinct Role Interfaces)
- **আধুনিক ইসলামিক মেহরাব ও অ্যারাবেস্ক ডিজাইন সিস্টেম (`IslamicDecorations.kt`, `Color.kt`)**:
  - মেহরাব আর্চ কার্ড (`MehrabArchCard`): খাঁটি ইসলামিক আর্কিটেকচারাল মেহরাব হেডারের আদলে ২৪ডিপি কার্ভ এবং রয়্যাল গোল্ড ট্রিম গ্র্যাডিয়েন্ট ডিভাইডার।
  - হিজরি ক্যালেন্ডার ও বিসমিল্লাহ হেডার ব্যানার (`HijriDateBanner`): ইসলামিক সানচুয়ারি সবুজ ও স্বর্ণালী ফ্রেমে আরবি বিসমিল্লাহ ক্যালিগ্রাফি এবং চলমান হিজরি ও গ্রেগরিয়ান তারিখ প্রদর্শনী।
  - রুব এল হিজব স্টার মোটিফ (`RubElHizbDivider` - ۞): সোনালী জ্যামিতিক ইসলামিক ডিভাইডার।
  - প্রিমিয়াম ইসলামিক কালার প্যালেট: রিচ এমারেল্ড (`#064E3B`), সানচুয়ারি ডিপ গ্রিন (`#022C22`), রয়্যাল ডোম গোল্ড (`#F59E0B`), ব্রোঞ্জ ও ইসলামিক ওয়ার্ম পার্চমেন্ট আইভরি (`#FBF9F5`)।
- **প্রতিটি পদের জন্য আলাদা ও স্বতন্ত্র ইন্টারফেস (Distinct Role Interfaces)**:
  - **সভাপতি (President)**: নির্বাহী সুপ্রিম কন্ট্রোল রুম, ১-ক্লিক ভাউচার অনুমোদন ও বাতিল হটলাইন (`PresidentPendingVouchersCard`), অডিট নিরীক্ষা ও কমিটি সমন্বয়।
  - **কোষাধ্যক্ষ (Treasurer)**: বায়তুল মাল ক্যাশ ও ব্যাংক খতিয়ান, তাৎক্ষণিক আয়-ব্যয় ভাউচার ও চাঁদা রেজিস্টার ম্যানেজমেন্ট।
  - **সাধারণ সম্পাদক (Secretariat)**: প্রশাসনিক সচিবালয় ও রেজুলেশন সেল, কার্যনির্বাহী সভার নোটিশ জারি ও সদস্য পরিষদ ডিরেক্টরি।
  - **পেশ ইমাম ও খাদেম (Religious Staff)**: মেহরাব খেদমত ও নূরানী তালিম পোর্টাল, ৫ ওয়াক্ত নামাজের জামাত রোস্টার ও খাবার শিডিউল।
  - **সাধারণ মুসুল্লি (Musalli)**: আধ্যাত্মিক পোর্টাল, আজকের নির্বাচিত আয়াত ও হাদিস অনুপ্রেরণা কার্ড (`MusalliDevotionalCard`), জামাত কাউন্টডাউন এবং তাৎক্ষণিক ডিজিটাল কিউআর রসিদ ভিউয়ার।
- **১০০% রিয়েলিস্টিক প্রোডাকশন-রেডি অভিজ্ঞতা (Zero 'Demo' Labeling)**:
  - অ্যাপের কোথাও কোনো "ডেমো" বা খেলনা ট্যাগ নেই; সম্পূর্ণ আসল প্রোডাকশন-গ্রেড এন্টারপ্রাইজ সিস্টেমের লুক অ্যান্ড ফিল।
  - যেকোনো বৈধ বাংলাদেশি মোবাইল নম্বর দিয়ে স্মুথ লগইন এবং রুম ডাটাবেজ (Room DB) ভিত্তিক স্থায়ী ডাটা স্টোরেজ।
  - অ্যাপের অভ্যন্তরীণ গ্লোবাল হেডার থেকে "মসজিদ পরিচালনা পর্ষদ (কমিটি পদবি পরিবর্তন)" মেন্যুর মাধ্যমে প্রতিটি পদের বাস্তব অভিজ্ঞতা গ্রহণের সুবিধা।

## [1.0.6] - 2026-11-09
### Added & Production-Ready (Splash, Auth & Multi-Step Registration)
- **Islamic Modern Animated Splash Screen (`SplashScreen.kt`)**:
  - ডিপ ইসলামিক এমারেল্ড গ্র্যাডিয়েন্ট ব্যাকগ্রাউন্ড, ৩ডি এম্বলেম লোগোর স্মুথ স্কেল ও আলফা এন্ট্রি অ্যানিমেশন।
  - গোল্ডেন শাইন রিং, বিসমিল্লাহ ক্যালিগ্রাফি এবং পবিত্র কুরআনের আয়াত উদ্ধৃতি (*"নিশ্চয়ই মসজিদসমূহ আল্লাহরই জন্য" - সূরা আল-জিন: ১৮*)।
  - অফিসিয়াল স্লোগান ও সংস্করণ ব্যাজ সহ স্বয়ংক্রিয় ২.২ সেকেন্ড টাইমার শেষে সেশন যাচাইপূর্বক সরাসরি ড্যাশবোর্ড বা লগইন স্ক্রিনে ট্রানজিশন।
- **প্রডাকশন-গ্রেড লগইন স্ক্রিন (`LoginScreen.kt`)**:
  - ডুয়েল মোড ট্যাব: "কমিটি ও স্টাফ প্রবেশ" (পাসওয়ার্ড প্রটেক্টেড) এবং "মুসুল্লি প্রবেশ" (পাসওয়ার্ডহীন উন্মুক্ত সেবা পোর্টাল)।
  - কমিটি আইডি, ১১ ডিজিটের বাংলাদেশি মোবাইল ভ্যালিডেশন, শো/হাইড পাসওয়ার্ড টগল এবং "আমাকে মনে রাখুন" (Remember Me) সেশন সেভ।
  - এক ক্লিকে টেস্টিংয়ের জন্য কুইক ডেমো রোল চিপস (👑 সভাপতি, 📝 সাধারণ সম্পাদক, 💰 কোষাধ্যক্ষ, 🕌 পেশ ইমাম, 👥 সাধারণ মুসুল্লি)।
  - বিস্তারিত লগইন সহায়তা ও ডেমো ক্রেডেনশিয়াল ডায়ালগ।
- **ডেডিকেটেড ৩-ধাপ বিশিষ্ট রেজিস্ট্রেশন উইজার্ড (`RegistrationScreen.kt`)**:
  - **ধাপ ১ (সভাপতির তথ্য):** পুরো নাম (বাংলা/ইংরেজি), মোবাইল, ইমেইল, NID ও পাসওয়ার্ড নিশ্চিতকরণ।
  - **ধাপ ২ (মসজিদ বিবরণ):** মসজিদের নাম, পূর্ণ ঠিকানা, প্রতিষ্ঠা সাল ও কমিটির মেয়াদকাল।
  - **ধাপ ৩ (যাচাই ও সম্মতি):** সারসংক্ষেপ রিভিউ এবং শর্তাবলীতে সম্মতি দিয়ে এক ক্লিকে ডাটাবেজে সংরক্ষণ।
  - নতুন মসজিদ ও সভাপতি এন্ট্রি সফল হলে তাৎক্ষণিকভাবে সভাপতি হিসেবে লগইন করে ড্যাশবোর্ডে প্রবেশের সুবিধা।
- **সেশন পারসিসটেন্স ও সিকিউর সেশন ম্যানেজার (`SessionManager.kt`)**:
  - `SharedPreferences` ব্যবহার করে লগইন সেশন, সক্রিয় রোল ও ইউজারের তথ্য ডিভাইস মেমরিতে নিরাপদে সংরক্ষণ।
  - গ্লোবাল হেডার মেন্যু থেকে "🚪 লগআউট করুন" ট্যাপ করলে সেশন ক্লিয়ার হয়ে মসৃণভাবে লগইন স্ক্রিনে প্রত্যাবর্তন।
- **সিস্টেম ব্যাক হ্যান্ডলার ও ফ্রেম-পারফেক্ট অ্যানিমেশন**:
  - Jetpack Compose `BackHandler` ইন্টিগ্রেশন এবং `AnimatedContent` ট্রানজিশন (Splash -> Login <-> Registration -> Main)।

## [1.0.5] - 2026-11-09
### Added & Refined (Role-Responsibility Isolation & Security)
- **Strict Role-Responsibility Feature Isolation (কমিটি ও স্টাফদের দায়িত্ব অনুযায়ী ফিচার বিভাজন)**:
  - **কোষাধ্যক্ষ (Treasurer)**:
    - সম্পূর্ণ অর্থ ব্যবস্থাপনা কেন্দ্রীভূত: আয় এন্ট্রি, ব্যয় ভাউচার তৈরি, নিয়মিত মাসিক চাঁদা আদায়/রসিদ প্রদান, স্টাফ বেতন ও ভাতা প্রদান, মাদরাসা ফি কালেকশন, এবং আর্থিক অডিট নিরীক্ষা রিপোর্ট।
    - কোষাধ্যক্ষের ইন্টারফেস থেকে অপপ্রাসঙ্গিক ফিচার (যেমন: মিটিং ডাকা, হিফজ মূল্যায়ন, খাবার শিডিউল, মেম্বার অ্যাড ইত্যাদি) সম্পূর্ণ অপসারণ।
  - **সাধারণ সম্পাদক ও সাংগঠনিক সম্পাদক (Secretariat)**:
    - সার্বিক প্রশাসনিক সচিবালয় ও সমন্বয়: নতুন সদস্য নিবন্ধন, জরুরি ও মাসিক কার্যনির্বাহী সভা আহ্বান ও কার্যবিবরণী লিপিবদ্ধকরণ, ডিজিটাল নোটিশ জারি ও AI ড্রাফটিং, সমাজকল্যাণ ও যাকাত আবেদন গ্রহণ এবং দ্বীনি মাহফিল/ইভেন্ট ব্যবস্থাপনা।
    - সচিবালয়ের ইন্টারফেস থেকে ক্যাশ হ্যান্ডলিং ও ভাউচার পেমেন্ট অপশন বাদ দিয়ে প্রশাসনিক শৃঙ্খলায় কেন্দ্রীভূত।
  - **সভাপতি ও সহ-সভাপতি (President & Vice President)**:
    - নির্বাহী তত্ত্বাবধান ও চূড়ান্ত অনুমোদন: কেন্দ্রীয় ড্যাশবোর্ড কেপিআই, পেন্ডিং ব্যয় ভাউচার অনুমোদন/বাতিল, যাকাত ও সমাজকল্যাণ অনুদান মঞ্জুরি, সভা আহ্বান ও রেজুলেশন অনুমোদন, এবং কেন্দ্রীয় অডিট নিরীক্ষা।
  - **ইমাম, মুয়াজ্জিন ও খাদেম (Religious Staff)**:
    - ধর্মীয় খেদমত, শিক্ষার্থী তালিম ও ব্যক্তিগত প্রাপ্তি: ৫ ওয়াক্ত নামাজের সঠিক সূচি, ব্যক্তিগত ওয়াক্ত ডিউটি রোস্টার, খাবার প্রদানকারী পরিবার তালিকা, মাদরাসা শিক্ষার্থীদের হিফজ ও মক্তব মূল্যায়ন (সবক এন্ট্রি), এবং ব্যক্তিগত বেতন ও ভাতা স্লিপ।
    - তহবিলের ক্যাশ ব্যালেন্স ও ডোনার ডাটাবেস থেকে নিরাপদ দূরত্ব বজায় রাখা হয়েছে।
  - **সাধারণ সদস্য ও মুসুল্লি (General Member / Musalli)**:
    - মুসুল্লি পোর্টাল ও স্বচ্ছ তহবিল: ওয়াক্ত সময় ও জামাত ট্র্যাকিং, ব্যক্তিগত চাঁদা প্রদানের ইতিহাস ও ডিজিটাল ভেরিফায়েড QR রসিদ, মসজিদের উন্মুক্ত মোট আয়-ব্যয় সারাংশ ও সার্বিক স্থিতি, নোটিশ বোর্ড, কমিটি ডিরেক্টরি এবং আপৎকালীন সমাজকল্যাণ আবেদন।
- **Architecture & Navigation Improvements**:
  - `overlaySubModule` আর্কিটেকচার: যেকোনো ডিপ সাব-মডিউল (নামাজের সূচি, অডিট লগ, ইভেন্ট মাহফিল, মাদরাসা ফি) বটম বার স্টেট অক্ষুণ্ণ রেখে ফুল-স্ক্রিন ওভারলে এবং ব্যাক বাটন দিয়ে সহজে ব্যবহারের সুবিধা।
  - কঠোর এক্সেস কন্ট্রোল গার্ডস: মেম্বার অ্যাড, আয়/ব্যয় এন্ট্রি, চাঁদা গ্রহণ, সভা আহ্বান, নোটিশ প্রকাশের প্রতিটি ডায়ালগ ও বাটন রোল-পারমিশন দ্বারা সুরক্ষিত।

## [1.0.4] - 2026-11-09
### Improved & Optimized (UI & Performance)
- **Islamic Modern Visual Refinement**:
  - Implemented multi-stop Islamic emerald gradients (`MosqueEmeraldGradient`, `MosqueEmeraldDarkGradient`) and gold metallic accents.
  - Added subtle card borders (`MosqueCardBorderSubtle`) and elevated depth (`elevation = 2.dp` / `4.dp`, `RoundedCornerShape(16.dp)` / `18.dp` / `20.dp`).
  - Polished Global Header with rich vertical gradient, gold trim accent divider, and refined logo container.
  - Upgraded HeroCard, Net Balance Card, and Prayer Waqt card with glowing next-waqt badges and gold callout banners.
- **Typography & Bengali Script Legibility**:
  - Optimized typography line-heights (24sp–38sp) to prevent clipping of complex Bengali diacritics and conjuncts.
- **Buttery-Smooth Tab Transitions & Elevation**:
  - Added `AnimatedContent` crossfade/slide transitions on bottom bar destination switches for 60/120fps smooth navigation.
  - Migrated KPI cards and Member cards to native `Card(onClick = ...)` for bounded ripple effects and unclipped drop shadows.
  - Added subtle top border and elevation to `NavigationBottomBar`.

## [1.0.3] - 2026-11-09
### Added
- **Dedicated Role-Based Interface Profiles (PRD v2.0)**:
  - **Treasurer (কোষাধ্যক্ষ)**: Distinct 5-tab financial command navigation (Dashboard, Income/Expense, Chanda Register, Staff Payroll & Madrasa Fees, Audit Reports). Custom cash/bank KPI and quick actions.
  - **General Secretary (সাধারণ সম্পাদক)**: Dedicated secretariat navigation (Dashboard, Member Council, Committee Meetings & Resolutions, Notice Board, Welfare Fund).
  - **President & Executive**: Administrative command center, voucher approval workflows, member directory, circulars and full mosque operations.
  - **Imam & Religious Staff**: Daily waqt duty roster, meal provider schedules, maktab/hifz progress tracking and salary status.
  - **General Member**: Musalli portal for personal chanda payment/history, verified digital QR receipts, transparent fund balance and notices.
- **Repository & DAO Enhancements**:
  - Implemented `getTransactionById` in `TransactionDao` and completed `approveTransaction` in `MosqueRepository`.
  - Generalized audit log actor attribution.

## [1.0.2] - 2026-10-29
### Added
- **Official 3D Launcher Icons & Brand Logo**:
  - Integrated official 3D emerald-and-gold emblem icon into `res/mipmap-*` (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`) and round variants.
  - Configured adaptive icon in `res/mipmap-anydpi-v26/ic_launcher.xml` with `@color/mosque_emerald_dark` background for Android 8.0+ devices.
  - Added official brand logo (`app_logo.png`) in `GlobalHeader.kt` and `LoginScreen.kt`.
- **Role-Based Dynamic Navigation & Interfaces (PRD v2.0)**:
  - **Admin (President, VP, Treasurer)**: Dashboard, Member Management, Financial Approvals & Vouchers, Circulars/Notices, Full Operations (all 11 sub-modules).
  - **Secretariat (General Secretary, Organizing Secretary, Executive)**: Dashboard, Member Directory, Notices & Committee Meetings, Welfare/Zakat Fund, Operations.
  - **Religious Staff (Imam, Muazzin, Khadem)**: Dashboard, Waqt Duty Roster, Food Schedule, Madrasa/Maktab, Staff Salary & Profile.
  - **General Member & Musalli**: Dashboard with Waqt countdown, My Chanda (Receipts & QR status), Transparent Financial Audit, Notice Board, Committee Directory.
- **Dynamic Context Routing**:
  - Auto-reset bottom tab index on role switch.
  - Dashboard Quick Actions and KPIs seamlessly navigate directly to their dedicated sub-module or action dialog.
  - Read-only transparency mode for General Member finance view with full audit report access.

## [1.0.1] - 2026-10-28
### Fixed
- **Critical Launch Crash (`ClassNotFoundException`)**: Added missing `alias(libs.plugins.kotlin.android)` to `build.gradle.kts` and `app/build.gradle.kts`, enabling full Kotlin compilation for all 25 source files and Room DAOs.
- **Bytecode Compatibility**: Aligned JVM bytecode target to `Java 17` (`JavaVersion.VERSION_17` and `jvmTarget = "17"`) for seamless ART compatibility across Android versions.
- **Workflow Automation**: Made release asset and release name dynamic based on tag name.

## [1.0.0] - 2026-10-28
### Added
- **Official Branding**: Set app name to "মসজিদ সেবা (Mosque Seba)" and package name to `com.mosqueseba.app`.
- **Slogan**: Added official slogan: *"মসজিদের খেদমতে আধুনিক প্রযুক্তি, পরিচালনায় স্বচ্ছ আস্থা"*.
- **Branding Assets**: Integrated official vector logo, 512x512 app icon, and Google Play Store feature banner into `design/branding/`.
- **Madrasa & Maktab Module** (`MadrasaScreen.kt`): Student directory, Hifz & Quran tracker with star ratings, Fee collection with auto-posting to Finance, and Guardian Portal.
- **Staff Operations & Payroll**: Staff directory, salary disbursement with auto-posting to Finance, 5-waqt Duty Roster, and Khadem/Imam Food Schedule.
- **Welfare & Zakat Fund**: Case assistance tracker for medical, education, widow/orphan, and food relief grants.
- **Events & Programs**: Waz Mahfil, Milad, Shab-e-Qadr, and Iftar program manager with speaker, venue, and budget tracking.
- **Committee Meetings**: Scheduled meetings directory, agendas, and resolutions viewer.
- **AI Notice Drafter**: 1-click Gemini AI powered notice drafting within Publish Notice dialog.
- **Financial Reports**: Monthly income/expense summary and shareable report dialog.
- **Authentication**: Committee ID login, 2-step President/Mosque onboarding, and Header logout.
- **Database Architecture**: Upgraded Room Database to version 3 with 17 entities and comprehensive seed data.
