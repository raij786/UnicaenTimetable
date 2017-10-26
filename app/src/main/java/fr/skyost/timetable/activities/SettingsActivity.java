package fr.skyost.timetable.activities;

import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.MenuItem;

import org.joda.time.DateTime;

import fr.skyost.timetable.R;
import fr.skyost.timetable.utils.AppCompatPreferenceActivity;

import java.text.DateFormat;
import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {

	private static final int INTRO_ACTIVITY_RESULT = 100;

	private static class BindPreferenceSummaryToValueListener implements Preference.OnPreferenceChangeListener {

		@Override
		public final boolean onPreferenceChange(final Preference preference, final Object value) {
			return notifyPreferenceChange(preference, value, true);
		}

		public final boolean notifyPreferenceChange(final Preference preference, final Object value, final boolean savePreference) {
			final Resources resources = preference.getContext().getResources();
			final String string = value.toString();
			switch(preference.getKey().toLowerCase()) {
			case MainActivity.PREFERENCES_SERVER:
				preference.setSummary(TextUtils.isEmpty(string) ? resources.getString(R.string.settings_default_server) : string);
				preference.setSummary(preference.getSummary() + "\n" + resources.getString(R.string.settings_default, resources.getString(R.string.settings_default_server)));
				break;
			case MainActivity.PREFERENCES_CALENDAR:
				preference.setSummary(TextUtils.isEmpty(string) ? resources.getString(R.string.settings_default_calendarname) : string);
				preference.setSummary(preference.getSummary() + "\n" + resources.getString(R.string.settings_default, resources.getString(R.string.settings_default_calendarname)));
				break;
			case MainActivity.PREFERENCES_CALENDAR_INTERVAL:
				DateTime inf = DateTime.now();
				DateTime sup = DateTime.now();

				switch(string) {
				case "1":
					inf = inf.minusMonths(1);
					sup = sup.plusMonths(1);
					break;
				case "2":
					inf = inf.minusMonths(3);
					sup = sup.plusMonths(3);
					break;
				case "3":
					inf = null;
					sup = null;
					break;
				default:
					inf = inf.minusWeeks(2);
					sup = sup.plusWeeks(2);
					break;
				}

				final DateFormat formatter = DateFormat.getDateInstance(DateFormat.MEDIUM);

				preference.setSummary(TextUtils.isEmpty(string) ? resources.getStringArray(R.array.preferences_server_calendar_interval_keys)[0] : resources.getStringArray(R.array.preferences_server_calendar_interval_keys)[Integer.valueOf(string)]);
				if(inf == null || sup == null) {
					preference.setSummary(preference.getSummary() + "\n" + resources.getString(R.string.settings_calendar_interval_description_2));
				}
				else {
					preference.setSummary(preference.getSummary() + "\n" + resources.getString(R.string.settings_calendar_interval_description_1, formatter.format(inf.toDate()), formatter.format(sup.toDate())));
				}

				if(savePreference) {
					preference.getContext().getSharedPreferences(MainActivity.PREFERENCES_TITLE, Context.MODE_PRIVATE).edit().putBoolean(MainActivity.PREFERENCES_CHANGED_INTERVAL, true).apply();
				}
				break;
			}
			return true;
		}

	};

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final ActionBar actionBar = this.getSupportActionBar();
		if(actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public final void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if(resultCode != Activity.RESULT_OK) {
			return;
		}
		switch(requestCode) {
		case INTRO_ACTIVITY_RESULT:
			this.getSharedPreferences(MainActivity.PREFERENCES_TITLE, Context.MODE_PRIVATE).edit().putBoolean(MainActivity.PREFERENCES_CHANGED_ACCOUNT, true).apply();
			onBackPressed();
			break;
		}
	}

	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			this.onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public final boolean onIsMultiPane() {
		return isXLargeTablet(this);
	}

	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public final void onBuildHeaders(final List<Header> target) {
		this.loadHeadersFromResource(R.xml.preferences_header, target);
	}

	@Override
	protected final boolean isValidFragment(final String fragmentName) {
		return PreferenceFragment.class.getName().equals(fragmentName) || ServerPreferenceFragment.class.getName().equals(fragmentName) || AccountPreferenceFragment.class.getName().equals(fragmentName) || AppPreferenceFragment.class.getName().equals(fragmentName);
	}

	private static final void bindPreferenceSummaryToValue(final SharedPreferences preferences, final Preference preference) {
		final BindPreferenceSummaryToValueListener listener = new BindPreferenceSummaryToValueListener();
		preference.setOnPreferenceChangeListener(listener);
		listener.notifyPreferenceChange(preference, preferences.getString(preference.getKey(), ""), false);
	}

	private static final void setDefaultPreferencesFile(final PreferenceFragment fragment) {
		final PreferenceManager manager = fragment.getPreferenceManager();
		manager.setSharedPreferencesName(MainActivity.PREFERENCES_TITLE);
		manager.setSharedPreferencesMode(Context.MODE_PRIVATE);
	}

	private static final boolean isXLargeTablet(final Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class ServerPreferenceFragment extends PreferenceFragment {

		@Override
		public final void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			setDefaultPreferencesFile(this);

			this.addPreferencesFromResource(R.xml.preferences_server);
			this.setHasOptionsMenu(true);

			final SharedPreferences preferences = this.getActivity().getSharedPreferences(MainActivity.PREFERENCES_TITLE, Context.MODE_PRIVATE);

			bindPreferenceSummaryToValue(preferences, findPreference(MainActivity.PREFERENCES_SERVER));
			bindPreferenceSummaryToValue(preferences, findPreference(MainActivity.PREFERENCES_CALENDAR));
			bindPreferenceSummaryToValue(preferences, findPreference(MainActivity.PREFERENCES_CALENDAR_INTERVAL));
		}

		@Override
		public final boolean onOptionsItemSelected(final MenuItem item) {
			switch(item.getItemId()) {
			case android.R.id.home:
				this.startActivity(new Intent(this.getActivity(), SettingsActivity.class));
				return true;
			}
			return super.onOptionsItemSelected(item);
		}

	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class AccountPreferenceFragment extends PreferenceFragment {

		@Override
		public final void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			setDefaultPreferencesFile(this);

			this.addPreferencesFromResource(R.xml.preferences_account);
			this.setHasOptionsMenu(true);

			final Activity activity = this.getActivity();

			final Preference account = this.findPreference("account");
			account.setSummary(this.getResources().getString(R.string.settings_account, AccountManager.get(activity).getAccountsByType(this.getString(R.string.account_type))[0].name));
			account.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

				@Override
				public final boolean onPreferenceClick(final Preference preference) {
					final Activity activity = AccountPreferenceFragment.this.getActivity();
					final Intent intent = new Intent(activity, IntroActivity.class);
					intent.putExtra(IntroActivity.INTENT_GOTO, IntroActivity.SLIDE_ACCOUNT);
					intent.putExtra(IntroActivity.INTENT_ALLOW_BACKWARD, false);
					activity.startActivityForResult(intent, INTRO_ACTIVITY_RESULT);
					return true;
				}

			});
		}

		@Override
		public final boolean onOptionsItemSelected(final MenuItem item) {
			switch(item.getItemId()) {
			case android.R.id.home:
				this.startActivity(new Intent(this.getActivity(), SettingsActivity.class));
				return true;
			}
			return super.onOptionsItemSelected(item);
		}

	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class AppPreferenceFragment extends PreferenceFragment {

		@Override
		public final void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			setDefaultPreferencesFile(this);

			this.addPreferencesFromResource(R.xml.preferences_application);
			this.setHasOptionsMenu(true);

			final SharedPreferences preferences = this.getActivity().getSharedPreferences(MainActivity.PREFERENCES_TITLE, Context.MODE_PRIVATE);
			((CheckBoxPreference)findPreference(MainActivity.PREFERENCES_AUTOMATICALLY_COLOR_LESSONS)).setChecked(preferences.getBoolean(MainActivity.PREFERENCES_AUTOMATICALLY_COLOR_LESSONS, false));
		}

		@Override
		public final boolean onOptionsItemSelected(final MenuItem item) {
			switch(item.getItemId()) {
			case android.R.id.home:
				this.startActivity(new Intent(this.getActivity(), SettingsActivity.class));
				return true;
			}
			return super.onOptionsItemSelected(item);
		}

	}

}