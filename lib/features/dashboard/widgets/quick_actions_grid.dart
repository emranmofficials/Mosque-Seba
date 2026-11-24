import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../shared/widgets/card_shell.dart';

class QuickActionItem {
  final String title;
  final IconData icon;
  final Color color;
  final VoidCallback onTap;

  const QuickActionItem({
    required this.title,
    required this.icon,
    required this.color,
    required this.onTap,
  });
}

/// 4-Grid Quick Actions Panel for Direct Administrative Operations
class QuickActionsGrid extends StatelessWidget {
  final VoidCallback onReceiveChanda;
  final VoidCallback onAddTransaction;
  final VoidCallback onCashboxCounter;
  final VoidCallback onPublishNotice;

  const QuickActionsGrid({
    super.key,
    required this.onReceiveChanda,
    required this.onAddTransaction,
    required this.onCashboxCounter,
    required this.onPublishNotice,
  });

  @override
  Widget build(BuildContext context) {
    final actions = [
      QuickActionItem(
        title: 'চাঁদা আদায়',
        icon: Icons.receipt_long_outlined,
        color: AppColors.emeraldPrimary,
        onTap: onReceiveChanda,
      ),
      QuickActionItem(
        title: 'আয়-ব্যয় এন্ট্রি',
        icon: Icons.add_card_outlined,
        color: AppColors.goldDeep,
        onTap: onAddTransaction,
      ),
      QuickActionItem(
        title: 'জুমার দানবাক্স',
        icon: Icons.inventory_2_outlined,
        color: AppColors.roleTreasurer,
        onTap: onCashboxCounter,
      ),
      QuickActionItem(
        title: 'বিজ্ঞপ্তি প্রকাশ',
        icon: Icons.campaign_outlined,
        color: AppColors.rolePublicity,
        onTap: onPublishNotice,
      ),
    ];

    return Row(
      children: actions.map((act) {
        return Expanded(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 4),
            child: CardShell(
              onTap: act.onTap,
              padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 6),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Container(
                    padding: const EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      color: act.color.withOpacity(0.12),
                    ),
                    child: Icon(act.icon, size: 20, color: act.color),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    act.title,
                    textAlign: TextAlign.center,
                    style: const TextStyle(
                      fontSize: 11,
                      fontWeight: FontWeight.bold,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                ],
              ),
            ),
          ),
        );
      }).toList(),
    );
  }
}
