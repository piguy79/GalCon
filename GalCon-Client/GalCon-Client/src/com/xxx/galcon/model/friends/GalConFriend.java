package com.xxx.galcon.model.friends;

import com.xxx.galcon.config.ConfigResolver;

public class GalConFriend extends CombinedFriend{
	
	public String handle;
	private int xp;

	public GalConFriend(String authId, String url, String handle, int xp) {
		super(authId, url);
		this.handle = handle;
		this.xp = xp;
	}

	@Override
	public boolean hasGalconAccount() {
		return true;
	}

	@Override
	public String getDisplay() {
		return handle + "[" + ConfigResolver.getRankForXp(xp).level +  "]";
	}

}
