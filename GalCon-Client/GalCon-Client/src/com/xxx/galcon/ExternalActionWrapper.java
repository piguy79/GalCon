package com.xxx.galcon;

import com.xxx.galcon.http.GameAction;

public class ExternalActionWrapper {
	
	private static GameAction gameAction;

	private ExternalActionWrapper() {
		super();
	}

	public static void setGameAction(GameAction gameAction) {
		ExternalActionWrapper.gameAction = gameAction;
	}
	
	public static void showAd(){
		gameAction.showAd();
	}

}
