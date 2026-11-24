import 'dart:io';

class DeviceSecurityAssessment {
  final bool isRootedOrCompromised;
  final bool isEmulator;
  final bool isDevelopmentBuild;
  final int trustScore; // 0 to 100
  final List<String> securityAlerts;

  const DeviceSecurityAssessment({
    required this.isRootedOrCompromised,
    required this.isEmulator,
    required this.isDevelopmentBuild,
    required this.trustScore,
    required this.securityAlerts,
  });
}

/// Device Security Heuristics Service (MosqueSecurityManager Parity)
class SecurityCheckService {
  /// Check whether the Android/iOS device appears rooted, emulated, or tampered
  Future<DeviceSecurityAssessment> runSecurityScan() async {
    final alerts = <String>[];
    bool isRooted = false;

    // Common root binary heuristics check on Android
    if (Platform.isAndroid) {
      const rootPaths = [
        '/system/app/Superuser.apk',
        '/sbin/su',
        '/system/bin/su',
        '/system/xbin/su',
        '/data/local/xbin/su',
        '/data/local/bin/su',
        '/system/sd/xbin/su',
        '/system/bin/failsafe/su',
        '/data/local/su',
      ];

      for (final path in rootPaths) {
        if (File(path).existsSync()) {
          isRooted = true;
          alerts.add('সুপার-ইউজার (Root Binary) শনাক্ত হয়েছে: $path');
          break;
        }
      }
    }

    final isEmulator = Platform.environment.containsKey('ANDROID_EMULATOR') ||
        Platform.isLinux && !Platform.isAndroid;

    int score = 100;
    if (isRooted) score -= 40;
    if (isEmulator) score -= 10;

    return DeviceSecurityAssessment(
      isRootedOrCompromised: isRooted,
      isEmulator: isEmulator,
      isDevelopmentBuild: false,
      trustScore: score,
      securityAlerts: alerts,
    );
  }
}
