# MOSQUE SEBA (মসজিদ সেবা) — KNOWN ISSUES & COMPATIBILITY LOG

**Project:** Mosque Seba (`com.mosqueseba.app`)  
**Tracking Period:** Pre-Development & Migration Lifecycle  

---

## 1. Identified Legacy Defects & Architecture Gaps

| ID | Component | Issue Description | Root Cause in Legacy Code | Rebuild Solution in Flutter |
|---|---|---|---|---|
| **ISSUE-01** | `FinanceScreen.kt` | "ডাটা এন্ট্রি হচ্ছে না" (Transactions disappearing after sync). | Supabase stored `income`/`expense` in lowercase, while Room DAO query filtered by uppercase `INCOME`/`EXPENSE`. | Type-safe Dart enum `TransactionType` with JSON converter normalizing on serialisation. |
| **ISSUE-02** | `Dialogs.kt` | Extreme UI thread lag when opening Add Member or Receive Chanda dialogs. | 4,098 lines in a single file with recomposition storms and inline canvas re-draws. | Extracted into small independent dialog widgets with isolated Riverpod state. |
| **ISSUE-03** | `SupabaseSyncManager.kt` | Date parsing crashes on non-standard Bengali dates (e.g., `১৭ নভেম্বর ২০২৬`). | Hardcoded locale string parsing without ISO 8601 fallback. | Robust `bengali_number_formatter.dart` and strict ISO 8601 `YYYY-MM-DD` storage format. |
| **ISSUE-04** | `MainActivity.kt` | Tablet layout breaking in landscape mode on smaller tablets. | Hardcoded 600dp breakpoint without flexible layout constraints. | `AdaptiveScaffold` utilizing `LayoutBuilder` and Material 3 `NavigationRail`. |
| **ISSUE-05** | `MosqueViewModel.kt` | High memory footprint and battery drain during background sync. | Monolithic ViewModel holding all flows open indefinitely (`SharingStarted.Eagerly`). | Riverpod `autoDispose` providers releasing resources when screens unmount. |
