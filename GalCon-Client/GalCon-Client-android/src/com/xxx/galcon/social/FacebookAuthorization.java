package com.xxx.galcon.social;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Request.GraphUserListCallback;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.MainActivity;
import com.xxx.galcon.Strings;
import com.xxx.galcon.http.AuthenticationListener;
import com.xxx.galcon.http.FriendsListener;
import com.xxx.galcon.model.Friend;


public class FacebookAuthorization implements Authorizer {
		
	private Activity activity;
	private AuthenticationListener listener;
	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	
	public FacebookAuthorization(Activity activity){
		this.activity = activity;
	}
		

	@Override
	public void signIn(AuthenticationListener listener) {
		this.listener = listener;
		
		Session current = Session.getActiveSession();
		if(current != null && current.isOpened()){
			listener.onSignInSucceeded(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK, current.getAccessToken());
		}else{
			Session session = createSession();
			session.openForRead(createRequest().setCallback(statusCallback));
		}
	}
	
	private Session createSession() {
        Session activeSession = Session.getActiveSession();
        if (activeSession == null || activeSession.getState().isClosed()) {
            activeSession = new Session.Builder(activity).build();
            Session.setActiveSession(activeSession);
        }
        return activeSession;
    }


	private Session.OpenRequest createRequest() {
		Session.OpenRequest request = new Session.OpenRequest(activity);
		request.setRequestCode(MainActivity.FACEBOOK_SIGN_IN_ACTIVITY_RESULT_CODE);
		return request;
	}

	@Override
	public void onActivityResult(int responseCode) {
       Request.newGraphPathRequest(Session.getActiveSession(), "/me", new Request.Callback() {
		
		@Override
		public void onCompleted(Response response) {
			GraphObject user = response.getGraphObject();
			GameLoop.USER.addAuthProvider(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK, user.getProperty("id").toString());

			Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
			prefs.putString(Constants.ID, GameLoop.USER.auth.getID(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK));
			prefs.flush();
			
			listener.onSignInSucceeded(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK, Session.getActiveSession().getAccessToken());
		}
	}).executeAsync();
      
       
	}

	@Override
	public void getToken(AuthenticationListener listener) {
		this.listener = listener;
		
		Session session = Session.getActiveSession();
        if (session == null) {
           listener.onSignInFailed("Unable to retrieve token.");
        } else if(session.isOpened()){
           listener.onSignInSucceeded(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK, Session.getActiveSession().getAccessToken());
        }
		
	}
	
	public class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if(SessionState.CLOSED_LOGIN_FAILED == state){
            	listener.onSignInFailed("Unable to connect to Facebook.");
            }else if(SessionState.OPENED == state){
            	listener.onSignInSucceeded(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK, session.getAccessToken());
            }
        }
    }

	@Override
	public void getFriends(final FriendsListener listener) {
		
		if(Session.getActiveSession() == null || !Session.getActiveSession().isOpened()){
			Session session = createSession();
			session.openForRead(createRequest().setCallback(new StatusCallback() {
				
				@Override
				public void call(Session session, SessionState state, Exception exception) {
					if(SessionState.OPENED == state){
						requestFriendInfo(listener, session);
					}
					
				}
			}));
		}else{
			requestFriendInfo(listener, Session.getActiveSession());
		}
		
	}
	
	private void requestFriendInfo(final FriendsListener listener,
			Session session) {
		Bundle parameters = new Bundle();
		parameters.putString("fields", "id, name, picture");
		
		
		Request friendRequest = Request.newMyFriendsRequest(session, new GraphUserListCallback() {
			@Override
			public void onCompleted(List<GraphUser> users, Response response) {
				List<Friend> friends = new ArrayList<Friend>();
				for(GraphUser user : users){
					String imageUrl = "";
					JSONObject jsonObject = user.getInnerJSONObject();
					try {
						JSONObject picture = jsonObject.getJSONObject("picture").getJSONObject("data");
						imageUrl = picture.getString("url");
					} catch (JSONException e) {
						
					}
					Friend friend = new Friend(user.getId(), user.getName(), imageUrl);
					friends.add(friend);
				}
				listener.onFriendsLoadedSuccess(friends, Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK);
			}
		});
		friendRequest.setParameters(parameters);
		friendRequest.executeAsync();
		
	}

}
