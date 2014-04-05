package com.railwaygames.solarsmash;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.railwaygames.solarsmash.service.PingService;

public class Autostart extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent pingIntent = new Intent(context, PingService.class);
		context.startService(pingIntent);
	}
}
