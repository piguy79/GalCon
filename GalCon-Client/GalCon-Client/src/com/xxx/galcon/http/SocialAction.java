package com.xxx.galcon.http;

public interface SocialAction {
	public void registerGooglePlusSignInListener(GooglePlusSignInListener signInListener);

	public void googlePlusSignIn();

	public void googlePlusSignOut();

	public boolean isLoggedInToGooglePlus();

	public void showLeaderboards();
}
