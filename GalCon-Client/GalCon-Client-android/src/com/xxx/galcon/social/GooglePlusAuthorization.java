package com.xxx.galcon.social;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import com.google.android.gms.plus.PlusClient.OnPeopleLoadedListener;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.MainActivity;
import com.xxx.galcon.Strings;
import com.xxx.galcon.http.AuthenticationListener;
import com.xxx.galcon.http.FriendsListener;
import com.xxx.galcon.model.Friend;

public class GooglePlusAuthorization implements Authorizer, ConnectionCallbacks, OnConnectionFailedListener {
	private PlusClient plusClient;
	private String scopes = Scopes.PLUS_LOGIN;

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
		GameLoop.USER.addAuthProvider(Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE, plusClient.getCurrentPerson().getId());

		Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
		prefs.putString(Constants.ID, GameLoop.USER.auth.getID(Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE));
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

	@Override
	public void getFriends(final FriendsListener listener) {
		if(plusClient != null && plusClient.isConnected()){
			retrieveFriends(listener);
		}else{
			plusClient = new PlusClient.Builder(activity, new ConnectionCallbacks() {
				
				@Override
				public void onDisconnected() {					
				}
				
				@Override
				public void onConnected(Bundle connectionHint) {
					retrieveFriends(listener);
					
				}
			}, new OnConnectionFailedListener() {
				
				@Override
				public void onConnectionFailed(ConnectionResult result) {
					listener.onFriendsLoadedFail("Unable to connect to GP");
					
				}
			}).setScopes(scopes).build();
			plusClient.connect();
		}
		
	}

	private void retrieveFriends(final FriendsListener listener) {
		plusClient.loadVisiblePeople(new OnPeopleLoadedListener() {
			
			@Override
			public void onPeopleLoaded(ConnectionResult status,
					PersonBuffer personBuffer, String nextPageToken) {
				List<Friend> friends = new ArrayList<Friend>();
				for(int i = 0; i < personBuffer.getCount(); i++){
					Person person = personBuffer.get(i);
					Friend friend = new Friend(person.getId() + ":" + Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE, person.getDisplayName(), "");
					friends.add(friend);
				}
				listener.onFriendsLoadedSuccess(friends, Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE);
			}
		}, null);
	}

	
}
