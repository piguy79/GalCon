package com.xxx.galcon.screen.signin;

import static com.xxx.galcon.Util.createShader;

import org.joda.time.DateTime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.xxx.galcon.Constants;
import com.xxx.galcon.ExternalActionWrapper;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.PartialScreenFeedback;
import com.xxx.galcon.Strings;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.screen.Action;
import com.xxx.galcon.screen.GraphicsUtils;
import com.xxx.galcon.screen.widget.ShaderLabel;

public class MainMenuScreen implements PartialScreenFeedback {
	private SpriteBatch spriteBatch;
	private String returnValue;
	private GameAction gameAction;
	private Stage stage;

	private Image loadingFrame;
	private Image loadingBarHidden;
	private Image loadingBg;

	private float startX, endX;
	private float percent;

	private Actor loadingBar;

	private Array<Actor> actors = new Array<Actor>();

	private AssetManager assetManager;

	private Skin skin;
	private ShaderProgram fontShader;
	private ShaderLabel newLabel;
	private ShaderLabel continueLabel;
	private ShaderLabel coinText;

	public MainMenuScreen(Skin skin, GameAction gameAction, AssetManager assetManager) {
		this.gameAction = gameAction;
		this.skin = skin;

		fontShader = createShader("data/shaders/font-vs.glsl", "data/shaders/font-fs.glsl");

		this.assetManager = assetManager;
	}

	private void addElementsToStage() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();

		assetManager.load("data/images/loading.pack", TextureAtlas.class);
		assetManager.finishLoading();

		TextureAtlas atlas = assetManager.get("data/images/loading.pack", TextureAtlas.class);

		loadingFrame = new Image(atlas.findRegion("loading-frame"));
		loadingBarHidden = new Image(atlas.findRegion("loading-bar-hidden"));
		loadingBg = new Image(atlas.findRegion("loading-frame-bg"));
		loadingBar = new Image(atlas.findRegion("loading-bar-anim"));

		newLabel = new ShaderLabel(fontShader, Strings.NEW, skin, Constants.UI.DEFAULT_FONT);
		newLabel.setAlignment(Align.center);
		newLabel.setWidth(width);
		newLabel.setX(width / 2 - newLabel.getWidth() / 2);
		newLabel.setY(0.45f * height);
		newLabel.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				startHideSequence(Strings.NEW);
			}
		});
		stage.addActor(newLabel);
		actors.add(newLabel);

		continueLabel = new ShaderLabel(fontShader, Strings.CONTINUE, skin, Constants.UI.DEFAULT_FONT);
		continueLabel.setAlignment(Align.center);
		continueLabel.setWidth(width);
		continueLabel.setX(width / 2 - continueLabel.getWidth() / 2);
		continueLabel.setY(0.33f * height);
		continueLabel.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				startHideSequence(Strings.CONTINUE);
			}
		});
		stage.addActor(continueLabel);
		actors.add(continueLabel);

		ImageButton coinImage = new ImageButton(skin, Constants.UI.COIN);
		GraphicsUtils.setCommonButtonSize(coinImage);
		coinImage.setX(width * 0.96f - coinImage.getWidth());
		coinImage.setY(height * 0.97f - coinImage.getHeight());
		coinImage.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				startHideSequence(Action.COINS);
			}
		});
		stage.addActor(coinImage);
		actors.add(coinImage);

		coinText = new ShaderLabel(fontShader, createCoinDisplay(), skin, Constants.UI.DEFAULT_FONT);
		coinText.setAlignment(Align.right, Align.right);
		float yMidPoint = coinImage.getY() + coinImage.getHeight() / 2;
		float coinTextWidth = 0.4f * width;
		coinText.setBounds(width * 0.93f - coinImage.getWidth() - coinTextWidth, yMidPoint - coinText.getHeight() / 2,
				coinTextWidth, coinText.getHeight());
		stage.addActor(coinText);
		actors.add(coinText);
	}

	private void startHideSequence(final String retVal) {
		GraphicsUtils.hideAnimated(actors, retVal.equals(Action.BACK), new Runnable() {
			@Override
			public void run() {
				returnValue = retVal;
			}
		});
	}

	private String currentUserText() {
		return "Level " + GameLoop.USER.rank.level;
	}

	@Override
	public void render(float delta) {
		coinText.setText(createCoinDisplay());
	}

	private String createCoinDisplay() {
		String coinsText = "";

		DateTime timeRemaining = GameLoop.USER.timeRemainingUntilCoinsAvailable();

		if (timeRemaining != null) {
			coinsText += timeRemaining.getMinuteOfHour() + ":" + timeRemaining.getSecondOfMinute();

		} else {
			coinsText += GameLoop.USER.coins;
		}

		return coinsText;
	}

	private boolean hasAppConfigInformation() {
		return GameLoop.CONFIG.configValues != null;
	}

	@Override
	public void show(Stage stage, float width, float height) {
		this.stage = stage;
		actors.clear();

		addElementsToStage();

		ExternalActionWrapper.recoverUsedCoinsCount();

		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void hide() {

	}

	@Override
	public void resetState() {
		returnValue = null;
	}

	@Override
	public Object getRenderResult() {
		return returnValue;
	}

	@Override
	public boolean hideTitleArea() {
		return false;
	}
}
