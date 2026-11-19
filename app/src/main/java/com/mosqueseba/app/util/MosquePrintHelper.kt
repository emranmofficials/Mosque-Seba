package com.mosqueseba.app.util

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Base64
import android.webkit.WebView
import android.webkit.WebViewClient
import com.mosqueseba.app.data.local.entity.ChandaPaymentEntity
import com.mosqueseba.app.data.local.entity.NoticeEntity
import com.mosqueseba.app.ui.components.toBengaliDigits
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * MosquePrintHelper - Native Android printing & PDF generation utility
 * for receipts, notices, vouchers, and monthly financial statements.
 */
object MosquePrintHelper {

    fun fileToBase64DataUri(path: String?): String? {
        if (path.isNullOrBlank()) return null
        return try {
            val cleanPath = path.removePrefix("file://")
            val file = File(cleanPath)
            if (!file.exists()) return null
            val bytes = file.readBytes()
            val mime = if (cleanPath.endsWith(".png", true)) "image/png" else "image/jpeg"
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            "data:$mime;base64,$base64"
        } catch (_: Exception) {
            null
        }
    }

    fun printHtml(context: Context, jobName: String, htmlContent: String) {
        val webView = WebView(context).apply {
            settings.javaScriptEnabled = false
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                    val printAdapter = createPrintDocumentAdapter(jobName)
                    val printAttributes = PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setResolution(PrintAttributes.Resolution("res_id", "high_res", 300, 300))
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()
                    printManager?.print(jobName, printAdapter, printAttributes)
                }
            }
            loadDataWithBaseURL("file:///android_asset/", htmlContent, "text/html", "UTF-8", null)
        }
    }

    fun printReceipt(
        context: Context,
        mosqueName: String,
        payment: ChandaPaymentEntity,
        methodLabel: String,
        treasurerSignatureUrl: String? = null,
        treasurerStampUrl: String? = null,
        collectorSignatureUrl: String? = null,
        mosqueSealUrl: String? = null
    ) {
        val html = buildReceiptHtml(
            mosqueName = mosqueName,
            payment = payment,
            methodLabel = methodLabel,
            treasurerSignatureUrl = treasurerSignatureUrl,
            treasurerStampUrl = treasurerStampUrl,
            collectorSignatureUrl = collectorSignatureUrl,
            mosqueSealUrl = mosqueSealUrl
        )
        printHtml(context, "Receipt-${payment.receiptCode}", html)
    }

    fun printNotice(
        context: Context,
        mosqueName: String,
        notice: NoticeEntity,
        presidentSignatureUrl: String? = null,
        presidentStampUrl: String? = null,
        mosqueSealUrl: String? = null
    ) {
        val html = buildNoticeHtml(
            mosqueName = mosqueName,
            notice = notice,
            presidentSignatureUrl = presidentSignatureUrl,
            presidentStampUrl = presidentStampUrl,
            mosqueSealUrl = mosqueSealUrl
        )
        printHtml(context, "Notice-${notice.id.take(8)}", html)
    }

    fun printFinanceSummary(
        context: Context,
        mosqueName: String,
        totalIncome: Double,
        totalExpense: Double,
        balance: Double,
        chanda: Double,
        cashFund: Double,
        bankFund: Double,
        jummahTotal: Double,
        jummahCount: Int,
        approvedCount: Int,
        pendingCount: Int,
        aiSummary: String,
        presidentSignatureUrl: String? = null,
        presidentStampUrl: String? = null,
        secretarySignatureUrl: String? = null,
        secretaryStampUrl: String? = null,
        treasurerSignatureUrl: String? = null,
        treasurerStampUrl: String? = null,
        mosqueSealUrl: String? = null
    ) {
        val html = buildFinanceHtml(
            mosqueName = mosqueName,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = balance,
            chanda = chanda,
            cashFund = cashFund,
            bankFund = bankFund,
            jummahTotal = jummahTotal,
            jummahCount = jummahCount,
            approvedCount = approvedCount,
            pendingCount = pendingCount,
            aiSummary = aiSummary,
            presidentSignatureUrl = presidentSignatureUrl,
            presidentStampUrl = presidentStampUrl,
            secretarySignatureUrl = secretarySignatureUrl,
            secretaryStampUrl = secretaryStampUrl,
            treasurerSignatureUrl = treasurerSignatureUrl,
            treasurerStampUrl = treasurerStampUrl,
            mosqueSealUrl = mosqueSealUrl
        )
        printHtml(context, "Finance-Report-${System.currentTimeMillis()}", html)
    }

    fun printCashboxVoucher(
        context: Context,
        mosqueName: String,
        dateStr: String,
        totalAmount: Double,
        breakdownList: List<Pair<String, Int>>,
        witness1: String,
        witness2: String,
        witness3: String,
        notes: String,
        imamSignatureUrl: String? = null,
        treasurerSignatureUrl: String? = null,
        treasurerStampUrl: String? = null,
        witnessSignatureUrl: String? = null,
        mosqueSealUrl: String? = null
    ) {
        val html = buildCashboxVoucherHtml(
            mosqueName = mosqueName,
            dateStr = dateStr,
            totalAmount = totalAmount,
            breakdownList = breakdownList,
            witness1 = witness1,
            witness2 = witness2,
            witness3 = witness3,
            notes = notes,
            imamSignatureUrl = imamSignatureUrl,
            treasurerSignatureUrl = treasurerSignatureUrl,
            treasurerStampUrl = treasurerStampUrl,
            witnessSignatureUrl = witnessSignatureUrl,
            mosqueSealUrl = mosqueSealUrl
        )
        printHtml(context, "Cashbox-Voucher-${System.currentTimeMillis()}", html)
    }

    fun printInvitationPoster(
        context: Context,
        mosqueName: String,
        mosqueAddress: String,
        title: String,
        eventType: String,
        chiefGuest: String,
        specialSpeaker: String,
        honorableGuests: String,
        president: String,
        dateStr: String,
        timeStr: String,
        venue: String,
        description: String,
        organizer: String,
        contact: String,
        presidentSignatureUrl: String? = null,
        presidentStampUrl: String? = null,
        mosqueSealUrl: String? = null
    ) {
        val html = buildInvitationPosterHtml(
            mosqueName = mosqueName,
            mosqueAddress = mosqueAddress,
            title = title,
            eventType = eventType,
            chiefGuest = chiefGuest,
            specialSpeaker = specialSpeaker,
            honorableGuests = honorableGuests,
            president = president,
            dateStr = dateStr,
            timeStr = timeStr,
            venue = venue,
            description = description,
            organizer = organizer,
            contact = contact,
            presidentSignatureUrl = presidentSignatureUrl,
            presidentStampUrl = presidentStampUrl,
            mosqueSealUrl = mosqueSealUrl
        )
        printHtml(context, "Invitation-${System.currentTimeMillis()}", html)
    }

    private fun buildReceiptHtml(
        mosqueName: String,
        payment: ChandaPaymentEntity,
        methodLabel: String,
        treasurerSignatureUrl: String? = null,
        treasurerStampUrl: String? = null,
        collectorSignatureUrl: String? = null,
        mosqueSealUrl: String? = null
    ): String {
        val amountBn = toBengaliDigits(String.format(Locale.US, "%,.0f", payment.amountPaid))
        val receiptCodeBn = toBengaliDigits(payment.receiptCode)
        val monthBn = toBengaliDigits(payment.month)
        val dateBn = toBengaliDigits(payment.paidAt)

        val sealDataUri = fileToBase64DataUri(mosqueSealUrl)
        val treasurerSigUri = fileToBase64DataUri(treasurerSignatureUrl)
        val treasurerStampUri = fileToBase64DataUri(treasurerStampUrl)
        val collectorSigUri = fileToBase64DataUri(collectorSignatureUrl)

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>চাঁদা রসিদ</title>
            <style>
                @page { size: A4; margin: 20mm; }
                body {
                    font-family: 'SolaimanLipi', 'Noto Sans Bengali', system-ui, sans-serif;
                    color: #1a1a1a;
                    margin: 0;
                    padding: 20px;
                    background: #fff;
                }
                .receipt-container {
                    border: 3px double #064E3B;
                    border-radius: 12px;
                    padding: 24px 30px;
                    max-width: 650px;
                    margin: 0 auto;
                    position: relative;
                }
                .bismillah {
                    text-align: center;
                    font-size: 18px;
                    color: #064E3B;
                    margin-bottom: 6px;
                }
                .header {
                    text-align: center;
                    border-bottom: 2px solid #D4AF37;
                    padding-bottom: 12px;
                    margin-bottom: 14px;
                }
                .seal-header-img {
                    width: 65px;
                    height: 65px;
                    object-fit: contain;
                    border-radius: 50%;
                    margin-bottom: 6px;
                    display: block;
                    margin-left: auto;
                    margin-right: auto;
                }
                .mosque-title {
                    font-size: 22px;
                    font-weight: bold;
                    color: #064E3B;
                    margin: 0;
                }
                .sub-title {
                    font-size: 14px;
                    color: #555;
                    margin-top: 4px;
                }
                .badge-receipt {
                    display: inline-block;
                    background: #064E3B;
                    color: #fff;
                    padding: 4px 16px;
                    border-radius: 20px;
                    font-size: 13px;
                    font-weight: bold;
                    margin-top: 8px;
                }
                .details-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin: 16px 0;
                }
                .details-table td {
                    padding: 10px 12px;
                    border-bottom: 1px dashed #ddd;
                    font-size: 14px;
                }
                .details-table td.label {
                    color: #555;
                    width: 40%;
                    font-weight: 600;
                }
                .details-table td.val {
                    color: #111;
                    font-weight: bold;
                }
                .amount-box {
                    background: #f4fbf7;
                    border: 1.5px solid #064E3B;
                    border-radius: 8px;
                    padding: 12px 18px;
                    margin: 16px 0;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }
                .amount-label {
                    font-size: 15px;
                    font-weight: bold;
                    color: #064E3B;
                }
                .amount-val {
                    font-size: 22px;
                    font-weight: 900;
                    color: #064E3B;
                }
                .signatures {
                    margin-top: 36px;
                    display: flex;
                    justify-content: space-between;
                }
                .sig-box {
                    text-align: center;
                    border-top: 1px solid #777;
                    padding-top: 6px;
                    font-size: 13px;
                    width: 170px;
                    font-weight: bold;
                    color: #333;
                }
                .sig-img {
                    max-height: 44px;
                    max-width: 130px;
                    object-fit: contain;
                    display: block;
                    margin: 0 auto 4px auto;
                }
                .stamp-img {
                    max-height: 50px;
                    max-width: 100px;
                    object-fit: contain;
                    display: block;
                    margin: 4px auto 0 auto;
                    opacity: 0.92;
                }
                .qr-section {
                    margin-top: 20px;
                    padding-top: 14px;
                    border-top: 1px dashed #ccc;
                    display: flex;
                    justify-content: space-between;
                    align-items: flex-end;
                }
                .qr-info {
                    font-size: 11px;
                    color: #666;
                    line-height: 1.5;
                }
                .footer {
                    text-align: center;
                    font-size: 11px;
                    color: #888;
                    margin-top: 24px;
                }
            </style>
        </head>
        <body>
            <div class="receipt-container">
                <div class="bismillah">بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ</div>
                <div class="header">
                    ${if (sealDataUri != null) """<img src="$sealDataUri" class="seal-header-img" />""" else ""}
                    <h1 class="mosque-title">$mosqueName</h1>
                    <div class="sub-title">অফিসিয়াল চাঁদা ও অনুদান আদায় রসিদ</div>
                    <div class="badge-receipt">অনলাইন ভেরিফায়েড রসিদ</div>
                </div>

                <table class="details-table">
                    <tr>
                        <td class="label">রসিদ নম্বর</td>
                        <td class="val">$receiptCodeBn</td>
                    </tr>
                    <tr>
                        <td class="label">সদস্যের নাম</td>
                        <td class="val">${payment.memberNameBn}</td>
                    </tr>
                    <tr>
                        <td class="label">মাসের বিবরণ</td>
                        <td class="val">$monthBn</td>
                    </tr>
                    <tr>
                        <td class="label">পরিশোধের মাধ্যম</td>
                        <td class="val">$methodLabel</td>
                    </tr>
                    <tr>
                        <td class="label">তারিখ ও সময়</td>
                        <td class="val">$dateBn</td>
                    </tr>
                    <tr>
                        <td class="label">আদায়কারীর নাম</td>
                        <td class="val">${payment.receivedBy}</td>
                    </tr>
                </table>

                <div class="amount-box">
                    <span class="amount-label">আদায়কৃত মোট টাকার পরিমাণ:</span>
                    <span class="amount-val">৳ $amountBn</span>
                </div>

                <div class="signatures">
                    <div class="sig-box">
                        ${if (collectorSigUri != null) """<img src="$collectorSigUri" class="sig-img" />""" else """<div style="height: 38px;"></div>"""}
                        <div>সদস্যের স্বাক্ষর</div>
                    </div>
                    <div class="sig-box">
                        ${if (treasurerSigUri != null) """<img src="$treasurerSigUri" class="sig-img" />""" else """<div style="height: 38px;"></div>"""}
                        <div>কোষাধ্যক্ষ / আদায়কারী</div>
                        ${if (treasurerStampUri != null) """<img src="$treasurerStampUri" class="stamp-img" />""" else ""}
                    </div>
                </div>

                <div class="qr-section">
                    <div class="qr-info">
                        <strong>ভেরিফিকেশন কিউআর কোড:</strong> ${payment.qrVerificationCode}<br>
                        <em>"মসজিদ সেবা" অটোমেটেড ফাইন্যান্সিয়াল ম্যানেজমেন্ট সিস্টেম দ্বারা প্রস্তুতকৃত।</em>
                    </div>
                </div>

                <div class="footer">
                    جَزَاكَ ٱللَّهُ خَيْرًا — আল্লাহ আপনার দানকে কবুল করুন ও উত্তম প্রতিদান দিন।
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    private fun buildNoticeHtml(
        mosqueName: String,
        notice: NoticeEntity,
        presidentSignatureUrl: String? = null,
        presidentStampUrl: String? = null,
        mosqueSealUrl: String? = null
    ): String {
        val dateBn = toBengaliDigits(notice.publishDate)
        val priorityBn = when (notice.priority) {
            "urgent" -> "জরুরি ঘোষণা"
            "important" -> "গুরুত্বপূর্ণ নোটিশ"
            else -> "সাধারণ নোটিশ"
        }
        val priorityColor = when (notice.priority) {
            "urgent" -> "#DC2626"
            "important" -> "#D97706"
            else -> "#064E3B"
        }

        val sealDataUri = fileToBase64DataUri(mosqueSealUrl)
        val presidentSigUri = fileToBase64DataUri(presidentSignatureUrl)
        val presidentStampUri = fileToBase64DataUri(presidentStampUrl)

        val imageTag = if (!notice.imageUrl.isNullOrBlank()) {
            """<div style="text-align:center; margin: 18px 0;"><img src="${notice.imageUrl}" style="max-width: 90%; max-height: 380px; border-radius: 8px; border: 1.5px solid #D4AF37; box-shadow: 0 4px 10px rgba(0,0,0,0.1);" /></div>"""
        } else ""

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>${notice.title}</title>
            <style>
                @page { size: A4; margin: 20mm; }
                body {
                    font-family: 'SolaimanLipi', 'Noto Sans Bengali', system-ui, sans-serif;
                    color: #1a1a1a;
                    margin: 0;
                    padding: 20px;
                    background: #fff;
                    line-height: 1.6;
                }
                .notice-container {
                    border: 3px double #064E3B;
                    border-radius: 12px;
                    padding: 30px 36px;
                    max-width: 700px;
                    margin: 0 auto;
                }
                .bismillah {
                    text-align: center;
                    font-size: 20px;
                    color: #064E3B;
                    margin-bottom: 8px;
                }
                .header {
                    text-align: center;
                    border-bottom: 2px solid #D4AF37;
                    padding-bottom: 14px;
                    margin-bottom: 20px;
                }
                .seal-header-img {
                    width: 70px;
                    height: 70px;
                    object-fit: contain;
                    border-radius: 50%;
                    margin-bottom: 8px;
                    display: block;
                    margin-left: auto;
                    margin-right: auto;
                }
                .mosque-title {
                    font-size: 26px;
                    font-weight: bold;
                    color: #064E3B;
                    margin: 0;
                }
                .sub-title {
                    font-size: 14px;
                    color: #555;
                    margin-top: 4px;
                }
                .badge-priority {
                    display: inline-block;
                    background: $priorityColor;
                    color: #fff;
                    padding: 4px 18px;
                    border-radius: 20px;
                    font-size: 13px;
                    font-weight: bold;
                    margin-top: 10px;
                }
                .notice-title {
                    text-align: center;
                    font-size: 20px;
                    font-weight: 800;
                    color: #111;
                    margin: 20px 0 16px 0;
                    padding: 10px;
                    background: #fbfbf5;
                    border-left: 4px solid #D4AF37;
                    border-radius: 4px;
                }
                .notice-body {
                    font-size: 15px;
                    white-space: pre-wrap;
                    color: #222;
                    text-align: justify;
                    margin: 20px 0 30px 0;
                }
                .date-info {
                    font-size: 13px;
                    color: #555;
                    margin-bottom: 25px;
                }
                .signature-section {
                    margin-top: 40px;
                    display: flex;
                    justify-content: flex-end;
                }
                .sig-box {
                    text-align: center;
                    border-top: 1px solid #333;
                    padding-top: 6px;
                    font-size: 14px;
                    font-weight: bold;
                    width: 220px;
                    color: #064E3B;
                }
                .sig-img {
                    max-height: 48px;
                    max-width: 140px;
                    object-fit: contain;
                    display: block;
                    margin: 0 auto 4px auto;
                }
                .stamp-img {
                    max-height: 52px;
                    max-width: 110px;
                    object-fit: contain;
                    display: block;
                    margin: 4px auto 0 auto;
                    opacity: 0.92;
                }
                .footer {
                    text-align: center;
                    font-size: 11px;
                    color: #999;
                    margin-top: 36px;
                    border-top: 1px dashed #ddd;
                    padding-top: 10px;
                }
            </style>
        </head>
        <body>
            <div class="notice-container">
                <div class="bismillah">بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ</div>
                <div class="header">
                    ${if (sealDataUri != null) """<img src="$sealDataUri" class="seal-header-img" />""" else ""}
                    <h1 class="mosque-title">$mosqueName</h1>
                    <div class="sub-title">অফিসিয়াল ডিজিটাল নোটিশ বোর্ড</div>
                    <div class="badge-priority">$priorityBn</div>
                </div>

                <div class="date-info">
                    <strong>প্রকাশের তারিখ:</strong> $dateBn &nbsp;|&nbsp; <strong>প্রচারে:</strong> ${notice.createdBy}
                </div>

                <div class="notice-title">${notice.title}</div>

                $imageTag

                <div class="notice-body">${notice.content}</div>

                <div class="signature-section">
                    <div class="sig-box">
                        ${if (presidentSigUri != null) """<img src="$presidentSigUri" class="sig-img" />""" else """<div style="height: 40px;"></div>"""}
                        <div>সভাপতি / সাধারণ সম্পাদক</div>
                        <div style="font-size: 12px; font-weight: normal; color: #555;">মসজিদ পরিচালনা কমিটি, $mosqueName</div>
                        ${if (presidentStampUri != null) """<img src="$presidentStampUri" class="stamp-img" />""" else ""}
                    </div>
                </div>

                <div class="footer">
                    মসজিদ সেবা (Mosque Seba) — স্বচ্ছ ও আধুনিক মসজিদ পরিচালনা ইকোসিস্টেম।
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    private fun buildFinanceHtml(
        mosqueName: String,
        totalIncome: Double,
        totalExpense: Double,
        balance: Double,
        chanda: Double,
        cashFund: Double,
        bankFund: Double,
        jummahTotal: Double,
        jummahCount: Int,
        approvedCount: Int,
        pendingCount: Int,
        aiSummary: String,
        presidentSignatureUrl: String? = null,
        presidentStampUrl: String? = null,
        secretarySignatureUrl: String? = null,
        secretaryStampUrl: String? = null,
        treasurerSignatureUrl: String? = null,
        treasurerStampUrl: String? = null,
        mosqueSealUrl: String? = null
    ): String {
        val incBn = toBengaliDigits(String.format(Locale.US, "%,.0f", totalIncome))
        val expBn = toBengaliDigits(String.format(Locale.US, "%,.0f", totalExpense))
        val balBn = toBengaliDigits(String.format(Locale.US, "%,.0f", balance))
        val chandaBn = toBengaliDigits(String.format(Locale.US, "%,.0f", chanda))
        val cashBn = toBengaliDigits(String.format(Locale.US, "%,.0f", cashFund))
        val bankBn = toBengaliDigits(String.format(Locale.US, "%,.0f", bankFund))
        val jummahBn = toBengaliDigits(String.format(Locale.US, "%,.0f", jummahTotal))
        val printDateBn = toBengaliDigits(SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.forLanguageTag("bn-BD")).format(Date()))

        val sealDataUri = fileToBase64DataUri(mosqueSealUrl)
        val presidentSigUri = fileToBase64DataUri(presidentSignatureUrl)
        val presidentStampUri = fileToBase64DataUri(presidentStampUrl)
        val secretarySigUri = fileToBase64DataUri(secretarySignatureUrl)
        val secretaryStampUri = fileToBase64DataUri(secretaryStampUrl)
        val treasurerSigUri = fileToBase64DataUri(treasurerSignatureUrl)
        val treasurerStampUri = fileToBase64DataUri(treasurerStampUrl)

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>আর্থিক অডিট রিপোর্ট - $mosqueName</title>
            <style>
                @page { size: A4; margin: 15mm; }
                body {
                    font-family: 'SolaimanLipi', 'Noto Sans Bengali', system-ui, sans-serif;
                    color: #1a1a1a;
                    margin: 0;
                    padding: 15px;
                    background: #fff;
                    line-height: 1.5;
                }
                .report-container {
                    border: 2px solid #064E3B;
                    border-radius: 10px;
                    padding: 24px 30px;
                    max-width: 750px;
                    margin: 0 auto;
                }
                .bismillah {
                    text-align: center;
                    font-size: 18px;
                    color: #064E3B;
                    margin-bottom: 6px;
                }
                .header {
                    text-align: center;
                    border-bottom: 2px solid #D4AF37;
                    padding-bottom: 12px;
                    margin-bottom: 16px;
                }
                .seal-header-img {
                    width: 70px;
                    height: 70px;
                    object-fit: contain;
                    border-radius: 50%;
                    margin-bottom: 6px;
                    display: block;
                    margin-left: auto;
                    margin-right: auto;
                }
                .mosque-title {
                    font-size: 24px;
                    font-weight: bold;
                    color: #064E3B;
                    margin: 0;
                }
                .sub-title {
                    font-size: 15px;
                    color: #333;
                    font-weight: 600;
                    margin-top: 4px;
                }
                .meta-row {
                    display: flex;
                    justify-content: space-between;
                    font-size: 12px;
                    color: #666;
                    margin-bottom: 16px;
                }
                .card-grid {
                    display: flex;
                    gap: 12px;
                    margin-bottom: 16px;
                }
                .stat-card {
                    flex: 1;
                    padding: 12px;
                    border-radius: 8px;
                    text-align: center;
                    border: 1px solid #ddd;
                }
                .stat-income { background: #f0fdf4; border-color: #86efac; color: #166534; }
                .stat-expense { background: #fef2f2; border-color: #fca5a5; color: #991b1b; }
                .stat-balance { background: #fefce8; border-color: #fde047; color: #854d0e; }
                .stat-label { font-size: 13px; font-weight: bold; margin-bottom: 4px; }
                .stat-val { font-size: 18px; font-weight: 900; }
                .section-title {
                    font-size: 16px;
                    font-weight: bold;
                    color: #064E3B;
                    border-left: 4px solid #064E3B;
                    padding-left: 8px;
                    margin: 18px 0 10px 0;
                }
                .table-data {
                    width: 100%;
                    border-collapse: collapse;
                    margin-bottom: 16px;
                }
                .table-data th, .table-data td {
                    border: 1px solid #e2e8f0;
                    padding: 8px 12px;
                    font-size: 13px;
                }
                .table-data th {
                    background: #f8fafc;
                    font-weight: 700;
                    text-align: left;
                    color: #334155;
                }
                .ai-box {
                    background: #f8fafc;
                    border: 1px solid #cbd5e1;
                    border-radius: 8px;
                    padding: 14px 18px;
                    margin: 16px 0;
                    font-size: 13.5px;
                    color: #1e293b;
                    white-space: pre-wrap;
                }
                .signatures {
                    margin-top: 45px;
                    display: flex;
                    justify-content: space-between;
                }
                .sig-box {
                    flex: 1;
                    text-align: center;
                    border-top: 1px solid #475569;
                    padding-top: 6px;
                    font-size: 13px;
                    font-weight: bold;
                    color: #1e293b;
                }
                .sig-img {
                    max-height: 44px;
                    max-width: 130px;
                    object-fit: contain;
                    display: block;
                    margin: 0 auto 4px auto;
                }
                .stamp-img {
                    max-height: 48px;
                    max-width: 100px;
                    object-fit: contain;
                    display: block;
                    margin: 4px auto 0 auto;
                    opacity: 0.92;
                }
                .footer {
                    text-align: center;
                    font-size: 11px;
                    color: #94a3b8;
                    margin-top: 24px;
                    border-top: 1px dashed #e2e8f0;
                    padding-top: 8px;
                }
            </style>
        </head>
        <body>
            <div class="report-container">
                <div class="bismillah">بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ</div>
                <div class="header">
                    ${if (sealDataUri != null) """<img src="$sealDataUri" class="seal-header-img" />""" else ""}
                    <h1 class="mosque-title">$mosqueName</h1>
                    <div class="sub-title">বায়তুল মাল ও সার্বজনীন আর্থিক অডিট বিবরণী</div>
                </div>

                <div class="meta-row">
                    <span>প্রিন্ট ও ইস্যুর তারিখ: $printDateBn</span>
                    <span>অনুমোদিত লেনদেন: ${toBengaliDigits(approvedCount.toString())} টি &nbsp;|&nbsp; অপেক্ষমাণ: ${toBengaliDigits(pendingCount.toString())} টি</span>
                </div>

                <div class="card-grid">
                    <div class="stat-card stat-income">
                        <div class="stat-label">মোট অনুমোদিত আয়</div>
                        <div class="stat-val">৳ $incBn</div>
                    </div>
                    <div class="stat-card stat-expense">
                        <div class="stat-label">মোট অনুমোদিত ব্যয়</div>
                        <div class="stat-val">৳ $expBn</div>
                    </div>
                    <div class="stat-card stat-balance">
                        <div class="stat-label">বর্তমান নিট ব্যালেন্স</div>
                        <div class="stat-val">৳ $balBn</div>
                    </div>
                </div>

                <div class="section-title">তহবিল বণ্টন ও খতিয়ান বিবরণ</div>
                <table class="table-data">
                    <tr>
                        <th>খাতের বিবরণ</th>
                        <th>স্থিতি / সংগৃহীত পরিমাণ</th>
                    </tr>
                    <tr>
                        <td>নগদ ও ক্যাশবক্স ফান্ড</td>
                        <td><strong>৳ $cashBn</strong></td>
                    </tr>
                    <tr>
                        <td>ব্যাংক ও অনলাইন একাউন্ট ফান্ড</td>
                        <td><strong>৳ $bankBn</strong></td>
                    </tr>
                    <tr>
                        <td>মোট সদস্য চাঁদা আদায়</td>
                        <td><strong>৳ $chandaBn</strong></td>
                    </tr>
                    <tr>
                        <td>জুমার দানবাক্স সংগ্রহ (${toBengaliDigits(jummahCount.toString())} জুমা)</td>
                        <td><strong>৳ $jummahBn</strong></td>
                    </tr>
                </table>

                ${if (aiSummary.isNotBlank()) """
                <div class="section-title">এআই অডিট ও স্বচ্ছতা পর্যবেক্ষণ</div>
                <div class="ai-box">$aiSummary</div>
                """ else ""}

                <div class="signatures">
                    <div class="sig-box">
                        ${if (treasurerSigUri != null) """<img src="$treasurerSigUri" class="sig-img" />""" else """<div style="height: 38px;"></div>"""}
                        <div>কোষাধ্যক্ষ</div>
                        ${if (treasurerStampUri != null) """<img src="$treasurerStampUri" class="stamp-img" />""" else ""}
                    </div>
                    <div class="sig-box">
                        ${if (secretarySigUri != null) """<img src="$secretarySigUri" class="sig-img" />""" else """<div style="height: 38px;"></div>"""}
                        <div>সাধারণ সম্পাদক</div>
                        ${if (secretaryStampUri != null) """<img src="$secretaryStampUri" class="stamp-img" />""" else ""}
                    </div>
                    <div class="sig-box">
                        ${if (presidentSigUri != null) """<img src="$presidentSigUri" class="sig-img" />""" else """<div style="height: 38px;"></div>"""}
                        <div>সভাপতি</div>
                        ${if (presidentStampUri != null) """<img src="$presidentStampUri" class="stamp-img" />""" else ""}
                    </div>
                </div>

                <div class="footer">
                    মসজিদ সেবা (Mosque Seba) — বায়তুল মালের প্রতিটি টাকার পুঙ্খানুপুঙ্খ হিসাব ও স্বচ্ছ আমানত।
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    private fun buildCashboxVoucherHtml(
        mosqueName: String,
        dateStr: String,
        totalAmount: Double,
        breakdownList: List<Pair<String, Int>>,
        witness1: String,
        witness2: String,
        witness3: String,
        notes: String,
        imamSignatureUrl: String? = null,
        treasurerSignatureUrl: String? = null,
        treasurerStampUrl: String? = null,
        witnessSignatureUrl: String? = null,
        mosqueSealUrl: String? = null
    ): String {
        val totalBn = toBengaliDigits(String.format(Locale.US, "%,.0f", totalAmount))
        val dateBn = toBengaliDigits(dateStr)
        val nowBn = toBengaliDigits(SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.forLanguageTag("bn-BD")).format(Date()))

        val sealDataUri = fileToBase64DataUri(mosqueSealUrl)
        val imamSigUri = fileToBase64DataUri(imamSignatureUrl)
        val treasurerSigUri = fileToBase64DataUri(treasurerSignatureUrl)
        val treasurerStampUri = fileToBase64DataUri(treasurerStampUrl)
        val witnessSigUri = fileToBase64DataUri(witnessSignatureUrl)

        val rowsHtml = breakdownList.filter { it.second > 0 }.joinToString("\n") { (denom, count) ->
            val denomVal = denom.filter { it.isDigit() }.toIntOrNull() ?: 0
            val subtotal = denomVal * count
            """
            <tr>
                <td style="font-weight: bold; text-align: left; padding: 8px 12px;">$denom নোট</td>
                <td style="text-align: center; padding: 8px 12px;">${toBengaliDigits(count.toString())} টি</td>
                <td style="text-align: right; padding: 8px 12px; font-weight: bold;">৳ ${toBengaliDigits(String.format(Locale.US, "%,d", subtotal))}</td>
            </tr>
            """.trimIndent()
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>জুমার দানবাক্স গণনা প্রত্যয়ন ভাউচার - $mosqueName</title>
            <style>
                @page { size: A4; margin: 15mm; }
                body {
                    font-family: 'SolaimanLipi', 'Noto Sans Bengali', system-ui, sans-serif;
                    color: #1a1a1a;
                    margin: 0;
                    padding: 16px;
                    background: #fff;
                }
                .voucher-container {
                    border: 2px solid #064E3B;
                    border-radius: 12px;
                    padding: 24px 28px;
                    max-width: 680px;
                    margin: 0 auto;
                }
                .bismillah {
                    text-align: center;
                    font-size: 19px;
                    color: #064E3B;
                    margin-bottom: 6px;
                }
                .header {
                    text-align: center;
                    border-bottom: 2px solid #D4AF37;
                    padding-bottom: 12px;
                    margin-bottom: 16px;
                }
                .mosque-title {
                    font-size: 24px;
                    font-weight: bold;
                    color: #064E3B;
                    margin: 0;
                }
                .sub-title {
                    font-size: 13px;
                    color: #555;
                    margin-top: 4px;
                }
                .badge-voucher {
                    display: inline-block;
                    background: #064E3B;
                    color: #fff;
                    padding: 4px 16px;
                    border-radius: 20px;
                    font-size: 13px;
                    font-weight: bold;
                    margin-top: 8px;
                }
                .meta-row {
                    display: flex;
                    justify-content: space-between;
                    background: #F4F9F5;
                    border: 1px solid #D1E5D9;
                    border-radius: 8px;
                    padding: 10px 16px;
                    margin-bottom: 16px;
                    font-size: 14px;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-bottom: 16px;
                }
                th {
                    background: #064E3B;
                    color: #fff;
                    padding: 8px 12px;
                    font-size: 13px;
                }
                td {
                    border-bottom: 1px solid #E5E7EB;
                    font-size: 14px;
                }
                tr:nth-child(even) {
                    background: #FAFAFA;
                }
                .total-box {
                    background: #E6F4EA;
                    border: 2px solid #064E3B;
                    border-radius: 8px;
                    padding: 14px;
                    text-align: center;
                    margin: 16px 0;
                }
                .total-amount {
                    font-size: 28px;
                    font-weight: 900;
                    color: #064E3B;
                }
                .witness-section {
                    margin-top: 30px;
                    border-top: 1px dashed #bbb;
                    padding-top: 16px;
                }
                .witness-grid {
                    display: flex;
                    justify-content: space-between;
                    gap: 12px;
                    margin-top: 40px;
                }
                .sig-box {
                    flex: 1;
                    border-top: 1px solid #333;
                    text-align: center;
                    padding-top: 6px;
                    font-size: 13px;
                    font-weight: bold;
                    color: #333;
                }
                .sig-img {
                    max-height: 42px;
                    max-width: 120px;
                    object-fit: contain;
                    display: block;
                    margin: 0 auto 4px auto;
                }
                .stamp-img {
                    max-height: 46px;
                    max-width: 90px;
                    object-fit: contain;
                    display: block;
                    margin: 4px auto 0 auto;
                    opacity: 0.92;
                }
                .seal-header-img {
                    width: 65px;
                    height: 65px;
                    object-fit: contain;
                    border-radius: 50%;
                    margin-bottom: 6px;
                    display: block;
                    margin-left: auto;
                    margin-right: auto;
                }
                .sig-name {
                    font-size: 12px;
                    font-weight: normal;
                    color: #555;
                    margin-top: 2px;
                }
                .verification-note {
                    font-size: 11px;
                    color: #555;
                    background: #FDFDF6;
                    border: 1px solid #EADBBA;
                    border-radius: 6px;
                    padding: 8px 12px;
                    margin-top: 14px;
                    text-align: center;
                }
                .footer {
                    margin-top: 18px;
                    text-align: center;
                    font-size: 11px;
                    color: #888;
                }
            </style>
        </head>
        <body>
            <div class="voucher-container">
                <div class="bismillah">بِسْمِ ٱللَّهِ ٱلرَّحْمَٰনِ ٱلرَّحِيمِ</div>
                <div class="header">
                    ${if (sealDataUri != null) """<img src="$sealDataUri" class="seal-header-img" />""" else ""}
                    <h1 class="mosque-title">$mosqueName</h1>
                    <div class="sub-title">স্বচ্ছ বায়তুলমাল ব্যবস্থাপনা ও নিরীক্ষা নথি</div>
                    <div class="badge-voucher">সাপ্তাহিক জুমার দানবাক্স গণনা প্রত্যয়ন ভাউচার</div>
                </div>

                <div class="meta-row">
                    <div><strong>গণনার তারিখ:</strong> $dateBn</div>
                    <div><strong>ভাউচার প্রস্তুত:</strong> $nowBn</div>
                </div>

                <table>
                    <thead>
                        <tr>
                            <th style="text-align: left;">টাকার নোটের মান</th>
                            <th style="text-align: center;">নোটের সংখ্যা</th>
                            <th style="text-align: right;">উপ-মোট পরিমাণ</th>
                        </tr>
                    </thead>
                    <tbody>
                        $rowsHtml
                    </tbody>
                </table>

                <div class="total-box">
                    <div style="font-size: 13px; color: #555; margin-bottom: 2px;">সর্বমোট সংগৃহীত দানবাক্স ফান্ড</div>
                    <div class="total-amount">৳ $totalBn</div>
                </div>

                ${if (notes.isNotBlank()) """
                <div style="font-size: 13px; background: #fafafa; border-left: 3px solid #D4AF37; padding: 8px 12px; margin-bottom: 12px;">
                    <strong>মন্তব্য / বিবরণ:</strong> $notes
                </div>
                """ else ""}

                <div class="witness-section">
                    <div style="font-size: 13px; font-weight: bold; color: #064E3B; margin-bottom: 4px;">
                        উপস্থিত ৩-জন সাক্ষীর প্রত্যয়ন ও স্বাক্ষর:
                    </div>
                    <div class="witness-grid">
                        <div class="sig-box">
                            ${if (imamSigUri != null) """<img src="$imamSigUri" class="sig-img" />""" else """<div style="height: 36px;"></div>"""}
                            <div>১ম সাক্ষী (ইমাম / খতিব)</div>
                            <div class="sig-name">$witness1</div>
                        </div>
                        <div class="sig-box">
                            ${if (treasurerSigUri != null) """<img src="$treasurerSigUri" class="sig-img" />""" else """<div style="height: 36px;"></div>"""}
                            <div>২য় সাক্ষী (কোষাধ্যক্ষ / সভাপতি)</div>
                            <div class="sig-name">$witness2</div>
                            ${if (treasurerStampUri != null) """<img src="$treasurerStampUri" class="stamp-img" />""" else ""}
                        </div>
                        <div class="sig-box">
                            ${if (witnessSigUri != null) """<img src="$witnessSigUri" class="sig-img" />""" else """<div style="height: 36px;"></div>"""}
                            <div>৩য় সাক্ষী (সাধারণ মুসল্লি)</div>
                            <div class="sig-name">$witness3</div>
                        </div>
                    </div>
                </div>

                <div class="verification-note">
                    🔒 এই ভাউচারটি মসজিদে উপস্থিত মুসল্লিদের প্রত্যক্ষ উপস্থিতিতে গণনাকৃত এবং মসজিদ সেবা ডিজিটাল লেজারে স্থায়ীভাবে সংরক্ষিত।
                </div>

                <div class="footer">
                    মসজিদ সেবা (Mosque Seba) • স্বচ্ছতা ও আমানতদারিতার ডিজিটাল প্রতীক
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    private fun buildInvitationPosterHtml(
        mosqueName: String,
        mosqueAddress: String,
        title: String,
        eventType: String,
        chiefGuest: String,
        specialSpeaker: String,
        honorableGuests: String,
        president: String,
        dateStr: String,
        timeStr: String,
        venue: String,
        description: String,
        organizer: String,
        contact: String,
        presidentSignatureUrl: String? = null,
        presidentStampUrl: String? = null,
        mosqueSealUrl: String? = null
    ): String {
        val sealDataUri = fileToBase64DataUri(mosqueSealUrl)
        val presidentSigUri = fileToBase64DataUri(presidentSignatureUrl)
        val presidentStampUri = fileToBase64DataUri(presidentStampUrl)

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>$title - $mosqueName</title>
            <style>
                @page { size: A4; margin: 12mm; }
                body {
                    font-family: 'SolaimanLipi', 'Noto Sans Bengali', system-ui, sans-serif;
                    color: #1a1a1a;
                    margin: 0;
                    padding: 16px;
                    background: #fff;
                    line-height: 1.45;
                }
                .poster-container {
                    border: 4px double #064E3B;
                    border-radius: 14px;
                    padding: 24px 26px;
                    max-width: 720px;
                    margin: 0 auto;
                    position: relative;
                    background: #FCFDFB;
                }
                .bismillah {
                    text-align: center;
                    font-size: 22px;
                    color: #064E3B;
                    margin-bottom: 4px;
                    font-weight: bold;
                }
                .salam {
                    text-align: center;
                    font-size: 13px;
                    color: #555;
                    margin-bottom: 10px;
                }
                .mosque-header {
                    text-align: center;
                    background: linear-gradient(135deg, #064E3B 0%, #15803D 100%);
                    color: #fff;
                    padding: 14px 18px;
                    border-radius: 10px;
                    margin-bottom: 16px;
                }
                .mosque-name {
                    font-size: 24px;
                    font-weight: 900;
                    color: #FFDF78;
                    margin: 0;
                }
                .mosque-addr {
                    font-size: 13px;
                    color: #E2F0D9;
                    margin-top: 3px;
                }
                .event-badge {
                    display: inline-block;
                    background: #D4AF37;
                    color: #0D3320;
                    font-size: 13px;
                    font-weight: 800;
                    padding: 4px 18px;
                    border-radius: 16px;
                    margin-top: 6px;
                }
                .poster-title {
                    text-align: center;
                    font-size: 23px;
                    font-weight: 900;
                    color: #064E3B;
                    margin: 14px 0;
                    padding: 10px;
                    background: #F2F8F4;
                    border-left: 4px solid #D4AF37;
                    border-right: 4px solid #D4AF37;
                    border-radius: 6px;
                }
                .guest-card {
                    background: #fff;
                    border: 1px solid #D4E3D9;
                    border-radius: 8px;
                    padding: 10px 14px;
                    margin-bottom: 8px;
                }
                .guest-role {
                    font-size: 12px;
                    font-weight: bold;
                    color: #D4AF37;
                    text-transform: uppercase;
                }
                .guest-name {
                    font-size: 15px;
                    font-weight: bold;
                    color: #111;
                    margin-top: 2px;
                }
                .info-banner {
                    display: flex;
                    justify-content: space-around;
                    background: #EAF4EE;
                    border: 1px solid #C1DEC9;
                    border-radius: 8px;
                    padding: 12px;
                    margin: 16px 0;
                    text-align: center;
                }
                .info-item-label {
                    font-size: 11px;
                    color: #555;
                    font-weight: bold;
                }
                .info-item-val {
                    font-size: 15px;
                    font-weight: 800;
                    color: #064E3B;
                    margin-top: 2px;
                }
                .program-details {
                    font-size: 14px;
                    color: #222;
                    text-align: justify;
                    white-space: pre-wrap;
                    background: #fff;
                    border: 1px dashed #A5D6A7;
                    border-radius: 8px;
                    padding: 12px 16px;
                    margin-bottom: 16px;
                }
                .invitation-call {
                    text-align: center;
                    font-size: 14px;
                    font-weight: bold;
                    color: #064E3B;
                    margin-bottom: 16px;
                    padding: 8px;
                    background: #FDFCF5;
                    border: 1px solid #EDE6CD;
                    border-radius: 6px;
                }
                .organizer-section {
                    display: flex;
                    justify-content: space-between;
                    border-top: 2px solid #D4AF37;
                    padding-top: 12px;
                    font-size: 13px;
                }
                .footer {
                    margin-top: 14px;
                    text-align: center;
                    font-size: 11px;
                    color: #888;
                }
            </style>
        </head>
        <body>
            <div class="poster-container">
                <div class="bismillah">بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ</div>
                <div class="salam">আসসালামু আলাইকুম ওয়া রাহমাতুল্লাহি ওয়া বারাকাতুহু</div>

                <div class="mosque-header">
                    <div class="mosque-name">$mosqueName</div>
                    <div class="mosque-addr">$mosqueAddress</div>
                    <div class="event-badge">$eventType</div>
                </div>

                <div class="poster-title">$title</div>

                <div class="info-banner">
                    <div>
                        <div class="info-item-label">📅 তারিখ</div>
                        <div class="info-item-val">$dateStr</div>
                    </div>
                    <div>
                        <div class="info-item-label">⏰ সময়</div>
                        <div class="info-item-val">$timeStr</div>
                    </div>
                    <div>
                        <div class="info-item-label">📍 স্থান</div>
                        <div class="info-item-val">$venue</div>
                    </div>
                </div>

                ${if (chiefGuest.isNotBlank()) """
                <div class="guest-card">
                    <div class="guest-role">★ প্রধান অতিথি</div>
                    <div class="guest-name">$chiefGuest</div>
                </div>
                """ else ""}

                ${if (specialSpeaker.isNotBlank()) """
                <div class="guest-card">
                    <div class="guest-role">★ প্রধান বক্তা / আলোচক</div>
                    <div class="guest-name">$specialSpeaker</div>
                </div>
                """ else ""}

                ${if (honorableGuests.isNotBlank()) """
                <div class="guest-card">
                    <div class="guest-role">★ বিশেষ মেহমান ও আলোচকবৃন্দ</div>
                    <div class="guest-name">$honorableGuests</div>
                </div>
                """ else ""}

                ${if (president.isNotBlank()) """
                <div class="guest-card">
                    <div class="guest-role">★ সভাপতিত্ব করবেন</div>
                    <div class="guest-name">$president</div>
                </div>
                """ else ""}

                ${if (description.isNotBlank()) """
                <div style="font-size: 12px; font-weight: bold; color: #064E3B; margin: 10px 0 4px 2px;">কর্মসূচি ও বিবরণ:</div>
                <div class="program-details">$description</div>
                """ else ""}

                <div class="invitation-call">
                    উক্ত দ্বীনি মাহফিলে আপনার উপস্থিতি ও আন্তরিক দোয়াই আমাদের একান্ত কাম্য।
                </div>

                <div class="organizer-section">
                    <div>
                        <strong>আমন্ত্রণে:</strong><br>
                        ${organizer.ifBlank { "মসজিদ পরিচালনা কমিটি ও এলাকাবাসী" }}
                    </div>
                    <div style="text-align: right;">
                        <strong>জরুরি যোগাযোগ:</strong><br>
                        ${contact.ifBlank { "মসজিদ দপ্তর" }}
                    </div>
                </div>

                <div style="margin-top: 20px; display: flex; justify-content: space-between; align-items: flex-end; padding: 0 10px;">
                    <div>
                        ${if (sealDataUri != null) """<img src="$sealDataUri" style="width: 60px; height: 60px; object-fit: contain; border-radius: 50%; opacity: 0.85;" />""" else ""}
                    </div>
                    ${if (president.isNotBlank() || presidentSigUri != null) """
                    <div style="text-align: center; border-top: 1px solid #D4AF37; padding-top: 4px; min-width: 160px;">
                        ${if (presidentSigUri != null) """<img src="$presidentSigUri" style="max-height: 42px; max-width: 130px; display: block; margin: 0 auto 2px auto;" />""" else """<div style="height: 32px;"></div>"""}
                        <div style="font-weight: bold; font-size: 12px; color: #064E3B;">সভাপতি / আহ্বায়ক</div>
                        <div style="font-size: 11px; color: #555;">${president.ifBlank { "মসজিদ পরিচালনা কমিটি" }}</div>
                        ${if (presidentStampUri != null) """<img src="$presidentStampUri" style="max-height: 46px; max-width: 90px; display: block; margin: 2px auto 0 auto; opacity: 0.9;" />""" else ""}
                    </div>
                    """ else ""}
                </div>

                <div class="footer">
                    মসজিদ সেবা (Mosque Seba) • ডিজিটাল ইসলামিক নোটিশ ও দাওয়াত সিস্টেম
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }
}
