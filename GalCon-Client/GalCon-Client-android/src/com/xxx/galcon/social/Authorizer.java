package com.xxx.galcon.social;

import com.xxx.galcon.http.AuthenticationListener;
import com.xxx.galcon.http.FriendsListener;

public interface Authorizer {

	public void signIn(AuthenticationListener listener);

	public void onActivityResult(int responseCode);
	
	public void getToken(AuthenticationListener listener);
	
	public void getFriends(FriendsListener listener);
	
		
}
