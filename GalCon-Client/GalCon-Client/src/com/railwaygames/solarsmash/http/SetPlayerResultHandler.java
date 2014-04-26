package com.railwaygames.solarsmash.http;

import com.railwaygames.solarsmash.model.Player;

public class SetPlayerResultHandler implements UIConnectionResultCallback<Player> {
	
	private Player player;

	public SetPlayerResultHandler(Player player) {
		super();
		this.player = player;
	}

	@Override
	public void onConnectionResult(Player result) {
		this.player.xp = result.xp;
		this.player.handle = result.handle;
		this.player.coins = result.coins;
	}

	@Override
	public void onConnectionError(String msg) {
		// TODO Auto-generated method stub
	}
}
