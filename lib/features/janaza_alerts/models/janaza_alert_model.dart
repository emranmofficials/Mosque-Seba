import 'package:equatable/equatable.dart';

/// Emergency Janaza & Demise Announcement Model (জরুরি জানাজা ও শোক সংবাদ মডেল)
class JanazaAlertModel extends Equatable {
  final String id;
  final String deceasedName;
  final String relativeRelation; // 'পিতা', 'মাতা', 'স্বামী', 'স্ত্রী'
  final String guardianName;
  final String age;
  final String villageArea;
  final String timeOfDeath;
  final String janazaVenue;
  final String janazaTime;
  final String graveyardVenue;
  final String contactPhone;
  final String extraNotes;
  final String broadcastAt;

  const JanazaAlertModel({
    required this.id,
    required this.deceasedName,
    this.relativeRelation = 'পিতা',
    this.guardianName = '',
    this.age = '',
    this.villageArea = 'মসজিদ সংলগ্ন এলাকা',
    required this.timeOfDeath,
    required this.janazaVenue,
    required this.janazaTime,
    this.graveyardVenue = 'মসজিদ কেন্দ্রীয় কবরস্থান',
    this.contactPhone = '',
    this.extraNotes = '',
    required this.broadcastAt,
  });

  /// Formats the broadcast announcement text ready for WhatsApp/SMS/Social Sharing
  String toBroadcastMessage(String mosqueName) {
    final sb = StringBuffer();
    sb.writeln('إِنَّا لِلَّهِ وَإِنَّا إِلَيْهِ رَاجِعُونَ');
    sb.writeln('------------------------------------------');
    sb.writeln('🚨 জরুরি জানাজা ও শোক সংবাদ');
    sb.writeln('মসজিদ: $mosqueName');
    sb.writeln('------------------------------------------');
    sb.writeln('অত্যন্ত শোকাহত হৃদয়ে জানানো যাচ্ছে যে, আমাদের এলাকার শ্রদ্ধেয় $deceasedName');
    if (guardianName.isNotEmpty) {
      sb.writeln('($relativeRelation: $guardianName)');
    }
    if (age.isNotEmpty) {
      sb.writeln('বয়স: $age বছর | ঠিকানা: $villageArea');
    }
    sb.writeln('তিনি $timeOfDeath ইন্তেকাল করেছেন।');
    sb.writeln();
    sb.writeln('🕌 নামাজে জানাজার সময় ও স্থান:');
    sb.writeln('• স্থান: $janazaVenue');
    sb.writeln('• সময়: $janazaTime');
    if (graveyardVenue.isNotEmpty) {
      sb.writeln('• দাফন: $graveyardVenue');
    }
    if (contactPhone.isNotEmpty) {
      sb.writeln('• যোগাযোগ: $contactPhone');
    }
    if (extraNotes.isNotEmpty) {
      sb.writeln();
      sb.writeln(extraNotes);
    }
    sb.writeln('------------------------------------------');
    sb.write('মসজিদ সেবা (Mosque Seba) জরুরি সম্প্রচার');
    return sb.toString();
  }

  factory JanazaAlertModel.fromJson(Map<String, dynamic> json) {
    return JanazaAlertModel(
      id: json['id'] as String? ?? '',
      deceasedName: json['deceased_name'] as String? ?? '',
      relativeRelation: json['relative_relation'] as String? ?? 'পিতা',
      guardianName: json['guardian_name'] as String? ?? '',
      age: json['age'] as String? ?? '',
      villageArea: json['village_area'] as String? ?? '',
      timeOfDeath: json['time_of_death'] as String? ?? '',
      janazaVenue: json['janaza_venue'] as String? ?? '',
      janazaTime: json['janaza_time'] as String? ?? '',
      graveyardVenue: json['graveyard_venue'] as String? ?? '',
      contactPhone: json['contact_phone'] as String? ?? '',
      extraNotes: json['extra_notes'] as String? ?? '',
      broadcastAt: json['broadcast_at'] as String? ?? '',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'deceased_name': deceasedName,
      'relative_relation': relativeRelation,
      'guardian_name': guardianName,
      'age': age,
      'village_area': villageArea,
      'time_of_death': timeOfDeath,
      'janaza_venue': janazaVenue,
      'janaza_time': janazaTime,
      'graveyard_venue': graveyardVenue,
      'contact_phone': contactPhone,
      'extra_notes': extraNotes,
      'broadcast_at': broadcastAt,
    };
  }

  @override
  List<Object?> get props => [
        id,
        deceasedName,
        timeOfDeath,
        janazaVenue,
        janazaTime,
        broadcastAt,
      ];
}
