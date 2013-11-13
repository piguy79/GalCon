package com.xxx.galcon.http;

public interface SocialAction {
	public void registerSignInListener(AuthenticationListener signInListener);

	public void signIn(String authProvider);

	public void signOut();

	public boolean isLoggedInToGooglePlus();
}
