# WCAG কনট্রাস্ট চেক — bKash-inspired theme

লাইট ও ডার্ক থিমের প্রধান foreground/background জোড়ার কনট্রাস্ট রেশিও (WCAG 2.1)।

| জোড়া | রেশিও | ন্যূনতম | ফলাফল |
|---|---:|---:|---|
| সাদা টেক্সট / `#E2136E` বাটন | 4.61:1 | 4.5 | PASS |
| `#273339` / `#F7F7F7` বডি | 12.11:1 | 4.5 | PASS |
| `#505050` / `#FFFFFF` সেকেন্ডারি | 8.06:1 | 4.5 | PASS |
| `#E2136E` / `#FDEFF4` আইকন | 4.14:1 | 3.0 | PASS |
| `#8A8A8A` / `#FFFFFF` মিউটেড | 3.45:1 | 3.0 | PASS (পরিবর্তন: `#9A9A9A` → `#8A8A8A`) |
| `#FFF1F6` / `#150912` ডার্ক বডি | 17.75:1 | 4.5 | PASS |
| `#FBCFE8` / `#24101C` ডার্ক সেকেন্ডারি | 13.03:1 | 4.5 | PASS |
| `#F472B6` / `#24101C` ডার্ক অ্যাকসেন্ট | 6.80:1 | 3.0 | PASS |
| `#2A0A1A` / `#F472B6` ডার্ক বাটন টেক্সট | 6.85:1 | 4.5 | PASS (নতুন টোকেন `MosqueNightOnPrimary`) |
| `#C4578F` / `#24101C` ডার্ক ইনপুট বর্ডার | 4.38:1 | 3.0 | PASS (পরিবর্তন: `#9D3469` → `#C4578F`) |
| `#FFF1F6` / `#D01060` ডার্ক primaryContainer | 4.88:1 | 4.5 | PASS (নতুন টোকেন `MosqueNightPrimaryContainer`) |
| `#FCE7F3` / `#4A1233` ডার্ক secondaryContainer | 12.53:1 | 4.5 | PASS |

## পরিবর্তিত টোকেন
- `MosqueTextMuted`: `#9A9A9A` → `#8A8A8A`
- `MosqueNightInputBorder`: `#9D3469` → `#C4578F`
- নতুন: `MosqueNightOnPrimary = #2A0A1A`
- নতুন: `MosqueNightPrimaryContainer = #D01060`

`Theme.kt`-র ডার্ক `colorScheme`-এ `onPrimary`, `onSecondary` এবং `primaryContainer` এই নতুন টোকেনগুলো ব্যবহার করে।
