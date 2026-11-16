package com.mosqueseba.app.data.model

import androidx.compose.ui.graphics.Color
import com.mosqueseba.app.ui.components.toBengaliDigits
import com.mosqueseba.app.ui.theme.*
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

enum class IslamicEventCategory(val labelBn: String, val badgeColor: Color) {
    OBLIGATORY("ফরজ ও ওয়াজিব", MosqueUrgentRed),
    EID("পবিত্র ঈদ ও আনন্দ", MosqueEmeraldPrimary),
    BLESSED_NIGHT("বরকতময় রজনী", MosqueGoldDark),
    SUNNAH_FAST("সুন্নাত ও নফল সিয়াম", MosqueSuccessGreen),
    HISTORIC("ঐতিহাসিক তাৎপর্য", MosqueInfoBlue)
}

data class IslamicEvent(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val hijriMonth: Int, // 1 = Muharram .. 12 = Dhul-Hijjah
    val hijriDay: Int,
    val hijriEndDay: Int? = null,
    val category: IslamicEventCategory,
    val summaryBn: String,
    val defaultSignificanceBn: String,
    val defaultAmalsBn: List<String>,
    val defaultKhutbahPoints: List<String>,
    val defaultNoticeTitle: String,
    val defaultNoticeContent: String
) {
    val monthNameBn: String
        get() = IslamicCalendarEngine.getHijriMonthNameBn(hijriMonth)

    val dateRangeBn: String
        get() {
            return if (hijriEndDay != null && hijriEndDay != hijriDay) {
                "${toBengaliDigits(hijriDay.toString())}-${toBengaliDigits(hijriEndDay.toString())} $monthNameBn"
            } else {
                "${toBengaliDigits(hijriDay.toString())} $monthNameBn"
            }
        }

    fun buildDefaultGuidanceSummary(): String = buildString {
        append("📌 তাৎপর্য ও ফজিলত:\n")
        append(defaultSignificanceBn)
        append("\n\n✨ গুরুত্বপূর্ণ নফল আমলসমূহ:\n")
        defaultAmalsBn.forEachIndexed { i, amal ->
            append("${toBengaliDigits((i + 1).toString())}. $amal\n")
        }
        append("\n🕌 ইমাম ও খতিবদের জন্য খুতবা/বয়ান রূপরেখা:\n")
        defaultKhutbahPoints.forEachIndexed { i, point ->
            append("${toBengaliDigits((i + 1).toString())}. $point\n")
        }
    }
}

data class IslamicEventCountdown(
    val event: IslamicEvent,
    val daysRemaining: Int,
    val isOngoing: Boolean,
    val isToday: Boolean,
    val isTomorrow: Boolean
) {
    val statusTextBn: String
        get() = when {
            isToday -> "আজ অনুষ্ঠিত হচ্ছে"
            isOngoing -> "বর্তমানে চলমান"
            isTomorrow -> "আগামীকাল"
            daysRemaining == 0 -> "আজকের দিন"
            else -> "আর মাত্র ${toBengaliDigits(daysRemaining.toString())} দিন বাকি"
        }
}

object IslamicCalendarEngine {

    val hijriMonthsBn = listOf(
        "মুহাররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি",
        "জমাদিউল আউয়াল", "জমাদিউস সানি", "রজব", "শাবান",
        "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
    )

    fun getHijriMonthNameBn(month: Int): String {
        return hijriMonthsBn.getOrElse(month - 1) { "হিজরি মাস" }
    }

    val events: List<IslamicEvent> = listOf(
        IslamicEvent(
            id = "ashura",
            titleBn = "পবিত্র আশুরা",
            titleEn = "Holy Ashura (10th Muharram)",
            hijriMonth = 1,
            hijriDay = 10,
            hijriEndDay = null,
            category = IslamicEventCategory.HISTORIC,
            summaryBn = "মুসা (আ.) ও বনী ইসরাঈলের মুক্তি এবং কারবালার প্রান্তরে শাহাদাতে হুসাইন (রা.)-এর ঐতিহাসিক স্মৃতিময় দিবস।",
            defaultSignificanceBn = "রাসুলুল্লাহ (সা.) ইরশাদ করেছেন, 'রমজানের পর আল্লাহর মাস মুহাররমের রোজাই সবচেয়ে উত্তম।' (সহিহ মুসলিম)। আশুরার ১ দিনের রোজার বিনিময়ে পেছনের এক বছরের গুনাহ মাফ হয়।",
            defaultAmalsBn = listOf(
                "৯ ও ১০ অথবা ১০ ও ১১ই মুহাররম মোট ২ দিন রোজা রাখা (ইয়াহুদিদের সাথে সাদৃশ্য বর্জনার্থে)।",
                "অধিক পরিমাণে ইস্তিগফার, দরুদ শরিফ ও কুরআন তিলাওয়াত করা।",
                "পরিবারের সদস্যদের জন্য সামর্থ্য অনুযায়ী উত্তম আহারের ব্যবস্থা করা।"
            ),
            defaultKhutbahPoints = listOf(
                "১. আশুরার ঐতিহাসিক প্রেক্ষাপট ও মুসা (আ.)-এর ঐতিহাসিক বিজয়।",
                "২. আশুরার রোজার মর্যাদা ও আগের এক বছরের গুনাহ মাফের হাদিস।",
                "৩. কারবালায় হযরত হুসাইন (রা.)-এর সত্য ও ন্যায়ের পক্ষে অটল ত্যাগ থেকে শিক্ষা।"
            ),
            defaultNoticeTitle = "পবিত্র আশুরা উপলক্ষে নফল রোজা ও বিশেষ বয়ানের বিজ্ঞপ্তি",
            defaultNoticeContent = "বিসমিল্লাহির রাহমানির রাহিম। সম্মানিত মুসল্লিবৃন্দ, আগামী ১০ই মুহাররম পবিত্র আশুরা। রাসুলুল্লাহ (সা.) আশুরার রোজা নিজে রেখেছেন এবং সাহাবিদের উৎসাহিত করেছেন। আশুরার রোজার বরকতে বিগত এক বছরের গুনাহ মোচনের সুসংবাদ রয়েছে। সুন্নত অনুযায়ী ৯ ও ১০ই মুহাররম রোজা রাখার আহ্বান জানাচ্ছি। বাদ মাগরিব মসজিদে বিশেষ দোয়ার মাহফিল অনুষ্ঠিত হবে।"
        ),
        IslamicEvent(
            id = "seeratunnabi",
            titleBn = "সীরাতুন্নবী (সা:) ও রবিউল আউয়াল",
            titleEn = "Seerat-un-Nabi (12th Rabiul Awwal)",
            hijriMonth = 3,
            hijriDay = 12,
            hijriEndDay = null,
            category = IslamicEventCategory.HISTORIC,
            summaryBn = "মানবতার মুক্তির দিশারি, সর্বশ্রেষ্ঠ রাসুল হযরত মুহাম্মদ (সা:)-এর আবির্ভাব ও জীবনাদর্শ স্মরণ।",
            defaultSignificanceBn = "মহান আল্লাহ তাআলা বলেন: 'নিশ্চয়ই রাসুলুল্লাহর জীবনের মধ্যে তোমাদের জন্য রয়েছে সর্বোত্তম আদর্শ।' (সূরা আহযাব: ২১)। রাসুলের সুন্নাহকে নিজের ব্যক্তি, পরিবার ও সমাজে বাস্তবায়ন করাই প্রকৃত ভালোবাসা।",
            defaultAmalsBn = listOf(
                "রাসুলুল্লাহ (সা:)-এর ওপর অধিক পরিমাণে দরুদ ও সালাম পাঠ করা।",
                "রাসুল (সা:)-এর সীরাত ও শামায়েলে তিরমিজি থেকে প্রিয় নবীর আখলাক অধ্যয়ন।",
                "প্রতি সোমবার রাসুল (সা:)-এর জন্মদিনের শুকরিয়া হিসেবে নফল সিয়াম পালন।"
            ),
            defaultKhutbahPoints = listOf(
                "১. বিশ্ব মানবতার রহমত হিসেবে রাসুল (সা:)-এর আগমন ও জাহিলিয়াতের অবসান।",
                "২. রাসুলুল্লাহ (সা:)-এর লেনদেন, ক্ষমাশীলতা ও চারিত্রিক মাধুর্য।",
                "৩. সমাজে সুন্নাহর পুনরুজ্জীবন ও বিদআত বর্জন।"
            ),
            defaultNoticeTitle = "সীরাতুন্নবী (সা:) উপলক্ষে বিশেষ আলোচনা ও দোয়া মাহফিল",
            defaultNoticeContent = "বিসমিল্লাহির রাহমানির রাহিম। আগামী ১২ই রবিউল আউয়াল জুমার পর মসজিদে সীরাতুন্নবী (সা:) শীর্ষক বিশেষ আলোচনা ও দোয়া মাহফিল অনুষ্ঠিত হবে। সম্মানিত মুসল্লিগণকে উপস্থিত হয়ে প্রিয় নবীর সুন্নাহ ও জীবনাদর্শ থেকে জ্ঞানার্জনের আমন্ত্রণ জানানো যাচ্ছে।"
        ),
        IslamicEvent(
            id = "shabe_meraj",
            titleBn = "পবিত্র শবে মেরাজ",
            titleEn = "Lailatul Miraj (27th Rajab)",
            hijriMonth = 7,
            hijriDay = 27,
            hijriEndDay = null,
            category = IslamicEventCategory.BLESSED_NIGHT,
            summaryBn = "মহানবী (সা:)-এর মসজিদুল হারাম থেকে বায়তুল মুকাদ্দাস ও সপ্ত আকাশ ভ্রমণের অলৌকিক রজনী এবং নামাজের উপহার।",
            defaultSignificanceBn = "আল্লাহ তাআলা মেরাজের রজনীতে উম্মতে মুহাম্মাদীর জন্য দৈনিক পাঁচ ওয়াক্ত নামাজ উপহার হিসেবে দান করেন। নামাজ হচ্ছে মুমিনের মেরাজ।",
            defaultAmalsBn = listOf(
                "রাতে নফল নামাজ, তাহাজ্জুদ ও কুরআন তিলাওয়াত করা।",
                "২৭শে রজব নফল রোজা রাখা।",
                "পাচ ওয়াক্ত ফরজ নামাজ জামাতে আদায়ের দৃঢ় অঙ্গীকার গ্রহণ।"
            ),
            defaultKhutbahPoints = listOf(
                "১. মেরাজের ঘটনা, এর বাস্তবতা এবং অলৌকিক নিদর্শনসমূহ।",
                "২. নামাজের গুরুত্ব: মেরাজের রাতে প্রদত্ত সর্বোত্তম উপহার।",
                "৩. বাইতুল মুকাদ্দাস ও মসজিদুল আকসার মর্যাদা।"
            ),
            defaultNoticeTitle = "পবিত্র শবে মেরাজ উপলক্ষে ওয়াজ ও দোয়ার মাহফিল",
            defaultNoticeContent = "বিসমিল্লাহির রাহমানির রাহিম। আগামী ২৭শে রজব বাদ এশা মসজিদে পবিত্র শবে মেরাজের তাৎপর্য ও নামাজের গুরুত্ব শীর্ষক আলোচনা অনুষ্ঠিত হবে। সকলকে উপস্থিত থাকার অনুরোধ করা যাচ্ছে।"
        ),
        IslamicEvent(
            id = "shabe_barat",
            titleBn = "পবিত্র শবে বরাত (লাইলাতুল নিসফ মিন শাবান)",
            titleEn = "Shab-e-Barat (15th Shaban)",
            hijriMonth = 8,
            hijriDay = 15,
            hijriEndDay = null,
            category = IslamicEventCategory.BLESSED_NIGHT,
            summaryBn = "শাবান মাসের মধ্য রজনী; তওবা, ক্ষমা প্রার্থনা ও রমজানের পূর্বপ্রস্তুতির বিশেষ বরকতময় রাত।",
            defaultSignificanceBn = "হাদিসে বর্ণিত হয়েছে, আল্লাহ তাআলা মধ্য শাবানের রাতে তাঁর বান্দাদের প্রতি বিশেষ অনুগ্রহে দৃষ্টি দেন এবং মুশরিক ও হিংসুক ব্যতীত সকলকে ক্ষমা করে দেন (ইবনে মাজাহ)।",
            defaultAmalsBn = listOf(
                "রাতের একান্তে তাহাজ্জুদ, তওবা ও ইস্তিগফারের সাথে আল্লাহর কাছে রোনাজারি করা।",
                "১৫ই শাবান নফল রোজা রাখা।",
                "হিংসা, বিদ্বেষ, অহংকার ও গুনাহ থেকে তওবা করে রমজানের জন্য আত্মশুদ্ধি লাভ।"
            ),
            defaultKhutbahPoints = listOf(
                "১. শাবান মাসের গুরুত্ব ও রমজানের প্রস্তুতি।",
                "২. ক্ষমা লাভের শর্ত: শিরক ও অন্তরের হিংসা-বিদ্বেষ থেকে মুক্তি।",
                "৩. রাতে একাকী নফল ইবাদত ও দিনে রোজা রাখা।"
            ),
            defaultNoticeTitle = "পবিত্র শবে বরাত উপলক্ষে মসজিদ পরিচালনা কমিটির ঘোষণা",
            defaultNoticeContent = "বিসমিল্লাহির রাহমানির রাহিম। সম্মানিত মুসল্লিবৃন্দ, আগামী ১৫ই শাবান দিবাগত রাতে পবিত্র শবে বরাত। মসজিদে রাতব্যাপী নফল ইবাদতের সুবিধার্থে বিদ্যুৎ, পানি ও নিরাপত্তা ব্যবস্থা সচল থাকবে। সকলে মসজিদে শান্তিময় পরিবেশে ইবাদত বন্দেগি করার জন্য অনুরোধ করা হলো।"
        ),
        IslamicEvent(
            id = "ramadan_start",
            titleBn = "পবিত্র মাহে রমজানুল মুবারক",
            titleEn = "Holy Month of Ramadan (1st Ramadan)",
            hijriMonth = 9,
            hijriDay = 1,
            hijriEndDay = 30,
            category = IslamicEventCategory.OBLIGATORY,
            summaryBn = "রহমত, মাগফিরাত ও নাজাতের মাস; পবিত্র কুরআন নাজিলের মাস ও ফরজ সিয়াম সাধনার সময়।",
            defaultSignificanceBn = "মহান আল্লাহ তাআলা বলেন: 'রমজান মাস, যাতে কুরআন নাজিল করা হয়েছে মানুষের হিদায়াত হিসেবে।' (সূরা বাকারা: ১৮৫)। এই মাসে জান্নাতের দরজা উন্মুক্ত হয় এবং জাহান্নামের দরজা বন্ধ করা হয়।",
            defaultAmalsBn = listOf(
                "একনিষ্ঠ নিয়তে ফরজ রোজা রাখা এবং সময়মতো সেহরি ও ইফতার গ্রহণ।",
                "প্রতি রাতে জামাতের সাথে ২০ রাকাত তারাবিহ নামাজ আদায় করা।",
                "প্রতিদিন এক পারা করে পুরো কুরআন তিলাওয়াত ও খতম করা।",
                "অসহায়দের মাঝে ইফতার ও অধিক পরিমাণে দান-সদকা করা।"
            ),
            defaultKhutbahPoints = listOf(
                "১. সিয়াম সাধনার লক্ষ্য: তাকওয়া অর্জন।",
                "২. রমজানের রোজা ভঙ্গের কারণ ও কাজা-কাফফারার হুকুম।",
                "৩. কুরআন তিলাওয়াত, তারাবিহ ও দানশীলতার অফুরন্ত সওয়াব।"
            ),
            defaultNoticeTitle = "পবিত্র মাহে রমজানের খতমে তারাবিহ ও সেহরি-ইফতারের সময়সূচি",
            defaultNoticeContent = "বিসমিল্লাহির রাহমানির রাহিম। আলহামদুলিল্লাহ, রহমত ও মাগফিরাতের পবিত্র মাহে রমজান সমাগত। মসজিদে খতমে তারাবিহ পরিচালনার জন্য বিজ্ঞ হাফেজ নিয়োগ সম্পন্ন হয়েছে। দৈনিক সেহরি ও ইফতারের সময়সূচি নোটিশ বোর্ডে ও অ্যাপে সংযুক্ত করা হলো। সকল মুসল্লিকে সময়মতো জামাতে শরিক হওয়ার আহ্বান জানাচ্ছি।"
        ),
        IslamicEvent(
            id = "shabe_qadr",
            titleBn = "পবিত্র লাইলাতুল কদর ও শেষ দশক",
            titleEn = "Lailat al-Qadr & Last 10 Days",
            hijriMonth = 9,
            hijriDay = 21,
            hijriEndDay = 29,
            category = IslamicEventCategory.BLESSED_NIGHT,
            summaryBn = "হাজার মাসের চেয়েও শ্রেষ্ঠতম রজনী; রমজানের শেষ দশকের বিজোড় রাতসমূহে কদর তালাশ ও সুন্নাত ইতিকাফ।",
            defaultSignificanceBn = "পবিত্র কুরআনে ইরশাদ হয়েছে: 'কদরের রাত এক হাজার মাসের চেয়েও উত্তম।' (সূরা আল-কদর: ৩)। এই রাতের ইবাদত ৮৩ বছর ৪ মাসের ইবাদতের চেয়েও অধিক সওয়াব এনে দেয়।",
            defaultAmalsBn = listOf(
                "শেষ দশকের বিজোড় রাতসমূহে (২১, ২৩, ২৫, ২৭, ২৯) রাত জেগে তাহাজ্জুদ ও নফল নামাজ পড়া।",
                "দোয়া পড়া: 'আল্লাহুম্মা ইন্নাকা আফুউন তুহিব্বুল আফওয়া ফা'ফু আন্নী' (হে আল্লাহ! আপনি ক্ষমাশীল, ক্ষমা পছন্দ করেন, অতএব আমাকে ক্ষমা করুন)।",
                "রমজানের শেষ দশ দিনে মসজিদে সুন্নাত ইতিকাফে বসা।"
            ),
            defaultKhutbahPoints = listOf(
                "১. লাইলাতুল কদরের মর্যাদা ও কুরআন নাজিলের প্রেক্ষাপট।",
                "২. শেষ দশকের বিজোড় রাতগুলোর ফজিলত ও রাসুল (সা:)-এর ইবাদতের আমল।",
                "৩. ইতিকাফের আদব ও লাইলাতুল কদরে বিশেষ মোনাজাত।"
            ),
            defaultNoticeTitle = "পবিত্র রমজানের শেষ দশকে ইতিকাফ ও শবে কদর দোয়া মাহফিল",
            defaultNoticeContent = "বিসমিল্লাহির রাহমানির রাহিম। রমজানের শেষ দশকে মসজিদে সুন্নাত ইতিকাফ পালন করতে ইচ্ছুক মুসল্লিদের আগামী ১৯শে রমজানের মধ্যে ইমাম সাহেবের নিকট নাম তালিকাভুক্ত করার অনুরোধ করা হচ্ছে। এছাড়া ২৭শে রমজান দিবাগত রাতে বিশেষ শবে কদর দোয়া অনুষ্ঠিত হবে।"
        ),
        IslamicEvent(
            id = "eid_ul_fitr",
            titleBn = "পবিত্র ঈদুল ফিতর",
            titleEn = "Eid-ul-Fitr (1st Shawwal)",
            hijriMonth = 10,
            hijriDay = 1,
            hijriEndDay = 1,
            category = IslamicEventCategory.EID,
            summaryBn = "সিয়াম সাধনা সমাপ্তির পর বিশ্ব মুসলিমের প্রধান ধর্মীয় আনন্দ উৎসব ও সদকাতুল ফিতর আদায়ের দিন।",
            defaultSignificanceBn = "ঈদের দিন রোজা রাখা হারাম। ঈদের নামাজ ওয়াজিব। ঈদের নামাজের পূর্বে সদকাতুল ফিতর গরিব-অসহায়দের মাঝে পৌঁছে দেওয়া সুন্নাত যাতে তারাও ঈদের আনন্দে শামিল হতে পারে।",
            defaultAmalsBn = listOf(
                "ঈদের নামাজের পূর্বেই সদকাতুল ফিতর পরিশোধ করা।",
                "সকাল বেলা গোসল করা, উত্তম পোশাক পরিধান করা ও সুগন্ধি ব্যবহার করা।",
                "ঈদের মাঠে যাওয়ার পূর্বে কিছু মিষ্টান্ন খাওয়া।",
                "ঈদের জামাতে যাওয়ার সময় তাকবির পাঠ করা: 'আল্লাহু আকবার, আল্লাহু আকবার, লা ইলাহা ইল্লাল্লাহু ওয়াল্লাহু আকবার, আল্লাহু আকবার ওয়া লিল্লাহিল হামদ'।"
            ),
            defaultKhutbahPoints = listOf(
                "১. ঈদুল ফিতরের তাৎপর্য: শুকরিয়া ও ভ্রাতৃত্ববোধ।",
                "২. সদকাতুল ফিতরের পরিমাণ ও হকদারদের কাছে পৌঁছানোর নিয়ম।",
                "৩. রমজানের পরও নিয়মিত নামাজ ও তাকওয়া বজায় রাখার গুরুত্ব।"
            ),
            defaultNoticeTitle = "পবিত্র ঈদুল ফিতরের নামাজের জামাতের সময়সূচি",
            defaultNoticeContent = "বিসমিল্লাহির রাহমানির রাহিম। সকল মুসল্লিকে পবিত্র ঈদুল ফিতরের আন্তরিক শুভেচ্ছা ও ঈদ মোবারক! আমাদের মসজিদে পবিত্র ঈদুল ফিতরের ১ম জামাত সকাল ৭:৩০ টায় এবং ২য় জামাত সকাল ৮:৩০ টায় অনুষ্ঠিত হবে। ঈদের নামাজের পূর্বে ফিতরা আদায়ের জন্য অনুরোধ জানানো যাচ্ছে।"
        ),
        IslamicEvent(
            id = "six_fasts_shawwal",
            titleBn = "শাওয়াল মাসের ৬ রোজা",
            titleEn = "Six Fasts of Shawwal",
            hijriMonth = 10,
            hijriDay = 2,
            hijriEndDay = 30,
            category = IslamicEventCategory.SUNNAH_FAST,
            summaryBn = "রমজানের পর শাওয়াল মাসে যেকোনো ৬টি নফল রোজা যা সারা বছর রোজা রাখার সমান সওয়াব এনে দেয়।",
            defaultSignificanceBn = "রাসুলুল্লাহ (সা.) বলেছেন: 'যে ব্যক্তি রমজানের রোজা রাখল, অতঃপর শাওয়াল মাসে ছয়টি রোজা রাখল, সে যেন সারা বছরই রোজা রাখল।' (সহিহ মুসলিম)।",
            defaultAmalsBn = listOf(
                "শাওয়াল মাসের ২ তারিখ থেকে মাসের শেষ পর্যন্ত সুবিধাজনক সময়ে ৬টি রোজা রাখা (একটানা বা বিরতি দিয়ে)।",
                "রমজানের কোনো কাজা রোজা থাকলে তা আগে সম্পন্ন করা উত্তম।"
            ),
            defaultKhutbahPoints = listOf(
                "১. শাওয়ালের ছয় রোজার সওয়াব ও ক্যালকুলেশন (১০ গুণ প্রতিদানে ৩৬০ দিন)।",
                "২. ফরজ ইবাদতের ঘাটতি পূরণে নফল আমলের ভূমিকা।"
            ),
            defaultNoticeTitle = "শাওয়াল মাসের ছয় রোজার ফজিলত ও অনুস্মারক",
            defaultNoticeContent = "বিসমিল্লাহির রাহমানির রাহিম। সম্মানিত মুসল্লিবৃন্দ, রমজানের ফরজ রোজার পর শাওয়ালের ৬টি নফল রোজা পুরো বছর রোজা রাখার সমতুল্য সওয়াব বহন করে। মাসের যেকোনো দিনে ধারাবাহিকভাবে বা ভেঙে ভেঙে এই রোজাগুলো সম্পন্ন করার আহ্বান জানানো হচ্ছে।"
        ),
        IslamicEvent(
            id = "dhul_hijjah_ten_days",
            titleBn = "জিলহজের প্রথম দশক",
            titleEn = "First 10 Days of Dhul-Hijjah",
            hijriMonth = 12,
            hijriDay = 1,
            hijriEndDay = 10,
            category = IslamicEventCategory.BLESSED_NIGHT,
            summaryBn = "বছরের সর্বশ্রেষ্ঠ দশ দিন; যার ইবাদত আল্লাহর নিকট জিহাদের চেয়েও অধিক প্রিয় ও মর্যাদাপূর্ণ।",
            defaultSignificanceBn = "রাসুল (সা:) বলেছেন: 'আল্লাহর নিকট জিলহজের প্রথম দশ দিনের নেক আমলের চেয়ে বেশি প্রিয় অন্য কোনো দিনের আমল নেই।' (সহিহ বুখারি)।",
            defaultAmalsBn = listOf(
                "১ থেকে ৯ই জিলহজ প্রতিদিন নফল রোজা রাখা।",
                "অধিক পরিমাণে তাহলিল (লা ইলাহা ইল্লাল্লাহ), তাকবির (আল্লাহু আকবার) ও তাহমিদ (আলহামদুলিল্লাহ) পাঠ করা।",
                "যাঁরা কোরবানি করার নিয়ত করেছেন, তাঁরা ১লা জিলহজ থেকে কোরবানি পর্যন্ত নখ ও চুল কাটা থেকে বিরত থাকা (মুস্তাহাব)।"
            ),
            defaultKhutbahPoints = listOf(
                "১. জিলহজের প্রথম দশকের অনন্য শ্রেষ্ঠত্ব ও ফজিলত।",
                "২. তাকবিরে তাশরিকের সময় ও নিয়ম।",
                "৩. কোরবানির উদ্দেশ্য: তাকওয়া ও আত্মত্যাগ।"
            ),
            defaultNoticeTitle = "পবিত্র জিলহজের প্রথম দশকের ফজিলত ও করণীয়",
            defaultNoticeContent = "বিসমিল্লাহির রাহমানির রাহিম। পবিত্র জিলহজ মাসের চাঁদ দেখা সাপেক্ষে আগামী ১লা জিলহজ থেকে ফজিলতপূর্ণ দশ দিন শুরু হতে যাচ্ছে। এই দিনগুলোতে অধিক দান-সদকা, নফল রোজা ও তাকবির পাঠ করার আহ্বান জানানো হচ্ছে। কোরবানিদাতাগণ নখ-চুল না কাটার মুস্তাহাব আমল পালন করুন।"
        ),
        IslamicEvent(
            id = "day_of_arafah",
            titleBn = "ইয়াওমে আরাফাহ (হজের দিন)",
            titleEn = "Day of Arafah (9th Dhul-Hijjah)",
            hijriMonth = 12,
            hijriDay = 9,
            hijriEndDay = 9,
            category = IslamicEventCategory.SUNNAH_FAST,
            summaryBn = "হজের মূল দিন এবং বছরের শ্রেষ্ঠতম দোয়ার দিন; আরাফাহর সিয়াম দুই বছরের গুনাহ মাফ করে।",
            defaultSignificanceBn = "রাসুলুল্লাহ (সা.) বলেছেন: 'আরাফার দিনের রোজার ব্যাপারে আমি আল্লাহর কাছে আশাবাদী যে, তিনি এর দ্বারা পূর্ববর্তী এক বছর ও পরবর্তী এক বছরের গুনাহ ক্ষমা করে দেবেন।' (সহিহ মুসলিম)।",
            defaultAmalsBn = listOf(
                "৯ই জিলহজ আরাফাহর দিন নফল রোজা রাখা।",
                "ফজরের পর থেকে ১৩ই জিলহজ আসর পর্যন্ত প্রত্যেক ফরজ নামাজের পর তাকবীরে তাশরিক একবার পাঠ করা ওয়াজিব।",
                "অধিক পরিমাণে সেরা দোয়া পাঠ করা: 'লা ইলাহা ইল্লাল্লাহু ওয়াহদাহু লা শারীকা লাহু, লাহুল মুলকু ওয়া লাহুল হামদু, ওয়া হুয়া আলা কুল্লি শাইয়িন কাদির'।"
            ),
            defaultKhutbahPoints = listOf(
                "১. আরাফাহর ময়দানে রাসুলুল্লাহ (সা:)-এর বিদায় হজের ঐতিহাসিক ভাষণ।",
                "২. দুই বছরের গুনাহ মাফের সুসংবাদ ও আরাফাহর রোজা।",
                "৩. তাকবীরে তাশরিকের বিধান ও নিয়ম।"
            ),
            defaultNoticeTitle = "ইয়াওমে আরাফাহর নফল রোজা ও তাকবীরে তাশরিকের ওয়াজিব বিধান",
            defaultNoticeContent = "বিসমিল্লাহির রাহমানির রাহিম। আগামী ৯ই জিলহজ পবিত্র আরাফাহর দিন। এই দিনের রোজা বিগত এক বছর ও আগামী এক বছরের গুনাহ মোচনের মাধ্যম। ৯ই জিলহজ ফজর থেকে ১৩ই জিলহজ আসর পর্যন্ত মোট ২৩ ওয়াক্তে ফরজ নামাজের সালাম ফেরানোর পর তাকবীরে তাশরিক একবার পাঠ করা ওয়াজিব।"
        ),
        IslamicEvent(
            id = "eid_ul_adha",
            titleBn = "পবিত্র ঈদুল আজহা (কোরবানি)",
            titleEn = "Eid-ul-Adha (10th Dhul-Hijjah)",
            hijriMonth = 12,
            hijriDay = 10,
            hijriEndDay = 10,
            category = IslamicEventCategory.EID,
            summaryBn = "হযরত ইব্রাহিম (আ.) ও ইসমাইল (আ.)-এর মহান আত্মত্যাগের স্মরণে পশু কোরবানি ও আনন্দের উৎসব।",
            defaultSignificanceBn = "মহান আল্লাহ বলেন: 'আল্লাহর নিকট পৌঁছায় না এগুলোর গোশত এবং রক্ত, বরং তাঁর নিকট পৌঁছায় তোমাদের তাকওয়া।' (সূরা হজ: ৩৭)। সামর্থ্যবান ব্যক্তির ওপর কোরবানি ওয়াজিব।",
            defaultAmalsBn = listOf(
                "সকালে কোনো কিছু না খেয়ে ঈদের নামাজ পড়তে যাওয়া এবং কোরবানির গোশত দিয়ে আহার শুরু করা।",
                "নামাজের পর শরীয়তসম্মত সুস্থ-সবল পশু আল্লাহর সন্তুষ্টির উদ্দেশ্যে জবেহ করা।",
                "কোরবানির গোশত তিন ভাগে ভাগ করে নিজে খাওয়া, আত্মীয়স্বজনকে দেওয়া এবং গরিবদের মাঝে বিতরণ করা।",
                "কোরবানির বর্জ্য নির্দিষ্ট স্থানে ফেলে মসজিদ ও আশপাশের পরিবেশ পরিচ্ছন্ন রাখা।"
            ),
            defaultKhutbahPoints = listOf(
                "১. ইব্রাহিম (আ.) ও ইসমাইল (আ.)-এর চরম আত্মত্যাগ ও আনুগত্য।",
                "২. কোরবানির মাসআলা, গোশত বণ্টন ও চামড়ার হকদার।",
                "৩. হালাল উপার্জনের কোরবানি এবং পরিবেশ পরিচ্ছন্ন রাখার দায়িত্ব।"
            ),
            defaultNoticeTitle = "পবিত্র ঈদুল আজহার জামাত ও কোরবানি বর্জ্য ব্যবস্থাপনা নির্দেশনা",
            defaultNoticeContent = "বিসমিল্লাহির রাহমানির রাহিম। সকল মুসল্লিকে পবিত্র ঈদুল আজহার শুভেচ্ছা! আমাদের মসজিদে ঈদের জামাত সকাল ৭:০০ টায় অনুষ্ঠিত হবে। কোরবানি করার পর রক্ত ও বর্জ্য দ্রুত পরিষ্কার করে ব্লিচিং পাউডার ছিটানোর জন্য অনুরোধ জানানো যাচ্ছে।"
        ),
        IslamicEvent(
            id = "ayyam_e_tashreeq",
            titleBn = "আইয়ামে তাশরিক",
            titleEn = "Days of Tashreeq (11-13th Dhul-Hijjah)",
            hijriMonth = 12,
            hijriDay = 11,
            hijriEndDay = 13,
            category = IslamicEventCategory.OBLIGATORY,
            summaryBn = "কোরবানির পরবর্তী ৩ দিন; পানাহার ও আনন্দের দিন এবং প্রত্যেক ফরজ নামাজের পর তাকবীরে তাশরিকের সময়।",
            defaultSignificanceBn = "রাসুল (সা:) বলেছেন: 'আইয়ামে তাশরিক হলো পানাহার ও আল্লাহকে স্মরণ করার দিন।' (সহিহ মুসলিম)। এই দিনগুলোতে রোজা রাখা নিষিদ্ধ।",
            defaultAmalsBn = listOf(
                "প্রত্যেক ফরজ নামাজের পর পুরুষদের উচ্চস্বরে এবং নারীদের নিম্নস্বরে তাকবীরে তাশরিক একবার পাঠ করা ওয়াজিব।",
                "যাঁরা ১০ই জিলহজ কোরবানি করতে পারেননি, তাঁরা ১১ ও ১২ই জিলহজ সূর্যাস্ত পর্যন্ত কোরবানি সম্পন্ন করতে পারবেন।"
            ),
            defaultKhutbahPoints = listOf(
                "১. আল্লাহর শুকরিয়া জ্ঞাপন ও আত্মীয়তার বন্ধন সুদৃঢ়করণ।",
                "২. তাকবীরে তাশরিকের তাৎপর্য ও ওয়াজিব আমল।"
            ),
            defaultNoticeTitle = "আইয়ামে তাশরিকের তাকবির স্মরণিকা",
            defaultNoticeContent = "বিসমিল্লাহির রাহমানির রাহিম। ১৩ই জিলহজ আসর নামাজ পর্যন্ত প্রত্যেক ফরজ নামাজের পর তাকবীরে তাশরিক পাঠ করার ওয়াজিব আমল অব্যাহত রাখুন।"
        ),
        IslamicEvent(
            id = "ayyam_al_beed",
            titleBn = "আইয়ামে বিজ (চান্দ্র মাসের তিন রোজা)",
            titleEn = "Ayyam al-Beed (13th, 14th, 15th of Hijri Months)",
            hijriMonth = 0, // 0 = every Hijri month
            hijriDay = 13,
            hijriEndDay = 15,
            category = IslamicEventCategory.SUNNAH_FAST,
            summaryBn = "প্রতি চান্দ্র মাসের ১৩, ১৪ ও ১৫ তারিখের পূর্ণিমার ৩টি সুন্নাত রোজা।",
            defaultSignificanceBn = "রাসুলুল্লাহ (সা.) সাহাবিদের প্রতি হিজরি মাসে তিনটি রোজা রাখার নির্দেশ দিতেন—১৩, ১৪ ও ১৫ তারিখ। (সুনানে নাসাঈ)।",
            defaultAmalsBn = listOf(
                "হিজরি মাসের ১৩, ১৪ ও ১৫ তারিখে সুন্নাত রোজা পালন করা।",
                "মাসের মাঝখানে আত্মনিয়ন্ত্রণ ও আধ্যাত্মিক চর্চা জোরদার করা।"
            ),
            defaultKhutbahPoints = listOf(
                "১. আইয়ামে বিজের সুন্নাহ ও শারীরিক ও আত্মিক উপকারিতা।",
                "২. নিয়মিত নফল ইবাদতের মাধ্যমে আল্লাহর নৈকট্য লাভ।"
            ),
            defaultNoticeTitle = "চলতি হিজরি মাসের আইয়ামে বিজের সুন্নাত রোজা",
            defaultNoticeContent = "বিসমিল্লাহির রাহমানির রাহিম। প্রিয় মুসল্লিবৃন্দ, চলতি হিজরি মাসের ১৩, ১৪ ও ১৫ তারিখে আইয়ামে বিজের সুন্নাত রোজা রাখার জন্য সকলকে আহ্বান জানানো হচ্ছে।"
        )
    )

    fun getCurrentHijriDate(offsetDays: Int = 0): Triple<Int, Int, Int> {
        val hijrah = HijrahDate.now().plus(offsetDays.toLong(), ChronoUnit.DAYS)
        val year = hijrah.get(ChronoField.YEAR)
        val month = hijrah.get(ChronoField.MONTH_OF_YEAR)
        val day = hijrah.get(ChronoField.DAY_OF_MONTH)
        return Triple(year, month, day)
    }

    fun getFormattedCurrentHijriDate(offsetDays: Int = 0): String {
        val (year, month, day) = getCurrentHijriDate(offsetDays)
        val monthName = getHijriMonthNameBn(month)
        return toBengaliDigits("$day $monthName $year হিজরি")
    }

    fun getEventCountdowns(offsetDays: Int = 0): List<IslamicEventCountdown> {
        val nowHijrah = HijrahDate.now().plus(offsetDays.toLong(), ChronoUnit.DAYS)
        val currentYear = nowHijrah.get(ChronoField.YEAR)
        val currentMonth = nowHijrah.get(ChronoField.MONTH_OF_YEAR)
        val currentDay = nowHijrah.get(ChronoField.DAY_OF_MONTH)

        return events.map { event ->
            val eventMonth = if (event.hijriMonth == 0) currentMonth else event.hijriMonth
            val isCurrentMonth = currentMonth == eventMonth

            val isOngoing = if (event.hijriEndDay != null) {
                isCurrentMonth && currentDay >= event.hijriDay && currentDay <= event.hijriEndDay
            } else {
                false
            }
            val isToday = isCurrentMonth && currentDay == event.hijriDay

            val daysRemaining: Int = if (isToday || isOngoing) {
                0
            } else {
                val targetYear = if (eventMonth < currentMonth || (eventMonth == currentMonth && event.hijriDay < currentDay)) {
                    currentYear + 1
                } else {
                    currentYear
                }
                try {
                    val safeDay = event.hijriDay.coerceIn(1, 29)
                    val targetDate = HijrahDate.of(targetYear, eventMonth, safeDay)
                    ChronoUnit.DAYS.between(nowHijrah, targetDate).toInt().coerceAtLeast(0)
                } catch (e: Exception) {
                    val monthsAway = if (eventMonth >= currentMonth) eventMonth - currentMonth else (12 - currentMonth) + eventMonth
                    (monthsAway * 29.5 + (event.hijriDay - currentDay)).toInt().coerceAtLeast(1)
                }
            }

            val isTomorrow = daysRemaining == 1

            IslamicEventCountdown(
                event = event,
                daysRemaining = daysRemaining,
                isOngoing = isOngoing,
                isToday = isToday,
                isTomorrow = isTomorrow
            )
        }.sortedWith(
            compareBy<IslamicEventCountdown> { if (it.isToday || it.isOngoing) 0 else 1 }
                .thenBy { it.daysRemaining }
        )
    }

    fun getNextUpcomingMajorEvent(offsetDays: Int = 0): IslamicEventCountdown {
        val list = getEventCountdowns(offsetDays)
        // Prefer ongoing or today first, else the soonest upcoming event
        return list.firstOrNull { it.isToday || it.isOngoing }
            ?: list.firstOrNull { it.event.category != IslamicEventCategory.SUNNAH_FAST || it.event.id != "ayyam_al_beed" }
            ?: list.first()
    }
}
