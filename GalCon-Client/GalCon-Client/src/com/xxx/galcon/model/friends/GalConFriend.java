package com.xxx.galcon.model.friends;

public class GalConFriend extends CombinedFriend{
	
	public String handle;
	private int rank;

	public GalConFriend(String authId, String url, String handle, int rank) {
		super(authId, url);
		this.handle = handle;
		this.rank = rank;
	}

	@Override
	public boolean hasGalconAccount() {
		return true;
	}

	@Override
	public String getDisplay() {
		return handle + "[" + rank +  "]";
	}

}
