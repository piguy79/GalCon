package com.xxx.galcon.screen;


import java.util.Collections;
import java.util.Comparator;
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
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
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
	private TextButtonStyle greenButtonStyle;
	private TextButtonStyle blackGreyButtonStyle;
	
	private TextButton watchAd;
	private TextButton timeRemaining;
	
	private ImageButton backButton;
	
	private Object returnValue;
	private InputProcessor oldInputProcessor;
	private MoveToAction move;
	private TextureRegionDrawable textureRegionDrawable;
	private TextureRegionDrawable bgTexture;
	
	private boolean loadInventory = false;
	

	public NoMoreCoinsDialog(Skin skin, AssetManager assetManager) {
		super();
		this.skin = skin;
		
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
		
		stage = new Stage();
		
		bgTexture = new TextureRegionDrawable(new TextureRegion(bg));
				
		timeRemaining = new TextButton(findTimeRemaining(), skin, "blackGreyButton");
		
		setupWatchAdButton();
		
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

	private void createBackButton(final float width, final float height) {
		backButton = new ImageButton(skin, "backButton");
		backButton.setWidth(width * 0.18f);
		backButton.setHeight(width * 0.18f);
		backButton.setPosition(-(width * 0.01f), height * 0.89f);
		
		move = new MoveToAction();
		move.setPosition(width * 0.01f, height * 0.89f);
		move.setDuration(0.3f);
		
		backButton.addAction(move);
		backButton.addListener(new InputListener(){
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
		stage.addActor(backButton);
	}

	private void createSkinWithDefaultStyle() {
		skin.add("default", new LabelStyle(Fonts.getInstance().largeFont(), Color.GRAY));
	}

	private void loadAssetsFromManager(AssetManager assetManager) {
		rectangleGreen = assetManager.get("data/images/green_button.png", Texture.class);
		blackGreyButton = assetManager.get("data/images/black_grey_button.png", Texture.class);
		bg = assetManager.get("data/images/level_select_bg.png", Texture.class);
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
		
		
		setupLayoutDefaultPosition(width, height, bgTexture);

		layout.add(timeRemaining).expandX().colspan(2).padBottom(height * 0.05f);
		layout.row();
		layout.add(createInAppBillingButtons(layout, stock.inventory)).expandX();
		stage.addActor(layout);
		createBackButton(width, height);
	}

	private Actor createInAppBillingButtons(Table layout2, List<InventoryItem> inventory) {
		Table scrollTable = new Table();
		
		scrollTable.add(watchAd).expandX().fill().padBottom(10f);
		scrollTable.row();
		
		Collections.sort(inventory, new Comparator<InventoryItem>() {

			@Override
			public int compare(InventoryItem o1, InventoryItem o2) {
				if(o1.numCoins < o2.numCoins){
					return -1;
				}else if(o1.numCoins > o2.numCoins){
					return 1;
				}
				return 0;
			}
		});
		
		for(InventoryItem item : inventory){
			TextButton button = new PaymentButton(item, skin, "greenButton", this);
			button.setWidth(Gdx.graphics.getWidth() * 0.8f);
			scrollTable.add(button).expandX().padBottom(10f);
			scrollTable.row();
		}
		
		
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
		if(!loadInventory){
			loadInventory = true;
			ExternalActionWrapper.loadStoreInventory(this);
		}
		handleWatchAd();
		
		stage.act(delta);
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
		loadInventory = false;
		stage = new Stage();
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
