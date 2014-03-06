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
		this.player.xp = result.xp;
		this.player.handle = result.handle;
		this.player.coins = result.coins;
		this.player.usedCoins = result.usedCoins;
		this.player.watchedAd = result.watchedAd;
	}

	@Override
	public void onConnectionError(String msg) {
		// TODO Auto-generated method stub
	}
}
