package com.xxx.galcon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.xxx.galcon.http.AuthenticationListener;
import com.xxx.galcon.http.FriendsListener;
import com.xxx.galcon.http.SocialAction;
import com.xxx.galcon.social.Authorizer;
import com.xxx.galcon.social.FacebookAuthorization;
import com.xxx.galcon.social.GooglePlusAuthorization;

public class AndroidSocialAction implements SocialAction {

	private MainActivity activity;

	private Authorizer authorizer;

	private AuthenticationListener authListener;

	public AndroidSocialAction(MainActivity activity) {
		this.activity = activity;
	}

	@Override
	public void signIn(final String authProvider) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				if (Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE.equals(authProvider)) {
					authorizer = new GooglePlusAuthorization(activity);
				}else if(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK.equals(authProvider)){
					authorizer = new FacebookAuthorization(activity);
				}

				authorizer.signIn(authListener);
			}
		});
	}

	@Override
	public void registerSignInListener(final AuthenticationListener authListener) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AndroidSocialAction.this.authListener = authListener;
			}
		});
	}

	@Override
	public void onActivityResult(int responseCode) {
		authorizer.onActivityResult(responseCode);
	}

	@Override
	public void getToken(AuthenticationListener listener) {
		setupAuthorizer();

		if (authorizer == null) {
			listener.onSignInFailed("Select login provider");
		} else {
			authorizer.getToken(listener);
		}
	}

	private void setupAuthorizer() {
		if (authorizer != null) {
			return;
		}
		Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
		String authProvider = prefs.getString(Constants.Auth.SOCIAL_AUTH_PROVIDER, "");
		setupAuthorizer(authProvider);
	}
	
	private void setupAuthorizer(String authProvider){
		if (Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE.equals(authProvider)) {
			authorizer = new GooglePlusAuthorization(activity);
		} else if(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK.equals(authProvider)){
			authorizer = new FacebookAuthorization(activity);
		}
	}

	@Override
	public void getFriends(final FriendsListener listener) {
		setupAuthorizer();
		activity.runOnUiThread(new Runnable() {
			public void run() {
				authorizer.getFriends(listener);
			}
		});
		
	}

	@Override
	public void addAuthDetails(final AuthenticationListener listener, final String authProvider) {
		setupAuthorizer(authProvider);
		activity.runOnUiThread(new Runnable() {
			public void run() {
				authorizer.signIn(listener);
			}
		});		
	}

}
