package com.railwaygames.solarsmash.social;

import com.railwaygames.solarsmash.http.AuthenticationListener;
import com.railwaygames.solarsmash.http.FriendPostListener;
import com.railwaygames.solarsmash.http.FriendsListener;

public interface Authorizer {

	/**
	 * Expected that the authorize will invoke it's own sign in process, which
	 * could be out of process. When complete, the listener should be invoked to
	 * indicate success or failure.
	 * 
	 * @param listener
	 */
	public void signIn(AuthenticationListener listener);

	public void onActivityResult(int responseCode);

	/**
	 * Extract a unique token from the authprovider which is used to uniquely
	 * identify a user.When complete, the listener should be invoked to indicate
	 * success or failure.
	 * 
	 * @param listener
	 */
	public void getToken(AuthenticationListener listener);

	public void getFriends(FriendsListener listener);

	public void postToFriend(FriendPostListener listener, String id);
}
