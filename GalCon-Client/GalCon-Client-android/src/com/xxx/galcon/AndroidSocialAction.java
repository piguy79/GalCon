package com.xxx.galcon;

import com.xxx.galcon.http.AuthenticationListener;
import com.xxx.galcon.http.SocialAction;

public class AndroidSocialAction implements SocialAction {

	private MainActivity activity;

	public AndroidSocialAction(MainActivity activity) {
		this.activity = activity;
	}

	@Override
	public void signIn(String authProvider) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				activity.beginUserInitiatedSignIn();
			}
		});
	}

	@Override
	public void signOut() {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				activity.signOut();
			}
		});
	}

	@Override
	public boolean isLoggedInToGooglePlus() {
		return activity.isSignedIn();
	}

	@Override
	public void registerSignInListener(final AuthenticationListener signInListener) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				activity.registerGooglePlusSignInListener(signInListener);
			}
		});
	}
}
