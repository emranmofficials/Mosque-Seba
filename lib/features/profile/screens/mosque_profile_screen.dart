import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../bootstrap/dependency_injection.dart';
import '../../../core/router/app_router.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../shared/widgets/card_shell.dart';
import '../../authentication/providers/auth_provider.dart';
import '../../role_permissions/models/committee_role.dart';
import '../../role_permissions/providers/role_permissions_provider.dart';

/// Mosque & User Profile Screen with Theme Toggle and Secure Logout
class MosqueProfileScreen extends ConsumerWidget {
  const MosqueProfileScreen({super.key});

  Future<void> _confirmLogout(BuildContext context, WidgetRef ref) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('লগআউট নিশ্চিত করুন'),
        content: const Text(
          'লগআউট করলে এই ডিভাইসের লোকাল ক্যাশ সেশন মুছে যাবে এবং পুনরায় লগইন করতে হবে।',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(false),
            child: const Text('বাতিল'),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.urgentRed,
              foregroundColor: Colors.white,
            ),
            onPressed: () => Navigator.of(ctx).pop(true),
            child: const Text('লগআউট'),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      await ref.read(authNotifierProvider.notifier).logout();
      if (context.mounted) {
        context.go(AppRouter.login);
      }
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final user = ref.watch(authNotifierProvider).valueOrNull;
    final role = ref.watch(currentRoleProvider);
    final isDark = ref.watch(isDarkModeProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('প্রোফাইল ও সেটিংস'),
      ),
      body: ListView(
        padding: const EdgeInsets.all(AppDimens.spacing16),
        children: [
          CardShell(
            padding: const EdgeInsets.all(AppDimens.spacing16),
            child: Row(
              children: [
                const CircleAvatar(
                  radius: 30,
                  backgroundColor: AppColors.primaryContainer,
                  child: Icon(
                    Icons.mosque,
                    size: 32,
                    color: AppColors.emeraldPrimary,
                  ),
                ),
                const SizedBox(width: AppDimens.spacing14),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        user?.memberName.isNotEmpty == true
                            ? user!.memberName
                            : 'সম্মানিত ব্যবহারকারী',
                        style: const TextStyle(
                          fontSize: 17,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        role.titleBn,
                        style: const TextStyle(
                          fontSize: 13,
                          fontWeight: FontWeight.w600,
                          color: AppColors.emeraldPrimary,
                        ),
                      ),
                      if (user != null && user.committeeId.isNotEmpty) ...[
                        const SizedBox(height: 2),
                        Text(
                          'কমিটি কোড: ${user.committeeId}',
                          style: const TextStyle(
                            fontSize: 12,
                            color: AppColors.textSecondary,
                          ),
                        ),
                      ],
                    ],
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: AppDimens.spacing12),
          CardShell(
            padding: const EdgeInsets.all(AppDimens.spacing14),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'মসজিদ ও অ্যাকাউন্ট তথ্য',
                  style: TextStyle(fontSize: 14, fontWeight: FontWeight.bold),
                ),
                const Divider(height: 18),
                _infoRow('মসজিদের নাম', user?.mosqueNameBn.isNotEmpty == true ? user!.mosqueNameBn : 'নির্ধারিত মসজিদ'),
                _infoRow('মসজিদ আইডি', user?.mosqueId.isNotEmpty == true ? user!.mosqueId : '—'),
                if (user != null && user.email.isNotEmpty) _infoRow('ইমেইল', user.email),
                if (user != null && user.mobile.isNotEmpty) _infoRow('মোবাইল', user.mobile),
                _infoRow('দায়িত্বের সারসংক্ষেপ', role.shortSummaryBn),
              ],
            ),
          ),
          const SizedBox(height: AppDimens.spacing12),
          CardShell(
            padding: EdgeInsets.zero,
            child: Column(
              children: [
                SwitchListTile(
                  secondary: Icon(
                    isDark ? Icons.dark_mode : Icons.light_mode,
                    color: AppColors.emeraldPrimary,
                  ),
                  title: const Text('নাইট মোড (Dark Theme)'),
                  subtitle: const Text('ফজর ও এশার সময়ের জন্য আরামদায়ক ডার্ক থিম'),
                  value: isDark,
                  activeColor: AppColors.emeraldPrimary,
                  onChanged: (val) async {
                    ref.read(isDarkModeProvider.notifier).state = val;
                    await ref.read(sessionStorageServiceProvider).setDarkMode(val);
                  },
                ),
                if (role == CommitteeRole.president || role.canEditMosqueProfile()) ...[
                  const Divider(height: 1),
                  ListTile(
                    leading: const Icon(Icons.security_outlined, color: AppColors.emeraldPrimary),
                    title: const Text('অ্যাপ্লিকেশন নিরাপত্তা ও অডিট'),
                    trailing: const Icon(Icons.chevron_right),
                    onTap: () => context.push(AppRouter.appSecurity),
                  ),
                  const Divider(height: 1),
                  ListTile(
                    leading: const Icon(Icons.cloud_sync_outlined, color: AppColors.emeraldPrimary),
                    title: const Text('ক্লাউড সিঙ্ক ও অফলাইন ব্যাকআপ'),
                    trailing: const Icon(Icons.chevron_right),
                    onTap: () => context.push(AppRouter.cloudSync),
                  ),
                ],
                if (role == CommitteeRole.president ||
                    role.canManageOfficeDocs() ||
                    role == CommitteeRole.treasurer) ...[
                  const Divider(height: 1),
                  ListTile(
                    leading: const Icon(Icons.draw_outlined, color: AppColors.emeraldPrimary),
                    title: const Text('অফিসিয়াল সিল ও ডিজিটাল স্বাক্ষর'),
                    trailing: const Icon(Icons.chevron_right),
                    onTap: () => context.push(AppRouter.sealSignature),
                  ),
                ],
              ],
            ),
          ),
          const SizedBox(height: AppDimens.spacing20),
          SizedBox(
            width: double.infinity,
            child: OutlinedButton.icon(
              style: OutlinedButton.styleFrom(
                foregroundColor: AppColors.urgentRed,
                side: const BorderSide(color: AppColors.urgentRed),
                padding: const EdgeInsets.symmetric(vertical: 14),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(AppDimens.radiusMd),
                ),
              ),
              icon: const Icon(Icons.logout),
              label: const Text(
                'লগআউট করুন',
                style: TextStyle(fontWeight: FontWeight.bold),
              ),
              onPressed: () => _confirmLogout(context, ref),
            ),
          ),
        ],
      ),
    );
  }

  Widget _infoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 120,
            child: Text(
              label,
              style: const TextStyle(fontSize: 12, color: AppColors.textSecondary),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
            ),
          ),
        ],
      ),
    );
  }
}
