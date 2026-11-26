import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../shared/widgets/card_shell.dart';
import '../models/madrasa_attendance_model.dart';

/// Row widget for toggling an individual student's attendance status
class AttendanceToggleRow extends StatelessWidget {
  final MadrasaAttendanceModel record;
  final ValueChanged<String> onStatusChanged;

  const AttendanceToggleRow({
    super.key,
    required this.record,
    required this.onStatusChanged,
  });

  @override
  Widget build(BuildContext context) {
    return CardShell(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      child: Row(
        children: [
          // Student details
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  record.studentNameBn,
                  style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15),
                ),
                const SizedBox(height: 2),
                Text(
                  record.className,
                  style: TextStyle(fontSize: 11, color: Colors.grey.shade600),
                ),
              ],
            ),
          ),

          // 4 Status buttons: Present (P), Absent (A), Late (L), Leave (Lv)
          Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              _buildStatusButton(
                code: 'present',
                label: 'উপস্থিত',
                activeColor: AppColors.emeraldPrimary,
              ),
              const SizedBox(width: 4),
              _buildStatusButton(
                code: 'absent',
                label: 'অনুপস্থিত',
                activeColor: Colors.red.shade600,
              ),
              const SizedBox(width: 4),
              _buildStatusButton(
                code: 'late',
                label: 'বিলম্ব',
                activeColor: Colors.orange.shade700,
              ),
              const SizedBox(width: 4),
              _buildStatusButton(
                code: 'leave',
                label: 'ছুটি',
                activeColor: Colors.blue.shade600,
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildStatusButton({
    required String code,
    required String label,
    required Color activeColor,
  }) {
    final isSelected = record.status == code;

    return InkWell(
      onTap: () => onStatusChanged(code),
      borderRadius: BorderRadius.circular(6),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 6),
        decoration: BoxDecoration(
          color: isSelected ? activeColor : Colors.grey.withOpacity(0.1),
          borderRadius: BorderRadius.circular(6),
          border: Border.all(
            color: isSelected ? activeColor : Colors.transparent,
          ),
        ),
        child: Text(
          label,
          style: TextStyle(
            fontSize: 11,
            fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
            color: isSelected ? Colors.white : Colors.grey.shade700,
          ),
        ),
      ),
    );
  }
}
