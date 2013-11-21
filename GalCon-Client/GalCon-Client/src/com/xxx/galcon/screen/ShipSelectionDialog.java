package com.xxx.galcon.screen;

import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.screen.hud.DragButton;

public class ShipSelectionDialog extends TouchRegion implements ScreenFeedback {
	private static final String OK = "ok";
	private static final String CANCEL = "cancel";
	private static final String DRAG = "drag";
	
	private DragButton shipDragButton;

	private SpriteBatch spriteBatch;
	private AtlasRegion dialogTextureBg;
	private AtlasRegion shipTex;
	private AtlasRegion okButtonTex;
	private AtlasRegion cancelButtonTex;

	private int shipsToSend = 0;
	private Move currentMoveToEdit = null;
	private int max;
	private Color alphaAnimColor = new Color(1.0f, 1.0f, 1.0f, 0.0f);

	private String returnResult;
	private String pendingReturnResult;

	private TweenManager tweenManager;
	private boolean isBaseDialogReady = false;
	private boolean isReady = false;
	private boolean isShownAndClosed = false;
	
	private ImageButton cancelButton;
	private ImageButton okButton;
	
	private Stage stage;
	private Table shipSelectionTable;
	
	private UISkin skin;
	
	private InputProcessor oldInputProcessor;
	
	private Slider slider;

	public ShipSelectionDialog(Move currentMoveToEdit, int x, int y, int width, int height, AssetManager assetManager,
			int max, UISkin skin) {
		this(x, y, width, height, assetManager, max, skin);
		if (currentMoveToEdit != null) {
			this.shipsToSend = currentMoveToEdit.shipsToMove;
			this.currentMoveToEdit = currentMoveToEdit;
		}
	}

	public ShipSelectionDialog(int x, int y, int width, int height, AssetManager assetManager, final int max, UISkin skin) {
		super(x, y, width, height, false);
		this.max = max;
		this.skin = skin;
		
		this.stage = new Stage();

		TextureAtlas menusAtlas = assetManager.get("data/images/menus.atlas", TextureAtlas.class);
		TextureAtlas gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);
		
		Drawable sliderBg = new TextureRegionDrawable(gameBoardAtlas.findRegion("ship_selection_dialog_bg"));
		Drawable sliderKnob =  new TextureRegionDrawable(gameBoardAtlas.findRegion("ship"));
		
		sliderBg.setMinHeight(height * 0.2f);
		sliderKnob.setMinHeight(height * 0.3f);
		sliderKnob.setMinWidth(width * 0.1f);
		
		skin.add("default-horizontal", new SliderStyle(sliderBg, sliderKnob));


		okButtonTex = menusAtlas.findRegion("ok_button");
		cancelButtonTex = menusAtlas.findRegion("cancel_button");
		dialogTextureBg = gameBoardAtlas.findRegion("ship_selection_dialog_bg");
		shipTex = gameBoardAtlas.findRegion("ship");
		
		

		createLayout();
	}

	private void createLayout() {
		shipSelectionTable = new Table();
		shipSelectionTable.setBackground(new TextureRegionDrawable(dialogTextureBg));
		shipSelectionTable.setWidth(width);
		shipSelectionTable.setHeight(height);
		shipSelectionTable.setX(-width);
		shipSelectionTable.setY(y);
		
		
		
		MoveToAction moveTo = new MoveToAction();
		moveTo.setPosition(width * 0.09f, shipSelectionTable.getY());
		moveTo.setDuration(0.2f);
		shipSelectionTable.addAction(moveTo);
		
		stage.addActor(shipSelectionTable);
		
		addCancelButton();
		addOkButton();
		addSlider();		
	}

	private void addSlider() {
		slider = new Slider(0, max, 1, false, skin);
		slider.setWidth(width * 0.75f);
		slider.setY(y  + (shipSelectionTable.getHeight() * 0.4f));
		slider.setX(width * 0.2f);
		slider.addAction(Actions.sequence(Actions.fadeOut(0.001f),Actions.delay(0.2f), Actions.fadeIn(0.4f)));
		
		stage.addActor(slider);
	}
	
	private void addCancelButton() {
		cancelButton = new ImageButton(skin, "cancelButton");
		cancelButton.setWidth(width * 0.15f);
		cancelButton.setHeight(width * 0.15f);
		cancelButton.setY(y);
		cancelButton.setX(width * 0.08f);
		cancelButton.addListener(new InputListener(){
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				pendingReturnResult = Action.DIALOG_DELETE;
				hide();
			}
		});
		cancelButton.addAction(Actions.sequence(Actions.delay(0.2f), Actions.fadeIn(0.4f)));
		stage.addActor(cancelButton);
		
	}
	
	private void addOkButton() {
			float buttonSize = width * 0.15f;
			
			okButton = new ImageButton(skin, "okButton");
			okButton.setX(width - (buttonSize * 0.5f));
			okButton.setY(y);
			okButton.setWidth(width * 0.15f);
			okButton.setHeight(width * 0.15f);
			okButton.setColor(0, 0, 0, 0);
			okButton.addListener(new InputListener(){
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					return true;
				}

				@Override
				public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
					pendingReturnResult = Action.HARVEST;
					hide();
				}
			});

			okButton.addAction(Actions.sequence(Actions.delay(0.2f), Actions.fadeIn(0.4f)));
			stage.addActor(okButton);
			
		}

	public boolean isReady() {
		return isReady;
	}


	@Override
	public void render(float delta) {
		if (pendingReturnResult != null && isShownAndClosed) {
			returnResult = pendingReturnResult;
		}
		
		stage.act(delta);
		stage.draw();
		
	}

	

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		oldInputProcessor = Gdx.input.getInputProcessor();
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void hide() {
		
		cancelButton.addAction(Actions.fadeOut(0.2f));
		okButton.addAction(Actions.fadeOut(0.2f));
		slider.addAction(Actions.fadeOut(0.2f));
		MoveToAction moveTo = new MoveToAction();
		moveTo.setPosition(-width, shipSelectionTable.getY());
		moveTo.setDuration(0.2f);
		shipSelectionTable.addAction(Actions.sequence(Actions.delay(0.2f) ,moveTo, Actions.run(new Runnable() {	
			@Override
			public void run() {
				isShownAndClosed = true;
				Gdx.input.setInputProcessor(oldInputProcessor);
			}
		})));
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
		hide();
	}

	@Override
	public Object getRenderResult() {
		if (currentMoveToEdit != null && returnResult != null) {
			if (returnResult.equals(Action.DIALOG_OK)) {
				return Action.DIALOG_UPDATE;
			} else {
				return Action.DIALOG_DELETE;
			}
		}
		return returnResult;
	}

	public int getShipsToSend() {
		return shipsToSend;
	}

	@Override
	public void resetState() {

	}

	public void setCurrentMoveToEdit(Move currentMoveToEdit) {
		this.currentMoveToEdit = currentMoveToEdit;
	}

	public Move getCurrentMoveToEdit() {
		return currentMoveToEdit;
	}

	
}
