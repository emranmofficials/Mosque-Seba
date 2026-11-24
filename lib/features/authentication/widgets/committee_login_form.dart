import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/router/app_router.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
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
      if (mounted) {
        context.go(AppRouter.dashboard);
      }
    } catch (e) {
      if (mounted) {
        setState(() => _errorMessage = e.toString().replaceFirst('Exception: ', ''));
      }
    } finally {
      if (mounted) {
        setState(() => _isLoading = false);
      }
    }
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
              onPressed: () {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('পাসওয়ার্ড রিসেটের জন্য প্রধান সেক্রেটারির সাথে যোগাযোগ করুন।')),
                );
              },
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
