import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/prayer_time_item.dart';
import '../services/prayer_calculator_service.dart';

final prayerTimeStreamProvider =
    StreamProvider<({List<PrayerTimeItem> schedule, NextPrayerSummary nextSummary})>((ref) {
  final controller = StreamController<
      ({List<PrayerTimeItem> schedule, NextPrayerSummary nextSummary})>();

  void emitLatest() {
    if (!controller.isClosed) {
      controller.add(PrayerCalculatorService.computeTodayPrayerSchedule());
    }
  }

  emitLatest();

  // Refresh every 30 seconds to keep live countdown accurate
  final timer = Timer.periodic(const Duration(seconds: 30), (_) {
    emitLatest();
  });

  ref.onDispose(() {
    timer.cancel();
    controller.close();
  });

  return controller.stream;
});
