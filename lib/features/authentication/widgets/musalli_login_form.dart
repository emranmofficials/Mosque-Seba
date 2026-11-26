import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/router/app_router.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../shared/widgets/app_button.dart';
import '../../../shared/widgets/app_text_field.dart';
import '../providers/auth_provider.dart';

class MusalliLoginForm extends ConsumerStatefulWidget {
  const MusalliLoginForm({super.key});

  @override
  ConsumerState<MusalliLoginForm> createState() => _MusalliLoginFormState();
}

class _MusalliLoginFormState extends ConsumerState<MusalliLoginForm> {
  final _committeeIdController = TextEditingController();
  final _mobileController = TextEditingController();
  final _passwordController = TextEditingController();
  bool _obscurePassword = true;
  bool _isLoading = false;
  String? _errorMessage;

  @override
  void dispose() {
    _committeeIdController.dispose();
    _mobileController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _handleLogin() async {
    final committeeId = _committeeIdController.text.trim();
    final mobile = _mobileController.text.trim();
    final password = _passwordController.text;

    if (committeeId.isEmpty) {
      setState(() => _errorMessage = 'মসজিদের ১০ ডিজিটের কমিটি কোড দিন।');
      return;
    }
    if (mobile.length < 11) {
      setState(() => _errorMessage = '১১ ডিজিটের সঠিক মোবাইল নম্বর দিন।');
      return;
    }
    if (password.isEmpty) {
      setState(() => _errorMessage = 'পাসওয়ার্ড প্রদান করুন।');
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      await ref.read(authNotifierProvider.notifier).loginWithMobile(
            committeeId,
            mobile,
            password,
          );
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
          label: 'মসজিদ কোড / কমিটি আইডি',
          hint: '১০ ডিজিটের কোড (যেমন: ২৬০৮৪৫১৯০৩)',
          controller: _committeeIdController,
          prefixIcon: const Icon(Icons.tag, color: AppColors.goldDeep, size: 20),
        ),
        const SizedBox(height: MosqueSpace.md),

        AppTextField(
          label: 'নিবন্ধিত মোবাইল নম্বর',
          hint: '০১৭১১-XXXXXX',
          controller: _mobileController,
          keyboardType: TextInputType.phone,
          prefixIcon: const Icon(Icons.phone_outlined, color: AppColors.emeraldPrimary, size: 20),
        ),
        const SizedBox(height: MosqueSpace.md),

        AppTextField(
          label: 'পাসওয়ার্ড',
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
        const SizedBox(height: MosqueSpace.lg),

        AppButton(
          label: 'মুসল্লি পোর্টালে প্রবেশ করুন',
          variant: AppButtonVariant.primary,
          isLoading: _isLoading,
          icon: Icons.login,
          onPressed: _handleLogin,
        ),
      ],
    );
  }
}
