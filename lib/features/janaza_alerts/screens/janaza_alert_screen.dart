import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../shared/components/empty_state_view.dart';
import '../../../shared/components/error_state_view.dart';
import '../../../shared/widgets/adaptive_scaffold.dart';
import '../../authentication/providers/auth_provider.dart';
import '../dialogs/broadcast_janaza_dialog.dart';
import '../providers/janaza_provider.dart';
import '../widgets/janaza_alert_card.dart';

/// Screen presenting Community Emergency Janaza Announcements (জরুরি জানাজা এলার্ট বোর্ড)
class JanazaAlertScreen extends ConsumerWidget {
  const JanazaAlertScreen({super.key});

  void _showBroadcastDialog(BuildContext context, WidgetRef ref) {
    final authState = ref.read(authNotifierProvider);
    final author = authState.valueOrNull?.memberName ?? 'পেশ ইমাম';

    showDialog(
      context: context,
      builder: (ctx) => BroadcastJanazaDialog(
        mosqueName: 'মসজিদ সেবা জামে মসজিদ',
        onBroadcast: (alert) => ref.read(janazaListProvider.notifier).broadcastAlert(alert, author),
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final alertsAsync = ref.watch(janazaListProvider);

    return AdaptiveScaffold(
      title: 'জরুরি জানাজা এলার্ট',
      floatingActionButton: FloatingActionButton.extended(
        backgroundColor: AppColors.error,
        foregroundColor: Colors.white,
        icon: const Icon(Icons.add_alert_outlined),
        label: const Text('জানাজা সম্প্রচার', style: TextStyle(fontWeight: FontWeight.bold)),
        onPressed: () => _showBroadcastDialog(context, ref),
      ),
      body: alertsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator(color: AppColors.primary)),
        error: (err, _) => ErrorStateView(
          title: 'তথ্য লোড করতে সমস্যা হয়েছে',
          message: err.toString(),
          onRetry: () => ref.read(janazaListProvider.notifier).loadAlerts(),
        ),
        data: (alerts) {
          if (alerts.isEmpty) {
            return EmptyStateView(
              title: 'কোনো সক্রিয় জানাজা সংবাদ নেই',
              message: 'এলাকার কোনো মুসল্লি ইন্তেকাল করলে তাৎক্ষণিক জানাজা সম্প্রচার করতে নিচে চাপ দিন।',
              actionLabel: 'জানাজা সম্প্রচার করুন',
              onAction: () => _showBroadcastDialog(context, ref),
            );
          }

          return ListView.separated(
            padding: const EdgeInsets.all(AppDimens.spacing16),
            itemCount: alerts.length,
            separatorBuilder: (_, __) => const SizedBox(height: 12),
            itemBuilder: (context, index) {
              final alert = alerts[index];
              return JanazaAlertCard(alert: alert);
            },
          );
        },
      ),
    );
  }
}
