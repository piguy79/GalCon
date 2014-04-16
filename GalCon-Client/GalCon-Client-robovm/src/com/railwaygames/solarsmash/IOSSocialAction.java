package com.railwaygames.solarsmash;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.railwaygames.solarsmash.http.AuthenticationListener;
import com.railwaygames.solarsmash.http.FriendPostListener;
import com.railwaygames.solarsmash.http.FriendsListener;
import com.railwaygames.solarsmash.http.SocialAction;
import com.railwaygames.solarsmash.social.Authorizer;
import com.railwaygames.solarsmash.social.FacebookAuthorization;
import com.railwaygames.solarsmash.social.GooglePlusAuthorization;

public class IOSSocialAction implements SocialAction {
	private Authorizer authorizer;
	private AuthenticationListener authListener;

	@Override
	public void signIn(final AuthenticationListener listener, final String authProvider) {
		setupAuthorizer(authProvider);
		authorizer.signIn(listener);
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

	private void setupAuthorizer() {
		if (authorizer != null) {
			return;
		}
		Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
		String authProvider = prefs.getString(Constants.Auth.SOCIAL_AUTH_PROVIDER, "");
		setupAuthorizer(authProvider);
	}

	private void setupAuthorizer(String authProvider) {
		if (Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE.equals(authProvider)) {
			authorizer = new GooglePlusAuthorization();
		} else if (Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK.equals(authProvider)) {
			authorizer = new FacebookAuthorization();
		}
	}

	@Override
	public void getFriends(final FriendsListener listener, String authProvider) {
		setupAuthorizer(authProvider);
		authorizer.getFriends(listener);
	}

	@Override
	public void postToFriends(FriendPostListener listener, String authProvider, String id) {
		setupAuthorizer(authProvider);
		authorizer.postToFriend(listener, id);
	}
}
