package com.xxx.galcon;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.xxx.galcon.http.ConnectionException;
import com.xxx.galcon.http.SetPlayerResultHandler;
import com.xxx.galcon.model.Player;

public class MainActivity extends AndroidApplication {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
		cfg.useGL20 = true;

		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		
		AndroidGameAction gameAction = new AndroidGameAction(this, connectivityManager,
		"stormy-sands-7424.herokuapp.com", "80");
		
		Player player = new Player();
		player.name = getUser();
		try {
			gameAction.findUserInformation(new SetPlayerResultHandler(player), player.name);
		} catch (ConnectionException e) {
			e.printStackTrace();
		}

		initialize(new GameLoop(player, gameAction), cfg);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	private String getUser() {
		AccountManager am = AccountManager.get(this);
		Account[] accounts = am.getAccountsByType("com.google");

		if (accounts == null || accounts.length == 0) {
			return "testUser";
		}

		return accounts[0].name;
	}
}