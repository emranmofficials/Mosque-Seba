import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/utils/bengali_number_formatter.dart';
import '../../../../shared/components/empty_state_view.dart';
import '../../../../shared/widgets/card_shell.dart';
import '../dialogs/admit_student_dialog.dart';
import '../models/student_model.dart';
import '../providers/madrasa_provider.dart';
import '../widgets/student_card.dart';

/// Screen presenting the full Madrasa/Maktab Student Directory with filters
class StudentDirectoryScreen extends ConsumerWidget {
  const StudentDirectoryScreen({super.key});

  void _showAdmitDialog(BuildContext context, WidgetRef ref) {
    showDialog(
      context: context,
      builder: (ctx) => AdmitStudentDialog(
        onSubmit: (student) => ref.read(studentListProvider.notifier).admitStudent(student),
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final studentsAsync = ref.watch(studentListProvider);
    final filteredStudents = ref.watch(filteredStudentsProvider);
    final selectedClass = ref.watch(studentClassFilterProvider);

    final classes = ['all', 'নূরানী বিভাগ', 'নাজেরা বিভাগ', 'হিফজুল কুরআন', 'কিতাব বিভাগ'];

    return Scaffold(
      appBar: AppBar(
        title: const Text('শিক্ষার্থী ডিরেক্টরি'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () => ref.read(studentListProvider.notifier).loadStudents(),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        backgroundColor: AppColors.emeraldPrimary,
        foregroundColor: Colors.white,
        icon: const Icon(Icons.person_add),
        label: const Text('নতুন ভর্তি', style: TextStyle(fontWeight: FontWeight.bold)),
        onPressed: () => _showAdmitDialog(context, ref),
      ),
      body: Column(
        children: [
          // Search & Filter Box
          Container(
            padding: const EdgeInsets.all(AppDimens.spacing12),
            color: Theme.of(context).cardColor,
            child: Column(
              children: [
                // Search Input
                TextField(
                  onChanged: (val) => ref.read(studentSearchQueryProvider.notifier).state = val,
                  decoration: InputDecoration(
                    hintText: 'শিক্ষার্থী বা অভিভাবকের নাম / ফোন খুঁজুন...',
                    prefixIcon: const Icon(Icons.search),
                    filled: true,
                    fillColor: Colors.grey.withOpacity(0.08),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(AppDimens.radiusSmall),
                      borderSide: BorderSide.none,
                    ),
                    contentPadding: const EdgeInsets.symmetric(vertical: 0, horizontal: 16),
                  ),
                ),
                const SizedBox(height: 8),
                // Class Filter Chips
                SingleChildScrollView(
                  scrollDirection: Axis.horizontal,
                  child: Row(
                    children: classes.map((cls) {
                      final isSelected = selectedClass == cls;
                      final label = cls == 'all' ? 'সকল বিভাগ' : cls;
                      return Padding(
                        padding: const EdgeInsets.only(right: 6),
                        child: ChoiceChip(
                          label: Text(label),
                          selected: isSelected,
                          selectedColor: AppColors.emeraldPrimary.withOpacity(0.15),
                          labelStyle: TextStyle(
                            color: isSelected ? AppColors.emeraldPrimary : Colors.grey.shade700,
                            fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                            fontSize: 12,
                          ),
                          onSelected: (_) {
                            ref.read(studentClassFilterProvider.notifier).state = cls;
                          },
                        ),
                      );
                    }).toList(),
                  ),
                ),
              ],
            ),
          ),

          // Total Count Banner
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: AppDimens.spacing16, vertical: 8),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'মোট শিক্ষার্থী: ${BengaliNumberFormatter.toBengali(filteredStudents.length.toString())} জন',
                  style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                ),
              ],
            ),
          ),

          // Students List
          Expanded(
            child: studentsAsync.when(
              loading: () => const Center(child: CircularProgressIndicator(color: AppColors.emeraldPrimary)),
              error: (err, _) => Center(child: Text('ত্রুটি: $err')),
              data: (_) {
                if (filteredStudents.isEmpty) {
                  return EmptyStateView(
                    title: 'কোনো শিক্ষার্থী পাওয়া যায়নি',
                    message: 'অনুগ্রহ করে অনুসন্ধান পরিবর্তন করুন অথবা নতুন শিক্ষার্থী ভর্তি করুন।',
                    actionLabel: 'শিক্ষার্থী ভর্তি করুন',
                    onAction: () => _showAdmitDialog(context, ref),
                  );
                }

                return ListView.builder(
                  padding: const EdgeInsets.all(AppDimens.spacing12),
                  itemCount: filteredStudents.length,
                  itemBuilder: (context, index) {
                    final student = filteredStudents[index];
                    return Padding(
                      padding: const EdgeInsets.only(bottom: AppDimens.spacing10),
                      child: StudentCard(
                        student: student,
                      ),
                    );
                  },
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
