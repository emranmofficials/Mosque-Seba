import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/router/app_router.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../shared/widgets/app_button.dart';
import '../../../shared/widgets/app_text_field.dart';
import '../../../shared/widgets/card_shell.dart';
import '../providers/auth_provider.dart';
import '../widgets/auth_header_banner.dart';
import '../widgets/registration_success_card.dart';

/// 3-Step Mosque & President Registration Wizard (RegistrationScreen.kt Parity)
class RegistrationScreen extends ConsumerStatefulWidget {
  const RegistrationScreen({super.key});

  @override
  ConsumerState<RegistrationScreen> createState() => _RegistrationScreenState();
}

class _RegistrationScreenState extends ConsumerState<RegistrationScreen> {
  int _currentStep = 1; // 1: President, 2: Mosque, 3: Success
  bool _isLoading = false;
  String? _errorMessage;
  String _generatedCommitteeId = '';

  // Step 1: President Details
  final _presNameBnController = TextEditingController();
  final _presNameEnController = TextEditingController();
  final _presMobileController = TextEditingController();
  final _presEmailController = TextEditingController();
  final _passwordController = TextEditingController();
  final _confirmPasswordController = TextEditingController();

  // Step 2: Mosque Details
  final _mosqueNameBnController = TextEditingController();
  final _mosqueNameEnController = TextEditingController();
  final _districtController = TextEditingController();
  final _upazilaController = TextEditingController();
  final _establishedYearController = TextEditingController(text: '1985');
  final _committeeStartYearController = TextEditingController(text: '2026');
  final _committeeEndYearController = TextEditingController(text: '2028');
  final _bankInfoController = TextEditingController();

  @override
  void dispose() {
    _presNameBnController.dispose();
    _presNameEnController.dispose();
    _presMobileController.dispose();
    _presEmailController.dispose();
    _passwordController.dispose();
    _confirmPasswordController.dispose();
    _mosqueNameBnController.dispose();
    _mosqueNameEnController.dispose();
    _districtController.dispose();
    _upazilaController.dispose();
    _establishedYearController.dispose();
    _committeeStartYearController.dispose();
    _committeeEndYearController.dispose();
    _bankInfoController.dispose();
    super.dispose();
  }

  void _validateStep1() {
    if (_presNameBnController.text.trim().isEmpty) {
      setState(() => _errorMessage = 'সভাপতির নাম বাংলায় লিখুন।');
      return;
    }
    if (_presMobileController.text.trim().length < 11) {
      setState(() => _errorMessage = 'সঠিক ১১ ডিজিটের মোবাইল নম্বর দিন।');
      return;
    }
    if (!_presEmailController.text.contains('@')) {
      setState(() => _errorMessage = 'সঠিক ইমেইল ঠিকানা দিন।');
      return;
    }
    if (_passwordController.text.length < 6) {
      setState(() => _errorMessage = 'পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে।');
      return;
    }
    if (_passwordController.text != _confirmPasswordController.text) {
      setState(() => _errorMessage = 'পাসওয়ার্ড দুটি মিলছে না।');
      return;
    }

    setState(() {
      _errorMessage = null;
      _currentStep = 2;
    });
  }

  Future<void> _submitRegistration() async {
    if (_mosqueNameBnController.text.trim().isEmpty) {
      setState(() => _errorMessage = 'মসজিদের নাম বাংলায় লিখুন।');
      return;
    }
    if (_districtController.text.trim().isEmpty) {
      setState(() => _errorMessage = 'জেলার নাম উল্লেখ করুন।');
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final authService = ref.read(authServiceProvider);
      final committeeId = await authService.registerPresidentAndMosque(
        presidentNameBn: _presNameBnController.text.trim(),
        presidentNameEn: _presNameEnController.text.trim(),
        presidentMobile: _presMobileController.text.trim(),
        presidentEmail: _presEmailController.text.trim(),
        password: _passwordController.text,
        mosqueNameBn: _mosqueNameBnController.text.trim(),
        mosqueNameEn: _mosqueNameEnController.text.trim(),
        district: _districtController.text.trim(),
        upazila: _upazilaController.text.trim(),
        establishedYear: int.tryParse(_establishedYearController.text) ?? 1985,
        committeeStartYear: int.tryParse(_committeeStartYearController.text) ?? 2026,
        committeeEndYear: int.tryParse(_committeeEndYearController.text) ?? 2028,
        bankAccountInfo: _bankInfoController.text.trim(),
      );
      await ref.read(authNotifierProvider.notifier).checkSession();

      if (mounted) {
        setState(() {
          _generatedCommitteeId = committeeId;
          _currentStep = 3;
        });
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
    return Scaffold(
      body: SingleChildScrollView(
        child: Column(
          children: [
            AuthHeaderBanner(
              title: _currentStep == 3 ? 'নিবন্ধন সফল হয়েছে!' : 'নতুন মসজিদ নিবন্ধন',
              subtitle: 'পদক্ষেপ $_currentStep / ৩: ${_getStepTitle()}',
            ),

            Padding(
              padding: const EdgeInsets.all(MosqueContentPadding.screenHorizontal),
              child: Column(
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

                  if (_currentStep == 1) _buildStep1President(),
                  if (_currentStep == 2) _buildStep2Mosque(),
                  if (_currentStep == 3) _buildStep3Success(),

                  const SizedBox(height: MosqueSpace.xl),

                  if (_currentStep < 3)
                    TextButton(
                      onPressed: () => context.go(AppRouter.login),
                      child: const Text('পূর্বে নিবন্ধিত থাকলে লগইন করুন'),
                    ),
                  const SizedBox(height: MosqueSpace.xl),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  String _getStepTitle() {
    switch (_currentStep) {
      case 1:
        return 'সভাপতি ও প্রশাসকের তথ্য';
      case 2:
        return 'মসজিদের বিস্তারিত পরিচিতি';
      case 3:
        return 'কমিটি কোড নিশ্চিতকরণ';
      default:
        return '';
    }
  }

  Widget _buildStep1President() {
    return CardShell(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          AppTextField(
            label: 'সভাপতির পূর্ণ নাম (বাংলায়)',
            hint: 'যেমন: হাজী মো: রফিকুল ইসলাম',
            controller: _presNameBnController,
          ),
          const SizedBox(height: MosqueSpace.md),
          AppTextField(
            label: 'Full Name (English)',
            hint: 'e.g. Md. Rafiqul Islam',
            controller: _presNameEnController,
          ),
          const SizedBox(height: MosqueSpace.md),
          AppTextField(
            label: 'মোবাইল নম্বর',
            hint: '০১৭১১-XXXXXX',
            controller: _presMobileController,
            keyboardType: TextInputType.phone,
          ),
          const SizedBox(height: MosqueSpace.md),
          AppTextField(
            label: 'ইমেইল ঠিকানা (লগইনের জন্য)',
            hint: 'president@example.com',
            controller: _presEmailController,
            keyboardType: TextInputType.emailAddress,
          ),
          const SizedBox(height: MosqueSpace.md),
          AppTextField(
            label: 'পাসওয়ার্ড',
            hint: 'কমপক্ষে ৬ অক্ষর',
            controller: _passwordController,
            obscureText: true,
          ),
          const SizedBox(height: MosqueSpace.md),
          AppTextField(
            label: 'পাসওয়ার্ড পুনরায় লিখুন',
            hint: '••••••••',
            controller: _confirmPasswordController,
            obscureText: true,
          ),
          const SizedBox(height: MosqueSpace.lg),
          AppButton(
            label: 'পরবর্তী ধাপ (মসজিদের তথ্য)',
            icon: Icons.arrow_forward,
            onPressed: _validateStep1,
          ),
        ],
      ),
    );
  }

  Widget _buildStep2Mosque() {
    return CardShell(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          AppTextField(
            label: 'মসজিদের নাম (বাংলায়)',
            hint: 'যেমন: বায়তুল মামুর জামে মসজিদ',
            controller: _mosqueNameBnController,
          ),
          const SizedBox(height: MosqueSpace.md),
          AppTextField(
            label: 'Mosque Name (English)',
            hint: 'e.g. Baitul Mamur Jame Mosque',
            controller: _mosqueNameEnController,
          ),
          const SizedBox(height: MosqueSpace.md),
          Row(
            children: [
              Expanded(
                child: AppTextField(
                  label: 'জেলা',
                  hint: 'ঢাকা',
                  controller: _districtController,
                ),
              ),
              const SizedBox(width: MosqueSpace.md),
              Expanded(
                child: AppTextField(
                  label: 'উপজেলা / থানা',
                  hint: 'মিরপুর',
                  controller: _upazilaController,
                ),
              ),
            ],
          ),
          const SizedBox(height: MosqueSpace.md),
          Row(
            children: [
              Expanded(
                child: AppTextField(
                  label: 'প্রতিষ্ঠার সাল',
                  hint: '1985',
                  controller: _establishedYearController,
                  keyboardType: TextInputType.number,
                ),
              ),
              const SizedBox(width: MosqueSpace.md),
              Expanded(
                child: AppTextField(
                  label: 'কমিটির শুরু',
                  hint: '2026',
                  controller: _committeeStartYearController,
                  keyboardType: TextInputType.number,
                ),
              ),
              const SizedBox(width: MosqueSpace.md),
              Expanded(
                child: AppTextField(
                  label: 'কমিটির শেষ',
                  hint: '2028',
                  controller: _committeeEndYearController,
                  keyboardType: TextInputType.number,
                ),
              ),
            ],
          ),
          const SizedBox(height: MosqueSpace.md),
          AppTextField(
            label: 'ব্যাংক হিসাব নম্বর ও বিস্তারিত',
            hint: 'হিসাব নাম, নম্বর ও ব্যাংক শাখা',
            controller: _bankInfoController,
          ),
          const SizedBox(height: MosqueSpace.lg),
          Row(
            children: [
              Expanded(
                child: AppButton(
                  label: 'পূর্ববর্তী ধাপ',
                  variant: AppButtonVariant.outline,
                  onPressed: () => setState(() => _currentStep = 1),
                ),
              ),
              const SizedBox(width: MosqueSpace.md),
              Expanded(
                child: AppButton(
                  label: 'নিবন্ধন সম্পন্ন করুন',
                  isLoading: _isLoading,
                  onPressed: _submitRegistration,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildStep3Success() {
    return RegistrationSuccessCard(
      committeeId: _generatedCommitteeId,
      onEnterDashboard: () => context.go(AppRouter.dashboard),
    );
  }
}
