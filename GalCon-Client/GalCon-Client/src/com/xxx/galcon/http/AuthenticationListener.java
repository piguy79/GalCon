package com.xxx.galcon.http;

public interface AuthenticationListener {
	void onSignInFailed(String failureMessage);

	void onSignInSucceeded(String authProvider, String token);

	void onSignOut();
}
