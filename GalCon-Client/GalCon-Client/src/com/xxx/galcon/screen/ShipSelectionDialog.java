package com.xxx.galcon.screen;

import static com.xxx.galcon.Util.createShader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.Constants;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.screen.widget.ShaderLabel;

public class ShipSelectionDialog extends Actor implements ScreenFeedback {

	

	private AtlasRegion dialogTextureBg;

	private int shipsToSend = 0;
	private Move currentMoveToEdit = null;
	private int max;

	private String returnResult;
	private String pendingReturnResult;

	private boolean isReady = false;
	private boolean isShownAndClosed = false;
	
	private ImageButton cancelButton;
	private ImageButton okButton;
	
	private Stage stage;
	private Table shipSelectionTable;
	
	private UISkin skin;
	
	private InputProcessor oldInputProcessor;
	
	private Slider slider;
	ShaderLabel counter;
	ShaderLabel initialCount;
	
	private ShaderProgram fontShader;

	public ShipSelectionDialog(Move currentMoveToEdit, int x, int y, int width, int height, AssetManager assetManager,
			int max, UISkin skin) {
		this(x, y, width, height, assetManager, max, skin);
		if (currentMoveToEdit != null) {
			this.shipsToSend = currentMoveToEdit.shipsToMove;
			this.currentMoveToEdit = currentMoveToEdit;
			counter.setText(shipsToSend + "");
			initialCount.setText("" + (max - shipsToSend));
			slider.setValue(shipsToSend);
		}
	}

	public ShipSelectionDialog(int x, int y, int width, int height, AssetManager assetManager, final int max, UISkin skin) {
		this.max = max;
		this.skin = skin;
		
		this.stage = new Stage();
		
		fontShader = createShader("data/shaders/font-vs.glsl", "data/shaders/font-fs.glsl");
		counter = new ShaderLabel(fontShader, shipsToSend + "", skin, Constants.UI.DEFAULT_FONT_BLACK);
		initialCount = new ShaderLabel(fontShader, (max - shipsToSend) + "", skin, Constants.UI.DEFAULT_FONT_BLACK);

		TextureAtlas menusAtlas = assetManager.get("data/images/menus.atlas", TextureAtlas.class);
		TextureAtlas gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);
		
		Drawable sliderBg = new TextureRegionDrawable(menusAtlas.findRegion("slider_bg"));
		Drawable sliderKnob =  new TextureRegionDrawable(gameBoardAtlas.findRegion("ship"));
		
		sliderBg.setMinHeight(height * 0.2f);
		sliderKnob.setMinHeight(height * 0.3f);
		sliderKnob.setMinWidth(width * 0.1f);
		
		skin.add("default-horizontal", new SliderStyle(sliderBg, sliderKnob));

		dialogTextureBg = menusAtlas.findRegion("dialog_bg");
		
		

		createLayout();
	}

	private void createLayout() {
		shipSelectionTable = new Table();
		shipSelectionTable.setBackground(new TextureRegionDrawable(dialogTextureBg));
		shipSelectionTable.setWidth(getWidth());
		shipSelectionTable.setHeight(getHeight());
		shipSelectionTable.setX(-getWidth());
		shipSelectionTable.setY(getY());
		
		
		
		MoveToAction moveTo = new MoveToAction();
		moveTo.setPosition(getWidth() * 0.05f, shipSelectionTable.getY());
		moveTo.setDuration(0.2f);
		shipSelectionTable.addAction(moveTo);
		
		stage.addActor(shipSelectionTable);
		
		addCancelButton();
		addOkButton();
		addSlider();
		addCounter();

		
	}

	private void addCounter() {
		Table countTable = new Table();

		countTable.add(initialCount);
		countTable.add(new ShaderLabel(fontShader, " >> ", skin, Constants.UI.DEFAULT_FONT_BLACK));
		countTable.add(counter);
		
		countTable.setX(countTable.getX() + (shipSelectionTable.getWidth() * 0.5f));
		countTable.setY(countTable.getY() + (shipSelectionTable.getHeight() * 0.8f));
		
		shipSelectionTable.addActor(countTable);
	}

	private void addSlider() {
		slider = new Slider(0, max, 1, false, skin);
		slider.setWidth(getWidth() * 0.8f);
		slider.setY(getY()  + (shipSelectionTable.getHeight() * 0.35f));
		slider.setX(getWidth() * 0.15f);
		slider.addAction(Actions.sequence(Actions.fadeOut(0.001f),Actions.delay(0.2f), Actions.fadeIn(0.4f)));
		
		slider.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				counter.setText((int)slider.getValue() + "");
				shipsToSend = (int)slider.getValue();
				initialCount.setText("" + (max - shipsToSend));
			}
		});
		
		stage.addActor(slider);
	}
	
	private void addCancelButton() {
		cancelButton = new ImageButton(skin, "cancelButton");
		cancelButton.setWidth(getWidth() * 0.15f);
		cancelButton.setHeight(getWidth() * 0.15f);
		cancelButton.setY(getY() - (int)(getWidth() * 0.07));
		cancelButton.setX(getWidth() * 0.04f);
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
			float buttonSize = getWidth() * 0.08f;
			
			okButton = new ImageButton(skin, "okButton");
			okButton.setX(getWidth() - (buttonSize));
			okButton.setY(getY() - (int)(getWidth() * 0.07));
			okButton.setWidth(getWidth() * 0.15f);
			okButton.setHeight(getWidth() * 0.15f);
			okButton.setColor(0, 0, 0, 0);
			okButton.addListener(new InputListener(){
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					return true;
				}

				@Override
				public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
					pendingReturnResult = Action.DIALOG_OK;
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
		moveTo.setPosition(-getWidth(), shipSelectionTable.getY());
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
