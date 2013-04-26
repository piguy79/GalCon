package com.xxx.galcon.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.xxx.galcon.Config;
import com.xxx.galcon.Connection;
import com.xxx.galcon.MainActivity;
import com.xxx.galcon.R;
import com.xxx.galcon.UserInfo;
import com.xxx.galcon.http.UrlConstants;
import com.xxx.galcon.model.AvailableGames;

public class PingService extends Service {
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	private static final int SLEEP_TIME = 120 * 1000;

	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			if (!isMainActivityActive()) {
				pingForPendingMove();
			}

			synchronized (this) {
				PingService.this.mServiceHandler.post(new Runnable() {
					public void run() {
						PingService.this.post();
					}
				});
			}
		}

		private void pingForPendingMove() {
			final Map<String, String> args = new HashMap<String, String>();
			args.put("userName", UserInfo.getUser(PingService.this));

			ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

			try {
				HttpURLConnection connection = Connection.establishGetConnection(Config.getValue(Config.HOST),
						Config.getValue(Config.PORT), UrlConstants.FIND_GAMES_WITH_A_PENDING_MOVE, args);
				AvailableGames result = (AvailableGames) Connection.doRequest(connectivityManager, connection,
						new AvailableGames());
				parseResult(result);
			} catch (IOException e) {
				Log.wtf("CONNECTION", e.getMessage());
			}
		}

		private void parseResult(AvailableGames result) {
			if (result != null && !result.getAllGames().isEmpty()) {
				sendNotification(result);
			}

		}

		private void sendNotification(AvailableGames games) {
			if (isMainActivityActive()) {
				return;
			}

			String text = "1 game is awaiting your move";
			int numberOfGames = games.getAllGames().size();
			if (numberOfGames > 1) {
				text = numberOfGames + " games are awaiting your move";
			}

			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(PingService.this)
					.setSmallIcon(R.drawable.ic_launcher).setContentTitle("GalCon").setContentText(text)
					.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL).setOnlyAlertOnce(true);

			Intent resultIntent = new Intent(PingService.this, MainActivity.class);
			resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

			mBuilder.setContentIntent(PendingIntent.getActivity(PingService.this.getBaseContext(), 0, resultIntent,
					PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT));

			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(1, mBuilder.build());
		}

		private boolean isMainActivityActive() {
			ActivityManager am = (ActivityManager) PingService.this.getSystemService(ACTIVITY_SERVICE);
			List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);

			String name = MainActivity.class.getName();
			String activityName = taskInfo.get(0).topActivity.getClassName();
			if (activityName.equals(name)) {
				return true;
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
		post();

		return START_STICKY;
	}

	protected void post() {
		Message msg = mServiceHandler.obtainMessage();
		mServiceHandler.sendMessageAtTime(msg, SystemClock.uptimeMillis() + SLEEP_TIME);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}