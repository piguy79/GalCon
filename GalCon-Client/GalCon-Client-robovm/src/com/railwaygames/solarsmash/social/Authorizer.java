package com.railwaygames.solarsmash.social;

import com.railwaygames.solarsmash.http.AuthenticationListener;
import com.railwaygames.solarsmash.http.FriendPostListener;
import com.railwaygames.solarsmash.http.FriendsListener;

public interface Authorizer {

	public void signIn(AuthenticationListener listener);

	public void onActivityResult(int responseCode);

	public void getToken(AuthenticationListener listener);

	public void getFriends(FriendsListener listener);

	public void postToFriend(FriendPostListener listener, String id);
}
