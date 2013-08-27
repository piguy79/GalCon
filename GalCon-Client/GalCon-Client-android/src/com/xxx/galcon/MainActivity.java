package com.xxx.galcon;

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
import com.xxx.galcon.http.GooglePlusSignInListener;
import com.xxx.galcon.http.SocialAction;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.service.PingService;

public class MainActivity extends AndroidApplication implements AdListener {
	public static final String LOG_NAME = "GalCon";

	private InterstitialAd interstitial;
	private static final String INTERSTITIAL_UNIT_ID = "ca-app-pub-7836100895640182/2621809877";

	protected String mDebugTag = "MainActivity";
	protected boolean mDebugLog = true;

	public MainActivity() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Crashlytics.start(this);

		interstitial = new InterstitialAd(this, INTERSTITIAL_UNIT_ID);
		interstitial.setAdListener(this);

		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
		cfg.useGL20 = true;

		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

		AndroidGameAction gameAction = new AndroidGameAction(this, connectivityManager);
		SocialAction socialGameAction = new AndroidSocialAction(this);

		Player player = new Player();
		player.name = UserInfo.getUser(getBaseContext());

		gameAction.findUserInformation(new SetOrPromptResultHandler(this, gameAction, player), player.name);

		initialize(new GameLoop(player, gameAction, socialGameAction), cfg);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		Intent intent = new Intent(this, PingService.class);
		startService(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onDismissScreen(Ad arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLeaveApplication(Ad arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPresentScreen(Ad arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onReceiveAd(Ad ad) {
		Log.d("OK", "Received ad");
		if (ad == interstitial) {
			interstitial.show();
		}
	}

	public void displayAd() {
		interstitial.loadAd(new AdRequest());
	}
}
