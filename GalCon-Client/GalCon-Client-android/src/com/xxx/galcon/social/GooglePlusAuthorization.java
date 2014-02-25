package com.xxx.galcon.social;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.net.Uri;
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
import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.plus.PlusClient.OnPeopleLoadedListener;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.MainActivity;
import com.xxx.galcon.Strings;
import com.xxx.galcon.http.AuthenticationListener;
import com.xxx.galcon.http.FriendPostListener;
import com.xxx.galcon.http.FriendsListener;
import com.xxx.galcon.model.Friend;

public class GooglePlusAuthorization implements Authorizer, ConnectionCallbacks, OnConnectionFailedListener {
	private PlusClient plusClient;
	private String scopes = Scopes.PLUS_LOGIN;

	private Activity activity;
	private AuthenticationListener listener;
	private FriendPostListener friendPostListener;

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
		if(plusClient.getCurrentPerson() == null){
			listener.onSignInFailed("Unable to load ID.");
		}else{
			GameLoop.USER.addAuthProvider(Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE, plusClient.getCurrentPerson().getId());

			Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
			prefs.putString(Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE + Constants.ID, GameLoop.USER.auth.getID(Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE));
			prefs.flush();

			new RetrieveTokenTask().execute(plusClient.getAccountName());
		}
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
		} else if(responseCode == MainActivity.GOOGLE_PLUS_PUBLISH_ACTIVITY_RESULT_CODE){
			friendPostListener.onPostSucceeded("Success");
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
			signIn(new AuthenticationListener() {
				
				@Override
				public void onSignOut() {					
				}
				
				@Override
				public void onSignInSucceeded(String authProvider, String token) {
					retrieveFriends(listener);
				}
				
				@Override
				public void onSignInFailed(String failureMessage) {
					listener.onFriendsLoadedFail(failureMessage);
				}
			});
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
					Friend friend = new Friend(person.getId(), person.getDisplayName(), "");
					friends.add(friend);
				}
				listener.onFriendsLoadedSuccess(friends, Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE);
			}
		}, null);
	}

	@Override
	public void postToFriend(final FriendPostListener listener,final  String id) {
		friendPostListener = listener;
		if(plusClient != null && plusClient.isConnected()){
			startFriendPost(listener, id);
		}else{
			signIn(new AuthenticationListener() {
				
				@Override
				public void onSignOut() {					
				}
				
				@Override
				public void onSignInSucceeded(String authProvider, String token) {
					startFriendPost(listener, id);
				}
				
				@Override
				public void onSignInFailed(String failureMessage) {
					listener.onPostFails("Unable to login to Google Plus.");
				}
			});
		}
	}

	private void startFriendPost(final FriendPostListener listener, String id) {
		plusClient.loadPeople(new OnPeopleLoadedListener() {
			
			@Override
			public void onPeopleLoaded(ConnectionResult status,
					PersonBuffer personBuffer, String nextPageToken) {
				if(status.isSuccess()){
					sendPost(personBuffer);
				}else{
					listener.onPostFails("Error Posting to Google Plus.");
				}
				
			}
		}, id);
	}
	
	private void sendPost(PersonBuffer personBuffer){	
		List<Person> people = new ArrayList<Person>();
		
		for(int i = 0; i < personBuffer.getCount(); i++){
			Person person = personBuffer.get(i);
			people.add(person);
		}
		
		Intent shareIntent = new PlusShare.Builder(activity, plusClient)
				.setText("Hi! Come join me playing Solar Smash. Invite me using the handle " + GameLoop.USER.handle).setType("text/plain")
				.setRecipients(people)
          .getIntent();
		
		activity.startActivityForResult(shareIntent, MainActivity.GOOGLE_PLUS_PUBLISH_ACTIVITY_RESULT_CODE);
		
	}

	
}
