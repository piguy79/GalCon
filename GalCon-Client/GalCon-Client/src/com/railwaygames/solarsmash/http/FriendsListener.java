package com.railwaygames.solarsmash.http;

import java.util.List;

import com.railwaygames.solarsmash.model.Friend;

public interface FriendsListener {
	
	void onFriendsLoadedSuccess(List<Friend> friends, String authProviderUsed);
	
	void onFriendsLoadedFail(String error);

}
