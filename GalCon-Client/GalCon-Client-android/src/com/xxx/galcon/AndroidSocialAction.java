package com.xxx.galcon;

import com.xxx.galcon.http.AuthenticationListener;
import com.xxx.galcon.http.SocialAction;
import com.xxx.galcon.social.Authorizer;
import com.xxx.galcon.social.GooglePlusAuthorization;

public class AndroidSocialAction implements SocialAction {

	private MainActivity activity;

	private Authorizer authorizer;

	private AuthenticationListener authListener;

	public AndroidSocialAction(MainActivity activity) {
		this.activity = activity;
	}

	@Override
	public void signIn(final String authProvider) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				if (Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE.equals(authProvider)) {
					authorizer = new GooglePlusAuthorization(activity);
				}

				authorizer.signIn(authListener);
			}
		});
	}

	@Override
	public void registerSignInListener(final AuthenticationListener authListener) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AndroidSocialAction.this.authListener = authListener;
			}
		});
	}

	@Override
	public void onActivityResult(int responseCode) {
		authorizer.onActivityResult(responseCode);
	}

	@Override
	public void getToken(AuthenticationListener listener) {
		authorizer.getToken(listener);
	}
}
