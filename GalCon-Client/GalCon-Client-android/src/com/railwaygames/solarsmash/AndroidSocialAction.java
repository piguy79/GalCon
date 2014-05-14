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

public class AndroidSocialAction implements SocialAction {

	private MainActivity activity;

	private Authorizer authorizer;

	public AndroidSocialAction(MainActivity activity) {
		this.activity = activity;
	}

	@Override
	public void onActivityResult(int responseCode) {
		setupAuthorizer();
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

	private void setupAuthorizer(String authProvider) {
		if (Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE.equals(authProvider)) {
			authorizer = new GooglePlusAuthorization(activity);
		} else if (Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK.equals(authProvider)) {
			authorizer = new FacebookAuthorization(activity);
		}
	}

	@Override
	public void getFriends(final FriendsListener listener, String authProvider) {
		setupAuthorizer(authProvider);
		activity.runOnUiThread(new Runnable() {
			public void run() {
				authorizer.getFriends(listener);
			}
		});
	}

	@Override
	public void signIn(final AuthenticationListener listener, final String authProvider) {
		setupAuthorizer(authProvider);
		activity.runOnUiThread(new Runnable() {
			public void run() {
				authorizer.signIn(listener);
			}
		});
	}

	@Override
	public void postToFriends(final FriendPostListener listener, String authProvider, final String id) {
		setupAuthorizer(authProvider);
		activity.runOnUiThread(new Runnable() {
			public void run() {
				authorizer.postToFriend(listener, id);
			}
		});
	}
}
