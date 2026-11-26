import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../shared/widgets/card_shell.dart';
import '../providers/prayer_times_provider.dart';
import '../services/prayer_calculator_service.dart';

/// Full Prayer Times, District Calculation, Custom Jamaat & Azan Alarm Screen
class PrayerScheduleScreen extends ConsumerWidget {
  const PrayerScheduleScreen({super.key});

  Future<void> _pickCustomTime(
    BuildContext context,
    WidgetRef ref,
    String waqtName,
    DateTime currentIqamah,
  ) async {
    final picked = await showTimePicker(
      context: context,
      initialTime: TimeOfDay(
        hour: currentIqamah.hour,
        minute: currentIqamah.minute,
      ),
    );
    if (picked == null) return;
    await ref
        .read(prayerSettingsProvider.notifier)
        .setCustomIqamah(waqtName, picked.hour, picked.minute);
    if (context.mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('$waqtName জামাতের সময় হালনাগাদ হয়েছে'),
          backgroundColor: AppColors.emeraldPrimary,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final prayerDataAsync = ref.watch(prayerTimeStreamProvider);
    final settings = ref.watch(prayerSettingsProvider);
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Scaffold(
      appBar: AppBar(
        title: const Text('নামাজের সময়সূচি ও আজান অ্যালার্ম'),
        actions: [
          if (settings.customIqamah.isNotEmpty)
            IconButton(
              icon: const Icon(Icons.restore),
              tooltip: 'কাস্টম জামাত সময় রিসেট',
              onPressed: () {
                ref.read(prayerSettingsProvider.notifier).resetCustomIqamah();
              },
            ),
        ],
      ),
      body: prayerDataAsync.when(
        data: (data) {
          final schedule = data.schedule;

          return ListView(
            padding: const EdgeInsets.all(MosqueContentPadding.screenHorizontal),
            children: [
              // District Coordinate Selector Card
              CardShell(
                color: isDark ? AppColors.nightSurface : AppColors.emeraldSoft,
                border: Border.all(color: AppColors.emeraldPrimary.withOpacity(0.3)),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        const Icon(Icons.location_on_outlined, color: AppColors.emeraldPrimary),
                        const SizedBox(width: MosqueSpace.sm),
                        Expanded(
                          child: Text(
                            'অবস্থান ভিত্তিক সূর্যোদয় ও আজানের সময় (হানাফি)',
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              fontSize: 13,
                              color: isDark ? AppColors.nightTextPrimary : AppColors.textPrimary,
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 10),
                    DropdownButtonFormField<String>(
                      value: settings.districtNameBn,
                      decoration: InputDecoration(
                        labelText: 'জেলা নির্বাচন করুন',
                        isDense: true,
                        filled: true,
                        fillColor: Theme.of(context).cardColor,
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(10),
                        ),
                      ),
                      items: PrayerCalculatorService.bangladeshDistricts
                          .map(
                            (d) => DropdownMenuItem(
                              value: d.nameBn,
                              child: Text('${d.nameBn} জেলা'),
                            ),
                          )
                          .toList(),
                      onChanged: (val) {
                        if (val != null) {
                          ref.read(prayerSettingsProvider.notifier).setDistrict(val);
                        }
                      },
                    ),
                  ],
                ),
              ),
              const SizedBox(height: MosqueSpace.lg),

              ...schedule.map((item) {
                final alarmKey = item.nameBn == 'জুমুআ' ? 'যোহর' : item.nameBn;
                final isAlarmOn = settings.alarmEnabled[alarmKey] ?? false;

                return Padding(
                  padding: const EdgeInsets.only(bottom: MosqueSpace.md),
                  child: CardShell(
                    border: item.isNext
                        ? Border.all(color: AppColors.gold, width: 1.5)
                        : null,
                    child: Column(
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Row(
                              children: [
                                Container(
                                  width: 36,
                                  height: 36,
                                  decoration: BoxDecoration(
                                    shape: BoxShape.circle,
                                    color: item.isNext
                                        ? AppColors.goldLight
                                        : (isDark
                                            ? AppColors.nightSurfaceVariant
                                            : AppColors.bgLight),
                                  ),
                                  child: Icon(
                                    Icons.access_time,
                                    size: 18,
                                    color: item.isNext
                                        ? AppColors.goldDark
                                        : AppColors.emeraldPrimary,
                                  ),
                                ),
                                const SizedBox(width: MosqueSpace.md),
                                Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(
                                      item.nameBn,
                                      style: const TextStyle(
                                        fontSize: 15,
                                        fontWeight: FontWeight.bold,
                                      ),
                                    ),
                                    Text(
                                      'আজান (শুরু): ${item.azanTime}',
                                      style: TextStyle(
                                        fontSize: 12,
                                        color: isDark
                                            ? AppColors.nightTextMuted
                                            : AppColors.textSecondary,
                                      ),
                                    ),
                                  ],
                                ),
                              ],
                            ),
                            Row(
                              children: [
                                Column(
                                  crossAxisAlignment: CrossAxisAlignment.end,
                                  children: [
                                    Text(
                                      'জামাত',
                                      style: TextStyle(
                                        fontSize: 11,
                                        color: isDark
                                            ? AppColors.nightTextMuted
                                            : AppColors.textSecondary,
                                      ),
                                    ),
                                    Text(
                                      item.iqamahTime,
                                      style: const TextStyle(
                                        fontSize: 16,
                                        fontWeight: FontWeight.bold,
                                        color: AppColors.emeraldPrimary,
                                      ),
                                    ),
                                  ],
                                ),
                                IconButton(
                                  icon: const Icon(Icons.edit_outlined, size: 18),
                                  tooltip: 'জামাত সময় পরিবর্তন',
                                  onPressed: () => _pickCustomTime(
                                    context,
                                    ref,
                                    item.nameBn,
                                    item.iqamahDateTime,
                                  ),
                                ),
                              ],
                            ),
                          ],
                        ),
                        const Divider(height: 16),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Row(
                              children: [
                                Icon(
                                  isAlarmOn
                                      ? Icons.notifications_active
                                      : Icons.notifications_none,
                                  size: 16,
                                  color: isAlarmOn ? AppColors.emeraldPrimary : Colors.grey,
                                ),
                                const SizedBox(width: 6),
                                Text(
                                  'দৈনিক আজান রিমাইন্ডার',
                                  style: TextStyle(
                                    fontSize: 12,
                                    color: isAlarmOn
                                        ? AppColors.emeraldPrimary
                                        : Colors.grey.shade600,
                                  ),
                                ),
                              ],
                            ),
                            Switch.adaptive(
                              value: isAlarmOn,
                              activeColor: AppColors.emeraldPrimary,
                              onChanged: (val) async {
                                final result = await ref
                                    .read(prayerSettingsProvider.notifier)
                                    .toggleWaqtAlarm(
                                      waqtName: item.nameBn,
                                      enabled: val,
                                      hour24: item.iqamahDateTime.hour,
                                      minute: item.iqamahDateTime.minute,
                                      displayTime: item.iqamahTime,
                                    );
                                if (context.mounted) {
                                  ScaffoldMessenger.of(context).showSnackBar(
                                    SnackBar(
                                      content: Text(
                                        result
                                            ? '${item.nameBn} অ্যালার্ম চালু হয়েছে'
                                            : '${item.nameBn} অ্যালার্ম বন্ধ করা হয়েছে',
                                      ),
                                    ),
                                  );
                                }
                              },
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                );
              }),
            ],
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (err, _) => Center(child: Text('ত্রুটি: $err')),
      ),
    );
  }
}
