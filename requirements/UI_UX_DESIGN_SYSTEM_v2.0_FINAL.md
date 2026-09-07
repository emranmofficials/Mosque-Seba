# মসজিদ কমিটি ম্যানেজমেন্ট সিস্টেম
## PRD v2.0 — FINAL UI/UX & INFORMATION ARCHITECTURE

**Platform:** Android Mobile  
**Primary Language:** বাংলা  
**Design:** Islamic Modern Custom UI  
**Material Design 3:** ব্যবহার করা যাবে না

---

## 1. UI/UX মূল নীতি

অ্যাপটি একটি single Android application হবে, কিন্তু user role, permission এবং relationship অনুযায়ী ভিন্ন interface experience দেখাবে।

মূল নীতি:
- Role-based UI
- Permission-aware navigation
- Mobile-first
- Bengali-first
- Islamic Modern
- Compact premium cards
- সর্বোচ্চ ৫টি primary bottom-navigation item
- বিস্তারিত feature module dashboard/More-এর মধ্যে
- UI permission security-এর বিকল্প নয়; backend authoritative

---

## 2. মোট Role-Based Interface Profile

অ্যাপে ৭টি primary interface profile থাকবে:

### UI-01 — President / Administrator
পূর্ণ administrative interface।

### UI-02 — Committee Executive
Vice-President, Secretary, Assistant Secretary, Treasurer, Assistant Treasurer, Organizing Secretary, Publicity Secretary, Welfare Secretary, Executive Member-এর permission-driven interface।

### UI-03 — General Member
নিজের চাঁদা, নোটিশ, নামাজ, profile ও mosque information-কেন্দ্রিক interface।

### UI-04 — Staff
Imam, Muazzin, Khadem ও Other Staff-এর duty/prayer/notice-কেন্দ্রিক interface।

### UI-05 — Madrasa Teacher
Student, attendance, homework, Hifz, exam/result ও academic operations-কেন্দ্রিক interface।

### UI-06 — Guardian
নিজের linked children-এর academic information-কেন্দ্রিক interface।

### UI-07 — System Super Admin
Platform-level mosque approval, subscription, support ও security interface।

একজন user-এর একাধিক role থাকতে পারে। সেক্ষেত্রে permission অনুযায়ী relevant features merged হবে।

---

## 3. Role Mapping

| Role | Interface |
|---|---|
| President | UI-01 |
| Vice-President | UI-02 |
| General Secretary | UI-02 |
| Assistant Secretary | UI-02 |
| Treasurer | UI-02 |
| Assistant Treasurer | UI-02 |
| Organizing Secretary | UI-02 |
| Publicity Secretary | UI-02 |
| Welfare Secretary | UI-02 |
| Executive Member | UI-02 |
| General Member | UI-03 |
| Imam | UI-04; academic permission থাকলে UI-05 |
| Muazzin | UI-04 |
| Khadem | UI-04 |
| Other Staff | UI-04 |
| Madrasa Teacher | UI-05 |
| Guardian | UI-06 |
| System Super Admin | UI-07 |

---

# 4. Universal App Shell

## Global Header

বাম:
- official app logo
- current user name
- role/designation

ডান:
- notification
- profile/menu

Mosque name এবং Committee ID global header-এ নয়; Dashboard Hero Card-এ থাকবে।

## Bottom Navigation

Role অনুযায়ী ৪–৫টি primary destination।

## Context Action

শুধুমাত্র প্রয়োজনীয় screen-এ:
- Add
- Create
- Pay
- Record
- Submit

FAB সর্বত্র ব্যবহার করা যাবে না।

---

# 5. UI-01 — President / Administrator

## Bottom Navigation — 5 items

1. ড্যাশবোর্ড
2. সদস্য
3. ফাইন্যান্স
4. নোটিশ
5. আরও

### Dashboard Order

1. Global Header
2. Mosque Hero Card
3. Smart Board
4. Prayer Time Card
5. KPI Summary
6. Quick Actions
7. Recent Activity
8. Upcoming Meetings/Events
9. Important Alerts

### Hero Card

- Mosque name
- Address
- Committee ID
- Mosque logo/avatar

### Smart Board

প্রায় ৫ সেকেন্ড পরপর পরিবর্তন:
- English date
- Bangla date
- Hijri date
- current time
- সকাল/দুপুর/বিকেল/সন্ধ্যা/রাত
- urgent notice
- Islamic/Hajj information
- meeting reminder
- prayer information

### KPI — 4 cards

- মোট সদস্য
- মোট ব্যালেন্স
- বকেয়া চাঁদা
- জরুরি/অমীমাংসিত বিষয়

### Quick Actions — 6

1. নতুন সদস্য
2. আয় যোগ করুন
3. ব্যয় যোগ করুন
4. চাঁদা গ্রহণ
5. সভা তৈরি
6. নোটিশ প্রকাশ

---

# 6. President — Member Interface

Member tab → Member Command Center

### KPI — exactly 3 compact cards

1. মোট সদস্য
2. সক্রিয় সদস্য
3. বকেয়া চাঁদা

### Search

`সদস্য খুঁজুন (নাম, মোবাইল, আইডি)`

### Actions

- নতুন সদস্য যোগ করুন
- filter
- sort

### Member Card

- photo/avatar
- name
- member ID
- role badge
- mobile
- monthly chanda
- payment status
- overdue preview

### Member Profile

1. পরিচিতি
2. যোগাযোগ
3. Role
4. চাঁদা
5. Payment history
6. Ledger where authorized
7. Receipt
8. Activity

---

# 7. President — Finance Interface

Finance tab → Finance Command Center

### KPI

- Current balance
- Total income
- Total expense
- Pending approval

### Internal Sections

1. Overview
2. Income
3. Expense
4. Chanda
5. Approval
6. Payroll
7. Madrasa Fees
8. Reports
9. Reconciliation

### Primary Actions

- আয় যোগ করুন
- ব্যয় যোগ করুন
- চাঁদা গ্রহণ
- salary payment
- fee verification

---

# 8. President — Notice Interface

Notice tab:

- সব
- গুরুত্বপূর্ণ
- জরুরি
- সভা
- মাদরাসা
- Targeted

Create/Edit:
- title
- content
- attachment
- audience
- priority
- publish/expiry

AI:
Generate Draft → Edit → Human Review → Publish

AI নিজে publish করতে পারবে না।

---

# 9. President — More

### Committee
- Committee Management
- Roles & Permissions
- Members
- Meetings
- Tasks

### Finance
- Finance
- Chanda
- Payroll
- Budget
- Reports
- Reconciliation

### Mosque Operations
- Prayer Times
- Duty Roster
- Food Schedule
- Events
- Welfare

### Madrasa
- Madrasa Dashboard
- Students
- Guardians
- Classes
- Subjects
- Teachers
- Attendance
- Hifz
- Exams
- Fees
- Timetable
- Homework
- Academic Reports

### System
- Notifications
- Documents
- Subscription
- Settings
- Audit/Security যেখানে অনুমোদিত

---

# 10. UI-02 — Committee Executive

## Bottom Navigation — 5

1. ড্যাশবোর্ড
2. সদস্য
3. ফাইন্যান্স
4. নোটিশ
5. আরও

Permission অনুযায়ী item/action hide বা disable হবে।

### Role emphasis

**Treasurer:** Finance prominent  
**Secretary:** Meeting + Notice + Tasks prominent  
**Welfare Secretary:** Welfare prominent  
**Publicity Secretary:** Notice + Events prominent  
**Executive Member:** assigned/read-only actions

---

# 11. UI-03 — General Member

## Bottom Navigation — 5

1. হোম
2. আমার চাঁদা
3. নোটিশ
4. নামাজ
5. আরও

### Home Order

1. Header
2. Mosque mini hero
3. Smart Board
4. Next Prayer
5. My Chanda Summary
6. Quick Actions
7. Important Notice

### Quick Actions — 4

1. চাঁদা পরিশোধ করুন
2. আমার চাঁদা
3. নোটিশ
4. নামাজের সময়

### My Chanda

- current month
- due
- paid
- outstanding
- payment history
- receipt

সাধারণ সদস্য committee financial ledger দেখতে পারবে না।

---

# 12. UI-04 — Staff

## Bottom Navigation — 5

1. হোম
2. দায়িত্ব
3. নামাজ
4. নোটিশ
5. আরও

### Home

- Today's duty
- Next prayer
- announcements
- attendance
- leave status

### Duty

- today's roster
- weekly roster
- assigned prayer
- substitute
- duty history

### Prayer

- prayer times
- iqamah
- special prayer
- relevant instructions

### More

- attendance
- leave
- profile
- payroll summary যেখানে entitled
- notifications
- settings

Salary amount `staff.salary.view` ছাড়া দেখানো যাবে না।

---

# 13. UI-05 — Madrasa Teacher

## Bottom Navigation — 5

1. মাদরাসা
2. শিক্ষার্থী
3. উপস্থিতি
4. হোমওয়ার্ক
5. আরও

### Madrasa Home

- total students
- today's classes
- attendance status
- pending homework review
- upcoming exams
- Hifz reminders

### Students

- search
- class filter
- student profile
- academic 360

### Attendance

- class
- date
- student status
- bulk mark
- submit

### Homework

- create
- list
- submissions
- review
- feedback

### More

- Hifz
- Exams
- Results
- Timetable
- Fees where permitted
- Reports
- Guardian communication
- Settings

---

# 14. UI-06 — Guardian

## Bottom Navigation — 5

1. হোম
2. সন্তান
3. পড়াশোনা
4. নোটিশ
5. আরও

### Home

- children count
- today's attendance
- homework
- latest result
- important notice

### Children

প্রতিটি child card:
- name
- class
- attendance
- Hifz progress
- fee status where permitted

### Study

- timetable
- homework
- Hifz
- exam/results
- academic report

Guardian শুধুমাত্র server-authorized linked children দেখতে পারবে।

---

# 15. UI-07 — System Super Admin

## Bottom Navigation — 5

1. ওভারভিউ
2. মসজিদ
3. সাবস্ক্রিপশন
4. সাপোর্ট
5. আরও

### Overview

- total mosques
- pending approvals
- active subscriptions
- incidents
- system health

### Mosques

- applications
- approval
- suspension
- tenant details
- security status

### Subscription

- plans
- subscriptions
- invoices
- payments
- entitlements

### Support

- tickets
- incidents
- messages

### More

- audit
- security events
- system configuration
- platform notifications

---

# 16. Madrasa Internal Navigation

Madrasa module-এর সব feature bottom navigation-এ রাখা যাবে না।

Primary module destinations:
1. Dashboard
2. Students
3. Classes
4. Attendance
5. Hifz
6. Exams
7. Fees
8. Timetable
9. Homework
10. Reports

Dashboard + cards + module menu দিয়ে access হবে।

---

# 17. Finance Internal Navigation

1. Overview
2. Income
3. Expense
4. Chanda
5. Approval
6. Payroll
7. Reports

---

# 18. Navigation Rules

1. Bottom navigation = high-frequency primary destinations only।
2. Maximum 5 bottom navigation items।
3. বড় feature set module dashboard/More-এর মধ্যে থাকবে।
4. একই action একাধিক জায়গায় অপ্রয়োজনে duplicate করা যাবে না।
5. এক screen-এ 8–10টি primary button রাখা যাবে না।
6. Delete/archive/reversal/reject/suspend confirmation ছাড়া হবে না।
7. Financial action: Review → Confirmation → Secure RPC → Result।
8. UI visibility permission-aware হবে; backend security independent।
9. Notification deep-link permission check করবে।

---

# 19. Common Screen Patterns

### List
Header → Search → Filter → KPI(optional) → List → Context Action

### Detail
Header → Summary → Sections → Actions

### Create/Edit
Header → Form → Validation → Review → Submit

### Payment
Select → Amount → Method → Review → Confirm → Receipt

### Approval
Pending → Detail → Approve/Reject → Confirmation → Result

### Report
Filter → Summary → Charts → Detail → Export

---

# 20. Global States

প্রতিটি data screen-এ থাকতে হবে:
- Loading
- Empty
- Error
- Retry
- Offline
- Permission denied
- Subscription restricted
- Success

---

# 21. Mobile UX

Target: Android phones; tablet later।

Rules:
- no horizontal overflow
- compact cards
- readable Bengali
- keyboard-safe forms
- state restoration
- scroll position preservation
- large touch targets
- no unnecessary full-screen decoration

---

# 22. Design System

Required:
- MosqueColors
- MosqueTypography
- MosqueShapes
- MosqueSpacing
- MosqueElevation
- MosqueMotion
- MosqueIcons
- MosqueComponents

Visual:
- Deep Emerald #1B5E20
- Gold/Amber #FFC107
- Light Green #4CAF50
- Background #F5F5F5/#FFFFFF
- Card #F8FAFC
- Text #212121/#1E293B
- 16dp primary radius
- Islamic geometric patterns
- subtle mosque motifs
- premium but restrained animation

Typography:
- H1 32sp ExtraBold
- H2 24sp Bold
- H3 18sp SemiBold
- Body Large 16sp Medium
- Body Medium 14sp
- Label 12sp Medium
- Button 16sp SemiBold

---

# 23. Permission-Aware UI

Examples:

`finance.approve` না থাকলে Approve action নেই।

`staff.salary.view` না থাকলে salary amount hidden।

`madrasa.ai.use` না থাকলে AI action নেই।

`notice.manage` না থাকলে publish/edit নেই।

`guardian.portal` না থাকলে guardian portal নেই।

কিন্তু UI hide কখনো security boundary নয়। Backend independently authorization করবে।

---

# 24. Notification Center

সব authenticated user-এর জন্য:

Categories:
- All
- Finance
- Chanda
- Notice
- Meeting
- Prayer
- Duty
- Madrasa
- Guardian
- System

Unread badge থাকবে।

Tap → relevant deep-linked screen।

---

# 25. Profile / Settings

Profile:
- photo
- name
- roles
- mosque
- member/staff ID
- contact
- account status

Settings:
- language
- notifications
- appearance
- privacy
- security
- password
- sync
- about
- support
- logout

---

# 26. Final Navigation Map

### President / Committee
Dashboard → Members → Finance → Notices → More

### General Member
Home → My Chanda → Notices → Prayer → More

### Staff
Home → Duty → Prayer → Notices → More

### Madrasa Teacher
Madrasa → Students → Attendance → Homework → More

### Guardian
Home → Children → Study → Notices → More

### System Super Admin
Overview → Mosques → Subscriptions → Support → More

---

# 27. UI/UX Acceptance Criteria

UI/UX complete হবে যখন:

- ৭টি role-based interface profile implemented।
- role mapping verified।
- primary bottom navigation সর্বোচ্চ ৫টি।
- প্রতিটি module-এর information architecture defined।
- Dashboard role-specific।
- Member/Finance/Staff/Madrasa/Guardian navigation complete।
- permission-aware UI implemented।
- Material Design 3 ব্যবহার নেই।
- custom Mosque Design System applied।
- mobile-first layout verified।
- loading/empty/error/offline states আছে।
- destructive actions confirmation-based।
- financial actions review/confirmation flow অনুসরণ করে।
- accessibility labels আছে।
- notification deep links permission-aware।
- state restoration verified।

---

# 28. Final Product Rule

**একটি application, একাধিক role-based experience।**

Login-এর পরে backend resolve করবে:
- mosque
- roles
- permissions
- subscription entitlements
- relationship permissions

তারপর Android client সেই অনুযায়ী:
- dashboard
- bottom navigation
- quick actions
- module access
- screen actions
- data visibility

compose করবে।

**Security decision সবসময় backend-এর। UI হলো authorization-এর presentation layer।**
