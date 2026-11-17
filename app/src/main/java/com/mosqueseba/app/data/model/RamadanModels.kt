package com.mosqueseba.app.data.model

import com.mosqueseba.app.ui.components.toBengaliDigits
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class RamadanAshra(
    val titleBn: String,
    val subtitleBn: String,
    val descriptionBn: String,
    val duaArabic: String,
    val duaTransliteration: String,
    val duaTranslation: String
) {
    RAHMAH(
        titleBn = "রহমতের দশক",
        subtitleBn = "১ থেকে ১০ রমজান",
        descriptionBn = "আল্লাহ তাআলার অসীম দয়া, অনুগ্রহ ও রহমত বর্ষণের বরকতময় দিনসমূহ।",
        duaArabic = "رَبِّ اغْفِرْ وَارْحَمْ وَأَنْتَ خَيْرُ الرَّاحِمِينَ",
        duaTransliteration = "রব্বিগফির ওয়ারহাম ওয়া আনতা খইরুর রহিমীন।",
        duaTranslation = "হে আমার পালনকর্তা! আমাকে ক্ষমা করুন ও আমার ওপর রহম করুন; আর আপনিই সর্বশ্রেষ্ঠ দয়ালু। (সূরা আল-মুমিনূন: ১১৮)"
    ),
    MAGHFIRAH(
        titleBn = "মাগফিরাতের দশক",
        subtitleBn = "১১ থেকে ২০ রমজান",
        descriptionBn = "অতীত জীবনের সকল গুনাহখাতা মাফ ও আল্লাহর কাছে ক্ষমা প্রার্থনার দশক।",
        duaArabic = "أَسْتَغْفِرُ اللهَ رَبِّي مِنْ كُلِّ ذَنْبٍ وَأَتُوبُ إِلَيْهِ",
        duaTransliteration = "আস্তাগফিরুল্লাহা রব্বী মিন কুল্লি যাম্বিওঁ ওয়া আতূবু ইলাইহি।",
        duaTranslation = "আমি আল্লাহর নিকট ক্ষমা প্রার্থনা করছি যিনি আমার রব, এবং তাঁরই কাছে তওবা করে ফিরে আসছি।"
    ),
    NAJAT(
        titleBn = "নাজাতের দশক",
        subtitleBn = "২১ থেকে ৩০ রমজান",
        descriptionBn = "জাহান্নামের আগুন থেকে চূড়ান্ত মুক্তি, ইতিকাফ ও পবিত্র শবে কদরের মহিমান্বিত দশক।",
        duaArabic = "اللَّهُمَّ أَجِرْنَا مِنَ النَّارِ ، اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي",
        duaTransliteration = "আল্লাহুম্মা আজিরনা মিনান নার। আল্লাহুম্মা ইন্নাকা আফুউন তুহিব্বুল আফওয়া ফা'ফু আন্নী।",
        duaTranslation = "হে আল্লাহ! আমাদের জাহান্নামের আগুন থেকে মুক্তি দিন। হে আল্লাহ! আপনি ক্ষমাশীল, ক্ষমা করা পছন্দ করেন, অতএব আমাকে ক্ষমা করে দিন।"
    );

    companion object {
        fun fromDay(day: Int): RamadanAshra = when (day) {
            in 1..10 -> RAHMAH
            in 11..20 -> MAGHFIRAH
            else -> NAJAT
        }
    }
}

data class BangladeshDistrictOffset(
    val nameBn: String,
    val nameEn: String,
    val sehriOffsetMinutes: Int,
    val iftarOffsetMinutes: Int
)

data class RamadanDaySchedule(
    val day: Int,
    val dateEn: String,
    val dateBn: String,
    val hijriDateBn: String,
    val ashra: RamadanAshra,
    val sehriEndTime: String,
    val fajrTime: String,
    val iftarTime: String,
    val maghribTime: String
)

data class RamadanDuaItem(
    val id: String,
    val titleBn: String,
    val categoryBn: String,
    val arabicText: String,
    val transliterationBn: String,
    val translationBn: String,
    val referenceBn: String
)

data class RamadanMasailCategory(
    val titleBn: String,
    val iconType: String,
    val points: List<String>
)

data class FitrahItem(
    val commodityBn: String,
    val arabicName: String,
    val quantityBn: String,
    val approxPriceMin: Int,
    val approxPriceMax: Int,
    val referenceBn: String
)

data class TodayRamadanStatus(
    val isRamadanNow: Boolean,
    val currentRamadanDay: Int,
    val hijriDateFormatted: String,
    val ashra: RamadanAshra,
    val nextMilestoneBn: String,
    val countdownTextBn: String,
    val todaySehri: String,
    val todayIftar: String,
    val progressFraction: Float,
    val daysUntilRamadan: Int
)

object RamadanEngine {

    val bangladeshDistricts: List<BangladeshDistrictOffset> = listOf(
        BangladeshDistrictOffset("ঢাকা (কেন্দ্রীয়)", "Dhaka", 0, 0),
        BangladeshDistrictOffset("চট্টগ্রাম", "Chattogram", -5, -5),
        BangladeshDistrictOffset("সিলেট", "Sylhet", -6, -6),
        BangladeshDistrictOffset("কুমিল্লা", "Cumilla", -4, -4),
        BangladeshDistrictOffset("ময়মনসিংহ", "Mymensingh", 0, 0),
        BangladeshDistrictOffset("বরিশাল", "Barishal", +1, +1),
        BangladeshDistrictOffset("খুলনা", "Khulna", +5, +5),
        BangladeshDistrictOffset("রাজশাহী", "Rajshahi", +7, +7),
        BangladeshDistrictOffset("রংপুর", "Rangpur", +6, +6),
        BangladeshDistrictOffset("বগুড়া", "Bogura", +6, +6),
        BangladeshDistrictOffset("কক্সবাজার", "Cox's Bazar", -7, -7),
        BangladeshDistrictOffset("নোয়াখালী", "Noakhali", -3, -3),
        BangladeshDistrictOffset("ফেনী", "Feni", -4, -4),
        BangladeshDistrictOffset("যশোর", "Jashore", +6, +6),
        BangladeshDistrictOffset("দিনাজপুর", "Dinajpur", +8, +8),
        BangladeshDistrictOffset("পাবনা", "Pabna", +5, +5),
        BangladeshDistrictOffset("কুষ্টিয়া", "Kushtia", +6, +6),
        BangladeshDistrictOffset("ব্রাহ্মণবাড়িয়া", "Brahmanbaria", -3, -3),
        BangladeshDistrictOffset("টাঙ্গাইল", "Tangail", +2, +2),
        BangladeshDistrictOffset("ফরিদপুর", "Faridpur", +2, +2)
    )

    // Base timetable: Dhaka Standard baseline for 30 days
    private val dhakaBaseSehriIftarTimes = listOf(
        Pair("05:05", "18:02"), // Day 1
        Pair("05:04", "18:03"), // Day 2
        Pair("05:03", "18:03"), // Day 3
        Pair("05:02", "18:04"), // Day 4
        Pair("05:01", "18:04"), // Day 5
        Pair("05:00", "18:05"), // Day 6
        Pair("04:59", "18:05"), // Day 7
        Pair("04:58", "18:06"), // Day 8
        Pair("04:57", "18:06"), // Day 9
        Pair("04:56", "18:07"), // Day 10 (End of Rahmah)
        Pair("04:55", "18:07"), // Day 11
        Pair("04:54", "18:08"), // Day 12
        Pair("04:53", "18:08"), // Day 13
        Pair("04:52", "18:09"), // Day 14
        Pair("04:51", "18:09"), // Day 15
        Pair("04:50", "18:10"), // Day 16
        Pair("04:49", "18:10"), // Day 17
        Pair("04:48", "18:11"), // Day 18
        Pair("04:47", "18:11"), // Day 19
        Pair("04:46", "18:12"), // Day 20 (End of Maghfirah)
        Pair("04:45", "18:12"), // Day 21
        Pair("04:44", "18:13"), // Day 22
        Pair("04:43", "18:13"), // Day 23
        Pair("04:41", "18:14"), // Day 24
        Pair("04:40", "18:14"), // Day 25
        Pair("04:39", "18:15"), // Day 26
        Pair("04:38", "18:15"), // Day 27 (Laylatul Qadr)
        Pair("04:37", "18:16"), // Day 28
        Pair("04:36", "18:16"), // Day 29
        Pair("04:35", "18:17")  // Day 30
    )

    fun get30DaySchedule(district: BangladeshDistrictOffset = bangladeshDistricts.first()): List<RamadanDaySchedule> {
        return (1..30).map { day ->
            val base = dhakaBaseSehriIftarTimes[day - 1]
            val adjustedSehri = adjustTime(base.first, district.sehriOffsetMinutes)
            val adjustedFajr = adjustTime(adjustedSehri, 6) // Fajr starts ~6 mins after Sehri ends
            val adjustedIftar = adjustTime(base.second, district.iftarOffsetMinutes)
            val adjustedMaghrib = adjustedIftar

            val ashra = RamadanAshra.fromDay(day)
            val dayBn = toBengaliDigits(day.toString())

            RamadanDaySchedule(
                day = day,
                dateEn = "Day $day",
                dateBn = "$dayBn রমজান",
                hijriDateBn = "$dayBn রমজানুল মুবারক",
                ashra = ashra,
                sehriEndTime = formatTimeBn(adjustedSehri),
                fajrTime = formatTimeBn(adjustedFajr),
                iftarTime = formatTimeBn(adjustedIftar),
                maghribTime = formatTimeBn(adjustedMaghrib)
            )
        }
    }

    private fun adjustTime(timeStr: String, offsetMinutes: Int): String {
        return try {
            val parts = timeStr.split(":")
            val hour = parts[0].toInt()
            val min = parts[1].toInt()
            var totalMin = hour * 60 + min + offsetMinutes
            if (totalMin < 0) totalMin += 24 * 60
            totalMin %= (24 * 60)
            val h = totalMin / 60
            val m = totalMin % 60
            String.format("%02d:%02d", h, m)
        } catch (e: Exception) {
            timeStr
        }
    }

    private fun formatTimeBn(timeStr: String): String {
        return try {
            val parts = timeStr.split(":")
            var hour = parts[0].toInt()
            val min = parts[1].toInt()
            val isPm = hour >= 12
            if (hour > 12) hour -= 12
            if (hour == 0) hour = 12
            val periodBn = if (isPm) "সন্ধ্যা" else "ভোর"
            "$periodBn ${toBengaliDigits(String.format("%02d:%02d", hour, min))}"
        } catch (e: Exception) {
            toBengaliDigits(timeStr)
        }
    }

    fun getTodayStatus(
        district: BangladeshDistrictOffset = bangladeshDistricts.first(),
        hijriDayOffset: Int = 0
    ): TodayRamadanStatus {
        val (hijriYear, hijriMonth, hijriDay) = IslamicCalendarEngine.getCurrentHijriDate(hijriDayOffset)
        val isRamadan = hijriMonth == 9

        val scheduleList = get30DaySchedule(district)

        val currentDay = if (isRamadan) hijriDay.coerceIn(1, 30) else 1
        val todaySchedule = scheduleList[currentDay - 1]
        val ashra = todaySchedule.ashra

        val now = LocalTime.now()
        val currentMinutes = now.hour * 60 + now.minute

        // Base timings for today
        val baseTimes = dhakaBaseSehriIftarTimes[currentDay - 1]
        val sehriTimeStr = adjustTime(baseTimes.first, district.sehriOffsetMinutes)
        val iftarTimeStr = adjustTime(baseTimes.second, district.iftarOffsetMinutes)

        val sehriParts = sehriTimeStr.split(":")
        val sehriMinutes = sehriParts[0].toInt() * 60 + sehriParts[1].toInt()

        val iftarParts = iftarTimeStr.split(":")
        val iftarMinutes = iftarParts[0].toInt() * 60 + iftarParts[1].toInt()

        val nextMilestoneBn: String
        val countdownTextBn: String
        val progressFraction: Float

        if (currentMinutes < sehriMinutes) {
            // Before Sehri end
            val diff = sehriMinutes - currentMinutes
            val h = diff / 60
            val m = diff % 60
            nextMilestoneBn = "সেহরির শেষ সময়"
            countdownTextBn = if (h > 0) "${toBengaliDigits(h.toString())} ঘণ্টা ${toBengaliDigits(m.toString())} মিনিট বাকি" else "${toBengaliDigits(m.toString())} মিনিট বাকি"
            progressFraction = 0.05f
        } else if (currentMinutes < iftarMinutes) {
            // Fasting period (between Sehri and Iftar)
            val diff = iftarMinutes - currentMinutes
            val h = diff / 60
            val m = diff % 60
            nextMilestoneBn = "ইফতারের সময়"
            countdownTextBn = if (h > 0) "${toBengaliDigits(h.toString())} ঘণ্টা ${toBengaliDigits(m.toString())} মিনিট বাকি" else "${toBengaliDigits(m.toString())} মিনিট বাকি"
            val totalFastingMinutes = (iftarMinutes - sehriMinutes).toFloat()
            val elapsed = (currentMinutes - sehriMinutes).toFloat()
            progressFraction = (elapsed / totalFastingMinutes).coerceIn(0f, 1f)
        } else {
            // After Iftar, looking forward to tomorrow's Sehri
            val tomorrowSehriMinutes = sehriMinutes + 24 * 60
            val diff = tomorrowSehriMinutes - currentMinutes
            val h = diff / 60
            val m = diff % 60
            nextMilestoneBn = "আগামীকালের সেহরি"
            countdownTextBn = if (h > 0) "${toBengaliDigits(h.toString())} ঘণ্টা ${toBengaliDigits(m.toString())} মিনিট বাকি" else "${toBengaliDigits(m.toString())} মিনিট বাকি"
            progressFraction = 1f
        }

        val countdowns = IslamicCalendarEngine.getEventCountdowns(hijriDayOffset)
        val ramadanCountdown = countdowns.firstOrNull { it.event.id == "ramadan_start" }
        val daysUntil = ramadanCountdown?.daysRemaining ?: 0

        val hijriFormatted = toBengaliDigits("${if (isRamadan) hijriDay else 1} রমজানুল মুবারক $hijriYear হিজরি")

        return TodayRamadanStatus(
            isRamadanNow = isRamadan,
            currentRamadanDay = currentDay,
            hijriDateFormatted = hijriFormatted,
            ashra = ashra,
            nextMilestoneBn = nextMilestoneBn,
            countdownTextBn = countdownTextBn,
            todaySehri = todaySchedule.sehriEndTime,
            todayIftar = todaySchedule.iftarTime,
            progressFraction = progressFraction,
            daysUntilRamadan = daysUntil
        )
    }

    fun getEssentialDuas(): List<RamadanDuaItem> = listOf(
        RamadanDuaItem(
            id = "sehri_niyyat",
            titleBn = "রোজার নিয়ত",
            categoryBn = "সেহরি ও নিয়ত",
            arabicText = "نَوَيْتُ أَنْ أَصُومَ غَدًا مِنْ شَهْرِ رَمَضَانَ الْمُبَارَكِ فَرْضًا لَكَ يَا اللهُ فَتَقَبَّلْ مِنِّي إِنَّكَ أَنْتَ السَّمِيعُ الْعَلِيمُ",
            transliterationBn = "নাওয়াইতু আন আসূমা গাদাম মিন শাহরি রমাদানাল মুবারকি ফারদাল্লাকা ইয়া আল্লাহু ফাতাকাব্বাল মিন্নী, ইন্নাকা আনতাস সামীউল আলীম।",
            translationBn = "হে আল্লাহ! আমি আগামীকাল পবিত্র মাহে রমজানের তোমার সন্তুষ্টির উদ্দেশ্যে ফরজ রোজা রাখার নিয়ত করলাম। অতএব আমার পক্ষ থেকে তা কবুল করুন, নিশ্চয়ই আপনি সর্বশ্রোতা ও সর্বজ্ঞানী।",
            referenceBn = "ফতোয়ায়ে শামী ও হিদায়া (রোজা রাখার অন্তরের ইচ্ছাই যথেষ্ট, মুখে নিয়ত করা মুস্তাহাব)"
        ),
        RamadanDuaItem(
            id = "iftar_dua_famous",
            titleBn = "ইফতারের দোয়া (প্রসিদ্ধ)",
            categoryBn = "ইফতার",
            arabicText = "اللَّهُمَّ لَكَ صُمْتُ وَعَلَى رِزْقِكَ أَفْطَرْتُ",
            transliterationBn = "আল্লাহুম্মা লাকা সুমতু ওয়া 'আলা রিযক্বিকা আফতারতু।",
            translationBn = "হে আল্লাহ! আমি আপনারই জন্য রোজা রেখেছি এবং আপনারই দেওয়া রিজিক দ্বারা ইফতার করছি।",
            referenceBn = "সুনানে আবু দাউদ: ২৩৫৮"
        ),
        RamadanDuaItem(
            id = "iftar_dua_sunnah",
            titleBn = "ইফতারের পর তৃষ্ণা নিবারণের সুন্নাত দোয়া",
            categoryBn = "ইফতার",
            arabicText = "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الأَجْرُ إِنْ شَاءَ اللَّهُ",
            transliterationBn = "যাহাবায যমউ ওয়াবতাল্লাতিল 'উ রূকু ওয়া ছাবাতাল আজরু ইনশাআল্লাহ।",
            translationBn = "তৃষ্ণা নিবারিত হলো, শিরা-উপশিরা সিক্ত হলো এবং ইনশাআল্লাহ রোজার সওয়াব নিশ্চিত হলো।",
            referenceBn = "সুনানে আবু দাউদ: ২৩৫৭, সুনানে নাসায়ী ও সহিহুল জামে: ৪৬৭৮"
        ),
        RamadanDuaItem(
            id = "tarabi_tasbih",
            titleBn = "তারাবীহর প্রতি চার রাকাত পর পর তাসবীহ",
            categoryBn = "তারাবীহ",
            arabicText = "سُبْحَانَ ذِي الْمُلْكِ وَالْمَلَكُوتِ ، سُبْحَانَ ذِي الْعِزَّةِ وَالْعَظَمَةِ وَالْهَيْبَةِ وَالْقُدْرَةِ وَالْكِبْرِيَاءِ وَالْجَبَرُوتِ ، سُبْحَانَ الْمَلِكِ الْحَيِّ الَّذِي لَا يَنَامُ وَلَا يَمُوتُ ، سُبُّوحٌ قُدُّوسٌ رَبُّنَا وَرَبُّ الْمَلَائِكَةِ وَالرُّوحِ",
            transliterationBn = "সুবহানা যিল মুলকি ওয়াল মালাকূত, সুবহানা যিল ইয্যাতি ওয়াল 'আযমাতি ওয়াল হাইবাতি ওয়াল কুদরাতি ওয়াল কিবরিয়ায়ি ওয়াল জাবারূত। সুবহানাল মালিকিল হাইয়্যিল্লাযী লা ইয়ানামু ওয়ালা ইয়ামূতু, সুব্বূহুন কুদ্দূসুর রব্বুনা ওয়া রব্বুল মালাইকাতি ওয়ার রূহ।",
            translationBn = "পবিত্র সেই মহান সত্তা যিনি পার্থিব ও আকাশমণ্ডলের রাজ্যের অধিপতি। পবিত্র সেই মহান সত্তা যিনি মর্যাদা, শ্রেষ্ঠত্ব, প্রভাব, মহাশক্তি ও পরম প্রতাপের অধিকারী। পবিত্র সেই চিরঞ্জীব সম্রাট যিনি ঘুমান না এবং কখনো মৃত্যুবরণ করবেন না। পরম পবিত্র ও নিখুঁত আমাদের প্রতিপালক এবং ফেরেশতাকুল ও জিবরাইল (আ.)-এর প্রতিপালক।",
            referenceBn = "ঐতিহ্যবাহী মুস্তাহাব আমল ও ইসলামী ফতোয়া গ্রন্থাবলি"
        ),
        RamadanDuaItem(
            id = "tarabi_munajat",
            titleBn = "তারাবীহ নামাজের বিশেষ মোনাজাত",
            categoryBn = "তারাবীহ",
            arabicText = "اللَّهُمَّ إِنَّا نَسْأَلُكَ الْجَنَّةَ وَنَعُوذُ بِكَ مِنَ النَّارِ ، يَا خَالِقَ الْجَنَّةِ وَالنَّارِ ، بِرَحْمَتِكَ يَا عَزِيزُ يَا غَفَّارُ يَا كَرِيمُ يَا سَتَّارُ يَا رَحِيمُ يَا جَبَّارُ يَا خَالِقُ يَا بَارُّ ، اللَّهُمَّ أَجِرْنَا مِنَ النَّارِ يَا مُجِيرُ يَا مُجِيرُ يَا مُجِيرُ ، بِرَحْمَتِكَ يَا أَرْحَمَ الرَّاحِمِينَ",
            transliterationBn = "আল্লাহুম্মা ইন্না নাসআলুকাল জান্নাতা ওয়া নাঊযু বিকা মিনান নার। ইয়া খালিক্বাল জান্নাতি ওয়ান নার, বিরাহমাতিকা ইয়া আযীযু ইয়া গাফফারু ইয়া কারীমু ইয়া সাত্তারু ইয়া রাহীমু ইয়া জাব্বারু ইয়া খালিকু ইয়া বার্র। আল্লাহুম্মা আজিরনা মিনান নার, ইয়া মুজীরু ইয়া মুজীরু ইয়া মুজীর, বিরাহমাতিকা ইয়া আরহামার রাহিমীন।",
            translationBn = "হে আল্লাহ! আমরা আপনার নিকট জান্নাত প্রার্থনা করছি এবং জাহান্নামের আগুন থেকে আশ্রয় চাচ্ছি। হে জান্নাত ও জাহান্নামের স্রষ্টা! আপনার অশেষ রহমতের উসিলায়, হে মহাশক্তিশালী, পরম ক্ষমাশীল, পরম দাতা, গোপনকারী, করুণাময়। হে আল্লাহ! আমাদের জাহান্নাম হতে রক্ষা করুন, হে পরিত্রাতা, হে উদ্ধারকারী! আপনার রহমতের উসিলায় হে পরম দয়ালু।",
            referenceBn = "তারাবীহ নামাজের জামাত শেষে বহুল পঠিত মাসনূন প্রার্থনা"
        ),
        RamadanDuaItem(
            id = "laylatul_qadr",
            titleBn = "পবিত্র শবে কদরের দোয়া",
            categoryBn = "শবে কদর",
            arabicText = "اللَّهُمَّ إِنَّكَ عَفُوٌّ كَرِيمٌ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي",
            transliterationBn = "আল্লাহুম্মা ইন্নাকা আফুউন কারীমুন তুহিব্বুল আফওয়া ফা'ফু আন্নী।",
            translationBn = "হে আল্লাহ! নিশ্চয়ই আপনি পরম ক্ষমাশীল, দয়াবান, আপনি ক্ষমা করাকে ভালোবাসেন। অতএব আপনি আমাকে ক্ষমা করে দিন।",
            referenceBn = "হযরত আয়েশা (রা.) কর্তৃক রাসুল (সা.)-এর নিকট হতে বর্ণিত (জামে তিরমিজি: ৩৫১৩, ইবনে মাজাহ: ৩৮৫০)"
        ),
        RamadanDuaItem(
            id = "crescent_moon",
            titleBn = "রমজানের নতুন চাঁদ দেখার দোয়া",
            categoryBn = "চাঁদ দেখা",
            arabicText = "اللَّهُ أَكْبَرُ ، اللَّهُمَّ أَهِلَّهُ عَلَيْنَا بِالأَمْنِ وَالإِيمَانِ ، وَالسَّلامَةِ وَالإِسْلامِ ، وَالتَّوْفِيقِ لِمَا تُحِبُّ وَتَرْضَى ، رَبُّنَا وَرَبُّكَ اللَّهُ",
            transliterationBn = "আল্লাহু আকবার, আল্লাহুম্মা আহিল্লাহু 'আলাইনা বিল আমনি ওয়াল ঈমান, ওয়াস সালামাতি ওয়াল ইসলাম, ওয়াত তাওফীক্বি লিমা তুহিব্বু ওয়া তারদা, রব্বুনা ওয়া রব্বুকাল্লাহ।",
            translationBn = "আল্লাহ মহান! হে আল্লাহ, এই নতুন চাঁদকে আমাদের ওপর নিরাপত্তা, ঈমান, শান্তি ও ইসলামের সাথে এবং আপনি যা ভালোবাসেন ও যাতে সন্তুষ্ট হন সেই তাওফিকের সাথে উদিত করুন। (হে চাঁদ!) আমাদের এবং তোমার রব হচ্ছেন এক আল্লাহ।",
            referenceBn = "জামে তিরমিজি: ৩৪৫১, সুনানে দারেমি: ১৬৮৭"
        ),
        RamadanDuaItem(
            id = "provocation_patience",
            titleBn = "রোজা অবস্থায় রাগ বা প্ররোচনায় পাঠ করার বাক্য",
            categoryBn = "সদাচার",
            arabicText = "إِنِّي صَائِمٌ ، إِنِّي صَائِمٌ",
            transliterationBn = "ইন্নী সায়িম, ইন্নী সায়িম।",
            translationBn = "নিশ্চয়ই আমি রোজাদার, নিশ্চয়ই আমি রোজাদার। (কাউকে গালি না দিয়ে বা বিবাদে না জড়িয়ে সংযত থাকা)",
            referenceBn = "সহিহ বুখারি: ১৮৯৪, সহিহ মুসলিম: ১১৫১"
        )
    )

    fun getFastingMasail(): List<RamadanMasailCategory> = listOf(
        RamadanMasailCategory(
            titleBn = "যেসব কারণে রোজা ভাঙে এবং কাজা ও কাফফারা উভয়ই ওয়াজিব হয়",
            iconType = "warning",
            points = listOf(
                "রোজা স্মরণ থাকা অবস্থায় ইচ্ছাকৃতভাবে কোনো ওষুধ, খাদ্য বা পানীয় গ্রহণ করা।",
                "ইচ্ছাকৃতভাবে স্ত্রী সহবাস করা (এতে রোজা ভেঙে যাবে এবং ১টি রোজার কাজার সাথে একটানা ৬০টি রোজা কাফফারা বা ৬০ জন মিসকিনকে আহার করাতে হবে)।",
                "তামাক, বিড়ি, সিগারেট বা হুক্কা ইচ্ছাকৃতভাবে পান করা।"
            )
        ),
        RamadanMasailCategory(
            titleBn = "যেসব কারণে রোজা ভাঙে এবং শুধুমাত্র ১টি কাজা ওয়াজিব হয় (কাফফারা লাগে না)",
            iconType = "info",
            points = listOf(
                "ভুলবশত কিছু খাওয়ার পর রোজা ভেঙে গেছে ভেবে ইচ্ছাকৃতভাবে পুনরায় খাওয়া বা পান করা।",
                "কুলি করার সময় অনিচ্ছাকৃতভাবে গলার ভেতরে পানি চলে যাওয়া (যদি রোজা থাকার কথা মনে থাকে)।",
                "ইচ্ছাকৃতভাবে মুখ ভরে বমি করা।",
                "নাক বা কানের ছিদ্র দিয়ে তরল ওষুধ বা তেল মস্তিষ্কে পৌঁছে যাওয়া।",
                "সেহরির সময় আছে মনে করে সুবহে সাদিকের পর খাওয়া, অথবা সূর্যাস্ত হয়েছে মনে করে ইফতারের সময়ের পূর্বে ইফতার করা।"
            )
        ),
        RamadanMasailCategory(
            titleBn = "যেসব কাজ করলে রোজা ভাঙে না এবং মাকরূহও হয় না",
            iconType = "check",
            points = listOf(
                "মিসওয়াক করা (শুকনো বা তাজা উভয়ই দিনের যেকোনো সময় ব্যবহার করা জায়েজ)।",
                "সুগন্ধি, আতর ব্যবহার করা বা ফুলের ঘ্রাণ নেওয়া।",
                "চোখে সুরমা বা ড্রপ ব্যবহার করা।",
                "শরীরে তেল বা লোশন মালিশ করা।",
                "অনিচ্ছাকৃতভাবে বমি হওয়া অথবা ধুলাবালি, মশা-মাছি অনিচ্ছাকৃতভাবে গলার ভেতর প্রবেশ করা।",
                "ভুলবশত কিছু খেয়ে ফেলা (মনে আসার সাথে সাথে মুখ থেকে বের করে ফেলা আবশ্যক)।",
                "ইনজেকশন বা ইনসুলিন গ্রহণ করা (যদি তা কেবল পুষ্টি বা খাদ্যের বিকল্প না হয়)।"
            )
        ),
        RamadanMasailCategory(
            titleBn = "যেসব কারণে রোজা মাকরূহ হয় কিন্তু রোজা ভাঙে না",
            iconType = "caution",
            points = listOf(
                "বিনা প্রয়োজনে জিহ্বা দিয়ে কোনো খাবারের স্বাদ পরীক্ষা করা।",
                "টুথপেস্ট বা মাজন দিয়ে দাঁত ব্রাশ করা (গলায় স্বাদ চলে যাওয়ার আশঙ্কা থাকায় মাকরূহ; তবে গিলে ফেললে রোজা ভেঙে যাবে)।",
                "মুখের মধ্যে অতিরিক্ত থুথু জমা করে তা গিলে ফেলা।",
                "রোজা অবস্থায় অনর্থক ঝগড়া-বিবাদ, মিথ্যা কথা, গীবত ও অশ্লীল কথাবার্তা বলা (এতে রোজার আত্মিক সওয়াব বিনষ্ট হয়ে যায়)।"
            )
        )
    )

    fun getFitrahGuidelines(): List<FitrahItem> = listOf(
        FitrahItem(
            commodityBn = "উন্নত আটা বা গম",
            arabicName = "الْحِنْطَة (আল-হিনত্বাহ)",
            quantityBn = "১ কেজি ৬৫০ গ্রাম (অর্ধ সা')",
            approxPriceMin = 115,
            approxPriceMax = 130,
            referenceBn = "ইসলামিক ফাউন্ডেশন নির্ধারিত সর্বনিম্ন ফিতরা হার"
        ),
        FitrahItem(
            commodityBn = "যব (বার্লি)",
            arabicName = "الشَّعِير (আশ-শাঈর)",
            quantityBn = "৩ কেজি ৩০০ গ্রাম (এক সা')",
            approxPriceMin = 390,
            approxPriceMax = 440,
            referenceBn = "সহিহ বুখারি ও মুসলিমের সহিহ হাদিস অনুসারে"
        ),
        FitrahItem(
            commodityBn = "কিসমিস",
            arabicName = "الزَّبِيب (আজ-যাবীব)",
            quantityBn = "৩ কেজি ৩০০ গ্রাম (এক সা')",
            approxPriceMin = 2100,
            approxPriceMax = 2300,
            referenceBn = "উচ্চবিত্ত ও সামর্থ্যবানদের জন্য সর্বোত্তম বিকল্প"
        ),
        FitrahItem(
            commodityBn = "খেজুর",
            arabicName = "التَّمْر (আত-তামর)",
            quantityBn = "৩ কেজি ৩০০ গ্রাম (এক সা')",
            approxPriceMin = 2400,
            approxPriceMax = 2600,
            referenceBn = "উন্নত মানের আজওয়া/মরিয়ম খেজুরের গড় হিসাব"
        ),
        FitrahItem(
            commodityBn = "পনির",
            arabicName = "الأَقِط (আল-আক্বিত)",
            quantityBn = "৩ কেজি ৩০০ গ্রাম (এক সা')",
            approxPriceMin = 2900,
            approxPriceMax = 3100,
            referenceBn = "শরিয়াহ সর্বোচ্চ ফিতরা পণ্য মানদণ্ড"
        )
    )

    fun generateRamadanNoticeDraft(
        mosqueName: String,
        district: BangladeshDistrictOffset = bangladeshDistricts.first()
    ): Pair<String, String> {
        val title = "পবিত্র মাহে রমজানুল মুবারক ও সেহরি-ইফতারের সময়সূচির বিজ্ঞপ্তি"
        val body = buildString {
            appendLine("বিসমিল্লাহির রাহমানির রাহিম।")
            appendLine("আসসালামু আলাইকুম ওয়া রাহমাতুল্লাহ।")
            appendLine("সম্মানিত এলাকাবাসী ও মুসল্লিবৃন্দ,")
            appendLine("পবিত্র মাহে রমজানুল মুবারক উপলক্ষে $mosqueName-এর পক্ষ থেকে সকলকে জানাই আন্তরিক মুবারকবাদ।")
            appendLine()
            appendLine("🕌 আমাদের মসজিদে রমজানের বিশেষ আয়োজনসমূহ:")
            appendLine("১. প্রতিদিন খতমে তারাবীহ নামাজ সুমধুর কণ্ঠে তিলাওয়াতসহ যথাসময়ে অনুষ্ঠিত হবে।")
            appendLine("২. বাদ আসর সংক্ষিপ্ত তাফসিরুল কুরআন ও রমজানের মাসায়েল আলোচনা অনুষ্ঠিত হবে।")
            appendLine("৩. মসজিদ প্রাঙ্গণে রোজাদারদের জন্য সম্মিলিত ইফতার ও দোয়ার উন্মুক্ত ব্যবস্থা রয়েছে।")
            appendLine("৪. শেষ দশকে নাজাতের দশক উপলক্ষে সুন্নাত ইতিকাফের সুব্যবস্থা থাকবে।")
            appendLine()
            appendLine("📍 জেলা ভিত্তিক সময় সমন্বয়: ${district.nameBn} (সেহরি ${if (district.sehriOffsetMinutes >= 0) "+${district.sehriOffsetMinutes}" else "${district.sehriOffsetMinutes}"} মিনিট, ইফতার ${if (district.iftarOffsetMinutes >= 0) "+${district.iftarOffsetMinutes}" else "${district.iftarOffsetMinutes}"} মিনিট)।")
            appendLine("সদকাতুল ফিতরা ঈদের নামাজের পূর্বেই অসহায় ও হকদার মিসকিনদের নিকট পৌঁছে দেওয়ার জন্য বিনীত অনুরোধ করা হচ্ছে।")
            appendLine()
            appendLine("— পরিচালনা পরিষদ, $mosqueName")
        }
        return Pair(title, body)
    }
}
