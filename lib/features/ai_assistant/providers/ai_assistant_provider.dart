import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/khutbah_topic_model.dart';
import '../services/gemini_ai_service.dart';

final geminiAiServiceProvider = Provider<GeminiAiService>((ref) {
  return GeminiAiService();
});

class KhutbahState {
  final bool isLoading;
  final KhutbahTopicModel? topic;
  final String? error;

  const KhutbahState({
    this.isLoading = false,
    this.topic,
    this.error,
  });
}

class KhutbahNotifier extends StateNotifier<KhutbahState> {
  final GeminiAiService _service;

  KhutbahNotifier(this._service) : super(const KhutbahState()) {
    generateTopic(category: 'character');
  }

  Future<void> generateTopic({String prompt = '', String category = 'character'}) async {
    state = const KhutbahState(isLoading: true);
    try {
      final res = await _service.generateKhutbahOutline(
        topicPrompt: prompt,
        category: category,
      );
      if (!mounted) return;
      state = KhutbahState(isLoading: false, topic: res);
    } catch (e) {
      if (!mounted) return;
      state = KhutbahState(isLoading: false, error: e.toString());
    }
  }
}

final khutbahNotifierProvider =
    StateNotifierProvider<KhutbahNotifier, KhutbahState>((ref) {
  final service = ref.watch(geminiAiServiceProvider);
  return KhutbahNotifier(service);
});
