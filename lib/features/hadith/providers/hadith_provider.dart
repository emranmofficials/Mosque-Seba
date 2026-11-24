import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/hadith_item.dart';
import '../services/hadith_service.dart';

final todayHadithProvider = Provider<HadithItem>((ref) {
  return HadithService.getTodayHadith();
});
