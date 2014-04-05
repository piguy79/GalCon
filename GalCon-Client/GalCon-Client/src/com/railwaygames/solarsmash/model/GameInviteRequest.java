package com.railwaygames.solarsmash.model;

public class GameInviteRequest {
	
	public String requesterHandle;
	public String inviteeHandle;
	public Long mapKey;
	
	
	public GameInviteRequest(String requesterHandle, String inviteeHandle, Long mapKey) {
		super();
		this.requesterHandle = requesterHandle;
		this.inviteeHandle = inviteeHandle;
		this.mapKey = mapKey;
	}
	
	
	

}
