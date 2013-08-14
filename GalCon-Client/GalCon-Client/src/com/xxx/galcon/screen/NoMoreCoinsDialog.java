package com.xxx.galcon.screen;


import org.joda.time.DateTime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.Constants;
import com.xxx.galcon.ExternalActionWrapper;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.Player;


public class NoMoreCoinsDialog implements ScreenFeedback, UIConnectionResultCallback<Player> {

	private Stage stage;
	private Table layout;
	private Skin skin;
	private Texture rectangleGreen;
	private Texture bg;
	private Texture blackGreyButton;
	private Texture cancelButtonTexture;
	private Texture deadPlanet;
	private Texture sunTexture;
	private TextButtonStyle greenButtonStyle;
	private TextButtonStyle blackGreyButtonStyle;
	
	private TextButton moreCoins;
	private TextButton watchAd;
	private TextButton timeRemaining;
	
	private ImageButton cancelButton;
	private ImageButton deadPlanetButton;
	private ImageButton sunButton;
	
	private Object returnValue;
	private InputProcessor oldInputProcessor;
	private MoveToAction move;
	private TextureRegionDrawable textureRegionDrawable;
	private Stack stack;


	public NoMoreCoinsDialog(AssetManager assetManager) {
		super();
		final float width = Gdx.graphics.getWidth(), height = Gdx.graphics.getHeight();
		
		loadAssetsFromManager(assetManager);	
		createSkinWithDefaultStyle();
		
		textureRegionDrawable = new TextureRegionDrawable(new TextureRegion(rectangleGreen));
		greenButtonStyle = new TextButtonStyle(textureRegionDrawable, textureRegionDrawable,
				textureRegionDrawable);
		greenButtonStyle.font = Fonts.getInstance().mediumFont();
		greenButtonStyle.font.setColor(Color.GRAY);	
		skin.add("greenButton", greenButtonStyle);

		
		
		textureRegionDrawable = new TextureRegionDrawable(new TextureRegion(blackGreyButton));
		blackGreyButtonStyle = new TextButtonStyle(textureRegionDrawable, textureRegionDrawable,
				textureRegionDrawable);
		blackGreyButtonStyle.font = Fonts.getInstance().mediumFont();
		blackGreyButtonStyle.font.setColor(Color.GRAY);
		skin.add("blackGreyButton", blackGreyButtonStyle);
		
		textureRegionDrawable = new TextureRegionDrawable(new TextureRegion(cancelButtonTexture));
		skin.add("cancelButton", new ImageButtonStyle(textureRegionDrawable, textureRegionDrawable,
				textureRegionDrawable, textureRegionDrawable, textureRegionDrawable, textureRegionDrawable));
		
		textureRegionDrawable = new TextureRegionDrawable(new TextureRegion(deadPlanet));
		skin.add("deadPlanet", new ImageButtonStyle(textureRegionDrawable, textureRegionDrawable,
				textureRegionDrawable, textureRegionDrawable, textureRegionDrawable, textureRegionDrawable));
		
		textureRegionDrawable = new TextureRegionDrawable(new TextureRegion(sunTexture));
		skin.add("sun", new ImageButtonStyle(textureRegionDrawable, textureRegionDrawable,
				textureRegionDrawable, textureRegionDrawable, textureRegionDrawable, textureRegionDrawable));
		
		
		
		TextureRegionDrawable bgTexture = new TextureRegionDrawable(new TextureRegion(bg));


		
		stage = new Stage();
		setupLayoutDefaultPosition(width, height, bgTexture);
				
		timeRemaining = new TextButton(findTimeRemaining(), skin, "blackGreyButton");	
		final UIConnectionResultCallback<Player> callback = this;
		
		Label coinsLabel = new Label("2 Coins:", skin);
		moreCoins = new TextButton("0.99", skin, "greenButton");
		moreCoins.addListener(new InputListener(){
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				UIConnectionWrapper.addCoins(callback, GameLoop.USER.handle, 2L, GameLoop.USER.usedCoins);
			}
		});

		
		Label adLabel = new Label("1/2 Time:", skin);
		watchAd = new TextButton("Watch Ad", skin, "greenButton");	
		watchAd.addListener(new InputListener(){
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				ExternalActionWrapper.showAd();
				if(null != GameLoop.USER.usedCoins || GameLoop.USER.usedCoins != -1){
					UIConnectionWrapper.reduceTimeUntilCoins(callback, GameLoop.USER.handle,GameLoop.USER.timeRemainingForNewcoins(), GameLoop.USER.usedCoins);
				}
			}
		});
		
		
		
		deadPlanetButton = new ImageButton(skin, "deadPlanet");	
		sunButton = new ImageButton(skin, "sun");
		sunButton.addAction(Actions.alpha(0f));
		
		stack = new Stack();
		stack.add(sunButton);
		stack.add(deadPlanetButton);
		
		
		createLayout(height, coinsLabel, adLabel);
		createCancelButton(width, height);
		
	}

	private void createCancelButton(final float width, final float height) {
		cancelButton = new ImageButton(skin, "cancelButton");
		cancelButton.setWidth(width * 0.18f);
		cancelButton.setHeight(width * 0.18f);
		cancelButton.setPosition(-(width * 0.78f), height * 0.87f);
		
		move = new MoveToAction();
		move.setPosition(width * 0.78f, height * 0.87f);
		move.setDuration(0.3f);
		
		cancelButton.addAction(move);
		cancelButton.addListener(new InputListener(){
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				stage.addAction(Actions.sequence(Actions.moveTo(-(width), 0f, 0.4f), Actions.run(new Runnable() {	
					@Override
					public void run() {
						returnValue = Action.DIALOG_CANCEL;
					}
				})));
			}
		});
		stage.addActor(cancelButton);
	}

	private void createSkinWithDefaultStyle() {
		skin = new Skin();
		skin.add("default", new LabelStyle(Fonts.getInstance().largeFont(), Color.GRAY));
	}

	private void loadAssetsFromManager(AssetManager assetManager) {
		rectangleGreen = assetManager.get("data/images/green_button.png", Texture.class);
		blackGreyButton = assetManager.get("data/images/black_grey_button.png", Texture.class);
		bg = assetManager.get("data/images/coins_bg.png", Texture.class);
		cancelButtonTexture = assetManager.get("data/images/cancel_button.png", Texture.class);
		deadPlanet = assetManager.get("data/images/planets/dead_planet.png", Texture.class);
		sunTexture = assetManager.get("data/images/planets/sun.png", Texture.class);
	}

	private void setupLayoutDefaultPosition(final float width,
			final float height, TextureRegionDrawable bgTexture) {
		layout = new Table(skin);
		float padding = width * 0.1f;
		layout.setWidth(width - padding);
		layout.setHeight(height - padding);
		layout.setBackground(bgTexture);
		layout.setPosition(-width, 0f);
		
		
		move = new MoveToAction();
		move.setPosition(padding/2, padding/2);
		move.setDuration(0.3f);
		
		layout.addAction(move);
	}

	private void createLayout(final float height, Label coinsLabel,
			Label adLabel) {
		layout.add(new Label("No More Coins", skin)).expandX().colspan(2).padBottom(height * 0.05f);
		layout.row();
		layout.add(stack).colspan(2);
		layout.row();
		layout.add(timeRemaining).expandX().colspan(2).padBottom(height * 0.1f);
		layout.row();
		layout.add(coinsLabel).padLeft(20f).padBottom(10f);
		layout.add(moreCoins).expandX().padBottom(10f);
		layout.row();
		layout.add(adLabel).padLeft(20f).padBottom(10f);
		layout.add(watchAd).expandX().padBottom(10f);
		stage.addActor(layout);
	}

	private String findTimeRemaining() {
		DateTime timeRemaining = GameLoop.USER.timeRemainingUntilCoinsAvailable();
		
		if(timeRemaining == null){
			return GameLoop.USER.coins + " Coin(s) available!";
		}
		return Constants.timeRemainingFormat.format(timeRemaining.toDate());
	}

	public void resize(int width, int height) {
		stage.setViewport(width, height, true);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		timeRemaining.setText(findTimeRemaining());
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();		
	}

	public void dispose() {
		stage.dispose();
	}

	@Override
	public void show() {
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
	public Object getRenderResult() {
		return returnValue;
	}

	@Override
	public void resetState() {
		returnValue = null;		
	}

	@Override
	public void onConnectionResult(Player result) {
		GameLoop.USER = result;
		if(result.coins > 0){
			deadPlanetButton.addAction(Actions.fadeOut(0.750f));
			sunButton.addAction(Actions.sequence(Actions.fadeIn(0.750f), Actions.delay(1.5f)));
		}
	}

	@Override
	public void onConnectionError(String msg) {
		
	}


}
