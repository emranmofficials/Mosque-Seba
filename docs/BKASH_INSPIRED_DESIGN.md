# bKash-অনুপ্রাণিত UI থিম (`bkash-inspired-ui` ব্রাঞ্চ)

এই ব্রাঞ্চে Mosque Seba অ্যাপের ডিজাইন সিস্টেম bKash অ্যাপের আসল Flutter এসেট থেকে নেওয়া রঙ দিয়ে পুনরায় সাজানো হয়েছে। মূল emerald/gold প্যালেট `main` ব্রাঞ্চে অক্ষত আছে।

## ব্র্যান্ড প্যালেট (bKash SVG এসেট থেকে)

| টোকেন | পুরোনো মান | নতুন মান | ব্যবহার |
|---|---|---|---|
| `MosqueEmeraldPrimary` | `#064E3B` | `#E2136E` | ব্র্যান্ড pink, primary বাটন/আইকন |
| `MosqueEmeraldDark` | `#022C22` | `#AD054F` | হেডার, ডার্ক pink |
| `MosqueEmeraldLight` | `#047857` | `#EE2A7B` | উজ্জ্বল pink |
| `MosqueEmeraldSoft` | `#E6F4EA` | `#FDEFF4` | সফট pink টিন্ট |
| `MosqueGreenAccent` | `#10B981` | `#F472B6` | ডার্ক থিম accent |
| `MosqueGold` | `#F59E0B` | `#F472B6` | rose-gold অ্যাকসেন্ট |
| `MosqueGoldDeep` | `#D97706` | `#DB2777` | গাঢ় rose |
| `MosqueGoldDark` | `#B45309` | `#BE185D` | dark rose |
| `MosqueGoldLight` | `#FEF3C7` | `#FCE7F3` | হালকা pink |
| `MosqueBgLight` | `#F8FAF8` | `#F7F7F7` | ক্যানভাস |
| `MosqueCardBorder` | `#E2E8F0` | `#EBEBEB` | কার্ড বর্ডার |
| `MosqueTextPrimary` | `#0F172A` | `#273339` | প্রধান টেক্সট |
| `MosqueTextSecondary` | `#475569` | `#505050` | সেকেন্ডারি টেক্সট |
| `MosqueNightBg` | `#071511` | `#150912` | ডার্ক মোড ব্যাকগ্রাউন্ড |

স্ট্যাটাস কালার: success `#01846C`, danger `#D0021B`, warning `#FFAB00`, info `#0284C7`।

## কীভাবে কাজ করে

সব কম্পোনেন্ট ইতিমধ্যে `Color.kt`-র টোকেন ও `MaterialTheme.colorScheme` ব্যবহার করে। তাই শুধু টোকেনগুলোর মান বদলে পুরো অ্যাপের লুক বদলে গেছে — কোনো স্ক্রিনের কোড বদলাতে হয়নি (মাত্র ৪টি কম্পোনেন্টে হার্ডকোডেড ব্র্যান্ড-গ্রিন লিটারেল আপডেট করা হয়েছে)।

- হেডার/গ্রেডিয়েন্ট: `MosqueEmeraldGradient`, `IslamicMehrabHeaderGradient`, `RoyalGoldShimmerGradient`
- বটম নেভ: `MaterialTheme.colorScheme.primary` → এখন pink
- স্প্ল্যাশ/স্ট্যাটাস বার: `colors.xml`-এ `splash_background = #E2136E`
- নামাজের সময়ভিত্তিক আকাশ গ্রেডিয়েন্ট (`Waqt*SkyGradient`) অপরিবর্তিত, কারণ সেটি সময়ের প্রতীক

## bKash-স্টাইল হোম শেল (নতুন)

`ui/screens/BkashStyleHomeScreen.kt` হোম ট্যাবের কনটেন্ট হিসেবে যোগ করা হয়েছে (`MainActivity`-তে `"dashboard"` ডেস্টিনেশন এখন এই স্ক্রিন ব্যবহার করে)।

| bKash UI প্যাটার্ন | মসজিদ সেবার ফিচার |
|---|---|
| ব্যালেন্স কার্ড | বায়তুল মাল নিট ব্যালেন্স + আয়/ব্যয়/সদস্য |
| সেন্ড মানি / ক্যাশ আউট গ্রিড | চাঁদা, আয়, ব্যয়, সদস্য, ক্যাশবক্স, মাদরাসা, সভা, ইভেন্ট |
| সাম্প্রতিক লেনদেন | `allTransactions` থেকে শেষ ৪টি এন্ট্রি |
| ইনবক্স | প্রকাশিত নোটিশ ও আগামী ইভেন্ট |
| QR / সার্ভিস কার্ড | চাঁদা QR রসিদ (`my_chanda`) ও স্বচ্ছ হিসাব (`transparency`) |

বটম নেভিগেশন ও নেভিগেশন রেলের সোনালি ট্রিমও পিংক ট্রিমে বদলানো হয়েছে (`NavigationBottomBar.kt`)। লগইন স্ক্রিন ইতিমধ্যে টোকেন ব্যবহার করায় স্বয়ংক্রিয়ভাবে পিংক হয়ে গেছে — আলাদা কোড বদলাতে হয়নি।

সব অ্যাকশন আগের `onOpenDialog(key)` কলব্যাকেই যায় — অর্থাৎ ফিচার/লজিক অপরিবর্তিত, শুধু দেখার ধরণ বদলেছে। বাকি ১৬টি স্ক্রিন ও রোল-ভিত্তিক নেভিগেশন যথারীতি আছে।

## পরবর্তীতে যা করা যায়

1. bKash-এর মতো `RoundedCornerShape` ও card elevation টোকেন (`MosqueRadius`, `MosqueElevation`) আরও বাড়ানো
2. লগইন/অনবোর্ডিং স্ক্রিনে bKash-এর `logo.png` শৈলীর ব্র্যান্ড ব্লক যোগ করা
3. ডার্ক মোডের কনট্রাস্ট চেক (WCAG) করা
4. স্ক্রিনশট নিয়ে ডিজাইন রিভিউ
5. বটম নেভের ৫টি ট্যাবকে bKash-এর `Home / QR / ইনবক্স / আমার অ্যাকাউন্ট` ম্যাপিংয়ে নিয়ে যাওয়া (রোল-ভিত্তিক destinations বজায় রেখে)
