package com.xxx.galcon.screen;


import java.util.List;

import org.joda.time.DateTime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.jirbo.adcolony.AdColonyVideoListener;
import com.xxx.galcon.Constants;
import com.xxx.galcon.ExternalActionWrapper;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.SetPlayerResultHandler;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.inappbilling.util.StoreResultCallback;
import com.xxx.galcon.model.InventoryItem;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.model.Inventory;
import com.xxx.galcon.screen.widget.PaymentButton;


public class NoMoreCoinsDialog implements ScreenFeedback, UIConnectionResultCallback<Player>, StoreResultCallback<Inventory> {

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
	
	private boolean animated;
	

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
		
		setupWatchAdButton();
		createPlanetView();
		ExternalActionWrapper.loadStoreInventory(this);

		
	}

	private void createPlanetView() {
		deadPlanetButton = new ImageButton(skin, "deadPlanet");	
		sunButton = new ImageButton(skin, "sun");
		sunButton.addAction(Actions.alpha(0f));
		
		stack = new Stack();
		stack.add(sunButton);
		stack.add(deadPlanetButton);
	}

	private void setupWatchAdButton() {
		watchAd = new TextButton("Watch Ad", skin, "greenButton");	
		watchAd.addListener(new InputListener(){
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				
				if((null != GameLoop.USER.usedCoins || GameLoop.USER.usedCoins != -1) && !GameLoop.USER.watchedAd){
					ExternalActionWrapper.showAd(new AdColonyVideoListener() {
						
						@Override
						public void onAdColonyVideoStarted() {						
						}
						
						@Override
						public void onAdColonyVideoFinished() {
								GameLoop.USER.watchedAd = true;
								UIConnectionWrapper.reduceTimeUntilCoins(new SetPlayerResultHandler(GameLoop.USER), GameLoop.USER.handle,GameLoop.USER.timeRemainingForNewcoins(), GameLoop.USER.usedCoins);
						}
					});
				}				
			}
		});
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
		float padding = 0;
		layout.setWidth(width);
		layout.setHeight(height);
		layout.setBackground(bgTexture);
		layout.setPosition(-width, 0f);
		
		
		move = new MoveToAction();
		move.setPosition(padding/2, padding/2);
		move.setDuration(0.3f);
		
		layout.addAction(move);
		
	}

	private void createLayout(Inventory stock) {
		final float height = Gdx.graphics.getHeight();
		final float width = Gdx.graphics.getWidth();
		layout.add(new Label("More Coins", skin)).expandX().colspan(2).padBottom(height * 0.05f);
		layout.row();
		layout.add(stack).colspan(2);
		layout.row();
		layout.add(timeRemaining).expandX().colspan(2).padBottom(height * 0.05f);
		layout.row();
		layout.add(createInAppBillingButtons(layout, stock.inventory)).expandX();
		stage.addActor(layout);
		createCancelButton(width, height);
	}

	private Actor createInAppBillingButtons(Table layout2, List<InventoryItem> inventory) {
		Table scrollTable = new Table();
		
		for(InventoryItem item : inventory){
			TextButton button = new PaymentButton(item, skin, "greenButton", this);
			scrollTable.add(button).expandX().padBottom(10f);
			scrollTable.row();
		}
		
		scrollTable.add(watchAd).expandX().padBottom(10f);
		
		final ScrollPane scrollPane = new ScrollPane(scrollTable);
		scrollPane.setScrollingDisabled(true, false);
		
		return scrollPane;
		
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
		handleWatchAd();
		if(GameLoop.USER.coins > 0 && !animated){
			deadPlanetButton.addAction(Actions.fadeOut(0.750f));
			sunButton.addAction(Actions.sequence(Actions.fadeIn(0.750f), Actions.delay(1.5f)));
			animated = true;
		}
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();		
	}

	private void handleWatchAd() {
		watchAd.setDisabled(GameLoop.USER.watchedAd);
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
		ExternalActionWrapper.loadStoreInventory(this);
	}

	@Override
	public void onConnectionResult(Player result) {
		GameLoop.USER = result;
	}

	@Override
	public void onConnectionError(String msg) {
		
	}

	@Override
	public void onResult(Inventory result) {
		createLayout(result);	
		
	}


}
