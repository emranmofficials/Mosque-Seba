import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../../../core/router/app_router.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../shared/widgets/card_shell.dart';
import '../dialogs/login_help_dialog.dart';
import '../widgets/auth_header_banner.dart';
import '../widgets/committee_login_form.dart';
import '../widgets/musalli_login_form.dart';

/// Centralized Dual-Tab Login Screen (LoginScreen.kt Parity)
class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  int _selectedTabIndex = 0; // 0: Committee, 1: Musalli

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Scaffold(
      body: SingleChildScrollView(
        child: Column(
          children: [
            // Top Sanctuary Header Banner with Help Action
            Stack(
              children: [
                const AuthHeaderBanner(
                  title: 'মসজিদ সেবা পোর্টালে স্বাগতম',
                  subtitle: 'পরিচালনায় স্বচ্ছ আস্থা ও আধুনিক ডিজিটালাইজেশন',
                ),
                Positioned(
                  top: 48,
                  right: 16,
                  child: IconButton(
                    icon: const Icon(Icons.help_outline, color: AppColors.goldLight),
                    onPressed: () {
                      showDialog(
                        context: context,
                        builder: (_) => const LoginHelpDialog(),
                      );
                    },
                  ),
                ),
              ],
            ),

            Padding(
              padding: const EdgeInsets.all(MosqueContentPadding.screenHorizontal),
              child: Column(
                children: [
                  // Tab Switcher (কমিটি vs মুসল্লি)
                  Container(
                    padding: const EdgeInsets.all(4),
                    decoration: BoxDecoration(
                      color: isDark ? AppColors.nightSurfaceVariant : AppColors.cardBorderSubtle,
                      borderRadius: BorderRadius.circular(MosqueRadius.pill),
                    ),
                    child: Row(
                      children: [
                        Expanded(
                          child: GestureDetector(
                            onTap: () => setState(() => _selectedTabIndex = 0),
                            child: Container(
                              padding: const EdgeInsets.symmetric(vertical: 10),
                              decoration: BoxDecoration(
                                color: _selectedTabIndex == 0
                                    ? AppColors.emeraldPrimary
                                    : Colors.transparent,
                                borderRadius: BorderRadius.circular(MosqueRadius.pill),
                              ),
                              child: Text(
                                'সভাপতি ও কমিটি',
                                textAlign: TextAlign.center,
                                style: TextStyle(
                                  fontSize: 13,
                                  fontWeight: FontWeight.bold,
                                  color: _selectedTabIndex == 0
                                      ? Colors.white
                                      : (isDark ? AppColors.nightTextMuted : AppColors.textSecondary),
                                ),
                              ),
                            ),
                          ),
                        ),
                        Expanded(
                          child: GestureDetector(
                            onTap: () => setState(() => _selectedTabIndex = 1),
                            child: Container(
                              padding: const EdgeInsets.symmetric(vertical: 10),
                              decoration: BoxDecoration(
                                color: _selectedTabIndex == 1
                                    ? AppColors.emeraldPrimary
                                    : Colors.transparent,
                                borderRadius: BorderRadius.circular(MosqueRadius.pill),
                              ),
                              child: Text(
                                'সাধারণ মুসল্লি',
                                textAlign: TextAlign.center,
                                style: TextStyle(
                                  fontSize: 13,
                                  fontWeight: FontWeight.bold,
                                  color: _selectedTabIndex == 1
                                      ? Colors.white
                                      : (isDark ? AppColors.nightTextMuted : AppColors.textSecondary),
                                ),
                              ),
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),

                  const SizedBox(height: MosqueSpace.lg),

                  // Form Container
                  CardShell(
                    child: AnimatedCrossFade(
                      firstChild: const CommitteeLoginForm(),
                      secondChild: const MusalliLoginForm(),
                      crossFadeState: _selectedTabIndex == 0
                          ? CrossFadeState.showFirst
                          : CrossFadeState.showSecond,
                      duration: const Duration(milliseconds: 250),
                    ),
                  ),

                  const SizedBox(height: MosqueSpace.xl),

                  // Link to Mosque Registration
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(
                        'আপনার মসজিদ কি এখনও নিবন্ধিত নয়? ',
                        style: TextStyle(
                          fontSize: 13,
                          color: isDark ? AppColors.nightTextMuted : AppColors.textSecondary,
                        ),
                      ),
                      GestureDetector(
                        onTap: () => context.go(AppRouter.register),
                        child: const Text(
                          'নিবন্ধন করুন',
                          style: TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.bold,
                            color: AppColors.emeraldPrimary,
                            decoration: TextDecoration.underline,
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: MosqueSpace.xxl),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
