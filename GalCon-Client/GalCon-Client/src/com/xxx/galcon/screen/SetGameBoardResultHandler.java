package com.xxx.galcon.screen;

import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.GameBoard;

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
