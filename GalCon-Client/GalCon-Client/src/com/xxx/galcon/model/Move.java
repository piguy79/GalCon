package com.xxx.galcon.model;

public class Move {
	public String fromPlanet;
	public String toPlanet;
	public int shipsToMove = 0;
	public int duration = 0;
	public String player;
	
	public boolean belongsToPlayer(Player player){
		return this.player.equals(player.name);
	}
}
