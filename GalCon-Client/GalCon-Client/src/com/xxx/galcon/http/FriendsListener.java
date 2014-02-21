package com.xxx.galcon.http;

import java.util.List;

import com.xxx.galcon.model.Friend;

public interface FriendsListener {
	
	void onFriendsLoadedSuccess(List<Friend> friends);
	
	void onFriendsLoadedFail(String error);

}