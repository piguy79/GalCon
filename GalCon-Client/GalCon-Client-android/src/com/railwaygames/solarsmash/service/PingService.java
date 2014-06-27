package com.railwaygames.solarsmash.service;

import static com.railwaygames.solarsmash.Config.HOST;
import static com.railwaygames.solarsmash.Config.PORT;
import static com.railwaygames.solarsmash.Config.PROTOCOL;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.railwaygames.solarsmash.AndroidConfig;
import com.railwaygames.solarsmash.Config;
import com.railwaygames.solarsmash.Connection;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.MainActivity;
import com.railwaygames.solarsmash.R;
import com.railwaygames.solarsmash.http.UrlConstants;
import com.railwaygames.solarsmash.model.GameCount;

public class PingService extends Service {
	public static final int NOTIFICATION_ID = 29484;
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	private static final int SLEEP_TIME = 5 * 60 * 1000;
	private static final int LONG_SLEEP_TIME = 8 * 60 * 60 * 1000;
	private static final int ONE_HOUR = 1 * 60 * 60 * 1000;
	private static final String DELETE_KEY = "DELETE";
	private Config config = new AndroidConfig();

	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			final int sleepTime;
			DateTime now = new DateTime();
			if (now.getHourOfDay() >= 22 || now.getHourOfDay() < 8) {
				sleepTime = ONE_HOUR;
			} else {
				sleepTime = SLEEP_TIME;
				if (!isMainActivityActive()) {
					pingForPendingMove();
				}
			}

			post(new Runnable() {
				@Override
				public void run() {
					PingService.this.post(sleepTime);
				}
			});
		}

		private void pingForPendingMove() {
			SharedPreferences prefs = PingService.this.getSharedPreferences(Constants.GALCON_PREFS, MODE_PRIVATE);
			String handle = prefs.getString(Constants.HANDLE, "");

			Log.i("PINGSERVICE", "Handle: " + handle);
			if (handle.isEmpty()) {
				return;
			}

			final Map<String, String> args = new HashMap<String, String>();
			args.put("handle", handle);

			ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

			try {
				HttpURLConnection connection = Connection
						.establishGetConnection(config.getValue(PROTOCOL), config.getValue(HOST),
								config.getValue(PORT), UrlConstants.FIND_GAMES_WITH_A_PENDING_MOVE, args);
				GameCount result = (GameCount) Connection.doRequest(connectivityManager, connection, new GameCount());
				parseResult(result);
			} catch (Exception e) {
				Log.w("PINGSERVICE", e.getMessage());
			}
		}

		private void parseResult(GameCount result) {
			if (result != null && (result.pendingGameCount > 0 || result.inviteCount > 0)) {
				sendNotification(result);
			}
		}

		private void sendNotification(GameCount result) {
			if (isMainActivityActive()) {
				return;
			}

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
				if (inviteCount == 1) {
					inviteText = "1 pending invite";
				} else {
					inviteText = inviteCount + " pending invites";
				}
			}

			String text = pendingText;
			if (text.length() > 0 && inviteText.length() > 0) {
				text += " and ";
			}
			text += inviteText;

			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(PingService.this)
					.setSmallIcon(R.drawable.notification).setContentTitle(Constants.APP_TITLE).setContentText(text)
					.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL).setOnlyAlertOnce(true);

			Intent resultIntent = new Intent(PingService.this, MainActivity.class);
			resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

			mBuilder.setContentIntent(PendingIntent.getActivity(PingService.this.getBaseContext(), 0, resultIntent,
					PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT));

			Intent deleteIntent = new Intent(PingService.this, PingService.class);
			deleteIntent.putExtra(DELETE_KEY, DELETE_KEY);
			mBuilder.setDeleteIntent(PendingIntent.getService(PingService.this, 0, deleteIntent,
					PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT));

			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
		}

		private boolean isMainActivityActive() {
			PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
			boolean isScreenOn = powerManager.isScreenOn();

			if (isScreenOn) {
				ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
				List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);

				String name = MainActivity.class.getName();
				String activityName = taskInfo.get(0).topActivity.getClassName();
				if (activityName.equals(name)) {
					return true;
				}
			}

			return false;
		}
	}

	@Override
	public void onCreate() {
		HandlerThread thread = new HandlerThread("PingServiceThread", Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey(DELETE_KEY)) {
			post(LONG_SLEEP_TIME);
		} else {
			post(SLEEP_TIME);
		}

		return START_STICKY;
	}

	protected void post(int sleepTime) {
		Log.i("PINGSERVICE", "Post with sleepTime: " + sleepTime);
		Message msg = mServiceHandler.obtainMessage();
		msg.what = 1;

		mServiceHandler.removeMessages(msg.what);
		mServiceHandler.sendMessageAtTime(msg, SystemClock.uptimeMillis() + sleepTime);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
