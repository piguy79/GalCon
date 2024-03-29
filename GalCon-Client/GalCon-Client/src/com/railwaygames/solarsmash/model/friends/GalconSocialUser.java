package com.railwaygames.solarsmash.model.friends;

public class GalconSocialUser extends GalConFriend {
	
	private String socialDisplayName;

	public GalconSocialUser(String authId, String url, String handle, int xp, String displayName) {
		super(authId, url, handle, xp);
		this.socialDisplayName = displayName;
		
	}
	
	@Override
	public String getDisplay(){
		return socialDisplayName + "\n" + super.getDisplay();
	}
	
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

}
