import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:google_generative_ai/google_generative_ai.dart';
import '../../../core/config/app_config.dart';
import '../../../core/network/supabase_service.dart';
import '../models/khutbah_topic_model.dart';

/// Gemini AI Service for Islamic Khutbah Ideas and Official Bengali Notice Drafting
class GeminiAiService {
  final String _apiKey;

  GeminiAiService({String? apiKey})
      : _apiKey = apiKey ?? AppConfig.geminiApiKey;

  Future<String?> _generateText(String prompt) async {
    if (SupabaseService.isInitialized && SupabaseService.hasActiveSession) {
      try {
        final res = await SupabaseService.client.functions.invoke(
          'gemini-proxy',
          body: {'prompt': prompt},
        );
        final data = res.data;
        if (data is Map && data['text'] != null) {
          final text = data['text'].toString().trim();
          if (text.isNotEmpty) return text;
        }
      } catch (e) {
        debugPrint('Supabase gemini-proxy fallback: $e');
      }
    }

    if (_apiKey.isNotEmpty) {
      try {
        final model = GenerativeModel(
          model: AppConfig.geminiModel,
          apiKey: _apiKey,
        );
        final response = await model.generateContent([Content.text(prompt)]);
        final text = response.text?.trim();
        if (text != null && text.isNotEmpty) return text;
      } catch (e) {
        debugPrint('Direct Gemini fallback: $e');
      }
    }
    return null;
  }

  /// Generate Khutbah Topic Outline with Quranic & Hadith citations
  Future<KhutbahTopicModel> generateKhutbahOutline({
    required String topicPrompt,
    String category = 'character',
  }) async {
    try {
      final prompt = '''
বাংলা ভাষায় জুমার খুতবার জন্য একটি সংক্ষিপ্ত ও প্রামাণ্য রূপরেখা JSON ফরম্যাটে লিখুন।
ক্যাটাগরি: $category
বিষয়/নির্দেশনা: ${topicPrompt.isNotEmpty ? topicPrompt : category}

শুধুমাত্র নিচের JSON কীগুলো ব্যবহার করুন:
{
  "title": "খুতবার শিরোনাম",
  "quranicAyat": "সংশ্লিষ্ট কুরআনের আয়াত ও সূরার রেফারেন্স",
  "hadithReference": "সংশ্লিষ্ট সহীহ হাদিস ও কিতাবের রেফারেন্স",
  "mainPoints": ["১. পয়েন্ট", "২. পয়েন্ট", "৩. পয়েন্ট", "৪. পয়েন্ট"],
  "practicalGuidance": "মুসল্লিদের জন্য বাস্তব করণীয়"
}
''';
      final rawText = await _generateText(prompt) ?? '';
      final cleaned = _stripJsonFences(rawText);
      if (cleaned.isNotEmpty) {
        final parsed = jsonDecode(cleaned) as Map<String, dynamic>;
        final pointsRaw = parsed['mainPoints'] as List<dynamic>? ?? [];
        return KhutbahTopicModel(
          id: 'khutbah-${DateTime.now().millisecondsSinceEpoch}',
          title: parsed['title'] as String? ?? topicPrompt,
          category: category,
          quranicAyat: parsed['quranicAyat'] as String? ?? '',
          hadithReference: parsed['hadithReference'] as String? ?? '',
          mainPoints: pointsRaw.map((e) => e.toString()).toList(),
          practicalGuidance: parsed['practicalGuidance'] as String? ?? '',
        );
      }
    } catch (e) {
      debugPrint('Gemini khutbah generation fallback: $e');
    }

    return _fallbackKhutbahOutline(
      topicPrompt: topicPrompt,
      category: category,
    );
  }

  String _stripJsonFences(String text) {
    var trimmed = text.trim();
    if (trimmed.startsWith('```')) {
      trimmed = trimmed.replaceFirst(RegExp(r'^```(?:json)?\s*'), '');
      trimmed = trimmed.replaceFirst(RegExp(r'\s*```$'), '');
    }
    return trimmed.trim();
  }

  KhutbahTopicModel _fallbackKhutbahOutline({
    required String topicPrompt,
    required String category,
  }) {
    if (category == 'halal_earnings') {
      return const KhutbahTopicModel(
        id: 'khutbah-halal',
        title: 'হালাল জীবিকা ও ব্যবসায় সততার গুরুত্ব',
        category: 'halal_earnings',
        quranicAyat: '“হে মুমিনগণ, তোমরা নিজেদের মধ্যে একে অন্যের ধন-সম্পদ অন্যায়ভাবে গ্রাস করো না...” — (সূরা আন-নিসা: ২৯)',
        hadithReference: '“সত্যবাদী ও আমানতদার ব্যবসায়ী কিয়ামতের দিন নবী, সত্যবাদী ও শহীদগণের সঙ্গে থাকবেন।” — (তিরমিযী: ১২০৯)',
        mainPoints: [
          '১. হালাল রুজির বরকত ও ইবাদত কবুলের পূর্বশর্ত',
          '২. ওজনে কম দেওয়া ও খাদ্যে ভেজালের পরিণাম',
          '৩. কর্মচারীদের প্রাপ্য পারিশ্রমিক যথাসময়ে পরিশোধের তাগিদ',
          '৪. সুদমুক্ত লেনদেন ও সমাজে ইনসাফ প্রতিষ্ঠা',
        ],
        practicalGuidance: 'ব্যবসায়ীদের প্রতি আহ্বান: মুনাফা অর্জনের চেয়ে বরকত ও মানুষের কল্যাণকে প্রাধান্য দিন। কর্মচারীদের ঘাম শুকানোর পূর্বেই মজুরি দিন।',
      );
    }

    if (category == 'charity') {
      return const KhutbahTopicModel(
        id: 'khutbah-charity',
        title: 'আল্লাহর পথে ব্যয় ও যাকাতের সামাজিক তাৎপর্য',
        category: 'charity',
        quranicAyat: '“যারা নিজেদের ধন-সম্পদ আল্লাহর পথে ব্যয় করে, তাদের উপমা একটি শস্যবীজ...” — (সূরা আল-বাক্বারা: ২৬১)',
        hadithReference: '“দানশীল ব্যক্তি আল্লাহর নিকটবর্তী, জান্নাতের নিকটবর্তী এবং জাহান্নাম থেকে দূরবর্তী।” — (তিরমিযী: ১৯৬১)',
        mainPoints: [
          '১. যাকাত কোনো অনুকম্পা নয়, বরং দুস্থের ন্যায্য অধিকার',
          '২. গোপনীয় ও প্রকাশ্য সদকার ফযিলত',
          '৩. কৃপণতার ভয়াবহ পরিণতি ও সম্পদের পবিত্রতা',
          '৪. সমাজের দরিদ্র ও এতিমদের পুনর্বাসনে মসজিদের ভূমিকা',
        ],
        practicalGuidance: 'প্রতিটি স্বচ্ছল মুসলিমের উচিত হিসাব করে পূর্ণ যাকাত আদায় করা এবং প্রতিবেশীদের খোঁজখবর রাখা।',
      );
    }

    // Default Topic
    return KhutbahTopicModel(
      id: 'khutbah-default',
      title: topicPrompt.isNotEmpty ? topicPrompt : 'উত্তম চরিত্র ও সুন্দর ব্যবহারের মর্যাদা',
      category: category,
      quranicAyat: '“নিশ্চয়ই আপনি মহান চরিত্রের ওপর অধিষ্ঠিত।” — (সূরা আল-কলম: ৪)',
      hadithReference: '“তোমাদের মধ্যে সর্বোত্তম ব্যক্তি সে, যার চরিত্র সবচেয়ে সুন্দর।” — (সহীহ বুখারী: ৬০২৯)',
      mainPoints: const [
        '১. ইসলামের দৃষ্টিতে আখলাক বা চরিত্রের অনন্য মর্যাদা',
        '২. পিতা-মাতা, প্রতিবেশী ও আত্মীয়দের সাথে সদাচরণ',
        '৩. ক্রোধ নিয়ন্ত্রণ ও ক্ষমা প্রদর্শনের মহত্ত্ব',
        '৪. ভাষা ও বাক্যালাপে শালীনতা বজায় রাখা',
      ],
      practicalGuidance: 'প্রতিদিনের জীবনে ছোটদের স্নেহ, বড়দের শ্রদ্ধা এবং মানুষের সাথে হাসিমুখে কথা বলে সুন্নাহর প্রতিফলন ঘটানো।',
    );
  }

  /// Draft formal administrative notice in elegant Bengali
  Future<String> draftAdministrativeNotice({
    required String topic,
    required String audience, // 'সাধারণ মুসল্লিবৃন্দ', 'কমিটি সদস্যবৃন্দ'
    required String mosqueName,
  }) async {
    final prompt =
        '$mosqueName পরিচালনা কমিটির পক্ষ থেকে "$audience"-এর উদ্দেশ্যে "$topic" বিষয়ে একটি সংক্ষিপ্ত, মার্জিত ও আনুষ্ঠানিক বাংলা নোটিশ খসড়া করুন।';
    final text = await _generateText(prompt);
    if (text != null && text.isNotEmpty) {
      return text;
    }

    return '''
সম্মানিত $audience,
আসসালামু আলাইকুম ওয়া রাহমাতুল্লাহ।

$mosqueName পরিচালনা কমিটির পক্ষ থেকে জানানো যাচ্ছে যে, $topic। 

উক্ত বিষয়ে সকলের সার্বিক সহযোগিতা ও দোয়া কামনা করছি। আল্লাহ সুবহানাহু ওয়া তাআলা আমাদের সকল নেক প্রচেষ্টা কবুল করুন।

বিনীত,
সাধারণ সম্পাদক ও সভাপতি
$mosqueName পরিচালনা কমিটি
''';
  }
}
