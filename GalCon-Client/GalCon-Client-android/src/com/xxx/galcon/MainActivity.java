package com.xxx.galcon;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class MainActivity extends AndroidApplication {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
		cfg.useGL20 = true;

		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

		initialize(new GameLoop(getUser(), new AndroidGameAction(this, connectivityManager,
				"damp-crag-7750.herokuapp.com", "80")), cfg);

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