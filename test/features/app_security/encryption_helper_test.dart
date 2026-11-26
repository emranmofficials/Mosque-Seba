import 'package:flutter_test/flutter_test.dart';
import 'package:mosqueseba/features/app_security/security/encryption_helper.dart';

void main() {
  group('EncryptionHelper', () {
    test('produces deterministic SHA-256 hashes', () {
      final h1 = EncryptionHelper.hashSha256('1234');
      final h2 = EncryptionHelper.hashSha256('1234');
      final h3 = EncryptionHelper.hashSha256('4321');

      expect(h1, equals(h2));
      expect(h1, isNot(equals(h3)));
      expect(h1.length, 64);
    });

    test('generates and verifies receipt HMAC signatures', () {
      final sig = EncryptionHelper.generateReceiptSignature(
        receiptNo: 'RCP-2026-001',
        amount: 500.0,
        memberCode: 'MEM-001',
      );

      expect(sig.length, 16);
      expect(
        EncryptionHelper.verifyReceiptCode(
          receiptNo: 'RCP-2026-001',
          amount: 500.0,
          memberCode: 'MEM-001',
          signature: sig,
        ),
        isTrue,
      );
      expect(
        EncryptionHelper.verifyReceiptCode(
          receiptNo: 'RCP-2026-001',
          amount: 999.0,
          memberCode: 'MEM-001',
          signature: sig,
        ),
        isFalse,
      );
    });
  });
}
