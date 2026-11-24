import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:qr_flutter/qr_flutter.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_dimens.dart';
import '../../../../core/theme/app_typography.dart';
import '../../../../core/utils/bengali_number_formatter.dart';
import '../../../../shared/widgets/adaptive_scaffold.dart';
import '../../../../shared/widgets/app_button.dart';
import '../../../../shared/widgets/card_shell.dart';
import '../models/member_model.dart';

/// Screen displaying full member profile and QR verification badge
class MemberProfileScreen extends StatelessWidget {
  final MemberModel member;

  const MemberProfileScreen({super.key, required this.member});

  @override
  Widget build(BuildContext context) {
    return AdaptiveScaffold(
      title: 'সদস্য পরিচিতি',
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(AppDimens.spacing16),
        child: Column(
          children: [
            // Hero Profile Card
            CardShell(
              padding: const EdgeInsets.all(AppDimens.spacing20),
              child: Column(
                children: [
                  CircleAvatar(
                    radius: 40,
                    backgroundColor: AppColors.primaryContainer,
                    child: Text(
                      member.nameBn.isNotEmpty ? member.nameBn.characters.first : 'স',
                      style: AppTypography.headlineMedium.copyWith(
                        color: AppColors.primary,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                  const SizedBox(height: AppDimens.spacing12),
                  Text(
                    member.nameBn,
                    style: AppTypography.titleLarge.copyWith(fontWeight: FontWeight.bold),
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 4),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: AppColors.primary.withValues(alpha: 0.1),
                      borderRadius: BorderRadius.circular(AppDimens.radiusSm),
                      border: Border.all(color: AppColors.primary.withValues(alpha: 0.3)),
                    ),
                    child: Text(
                      'সদস্য কোড: ${BengaliNumberFormatter.toBengali(member.memberCode)}',
                      style: AppTypography.bodySmall.copyWith(
                        color: AppColors.primary,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                  const SizedBox(height: AppDimens.spacing16),
                  const Divider(),
                  const SizedBox(height: AppDimens.spacing12),
                  _buildProfileRow(Icons.phone, 'মোবাইল', BengaliNumberFormatter.toBengali(member.mobile)),
                  _buildProfileRow(Icons.location_on, 'গ্রাম / এলাকা', member.villageArea.isNotEmpty ? member.villageArea : 'উল্লেখ নেই'),
                  _buildProfileRow(Icons.payments, 'মাসিক চাঁদা অঙ্গীকার', BengaliNumberFormatter.formatCurrency(member.monthlyChanda)),
                  _buildProfileRow(Icons.credit_card, 'জাতীয় পরিচয়পত্র (NID)', member.nid.isNotEmpty ? BengaliNumberFormatter.toBengali(member.nid) : 'সংরক্ষিত নেই'),
                  _buildProfileRow(
                    Icons.restaurant,
                    'ইমাম-মুয়াজ্জিনের খাবার',
                    member.willingToProvideStaffMeals ? 'হ্যাঁ, দায়িত্ব গ্রহণে ইচ্ছুক' : 'না',
                    valueColor: member.willingToProvideStaffMeals ? AppColors.success : null,
                  ),
                ],
              ),
            ),
            const SizedBox(height: AppDimens.spacing16),
            // Digital Member QR Card
            CardShell(
              padding: const EdgeInsets.all(AppDimens.spacing20),
              child: Column(
                children: [
                  Text(
                    'ডিজিটাল সদস্যপত্র ও কিউআর কোড',
                    style: AppTypography.titleMedium.copyWith(fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: AppDimens.spacing8),
                  Text(
                    'মসজিদের চাঁদা পরিশোধ এবং পরিচিতি যাচাইয়ে এই কোড স্ক্যান করুন',
                    style: AppTypography.caption.copyWith(color: AppColors.textSecondary),
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: AppDimens.spacing16),
                  Container(
                    padding: const EdgeInsets.all(AppDimens.spacing12),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(AppDimens.radiusMd),
                      border: Border.all(color: AppColors.gold.withValues(alpha: 0.4), width: 1.5),
                    ),
                    child: QrImageView(
                      data: 'MOSQUE_SEBA:MEMBER:${member.memberCode}:${member.id}',
                      version: QrVersions.auto,
                      size: 160.0,
                    ),
                  ),
                  const SizedBox(height: AppDimens.spacing16),
                  AppButton(
                    text: 'চাঁদা গ্রহণ ও রসিদ তৈরি',
                    icon: Icons.receipt_long,
                    onPressed: () {
                      context.push('/chanda-ledger', extra: member);
                    },
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildProfileRow(IconData icon, String title, String value, {Color? valueColor}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, size: 18, color: AppColors.textSecondary),
          const SizedBox(width: AppDimens.spacing8),
          Text(
            title,
            style: AppTypography.bodyMedium.copyWith(color: AppColors.textSecondary),
          ),
          const Spacer(),
          Flexible(
            child: Text(
              value,
              style: AppTypography.bodyMedium.copyWith(
                fontWeight: FontWeight.bold,
                color: valueColor ?? AppColors.textPrimary,
              ),
              textAlign: TextAlign.right,
            ),
          ),
        ],
      ),
    );
  }
}
