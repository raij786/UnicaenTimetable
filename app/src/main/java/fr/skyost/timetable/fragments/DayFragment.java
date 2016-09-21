package fr.skyost.timetable.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import fr.skyost.timetable.R;
import fr.skyost.timetable.Timetable;
import fr.skyost.timetable.Timetable.Day;
import fr.skyost.timetable.Timetable.Lesson;
import fr.skyost.timetable.activities.MainActivity;
import fr.skyost.timetable.utils.Utils;

public class DayFragment extends Fragment {

	private Day day;

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Bundle args = this.getArguments();
		if(args != null) {
			day = Day.valueOf(args.getString(Day.class.getName().toLowerCase()));
		}
	}

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_main_day, container, false);

		/* https://github.com/Quivr/Android-Week-View */
		final WeekView weekView = (WeekView)view.findViewById(R.id.main_day_weekview_day);
		weekView.setDateTimeInterpreter(new DateTimeInterpreter() {

			@Override
			public final String interpretDate(final Calendar calendar) {
				final Date date = calendar.getTime();
				return new SimpleDateFormat("E").format(date) + " " + DateFormat.getDateFormat(DayFragment.this.getActivity()).format(date);
			}

			@Override
			public final String interpretTime(final int hour, final int minutes) {
				return Utils.addZeroIfNeeded(hour) + ":" + Utils.addZeroIfNeeded(minutes);
			}

		});

		final Calendar calendar = Calendar.getInstance();
		final int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
		if(currentDay == Calendar.SATURDAY || currentDay == Calendar.SUNDAY) {
			final long aWeek = 1000L * 60L * 60L * 24L * 7L;
			calendar.setTimeInMillis(calendar.getTimeInMillis() + aWeek);
		}
		calendar.set(Calendar.DAY_OF_WEEK, day.getValue());

		weekView.setMinDate(calendar);
		weekView.setMaxDate(calendar);
		weekView.goToDate(calendar);
		weekView.setHorizontalFlingEnabled(false);
		weekView.setMonthChangeListener(new MonthLoader.MonthChangeListener() {

			@Override
			public final List<? extends WeekViewEvent> onMonthChange(final int newYear, final int newMonth) {
				final List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();
				if(weekView.getFirstVisibleDay().get(Calendar.MONTH) + 1 != newMonth) {
					return events;
				}

				final Timetable timetable = ((MainActivity)DayFragment.this.getActivity()).getTimetable();
				if(timetable == null) {
					return events;
				}

				for(final Lesson lesson : timetable.getLessons()) {
					final Calendar start = lesson.getStart();
					if(start.get(Calendar.DAY_OF_MONTH) != calendar.get(Calendar.DAY_OF_MONTH)) {
						continue;
					}
					events.add(new WeekViewEvent(lesson.getId(), lesson.getName(), lesson.getLocation(), start, lesson.getEnd()));
				}

				return events;
			}
		});
		return view;
	}

	@Override
	public final void onAttach(final Context context) {
		super.onAttach(context);
	}

	@Override
	public final void onDetach() {
		super.onDetach();
	}

	/**
	 * Creates a new instance of this fragment for the specified day.
	 *
	 * @param day The day.
	 *
	 * @return An instance of this fragment corresponding to the day.
	 */

	public static final DayFragment newInstance(final Day day) {
		final DayFragment instance = new DayFragment();
		final Bundle args = new Bundle();
		args.putString(Day.class.getName().toLowerCase(), day.name());
		instance.setArguments(args);
		return instance;
	}

}