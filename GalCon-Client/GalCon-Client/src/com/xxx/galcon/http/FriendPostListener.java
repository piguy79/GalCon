package com.xxx.galcon.http;

public interface FriendPostListener {
	
	public void onPostSucceeded();

	public void onPostFails(String msg);
	
	public void onPostCancelled();
}
