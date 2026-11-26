import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/widgets/card_shell.dart';
import '../../madrasa_attendance/providers/madrasa_attendance_provider.dart';
import '../dialogs/admit_student_dialog.dart';
import '../providers/madrasa_provider.dart';

/// Comprehensive Madrasa & Islamic Education Hub (মাদরাসা ও ইসলামী শিক্ষা ড্যাশবোর্ড)
class MadrasaDashboardScreen extends ConsumerWidget {
  const MadrasaDashboardScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final studentsAsync = ref.watch(studentListProvider);
    final students = studentsAsync.valueOrNull ?? [];
    final attendanceState = ref.watch(attendanceNotifierProvider);
    final attendancePct = attendanceState.totalCount > 0
        ? '${BengaliNumberFormatter.toBengali(attendanceState.attendancePercentage.round().toString())}%'
        : '—';

    final hifzCount = students.where((s) => s.className.contains('হিফজ')).length;
    final totalFees = students.fold<double>(0.0, (acc, s) => acc + s.monthlyFee);

    return Scaffold(
      appBar: AppBar(
        title: const Text('মাদরাসা ও মক্তব শিক্ষা'),
        actions: [
          IconButton(
            icon: const Icon(Icons.person_add),
            tooltip: 'নতুন শিক্ষার্থী ভর্তি',
            onPressed: () {
              showDialog(
                context: context,
                builder: (ctx) => AdmitStudentDialog(
                  onSubmit: (student) =>
                      ref.read(studentListProvider.notifier).admitStudent(student),
                ),
              );
            },
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () => ref.read(studentListProvider.notifier).loadStudents(),
        child: ListView(
          padding: const EdgeInsets.all(AppDimens.spacing16),
          children: [
            // KPI Summary Row
            Row(
              children: [
                Expanded(
                  child: _buildKpiCard(
                    title: 'মোট শিক্ষার্থী',
                    value: '${BengaliNumberFormatter.toBengali(students.length.toString())} জন',
                    icon: Icons.school,
                    color: AppColors.emeraldPrimary,
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: _buildKpiCard(
                    title: 'হিফজ শিক্ষার্থী',
                    value: '${BengaliNumberFormatter.toBengali(hifzCount.toString())} জন',
                    icon: Icons.menu_book,
                    color: AppColors.goldDeep,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 10),
            Row(
              children: [
                Expanded(
                  child: _buildKpiCard(
                    title: 'আজকের হাজিরা',
                    value: attendancePct,
                    icon: Icons.how_to_reg,
                    color: Colors.blue.shade700,
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: _buildKpiCard(
                    title: 'মাসিক সম্ভাব্য ফি',
                    value: '৳ ${BengaliNumberFormatter.toBengali(totalFees.toInt().toString())}',
                    icon: Icons.account_balance_wallet,
                    color: AppColors.roleTreasurer,
                  ),
                ),
              ],
            ),
            const SizedBox(height: AppDimens.spacing20),

            // Navigation Modules (The 5 Madrasa Pillars)
            const Text(
              'শিক্ষা ব্যবস্থাপনা মডিউলসমূহ',
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 12),

            _buildNavModuleTile(
              context: context,
              title: 'শিক্ষার্থী ডিরেক্টরি',
              subtitle: 'সকল ছাত্রের তথ্য, শ্রেণীবিভাগ ও অভিভাবক বিবরণ',
              icon: Icons.groups_outlined,
              color: AppColors.emeraldPrimary,
              onTap: () => context.push('/madrasa/students'),
            ),
            const SizedBox(height: 10),

            _buildNavModuleTile(
              context: context,
              title: 'দৈনিক হাজিরা খাতা',
              subtitle: 'উপস্থিতি, অনুপস্থিতি ও দৈনিক শতকরা প্রতিবেদন',
              icon: Icons.fact_check_outlined,
              color: Colors.blue.shade700,
              onTap: () => context.push('/madrasa/attendance'),
            ),
            const SizedBox(height: 10),

            _buildNavModuleTile(
              context: context,
              title: 'হিফজুল কুরআন ট্র্যাকার',
              subtitle: 'পারা, পৃষ্ঠা ও ছবক/ছবকী/আমোখতা ৫-স্টার মূল্যায়ন',
              icon: Icons.auto_stories_outlined,
              color: AppColors.goldDeep,
              onTap: () => context.push('/madrasa/hifz'),
            ),
            const SizedBox(height: 10),

            _buildNavModuleTile(
              context: context,
              title: 'পরীক্ষা ও ফলাফল',
              subtitle: 'সাময়িক ও বার্ষিক পরীক্ষার নম্বর ও ফলাফল প্রকাশ',
              icon: Icons.grade_outlined,
              color: Colors.purple.shade700,
              onTap: () => context.push('/madrasa/exams'),
            ),
            const SizedBox(height: 10),

            _buildNavModuleTile(
              context: context,
              title: 'মাদরাসা ফি আদায় ও রসিদ',
              subtitle: 'মাসিক টিউশন ফি সংগ্রহ ও অটোমেটিক ফাইন্যান্স পোস্টিং',
              icon: Icons.receipt_long_outlined,
              color: AppColors.roleTreasurer,
              onTap: () => context.push('/madrasa/fees'),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildKpiCard({
    required String title,
    required String value,
    required IconData icon,
    required Color color,
  }) {
    return CardShell(
      padding: const EdgeInsets.all(AppDimens.spacing14),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(title, style: const TextStyle(fontSize: 12, color: Colors.grey)),
              Icon(icon, size: 20, color: color),
            ],
          ),
          const SizedBox(height: 6),
          Text(
            value,
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: color,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildNavModuleTile({
    required BuildContext context,
    required String title,
    required String subtitle,
    required IconData icon,
    required Color color,
    required VoidCallback onTap,
  }) {
    return CardShell(
      onTap: onTap,
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: color.withOpacity(0.12),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Icon(icon, color: color, size: 24),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(fontSize: 15, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 2),
                Text(
                  subtitle,
                  style: TextStyle(fontSize: 12, color: Colors.grey.shade600),
                ),
              ],
            ),
          ),
          const Icon(Icons.chevron_right, color: Colors.grey),
        ],
      ),
    );
  }
}
