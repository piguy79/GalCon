package com.railwaygames.solarsmash.model.friends;

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
	
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

}
