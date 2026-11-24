/// Granular Permission Keys Catalog (PERMISSION_MATRIX.md Parity)
class PermissionCatalog {
  // Members Domain
  static const String memberView = 'member.view';
  static const String memberCreate = 'member.create';
  static const String memberUpdate = 'member.update';
  static const String memberArchive = 'member.archive';

  // Chanda / Subscription Domain
  static const String chandaView = 'chanda.view';
  static const String chandaPay = 'chanda.pay';
  static const String chandaReceive = 'chanda.receive';

  // Finance Domain
  static const String financeView = 'finance.view';
  static const String financeCreate = 'finance.create';
  static const String financeApprove = 'finance.approve';
  static const String financeReport = 'finance.report';
  static const String cashboxManage = 'cashbox.manage';
  static const String cashboxWitness = 'cashbox.witness';

  // Staff & Payroll Domain
  static const String staffView = 'staff.view';
  static const String staffManage = 'staff.manage';
  static const String staffSalaryView = 'staff.salary.view';
  static const String staffSalaryPay = 'staff.salary.pay';

  // Madrasa & Education Domain
  static const String madrasaView = 'madrasa.view';
  static const String madrasaManage = 'madrasa.manage';
  static const String studentView = 'student.view';
  static const String studentCreate = 'student.create';
  static const String attendanceView = 'attendance.view';
  static const String attendanceManage = 'attendance.manage';
  static const String hifzView = 'hifz.view';
  static const String hifzManage = 'hifz.manage';
  static const String examPublish = 'exam.publish';
  static const String feeReceive = 'fee.receive';

  // Notices, Meetings & Admin
  static const String noticeView = 'notice.view';
  static const String noticeManage = 'notice.manage';
  static const String meetingView = 'meeting.view';
  static const String meetingManage = 'meeting.manage';
  static const String prayerManage = 'prayer.manage';
  static const String dutyManage = 'duty.manage';
  static const String welfareManage = 'welfare.manage';
}
