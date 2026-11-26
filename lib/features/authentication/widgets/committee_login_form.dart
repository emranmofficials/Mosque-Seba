import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../bootstrap/dependency_injection.dart';
import '../../../core/router/app_router.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/utils/app_toast.dart';
import '../../../shared/widgets/app_button.dart';
import '../../../shared/widgets/app_text_field.dart';
import '../providers/auth_provider.dart';

class CommitteeLoginForm extends ConsumerStatefulWidget {
  const CommitteeLoginForm({super.key});

  @override
  ConsumerState<CommitteeLoginForm> createState() => _CommitteeLoginFormState();
}

class _CommitteeLoginFormState extends ConsumerState<CommitteeLoginForm> {
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  bool _obscurePassword = true;
  bool _rememberMe = true;
  bool _isLoading = false;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    final remembered = ref.read(sessionStorageServiceProvider).rememberedEmail;
    if (remembered.isNotEmpty) {
      _emailController.text = remembered;
    }
  }

  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _handleLogin() async {
    final email = _emailController.text.trim();
    final password = _passwordController.text;

    if (email.isEmpty || !email.contains('@')) {
      setState(() => _errorMessage = 'সঠিক ইমেইল ঠিকানা প্রদান করুন।');
      return;
    }
    if (password.length < 6) {
      setState(() => _errorMessage = 'পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে।');
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      await ref.read(authNotifierProvider.notifier).loginWithEmail(email, password);
      await ref
          .read(sessionStorageServiceProvider)
          .setRememberedEmail(_rememberMe ? email : null);
      if (mounted) {
        AppToast.success(context, 'লগইন সফল হয়েছে');
        context.go(AppRouter.dashboard);
      }
    } catch (e) {
      if (mounted) {
        final err = e.toString().replaceFirst('Exception: ', '');
        setState(() => _errorMessage = err);
        AppToast.error(context, err);
      }
    } finally {
      if (mounted) {
        setState(() => _isLoading = false);
      }
    }
  }

  Future<void> _showForgotPasswordDialog() async {
    final resetEmailCtrl = TextEditingController(text: _emailController.text.trim());
    bool sending = false;
    String? dialogError;

    await showDialog<void>(
      context: context,
      builder: (dialogCtx) => StatefulBuilder(
        builder: (dialogCtx, setDialogState) => AlertDialog(
          title: const Text('পাসওয়ার্ড রিসেট'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'নিবন্ধিত ইমেইল দিন। পাসওয়ার্ড রিসেট করার লিংক পাঠানো হবে।',
                style: TextStyle(fontSize: 13),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: resetEmailCtrl,
                keyboardType: TextInputType.emailAddress,
                decoration: const InputDecoration(
                  labelText: 'ইমেইল ঠিকানা',
                  hintText: 'president@mosque.org',
                  border: OutlineInputBorder(),
                ),
              ),
              if (dialogError != null) ...[
                const SizedBox(height: 8),
                Text(
                  dialogError!,
                  style: const TextStyle(color: AppColors.urgentRed, fontSize: 12),
                ),
              ],
            ],
          ),
          actions: [
            TextButton(
              onPressed: sending ? null : () => Navigator.pop(dialogCtx),
              child: const Text('বাতিল'),
            ),
            FilledButton(
              onPressed: sending
                  ? null
                  : () async {
                      final email = resetEmailCtrl.text.trim();
                      if (email.isEmpty || !email.contains('@')) {
                        setDialogState(
                          () => dialogError = 'সঠিক ইমেইল ঠিকানা প্রদান করুন।',
                        );
                        return;
                      }
                      setDialogState(() {
                        sending = true;
                        dialogError = null;
                      });
                      try {
                        await ref.read(authServiceProvider).sendPasswordResetEmail(email);
                        if (!dialogCtx.mounted) return;
                        Navigator.pop(dialogCtx);
                        if (!mounted) return;
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(
                            content: Text('পাসওয়ার্ড রিসেট লিংক ইমেইলে পাঠানো হয়েছে।'),
                            backgroundColor: AppColors.emeraldPrimary,
                          ),
                        );
                      } catch (e) {
                        setDialogState(() {
                          sending = false;
                          dialogError = e.toString().replaceFirst('Exception: ', '');
                        });
                      }
                    },
              child: sending
                  ? const SizedBox(
                      width: 16,
                      height: 16,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                  : const Text('লিংক পাঠান'),
            ),
          ],
        ),
      ),
    );

    resetEmailCtrl.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        if (_errorMessage != null) ...[
          Container(
            padding: const EdgeInsets.all(MosqueSpace.md),
            decoration: BoxDecoration(
              color: AppColors.urgentRed.withOpacity(0.1),
              borderRadius: BorderRadius.circular(MosqueRadius.medium),
              border: Border.all(color: AppColors.urgentRed.withOpacity(0.4)),
            ),
            child: Row(
              children: [
                const Icon(Icons.error_outline, color: AppColors.urgentRed, size: 18),
                const SizedBox(width: MosqueSpace.sm),
                Expanded(
                  child: Text(
                    _errorMessage!,
                    style: const TextStyle(color: AppColors.urgentRed, fontSize: 12),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: MosqueSpace.md),
        ],

        AppTextField(
          label: 'সভাপতি বা প্রশাসনিক ইমেইল',
          hint: 'উদাহরণ: president@mosque.org',
          controller: _emailController,
          keyboardType: TextInputType.emailAddress,
          prefixIcon: const Icon(Icons.email_outlined, color: AppColors.emeraldPrimary, size: 20),
        ),
        const SizedBox(height: MosqueSpace.md),

        AppTextField(
          label: 'গোপন পাসওয়ার্ড',
          hint: '••••••••',
          controller: _passwordController,
          obscureText: _obscurePassword,
          prefixIcon: const Icon(Icons.lock_outline, color: AppColors.emeraldPrimary, size: 20),
          suffixIcon: IconButton(
            icon: Icon(
              _obscurePassword ? Icons.visibility_off_outlined : Icons.visibility_outlined,
              size: 20,
            ),
            onPressed: () => setState(() => _obscurePassword = !_obscurePassword),
          ),
        ),
        const SizedBox(height: MosqueSpace.sm),

        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Row(
              children: [
                Checkbox(
                  value: _rememberMe,
                  activeColor: AppColors.emeraldPrimary,
                  onChanged: (val) => setState(() => _rememberMe = val ?? true),
                ),
                const Text('মনে রাখুন', style: TextStyle(fontSize: 12)),
              ],
            ),
            TextButton(
              onPressed: _showForgotPasswordDialog,
              child: const Text('পাসওয়ার্ড ভুলে গেছেন?', style: TextStyle(fontSize: 12)),
            ),
          ],
        ),
        const SizedBox(height: MosqueSpace.lg),

        AppButton(
          label: 'কমিটি পোর্টালে প্রবেশ করুন',
          isLoading: _isLoading,
          icon: Icons.login,
          onPressed: _handleLogin,
        ),
      ],
    );
  }
}
