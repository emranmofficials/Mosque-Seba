import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/widgets/card_shell.dart';
import '../../cashbox/providers/cashbox_provider.dart';
import '../../finance/providers/finance_provider.dart';
import '../../madrasa/providers/madrasa_provider.dart';
import '../../membership/providers/member_provider.dart';
import '../../notices/providers/notice_provider.dart';
import '../../subscriptions/providers/chanda_provider.dart';
import '../../waqf_assets/providers/waqf_provider.dart';
import '../providers/sync_status_provider.dart';

/// Screen presenting Supabase Cloud Synchronization & Offline Data Vault Status
class CloudSyncScreen extends ConsumerWidget {
  const CloudSyncScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final syncState = ref.watch(syncNotifierProvider);
    final membersCount = ref.watch(memberListProvider).valueOrNull?.length ?? 0;
    final txnsCount = ref.watch(financeListProvider).valueOrNull?.length ?? 0;
    final chandaCount = ref.watch(chandaListProvider).valueOrNull?.length ?? 0;
    final cashboxCount = ref.watch(cashboxListProvider).valueOrNull?.length ?? 0;
    final studentsCount = ref.watch(studentListProvider).valueOrNull?.length ?? 0;
    final waqfCount = ref.watch(waqfListProvider).valueOrNull?.length ?? 0;
    final noticesCount = ref.watch(noticeListProvider).valueOrNull?.length ?? 0;

    final pendingLabel = syncState.pendingOutboxCount > 0
        ? '${BengaliNumberFormatter.toBengali(syncState.pendingOutboxCount)} টি পেন্ডিং'
        : 'সিঙ্কড';

    final tables = [
      {'name': 'সদস্য তালিকা (Members)', 'count': '${BengaliNumberFormatter.toBengali(membersCount)} জন', 'status': pendingLabel},
      {'name': 'আয় ও ব্যয় লেজার (Transactions)', 'count': '${BengaliNumberFormatter.toBengali(txnsCount)} টি', 'status': pendingLabel},
      {'name': 'চাঁদা আদায় রেকর্ড (Chanda)', 'count': '${BengaliNumberFormatter.toBengali(chandaCount)} টি', 'status': pendingLabel},
      {'name': 'জুমার দানবাক্স (Cashbox)', 'count': '${BengaliNumberFormatter.toBengali(cashboxCount)} টি', 'status': pendingLabel},
      {'name': 'মাদরাসা শিক্ষার্থী (Students)', 'count': '${BengaliNumberFormatter.toBengali(studentsCount)} জন', 'status': pendingLabel},
      {'name': 'ওয়াকফ সম্পত্তি (Properties)', 'count': '${BengaliNumberFormatter.toBengali(waqfCount)} টি', 'status': pendingLabel},
      {'name': 'বিজ্ঞপ্তি (Notices)', 'count': '${BengaliNumberFormatter.toBengali(noticesCount)} টি', 'status': pendingLabel},
    ];

    return Scaffold(
      appBar: AppBar(
        title: const Text('ক্লাউড সিঙ্ক ও অফলাইন ব্যাকআপ'),
      ),
      body: ListView(
        padding: const EdgeInsets.all(AppDimens.spacing16),
        children: [
          // Cloud Status Banner
          CardShell(
            padding: const EdgeInsets.all(AppDimens.spacing16),
            child: Column(
              children: [
                Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: AppColors.emeraldPrimary.withOpacity(0.12),
                        shape: BoxShape.circle,
                      ),
                      child: Icon(
                        syncState.isSyncing ? Icons.sync : Icons.cloud_done,
                        color: AppColors.emeraldPrimary,
                        size: 32,
                      ),
                    ),
                    const SizedBox(width: 14),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text('সুপ্রা-বেস (Supabase) সংযোগ স্ট্যাটাস', style: TextStyle(fontSize: 12, color: Colors.grey)),
                          const SizedBox(height: 2),
                          Text(
                            syncState.isSyncing ? 'সিঙ্ক চলমান...' : 'ক্লাউড ব্যাকআপ সক্রিয়',
                            style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: AppColors.emeraldPrimary),
                          ),
                          const SizedBox(height: 2),
                          Text(
                            'সর্বশেষ সিঙ্ক: ${syncState.lastSyncedAt != null ? BengaliNumberFormatter.toBengali('${syncState.lastSyncedAt!.hour}:${syncState.lastSyncedAt!.minute.toString().padLeft(2, '0')}') : 'এখনই'} • অপেক্ষমাণ: ${BengaliNumberFormatter.toBengali(syncState.pendingOutboxCount)} টি',
                            style: TextStyle(fontSize: 11, color: Colors.grey.shade600),
                          ),
                          if (syncState.errorMessage != null && syncState.errorMessage!.isNotEmpty) ...[
                            const SizedBox(height: 4),
                            Text(
                              syncState.errorMessage!,
                              style: const TextStyle(fontSize: 11, color: AppColors.urgentRed),
                            ),
                          ],
                        ],
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton.icon(
                    onPressed: syncState.isSyncing
                        ? null
                        : () async {
                            final ok = await ref.read(syncNotifierProvider.notifier).triggerSync();
                            if (context.mounted) {
                              ScaffoldMessenger.of(context).showSnackBar(
                                SnackBar(
                                  content: Text(ok ? 'ক্লাউড সিঙ্ক সফলভাবে সম্পন্ন হয়েছে' : 'সিঙ্কে ত্রুটি হয়েছে'),
                                  backgroundColor: ok ? AppColors.emeraldPrimary : Colors.red,
                                ),
                              );
                            }
                          },
                    icon: syncState.isSyncing
                        ? const SizedBox(
                            width: 16,
                            height: 16,
                            child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                          )
                        : const Icon(Icons.sync, size: 18),
                    label: const Text('এখনই সম্পূর্ণ সিঙ্ক করুন', style: TextStyle(fontWeight: FontWeight.bold)),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.emeraldPrimary,
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(vertical: 12),
                    ),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 20),

          // Synced Tables Status
          const Text(
            'সিঙ্ককৃত টেবিলসমূহ (SSoT পারমিশন ও ডাটাবেস)',
            style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 10),

          ...tables.map((t) {
            return Padding(
              padding: const EdgeInsets.only(bottom: 8),
              child: CardShell(
                padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                child: Row(
                  children: [
                    const Icon(Icons.table_chart_outlined, size: 20, color: AppColors.emeraldPrimary),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(t['name']!, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
                          Text('মোট রেকর্ড: ${t['count']}', style: TextStyle(fontSize: 11, color: Colors.grey.shade600)),
                        ],
                      ),
                    ),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                      decoration: BoxDecoration(
                        color: AppColors.emeraldPrimary.withOpacity(0.12),
                        borderRadius: BorderRadius.circular(6),
                      ),
                      child: Text(
                        t['status']!,
                        style: const TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.bold,
                          color: AppColors.emeraldPrimary,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            );
          }),
        ],
      ),
    );
  }
}
