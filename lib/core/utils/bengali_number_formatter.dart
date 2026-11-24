/// Utility for formatting numerals, currency, and date strings into Bengali script
class BengaliNumberFormatter {
  static const Map<String, String> _enToBnDigits = {
    '0': '০',
    '1': '১',
    '2': '২',
    '3': '৩',
    '4': '৪',
    '5': '৫',
    '6': '৬',
    '7': '৭',
    '8': '৮',
    '9': '৯',
  };

  static const Map<String, String> _bnToEnDigits = {
    '০': '0',
    '১': '1',
    '২': '2',
    '৩': '3',
    '৪': '4',
    '৫': '5',
    '৬': '6',
    '৭': '7',
    '৮': '8',
    '৯': '9',
  };

  /// Converts English digit characters in any string to Bengali digits
  static String toBengaliDigits(dynamic input) {
    if (input == null) return '';
    final str = input.toString();
    final buffer = StringBuffer();
    for (int i = 0; i < str.length; i++) {
      final char = str[i];
      buffer.write(_enToBnDigits[char] ?? char);
    }
    return buffer.toString();
  }

  /// Converts Bengali digit characters to English digits (for parsing numeric inputs)
  static String toEnglishDigits(dynamic input) {
    if (input == null) return '';
    final str = input.toString();
    final buffer = StringBuffer();
    for (int i = 0; i < str.length; i++) {
      final char = str[i];
      buffer.write(_bnToEnDigits[char] ?? char);
    }
    return buffer.toString();
  }

  /// Formats currency with Taka symbol: e.g. ৳১,৫০০.০০
  static String formatCurrency(num amount, {bool includeDecimals = false}) {
    final formatted = includeDecimals
        ? amount.toStringAsFixed(2)
        : amount.round().toString();
    return '৳${toBengaliDigits(formatted)}';
  }

  /// Parses numeric value from either English or Bengali text
  static double parseDouble(String text) {
    final clean = toEnglishDigits(text).replaceAll(',', '').trim();
    return double.tryParse(clean) ?? 0.0;
  }
}
