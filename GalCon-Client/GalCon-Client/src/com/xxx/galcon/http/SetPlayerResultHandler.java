package com.xxx.galcon.http;

import com.xxx.galcon.model.Player;

public class SetPlayerResultHandler implements UIConnectionResultCallback<Player> {
	
	private Player player;

	public SetPlayerResultHandler(Player player) {
		super();
		this.player = player;
	}

	@Override
	public void onConnectionResult(Player result) {
		this.player.rank = result.rank;
		this.player.currentGames = result.currentGames;
		this.player.xp = result.xp;
		this.player.handle = result.handle;
	}

	@Override
	public void onConnectionError(String msg) {
		// TODO Auto-generated method stub
	}
}
