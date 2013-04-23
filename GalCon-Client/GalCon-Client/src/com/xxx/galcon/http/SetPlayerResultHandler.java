package com.xxx.galcon.http;

import com.xxx.galcon.model.Player;

public class SetPlayerResultHandler implements ConnectionResultCallback<Player> {
	
	private Player player;

	public SetPlayerResultHandler(Player player) {
		super();
		this.player = player;
	}

	@Override
	public void result(Player result) {
		this.player = result;
	}
	
	

}
