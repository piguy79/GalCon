package com.railwaygames.solarsmash.screen.event;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.railwaygames.solarsmash.model.GameBoard;

public class InviteEventListener implements EventListener {

	@Override
	public boolean handle(Event event) {
		if(event instanceof AcceptInviteEvent){
			handleAcceptInvite((AcceptInviteEvent) event);
		}else if(event instanceof DeclineInviteEvent){
			handleDeclineInvite((DeclineInviteEvent) event);
		}
		return false;
	}

	private void handleDeclineInvite(DeclineInviteEvent event) {
		if(event.isSuccess()){
			inviteDeclineSuccess();
		}else{
			inviteDeclineFail();
		}
		
	}

	private void handleAcceptInvite(AcceptInviteEvent event) {
		if(event.isSuccess()){
			inviteAcceptedSuccess(event.getGameBoard());
		}else{
			inviteAcceptedFail(event.getErrorMessage());
		}
		
	}
	
	public void inviteDeclineFail() {
	}

	public void inviteDeclineSuccess() {
	}

	public void inviteAcceptedFail(String errorMessage) {
	}

	public void inviteAcceptedSuccess(GameBoard gameBoard) {		
	}

	
	

}
