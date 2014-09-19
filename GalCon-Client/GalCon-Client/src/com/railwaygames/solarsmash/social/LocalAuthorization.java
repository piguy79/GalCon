package com.railwaygames.solarsmash.social;

import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.http.AuthenticationListener;
import com.railwaygames.solarsmash.http.FriendPostListener;
import com.railwaygames.solarsmash.http.FriendsListener;

public class LocalAuthorization implements Authorizer {

	@Override
	public void signIn(AuthenticationListener listener) {
		String token = findRandomToken();

		GameLoop.getUser().addAuthProvider(Constants.Auth.SOCIAL_AUTH_PROVIDER_LOCAL, token);

		Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
		prefs.putString(Constants.Auth.SOCIAL_AUTH_PROVIDER_LOCAL + Constants.ID, token);
		prefs.flush();
		
		listener.onSignInSucceeded(Constants.Auth.SOCIAL_AUTH_PROVIDER_LOCAL, token);
	}

	@Override
	public void onActivityResult(int responseCode) {
		throw new IllegalStateException("Method not supported");
	}

	@Override
	public void getToken(AuthenticationListener listener) {
		listener.onSignInSucceeded(Constants.Auth.SOCIAL_AUTH_PROVIDER_LOCAL, findRandomToken());
	}

	private String findRandomToken() {
		Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
		String installId = prefs.getString("INSTALL_ID");

		if (installId != null && installId.length() > 0) {
			return installId;
		}

		installId = UUID.randomUUID().toString();
		prefs.putString("INSTALL_ID", installId);
		prefs.flush();

		return installId;
	}

	@Override
	public void getFriends(FriendsListener listener) {
		throw new IllegalStateException("Method not supported");
	}

	@Override
	public void postToFriend(FriendPostListener listener, String id) {
		throw new IllegalStateException("Method not supported");
	}

}
