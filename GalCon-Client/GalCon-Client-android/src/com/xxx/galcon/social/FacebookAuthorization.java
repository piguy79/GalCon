package com.xxx.galcon.social;

import java.io.IOException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.model.GraphObject;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.MainActivity;
import com.xxx.galcon.Strings;
import com.xxx.galcon.http.AuthenticationListener;


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
		
		Session session = createSession();
		session.openForRead(createRequest().setCallback(statusCallback));
		
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
			GameLoop.USER.authId = user.getProperty("id")  + ":" + Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK;

			Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
			prefs.putString(Constants.ID, GameLoop.USER.authId);
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
            session.openForRead(createRequest().setCallback(statusCallback));
        } else if(session.isOpened()){
           listener.onSignInSucceeded(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK, Session.getActiveSession().getAccessToken());
        }
		
	}
	
	public class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if(SessionState.CLOSED_LOGIN_FAILED == state){
            	listener.onSignInFailed("Unable to connect to Facebook.");
            }
        }
    }

}
