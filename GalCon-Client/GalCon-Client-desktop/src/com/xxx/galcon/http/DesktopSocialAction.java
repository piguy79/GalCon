package com.xxx.galcon.http;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;

public class DesktopSocialAction implements SocialAction {

	private AuthenticationListener listener;

	@Override
	public void signIn(String provider) {
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
						String email = "me" + rand + "@fake.com";
						GameLoop.USER.email = email;

						Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
						prefs.putString(Constants.EMAIL, GameLoop.USER.email);
						prefs.flush();

						listener.onSignInSucceeded(Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE, "FAKE_TOKEN");
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

	
}
