package com.xxx.galcon.http;

public interface AuthenticationListener {
	void onSignInFailed();

	void onSignInSucceeded(String token);

	void onSignOut();
}
