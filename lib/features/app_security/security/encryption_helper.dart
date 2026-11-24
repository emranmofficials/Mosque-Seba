import 'dart:convert';
import 'package:crypto/crypto.dart';

/// Cryptographic and Encryption Helper for Mosque Seba
class EncryptionHelper {
  /// Generate SHA-256 Hash of string (used for offline PIN and receipt verification codes)
  static String hashSha256(String input, {String salt = 'mosque_seba_salt_2026'}) {
    final bytes = utf8.encode('$salt:$input');
    final digest = sha256.convert(bytes);
    return digest.toString();
  }

  /// Generate HMAC verification signature for receipts
  static String generateReceiptSignature({
    required String receiptNo,
    required double amount,
    required String memberCode,
    String secretKey = 'mosque_treasury_hmac_secret',
  }) {
    final keyBytes = utf8.encode(secretKey);
    final messageBytes = utf8.encode('$receiptNo|$amount|$memberCode');
    final hmacSha256 = Hmac(sha256, keyBytes);
    final digest = hmacSha256.convert(messageBytes);
    return digest.toString().substring(0, 16).toUpperCase();
  }

  /// Verify whether a QR verification code is valid
  static bool verifyReceiptCode({
    required String receiptNo,
    required double amount,
    required String memberCode,
    required String signature,
  }) {
    final expected = generateReceiptSignature(
      receiptNo: receiptNo,
      amount: amount,
      memberCode: memberCode,
    );
    return expected == signature;
  }
}
