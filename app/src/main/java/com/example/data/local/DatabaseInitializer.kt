package com.example.data.local

import com.example.data.local.entity.*

object DatabaseInitializer {

    suspend fun populateInitialData(database: MosqueDatabase) {
        val mosqueDao = database.mosqueDao()
        val memberDao = database.memberDao()
        val transactionDao = database.transactionDao()
        val chandaDao = database.chandaDao()
        val staffDao = database.staffDao()
        val noticeDao = database.noticeDao()
        val meetingDao = database.meetingDao()
        val auditDao = database.auditDao()

        // 1. Mosque
        mosqueDao.insertOrUpdate(
            MosqueEntity(
                id = "mosque-001",
                committeeId = "MC-DHK-2024",
                nameBn = "বায়তুল জান্নাত জামে মসজিদ",
                nameEn = "Baitul Jannat Jame Masjid",
                address = "রোড-৭, ব্লক-বি, মিরপুর-১০, ঢাকা-১২১৬",
                establishedYear = 1998,
                committeeStartYear = 2024,
                committeeEndYear = 2026,
                status = "active"
            )
        )

        // 2. Members
        val members = listOf(
            MemberEntity(
                id = "mem-101",
                memberCode = "MEM-101",
                nameBn = "মো: রফিকুল ইসলাম",
                nameEn = "Md. Rafiqul Islam",
                mobile = "01711-223344",
                email = "rafiqul.islam@example.com",
                nid = "19752691234567890",
                address = "বাড়ি-১২, মিরপুর-১০",
                villageArea = "মিরপুর",
                roleCode = "president",
                monthlyChanda = 2000.0,
                joinDate = "2024-01-01",
                status = "active"
            ),
            MemberEntity(
                id = "mem-102",
                memberCode = "MEM-102",
                nameBn = "আলহাজ্ব আব্দুল বারী",
                nameEn = "Alhaj Abdul Bari",
                mobile = "01819-334455",
                email = "abdul.bari@example.com",
                nid = "19682691234567891",
                address = "বাড়ি-২৫, মিরপুর-১০",
                villageArea = "মিরপুর",
                roleCode = "vice_president",
                monthlyChanda = 1500.0,
                joinDate = "2024-01-01",
                status = "active"
            ),
            MemberEntity(
                id = "mem-103",
                memberCode = "MEM-103",
                nameBn = "ইঞ্জি: আব্দুল্লাহ আল মামুন",
                nameEn = "Engr. Abdullah Al Mamun",
                mobile = "01912-445566",
                email = "mamun.engr@example.com",
                nid = "19842691234567892",
                address = "ফ্ল্যাট-৪বি, ধানসিঁড়ি কমপ্লেক্স",
                villageArea = "মিরপুর",
                roleCode = "general_secretary",
                monthlyChanda = 1500.0,
                joinDate = "2024-01-01",
                status = "active"
            ),
            MemberEntity(
                id = "mem-104",
                memberCode = "MEM-104",
                nameBn = "হাজী মো: আবুল কাসেম",
                nameEn = "Haji Md. Abul Kashem",
                mobile = "01715-556677",
                email = "kashem.tr@example.com",
                nid = "19722691234567893",
                address = "বাড়ি-০৮, লেন-৩",
                villageArea = "মিরপুর",
                roleCode = "treasurer",
                monthlyChanda = 2000.0,
                joinDate = "2024-01-01",
                status = "active"
            ),
            MemberEntity(
                id = "mem-105",
                memberCode = "MEM-105",
                nameBn = "মো: আসাদুজ্জামান",
                nameEn = "Md. Asaduzzaman",
                mobile = "01611-778899",
                email = "asad.org@example.com",
                nid = "19882691234567894",
                address = "বাড়ি-১৭, ব্লক-সি",
                villageArea = "মিরপুর",
                roleCode = "organizing_secretary",
                monthlyChanda = 1000.0,
                joinDate = "2024-01-15",
                status = "active"
            ),
            MemberEntity(
                id = "mem-106",
                memberCode = "MEM-106",
                nameBn = "ডা: তারিকুল ইসলাম",
                nameEn = "Dr. Tariqul Islam",
                mobile = "01552-889900",
                email = "dr.tariq@example.com",
                nid = "19812691234567895",
                address = "বাড়ি-৩, রোড-৫",
                villageArea = "মিরপুর",
                roleCode = "executive_member",
                monthlyChanda = 1500.0,
                joinDate = "2024-02-01",
                status = "active"
            ),
            MemberEntity(
                id = "mem-107",
                memberCode = "MEM-107",
                nameBn = "মো: শাহজাহান আলী",
                nameEn = "Md. Shahjahan Ali",
                mobile = "01723-001122",
                email = "shahjahan@example.com",
                nid = "19902691234567896",
                address = "বাড়ি-৪৫, রোড-৭",
                villageArea = "মিরপুর",
                roleCode = "general_member",
                monthlyChanda = 500.0,
                joinDate = "2024-02-10",
                status = "active"
            ),
            MemberEntity(
                id = "mem-108",
                memberCode = "MEM-108",
                nameBn = "কামাল হোসেন",
                nameEn = "Kamal Hossain",
                mobile = "01834-112233",
                email = "kamal.h@example.com",
                nid = "19922691234567897",
                address = "বাড়ি-১৮, রোড-২",
                villageArea = "মিরপুর",
                roleCode = "general_member",
                monthlyChanda = 500.0,
                joinDate = "2024-03-01",
                status = "active"
            ),
            MemberEntity(
                id = "mem-109",
                memberCode = "MEM-109",
                nameBn = "মো: মাহবুব আলম",
                nameEn = "Md. Mahbub Alam",
                mobile = "01945-223344",
                email = "mahbub.alam@example.com",
                nid = "19862691234567898",
                address = "বাড়ি-৯, রোড-৮",
                villageArea = "মিরপুর",
                roleCode = "general_member",
                monthlyChanda = 1000.0,
                joinDate = "2024-03-15",
                status = "active"
            )
        )
        members.forEach { memberDao.insertMember(it) }

        // 3. Staff
        val staffList = listOf(
            StaffEntity(
                id = "staff-01",
                nameBn = "মাওলানা মুফতি মিজানুর রহমান",
                roleCode = "imam",
                designationBn = "পেশ ইমাম ও খতীব",
                employmentType = "full_time",
                joinDate = "2021-04-01",
                salaryAmount = 28000.0,
                salaryFrequency = "monthly",
                mealRequired = true,
                mobile = "01712-998877",
                isActive = true
            ),
            StaffEntity(
                id = "staff-02",
                nameBn = "কারী মো: ইসমাইল হোসেন",
                roleCode = "muazzin",
                designationBn = "মুয়াজ্জিন ও সহকারী কারী",
                employmentType = "full_time",
                joinDate = "2022-06-15",
                salaryAmount = 16000.0,
                salaryFrequency = "monthly",
                mealRequired = true,
                mobile = "01823-887766",
                isActive = true
            ),
            StaffEntity(
                id = "staff-03",
                nameBn = "মো: মোস্তফা মিয়া",
                roleCode = "khadem",
                designationBn = "প্রধান খাদেম ও তত্ত্বাবধায়ক",
                employmentType = "full_time",
                joinDate = "2020-01-10",
                salaryAmount = 12000.0,
                salaryFrequency = "monthly",
                mealRequired = true,
                mobile = "01934-776655",
                isActive = true
            )
        )
        staffList.forEach { staffDao.insertStaff(it) }

        // 4. Finance Transactions
        val transactions = listOf(
            TransactionEntity(
                id = "txn-001",
                transactionCode = "TXN-2024-001",
                categoryNameBn = "জুমার দান বাক্স",
                type = "INCOME",
                amount = 22500.0,
                txnDate = "2024-09-06",
                description = "পবিত্র জুমার নামাজের পর দান বাক্স উন্মুক্তকরণ ও গণনা",
                sourceType = "jummah_collection",
                createdBy = "হাজী মো: আবুল কাসেম (কোষাধ্যক্ষ)",
                approvedBy = "মো: রফিকুল ইসলাম (সভাপতি)",
                approvalStatus = "approved"
            ),
            TransactionEntity(
                id = "txn-002",
                transactionCode = "TXN-2024-002",
                categoryNameBn = "মাসিক সদস্য চাঁদা",
                type = "INCOME",
                amount = 45000.0,
                txnDate = "2024-09-05",
                description = "সেপ্টেম্বর মাসের সদস্য চাঁদা আদায়ের সংগৃহীত অর্থ জমা",
                sourceType = "chanda_payment",
                createdBy = "ইঞ্জি: আব্দুল্লাহ আল মামুন",
                approvedBy = "মো: রফিকুল ইসলাম (সভাপতি)",
                approvalStatus = "approved"
            ),
            TransactionEntity(
                id = "txn-003",
                transactionCode = "TXN-2024-003",
                categoryNameBn = "মসজিদ এসি সংস্কার অনুদান",
                type = "INCOME",
                amount = 35000.0,
                txnDate = "2024-09-03",
                description = "২য় তলার নতুন এসি স্থাপনের জন্য বিশেষ এককালীন অনুদান",
                sourceType = "special_donation",
                createdBy = "হাজী মো: আবুল কাসেম",
                approvedBy = "মো: রফিকুল ইসলাম",
                approvalStatus = "approved"
            ),
            TransactionEntity(
                id = "txn-004",
                transactionCode = "TXN-2024-004",
                categoryNameBn = "ইমাম ও কর্মচারীদের বেতন",
                type = "EXPENSE",
                amount = 56000.0,
                txnDate = "2024-09-01",
                description = "আগস্ট মাসের পেশ ইমাম, মুয়াজ্জিন ও খাদেমের পূর্ণ বেতন পরিশোধ",
                sourceType = "payroll",
                createdBy = "হাজী মো: আবুল কাসেম",
                approvedBy = "মো: রফিকুল ইসলাম",
                approvalStatus = "approved"
            ),
            TransactionEntity(
                id = "txn-005",
                transactionCode = "TXN-2024-005",
                categoryNameBn = "বিদ্যুৎ ও ইউটিলিটি বিল",
                type = "EXPENSE",
                amount = 11450.0,
                txnDate = "2024-09-02",
                description = "ডেসকো আগস্ট মাসের মসজিদ বিদ্যুৎ বিল পরিশোধ",
                sourceType = "utility",
                createdBy = "হাজী মো: আবুল কাসেম",
                approvedBy = "মো: রফিকুল ইসলাম",
                approvalStatus = "approved"
            ),
            TransactionEntity(
                id = "txn-006",
                transactionCode = "TXN-2024-006",
                categoryNameBn = "মসজিদ রক্ষণাবেক্ষণ ও ওয়াসরুম",
                type = "EXPENSE",
                amount = 4800.0,
                txnDate = "2024-09-04",
                description = "অজুখানার পানির নতুন ট্যাপ, পাইপ ও ফিনাইল ক্রয়",
                sourceType = "maintenance",
                createdBy = "মো: আসাদুজ্জামান",
                approvedBy = "মো: রফিকুল ইসলাম",
                approvalStatus = "approved"
            ),
            TransactionEntity(
                id = "txn-007",
                transactionCode = "TXN-2024-007",
                categoryNameBn = "জেনারেটর সার্ভিসিং ও ব্যাটারি",
                type = "EXPENSE",
                amount = 18500.0,
                txnDate = "2024-09-07",
                description = "লোডশেডিংকালে নিরবচ্ছিন্ন বিদ্যুৎ সংযোগ রক্ষার্থে ডিজেল জেনারেটর সার্ভিসিং ও নতুন ব্যাটারি",
                sourceType = "manual",
                createdBy = "ইঞ্জি: আব্দুল্লাহ আল মামুন (সাধারণ সম্পাদক)",
                approvalStatus = "pending"
            )
        )
        transactions.forEach { transactionDao.insertTransaction(it) }

        // 5. Chanda Payments
        val chandaRecords = listOf(
            ChandaPaymentEntity(
                id = "cp-001",
                memberId = "mem-101",
                memberNameBn = "মো: রফিকুল ইসলাম",
                month = "সেপ্টেম্বর ২০২৪",
                amountDue = 2000.0,
                amountPaid = 2000.0,
                method = "cash",
                status = "paid",
                receiptCode = "RCP-2024-091",
                qrVerificationCode = "VER-MC-DHK-2024-091-88A",
                receivedBy = "হাজী মো: আবুল কাসেম",
                paidAt = "2024-09-01 10:30 AM"
            ),
            ChandaPaymentEntity(
                id = "cp-002",
                memberId = "mem-102",
                memberNameBn = "আলহাজ্ব আব্দুল বারী",
                month = "সেপ্টেম্বর ২০২৪",
                amountDue = 1500.0,
                amountPaid = 1500.0,
                method = "mobile_banking",
                status = "paid",
                receiptCode = "RCP-2024-092",
                qrVerificationCode = "VER-MC-DHK-2024-092-33B",
                receivedBy = "হাজী মো: আবুল কাসেম",
                paidAt = "2024-09-02 04:15 PM"
            ),
            ChandaPaymentEntity(
                id = "cp-003",
                memberId = "mem-103",
                memberNameBn = "ইঞ্জি: আব্দুল্লাহ আল মামুন",
                month = "সেপ্টেম্বর ২০২৪",
                amountDue = 1500.0,
                amountPaid = 1500.0,
                method = "cash",
                status = "paid",
                receiptCode = "RCP-2024-093",
                qrVerificationCode = "VER-MC-DHK-2024-093-41C",
                receivedBy = "হাজী মো: আবুল কাসেম",
                paidAt = "2024-09-03 08:20 PM"
            ),
            ChandaPaymentEntity(
                id = "cp-004",
                memberId = "mem-104",
                memberNameBn = "হাজী মো: আবুল কাসেম",
                month = "সেপ্টেম্বর ২০২৪",
                amountDue = 2000.0,
                amountPaid = 2000.0,
                method = "cash",
                status = "paid",
                receiptCode = "RCP-2024-094",
                qrVerificationCode = "VER-MC-DHK-2024-094-19D",
                receivedBy = "মো: রফিকুল ইসলাম",
                paidAt = "2024-09-03 09:00 PM"
            ),
            ChandaPaymentEntity(
                id = "cp-005",
                memberId = "mem-105",
                memberNameBn = "মো: আসাদুজ্জামান",
                month = "সেপ্টেম্বর ২০২৪",
                amountDue = 1000.0,
                amountPaid = 1000.0,
                method = "mobile_banking",
                status = "paid",
                receiptCode = "RCP-2024-095",
                qrVerificationCode = "VER-MC-DHK-2024-095-72E",
                receivedBy = "হাজী মো: আবুল কাসেম",
                paidAt = "2024-09-04 11:10 AM"
            )
        )
        chandaRecords.forEach { chandaDao.insertPayment(it) }

        // 6. Notices
        val notices = listOf(
            NoticeEntity(
                id = "not-001",
                title = "পবিত্র ঈদে মিলাদুন্নবী (সা:) উপলক্ষে বিশেষ আলোচনা ও দোয়া মাহফিল",
                content = "সম্মানিত এলাকাবাসী ও মুসল্লিয়ানে কেরাম, আগামী ১২ রবিউল আউয়াল পবিত্র ঈদে মিলাদুন্নবী (সা:) উপলক্ষে বা’দ এশা মসজিদে এক বিশেষ আলোচনা সভা ও দোয়া মাহফিলের আয়োজন করা হয়েছে। প্রধান আলোচক হিসেবে উপস্থিত থাকবেন প্রখ্যাত আলেমে দ্বীন। আপনারা সকলে সপরিবারে আমন্ত্রিত।",
                priority = "important",
                isPinned = true,
                publishDate = "০৫ সেপ্টেম্বর ২০২৪",
                createdBy = "ইঞ্জি: আব্দুল্লাহ আল মামুন (সাধারণ সম্পাদক)"
            ),
            NoticeEntity(
                id = "not-002",
                title = "মসজিদের ২য় তলার এসি ও ফ্যান পরিচ্ছন্নতা অভিযান",
                content = "আগামী শনিবার সকাল ৮টা থেকে যোহর পর্যন্ত মসজিদ প্রাঙ্গণ ও ২য় তলার এসি ফিল্টার পরিচ্ছন্নতা কার্যক্রম অনুষ্ঠিত হবে। যুব সমাজের ভাইদের স্বেচ্ছাশ্রমে অংশগ্রহণ করার অনুরোধ জানানো যাচ্ছে।",
                priority = "urgent",
                isPinned = false,
                publishDate = "০৬ সেপ্টেম্বর ২০২৪",
                createdBy = "মো: আসাদুজ্জামান (সাংগঠনিক সম্পাদক)"
            ),
            NoticeEntity(
                id = "not-003",
                title = "সেপ্টেম্বর মাসের চাঁদা পরিশোধের বিনীত অনুরোধ",
                content = "কমিটির সম্মানিত সদস্য ও নিয়মিত মুসল্লিদের সেপ্টেম্বর মাসের মাসিক চাঁদা কোষাধ্যক্ষের কাছে বা মসজিদ অফিসে জমা দিয়ে অফিসিয়াল রসিদ গ্রহণ করার জন্য অনুরোধ করা যাচ্ছে।",
                priority = "normal",
                isPinned = false,
                publishDate = "০১ সেপ্টেম্বর ২০২৪",
                createdBy = "হাজী মো: আবুল কাসেম (কোষাধ্যক্ষ)"
            )
        )
        notices.forEach { noticeDao.insertNotice(it) }

        // 7. Meetings
        val meetings = listOf(
            MeetingEntity(
                id = "mtg-001",
                title = "মাসিক কার্যনির্বাহী কমিটি ও হিসাব পর্যালোচনা সভা",
                meetingDate = "১৫ সেপ্টেম্বর ২০২৪, বা’দ মাগরিব",
                location = "মসজিদ কমিটি কার্যালয় (২য় তলা)",
                agenda = "১. আগস্ট মাসের আয়-ব্যয় হিসাব অনুমোদন, ২. আসন্ন রবিউল আউয়াল মাহফিল প্রস্তুতি, ৩. জেনারেটর ব্যাটারি বাজেট অনুমোদন।",
                status = "upcoming"
            ),
            MeetingEntity(
                id = "mtg-002",
                title = "মসজিদ সম্প্রসারণ ও হিফজখানা তদারকি সভা",
                meetingDate = "২৫ সেপ্টেম্বর ২০২৪, বা’দ এশা",
                location = "মসজিদ মূল হলরুম",
                agenda = "হিফজখানার নতুন শিক্ষক নিয়োগ ও শিক্ষার্থীদের খাবার ব্যবস্থাপনা বাজেট পর্যালোচনা।",
                status = "scheduled"
            )
        )
        meetings.forEach { meetingDao.insertMeeting(it) }

        // 8. Audit Logs
        val logs = listOf(
            AuditLogEntity(
                action = "INITIALIZE_SYSTEM",
                actor = "System Super Admin",
                details = "মসজিদ কমিটি ম্যানেজমেন্ট সিস্টেম ডাটাবেজ প্রারম্ভিক সক্রিয়করণ সম্পন্ন",
                timestamp = "2024-09-01 09:00:00"
            ),
            AuditLogEntity(
                action = "TRANSACTION_APPROVED",
                actor = "মো: রফিকুল ইসলাম (সভাপতি)",
                details = "TXN-2024-001 (৳২২,৫০০ জুমার দান) অনুমোদন করা হয়েছে",
                timestamp = "2024-09-06 14:10:00"
            ),
            AuditLogEntity(
                action = "NOTICE_PUBLISHED",
                actor = "ইঞ্জি: আব্দুল্লাহ আল মামুন",
                details = "পবিত্র ঈদে মিলাদুন্নবী (সা:) বিশেষ দোয়া মাহফিল নোটিশ প্রকাশিত",
                timestamp = "2024-09-05 17:30:00"
            )
        )
        logs.forEach { auditDao.insertLog(it) }
    }
}
