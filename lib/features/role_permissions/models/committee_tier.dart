/// Committee Tiers for Mosque Governance Hierarchy (Enums.kt Parity)
enum CommitteeTier {
  coreExecutive(
    titleBn: '১. মূল কার্যনির্বাহী পর্ষদ',
    subtitleBn: 'মসজিদের সর্বোচ্চ অভিভাবকত্ব, নীতিনির্ধারণ, প্রশাসন ও তহবিল নিরাপত্তা',
  ),
  secretarial(
    titleBn: '২. সম্পাদকীয় পর্ষদ',
    subtitleBn: 'সাংগঠনিক, প্রচার ও প্রকাশনা, দপ্তর এবং সমাজকল্যাণ ও ধর্মীয় শিক্ষা',
  ),
  generalAdvisory(
    titleBn: '৩. সাধারণ সদস্য, পদাধিকারবলে ও উপদেষ্টা',
    subtitleBn: 'কার্যনির্বাহী সদস্য, পদাধিকারবলে খতিব/ইমাম এবং সম্মানিত উপদেষ্টা পরিষদ',
  ),
  staffMusalli(
    titleBn: '৪. মসজিদ খেদমত ও মুসল্লি',
    subtitleBn: 'মুয়াজ্জিন, খাদেম ও সাধারণ মুসল্লি সেবা পোর্টাল',
  );

  final String titleBn;
  final String subtitleBn;

  const CommitteeTier({
    required this.titleBn,
    required this.subtitleBn,
  });
}
