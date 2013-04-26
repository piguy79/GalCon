package com.xxx.galcon;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.xxx.galcon.http.ConnectionException;
import com.xxx.galcon.http.SetPlayerResultHandler;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.service.PingService;

public class MainActivity extends AndroidApplication {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
		cfg.useGL20 = true;

		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

		AndroidGameAction gameAction = new AndroidGameAction(this, connectivityManager);

		Player player = new Player();
		player.name = UserInfo.getUser(getBaseContext());
		try {
			gameAction.findUserInformation(new SetPlayerResultHandler(player), player.name);
		} catch (ConnectionException e) {
			e.printStackTrace();
		}

		initialize(new GameLoop(player, gameAction), cfg);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		Intent intent = new Intent(this, PingService.class);
		startService(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
}
