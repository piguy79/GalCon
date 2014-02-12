package com.xxx.galcon.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.GameQueue;
import com.xxx.galcon.model.GameQueueItem;
import com.xxx.galcon.screen.widget.ScrollList;

public class GameQueueScreen implements ScreenFeedback {
	private MenuScreenContainer previousScreen;
	private InputProcessor oldInputProcessor;
	
	private Stage stage;
	
	private String returnCode = null;
	
	private ScrollList<GameQueueItem> scrollList;


	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		stage.act(delta);
		stage.draw();
		
	}
	
	private void initialize(){
		
		showQueueItems();
		
		Gdx.input.setInputProcessor(stage);
	}

	private void showQueueItems() {
		UIConnectionWrapper.findPendingInvites(new UIConnectionResultCallback<GameQueue>() {
			
			@Override
			public void onConnectionResult(GameQueue result) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onConnectionError(String msg) {
				// TODO Auto-generated method stub
				
			}
		}, GameLoop.USER.handle);
	}

	@Override
	public void resize(int width, int height) {		
	}

	@Override
	public void show() {
		stage = new Stage();
		initialize();
		oldInputProcessor = Gdx.input.getInputProcessor();
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(oldInputProcessor);
	}

	@Override
	public void pause() {		
	}

	@Override
	public void resume() {		
	}

	@Override
	public void dispose() {		
	}

	@Override
	public Object getRenderResult() {
		return returnCode;
	}

	@Override
	public void resetState() {
		returnCode = null;		
	}

}
