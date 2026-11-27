import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/widgets/card_shell.dart';
import '../../authentication/providers/auth_provider.dart';
import '../../printing_pdf/services/native_printing_service.dart';
import '../models/exam_model.dart';
import '../providers/exam_provider.dart';

/// Screen presenting a comprehensive 360-degree Student Report Card with PDF Printing
class StudentReportCardScreen extends ConsumerWidget {
  final String studentId;
  final String studentNameBn;
  final String className;
  final String examTitle;
  final String? examId;

  const StudentReportCardScreen({
    super.key,
    required this.studentId,
    required this.studentNameBn,
    this.className = 'মাদরাসা বিভাগ',
    required this.examTitle,
    this.examId,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final resultsAsync = ref.watch(studentReportCardProvider(studentId));
    final mosqueName =
        ref.watch(authNotifierProvider).valueOrNull?.mosqueNameBn ?? '';
    final printingService = NativePrintingService();

    return Scaffold(
      appBar: AppBar(
        title: const Text('শিক্ষার্থীর ফলাফল পত্র'),
        actions: [
          IconButton(
            icon: const Icon(Icons.print_outlined),
            tooltip: 'রিপোর্ট কার্ড প্রিন্ট / PDF',
            onPressed: () {
              final allResults = resultsAsync.valueOrNull ?? const <ExamResultModel>[];
              final filtered = examId != null
                  ? allResults.where((r) => r.examId == examId).toList()
                  : allResults;
              printingService.printReportCard(
                examName: examTitle,
                studentName: studentNameBn,
                className: filtered.isNotEmpty ? filtered.first.className : className,
                results: filtered,
                mosqueName: mosqueName.isNotEmpty
                    ? '$mosqueName — মাদরাসা বিভাগ'
                    : 'মসজিদ সেবা মাদরাসা ও মক্তব',
              );
            },
          ),
        ],
      ),
      body: resultsAsync.when(
        loading: () => const Center(
          child: CircularProgressIndicator(color: AppColors.emeraldPrimary),
        ),
        error: (err, _) => Center(child: Text('ত্রুটি: $err')),
        data: (allResults) {
          final results = examId != null
              ? allResults.where((r) => r.examId == examId).toList()
              : allResults;

          final totalObtained =
              results.fold<double>(0.0, (acc, r) => acc + r.marksObtained);
          final maxTotal =
              results.fold<double>(0.0, (acc, r) => acc + r.totalMarks);
          final avgPercentage =
              maxTotal > 0 ? (totalObtained / maxTotal) * 100 : 0.0;
          final overallGrade = maxTotal > 0
              ? ExamResultModel.calculateIslamicGrade(totalObtained, maxTotal)
              : 'নম্বর এন্ট্রি নেই';
          final resolvedClass =
              results.isNotEmpty ? results.first.className : className;

          return SingleChildScrollView(
            padding: const EdgeInsets.all(AppDimens.spacing16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
              // Header Card
              CardShell(
                padding: const EdgeInsets.all(AppDimens.spacing16),
                child: Column(
                  children: [
                    Text(
                      mosqueName.isNotEmpty
                          ? '$mosqueName — মাদরাসা ও মক্তব'
                          : 'মসজিদ সেবা মাদরাসা ও মক্তব',
                      style: const TextStyle(
                        fontSize: 17,
                        fontWeight: FontWeight.bold,
                        color: AppColors.emeraldPrimary,
                      ),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 2),
                    Text(
                      examTitle,
                      style: const TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    const SizedBox(height: 12),
                    const Divider(height: 1),
                    const SizedBox(height: 12),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                'শিক্ষার্থী: $studentNameBn',
                                style: const TextStyle(
                                  fontWeight: FontWeight.bold,
                                  fontSize: 15,
                                ),
                              ),
                              const SizedBox(height: 2),
                              Text(
                                'শ্রেণী: $resolvedClass',
                                style: const TextStyle(
                                  fontSize: 12,
                                  color: Colors.grey,
                                ),
                              ),
                            ],
                          ),
                        ),
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 10,
                            vertical: 6,
                          ),
                          decoration: BoxDecoration(
                            color: AppColors.emeraldPrimary.withOpacity(0.15),
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: Text(
                            overallGrade,
                            style: const TextStyle(
                              fontSize: 13,
                              fontWeight: FontWeight.bold,
                              color: AppColors.emeraldPrimary,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 16),

              // Subject Wise Marks Table Card
              CardShell(
                padding: const EdgeInsets.all(AppDimens.spacing14),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'বিষয়ভিত্তিক পরীক্ষার ফলাফল',
                      style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 12),
                    if (results.isEmpty)
                      const Padding(
                        padding: EdgeInsets.symmetric(vertical: 16),
                        child: Center(
                          child: Text(
                            'এই শিক্ষার্থীর জন্য এখনো কোনো বিষয়ের নম্বর যুক্ত করা হয়নি।',
                            style: TextStyle(fontSize: 12, color: Colors.grey),
                          ),
                        ),
                      )
                    else
                      Table(
                        border: TableBorder.all(
                          color: Colors.grey.shade300,
                          width: 1,
                          borderRadius: BorderRadius.circular(6),
                        ),
                        columnWidths: const {
                          0: FlexColumnWidth(3),
                          1: FlexColumnWidth(1.5),
                          2: FlexColumnWidth(2),
                        },
                        children: [
                          TableRow(
                            decoration: BoxDecoration(
                              color: Colors.grey.withOpacity(0.1),
                            ),
                            children: const [
                              Padding(
                                padding: EdgeInsets.all(8.0),
                                child: Text(
                                  'বিষয়',
                                  style: TextStyle(
                                    fontWeight: FontWeight.bold,
                                    fontSize: 12,
                                  ),
                                ),
                              ),
                              Padding(
                                padding: EdgeInsets.all(8.0),
                                child: Text(
                                  'প্রাপ্ত নম্বর',
                                  style: TextStyle(
                                    fontWeight: FontWeight.bold,
                                    fontSize: 12,
                                  ),
                                  textAlign: TextAlign.center,
                                ),
                              ),
                              Padding(
                                padding: EdgeInsets.all(8.0),
                                child: Text(
                                  'গ্রেড / মান',
                                  style: TextStyle(
                                    fontWeight: FontWeight.bold,
                                    fontSize: 12,
                                  ),
                                  textAlign: TextAlign.center,
                                ),
                              ),
                            ],
                          ),
                          ...results.map((r) {
                            return TableRow(
                              children: [
                                Padding(
                                  padding: const EdgeInsets.all(8.0),
                                  child: Text(
                                    r.subjectName,
                                    style: const TextStyle(fontSize: 12),
                                  ),
                                ),
                                Padding(
                                  padding: const EdgeInsets.all(8.0),
                                  child: Text(
                                    BengaliNumberFormatter.toBengali(
                                      r.marksObtained.toInt().toString(),
                                    ),
                                    style: const TextStyle(
                                      fontWeight: FontWeight.bold,
                                      fontSize: 12,
                                    ),
                                    textAlign: TextAlign.center,
                                  ),
                                ),
                                Padding(
                                  padding: const EdgeInsets.all(8.0),
                                  child: Text(
                                    r.grade,
                                    style: const TextStyle(
                                      fontSize: 11,
                                      color: AppColors.emeraldPrimary,
                                      fontWeight: FontWeight.w600,
                                    ),
                                    textAlign: TextAlign.center,
                                  ),
                                ),
                              ],
                            );
                          }),
                        ],
                      ),
                    const SizedBox(height: 14),

                    // Total & Average
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          'মোট প্রাপ্ত নম্বর: ${BengaliNumberFormatter.toBengali(totalObtained.toInt().toString())} / ${BengaliNumberFormatter.toBengali(maxTotal.toInt().toString())}',
                          style: const TextStyle(
                            fontWeight: FontWeight.bold,
                            fontSize: 13,
                          ),
                        ),
                        Text(
                          'শতকরা: ${BengaliNumberFormatter.toBengali(avgPercentage.toInt().toString())}%',
                          style: const TextStyle(
                            fontWeight: FontWeight.bold,
                            fontSize: 13,
                            color: AppColors.emeraldPrimary,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 16),

              // Teacher & Principal Signatures Section
              CardShell(
                padding: const EdgeInsets.all(AppDimens.spacing16),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Column(
                      children: [
                        const SizedBox(height: 30),
                        Container(width: 100, height: 1, color: Colors.grey),
                        const SizedBox(height: 4),
                        const Text('শ্রেণী শিক্ষক', style: TextStyle(fontSize: 12)),
                      ],
                    ),
                    Column(
                      children: [
                        const SizedBox(height: 30),
                        Container(width: 100, height: 1, color: Colors.grey),
                        const SizedBox(height: 4),
                        const Text(
                          'মুহতামিম / প্রধান শিক্ষক',
                          style: TextStyle(fontSize: 12),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ],
          ),
        );
        },
      ),
    );
  }
}
