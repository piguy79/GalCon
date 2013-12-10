package com.xxx.galcon.screen.signin;

import static com.xxx.galcon.Util.createShader;

import org.joda.time.DateTime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.xxx.galcon.Constants;
import com.xxx.galcon.ExternalActionWrapper;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.PartialScreenFeedback;
import com.xxx.galcon.Strings;
import com.xxx.galcon.http.GameAction;
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

	private AssetManager assetManager;

	private Skin skin;
	private ShaderProgram fontShader;
	private ShaderLabel newLabel;
	private ShaderLabel continueLabel;

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
				returnValue = Strings.NEW;
			}
		});
		stage.addActor(newLabel);

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
				returnValue = Strings.CONTINUE;
			}
		});
		stage.addActor(continueLabel);
	}

	private String currentUserText() {
		return "Level " + GameLoop.USER.rank.level;
	}

	@Override
	public void render(float delta) {

	}

	private void createCoinDisplay(int width, int height) {
		String coinsText = "";

		DateTime timeRemaining = GameLoop.USER.timeRemainingUntilCoinsAvailable();

		if (timeRemaining != null) {
			coinsText += timeRemaining.getMinuteOfHour() + ":" + timeRemaining.getSecondOfMinute();

		} else {
			coinsText += GameLoop.USER.coins;
		}

		BitmapFont extraLargeFont = Fonts.getInstance(assetManager).mediumFont();
		double percentageOfWidth = width * 0.04;
		int x = (int) percentageOfWidth;
		extraLargeFont.draw(spriteBatch, coinsText, x, (int) (height * .97f));
	}

	private boolean hasAppConfigInformation() {
		return GameLoop.CONFIG.configValues != null;
	}

	@Override
	public void show(Stage stage, float width, float height) {
		this.stage = stage;

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
}
