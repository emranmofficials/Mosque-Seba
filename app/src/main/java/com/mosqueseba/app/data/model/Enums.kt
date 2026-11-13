package com.mosqueseba.app.data.model

enum class CommitteeTier(
    val titleBn: String,
    val subtitleBn: String
) {
    CORE_EXECUTIVE(
        titleBn = "১. মূল কার্যনির্বাহী পর্ষদ",
        subtitleBn = "মসজিদের সর্বোচ্চ অভিভাবকত্ব, নীতিনির্ধারণ, প্রশাসন ও তহবিল নিরাপত্তা"
    ),
    SECRETARIAL(
        titleBn = "২. সম্পাদকীয় পর্ষদ",
        subtitleBn = "সাংগঠনিক, প্রচার ও প্রকাশনা, দপ্তর এবং সমাজকল্যাণ ও ধর্মীয় শিক্ষা"
    ),
    GENERAL_ADVISORY(
        titleBn = "৩. সাধারণ সদস্য, পদাধিকারবলে ও উপদেষ্টা",
        subtitleBn = "কার্যনির্বাহী সদস্য, পদাধিকারবলে খতিব/ইমাম এবং সম্মানিত উপদেষ্টা পরিষদ"
    ),
    STAFF_MUSALLI(
        titleBn = "৪. মসজিদ খেদমত ও মুসল্লি",
        subtitleBn = "মুয়াজ্জিন, খাদেম ও সাধারণ মুসল্লি সেবা পোর্টাল"
    )
}

enum class UserRole(
    val code: String,
    val titleBn: String,
    val level: Int,
    val tier: CommitteeTier,
    val emoji: String,
    val shortSummaryBn: String,
    val dutiesBn: List<String>
) {
    PRESIDENT(
        code = "president",
        titleBn = "সভাপতি",
        level = 1,
        tier = CommitteeTier.CORE_EXECUTIVE,
        emoji = "👑",
        shortSummaryBn = "মসজিদের সর্বস্তরের অভিভাবক, সভায় সভাপতিত্ব ও চূড়ান্ত অনুমোদন",
        dutiesBn = listOf(
            "মসজিদের সর্বস্তরের অভিভাবক হিসেবে দায়িত্ব পালন করা।",
            "কমিটির সকল সাধারণ ও জরুরি সভায় সভাপতিত্ব করা।",
            "মসজিদের উন্নয়নমূলক ও প্রশাসনিক কাজের চূড়ান্ত সিদ্ধান্ত অনুমোদন করা।",
            "কমিটির অভ্যন্তরীণ যেকোনো মতবিরোধ বা সমস্যার সমাধান করা।"
        )
    ),
    VICE_PRESIDENT(
        code = "vice_president",
        titleBn = "সহ-সভাপতি",
        level = 2,
        tier = CommitteeTier.CORE_EXECUTIVE,
        emoji = "🎖️",
        shortSummaryBn = "সভাপতির অনুপস্থিতিতে দায়িত্ব পালন ও উপ-কমিটি তদারকি",
        dutiesBn = listOf(
            "সভাপতির অনুপস্থিতিতে তার সকল দায়িত্ব ও কর্তব্য পালন করা।",
            "সভাপতির নির্দেশ অনুযায়ী মসজিদের বিশেষ উপ-কমিটি ও উন্নয়ন কাজের তদারকি করা।"
        )
    ),
    GENERAL_SECRETARY(
        code = "general_secretary",
        titleBn = "সাধারণ সম্পাদক",
        level = 2,
        tier = CommitteeTier.CORE_EXECUTIVE,
        emoji = "📝",
        shortSummaryBn = "দৈনন্দিন প্রশাসন, সভা আহ্বান, এজেন্ডা-রেজুলেশন ও স্টাফ তদারকি",
        dutiesBn = listOf(
            "মসজিদের দৈনন্দিন প্রশাসনিক ও সাংগঠনিক কাজ পরিচালনা করা।",
            "সভাপতির সাথে পরামর্শ করে কমিটির সাধারণ বা জরুরি সভা আহ্বান করা।",
            "সভার আলোচ্যসূচি (এজেন্ডা) তৈরি করা এবং সিদ্ধান্তসমূহ রেজুলেশন খাতায় লিপিবদ্ধ করা।",
            "ইমাম, মুয়াজ্জিন ও খাদেমদের কাজের তদারকি করা এবং তাদের সুযোগ-সুবিধা দেখাশোনা করা।"
        )
    ),
    ASSISTANT_SECRETARY(
        code = "assistant_secretary",
        titleBn = "সহ-সাধারণ সম্পাদক",
        level = 2,
        tier = CommitteeTier.CORE_EXECUTIVE,
        emoji = "🗂️",
        shortSummaryBn = "সাধারণ সম্পাদককে প্রশাসনিক সহায়তা ও অনুপস্থিতিতে দায়িত্ব পালন",
        dutiesBn = listOf(
            "সাধারণ সম্পাদককে তার দৈনন্দিন কাজে প্রশাসনিকভাবে সাহায্য করা।",
            "সাধারণ সম্পাদকের অনুপস্থিতিতে সাময়িকভাবে তার দায়িত্ব পালন করা।"
        )
    ),
    TREASURER(
        code = "treasurer",
        titleBn = "কোষাধ্যক্ষ / অর্থ সম্পাদক",
        level = 2,
        tier = CommitteeTier.CORE_EXECUTIVE,
        emoji = "💰",
        shortSummaryBn = "তহবিল নিরাপত্তা, ক্যাশবক্স গণনা, ব্যাংক জমা, ক্যাশবই ও মাসিক রিপোর্ট",
        dutiesBn = listOf(
            "মসজিদের তহবিলের সম্পূর্ণ হিসাব ও নিরাপত্তা নিশ্চিত করা।",
            "সাপ্তাহিক ও মাসিক দান বাক্স (ক্যাশবক্স) খোলার সময় উপস্থিত থেকে গণনার পর টাকা ব্যাংকে জমা করা।",
            "দৈনিক ও মাসিক আয়-ব্যয়ের সঠিক হিসাব ক্যাশ বইতে লিখে রাখা এবং ভাউচার সংরক্ষণ করা।",
            "প্রতি মাসে বা নির্দিষ্ট সময় পর পর কমিটির সভায় আর্থিক রিপোর্ট পেশ করা।"
        )
    ),
    ASSISTANT_TREASURER(
        code = "assistant_treasurer",
        titleBn = "সহ-অর্থ সম্পাদক",
        level = 2,
        tier = CommitteeTier.CORE_EXECUTIVE,
        emoji = "🏦",
        shortSummaryBn = "অর্থ সম্পাদককে হিসাব সংরক্ষণ ও ব্যাংকিং কার্যক্রমে সহায়তা",
        dutiesBn = listOf(
            "অর্থ সম্পাদককে আয়-ব্যয়ের হিসাব রাখা ও ভাউচার মিলিয়ে দেখতে সরাসরি সাহায্য করা।",
            "দান বাক্স গণনা ও ব্যাংকিং কার্যক্রমে অর্থ সম্পাদককে সহায়তা করা।"
        )
    ),
    ORGANIZING_SECRETARY(
        code = "organizing_secretary",
        titleBn = "সাংগঠনিক সম্পাদক",
        level = 3,
        tier = CommitteeTier.SECRETARIAL,
        emoji = "🤝",
        shortSummaryBn = "মুসল্লিদের ঐক্য-সৌহার্দ্য রক্ষা, সামাজিক যোগাযোগ ও মতামত সমন্বয়",
        dutiesBn = listOf(
            "মসজিদের মুসল্লিদের মধ্যে ঐক্য ও সৌহার্দ্য বজায় রাখার কাজ করা।",
            "মসজিদের আওতাধীন এলাকার সামাজিক যোগাযোগ বৃদ্ধি করা।",
            "সাধারণ মুসল্লিদের মতামত ও প্রস্তাবনা কমিটির কাছে তুলে ধরা।"
        )
    ),
    PUBLICITY_SECRETARY(
        code = "publicity_secretary",
        titleBn = "প্রচার ও প্রকাশনা সম্পাদক",
        level = 3,
        tier = CommitteeTier.SECRETARIAL,
        emoji = "📢",
        shortSummaryBn = "ধর্মীয় অনুষ্ঠান প্রচার, দেয়াল লিখন/উক্তি ও স্মার্ট নোটিশ বোর্ড পরিচালনা",
        dutiesBn = listOf(
            "ঈদে মিলাদুন্নবী, শবে বরাত, শবে কদর ও ওয়াজ মাহফিলসহ ধর্মীয় অনুষ্ঠান এবং জরুরি নোটিশ প্রচার করা।",
            "মসজিদের দেয়াল লিখন/উক্তি, দিকনির্দেশনা ও নোটিশ বোর্ড (স্মার্ট বোর্ড) পরিচালনা করা।"
        )
    ),
    OFFICE_SECRETARY(
        code = "office_secretary",
        titleBn = "দপ্তর সম্পাদক",
        level = 3,
        tier = CommitteeTier.SECRETARIAL,
        emoji = "🗄️",
        shortSummaryBn = "অফিসিয়াল নথিপত্র, দলিল-চুক্তি, চিঠিপত্র সংরক্ষণ ও জবাব তৈরি",
        dutiesBn = listOf(
            "মসজিদের অফিসিয়াল নথিপত্র, ওয়াকফ দলিল-চুক্তি এবং চিঠিপত্র নিরাপদে সংরক্ষণ করা।",
            "আগত চিঠিপত্রের জবাব তৈরি করা এবং অফিস ফাইল নিয়মিত রক্ষণাবেক্ষণ করা।"
        )
    ),
    WELFARE_EDUCATION_SECRETARY(
        code = "welfare_education_secretary",
        titleBn = "সমাজকল্যাণ ও ধর্মীয় শিক্ষা সম্পাদক",
        level = 3,
        tier = CommitteeTier.SECRETARIAL,
        emoji = "🎓",
        shortSummaryBn = "মক্তব/হিফজ শিক্ষা তদারকি, যাকাত বিতরণ, মেডিকেল ক্যাম্প ও ত্রাণ কার্যক্রম",
        dutiesBn = listOf(
            "মসজিদভিত্তিক মক্তব, হিফজ বা নূরানী মাদরাসার শিক্ষা কার্যক্রম তদারকি করা।",
            "দরিদ্রদের মাঝে যাকাত ও দান-সদকা বিতরণ তত্ত্বাবধান করা।",
            "ফ্রি মেডিকেল ক্যাম্প ও দুর্যোগকালীন ত্রাণ কার্যক্রম পরিচালনা করা।"
        )
    ),
    EXECUTIVE_MEMBER(
        code = "executive_member",
        titleBn = "কার্যনির্বাহী সদস্য",
        level = 3,
        tier = CommitteeTier.GENERAL_ADVISORY,
        emoji = "🛡️",
        shortSummaryBn = "সিদ্ধান্তে অংশগ্রহণ, জুমা-ঈদের শৃঙ্খলা ও ক্যাশবক্স/ক্রয়ে সাক্ষী",
        dutiesBn = listOf(
            "কমিটির বড় সিদ্ধান্ত ও উন্নয়নমূলক কাজে সক্রিয় অংশগ্রহণ করা।",
            "জুমা ও ঈদের জামাতে শৃঙ্খলা বজায় রাখতে এবং মুসল্লি ব্যবস্থাপনায় সহায়তা করা।",
            "মসজিদের দান বাক্স খোলা ও যেকোনো ক্রয়-বিক্রয়ে সাক্ষী ও সহায়তাকারী হিসেবে উপস্থিত থাকা।"
        )
    ),
    IMAM(
        code = "imam",
        titleBn = "পেশ ইমাম ও খতীব (পদাধিকারবলে)",
        level = 3,
        tier = CommitteeTier.GENERAL_ADVISORY,
        emoji = "🕌",
        shortSummaryBn = "পদাধিকারবলে সদস্য: শরিয়াহভিত্তিক পরামর্শ ও ইসলামিক নীতিমালা যাচাই",
        dutiesBn = listOf(
            "পদাধিকারবলে কমিটির সদস্য হিসেবে শরিয়াহভিত্তিক পরামর্শ প্রদান করা।",
            "কমিটির সকল সিদ্ধান্ত ইসলামিক নীতিমালার সাথে সামঞ্জস্যপূর্ণ কি না তা নিশ্চিত করা।",
            "৫ ওয়াক্ত নামাজের ইমামতি, জুমার খুতবা, তালিম ও মুসল্লিদের দ্বীনি দিকনির্দেশনা দেওয়া।"
        )
    ),
    ADVISORY_BOARD(
        code = "advisory_board",
        titleBn = "উপদেষ্টা পরিষদ",
        level = 2,
        tier = CommitteeTier.GENERAL_ADVISORY,
        emoji = "🏛️",
        shortSummaryBn = "সংকটকালীন ও বড় সম্প্রসারণ/বহুতল নির্মাণকাজে কৌশলগত পরামর্শ",
        dutiesBn = listOf(
            "এলাকার সম্মানিত মুরুব্বি ও প্রাজ্ঞ ব্যক্তিবর্গ হিসেবে মসজিদ কল্যাণে দিকনির্দেশনা দেওয়া।",
            "সংকটকালীন পরিস্থিতি বা বড় ধরনের সম্প্রসারণ ও বহুতল নির্মাণকাজে কৌশলগত পরামর্শ প্রদান করা।"
        )
    ),
    MUAZZIN(
        code = "muazzin",
        titleBn = "মুয়াজ্জিন",
        level = 4,
        tier = CommitteeTier.STAFF_MUSALLI,
        emoji = "🎙️",
        shortSummaryBn = "সময়মতো আজান, ইকামত ও সাউন্ড/মসজিদ শৃঙ্খলা রক্ষণাবেক্ষণ",
        dutiesBn = listOf(
            "৫ ওয়াক্ত নামাজের সময়মতো আজান ও ইকামত প্রদান করা।",
            "মসজিদের মাইক, সাউন্ড সিস্টেম ও নামাজ পূর্ববর্তী প্রস্তুতি তদারকি করা।"
        )
    ),
    KHADEM(
        code = "khadem",
        titleBn = "খাদেম",
        level = 4,
        tier = CommitteeTier.STAFF_MUSALLI,
        emoji = "🧹",
        shortSummaryBn = "মসজিদ প্রাঙ্গণ, অজুখানা পরিচ্ছন্নতা ও মুসল্লিদের খেদমত",
        dutiesBn = listOf(
            "মসজিদের মূল ভবন, কাতার, অজুখানা ও প্রাঙ্গণ পরিষ্কার-পরিচ্ছন্ন রাখা।",
            "জামাতের পূর্বে আলো, ফ্যান, এসি ও পানীয় জলের ব্যবস্থা নিশ্চিত করা।"
        )
    ),
    GENERAL_MEMBER(
        code = "general_member",
        titleBn = "সাধারণ সদস্য / মুসল্লি",
        level = 5,
        tier = CommitteeTier.STAFF_MUSALLI,
        emoji = "👥",
        shortSummaryBn = "নিয়মিত জামাত ট্র্যাকিং, স্বচ্ছ তহবিল পর্যবেক্ষণ ও মাসিক চাঁদা",
        dutiesBn = listOf(
            "নিয়মিত জামাতে অংশগ্রহণ ও মাসিক চাঁদা পরিশোধ করে মসজিদ তহবিলে শরিক হওয়া।",
            "স্বচ্ছ হিসাব-নিকাশ পর্যবেক্ষণ ও কমিটিকে গঠনমূলক মতামত প্রদান করা।"
        )
    );

    fun canApproveFinance(): Boolean =
        this == PRESIDENT || this == VICE_PRESIDENT

    fun canManageFinance(): Boolean =
        this == PRESIDENT || this == VICE_PRESIDENT || this == TREASURER || this == ASSISTANT_TREASURER

    fun canManageMembers(): Boolean =
        this == PRESIDENT || this == VICE_PRESIDENT || this == GENERAL_SECRETARY || this == ASSISTANT_SECRETARY || this == ORGANIZING_SECRETARY

    fun canScheduleMeetings(): Boolean =
        this == PRESIDENT || this == VICE_PRESIDENT || this == GENERAL_SECRETARY || this == ASSISTANT_SECRETARY || this == ORGANIZING_SECRETARY

    fun canPublishNotices(): Boolean =
        this == PRESIDENT || this == VICE_PRESIDENT || this == GENERAL_SECRETARY || this == ASSISTANT_SECRETARY || this == ORGANIZING_SECRETARY || this == PUBLICITY_SECRETARY

    fun canManageSmartBoard(): Boolean =
        this == PRESIDENT || this == GENERAL_SECRETARY || this == PUBLICITY_SECRETARY

    fun canManageOfficeDocs(): Boolean =
        this == PRESIDENT || this == VICE_PRESIDENT || this == GENERAL_SECRETARY || this == ASSISTANT_SECRETARY || this == OFFICE_SECRETARY

    fun canManageCashbox(): Boolean =
        this == PRESIDENT || this == VICE_PRESIDENT || this == TREASURER || this == ASSISTANT_TREASURER

    fun canWitnessCashbox(): Boolean =
        this == EXECUTIVE_MEMBER || this == PRESIDENT || this == VICE_PRESIDENT || this == GENERAL_SECRETARY

    fun canSubmitShariaReview(): Boolean =
        this == IMAM

    fun canSubmitAdvisoryGuidance(): Boolean =
        this == ADVISORY_BOARD

    fun canManageWelfareEducation(): Boolean =
        this == PRESIDENT || this == VICE_PRESIDENT || this == GENERAL_SECRETARY || this == WELFARE_EDUCATION_SECRETARY || this == IMAM

    companion object {
        fun fromCode(code: String): UserRole =
            entries.find { it.code.equals(code, ignoreCase = true) } ?: GENERAL_MEMBER

        fun byTier(tier: CommitteeTier): List<UserRole> =
            entries.filter { it.tier == tier }
    }
}

enum class TransactionType {
    INCOME, EXPENSE
}

enum class ApprovalStatus {
    PENDING, APPROVED, REJECTED
}

enum class PriorityLevel {
    NORMAL, IMPORTANT, URGENT
}

enum class PaymentMethod(val labelBn: String) {
    CASH("নগদ"),
    MOBILE_BANKING("বিকাশ/নগদ"),
    ONLINE("অনলাইন গেটওয়ে")
}
