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
		this.player.rank = result.rank;
		this.player.currentGames = result.currentGames;
		this.player.xp = result.xp;
	}
	
	

}
