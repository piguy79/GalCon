package com.xxx.galcon.model;

public class Move {
	public String fromPlanet;
	public String toPlanet;
	public int shipsToMove = 0;
	public float duration = 0;
	public String playerHandle;
	
	public boolean belongsToPlayer(Player player){
		return this.playerHandle.equals(player.handle);
	}
}
