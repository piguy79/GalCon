package com.railwaygames.solarsmash.screen.event;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.railwaygames.solarsmash.model.GameBoard;

public class AcceptInviteEvent extends Event{

	private boolean success;
	private GameBoard gameBoard;
	private String errorMessage;
	
	public AcceptInviteEvent(boolean success){
		this.success = success;
	}
	
	public AcceptInviteEvent(boolean success, GameBoard gameBoard){
		this.success = success;
		this.gameBoard = gameBoard;
	}
	
	public AcceptInviteEvent(boolean success, String errorMessage){
		this.success = success;
		this.errorMessage = errorMessage;
	}

	public boolean isSuccess() {
		return success;
	}
	
	public GameBoard getGameBoard() {
		return gameBoard;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
}
