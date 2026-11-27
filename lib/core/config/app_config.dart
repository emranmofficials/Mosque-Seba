/// Global Application Configuration for Mosque Seba (মসজিদ সেবা)
class AppConfig {
  static const String appNameBn = 'মসজিদ সেবা';
  static const String appNameEn = 'Mosque Seba';
  static const String packageName = 'com.mosqueseba.app';
  static const String appVersion = '1.3.1';
  static const int buildNumber = 12;
  static const String tagline = 'মসজিদের খেদমতে আধুনিক প্রযুক্তি, পরিচালনায় স্বচ্ছ আস্থা';

  // Production Supabase Cloud Backend Configuration (overrideable via --dart-define)
  static const String defaultSupabaseUrl = String.fromEnvironment(
    'SUPABASE_URL',
    defaultValue: 'https://donglyxtrfyzwgtrskhz.supabase.co',
  );
  static const String defaultSupabaseAnonKey = String.fromEnvironment(
    'SUPABASE_ANON_KEY',
    defaultValue:
        'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRvbmdseXh0cmZ5endndHJza2h6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTQ5MTI4MTgsImV4cCI6MjExMDQ4ODgxOH0.0dVON58e2nsB6g2_UuMACtzvSJvPEcdOiBnjPovxcYU',
  );

  // Gemini AI Configuration (inject GEMINI_API_KEY via --dart-define)
  static const String geminiApiKey = String.fromEnvironment('GEMINI_API_KEY');
  static const String geminiModel = String.fromEnvironment(
    'GEMINI_MODEL',
    defaultValue: 'gemini-3.5-flash-lite',
  );

  // Asset paths
  static const String logoAsset = 'assets/images/official_logo.png';
  static const String appLogoAsset = 'assets/images/app_logo.png';
  static const String fontHindSiliguri = 'HindSiliguri';
}
