package com.xxx.galcon.http;

public interface GooglePlusSignInListener {
	void onSignInFailed();

	void onSignInSucceeded();

	void onSignOut();
}
