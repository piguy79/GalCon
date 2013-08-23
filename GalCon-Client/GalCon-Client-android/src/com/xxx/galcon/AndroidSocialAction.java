package com.xxx.galcon;

import com.xxx.galcon.http.GooglePlusSignInListener;
import com.xxx.galcon.http.SocialAction;

public class AndroidSocialAction implements SocialAction {

	private MainActivity activity;

	public AndroidSocialAction(MainActivity activity) {
		this.activity = activity;
	}

	@Override
	public void googlePlusSignIn() {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				activity.beginUserInitiatedSignIn();
			}
		});
	}

	@Override
	public void googlePlusSignOut() {
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
	public void registerGooglePlusSignInListener(final GooglePlusSignInListener signInListener) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				activity.registerGooglePlusSignInListener(signInListener);
			}
		});
	}

	@Override
	public void showLeaderboards() {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				activity.showLeaderboards();
			}
		});
	}
}
