import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/router/app_router.dart';
import '../../../core/theme/app_dimens.dart';
import '../../role_permissions/models/committee_role.dart';
import '../providers/auth_provider.dart';

/// Dialog allowing users to preview any of the 18 role interfaces in Demo mode.
class DemoRolePickerDialog extends ConsumerWidget {
  const DemoRolePickerDialog({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;
    final textTheme = theme.textTheme;

    return AlertDialog(
      title: Text(
        'ডেমো মোড — রোল নির্বাচন করুন',
        style: textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold),
      ),
      content: SizedBox(
        width: double.maxFinite,
        height: 420,
        child: ListView.separated(
          itemCount: CommitteeRole.values.length,
          separatorBuilder: (_, __) => const Divider(height: 1),
          itemBuilder: (context, index) {
            final role = CommitteeRole.values[index];
            return ListTile(
              dense: true,
              contentPadding: const EdgeInsets.symmetric(
                horizontal: MosqueSpace.xs,
                vertical: 2,
              ),
              leading: CircleAvatar(
                radius: 16,
                backgroundColor: colorScheme.primaryContainer,
                foregroundColor: colorScheme.onPrimaryContainer,
                child: Text(
                  '${index + 1}',
                  style: textTheme.labelSmall?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              title: Text(
                role.titleBn,
                style: textTheme.bodyMedium?.copyWith(
                  fontWeight: FontWeight.w600,
                ),
              ),
              subtitle: Text(
                role.shortSummaryBn,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                style: textTheme.bodySmall?.copyWith(
                  color: colorScheme.onSurfaceVariant,
                ),
              ),
              trailing: const Icon(Icons.chevron_right, size: 18),
              onTap: () {
                ref.read(authNotifierProvider.notifier).enterDemoMode(role);
                Navigator.of(context).pop();
                context.go(AppRouter.dashboard);
              },
            );
          },
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('বন্ধ করুন'),
        ),
      ],
    );
  }
}
