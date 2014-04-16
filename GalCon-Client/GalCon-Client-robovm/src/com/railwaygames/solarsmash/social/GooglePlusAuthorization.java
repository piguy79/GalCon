package com.railwaygames.solarsmash.social;

import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.foundation.NSString;
import org.robovm.bindings.gpp.GPPSignIn;
import org.robovm.bindings.gpp.GPPSignInDelegate;
import org.robovm.bindings.gt.GTMOAuth2Authentication;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.http.AuthenticationListener;
import com.railwaygames.solarsmash.http.FriendPostListener;
import com.railwaygames.solarsmash.http.FriendsListener;

public class GooglePlusAuthorization implements Authorizer {
	private GPPSignIn gppSignIn;
	private String clientId = "1066768766862-7jltmge7ot8nje89hd47qu45jeir4dij.apps.googleusercontent.com";
	private NSString scopes = new NSString("https://www.googleapis.com/auth/plus.login");

	private AuthenticationListener listener;

	public class SignInDelegate extends GPPSignInDelegate.Adapter {
		@Override
		public void finishedWithAuth(GTMOAuth2Authentication auth, NSError error) {
			if (error != null) {
				listener.onSignInFailed(error.description());
			} else {
				GameLoop.USER.addAuthProvider(Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE, gppSignIn.getUserId()
						.toString());

				Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
				prefs.putString(Constants.ID, gppSignIn.getUserId().toString());
				prefs.flush();

				// hacky, but parse the access token out as it's not exposed
				String t = auth.description();
				int first = t.indexOf("\"");
				String token = t.substring(first + 1, t.indexOf("\"", first + 1));

				listener.onSignInSucceeded(Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE, token);
			}
		}
	}

	public GooglePlusAuthorization() {
		gppSignIn = GPPSignIn.sharedInstance();
		gppSignIn.setShouldFetchGooglePlusUser(true);
		gppSignIn.setShouldFetchGoogleUserID(true);
		gppSignIn.setClientID(clientId);
		gppSignIn.setScopes(new NSArray<NSString>(scopes));
		gppSignIn.setDelegate(new SignInDelegate());
	}

	@Override
	public void signIn(AuthenticationListener listener) {
		this.listener = listener;
		gppSignIn.authenticate();
	}

	@Override
	public void getToken(AuthenticationListener listener) {
		this.listener = listener;
		gppSignIn.authenticate();
	}

	@Override
	public void onActivityResult(int responseCode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void getFriends(FriendsListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postToFriend(FriendPostListener listener, String id) {
		// TODO Auto-generated method stub

	}
}
