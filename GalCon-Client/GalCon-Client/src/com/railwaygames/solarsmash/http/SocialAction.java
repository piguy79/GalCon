package com.railwaygames.solarsmash.http;

public interface SocialAction {
	public void registerSignInListener(AuthenticationListener signInListener);

	public void signIn(String authProvider);

	public void onActivityResult(int responseCode);
		
	public void getToken(AuthenticationListener listener);
	
	public void getFriends(FriendsListener listener, String authProvider);
	
	public void addAuthDetails(AuthenticationListener listener, String authProvider);
	
	public void postToFriends(FriendPostListener listener, String authProvider, String id);
	
}
