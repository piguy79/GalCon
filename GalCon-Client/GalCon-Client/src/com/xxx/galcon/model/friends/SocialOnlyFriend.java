package com.xxx.galcon.model.friends;

public class SocialOnlyFriend extends CombinedFriend{
	
	private String displayName;

	public SocialOnlyFriend(String authId, String url, String displayName) {
		super(authId, url);
		this.displayName = displayName;
	}

	@Override
	public boolean hasGalconAccount() {
		return false;
	}

	@Override
	public String getDisplay() {
		return displayName;
	}

}
