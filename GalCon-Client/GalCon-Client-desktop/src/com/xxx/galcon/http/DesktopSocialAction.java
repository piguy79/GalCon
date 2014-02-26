package com.xxx.galcon.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.model.Friend;

public class DesktopSocialAction implements SocialAction {

	private AuthenticationListener listener;

	@Override
	public void signIn(final String provider) {
		(new Thread() {
			@Override
			public void run() {
				Random rand = new Random();
				try {
					Thread.sleep(Math.abs((rand.nextInt() % 5 + 1) * 1000));
				} catch (InterruptedException e) {

				}

				Gdx.app.postRunnable(new Runnable() {
					public void run() {
						int rand = (int) (Math.random() * 10000);
						String id = "me" + rand + ":google";
						GameLoop.USER.addAuthProvider(provider, id);
						
						Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
						prefs.putString(provider + Constants.ID, GameLoop.USER.auth.getID(provider));
						prefs.flush();

						listener.onSignInSucceeded(provider, "FAKE_TOKEN");
					}
				});
			}
		}).start();
	}

	@Override
	public void registerSignInListener(AuthenticationListener signInListener) {
		this.listener = signInListener;
	}

	@Override
	public void onActivityResult(int responseCode) {
		listener.onSignInSucceeded("google", "");
	}

	@Override
	public void getToken(AuthenticationListener listener) {
		listener.onSignInSucceeded(Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE, "FAKE_TOKEN");
	}

	@Override
	public void getFriends(FriendsListener listener, String authProvider) {
		Friend friend = new Friend("12345", "Pal", "url");
		List<Friend> friends = new ArrayList<Friend>();
		friends.add(friend);
		listener.onFriendsLoadedSuccess(friends, authProvider);
	}

	@Override
	public void addAuthDetails(AuthenticationListener listener,
			String authProvider) {
		signIn(authProvider);
		
	}

	@Override
	public void postToFriends(FriendPostListener listener, String authProvider,
			String id) {
		listener.onPostSucceeded();
		
	}

	
}
