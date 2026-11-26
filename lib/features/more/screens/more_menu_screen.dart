import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/router/app_router.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../shared/widgets/card_shell.dart';
import '../../role_permissions/models/committee_role.dart';
import '../../role_permissions/models/permission_catalog.dart';
import '../../role_permissions/providers/role_permissions_provider.dart';
import '../../role_permissions/services/permission_engine.dart';

/// Screen listing All Mosque Operations, Modules, and Educational Hubs
class MoreMenuScreen extends ConsumerWidget {
  const MoreMenuScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final role = ref.watch(currentRoleProvider);
    final canManageMadrasa = role == CommitteeRole.president ||
        PermissionEngine.hasPermission(role, PermissionCatalog.madrasaManage);
    final canReceiveFees = role == CommitteeRole.president ||
        PermissionEngine.hasPermission(role, PermissionCatalog.feeReceive);
    final canCashbox = role == CommitteeRole.president ||
        PermissionEngine.hasPermission(role, PermissionCatalog.cashboxManage) ||
        PermissionEngine.hasPermission(role, PermissionCatalog.cashboxWitness);
    final canAdmin = role == CommitteeRole.president || role.canEditMosqueProfile();
    final canSeal = role == CommitteeRole.president ||
        role.canManageOfficeDocs() ||
        role == CommitteeRole.treasurer;
    final canPosterAi = role == CommitteeRole.president ||
        role.canPublishNotices() ||
        role.canManageSmartBoard();

    return Scaffold(
      appBar: AppBar(
        title: const Text('অন্যান্য কার্যক্রম ও মডিউল'),
      ),
      body: ListView(
        padding: const EdgeInsets.all(AppDimens.spacing16),
        children: [
          // 1. Madrasa & Islamic Education
          _buildCategoryHeader('মাদরাসা ও ইসলামী শিক্ষা', Icons.school, AppColors.emeraldPrimary),
          const SizedBox(height: 8),
          _buildMenuItem(
            context: context,
            title: 'মাদরাসা ও মক্তব হাব',
            subtitle: 'শিক্ষার্থী, হাজিরা, হিফজ, পরীক্ষা ও ফি আদায়ের মূল কেন্দ্র',
            icon: Icons.dashboard_outlined,
            color: AppColors.emeraldPrimary,
            onTap: () => context.push(AppRouter.madrasa),
          ),
          const SizedBox(height: 6),
          _buildMenuItem(
            context: context,
            title: 'শিক্ষার্থী ডিরেক্টরি',
            subtitle: 'ভর্তি ফরম ও সকল শ্রেণীর শিক্ষার্থী তালিকা',
            icon: Icons.groups_outlined,
            color: AppColors.emeraldPrimary,
            onTap: () => context.push(AppRouter.madrasaStudents),
          ),
          if (canManageMadrasa) ...[
            const SizedBox(height: 6),
            _buildMenuItem(
              context: context,
              title: 'দৈনিক হাজিরা খাতা',
              subtitle: 'ছাত্রদের দৈনিক উপস্থিতি, অনুপস্থিতি ও শতকরা হার',
              icon: Icons.fact_check_outlined,
              color: Colors.blue.shade700,
              onTap: () => context.push(AppRouter.madrasaAttendance),
            ),
            const SizedBox(height: 6),
            _buildMenuItem(
              context: context,
              title: 'হিফজুল কুরআন ট্র্যাকার',
              subtitle: 'পারা-পৃষ্ঠা ও ছবক, ছবকী, আমোখতা ৫-স্টার মূল্যায়ন',
              icon: Icons.auto_stories_outlined,
              color: AppColors.goldDeep,
              onTap: () => context.push(AppRouter.madrasaHifz),
            ),
          ],
          const SizedBox(height: 6),
          _buildMenuItem(
            context: context,
            title: 'পরীক্ষা ও ফলাফল',
            subtitle: 'সাময়িক ও বার্ষিক পরীক্ষার নম্বর ও ৩৬০° রিপোর্ট কার্ড',
            icon: Icons.grade_outlined,
            color: Colors.purple.shade700,
            onTap: () => context.push(AppRouter.madrasaExams),
          ),
          if (canReceiveFees) ...[
            const SizedBox(height: 6),
            _buildMenuItem(
              context: context,
              title: 'মাদরাসা ফি আদায় ও রসিদ',
              subtitle: 'ছাত্রদের মাসিক ফি সংগ্রহ ও অটোমেটিক ফাইন্যান্স এন্ট্রি',
              icon: Icons.receipt_long_outlined,
              color: AppColors.roleTreasurer,
              onTap: () => context.push(AppRouter.madrasaFees),
            ),
          ],
          const SizedBox(height: 20),

          // 2. Staff & Administration
          _buildCategoryHeader('প্রশাসন ও পরিচালনা', Icons.business_center, AppColors.rolePresident),
          const SizedBox(height: 8),
          _buildMenuItem(
            context: context,
            title: 'কর্মকর্তা ও কর্মচারী তালিকা',
            subtitle: 'ইমাম, মুয়াজ্জিন, খাদেম ও বেতন কাঠামো',
            icon: Icons.badge_outlined,
            color: AppColors.rolePresident,
            onTap: () => context.push(AppRouter.staff),
          ),
          const SizedBox(height: 6),
          _buildMenuItem(
            context: context,
            title: 'নামাজের দায়িত্ব রোস্টার',
            subtitle: 'ওয়াক্তভিত্তিক ইমাম ও মুয়াজ্জিন বদলি ডিউটি বণ্টন',
            icon: Icons.calendar_today_outlined,
            color: Colors.teal.shade700,
            onTap: () => context.push(AppRouter.dutyRoster),
          ),
          const SizedBox(height: 6),
          _buildMenuItem(
            context: context,
            title: 'ইমাম ও খাদেম খাবার তালিকা',
            subtitle: 'মুসাল্লিদের স্বতঃস্ফূর্ত খাবার পরিবেশন শিডিউল',
            icon: Icons.restaurant_outlined,
            color: Colors.orange.shade700,
            onTap: () => context.push(AppRouter.mealSchedule),
          ),
          const SizedBox(height: 6),
          _buildMenuItem(
            context: context,
            title: 'কমিটির সভা ও রেজুলেশন',
            subtitle: 'মাসিক মিটিং ও সিদ্ধান্ত রেকর্ড বুক',
            icon: Icons.gavel_outlined,
            color: Colors.indigo.shade700,
            onTap: () => context.push(AppRouter.meetings),
          ),
          const SizedBox(height: 6),
          _buildMenuItem(
            context: context,
            title: 'নির্বাচন ও গ্যাজেট',
            subtitle: 'মসজিদ কমিটির তফসিল, প্রার্থী ও ভোট গণনা',
            icon: Icons.how_to_vote_outlined,
            color: Colors.deepPurple.shade700,
            onTap: () => context.push(AppRouter.elections),
          ),
          const SizedBox(height: 20),

          // 3. Religious, Community & Assets
          _buildCategoryHeader('ধর্মীয় ও কমিউনিটি সেবা', Icons.mosque_outlined, AppColors.emeraldPrimary),
          const SizedBox(height: 8),
          _buildMenuItem(
            context: context,
            title: 'ওয়াকফ ও মসজিদ সম্পত্তি',
            subtitle: 'জমি, মার্কেট, দোকান ও স্থাপনার রেজিস্ট্রি',
            icon: Icons.home_work_outlined,
            color: Colors.green.shade800,
            onTap: () => context.push(AppRouter.waqfAssets),
          ),
          if (canCashbox) ...[
            const SizedBox(height: 6),
            _buildMenuItem(
              context: context,
              title: 'জুমার দানবাক্স গণনা',
              subtitle: '৩-সাক্ষী নোট গণনা ও ভাউচার রেজিস্টার',
              icon: Icons.inventory_2_outlined,
              color: AppColors.roleTreasurer,
              onTap: () => context.push(AppRouter.cashbox),
            ),
          ],
          const SizedBox(height: 6),
          _buildMenuItem(
            context: context,
            title: 'জরুরি জানাজা বিজ্ঞপ্তি',
            subtitle: 'তাত্ক্ষণিক জানাজা অ্যালার্ট ও এসএমএস ব্রডকাস্ট',
            icon: Icons.notifications_active_outlined,
            color: Colors.red.shade700,
            onTap: () => context.push(AppRouter.janazaAlerts),
          ),
          const SizedBox(height: 6),
          _buildMenuItem(
            context: context,
            title: 'নামাজের সময়সূচি',
            subtitle: 'দৈনিক ৫ ওয়াক্ত সালাত ও আজানের সময়',
            icon: Icons.access_time_outlined,
            color: AppColors.emeraldPrimary,
            onTap: () => context.push(AppRouter.prayerTimes),
          ),
          const SizedBox(height: 6),
          _buildMenuItem(
            context: context,
            title: 'ইসলামিক ক্যালেন্ডার ও ইভেন্ট',
            subtitle: 'হিজরি তারিখ, চাঁদ দেখা অফসেট ও গুরুত্বপূর্ণ দিবস',
            icon: Icons.event_note_outlined,
            color: AppColors.goldDeep,
            onTap: () => context.push(AppRouter.islamicCalendar),
          ),
          const SizedBox(height: 6),
          _buildMenuItem(
            context: context,
            title: 'মাহে রমজান হাব',
            subtitle: 'সেহরি, ইফতার ও তিন আশরার দিনলিপি',
            icon: Icons.nights_stay_outlined,
            color: Colors.amber.shade800,
            onTap: () => context.push(AppRouter.ramadanHub),
          ),
          const SizedBox(height: 20),

          // 4. Events, Welfare & AI Creative
          _buildCategoryHeader('অনুষ্ঠান, সমাজকল্যাণ ও পোস্টার AI', Icons.celebration_outlined, AppColors.goldDeep),
          const SizedBox(height: 8),
          _buildMenuItem(
            context: context,
            title: 'অনুষ্ঠান ও মাহফিল ব্যবস্থাপনা',
            subtitle: 'ওয়াজ মাহফিল, শবে কদর, ইফতার সমাবেশ ও বাজেট',
            icon: Icons.event_available_outlined,
            color: AppColors.goldDeep,
            onTap: () => context.push(AppRouter.events),
          ),
          if (canPosterAi) ...[
            const SizedBox(height: 6),
            _buildMenuItem(
              context: context,
              title: 'ইসলামিক দাওয়াতনামা পোস্টার AI',
              subtitle: 'Gemini AI দিয়ে মাহফিলের ব্যানার ও আমন্ত্রণপত্র ডিজাইন',
              icon: Icons.auto_awesome_outlined,
              color: AppColors.emeraldPrimary,
              onTap: () => context.push(AppRouter.posterAi),
            ),
          ],
          const SizedBox(height: 6),
          _buildMenuItem(
            context: context,
            title: 'যাকাত ও সমাজকল্যাণ তহবিল',
            subtitle: 'চিকিৎসা, বিধবা ও এতিম অনুদান আবেদন ও বিতরণ ব্যবস্থাপনা',
            icon: Icons.volunteer_activism_outlined,
            color: Colors.teal.shade700,
            onTap: () => context.push(AppRouter.welfare),
          ),
          const SizedBox(height: 20),

          // 5. Documents, Security & Cloud Sync
          _buildCategoryHeader('ডকুমেন্ট, নিরাপত্তা ও ক্লাউড সিঙ্ক', Icons.lock_outline, Colors.blueGrey.shade800),
          const SizedBox(height: 8),
          _buildMenuItem(
            context: context,
            title: 'জুমার খুতবা ও বয়ান প্রস্তুতি AI',
            subtitle: 'Gemini AI দিয়ে বিষয়ভিত্তিক খুতবার সারসংক্ষেপ ও হাদিস রেফারেন্স',
            icon: Icons.psychology_outlined,
            color: AppColors.emeraldPrimary,
            onTap: () => context.push(AppRouter.khutbahIdeas),
          ),
          if (canSeal) ...[
            const SizedBox(height: 6),
            _buildMenuItem(
              context: context,
              title: 'অফিসিয়াল সিল ও ডিজিটাল স্বাক্ষর',
              subtitle: 'কমিটির সভাপতি ও কোষাধ্যক্ষের এনক্রিপ্টেড স্বাক্ষর ও সিল',
              icon: Icons.draw_outlined,
              color: Colors.indigo.shade700,
              onTap: () => context.push(AppRouter.sealSignature),
            ),
          ],
          const SizedBox(height: 6),
          _buildMenuItem(
            context: context,
            title: 'ডকুমেন্ট ও প্রিন্ট সেন্টার',
            subtitle: 'রসিদ, ভাউচার ও নোটিশের সরাসরি প্রিন্ট এবং PDF ডাউনলোড',
            icon: Icons.print_outlined,
            color: Colors.deepOrange.shade700,
            onTap: () => context.push(AppRouter.printHub),
          ),
          if (canAdmin) ...[
            const SizedBox(height: 6),
            _buildMenuItem(
              context: context,
              title: 'অ্যাপ্লিকেশন নিরাপত্তা ও ডিভাইস অডিট',
              subtitle: 'রুট ও টেম্পারিং সুরক্ষা এবং এনক্রিপশন স্ট্যাটাস নিরীক্ষা',
              icon: Icons.security_outlined,
              color: Colors.red.shade700,
              onTap: () => context.push(AppRouter.appSecurity),
            ),
            const SizedBox(height: 6),
            _buildMenuItem(
              context: context,
              title: 'ক্লাউড সিঙ্ক ও ব্যাকআপ',
              subtitle: 'Supabase ব্যাকএন্ডে রিয়েল-টাইম অফলাইন ও অনলাইন সিঙ্ক',
              icon: Icons.cloud_sync_outlined,
              color: Colors.teal.shade800,
              onTap: () => context.push(AppRouter.cloudSync),
            ),
          ],
          const SizedBox(height: AppDimens.spacing24),
        ],
      ),
    );
  }

  Widget _buildCategoryHeader(String title, IconData icon, Color color) {
    return Row(
      children: [
        Icon(icon, size: 18, color: color),
        const SizedBox(width: 8),
        Text(
          title,
          style: TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.bold,
            color: color,
          ),
        ),
      ],
    );
  }

  Widget _buildMenuItem({
    required BuildContext context,
    required String title,
    required String subtitle,
    required IconData icon,
    required Color color,
    required VoidCallback onTap,
  }) {
    return CardShell(
      onTap: onTap,
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: color.withOpacity(0.12),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Icon(icon, color: color, size: 20),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                ),
                Text(
                  subtitle,
                  style: TextStyle(fontSize: 11, color: Colors.grey.shade600),
                ),
              ],
            ),
          ),
          const Icon(Icons.arrow_forward_ios, size: 14, color: Colors.grey),
        ],
      ),
    );
  }
}
