package com.xxx.galcon;

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
}
