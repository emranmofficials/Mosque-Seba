import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/utils/bengali_number_formatter.dart';
import '../../../../shared/widgets/card_shell.dart';
import '../models/janaza_alert_model.dart';

/// Card component presenting an emergency Janaza alert with Islamic styling
class JanazaAlertCard extends StatelessWidget {
  final JanazaAlertModel alert;
  final String mosqueName;

  const JanazaAlertCard({
    super.key,
    required this.alert,
    this.mosqueName = 'মসজিদ সেবা জামে মসজিদ',
  });

  void _copyToClipboard(BuildContext context) {
    final message = alert.toBroadcastMessage(mosqueName);
    Clipboard.setData(ClipboardData(text: message));
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('জানাজার শোক সংবাদ কপি করা হয়েছে (শেয়ারের জন্য প্রস্তুত)')),
    );
  }

  @override
  Widget build(BuildContext context) {
    return CardShell(
      padding: const EdgeInsets.all(AppDimens.spacing16),
      border: Border.all(color: AppColors.error.withValues(alpha: 0.5), width: 1.5),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Inna Lillahi Banner
          Container(
            width: double.infinity,
            padding: const EdgeInsets.symmetric(vertical: 6),
            decoration: BoxDecoration(
              color: AppColors.error.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(AppDimens.radiusSm),
            ),
            child: const Text(
              'إِنَّا لِلَّهِ وَإِنَّا إِلَيْهِ رَاجِعُونَ',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: AppColors.error,
              ),
              textAlign: TextAlign.center,
            ),
          ),
          const SizedBox(height: AppDimens.spacing12),
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      alert.deceasedName,
                      style: AppTypography.titleLarge.copyWith(fontWeight: FontWeight.bold),
                    ),
                    if (alert.guardianName.isNotEmpty) ...[
                      const SizedBox(height: 2),
                      Text(
                        '(${alert.relativeRelation}: ${alert.guardianName})',
                        style: AppTypography.bodySmall.copyWith(color: AppColors.textSecondary),
                      ),
                    ],
                  ],
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(
                  color: AppColors.error,
                  borderRadius: BorderRadius.circular(4),
                ),
                child: const Text(
                  'জরুরি জানাজা',
                  style: TextStyle(color: Colors.white, fontSize: 11, fontWeight: FontWeight.bold),
                ),
              ),
            ],
          ),
          const SizedBox(height: AppDimens.spacing12),
          const Divider(),
          const SizedBox(height: AppDimens.spacing8),
          _buildInfoRow(Icons.place, 'জানাজার স্থান', alert.janazaVenue),
          _buildInfoRow(Icons.access_time, 'জানাজার সময়', alert.janazaTime),
          if (alert.graveyardVenue.isNotEmpty)
            _buildInfoRow(Icons.brightness_2_outlined, 'দাফনের স্থান', alert.graveyardVenue),
          if (alert.contactPhone.isNotEmpty)
            _buildInfoRow(Icons.phone, 'যোগাযোগ', BengaliNumberFormatter.toBengali(alert.contactPhone)),
          const SizedBox(height: AppDimens.spacing12),
          Row(
            mainAxisAlignment: MainAxisAlignment.end,
            children: [
              OutlinedButton.icon(
                icon: const Icon(Icons.copy, size: 16),
                label: const Text('বার্তা কপি করুন'),
                onPressed: () => _copyToClipboard(context),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildInfoRow(IconData icon, String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 3),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, size: 15, color: AppColors.primary),
          const SizedBox(width: 6),
          Text(
            '$label: ',
            style: AppTypography.caption.copyWith(fontWeight: FontWeight.bold, color: AppColors.textSecondary),
          ),
          Expanded(
            child: Text(
              value,
              style: AppTypography.bodySmall.copyWith(fontWeight: FontWeight.w600),
            ),
          ),
        ],
      ),
    );
  }
}
