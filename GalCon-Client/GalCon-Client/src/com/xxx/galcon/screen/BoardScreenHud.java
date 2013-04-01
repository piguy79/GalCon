package com.xxx.galcon.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.xxx.galcon.ScreenFeedback;

public class BoardScreenHud implements ScreenFeedback {

	private AssetManager assetManager;
	private SpriteBatch spriteBatch;
	private Texture sendButton;
	private Texture endTurnButton;

	public BoardScreenHud(AssetManager assetManager) {
		spriteBatch = new SpriteBatch();
		sendButton = assetManager.get("data/images/arrow_right.png", Texture.class);
		endTurnButton = assetManager.get("data/images/end_turn.png", Texture.class);
	}

	@Override
	public void render(float delta) {
		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();

		spriteBatch.begin();

		spriteBatch.draw(sendButton, 15, 150, 80, 80);

		int buttonWidth = 80;
		spriteBatch.draw(endTurnButton, width - buttonWidth - 15, 160, buttonWidth, 50);

		spriteBatch.end();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getRenderResult() {
		// TODO Auto-generated method stub
		return null;
	}

}
