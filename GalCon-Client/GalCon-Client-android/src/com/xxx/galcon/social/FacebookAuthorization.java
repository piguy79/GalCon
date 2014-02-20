package com.xxx.galcon.social;

import android.app.Activity;
import android.os.Bundle;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.android.AsyncFacebookRunner;
import com.xxx.galcon.MainActivity;
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
       Request.newGraphPathRequest(Session.getActiveSession(), "me", new Request.Callback() {
		
		@Override
		public void onCompleted(Response response) {
			System.out.println(response.toString());
			
		}
	}).executeAsync();
      
       
	}
	
	

	@Override
	public void getToken(AuthenticationListener listener) {
		this.listener = listener;
		
		Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed()) {
            session.openForRead(createRequest().setCallback(statusCallback));
        } else {
            Session.openActiveSession(activity, true, statusCallback);
        }
		
	}
	
	public class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if(SessionState.CLOSED_LOGIN_FAILED == state){
            	System.out.println("***** MSG: " + exception.getMessage());
            }
        }
    }

}
