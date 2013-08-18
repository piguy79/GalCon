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
import com.google.android.gms.appstate.AppStateClient;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.plus.PlusClient;
import com.google.example.games.basegameutils.GameHelper;
import com.xxx.galcon.http.GooglePlusSignInListener;
import com.xxx.galcon.http.SocialAction;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.service.PingService;

public class MainActivity extends AndroidApplication implements GameHelper.GameHelperListener, AdListener {
	public static final String LOG_NAME = "GalCon";

	public static final int CLIENT_GAMES = GameHelper.CLIENT_GAMES;
	public static final int CLIENT_APPSTATE = GameHelper.CLIENT_APPSTATE;
	public static final int CLIENT_PLUS = GameHelper.CLIENT_PLUS;
	public static final int CLIENT_ALL = GameHelper.CLIENT_ALL;

	protected GameHelper mHelper;

	// Requested clients. By default, that's just the games client.
	protected int mRequestedClients = CLIENT_GAMES;

	// stores any additional scopes.
	private String[] mAdditionalScopes;

	private InterstitialAd interstitial;
	private static final String INTERSTITIAL_UNIT_ID = "ca-app-pub-7836100895640182/2621809877";

	protected String mDebugTag = "MainActivity";
	protected boolean mDebugLog = true;

	private GooglePlusSignInListener signInListener;

	public MainActivity() {
		super();
		mHelper = new GameHelper(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Crashlytics.start(this);

		if (mDebugLog) {
			mHelper.enableDebugLog(mDebugLog, mDebugTag);
		}
		mHelper.setup(this, mRequestedClients, mAdditionalScopes);

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

	protected void beginUserInitiatedSignIn() {
		mHelper.beginUserInitiatedSignIn();
	}

	protected void signOut() {
		mHelper.signOut();
		signInListener.onSignOut();
	}

	/**
	 * Sets the requested clients. The preferred way to set the requested
	 * clients is via the constructor, but this method is available if for some
	 * reason your code cannot do this in the constructor. This must be called
	 * before onCreate in order to have any effect. If called after onCreate,
	 * this method is a no-op.
	 * 
	 * @param requestedClients
	 *            A combination of the flags CLIENT_GAMES, CLIENT_PLUS and
	 *            CLIENT_APPSTATE, or CLIENT_ALL to request all available
	 *            clients.
	 * @param additionalScopes
	 *            . Scopes that should also be requested when the auth request
	 *            is made.
	 */
	protected void setRequestedClients(int requestedClients, String... additionalScopes) {
		mRequestedClients = requestedClients;
		mAdditionalScopes = additionalScopes;
	}

	@Override
	protected void onStart() {
		super.onStart();
		mHelper.onStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		mHelper.onStop();
	}

	@Override
	protected void onActivityResult(int request, int response, Intent data) {
		super.onActivityResult(request, response, data);
		mHelper.onActivityResult(request, response, data);
	}

	protected GamesClient getGamesClient() {
		return mHelper.getGamesClient();
	}

	protected AppStateClient getAppStateClient() {
		return mHelper.getAppStateClient();
	}

	protected PlusClient getPlusClient() {
		return mHelper.getPlusClient();
	}

	protected synchronized boolean isSignedIn() {
		return mHelper.isSignedIn();
	}

	protected void showAlert(String title, String message) {
		mHelper.showAlert(title, message);
	}

	protected void showAlert(String message) {
		mHelper.showAlert(message);
	}

	protected void enableDebugLog(boolean enabled, String tag) {
		mDebugLog = true;
		mDebugTag = tag;
		if (mHelper != null) {
			mHelper.enableDebugLog(enabled, tag);
		}
	}

	protected String getInvitationId() {
		return mHelper.getInvitationId();
	}

	protected void reconnectClients(int whichClients) {
		mHelper.reconnectClients(whichClients);
	}

	protected String getScopes() {
		return mHelper.getScopes();
	}

	protected String[] getScopesArray() {
		return mHelper.getScopesArray();
	}

	protected boolean hasSignInError() {
		return mHelper.hasSignInError();
	}

	protected GameHelper.SignInFailureReason getSignInError() {
		return mHelper.getSignInError();
	}

	@Override
	public void onSignInFailed() {
		if (signInListener != null) {
			signInListener.onSignInFailed();
		}
	}

	@Override
	public void onSignInSucceeded() {
		if (signInListener != null) {
			signInListener.onSignInSucceeded();
		}
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

	public void registerGooglePlusSignInListener(GooglePlusSignInListener signInListener) {
		this.signInListener = signInListener;
	}
}
