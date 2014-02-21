package com.xxx.galcon.model.friends;

public class GalconSocialUser extends GalConFriend {
	
	private String socialDisplayName;

	public GalconSocialUser(String authId, String url, String handle, int rank, String displayName) {
		super(authId, url, handle, rank);
		this.socialDisplayName = displayName;
		
	}
	
	@Override
	public String getDisplay(){
		return socialDisplayName + "\n" + super.getDisplay();
	}

}
