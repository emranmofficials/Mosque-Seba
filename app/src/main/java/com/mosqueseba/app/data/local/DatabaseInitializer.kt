package com.mosqueseba.app.data.local

import com.mosqueseba.app.data.local.entity.*

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
                status = "active",
                willingToProvideStaffMeals = true
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
                status = "active",
                willingToProvideStaffMeals = true
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
                status = "active",
                willingToProvideStaffMeals = true
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
                status = "active",
                willingToProvideStaffMeals = true
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
                status = "active",
                willingToProvideStaffMeals = true
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
                status = "active",
                willingToProvideStaffMeals = true
            ),
            MemberEntity(
                id = "mem-110",
                memberCode = "MEM-110",
                nameBn = "মো: জাকির হোসেন",
                nameEn = "Md. Zakir Hossain",
                mobile = "01716-334411",
                email = "zakir.asstsec@example.com",
                nid = "19852691234567899",
                address = "বাড়ি-২২, ব্লক-এ",
                villageArea = "মিরপুর",
                roleCode = "assistant_secretary",
                monthlyChanda = 1200.0,
                joinDate = "2024-01-05",
                status = "active"
            ),
            MemberEntity(
                id = "mem-111",
                memberCode = "MEM-111",
                nameBn = "মো: নজরুল ইসলাম",
                nameEn = "Md. Nazrul Islam",
                mobile = "01817-445522",
                email = "nazrul.assttr@example.com",
                nid = "19832691234567900",
                address = "বাড়ি-১৪, রোড-৪",
                villageArea = "মিরপুর",
                roleCode = "assistant_treasurer",
                monthlyChanda = 1200.0,
                joinDate = "2024-01-05",
                status = "active"
            ),
            MemberEntity(
                id = "mem-112",
                memberCode = "MEM-112",
                nameBn = "হাফেজ মাওলানা সাইফুল ইসলাম",
                nameEn = "Hafez Mawlana Saiful Islam",
                mobile = "01918-556633",
                email = "saiful.pub@example.com",
                nid = "19892691234567901",
                address = "বাড়ি-৬, লেন-২",
                villageArea = "মিরপুর",
                roleCode = "publicity_secretary",
                monthlyChanda = 1000.0,
                joinDate = "2024-01-10",
                status = "active"
            ),
            MemberEntity(
                id = "mem-113",
                memberCode = "MEM-113",
                nameBn = "মো: তানভীর আহমেদ",
                nameEn = "Md. Tanvir Ahmed",
                mobile = "01619-667744",
                email = "tanvir.office@example.com",
                nid = "19872691234567902",
                address = "বাড়ি-৩০, ব্লক-ডি",
                villageArea = "মিরপুর",
                roleCode = "office_secretary",
                monthlyChanda = 1000.0,
                joinDate = "2024-01-12",
                status = "active"
            ),
            MemberEntity(
                id = "mem-114",
                memberCode = "MEM-114",
                nameBn = "মাওলানা আব্দুর রাজ্জাক",
                nameEn = "Mawlana Abdur Razzak",
                mobile = "01720-778855",
                email = "razzak.welfare@example.com",
                nid = "19792691234567903",
                address = "বাড়ি-১৮, ব্লক-বি",
                villageArea = "মিরপুর",
                roleCode = "welfare_education_secretary",
                monthlyChanda = 1200.0,
                joinDate = "2024-01-12",
                status = "active"
            ),
            MemberEntity(
                id = "mem-115",
                memberCode = "MEM-115",
                nameBn = "মাওলানা মুফতি মিজানুর রহমান",
                nameEn = "Mufti Mizanur Rahman",
                mobile = "01712-998877",
                email = "imam.mizan@example.com",
                nid = "19782691234567904",
                address = "মসজিদ ইমাম কোয়ার্টার",
                villageArea = "মিরপুর",
                roleCode = "imam",
                monthlyChanda = 1000.0,
                joinDate = "2021-04-01",
                status = "active"
            ),
            MemberEntity(
                id = "mem-116",
                memberCode = "MEM-116",
                nameBn = "আলহাজ্ব শামসুল হক চৌধুরী",
                nameEn = "Alhaj Shamsul Haque Chowdhury",
                mobile = "01711-009988",
                email = "shamsul.adv@example.com",
                nid = "19582691234567905",
                address = "বাড়ি-১, রোড-১, মিরপুর-১০",
                villageArea = "মিরপুর",
                roleCode = "advisory_board",
                monthlyChanda = 3000.0,
                joinDate = "2024-01-01",
                status = "active",
                willingToProvideStaffMeals = true
            ),
            MemberEntity(
                id = "mem-117",
                memberCode = "MEM-117",
                nameBn = "কারী মো: ইসমাইল হোসেন",
                nameEn = "Qari Md. Ismail Hossain",
                mobile = "01823-887766",
                email = "",
                nid = "",
                address = "মসজিদ মুয়াজ্জিন কোয়ার্টার",
                villageArea = "মিরপুর",
                roleCode = "muazzin",
                monthlyChanda = 0.0,
                joinDate = "2022-06-15",
                status = "active"
            ),
            MemberEntity(
                id = "mem-118",
                memberCode = "MEM-118",
                nameBn = "মো: মোস্তফা মিয়া",
                nameEn = "Md. Mostafa Mia",
                mobile = "01934-776655",
                email = "",
                nid = "",
                address = "মসজিদ খাদেম কোয়ার্টার",
                villageArea = "মিরপুর",
                roleCode = "khadem",
                monthlyChanda = 0.0,
                joinDate = "2020-01-10",
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
                designationBn = "পেশ ইমাম ও খতিব",
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
                categoryNameBn = "মসজিদ রক্ষণাবেক্ষণ ও অজুখানা",
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
                paidAt = "২০২৪-০৯-০১ ১০:৩০ পূর্বাহ্ণ"
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
                paidAt = "২০২৪-০৯-০২ ০৪:১৫ অপরাহ্ণ"
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
                paidAt = "২০২৪-০৯-০৩ ০৮:২০ অপরাহ্ণ"
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
                paidAt = "২০২৪-০৯-০৩ ০৯:০০ অপরাহ্ণ"
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
                paidAt = "২০২৪-০৯-০৪ ১১:১০ পূর্বাহ্ণ"
            )
        )
        chandaRecords.forEach { chandaDao.insertPayment(it) }

        // 6. Notices
        val notices = listOf(
            NoticeEntity(
                id = "not-001",
                title = "পবিত্র ঈদে মিলাদুন্নবী (সা:) উপলক্ষে বিশেষ আলোচনা ও দোয়া মাহফিল",
                content = "সম্মানিত এলাকাবাসী ও মুসল্লিয়ানে কেরাম, আগামী ১২ রবিউল আউয়াল পবিত্র ঈদে মিলাদুন্নবী (সা:) উপলক্ষে বাদ এশা মসজিদে এক বিশেষ আলোচনা সভা ও দোয়া মাহফিলের আয়োজন করা হয়েছে। প্রধান আলোচক হিসেবে উপস্থিত থাকবেন প্রখ্যাত আলেমে দ্বীন। আপনারা সকলে সপরিবারে আমন্ত্রিত।",
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
                meetingDate = "১৫ সেপ্টেম্বর ২০২৪, বাদ মাগরিব",
                location = "মসজিদ কমিটি কার্যালয় (২য় তলা)",
                agenda = "১. আগস্ট মাসের আয়-ব্যয় হিসাব অনুমোদন, ২. আসন্ন রবিউল আউয়াল মাহফিল প্রস্তুতি, ৩. জেনারেটর ব্যাটারি বাজেট অনুমোদন।",
                status = "upcoming"
            ),
            MeetingEntity(
                id = "mtg-002",
                title = "মসজিদ সম্প্রসারণ ও হিফজখানা তদারকি সভা",
                meetingDate = "২৫ সেপ্টেম্বর ২০২৪, বাদ এশা",
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
                actor = "সিস্টেম প্রশাসক",
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

        // 9. Madrasa Students
        val studentDao = database.studentDao()
        val students = listOf(
            StudentEntity(
                id = "stu-001",
                studentCode = "STU-2024-001",
                nameBn = "আব্দুর রহমান",
                nameEn = "Abdur Rahman",
                className = "হিফজুল কুরআন বিভাগ",
                guardianName = "মো: রফিকুল ইসলাম",
                guardianMobile = "01711-223344",
                monthlyFee = 1000.0,
                joinDate = "2024-01-10",
                status = "active"
            ),
            StudentEntity(
                id = "stu-002",
                studentCode = "STU-2024-002",
                nameBn = "মুহাম্মদ সালমান",
                nameEn = "Muhammad Salman",
                className = "নাজেরা বিভাগ",
                guardianName = "আলহাজ্ব আব্দুল বারী",
                guardianMobile = "01819-334455",
                monthlyFee = 800.0,
                joinDate = "2024-01-15",
                status = "active"
            ),
            StudentEntity(
                id = "stu-003",
                studentCode = "STU-2024-003",
                nameBn = "তাহমিদ হাসান",
                nameEn = "Tahmid Hasan",
                className = "কায়েদা / শিশু মক্তব",
                guardianName = "ইঞ্জি: আব্দুল্লাহ আল মামুন",
                guardianMobile = "01912-445566",
                monthlyFee = 500.0,
                joinDate = "2024-02-01",
                status = "active"
            )
        )
        students.forEach { studentDao.insertStudent(it) }

        // 10. Hifz Progress
        val hifzDao = database.hifzDao()
        val hifzList = listOf(
            HifzProgressEntity(
                id = "hfz-001",
                studentId = "stu-001",
                studentNameBn = "আব্দুর রহমান",
                date = "2024-09-10",
                paraNumber = 12,
                pageOrAyah = "পৃষ্ঠা ৫, আয়াত ৪৫-৫৫",
                qualityScore = 5,
                mistakeCount = 1,
                teacherFeedback = "উচ্চারণ মাখরাজ অত্যন্ত সুন্দর, এগিয়ে যাও।"
            ),
            HifzProgressEntity(
                id = "hfz-002",
                studentId = "stu-002",
                studentNameBn = "মুহাম্মদ সালমান",
                date = "2024-09-10",
                paraNumber = 3,
                pageOrAyah = "সূরা আল-ইমরান ১-১৫",
                qualityScore = 4,
                mistakeCount = 3,
                teacherFeedback = "মদ্দের কায়দা আরও ভালো করতে হবে।"
            )
        )
        hifzList.forEach { hifzDao.insertProgress(it) }

        // 11. Duty Roster
        val dutyDao = database.dutyRosterDao()
        val rosters = listOf(
            DutyRosterEntity(
                id = "rost-001",
                staffId = "stf-001",
                staffNameBn = "মাওলানা মুফতি মাহফুজুর রহমান",
                dayOfWeek = "শুক্রবার (জুমআ)",
                prayerName = "জুমআ ও আসর",
                roleTitle = "খুতবা ও ইমামতি",
                notes = "জুমার খুতবা ও মোনাজাত পরিচালনা"
            ),
            DutyRosterEntity(
                id = "rost-002",
                staffId = "stf-002",
                staffNameBn = "হাফেজ কারী নূরুল হক",
                dayOfWeek = "শুক্রবার (জুমআ)",
                prayerName = "জুমআ",
                roleTitle = "আজান ও সানি আজান",
                notes = "মাইক ও সাউন্ড সিস্টেম তদারকি"
            )
        )
        rosters.forEach { dutyDao.insertRoster(it) }

        // 12. Food Schedules (১ দিন = ১ জন সদস্য = ১ জন স্টাফ, কোনো ওয়াক্ত নেই)
        val foodDao = database.foodScheduleDao()
        val isoFmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH)
        val todayCal = java.util.Calendar.getInstance()
        val todayIso = isoFmt.format(todayCal.time)
        val tomorrowIso = (todayCal.clone() as java.util.Calendar).apply {
            add(java.util.Calendar.DAY_OF_YEAR, 1)
        }.let { isoFmt.format(it.time) }
        val foods = listOf(
            FoodScheduleEntity(
                id = "food-001",
                dateStr = todayIso,
                mealType = "দৈনিক খাবার",
                providerName = "হাজী মো: আবুল কাসেম",
                providerMobile = "01715-556677",
                receiverStaffName = "মাওলানা মুফতি মিজানুর রহমান (পেশ ইমাম ও খতিব)",
                status = "approved"
            ),
            FoodScheduleEntity(
                id = "food-002",
                dateStr = todayIso,
                mealType = "দৈনিক খাবার",
                providerName = "মো: আসাদুজ্জামান",
                providerMobile = "01611-778899",
                receiverStaffName = "কারী মো: ইসমাইল হোসেন (মুয়াজ্জিন ও সহকারী কারী)",
                status = "approved"
            ),
            FoodScheduleEntity(
                id = "food-003",
                dateStr = todayIso,
                mealType = "দৈনিক খাবার",
                providerName = "ডা: তারিকুল ইসলাম",
                providerMobile = "01552-889900",
                receiverStaffName = "মো: মোস্তফা মিয়া (প্রধান খাদেম ও তত্ত্বাবধায়ক)",
                status = "scheduled"
            ),
            FoodScheduleEntity(
                id = "food-004",
                dateStr = tomorrowIso,
                mealType = "দৈনিক খাবার",
                providerName = "আলহাজ্ব আব্দুল বারী",
                providerMobile = "01819-334455",
                receiverStaffName = "মাওলানা মুফতি মিজানুর রহমান (পেশ ইমাম ও খতিব)",
                status = "scheduled"
            ),
            FoodScheduleEntity(
                id = "food-005",
                dateStr = tomorrowIso,
                mealType = "দৈনিক খাবার",
                providerName = "মো: শাহজাহান আলী",
                providerMobile = "01723-001122",
                receiverStaffName = "কারী মো: ইসমাইল হোসেন (মুয়াজ্জিন ও সহকারী কারী)",
                status = "scheduled"
            ),
            FoodScheduleEntity(
                id = "food-006",
                dateStr = tomorrowIso,
                mealType = "দৈনিক খাবার",
                providerName = "আলহাজ্ব শামসুল হক চৌধুরী",
                providerMobile = "01711-009988",
                receiverStaffName = "মো: মোস্তফা মিয়া (প্রধান খাদেম ও তত্ত্বাবধায়ক)",
                status = "scheduled"
            )
        )
        foods.forEach { foodDao.insertSchedule(it) }

        // 13. Welfare Cases (যাকাত ও সমাজকল্যাণ)
        val welfareDao = database.welfareDao()
        val welfareCases = listOf(
            WelfareCaseEntity(
                id = "welfare-001",
                beneficiaryName = "মোসাম্মৎ রহিমা খাতুন",
                category = "বিধবা ও এতিম সহায়তা",
                mobile = "01923-456789",
                amountRequested = 10000.0,
                amountApproved = 10000.0,
                status = "disbursed",
                applicationDate = "২০২৪-০৯-০৪",
                remarks = "মাসিক পারিবারিক খরচ ও শিশুদের পড়াশোনা"
            ),
            WelfareCaseEntity(
                id = "welfare-002",
                beneficiaryName = "মোঃ আক্তার হোসেন",
                category = "জরুরি চিকিৎসা সহায়তা",
                mobile = "01819-334455",
                amountRequested = 15000.0,
                amountApproved = 15000.0,
                status = "approved",
                applicationDate = "২০২৪-০৯-০৮",
                remarks = "হৃদরোগের অপারেশনের জন্য প্রেসক্রিপশন যাচাইকৃত"
            )
        )
        welfareCases.forEach { welfareDao.insertCase(it) }

        // 14. Mosque Events (মাহফিল ও বিশেষ অনুষ্ঠান)
        val eventDao = database.eventDao()
        val events = listOf(
            MosqueEventEntity(
                id = "event-001",
                title = "বাৎসরিক তাফসিরুল কুরআন ও ওয়াজ মাহফিল",
                dateStr = "২০২৪-১০-১৫",
                timeStr = "বাদ আসর হতে রাত ১১টা",
                speakerOrGuest = "মুফতি ড. মিজানুর রহমান আল-আজহারী",
                venue = "মসজিদ প্রাঙ্গণ ও সংলগ্ন ময়দান",
                budget = 120000.0,
                volunteerLead = "ইঞ্জি: আব্দুল্লাহ আল মামুন",
                status = "upcoming"
            ),
            MosqueEventEntity(
                id = "event-002",
                title = "পবিত্র শবে কদর ও খতমে কুরআন আলোচনা",
                dateStr = "২০২৪-০৪-০৫",
                timeStr = "রাত ১০টা হতে ফজর পর্যন্ত",
                speakerOrGuest = "মাওলানা মুফতি মিজানুর রহমান (পেশ ইমাম)",
                venue = "মসজিদ মূল ভবন (১ম ও ২য় তলা)",
                budget = 35000.0,
                volunteerLead = "মো: জাহিদুল ইসলাম",
                status = "completed"
            )
        )
        events.forEach { eventDao.insertEvent(it) }

        // 15. Cashbox Witness & Bank Deposit Records
        val cashboxDao = database.cashboxDao()
        val cashboxes = listOf(
            CashboxRecordEntity(
                id = "cb-001",
                collectionDate = "2024-09-06",
                boxType = "সাপ্তাহিক জুমার দান বাক্স",
                countedAmount = 22500.0,
                countedBy = "হাজী মো: আবুল কাসেম (কোষাধ্যক্ষ) ও মো: নজরুল ইসলাম",
                witnessNames = "ডা: তারিকুল ইসলাম, মো: আসাদুজ্জামান",
                witnessVerified = true,
                verifiedBy = "ডা: তারিকুল ইসলাম (কার্যনির্বাহী সদস্য)",
                bankName = "ইসলামী ব্যাংক বাংলাদেশ পিএলসি (মিরপুর শাখা)",
                depositSlipNo = "IBBL-SLIP-98412",
                depositStatus = "bank_deposited",
                notes = "জুমার নামাজ শেষে মূল ফটকের ৩টি দান বাক্স খুলে উপস্থিত মুসল্লি ও সাক্ষীদের সামনে গণনা সম্পন্ন।"
            ),
            CashboxRecordEntity(
                id = "cb-002",
                collectionDate = "2024-09-13",
                boxType = "মাসিক সিন্দুক ও দান বাক্স",
                countedAmount = 18400.0,
                countedBy = "মো: নজরুল ইসলাম (সহ-অর্থ সম্পাদক)",
                witnessNames = "ডা: তারিকুল ইসলাম",
                witnessVerified = false,
                verifiedBy = null,
                bankName = "",
                depositSlipNo = "",
                depositStatus = "counted",
                notes = "মসজিদ অফিসে গণনাকৃত; কার্যনির্বাহী সদস্যের ডিজিটাল সাক্ষ্য ও ব্যাংক জমা প্রক্রিয়াধীন।"
            )
        )
        cashboxes.forEach { cashboxDao.insertRecord(it) }

        // 16. Office Documents & Correspondence Register (দপ্তর সম্পাদক)
        val officeDocDao = database.officeDocumentDao()
        val officeDocs = listOf(
            OfficeDocumentEntity(
                id = "doc-001",
                docCode = "DOC-WAQF-001",
                title = "মসজিদ মূল জমি ও ওয়াকফ এস্টেট দলিল",
                category = "দলিল",
                counterparty = "সাব-রেজিস্ট্রি অফিস, মিরপুর",
                docDate = "1998-05-14",
                summary = "১০ কাঠা জমির মূল ওয়াকফনামা ও সরকারি খতিয়ান সত্যায়িত কপি ভল্টে সংরক্ষিত।",
                status = "সংরক্ষিত",
                recordedBy = "মো: তানভীর আহমেদ (দপ্তর সম্পাদক)"
            ),
            OfficeDocumentEntity(
                id = "doc-002",
                docCode = "DOC-LTR-014",
                title = "সিটি কর্পোরেশন হতে অজুখানা ড্রেনেজ সংস্কার সংক্রান্ত চিঠি",
                category = "আগত চিঠি",
                counterparty = "ঢাকা উত্তর সিটি কর্পোরেশন (অঞ্চল-২)",
                docDate = "2024-09-05",
                summary = "মসজিদের সম্মুখভাগের ড্রেন ও পানি নিষ্কাশন সংযোগ উন্নয়ন বিষয়ে দাপ্তরিক পত্র।",
                replyNote = "",
                status = "জবাব অপেক্ষমান",
                recordedBy = "মো: তানভীর আহমেদ (দপ্তর সম্পাদক)"
            ),
            OfficeDocumentEntity(
                id = "doc-003",
                docCode = "DOC-CNT-009",
                title = "নতুন সোলার ও জেনারেটর রক্ষণাবেক্ষণ বার্ষিক চুক্তি",
                category = "চুক্তি",
                counterparty = "গ্রিন পাওয়ার টেকনোলজিস লি.",
                docDate = "2024-08-20",
                summary = "১ বছরের ফ্রি সার্ভিসিং ও জরুরি কারিগরি সহায়তা চুক্তিপত্র।",
                status = "সংরক্ষিত",
                recordedBy = "ইঞ্জি: আব্দুল্লাহ আল মামুন (সাধারণ সম্পাদক)"
            )
        )
        officeDocs.forEach { officeDocDao.insertDocument(it) }

        // 17. Sharia Review & Advisory Board Guidance
        val advisoryDao = database.advisoryNoteDao()
        val advisories = listOf(
            AdvisoryNoteEntity(
                id = "adv-001",
                noteType = "SHARIA_REVIEW",
                topic = "যাকাত ও ফিতরা তহবিল পৃথক খাতে সংরক্ষণ ও বণ্টন নীতিমালা",
                recommendation = "সাধারণ দান বাক্সের অর্থ ও যাকাতের অর্থ সম্পূর্ণ আলাদা খাতে রাখতে হবে। যাকাতের অর্থ কেবল কুরআনে বর্ণিত ৮টি নির্ধারিত খাতে (মাসারিফে যাকাত) দরিদ্র ও অসহায়দের মালিকানায় হস্তান্তরযোগ্য—মসজিদ নির্মাণ বা সাধারণ বিলে ব্যয় করা যাবে না।",
                authorName = "মাওলানা মুফতি মিজানুর রহমান",
                authorRoleTitle = "পেশ ইমাম ও খতিব (পদাধিকারবলে সদস্য)",
                shariaCompliance = "শরিয়াহ সম্মত",
                status = "গৃহীত",
                createdDate = "2024-09-04"
            ),
            AdvisoryNoteEntity(
                id = "adv-002",
                noteType = "ADVISORY_GUIDANCE",
                topic = "৩য় ও ৪র্থ তলা সম্প্রসারণ নির্মাণকাজের ধাপ ও আর্থিক নিরাপত্তা",
                recommendation = "বহুতল সম্প্রসারণ কাজের পূর্বে কাঠামোগত প্রকৌশল সনদ সংগ্রহ এবং ধাপে ধাপে আলাদা 'নির্মাণ তহবিল' গঠন করে ঠিকাদারের সাথে লিখিত শর্তে কাজ শুরুর পরামর্শ প্রদান করা হলো।",
                authorName = "আলহাজ্ব শামসুল হক চৌধুরী",
                authorRoleTitle = "উপদেষ্টা পরিষদ",
                shariaCompliance = "কৌশলগত পরামর্শ",
                status = "পর্যালোচনাধীন",
                createdDate = "2024-09-08"
            )
        )
        advisories.forEach { advisoryDao.insertNote(it) }

        // 18. Smart Board Wall Quotes & Publicity Announcements (প্রচার ও প্রকাশনা)
        val smartQuoteDao = database.smartBoardQuoteDao()
        val quotes = listOf(
            SmartBoardQuoteEntity(
                id = "sbq-001",
                category = "দেয়াল উক্তি",
                title = "মসজিদ আবাদকারীর মর্যাদা",
                message = "যে ব্যক্তি আল্লাহর সন্তুষ্টির উদ্দেশ্যে মসজিদ নির্মাণ ও আবাদ করে, আল্লাহ তাআলা তার জন্য জান্নাতে গৃহ নির্মাণ করেন। (সহিহ বুখারি)",
                isPinned = true,
                createdBy = "হাফেজ মাওলানা সাইফুল ইসলাম (প্রচার সম্পাদক)"
            ),
            SmartBoardQuoteEntity(
                id = "sbq-002",
                category = "বিশেষ ধর্মীয় দিবস",
                title = "পবিত্র ঈদে মিলাদুন্নবী (সা:) ও তাফসির মাহফিল প্রচার",
                message = "আগামী ১২ রবিউল আউয়াল বাদ মাগরিব সীরাতুন্নবী (সা:) ও দোয়া মাহফিল অনুষ্ঠিত হবে।",
                isPinned = true,
                createdBy = "হাফেজ মাওলানা সাইফুল ইসলাম (প্রচার সম্পাদক)"
            ),
            SmartBoardQuoteEntity(
                id = "sbq-003",
                category = "জুমার নির্দেশনা",
                title = "জুমার দিনে কাতার সোজা ও মোবাইল সাইলেন্ট রাখার অনুরোধ",
                message = "মসজিদে প্রবেশের পূর্বে অনুগ্রহ করে মোবাইল ফোন বন্ধ বা সাইলেন্ট রাখুন এবং সামনের কাতার পূর্ণ করুন।",
                isPinned = false,
                createdBy = "হাফেজ মাওলানা সাইফুল ইসলাম (প্রচার সম্পাদক)"
            )
        )
        quotes.forEach { smartQuoteDao.insertQuote(it) }

        // 19. Committee Election & Reconstruction (পরিচালনা পরিষদ নির্বাচন ও পুনর্গঠন)
        val electionDao = database.electionDao()
        val sampleElection = ElectionScheduleEntity(
            id = "elec-2026",
            termTitle = "২০২৬–২০২৮ মেয়াদের পরিচালনা পরিষদ নির্বাচন",
            termStartYear = 2026,
            termEndYear = 2028,
            chiefElectionCommissioner = "আলহাজ্ব ড. মো: রফিকুল ইসলাম",
            commissionerMobile = "01711-889900",
            scheduleDate = "২০২৫-১২-০১",
            nominationDeadline = "২০২৫-১২-১৫",
            votingDate = "২০২৫-১২-২৮",
            status = "nomination_open",
            electionType = "সর্বসম্মত ও ব্যালট নির্বাচন",
            gazetteNotes = "মসজিদের গঠনতন্ত্র অনুযায়ী গণতান্ত্রিক ও শরিয়াহ নির্দেশিত পদ্ধতিতে নির্বাচন ও পুনর্গঠন কার্যক্রম পরিচালিত হচ্ছে।"
        )
        electionDao.insertElection(sampleElection)

        val sampleCandidates = listOf(
            ElectionCandidateEntity(
                id = "cand-001",
                electionId = "elec-2026",
                targetRoleCode = "president",
                targetRoleTitleBn = "সভাপতি",
                candidateNameBn = "মো: রফিকুল ইসলাম",
                candidateMobile = "01711-223344",
                memberCode = "MEM-101",
                proposerNameBn = "আলহাজ্ব আব্দুল বারী",
                seconderNameBn = "ইঞ্জি: আব্দুল্লাহ আল মামুন",
                status = "valid",
                symbolOrPanel = "মসজিদ ঐক্য পরিষদ",
                votesReceived = 142,
                isElected = true
            ),
            ElectionCandidateEntity(
                id = "cand-002",
                electionId = "elec-2026",
                targetRoleCode = "general_secretary",
                targetRoleTitleBn = "সাধারণ সম্পাদক",
                candidateNameBn = "ইঞ্জি: আব্দুল্লাহ আল মামুন",
                candidateMobile = "01912-445566",
                memberCode = "MEM-103",
                proposerNameBn = "মো: রফিকুল ইসলাম",
                seconderNameBn = "হাজী মো: আবুল কাসেম",
                status = "valid",
                symbolOrPanel = "মসজিদ ঐক্য পরিষদ",
                votesReceived = 138,
                isElected = true
            ),
            ElectionCandidateEntity(
                id = "cand-003",
                electionId = "elec-2026",
                targetRoleCode = "treasurer",
                targetRoleTitleBn = "কোষাধ্যক্ষ",
                candidateNameBn = "হাজী মো: আবুল কাসেম",
                candidateMobile = "01814-556677",
                memberCode = "MEM-105",
                proposerNameBn = "ইঞ্জি: আব্দুল্লাহ আল মামুন",
                seconderNameBn = "মো: আসাদুজ্জামান",
                status = "valid",
                symbolOrPanel = "স্বচ্ছ তহবিল প্যানেল",
                votesReceived = 135,
                isElected = true
            ),
            ElectionCandidateEntity(
                id = "cand-004",
                electionId = "elec-2026",
                targetRoleCode = "publicity",
                targetRoleTitleBn = "প্রচার ও প্রকাশনা সম্পাদক",
                candidateNameBn = "হাফেজ মাওলানা সাইফুল ইসলাম",
                candidateMobile = "01715-667788",
                memberCode = "MEM-108",
                proposerNameBn = "মাওলানা আব্দুর রাজ্জাক",
                seconderNameBn = "মো: তানভীর আহমেদ",
                status = "valid",
                symbolOrPanel = "দ্বীনি খেদমত প্যানেল",
                votesReceived = 129,
                isElected = true
            )
        )
        sampleCandidates.forEach { electionDao.insertCandidate(it) }

        // 22. Mosque Properties & Waqf Assets
        val propertyDao = database.mosquePropertyDao()
        val sampleProperties = listOf(
            MosquePropertyEntity(
                id = "prop-001",
                title = "মসজিদের মূল জমি ও কমপ্লেক্স",
                category = "জমি / প্লট",
                areaOrQuantity = "১২.৫ শতক",
                locationOrDagNo = "সিএস/এসএ দাগ ১২৪, আরএস দাগ ৪১০, মিরপুর মৌজা",
                deedOrRegNo = "ওয়াকফ দলিল নং-৪৫২১/১৯৯৮",
                estimatedValueOrIncome = "আনুমানিক বাজারমূল্য: ১ কোটি ২০ লাখ টাকা",
                status = "সংরক্ষিত",
                description = "মসজিদের প্রধান ভবন, অজুখানা, মিনার এবং অভ্যন্তরীণ প্রাঙ্গণ সংলগ্ন মূল ওয়াকফকৃত জমি।",
                recordedDate = "১৯৯৮-০৩-১৫"
            ),
            MosquePropertyEntity(
                id = "prop-002",
                title = "মসজিদ ওয়াকফ মার্কেট ও দোকানসমূহ",
                category = "বাণিজ্যিক দোকান / মার্কেট",
                areaOrQuantity = "৪ টি পাকা দোকান",
                locationOrDagNo = "মসজিদ কমপ্লেক্সের দক্ষিণ-পূর্ব সীমানা সংলগ্ন",
                deedOrRegNo = "চুক্তিপত্র নং-০৮/২০২২",
                estimatedValueOrIncome = "মাসিক ভাড়া আয়: ৳ ৪০,০০০ (বাৎসরিক ৳ ৪,৮০,০০০)",
                status = "ভাড়া/ইজারাকৃত",
                description = "মসজিদের পরিচালনা তহবিল ও নিয়মিত ব্যয়ের জন্য ৪টি বাণিজ্যিক দোকান ইজারা প্রদানকৃত। নিয়মিত ভাড়া মসজিদ অ্যাকাউন্টে জমা হয়।",
                recordedDate = "২০২২-০২-০১"
            ),
            MosquePropertyEntity(
                id = "prop-003",
                title = "হিফজখানা ও দ্বিমুখী সম্প্রসারিত ভবন",
                category = "ভবন ও অবকাঠামো",
                areaOrQuantity = "৩ তলা বিশিষ্ট পাকা ভবন",
                locationOrDagNo = "মসজিদ উত্তর ব্লক সংলগ্ন",
                deedOrRegNo = "বিল্ডিং প্ল্যান নং-ডিএমপি-৭৮",
                estimatedValueOrIncome = "নির্মাণ ব্যয়: ৳ ৬৫ লাখ টাকা",
                status = "সক্রিয়",
                description = "১ম তলায় অজুখানা ও স্টাফ কক্ষ, ২য় ও ৩য় তলায় নূরানী ও হিফজ মাদরাসার শিক্ষা কক্ষ ও আবাসিক ব্যবস্থা।",
                recordedDate = "২০২০-১১-১০"
            ),
            MosquePropertyEntity(
                id = "prop-004",
                title = "স্ট্যান্ডবাই বিদ্যুৎ ব্যাকআপ জেনারেটর ও সোলার সিস্টেম",
                category = "যন্ত্রপাতি ও মূল সম্পদ",
                areaOrQuantity = "১ টি ৪০ কেভিএ ডিজেল জেনারেটর + ৫ কিলোওয়াট সোলার",
                locationOrDagNo = "মসজিদ পাওয়ার হাউস ও ছাদ",
                deedOrRegNo = "ক্রয় রসিদ নং-জেন-২০২৩/১৪",
                estimatedValueOrIncome = "সম্পদ মূল্য: ৳ ৮ লাখ ৫০ হাজার টাকা",
                status = "সক্রিয়",
                description = "লোডশেডিং ও বিদ্যুৎ বিভ্রাটে পুরো মসজিদের লাইট, ফ্যান, সাউন্ড সিস্টেম ও পানির পাম্প সচল রাখার স্বয়ংক্রিয় ব্যাকআপ।",
                recordedDate = "২০২৩-০৬-২০"
            )
        )
        sampleProperties.forEach { propertyDao.insertProperty(it) }
    }
}

