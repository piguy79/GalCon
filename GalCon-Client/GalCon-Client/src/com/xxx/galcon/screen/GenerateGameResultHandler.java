package com.xxx.galcon.screen;

import com.xxx.galcon.http.ConnectionResultCallback;
import com.xxx.galcon.model.GameBoard;

public class GenerateGameResultHandler implements ConnectionResultCallback<GameBoard> {

	private BoardScreen boardScreen;

	public GenerateGameResultHandler(BoardScreen boardScreen) {
		this.boardScreen = boardScreen;
	}

	@Override
	public void result(GameBoard result) {
		boardScreen.setGameBoard(result);
	}
}
