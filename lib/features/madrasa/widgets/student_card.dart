import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../core/utils/bengali_number_formatter.dart';
import '../../../shared/widgets/card_shell.dart';
import '../models/student_model.dart';

/// Card Component displaying a Madrasa Student Summary
class StudentCard extends StatelessWidget {
  final StudentModel student;
  final VoidCallback? onTap;
  final VoidCallback? onTakeAttendance;
  final VoidCallback? onLogHifz;
  final VoidCallback? onCollectFee;

  const StudentCard({
    super.key,
    required this.student,
    this.onTap,
    this.onTakeAttendance,
    this.onLogHifz,
    this.onCollectFee,
  });

  Color _getClassColor(String className) {
    if (className.contains('হিফজ')) return AppColors.emeraldPrimary;
    if (className.contains('নাজেরা')) return AppColors.rolePresident;
    if (className.contains('নূরানী')) return AppColors.roleTreasurer;
    return AppColors.primary;
  }

  @override
  Widget build(BuildContext context) {
    final classColor = _getClassColor(student.className);

    return CardShell(
      onTap: onTap,
      padding: const EdgeInsets.all(AppDimens.spacing14),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Avatar with Initials
              CircleAvatar(
                radius: 24,
                backgroundColor: classColor.withOpacity(0.12),
                child: Text(
                  student.nameBn.isNotEmpty ? student.nameBn.characters.first : 'ম',
                  style: TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                    color: classColor,
                  ),
                ),
              ),
              const SizedBox(width: 12),
              // Name & Code
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      student.nameBn,
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      'রোল/আইডি: ${BengaliNumberFormatter.toBengali(student.studentCode)}',
                      style: TextStyle(
                        fontSize: 12,
                        color: Colors.grey.shade600,
                      ),
                    ),
                  ],
                ),
              ),
              // Class Badge
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: classColor.withOpacity(0.12),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: classColor.withOpacity(0.3)),
                ),
                child: Text(
                  student.className,
                  style: TextStyle(
                    fontSize: 11,
                    fontWeight: FontWeight.bold,
                    color: classColor,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          const Divider(height: 1),
          const SizedBox(height: 8),

          // Guardian & Financial Info
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  const Icon(Icons.family_restroom, size: 16, color: Colors.grey),
                  const SizedBox(width: 4),
                  Text(
                    'অভিভাবক: ${student.guardianName}',
                    style: const TextStyle(fontSize: 12),
                  ),
                ],
              ),
              Text(
                'ফি: ৳ ${BengaliNumberFormatter.toBengali(student.monthlyFee.toInt().toString())}',
                style: const TextStyle(
                  fontSize: 13,
                  fontWeight: FontWeight.bold,
                  color: AppColors.emeraldPrimary,
                ),
              ),
            ],
          ),
          const SizedBox(height: 4),
          Row(
            children: [
              const Icon(Icons.phone, size: 14, color: Colors.grey),
              const SizedBox(width: 4),
              Text(
                BengaliNumberFormatter.toBengali(student.guardianMobile),
                style: TextStyle(fontSize: 12, color: Colors.grey.shade700),
              ),
              if (student.bloodGroup.isNotEmpty) ...[
                const SizedBox(width: 12),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                  decoration: BoxDecoration(
                    color: Colors.red.shade50,
                    borderRadius: BorderRadius.circular(4),
                  ),
                  child: Text(
                    'রক্ত: ${student.bloodGroup}',
                    style: TextStyle(fontSize: 10, fontWeight: FontWeight.bold, color: Colors.red.shade700),
                  ),
                ),
              ],
            ],
          ),

          // Action Shortcuts
          const SizedBox(height: 10),
          Row(
            mainAxisAlignment: MainAxisAlignment.end,
            children: [
              if (onLogHifz != null && student.className.contains('হিফজ'))
                TextButton.icon(
                  onPressed: onLogHifz,
                  icon: const Icon(Icons.menu_book, size: 14),
                  label: const Text('হিফজ এন্ট্রি', style: TextStyle(fontSize: 11)),
                  style: TextButton.styleFrom(
                    foregroundColor: AppColors.emeraldPrimary,
                    padding: const EdgeInsets.symmetric(horizontal: 8),
                  ),
                ),
              if (onCollectFee != null)
                TextButton.icon(
                  onPressed: onCollectFee,
                  icon: const Icon(Icons.receipt, size: 14),
                  label: const Text('ফি আদায়', style: TextStyle(fontSize: 11)),
                  style: TextButton.styleFrom(
                    foregroundColor: AppColors.roleTreasurer,
                    padding: const EdgeInsets.symmetric(horizontal: 8),
                  ),
                ),
            ],
          ),
        ],
      ),
    );
  }
}
