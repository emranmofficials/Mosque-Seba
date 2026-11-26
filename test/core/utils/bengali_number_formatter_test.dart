import 'package:flutter_test/flutter_test.dart';
import 'package:mosqueseba/core/utils/bengali_number_formatter.dart';

void main() {
  group('BengaliNumberFormatter', () {
    test('converts English digits to Bengali digits', () {
      expect(BengaliNumberFormatter.toBengaliDigits('2026-11-26'), '২০২৬-১১-২৬');
      expect(BengaliNumberFormatter.toBengali(1500), '১৫০০');
      expect(BengaliNumberFormatter.toBengaliDigits(null), '');
    });

    test('converts Bengali digits back to English digits', () {
      expect(BengaliNumberFormatter.toEnglishDigits('০১৭১১-১২৩৪৫৬'), '01711-123456');
      expect(BengaliNumberFormatter.toEnglishDigits(null), '');
    });

    test('formats BDT currency with Taka symbol', () {
      expect(BengaliNumberFormatter.formatCurrency(1500), '৳১৫০০');
      expect(
        BengaliNumberFormatter.formatCurrency(1500.5, includeDecimals: true),
        '৳১৫০০.৫০',
      );
    });

    test('parses double from English and Bengali numerals', () {
      expect(BengaliNumberFormatter.parseDouble('১,৫০০.৫০'), 1500.5);
      expect(BengaliNumberFormatter.parseDouble('invalid'), 0.0);
    });
  });
}
