import 'committee_tier.dart';

/// Complete 16-Position Committee Role Catalog (Enums.kt Parity)
enum CommitteeRole {
  president(
    code: 'president',
    titleBn: 'সভাপতি',
    level: 1,
    tier: CommitteeTier.coreExecutive,
    shortSummaryBn: 'মসজিদের সর্বস্তরের অভিভাবক, সভায় সভাপতিত্ব ও চূড়ান্ত অনুমোদন',
    dutiesBn: [
      'মসজিদের সর্বস্তরের অভিভাবক হিসেবে দায়িত্ব পালন করা।',
      'কমিটির সকল সাধারণ ও জরুরি সভায় সভাপতিত্ব করা।',
      'মসজিদের উন্নয়নমূলক ও প্রশাসনিক কাজের চূড়ান্ত সিদ্ধান্ত অনুমোদন করা।',
      'কমিটির অভ্যন্তরীণ যেকোনো মতবিরোধ বা সমস্যার সমাধান করা।',
    ],
  ),
  vicePresident(
    code: 'vice_president',
    titleBn: 'সহ-সভাপতি',
    level: 2,
    tier: CommitteeTier.coreExecutive,
    shortSummaryBn: 'সভাপতির অনুপস্থিতিতে দায়িত্ব পালন ও উপ-কমিটি তদারকি',
    dutiesBn: [
      'সভাপতির অনুপস্থিতিতে তার সকল দায়িত্ব ও কর্তব্য পালন করা।',
      'সভাপতির নির্দেশ অনুযায়ী বিশেষ উপ-কমিটি ও উন্নয়ন কাজের তদারকি করা।',
    ],
  ),
  generalSecretary(
    code: 'general_secretary',
    titleBn: 'সাধারণ সম্পাদক / সেক্রেটারি',
    level: 2,
    tier: CommitteeTier.coreExecutive,
    shortSummaryBn: 'দৈনন্দিন প্রশাসন, সভা আহ্বান, এজেন্ডা-রেজুলেশন ও স্টাফ তদারকি',
    dutiesBn: [
      'মসজিদের দৈনন্দিন প্রশাসনিক ও সাংগঠনিক কাজ পরিচালনা করা।',
      'সভাপতির সাথে পরামর্শ করে কমিটির সাধারণ বা জরুরি সভা আহ্বান করা।',
      'সভার এজেন্ডা তৈরি ও রেজুলেশন খাতায় লিপিবদ্ধ করা।',
      'ইমাম, মুয়াজ্জিন ও খাদেমদের কাজের তদারকি করা।',
    ],
  ),
  assistantSecretary(
    code: 'assistant_secretary',
    titleBn: 'সহ-সাধারণ সম্পাদক',
    level: 3,
    tier: CommitteeTier.coreExecutive,
    shortSummaryBn: 'সাধারণ সম্পাদকের কাজে সার্বিক সহায়তা প্রদান',
    dutiesBn: [
      'সাধারণ সম্পাদকের অনুপস্থিতিতে তার দায়িত্ব পালন করা।',
      'দাপ্তরিক কার্যক্রমে সার্বিক সহায়তা প্রদান করা।',
    ],
  ),
  treasurer(
    code: 'treasurer',
    titleBn: 'কোষাধ্যক্ষ / অর্থ সম্পাদক',
    level: 2,
    tier: CommitteeTier.coreExecutive,
    shortSummaryBn: 'আয়-ব্যয়ের হিসাব, চাঁদা আদায় ও ব্যাংকিং স্বচ্ছতা',
    dutiesBn: [
      'মসজিদের আয়-ব্যয়ের সঠিক ও স্বচ্ছ হিসাব সংরক্ষণ করা।',
      'চাঁদা, দান ও ভাড়ার টাকা সংগ্রহ করে ব্যাংক একাউন্টে জমা রাখা।',
      'মাসিক ও বাৎসরিক আয়-ব্যয়ের প্রতিবেদন তৈরি করে পেশ করা।',
    ],
  ),
  assistantTreasurer(
    code: 'assistant_treasurer',
    titleBn: 'সহ-অর্থ সম্পাদক',
    level: 3,
    tier: CommitteeTier.coreExecutive,
    shortSummaryBn: 'অর্থ সম্পাদকের সার্বিক আর্থিক কাজে সহায়তা প্রদান',
    dutiesBn: [
      'অর্থ সম্পাদকের সাথে সমন্বয় করে চাঁদা ও দান সংগ্রহ করা।',
      'ক্যাশবক্স গণনা ও হিসাব সংরক্ষণে সহায়তা করা।',
    ],
  ),
  organizingSecretary(
    code: 'organizing_secretary',
    titleBn: 'সাংগঠনিক সম্পাদক',
    level: 3,
    tier: CommitteeTier.secretarial,
    shortSummaryBn: 'সাংগঠনিক শৃঙ্খলা, সদস্য যোগাযোগ ও ধর্মীয় অনুষ্ঠান সমন্বয়',
    dutiesBn: [
      'মসজিদের সদস্য ও স্থানীয় মুসল্লিদের ঐক্যবদ্ধ রাখা।',
      'ওয়াজ মাহফিল, তাফসির ও বিশেষ ধর্মীয় অনুষ্ঠান সফল করা।',
    ],
  ),
  publicitySecretary(
    code: 'publicity_secretary',
    titleBn: 'প্রচার ও প্রকাশনা সম্পাদক',
    level: 3,
    tier: CommitteeTier.secretarial,
    shortSummaryBn: 'বিজ্ঞপ্তি প্রচার, স্মার্ট বোর্ড ও প্রকাশনা ব্যবস্থাপনা',
    dutiesBn: [
      'মসজিদের নোটিশ, ক্যালেন্ডার ও নামাজের সময়সূচি প্রচার করা।',
      'ডিজিটাল স্মার্ট বোর্ড ও দেয়াল উক্তি পরিচালনা করা।',
    ],
  ),
  officeSecretary(
    code: 'office_secretary',
    titleBn: 'দপ্তর সম্পাদক',
    level: 3,
    tier: CommitteeTier.secretarial,
    shortSummaryBn: 'দাপ্তরিক ফাইল, রেজুলেশন খাতা ও ওয়াকফ দলিল সংরক্ষণ',
    dutiesBn: [
      'মসজিদের সকল অফিসিয়াল নথিপত্র ও ওয়াকফ দলিল সংরক্ষণ করা।',
      'সভার রেজুলেশন খাতা এবং চিঠিপত্রের কপি ফাইলিং করা।',
    ],
  ),
  welfareEducationSecretary(
    code: 'welfare_education_secretary',
    titleBn: 'সমাজকল্যাণ ও ধর্মীয় শিক্ষা সম্পাদক',
    level: 3,
    tier: CommitteeTier.secretarial,
    shortSummaryBn: 'মক্তব/মাদরাসা তদারকি, যাকাত ও সমাজসেবামূলক সহায়তা',
    dutiesBn: [
      'মসজিদভিত্তিক মক্তব ও হিফজখানার শিক্ষা কার্যক্রম দেখাশোনা করা।',
      'যাকাত ও সমাজকল্যাণ তহবিল সংগ্রহ এবং বণ্টন তদারকি করা।',
    ],
  ),
  executiveMember(
    code: 'executive_member',
    titleBn: 'সাধারণ সদস্য (কার্যনির্বাহী)',
    level: 4,
    tier: CommitteeTier.generalAdvisory,
    shortSummaryBn: 'কমিটির সভায় অংশগ্রহণ ও ক্যাশবক্সের নিরপেক্ষ সাক্ষী',
    dutiesBn: [
      'কমিটির সকল সভায় নিয়মিত উপস্থিত থেকে মতামত প্রদান করা।',
      'জুমা ও ঈদের জামাতে শৃঙ্খলা রক্ষা এবং ক্যাশবক্স গণনায় সাক্ষী থাকা।',
    ],
  ),
  imam(
    code: 'imam',
    titleBn: 'পদাধিকারবলে সদস্য (খতিব/ইমাম)',
    level: 3,
    tier: CommitteeTier.generalAdvisory,
    shortSummaryBn: 'ইমামতি, জুমার খুতবা, শরিয়াহ নির্দেশনা ও দ্বীনি তালিম',
    dutiesBn: [
      'পাঁচ ওয়াক্ত নামাজ ও জুমার নামাজে ইমামতি করা।',
      'মসজিদ কমিটির সিদ্ধান্ত ও উন্নয়ন পরিকল্পনায় শরিয়াহ পরামর্শ দেওয়া।',
    ],
  ),
  advisoryBoard(
    code: 'advisory_board',
    titleBn: 'উপদেষ্টা পরিষদ',
    level: 2,
    tier: CommitteeTier.generalAdvisory,
    shortSummaryBn: 'কৌশলগত দিকনির্দেশনা ও সংকটকালীন সমাধান',
    dutiesBn: [
      'মসজিদের উন্নয়ন ও পরিচালনায় অভিজ্ঞতামূলক পরামর্শ দেওয়া।',
      'কমিটির যেকোনো বিতর্ক বা মতবিরোধ নিরসনে ভূমিকা রাখা।',
    ],
  ),
  muazzin(
    code: 'muazzin',
    titleBn: 'মুয়াজ্জিন',
    level: 5,
    tier: CommitteeTier.staffMusalli,
    shortSummaryBn: 'যথাসময়ে আজান, নামাজের ইকামত ও ওয়াক্ত পরিচালনা',
    dutiesBn: [
      'পাঁচ ওয়াক্ত ও জুমার নামাজের সঠিক সময়ে আজান দেওয়া।',
      'নামাজের পূর্বে মেহরাব ও মাইক প্রস্তুত রাখা।',
    ],
  ),
  khadem(
    code: 'khadem',
    titleBn: 'খাদেম',
    level: 5,
    tier: CommitteeTier.staffMusalli,
    shortSummaryBn: 'মসজিদ ক্যাম্পাস ও অজুখানার পরিচ্ছন্নতা নিশ্চিতকরণ',
    dutiesBn: [
      'মসজিদের অভ্যন্তর ও অজুখানা সবসময় পরিষ্কার-পরিচ্ছন্ন রাখা।',
      'মসজিদের বাতি, ফ্যান ও পানির সুব্যবস্থা নিশ্চিত করা।',
    ],
  ),
  generalMember(
    code: 'general_member',
    titleBn: 'সাধারণ সদস্য / মুসল্লি',
    level: 5,
    tier: CommitteeTier.staffMusalli,
    shortSummaryBn: 'জামাতে নামাজ আদায়, নিয়মিত চাঁদা প্রদান ও মতবিনিময়',
    dutiesBn: [
      'নিয়মিত মসজিদে এসে জামাতে নামাজ আদায় করা।',
      'মসজিদের মাসিক চাঁদা ও অনুদান প্রদান করা।',
    ],
  );

  final String code;
  final String titleBn;
  final int level;
  final CommitteeTier tier;
  final String shortSummaryBn;
  final List<String> dutiesBn;

  const CommitteeRole({
    required this.code,
    required this.titleBn,
    required this.level,
    required this.tier,
    required this.shortSummaryBn,
    required this.dutiesBn,
  });

  // Action Permissions Parity
  bool canApproveFinance() => this == president || this == vicePresident;
  bool canManageFinance() =>
      this == president || this == vicePresident || this == treasurer || this == assistantTreasurer;
  bool canManageMembers() =>
      this == president || this == vicePresident || this == generalSecretary || this == assistantSecretary || this == organizingSecretary;
  bool canScheduleMeetings() =>
      this == president || this == vicePresident || this == generalSecretary || this == assistantSecretary || this == organizingSecretary;
  bool canPublishNotices() =>
      this == president || this == vicePresident || this == generalSecretary || this == assistantSecretary || this == organizingSecretary || this == publicitySecretary;
  bool canManageSmartBoard() =>
      this == president || this == generalSecretary || this == publicitySecretary;
  bool canManageOfficeDocs() =>
      this == president || this == vicePresident || this == generalSecretary || this == assistantSecretary || this == officeSecretary;
  bool canManageCashbox() =>
      this == president || this == vicePresident || this == treasurer || this == assistantTreasurer;
  bool canWitnessCashbox() =>
      this == executiveMember || this == president || this == vicePresident || this == generalSecretary || this == imam;
  bool canSubmitShariaReview() => this == imam;
  bool canSubmitAdvisoryGuidance() => this == advisoryBoard;
  bool canManageWelfareEducation() =>
      this == president || this == vicePresident || this == generalSecretary || this == welfareEducationSecretary || this == imam;
  bool canEditMosqueProfile() =>
      this == president || this == vicePresident || this == generalSecretary;

  static CommitteeRole fromCode(String? code) {
    if (code == null || code.isEmpty) return CommitteeRole.generalMember;
    for (final role in CommitteeRole.values) {
      if (role.code.toLowerCase() == code.toLowerCase().trim()) {
        return role;
      }
    }
    return CommitteeRole.generalMember;
  }
}
