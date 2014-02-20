package com.xxx.galcon.social;

import java.io.IOException;

import android.app.Activity;
import android.content.IntentSender.SendIntentException;
import android.os.AsyncTask;
import android.os.Bundle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.plus.PlusClient;
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.MainActivity;
import com.xxx.galcon.Strings;
import com.xxx.galcon.http.AuthenticationListener;

public class GooglePlusAuthorization implements Authorizer, ConnectionCallbacks, OnConnectionFailedListener {
	private PlusClient plusClient;
	private String scopes = Scopes.PLUS_LOGIN + " https://www.googleapis.com/auth/userinfo.email";

	private Activity activity;
	private AuthenticationListener listener;

	public GooglePlusAuthorization(Activity activity) {
		this.activity = activity;
	}

	@Override
	public void signIn(AuthenticationListener listener) {
		this.listener = listener;

		plusClient = new PlusClient.Builder(activity, this, this).setScopes(scopes).build();
		plusClient.connect();
		
	}

	@Override
	public void getToken(AuthenticationListener listener) {
		this.listener = listener;
		if(plusClient.isConnected()){
			new RetrieveTokenTask().execute(plusClient.getAccountName());
		}else{
			plusClient = new PlusClient.Builder(activity, this, this).setScopes(scopes).build();
			plusClient.connect();
		}
	}

	@Override
	public void onConnected(Bundle bundle) {
		GameLoop.USER.authId = plusClient.getCurrentPerson().getId()  + ":" + Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE;

		Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
		prefs.putString(Constants.ID, GameLoop.USER.authId);
		prefs.flush();

		new RetrieveTokenTask().execute(plusClient.getAccountName());
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onActivityResult(int responseCode) {
		if (responseCode == Activity.RESULT_OK) {
			plusClient.connect();
		} else if (responseCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED) {
			plusClient.connect();
		} else if (responseCode == Activity.RESULT_CANCELED) {
			Gdx.app.postRunnable(new Runnable() {
				public void run() {
					listener.onSignInFailed(Strings.AUTH_FAIL);
				}
			});
		} else {
			Gdx.app.postRunnable(new Runnable() {
				public void run() {
					listener.onSignInFailed(Strings.AUTH_FAIL);
				}
			});
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (result.hasResolution()) {
			try {
				result.startResolutionForResult(activity, MainActivity.GOOGLE_PLUS_SIGN_IN_ACTIVITY_RESULT_CODE);
			} catch (SendIntentException e) {
				Crashlytics.logException(e);
				Gdx.app.postRunnable(new Runnable() {
					public void run() {
						listener.onSignInFailed(Strings.AUTH_INTENT_FAIL);
					}
				});
			}
		} else {
			Gdx.app.postRunnable(new Runnable() {
				public void run() {
					listener.onSignInFailed(Strings.AUTH_FAIL);
				}
			});
		}
	}

	private class RetrieveTokenTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			String token = "";
			try {
				token = GoogleAuthUtil.getToken(activity, params[0], "oauth2:" + scopes);
			} catch (UserRecoverableAuthException e) {
				token = "ERROR";
				Crashlytics.logException(e);
				activity.startActivity(e.getIntent());
			} catch (IOException e) {
				token = "ERROR";
				Crashlytics.logException(e);
			} catch (GoogleAuthException e) {
				token = "ERROR";
				Crashlytics.logException(e);
			}

			return token;
		}

		@Override
		protected void onPostExecute(final String result) {
			if (result.equals("ERROR")) {
				Gdx.app.postRunnable(new Runnable() {
					public void run() {
						listener.onSignInFailed(Strings.AUTH_FAIL);
					}
				});
			} else {
				Gdx.app.postRunnable(new Runnable() {
					public void run() {
						listener.onSignInSucceeded(Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE, result);
					}
				});
			}
		}
	}

	
}
