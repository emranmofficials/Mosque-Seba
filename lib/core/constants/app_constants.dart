/// Global Constants for Mosque Seba
class AppConstants {
  // Financial Approval Thresholds (BDT)
  static const double approvalThresholdTier1 = 5000.0;
  static const double approvalThresholdTier2 = 20000.0;

  // Responsive Breakpoints (dp)
  static const double tabletBreakpoint = 600.0;
  static const double desktopBreakpoint = 1024.0;

  // Cashbox Standard Banknote Denominations (BDT)
  static const List<int> cashboxDenominations = [1000, 500, 200, 100, 50, 20, 10, 5];

  // Daily Prayer Names in Bengali
  static const String waqtFajr = 'ফজর';
  static const String waqtDhuhr = 'যোহর';
  static const String waqtAsr = 'আসর';
  static const String waqtMaghrib = 'মাগরিব';
  static const String waqtIsha = 'ইশা';
  static const String waqtJummah = 'জুমুআ';

  // Local Storage Keys
  static const String keyActiveMosqueId = 'key_active_mosque_id';
  static const String keyUserRole = 'key_user_role';
  static const String keyMemberId = 'key_member_id';
  static const String keyMemberName = 'key_member_name';
  static const String keyCommitteeId = 'key_committee_id';
  static const String keyIsDarkMode = 'key_is_dark_mode';
  static const String keyHijriOffset = 'key_hijri_offset';
}
