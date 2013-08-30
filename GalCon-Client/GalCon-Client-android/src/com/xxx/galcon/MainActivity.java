package com.xxx.galcon;

import java.util.HashSet;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.crashlytics.android.Crashlytics;
import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.InterstitialAd;
import com.tapjoy.TapjoyConnect;
import com.tapjoy.TapjoyFullScreenAdNotifier;
import com.tapjoy.TapjoyVideoNotifier;
import com.xxx.galcon.config.Configuration;
import com.xxx.galcon.http.GooglePlusSignInListener;
import com.xxx.galcon.http.SetConfigurationResultHandler;
import com.xxx.galcon.http.SocialAction;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.service.PingService;

public class MainActivity extends AndroidApplication implements TapjoyFullScreenAdNotifier, TapjoyVideoNotifier {
	public static final String LOG_NAME = "GalCon";


	protected String mDebugTag = "MainActivity";
	protected boolean mDebugLog = true;

	public MainActivity() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Crashlytics.start(this);


		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
		cfg.useGL20 = true;

		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

		AndroidGameAction gameAction = new AndroidGameAction(this, connectivityManager);
		SocialAction socialGameAction = new AndroidSocialAction(this);

		Player player = new Player();
		player.name = UserInfo.getUser(getBaseContext());

		gameAction.findUserInformation(new SetOrPromptResultHandler(this, gameAction, player), player.name);
		
		Configuration config = new Configuration();
		gameAction.findConfigByType(new SetConfigurationResultHandler(config), "app");

		initialize(new GameLoop(player, gameAction, socialGameAction, config), cfg);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		Intent intent = new Intent(this, PingService.class);
		startService(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}



	public void displayAd() {
		TapjoyConnect.getTapjoyConnectInstance().getFullScreenAdWithCurrencyID("<CURRENCY_ID>", this);
	}

	@Override
	public void videoComplete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void videoError(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void videoStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getFullScreenAdResponse() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getFullScreenAdResponseFailed(int arg0) {
		// TODO Auto-generated method stub
		
	}
}
