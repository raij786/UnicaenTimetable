import 'package:ez_localization/ez_localization.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:unicaen_timetable/model/lesson.dart';
import 'package:unicaen_timetable/pages/home/cards/card.dart';
import 'package:unicaen_timetable/pages/page.dart';
import 'package:unicaen_timetable/pages/week_view/day_view.dart';
import 'package:unicaen_timetable/utils/utils.dart';

/// A card that allows to show the next lesson of today.
class NextLessonCard extends RemainingLessonsCard {
  /// The card id.
  static const String ID = 'next_lesson';

  /// Creates a new next lesson card instance.
  const NextLessonCard() : super(cardId: ID);

  @override
  IconData buildIcon(BuildContext context) => Icons.arrow_forward;

  @override
  Color buildColor(BuildContext context) => Colors.purple;

  @override
  String buildSubtitle(BuildContext context) {
    List<Lesson> remainingLessons = Provider.of<List<Lesson>>(context);
    if(remainingLessons == null) {
      return EzLocalization.of(context).get('home.loading');
    }

    DateTime now = DateTime.now();
    Lesson lesson = remainingLessons.firstWhere((lesson) => now.isBefore(lesson.start), orElse: () => null);
    return lesson?.toString(context) ?? EzLocalization.of(context).get('home.next_lesson.nothing');
  }

  @override
  void onTap(BuildContext context) {
    DateTime now = DateTime.now();
    if (now.weekday == DateTime.saturday || now.weekday == DateTime.sunday) {
      now = now.atMonday;
    }

    Provider.of<ValueNotifier<Page>>(context, listen: false).value = DayViewPage(weekDay: now.weekday);
  }
}