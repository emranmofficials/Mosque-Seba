# MOSQUE SEBA (মসজিদ সেবা) — ROLE & PERMISSION AUDIT (RBAC ENGINE)

**Project:** Mosque Seba (`com.mosqueseba.app`)  
**Authorization Principle:** Client UX adaptation + Server-side authoritative enforcement via Supabase RLS & Stored Procedures.  
**Formula:** `Authenticated AND Tenant Match AND Role Permission AND Override AND Relationship -> Allow`  

---

## 1. Role Catalog & Reconciliation

The source documentation and `Enums.kt` define an exact, culturally authentic Bangladeshi mosque governance structure consisting of **16 standardized positions** organized into **4 committee tiers**, plus specialized roles for Islamic education:

```
                                  [MOSQUE COMMITTEE STRUCTURE]
                                                │
         ┌──────────────────┬───────────────────┴───────────────────┬──────────────────┐
         │                  │                                       │                  │
   [1. CORE EXECUTIVE]  [2. SECRETARIAL]                    [3. ADVISORY & EXEC]  [4. STAFF & MUSALLI]
   • President          • Organizing Secretary              • Executive Member    • Muazzin
   • Vice-President     • Publicity Secretary               • Imam / Khatib       • Khadem
   • General Secretary  • Office Secretary                  • Advisory Board      • General Musalli
   • Assistant Secretary• Welfare & Religious Education
   • Treasurer
   • Assistant Treasurer
```

### 1.1 Complete 16-Role Catalog

| # | Role Code | Bengali Title | Tier | Level | Primary Duties & Responsibilities |
|---|---|---|---|---|---|
| 1 | `president` | সভাপতি | 1. Core Executive | 1 | Supreme administrative guardian, chairs meetings, final authority on finance & policy. |
| 2 | `vice_president` | সহ-সভাপতি | 1. Core Executive | 2 | Acts in absence of President, supervises development sub-committees and finance. |
| 3 | `general_secretary`| সাধারণ সম্পাদক / সেক্রেটারি | 1. Core Executive | 2 | Daily mosque administration, convenes meetings, drafts agenda/resolutions, oversees staff. |
| 4 | `assistant_secretary`| সহ-সাধারণ সম্পাদক | 1. Core Executive | 3 | Assists General Secretary in executive and administrative functions. |
| 5 | `treasurer` | কোষাধ্যক্ষ / অর্থ সম্পাদক | 1. Core Executive | 2 | Financial bookkeeping, Chanda collection, bank deposits, payroll, monthly financial statements. |
| 6 | `assistant_treasurer`| সহ-অর্থ সম্পাদক | 1. Core Executive | 3 | Assists Treasurer with ledger entries, vouchers, and Jummah cashbox collections. |
| 7 | `organizing_secretary`| সাংগঠনিক সম্পাদক | 2. Secretarial | 3 | Community outreach, coordinating religious events, membership drives. |
| 8 | `publicity_secretary`| প্রচার ও প্রকাশনা সম্পাদক | 2. Secretarial | 3 | Notice publishing, wall quotes, digital smart board maintenance, event campaigns. |
| 9 | `office_secretary`| দপ্তর সম্পাদক | 2. Secretarial | 3 | Archive maintenance, waqf deeds, official letters, meeting minutes, resolution books. |
| 10 | `welfare_education_secretary` | সমাজকল্যাণ ও ধর্মীয় শিক্ষা সম্পাদক | 2. Secretarial | 3 | Madrasa/Maktab supervision, Zakat fund, medical assistance, orphan relief. |
| 11 | `executive_member`| সাধারণ সদস্য (কার্যনির্বাহী) | 3. General & Advisory | 4 | Participates in committee voting, serves as independent Jummah cashbox witness. |
| 12 | `imam` | পদাধিকারবলে সদস্য (খতিব/ইমাম) | 3. General & Advisory | 3 | Leads prayers, delivers Khutbah, Sharia review, religious curriculum guidance. |
| 13 | `advisory_board` | উপদেষ্টা পরিষদ | 3. General & Advisory | 2 | Strategic counsel, dispute resolution, financial auditing, election oversight. |
| 14 | `muazzin` | মুয়াজ্জিন | 4. Staff & Musalli | 5 | Adhan, prayer time coordination, duty roster, Maktab teaching assistance. |
| 15 | `khadem` | খাদেম | 4. Staff & Musalli | 5 | Mosque cleaning, Wudu area maintenance, utility upkeep, food distribution. |
| 16 | `general_member` | সাধারণ সদস্য / মুসল্লি | 4. Staff & Musalli | 5 | General prayer congregation, monthly Chanda payment, voting in elections. |

*Additional Specialized Roles:*
- `madrasa_teacher` (মাদরাসা শিক্ষক) — Academic evaluation, student attendance, Hifz star rating, report cards.
- `guardian` (অভিভাবক) — Portal access restricted strictly to linked children's progress.

---

## 2. Seven UI Interface Profiles (UIUX §2)

The application adapts dynamically to the authenticated user's assigned role and permissions:

| Profile | Target Audience | Primary Bottom Navigation Destinations | More Hub Focus |
|---|---|---|---|
| **UI-01** | President / Super Admin | 1. ড্যাশবোর্ড<br>2. সদস্য<br>3. ফাইন্যান্স<br>4. নোটিশ<br>5. অন্যান্য | Full administrative control, voucher approval, election gazette, official seal & signatures. |
| **UI-02** | Committee Executives | 1. ড্যাশবোর্ড<br>2. সদস্য<br>3. ফাইন্যান্স<br>4. নোটিশ<br>5. অন্যান্য | Role-gated forms (Treasury entry, Secretarial minutes, Publicity smart board). |
| **UI-03** | General Musalli | 1. ড্যাশবোর্ড<br>2. আমার চাঁদা<br>3. নামাজের সময়<br>4. নোটিশ<br>5. প্রোফাইল | Transparent financial reports, digital donation receipts, Janaza alerts, community voting. |
| **UI-04** | Operational Staff | 1. ড্যাশবোর্ড<br>2. নামাজের সময়<br>3. রোস্টার<br>4. খাবার তালিকা<br>5. নোটিশ | Daily waqt rosters, meal arrangement schedule, leave applications. |
| **UI-05** | Madrasa Teacher | 1. ড্যাশবোর্ড<br>2. শিক্ষার্থী<br>3. হাজিরা<br>4. হিফজ ও ফলাফল<br>5. নোটিশ | Student directory, daily attendance register, para-by-para star tracking. |
| **UI-06** | Guardian | 1. ড্যাশবোর্ড<br>2. আমার সন্তান<br>3. অগ্রগতি ও ফলাফল<br>4. ফি প্রদান<br>5. নোটিশ | Child's academic report card, attendance history, fee payment receipts. |
| **UI-07** | System Super Admin | 1. ড্যাশবোর্ড<br>2. মসজিদসমূহ<br>3. সাবস্ক্রিপশন<br>4. অডিট লগ<br>5. সেটিংস | Platform management, mosque onboarding approval, security audit trail. |

---

## 3. Granular Action-Level Permissions

| Permission Key | Description | Authorized Roles |
|---|---|---|
| `finance.view` | View general income and expense ledgers | All Committee, Musalli (summary view) |
| `finance.create` | Submit income or expense transaction vouchers | President, Vice President, Treasurer, Assistant Treasurer |
| `finance.approve` | Approve pending expense vouchers (per threshold) | President, Vice President |
| `finance.reconcile` | Perform monthly cash/bank closing | President, Treasurer |
| `staff.salary.view` | View individual staff salary amounts and bonuses | President, Vice President, Treasurer, Secretary |
| `staff.salary.pay` | Generate salary payments and cross-post to Finance | President, Treasurer |
| `member.manage` | Add, edit, or archive member profiles | President, VP, General Secretary, Organizing Secretary |
| `chanda.receive` | Collect Chanda and generate digital QR receipt | President, Treasurer, Assistant Treasurer, Office Secretary |
| `cashbox.manage` | Open cashbox and record banknote denominations | President, Vice President, Treasurer |
| `cashbox.witness` | Sign as independent cashbox witness | Executive Member, General Secretary, Imam |
| `notice.publish` | Publish official public or committee notices | President, VP, General Secretary, Publicity Secretary |
| `meeting.schedule` | Schedule committee meetings and record resolutions | President, General Secretary, Office Secretary |
| `madrasa.manage` | Student admission, fee collection, exam publishing | Welfare Secretary, Madrasa Teacher, President |
| `sharia.review` | Submit formal Sharia compliance notes | Imam / Khatib |
| `advisory.guidance`| Submit advisory board guidance and audit findings | Advisory Board |

---

## 4. Mission-Critical Security Invariants

1. **Self-Approval Prohibition:**
   - Enforced by PostgreSQL CHECK constraint and RPC logic: `CHECK (approved_by is null or approved_by <> created_by)`. A user cannot approve a transaction they created, regardless of rank.
2. **Tiered Financial Approval Thresholds:**
   - **Tier 1 (≤ ৳5,000):** Assigned authority (Treasurer / Assistant Treasurer).
   - **Tier 2 (৳5,001 – ৳20,000):** President approval required.
   - **Tier 3 (> ৳20,000):** President + Vice President joint committee approval required.
3. **Salary Visibility Cloaking:**
   - Operational staff profiles reveal compensation details ONLY to holders of `staff.salary.view`. For all other roles, salary columns are masked in both UI and API queries.
4. **Three-Witness Cashbox Requirement:**
   - Jummah cashbox voucher submission requires verification and signatures from 3 distinct committee members (e.g., President, Treasurer, and Executive Member).
