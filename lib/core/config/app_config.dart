/// Global Application Configuration for Mosque Seba (মসজিদ সেবা)
class AppConfig {
  static const String appNameBn = 'মসজিদ সেবা';
  static const String appNameEn = 'Mosque Seba';
  static const String packageName = 'com.mosqueseba.app';
  static const String appVersion = '1.2.5';
  static const int buildNumber = 10;
  static const String tagline = 'মসজিদের খেদমতে আধুনিক প্রযুক্তি, পরিচালনায় স্বচ্ছ আস্থা';

  // Production Supabase Cloud Backend Configuration
  static const String defaultSupabaseUrl = 'https://donglyxtrfyzwgtrskhz.supabase.co';
  static const String defaultSupabaseAnonKey =
      'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRvbmdseXh0cmZ5endndHJza2h6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTQ5MTI4MTgsImV4cCI6MjExMDQ4ODgxOH0.0dVON58e2nsB6g2_UuMACtzvSJvPEcdOiBnjPovxcYU';

  // Gemini AI Model Target
  static const String geminiModel = 'gemini-1.5-flash';

  // Asset paths
  static const String logoAsset = 'assets/images/official_logo.png';
  static const String appLogoAsset = 'assets/images/app_logo.png';
  static const String fontHindSiliguri = 'HindSiliguri';
}
