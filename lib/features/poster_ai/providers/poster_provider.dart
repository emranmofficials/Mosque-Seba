import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../events/models/mosque_event_model.dart';
import '../models/poster_design_model.dart';
import '../services/poster_ai_service.dart';

final posterAiServiceProvider = Provider<PosterAiService>((ref) {
  return PosterAiService();
});

class PosterState {
  final bool isGenerating;
  final PosterDesignModel design;
  final String? errorMessage;

  const PosterState({
    this.isGenerating = false,
    required this.design,
    this.errorMessage,
  });

  PosterState copyWith({
    bool? isGenerating,
    PosterDesignModel? design,
    String? errorMessage,
  }) {
    return PosterState(
      isGenerating: isGenerating ?? this.isGenerating,
      design: design ?? this.design,
      errorMessage: errorMessage,
    );
  }
}

class PosterNotifier extends StateNotifier<PosterState> {
  final PosterAiService _service;

  PosterNotifier(this._service)
      : super(
          PosterState(
            design: const PosterDesignModel(
              eventTitle: 'ঐতিহাসিক বাৎসরিক তাফসিরুল কুরআন ও ওয়াজ মাহফিল',
              eventDate: '১৫ই ডিসেম্বর ২০২৬',
            ),
          ),
        );

  void initFromEvent(MosqueEventModel event) {
    state = state.copyWith(
      design: state.design.copyWith(
        eventTitle: event.title,
        chiefGuest: event.chiefGuest.isNotEmpty ? event.chiefGuest : state.design.chiefGuest,
        venue: event.venue.isNotEmpty ? event.venue : state.design.venue,
        eventDate: event.eventDate,
        eventTime: event.eventTime,
      ),
    );
  }

  void updateTheme(String theme) {
    state = state.copyWith(
      design: state.design.copyWith(themeStyle: theme),
    );
  }

  void updateDesign(PosterDesignModel updated) {
    state = state.copyWith(design: updated);
  }

  Future<void> generateWithAi({
    required String prompt,
    String? eventType,
    String? venue,
    String? date,
  }) async {
    state = state.copyWith(isGenerating: true);
    try {
      final generated = await _service.generatePosterCopy(
        prompt: prompt,
        eventType: eventType,
        venue: venue,
        date: date,
      );
      state = state.copyWith(isGenerating: false, design: generated);
    } catch (e) {
      state = state.copyWith(isGenerating: false, errorMessage: e.toString());
    }
  }
}

final posterNotifierProvider =
    StateNotifierProvider<PosterNotifier, PosterState>((ref) {
  final service = ref.watch(posterAiServiceProvider);
  return PosterNotifier(service);
});
