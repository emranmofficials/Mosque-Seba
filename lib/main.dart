import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'app.dart';
import 'bootstrap/app_bootstrap.dart';

void main() async {
  // Initialize foundational services, storage, and platform UI
  final container = await AppBootstrap.initialize();

  runApp(
    UncontrolledProviderScope(
      container: container,
      child: const MosqueSebaApp(),
    ),
  );
}
