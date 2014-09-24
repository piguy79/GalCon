package com.railwaygames.solarsmash;

import static com.railwaygames.solarsmash.Config.HOST;
import static com.railwaygames.solarsmash.Config.PORT;
import static com.railwaygames.solarsmash.Config.PROTOCOL;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.railwaygames.solarsmash.http.UrlConstants;
import com.railwaygames.solarsmash.model.GameCount;

public class PingingBroadcastReceiver extends BroadcastReceiver {
	public static final int NOTIFICATION_ID = 29484;
	private static final int SLEEP_TIME = 5 * 60 * 1000;
	private static final int SHORT_SLEEP_TIME = 1 * 60 * 60 * 1000;
	private static final int LONG_SLEEP_TIME = 8 * 60 * 60 * 1000;
	private static final int ONE_HOUR = 1 * 60 * 60 * 1000;
	private static final String LAST_CHECK_KEY = "LAST_CHECK_KEY";
	private static final String DELETE_KEY = "DELETE";
	private Config config = new AndroidConfig();

	private class NetworkTask extends AsyncTask<Context, Void, Void> {

		@Override
		protected Void doInBackground(Context... params) {
			pingForPendingMove(params[0]);
			return null;
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction() != null && intent.getAction().equals("android.intent.action.USER_PRESENT")
				&& !isMainActivityActive(context)) {

			SharedPreferences prefs = context.getSharedPreferences(Constants.GALCON_PREFS, Context.MODE_PRIVATE);
			long lastCheckTime = prefs.getLong(LAST_CHECK_KEY, 0);

			if (lastCheckTime < System.currentTimeMillis() - ONE_HOUR) {
				Log.i("PINGSERVICE", "Issueing call from USER_PRESENT");
				updateLastCheckTime(context, System.currentTimeMillis());
				new NetworkTask().execute(context);
			}
		} else if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			Log.i("PINGSERVICE", "Setting up normal sleep alarm");
			setupAlarm(context, SLEEP_TIME);
		} else if (intent.getExtras() != null && intent.getExtras().containsKey(DELETE_KEY)) {
			Log.i("PINGSERVICE", "Setting up long sleep alarm");
			int sleepTime = intent.getExtras().getInt(DELETE_KEY);
			setupAlarm(context, sleepTime);
			updateLastCheckTime(context, System.currentTimeMillis() + sleepTime);

			NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			manager.cancel(NOTIFICATION_ID);
		} else if (!isMainActivityActive(context)) {
			SharedPreferences prefs = context.getSharedPreferences(Constants.GALCON_PREFS, Context.MODE_PRIVATE);
			long lastCheckTime = prefs.getLong(LAST_CHECK_KEY, 0);

			if (lastCheckTime < System.currentTimeMillis() - SLEEP_TIME) {
				Log.i("PINGSERVICE", "Issueing call from from alarm manager");
				updateLastCheckTime(context, System.currentTimeMillis());
				new NetworkTask().execute(context);
			}
		}
	}

	private void updateLastCheckTime(Context context, long newTime) {
		SharedPreferences.Editor editor = context.getSharedPreferences(Constants.GALCON_PREFS, Context.MODE_PRIVATE)
				.edit();
		editor.putLong(LAST_CHECK_KEY, newTime);
		editor.commit();
	}

	private void setupAlarm(Context context, long initialSleepTime) {
		PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, new Intent(
				"com.railwaygames.solarsmash.service.PingService"), 0);

		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmMgr.cancel(alarmIntent);
		alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, initialSleepTime, SLEEP_TIME, alarmIntent);
	}

	private void pingForPendingMove(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.GALCON_PREFS, Context.MODE_PRIVATE);
		String handle = prefs.getString(Constants.HANDLE, "");

		Log.i("PINGSERVICE", "Handle: " + handle);
		if (handle.isEmpty()) {
			return;
		}

		final Map<String, String> args = new HashMap<String, String>();
		args.put("handle", handle);

		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		try {
			HttpURLConnection connection = Connection.establishGetConnection(config.getValue(PROTOCOL),
					config.getValue(HOST), config.getValue(PORT), UrlConstants.FIND_GAMES_WITH_A_PENDING_MOVE, args);
			GameCount result = (GameCount) Connection.doRequest(connectivityManager, connection, new GameCount());
			parseResult(context, result);
		} catch (Exception e) {
			Log.w("PINGSERVICE", e.getMessage());
		}
	}

	private void parseResult(Context context, GameCount result) {
		if (result != null && (result.pendingGameCount > 0 || result.inviteCount > 0)) {
			sendNotification(context, result);
		}
	}

	private void sendNotification(Context context, GameCount result) {
		String pendingText = "";
		int pendingGamesCount = result.pendingGameCount;
		if (pendingGamesCount > 0) {
			if (pendingGamesCount == 1) {
				pendingText = "1 game is awaiting your move";
			} else {
				pendingText = pendingGamesCount + " games are awaiting your move";
			}
		}

		String inviteText = "";
		int inviteCount = result.inviteCount;
		if (inviteCount > 0) {

			inviteText = "You have challenges waiting";
		}

		String text = pendingText;
		if (text.length() > 0 && inviteText.length() > 0) {
			text += " and ";
		}
		text += inviteText;

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.notification).setContentTitle(Constants.APP_TITLE).setContentText(text)
				.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL).setOnlyAlertOnce(true);

		Intent resultIntent = new Intent(context, MainActivity.class);
		resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		mBuilder.setContentIntent(PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT
				| PendingIntent.FLAG_ONE_SHOT));

		Intent shortSleepIntent = new Intent(context, PingingBroadcastReceiver.class);
		shortSleepIntent.putExtra(DELETE_KEY, SHORT_SLEEP_TIME);
		mBuilder.addAction(R.drawable.snooze, "1 hour",
				PendingIntent.getBroadcast(context, 0, shortSleepIntent, 0));

		mBuilder.setDeleteIntent(PendingIntent.getBroadcast(context, 0, shortSleepIntent,
				PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT));

		Intent deleteIntent = new Intent(context, PingingBroadcastReceiver.class);
		deleteIntent.putExtra(DELETE_KEY, LONG_SLEEP_TIME);
		mBuilder.addAction(R.drawable.snooze, "8 hours",
				PendingIntent.getBroadcast(context, 0, deleteIntent, 0));

		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

	private boolean isMainActivityActive(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
		String name = MainActivity.class.getName();
		String activityName = taskInfo.get(0).topActivity.getClassName();
		if (activityName.equals(name)) {
			return true;
		}

		return false;
	}
}
