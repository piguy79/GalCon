package com.railwaygames.solarsmash.model.friends;

public abstract class CombinedFriend {
	
	public String authId;
	public String url;
	
	public CombinedFriend(String authId, String url) {
		super();
		this.authId = authId;
		this.url = url;
	}
	
	public abstract boolean hasGalconAccount();
	
	public abstract String getDisplay();
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CombinedFriend){
			if(authId == ((CombinedFriend)obj).authId && hasGalconAccount() == ((CombinedFriend)obj).hasGalconAccount()){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
