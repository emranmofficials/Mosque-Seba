import 'package:flutter/material.dart';
import '../../../core/services/biometric_auth_service.dart';
import '../../../core/theme/app_colors.dart';

/// Wraps the application root to enforce Biometric / Committee PIN App Lock on launch.
class AppLockWrapper extends StatefulWidget {
  final Widget child;

  const AppLockWrapper({super.key, required this.child});

  @override
  State<AppLockWrapper> createState() => _AppLockWrapperState();
}

class _AppLockWrapperState extends State<AppLockWrapper> {
  final _auth = BiometricAuthService.instance;
  final _pinController = TextEditingController();

  bool _checking = true;
  bool _locked = false;
  bool _biometricEnabled = false;
  bool _pinEnabled = false;
  String? _errorText;

  @override
  void initState() {
    super.initState();
    _checkLock();
  }

  @override
  void dispose() {
    _pinController.dispose();
    super.dispose();
  }

  Future<void> _checkLock() async {
    final bio = await _auth.isBiometricLockEnabled();
    final pin = await _auth.isPinLockEnabled();
    if (!mounted) return;
    if (!bio && !pin) {
      setState(() {
        _checking = false;
        _locked = false;
      });
      return;
    }

    setState(() {
      _checking = false;
      _locked = true;
      _biometricEnabled = bio;
      _pinEnabled = pin;
    });

    if (bio) {
      await _tryBiometricUnlock();
    }
  }

  Future<void> _tryBiometricUnlock() async {
    final ok = await _auth.authenticateWithBiometrics();
    if (!mounted) return;
    if (ok) {
      setState(() {
        _locked = false;
        _errorText = null;
      });
    }
  }

  Future<void> _tryPinUnlock() async {
    final pin = _pinController.text.trim();
    if (pin.isEmpty) return;
    final ok = await _auth.verifyPin(pin);
    if (!mounted) return;
    if (ok) {
      _pinController.clear();
      setState(() {
        _locked = false;
        _errorText = null;
      });
    } else {
      setState(() => _errorText = 'ভুল পিন! অনুগ্রহ করে আবার চেষ্টা করুন।');
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_checking || !_locked) {
      return widget.child;
    }

    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        backgroundColor: AppColors.emeraldDark,
        body: Center(
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 360),
            child: Card(
              margin: const EdgeInsets.all(24),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(18),
              ),
              child: Padding(
                padding: const EdgeInsets.all(24),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Icon(
                      Icons.lock_outline,
                      size: 48,
                      color: AppColors.emeraldPrimary,
                    ),
                    const SizedBox(height: 12),
                    const Text(
                      'মসজিদ সেবা নিরাপত্তা লক',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 4),
                    const Text(
                      'অ্যাপ ব্যবহার করতে যাচাই সম্পন্ন করুন',
                      style: TextStyle(fontSize: 12, color: Colors.grey),
                    ),
                    const SizedBox(height: 18),
                    if (_pinEnabled) ...[
                      TextField(
                        controller: _pinController,
                        obscureText: true,
                        keyboardType: TextInputType.number,
                        maxLength: 6,
                        decoration: const InputDecoration(
                          labelText: 'কমিটি নিরাপত্তা পিন',
                          border: OutlineInputBorder(),
                          counterText: '',
                        ),
                        onSubmitted: (_) => _tryPinUnlock(),
                      ),
                      const SizedBox(height: 12),
                      SizedBox(
                        width: double.infinity,
                        child: ElevatedButton(
                          onPressed: _tryPinUnlock,
                          style: ElevatedButton.styleFrom(
                            backgroundColor: AppColors.emeraldPrimary,
                            foregroundColor: Colors.white,
                          ),
                          child: const Text('পিন দিয়ে আনলক করুন'),
                        ),
                      ),
                    ],
                    if (_biometricEnabled) ...[
                      const SizedBox(height: 10),
                      SizedBox(
                        width: double.infinity,
                        child: OutlinedButton.icon(
                          onPressed: _tryBiometricUnlock,
                          icon: const Icon(Icons.fingerprint),
                          label: const Text('ফিঙ্গারপ্রিন্ট / ফেস আনলক'),
                        ),
                      ),
                    ],
                    if (_errorText != null) ...[
                      const SizedBox(height: 10),
                      Text(
                        _errorText!,
                        style: const TextStyle(color: Colors.red, fontSize: 12),
                      ),
                    ],
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
