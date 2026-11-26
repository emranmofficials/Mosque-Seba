import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_dimens.dart';
import '../../../shared/components/empty_state_view.dart';
import '../dialogs/add_event_dialog.dart';
import '../models/mosque_event_model.dart';
import '../providers/event_provider.dart';
import '../widgets/event_card.dart';

/// Screen listing All Mosque Events, Mahfils, and Religious Programs
class EventListScreen extends ConsumerWidget {
  const EventListScreen({super.key});

  void _showAddDialog(BuildContext context, WidgetRef ref) {
    showDialog(
      context: context,
      builder: (ctx) => AddEventDialog(
        onSubmit: (event) => ref.read(eventListProvider.notifier).createEvent(event),
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final eventsAsync = ref.watch(eventListProvider);
    final filteredEvents = ref.watch(filteredEventsProvider);
    final statusFilter = ref.watch(eventStatusFilterProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('অনুষ্ঠান ও মাহফিল ব্যবস্থাপনা'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () => ref.read(eventListProvider.notifier).loadEvents(),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        backgroundColor: AppColors.goldDeep,
        foregroundColor: Colors.white,
        icon: const Icon(Icons.add_circle_outline),
        label: const Text('নতুন অনুষ্ঠান', style: TextStyle(fontWeight: FontWeight.bold)),
        onPressed: () => _showAddDialog(context, ref),
      ),
      body: Column(
        children: [
          // Filter Tabs (সব, আসন্ন, সম্পন্ন)
          Container(
            color: Theme.of(context).cardColor,
            padding: const EdgeInsets.symmetric(horizontal: AppDimens.spacing14, vertical: 8),
            child: Row(
              children: [
                _buildFilterChip(ref, 'সকল মাহফিল', 'all', statusFilter),
                const SizedBox(width: 8),
                _buildFilterChip(ref, 'আসন্ন অনুষ্ঠান', 'upcoming', statusFilter),
                const SizedBox(width: 8),
                _buildFilterChip(ref, 'সম্পন্ন অনুষ্ঠান', 'completed', statusFilter),
              ],
            ),
          ),

          // Events List
          Expanded(
            child: eventsAsync.when(
              loading: () => const Center(child: CircularProgressIndicator(color: AppColors.goldDeep)),
              error: (err, _) => Center(child: Text('ত্রুটি: $err')),
              data: (_) {
                if (filteredEvents.isEmpty) {
                  return EmptyStateView(
                    title: 'কোনো অনুষ্ঠান বা মাহফিল নেই',
                    message: 'মসজিদের ওয়াজ মাহফিল, শবে কদর বা ইফতারের আয়োজন যুক্ত করতে চাপ দিন।',
                    actionLabel: 'অনুষ্ঠান যুক্ত করুন',
                    onAction: () => _showAddDialog(context, ref),
                  );
                }

                return ListView.builder(
                  padding: const EdgeInsets.all(AppDimens.spacing14),
                  itemCount: filteredEvents.length,
                  itemBuilder: (context, index) {
                    final event = filteredEvents[index];
                    return Padding(
                      padding: const EdgeInsets.only(bottom: AppDimens.spacing12),
                      child: EventCard(
                        event: event,
                        onDesignPoster: () {
                          // Jump directly to Islamic Dawat Poster AI with prefilled parameters
                          context.push('/poster-ai', extra: event);
                        },
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

  Widget _buildFilterChip(WidgetRef ref, String label, String value, String current) {
    final isSelected = current == value;
    return ChoiceChip(
      label: Text(label),
      selected: isSelected,
      selectedColor: AppColors.goldDeep.withOpacity(0.15),
      labelStyle: TextStyle(
        color: isSelected ? AppColors.goldDeep : Colors.grey.shade700,
        fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
        fontSize: 12,
      ),
      onSelected: (_) {
        ref.read(eventStatusFilterProvider.notifier).state = value;
      },
    );
  }
}
