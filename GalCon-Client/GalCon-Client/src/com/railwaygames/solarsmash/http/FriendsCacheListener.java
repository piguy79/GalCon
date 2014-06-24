package com.railwaygames.solarsmash.http;

import java.util.List;

import com.railwaygames.solarsmash.model.Friend;

public class FriendsCacheListener implements FriendsListener {

	private FriendsListener delegate;
	private final long expireTime;
	private List<Friend> cache;

	public FriendsCacheListener(long expireInMilliseconds) {
		expireTime = System.currentTimeMillis() + expireInMilliseconds;
	}

	public void setDelegate(FriendsListener delegate) {
		this.delegate = delegate;
	}

	@Override
	public void onFriendsLoadedSuccess(List<Friend> friends, String authProviderUsed) {
		cache = friends;
		delegate.onFriendsLoadedSuccess(friends, authProviderUsed);
		this.delegate = null;
	}

	@Override
	public void onFriendsLoadedFail(String error) {
		delegate.onFriendsLoadedFail(error);
		this.delegate = null;
	}

	public List<Friend> getCache() {
		if (System.currentTimeMillis() > expireTime) {
			cache = null;
		}
		return cache;
	}

}
