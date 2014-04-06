package com.railwaygames.solarsmash.screen;

import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.model.GameBoard;

public class SetGameBoardResultHandler implements UIConnectionResultCallback<GameBoard> {

	private BoardScreen boardScreen;

	public SetGameBoardResultHandler(BoardScreen boardScreen) {
		this.boardScreen = boardScreen;
	}

	@Override
	public void onConnectionResult(GameBoard result) {
		boardScreen.setGameBoard(result);
	}

	@Override
	public void onConnectionError(String msg) {
		boardScreen.setConnectionError(msg);
	}
}
