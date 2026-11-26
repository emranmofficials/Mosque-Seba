import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/components/empty_state_view.dart';
import '../providers/madrasa_attendance_provider.dart';
import '../widgets/attendance_toggle_row.dart';

/// Screen for recording Daily Madrasa Student Attendance (দৈনিক মাদরাসা হাজিরা খাতা)
class MadrasaAttendanceScreen extends ConsumerWidget {
  const MadrasaAttendanceScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(attendanceNotifierProvider);
    final selectedDate = ref.watch(selectedAttendanceDateProvider);
    final selectedClass = ref.watch(attendanceClassFilterProvider);

    final classes = ['all', 'নূরানী বিভাগ', 'নাজেরা বিভাগ', 'হিফজুল কুরআন', 'কিতাব বিভাগ'];

    return Scaffold(
      appBar: AppBar(
        title: const Text('দৈনিক হাজিরা খাতা'),
        actions: [
          TextButton.icon(
            onPressed: () => ref.read(attendanceNotifierProvider.notifier).markAllPresent(),
            icon: const Icon(Icons.done_all, size: 18, color: AppColors.emeraldPrimary),
            label: const Text('সবাই উপস্থিত', style: TextStyle(color: AppColors.emeraldPrimary, fontWeight: FontWeight.bold)),
          ),
        ],
      ),
      body: Column(
        children: [
          // Date & Class Filters Container
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
            color: Theme.of(context).cardColor,
            child: Column(
              children: [
                // Date Picker Row
                Row(
                  children: [
                    Expanded(
                      child: InkWell(
                        onTap: () async {
                          final picked = await showDatePicker(
                            context: context,
                            initialDate: selectedDate,
                            firstDate: DateTime(2025),
                            lastDate: DateTime(2030),
                          );
                          if (!context.mounted) return;
                          if (picked != null) {
                            ref.read(selectedAttendanceDateProvider.notifier).state = picked;
                            ref.read(attendanceNotifierProvider.notifier).loadAttendance();
                          }
                        },
                        child: Container(
                          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                          decoration: BoxDecoration(
                            border: Border.all(color: Colors.grey.shade400),
                            borderRadius: BorderRadius.circular(AppDimens.radiusSmall),
                          ),
                          child: Row(
                            children: [
                              const Icon(Icons.calendar_today, size: 18, color: AppColors.emeraldPrimary),
                              const SizedBox(width: 8),
                              Text(
                                'তারিখ: ${BengaliNumberFormatter.toBengali('${selectedDate.day}/${selectedDate.month}/${selectedDate.year}')}',
                                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                              ),
                            ],
                          ),
                        ),
                      ),
                    ),
                    const SizedBox(width: 10),
                    // Class Selector
                    Expanded(
                      child: DropdownButtonFormField<String>(
                        value: selectedClass,
                        decoration: InputDecoration(
                          border: OutlineInputBorder(borderRadius: BorderRadius.circular(AppDimens.radiusSmall)),
                          contentPadding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
                          isDense: true,
                        ),
                        items: classes.map((c) {
                          return DropdownMenuItem(
                            value: c,
                            child: Text(c == 'all' ? 'সকল বিভাগ' : c, style: const TextStyle(fontSize: 12)),
                          );
                        }).toList(),
                        onChanged: (val) {
                          if (val != null) {
                            ref.read(attendanceClassFilterProvider.notifier).state = val;
                            ref.read(attendanceNotifierProvider.notifier).loadAttendance();
                          }
                        },
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),

                // Attendance Metrics Bar
                Row(
                  children: [
                    _buildMetricChip(
                      label: 'মোট ছাত্র',
                      count: state.totalCount,
                      color: Colors.grey.shade700,
                    ),
                    const SizedBox(width: 6),
                    _buildMetricChip(
                      label: 'উপস্থিত',
                      count: state.presentCount,
                      color: AppColors.emeraldPrimary,
                    ),
                    const SizedBox(width: 6),
                    _buildMetricChip(
                      label: 'অনুপস্থিত',
                      count: state.absentCount,
                      color: Colors.red.shade600,
                    ),
                    const SizedBox(width: 6),
                    _buildMetricChip(
                      label: 'হার',
                      percent: state.attendancePercentage,
                      color: Colors.blue.shade700,
                    ),
                  ],
                ),
              ],
            ),
          ),

          // Attendance Student List
          Expanded(
            child: state.isLoading
                ? const Center(child: CircularProgressIndicator(color: AppColors.emeraldPrimary))
                : state.records.isEmpty
                    ? const EmptyStateView(
                        title: 'কোনো শিক্ষার্থী পাওয়া যায়নি',
                        message: 'এই বিভাগে কোনো শিক্ষার্থী নথিভুক্ত নেই।',
                      )
                    : ListView.builder(
                        padding: const EdgeInsets.all(AppDimens.spacing12),
                        itemCount: state.records.length,
                        itemBuilder: (context, index) {
                          final record = state.records[index];
                          return Padding(
                            padding: const EdgeInsets.only(bottom: AppDimens.spacing8),
                            child: AttendanceToggleRow(
                              record: record,
                              onStatusChanged: (newStatus) {
                                ref
                                    .read(attendanceNotifierProvider.notifier)
                                    .updateStatus(record.studentId, newStatus);
                              },
                            ),
                          );
                        },
                      ),
          ),

          // Save Attendance Sticky Bottom Bar
          Container(
            padding: const EdgeInsets.all(AppDimens.spacing14),
            decoration: BoxDecoration(
              color: Theme.of(context).cardColor,
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.06),
                  blurRadius: 6,
                  offset: const Offset(0, -2),
                ),
              ],
            ),
            child: SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: state.isLoading
                    ? null
                    : () async {
                        final success =
                            await ref.read(attendanceNotifierProvider.notifier).saveAttendance();
                        if (context.mounted) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(
                              content: Text(
                                success
                                    ? 'হাজিরা সফলভাবে সংরক্ষিত হয়েছে'
                                    : 'হাজিরা সংরক্ষণে সমস্যা হয়েছে',
                              ),
                              backgroundColor: success ? AppColors.emeraldPrimary : Colors.red,
                            ),
                          );
                        }
                      },
                icon: const Icon(Icons.save_outlined),
                label: const Text(
                  'হাজিরা সংরক্ষণ করুন',
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                ),
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.emeraldPrimary,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 14),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppDimens.radiusButton)),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildMetricChip({
    required String label,
    int? count,
    double? percent,
    required Color color,
  }) {
    final valueText = count != null
        ? BengaliNumberFormatter.toBengali(count.toString())
        : '${BengaliNumberFormatter.toBengali(percent!.toInt().toString())}%';

    return Expanded(
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 6),
        decoration: BoxDecoration(
          color: color.withOpacity(0.1),
          borderRadius: BorderRadius.circular(6),
          border: Border.all(color: color.withOpacity(0.2)),
        ),
        child: Column(
          children: [
            Text(
              label,
              style: TextStyle(fontSize: 10, color: color, fontWeight: FontWeight.w600),
            ),
            Text(
              valueText,
              style: TextStyle(fontSize: 13, fontWeight: FontWeight.bold, color: color),
            ),
          ],
        ),
      ),
    );
  }
}
