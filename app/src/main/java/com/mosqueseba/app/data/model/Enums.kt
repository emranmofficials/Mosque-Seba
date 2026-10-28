package com.mosqueseba.app.data.model

enum class UserRole(val code: String, val titleBn: String, val level: Int) {
    PRESIDENT("president", "সভাপতি", 1),
    VICE_PRESIDENT("vice_president", "সহ-সভাপতি", 2),
    GENERAL_SECRETARY("general_secretary", "সাধারণ সম্পাদক", 2),
    TREASURER("treasurer", "কোষাধ্যক্ষ", 2),
    ORGANIZING_SECRETARY("organizing_secretary", "সাংগঠনিক সম্পাদক", 3),
    EXECUTIVE_MEMBER("executive_member", "কার্যনির্বাহী সদস্য", 3),
    GENERAL_MEMBER("general_member", "সাধারণ সদস্য", 4),
    IMAM("imam", "পেশ ইমাম ও খতীব", 3),
    MUAZZIN("muazzin", "মুয়াজ্জিন", 3),
    KHADEM("khadem", "খাদেম", 4);

    companion object {
        fun fromCode(code: String): UserRole =
            entries.find { it.code.equals(code, ignoreCase = true) } ?: GENERAL_MEMBER
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
