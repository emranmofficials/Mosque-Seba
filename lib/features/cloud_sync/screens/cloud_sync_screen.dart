import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/database/offline_cache_service.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/widgets/card_shell.dart';
import '../../authentication/providers/auth_provider.dart';
import '../providers/sync_status_provider.dart';

/// Screen presenting Supabase Cloud Synchronization & Offline SQLite Cache Status
class CloudSyncScreen extends ConsumerWidget {
  const CloudSyncScreen({super.key});

  static const List<Map<String, String>> _tableMetadata = [
    {'key': 'members', 'label': 'সদস্য তালিকা (Members)', 'unit': 'জন'},
    {'key': 'transactions', 'label': 'আয় ও ব্যয় লেজার (Transactions)', 'unit': 'টি'},
    {'key': 'chanda_payments', 'label': 'চাঁদা আদায় রেকর্ড (Chanda)', 'unit': 'টি'},
    {'key': 'cashbox_records', 'label': 'জুমার দানবাক্স (Cashbox)', 'unit': 'টি'},
    {'key': 'staff', 'label': 'স্টাফ ও ইমাম তালিকা (Staff)', 'unit': 'জন'},
    {'key': 'staff_payroll', 'label': 'বেতনভাতা লেজার (Payroll)', 'unit': 'টি'},
    {'key': 'duty_roster', 'label': 'নামাজের রোস্টার (Duty Roster)', 'unit': 'টি'},
    {'key': 'food_schedules', 'label': 'খাবার তালিকা (Meal Schedule)', 'unit': 'টি'},
    {'key': 'madrasa_students', 'label': 'মাদরাসা শিক্ষার্থী (Students)', 'unit': 'জন'},
    {'key': 'madrasa_attendance', 'label': 'হাজিরা রেকর্ড (Attendance)', 'unit': 'টি'},
    {'key': 'hifz_progress', 'label': 'হিফজ প্রোগ্রেস (Hifz)', 'unit': 'টি'},
    {'key': 'examinations', 'label': 'পরীক্ষাসমূহ (Examinations)', 'unit': 'টি'},
    {'key': 'madrasa_fee_payments', 'label': 'মাদরাসা ফি (Madrasa Fees)', 'unit': 'টি'},
    {'key': 'mosque_properties', 'label': 'ওয়াকফ সম্পত্তি (Properties)', 'unit': 'টি'},
    {'key': 'notices', 'label': 'বিজ্ঞপ্তি ও জানাজা (Notices)', 'unit': 'টি'},
    {'key': 'meetings', 'label': 'কমিটির সভা (Meetings)', 'unit': 'টি'},
    {'key': 'mosque_events', 'label': 'অনুষ্ঠান ও মাহফিল (Events)', 'unit': 'টি'},
    {'key': 'welfare_cases', 'label': 'যাকাত ও কল্যাণ (Welfare)', 'unit': 'টি'},
  ];

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final syncState = ref.watch(syncNotifierProvider);
    final mosqueId = ref.watch(authNotifierProvider).valueOrNull?.mosqueId ?? '';

    final lastSyncLabel = syncState.lastSyncedAt != null
        ? BengaliNumberFormatter.toBengali(
            '${syncState.lastSyncedAt!.hour.toString().padLeft(2, '0')}:${syncState.lastSyncedAt!.minute.toString().padLeft(2, '0')}',
          )
        : 'এখনো হয়নি';

    return Scaffold(
      appBar: AppBar(
        title: const Text('ক্লাউড সিঙ্ক ও অফলাইন ব্যাকআপ'),
      ),
      body: FutureBuilder<Map<String, int>>(
        future: OfflineCacheService.instance.countCachedByTable(mosqueId),
        builder: (context, snapshot) {
          final cachedCounts = snapshot.data ?? const <String, int>{};

          return SingleChildScrollView(
            padding: const EdgeInsets.all(AppDimens.spacing16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
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
                              Text(
                                'Supabase + Drift SQLite টু-ওয়ে সিঙ্ক',
                                style: AppTypography.caption.copyWith(color: Colors.grey),
                              ),
                              const SizedBox(height: 2),
                              Text(
                                syncState.isSyncing
                                    ? 'সিঙ্ক চলমান...'
                                    : (syncState.pendingOutboxCount > 0
                                        ? 'অফলাইন ডাটা আপলোডের অপেক্ষায়'
                                        : 'লোকাল ও ক্লাউড ডাটাবেস প্রস্তুত'),
                                style: Theme.of(context).textTheme.titleSmall?.copyWith(
                                  fontWeight: FontWeight.bold,
                                  color: AppColors.emeraldPrimary,
                                ),
                              ),
                              const SizedBox(height: 2),
                              Text(
                                'সর্বশেষ সিঙ্ক: $lastSyncLabel • আউটবক্স: ${BengaliNumberFormatter.toBengali(syncState.pendingOutboxCount)} টি',
                                style: Theme.of(context).textTheme.labelSmall?.copyWith(color: Colors.grey.shade600),
                              ),
                              if (syncState.errorMessage != null &&
                                  syncState.errorMessage!.isNotEmpty) ...[
                                const SizedBox(height: 4),
                                Text(
                                  syncState.errorMessage!,
                                  style: Theme.of(context).textTheme.labelSmall?.copyWith(
                                    color: AppColors.urgentRed,
                                  ),
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
                                final ok = await ref
                                    .read(syncNotifierProvider.notifier)
                                    .triggerSync();
                                if (context.mounted) {
                                  ScaffoldMessenger.of(context).showSnackBar(
                                    SnackBar(
                                      content: Text(
                                        ok
                                            ? 'টু-ওয়ে ক্লাউড সিঙ্ক সফলভাবে সম্পন্ন হয়েছে'
                                            : 'সিঙ্কে আংশিক বা পূর্ণ ত্রুটি হয়েছে',
                                      ),
                                      backgroundColor:
                                          ok ? AppColors.emeraldPrimary : Colors.red,
                                    ),
                                  );
                                }
                              },
                        icon: syncState.isSyncing
                            ? const SizedBox(
                                width: 16,
                                height: 16,
                                child: CircularProgressIndicator(
                                  strokeWidth: 2,
                                  color: Colors.white,
                                ),
                              )
                            : const Icon(Icons.sync, size: 18),
                        label: const Text(
                          'এখনই সম্পূর্ণ সিঙ্ক করুন (Push & Pull)',
                          style: TextStyle(fontWeight: FontWeight.bold),
                        ),
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
              Text(
                'অফলাইন SQLite ক্যাশ ও পেন্ডিং আউটবক্স',
                style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 10),

              ..._tableMetadata.map((meta) {
                final tableKey = meta['key']!;
                final cachedCount = cachedCounts[tableKey] ?? 0;
                final pendingCount = syncState.pendingByTable[tableKey] ?? 0;
                final hasPending = pendingCount > 0;
                final statusText = hasPending
                    ? '${BengaliNumberFormatter.toBengali(pendingCount)} টি পেন্ডিং'
                    : (cachedCount > 0 ? 'ক্যাশড' : 'সিঙ্ক রেডি');

                return Padding(
                  padding: const EdgeInsets.only(bottom: 8),
                  child: CardShell(
                    padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                    child: Row(
                      children: [
                        Icon(
                          Icons.table_chart_outlined,
                          size: 20,
                          color: hasPending
                              ? AppColors.warning
                              : AppColors.emeraldPrimary,
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                meta['label']!,
                                style: Theme.of(context).textTheme.bodySmall?.copyWith(
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                              Text(
                                'লোকাল ক্যাশ: ${BengaliNumberFormatter.toBengali(cachedCount)} ${meta['unit']}',
                                style: Theme.of(context).textTheme.labelSmall?.copyWith(color: Colors.grey.shade600),
                              ),
                            ],
                          ),
                        ),
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                          decoration: BoxDecoration(
                            color: (hasPending
                                    ? AppColors.warning
                                    : AppColors.emeraldPrimary)
                                .withOpacity(0.12),
                            borderRadius: BorderRadius.circular(6),
                          ),
                          child: Text(
                            statusText,
                            style: Theme.of(context).textTheme.labelSmall?.copyWith(
                              fontWeight: FontWeight.bold,
                              color: hasPending
                                  ? Colors.orange.shade900
                                  : AppColors.emeraldPrimary,
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
        },
      ),
    );
  }
}
