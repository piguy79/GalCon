package com.xxx.galcon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.xxx.galcon.http.AuthenticationListener;
import com.xxx.galcon.http.FriendsListener;
import com.xxx.galcon.http.SocialAction;
import com.xxx.galcon.social.Authorizer;
import com.xxx.galcon.social.GooglePlusAuthorization;

public class IOSSocialAction implements SocialAction {
	private Authorizer authorizer;
	private AuthenticationListener authListener;

	@Override
	public void registerSignInListener(AuthenticationListener signInListener) {
		this.authListener = signInListener;
	}

	@Override
	public void signIn(String authProvider) {
		if (Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE.equals(authProvider)) {
			authorizer = new GooglePlusAuthorization();
		} else if (Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK.equals(authProvider)) {

		}

		authorizer.signIn(authListener);
	}

	@Override
	public void onActivityResult(int responseCode) {
		// android only
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

	@Override
	public void getFriends(FriendsListener listener) {
		// TODO Auto-generated method stub

	}

	private void setupAuthorizer() {
		if (authorizer != null) {
			return;
		}
		Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
		String authProvider = prefs.getString(Constants.Auth.SOCIAL_AUTH_PROVIDER, "");
		if (Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE.equals(authProvider)) {
			authorizer = new GooglePlusAuthorization();
		} else if (Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK.equals(authProvider)) {

		}
	}

}
