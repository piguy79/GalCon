package com.xxx.galcon.screen;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.rotateBy;
import static com.xxx.galcon.Constants.APP_TITLE;
import static com.xxx.galcon.Util.createShader;

import java.util.HashMap;
import java.util.Map;

import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.InGameInputProcessor;
import com.xxx.galcon.PartialScreenFeedback;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.Strings;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.SocialAction;
import com.xxx.galcon.screen.signin.ChooseHandleScreen;
import com.xxx.galcon.screen.signin.MainMenuScreen;
import com.xxx.galcon.screen.signin.SignInScreen;
import com.xxx.galcon.screen.widget.ShaderLabel;

public class MenuScreenContainer implements ScreenFeedback {

	private UISkin skin;
	private ShaderProgram fontShader;

	private SocialAction socialAction;
	private GameAction gameAction;

	private Stage stage;
	private ShaderLabel titleText;

	private SignInScreen signInScreen;
	private MainMenuScreen mainMenuScreen;
	private ChooseHandleScreen chooseHandleScreen;
	private LevelSelectionScreen levelSelectionScreen;
	private PartialScreenFeedback currentScreen;

	private GameListScreen currentGameScreen;
	private NoMoreCoinsDialog noMoreCoinsScreen;

	private TextureAtlas menusAtlas;

	Map<Class<?>, ScreenResultHandler> screenResultHandlers = new HashMap<Class<?>, ScreenResultHandler>();

	public MenuScreenContainer(UISkin skin, SocialAction socialAction, GameAction gameAction,
			AssetManager assetManager, TweenManager tweenManager) {
		this.skin = skin;
		this.stage = new Stage();

		fontShader = createShader("data/shaders/font-vs.glsl", "data/shaders/font-fs.glsl");
		menusAtlas = assetManager.get("data/images/menus.atlas", TextureAtlas.class);

		this.socialAction = socialAction;
		this.gameAction = gameAction;

		signInScreen = new SignInScreen(skin, socialAction, gameAction);
		mainMenuScreen = new MainMenuScreen(skin, gameAction, assetManager);
		chooseHandleScreen = new ChooseHandleScreen(skin, gameAction, assetManager);
		levelSelectionScreen = new LevelSelectionScreen(skin, assetManager);
		currentGameScreen = new GameListScreen(assetManager, skin);
		noMoreCoinsScreen = new NoMoreCoinsDialog(skin, assetManager);

		screenResultHandlers.put(SignInScreen.class, new SignInScreenResultHandler());
		screenResultHandlers.put(ChooseHandleScreen.class, new ChooseHandleScreenResultHandler());
		screenResultHandlers.put(MainMenuScreen.class, new MainMenuScreenResultHandler());
		screenResultHandlers.put(LevelSelectionScreen.class, new LevelSelectionScreenResultHandler());
		screenResultHandlers.put(GameListScreen.class, new GameListScreenResultHandler());
		screenResultHandlers.put(NoMoreCoinsDialog.class, new NoMoreCoinsDialogResultHandler());

		currentScreen = signInScreen;
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		stage.act(delta);
		stage.draw();

		currentScreen.render(delta);
	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void show() {
		Color bg = skin.get(Constants.UI.DEFAULT_BG_COLOR, Color.class);
		Gdx.gl.glClearColor(bg.r, bg.g, bg.b, bg.a);

		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();

		stage.clear();
		stage.setViewport(width, height, true);

		Image bgImage = new Image(menusAtlas.findRegion("bg"));
		bgImage.setX(-2 * width);
		bgImage.setWidth(width * 4);
		bgImage.setY(-0.5f * height);
		bgImage.setHeight(height * 2f);
		bgImage.setColor(0.0f, 0.7f, 0.7f, 0.6f);
		bgImage.setOrigin((float) width * 2.0f, (float) height * 1.0f);
		bgImage.addAction(forever(rotateBy(360, 150)));
		stage.addActor(bgImage);

		titleText = new ShaderLabel(fontShader, APP_TITLE, skin, Constants.UI.LARGE_FONT);
		titleText.setX(width / 2 - titleText.getWidth() / 2);
		titleText.setY(height * 0.7f);
		stage.addActor(titleText);

		Gdx.input.setInputProcessor(stage);

		currentScreen.show(stage, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		if (currentScreen.hideTitleArea()) {
			titleText.remove();
		} else {
			stage.addActor(titleText);
		}
	}

	@Override
	public void hide() {
		currentScreen.hide();
		Gdx.input.setInputProcessor(new InGameInputProcessor());
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
		Object value = currentScreen.getRenderResult();
		if (value == null) {
			return null;
		}

		for (Map.Entry<Class<?>, ScreenResultHandler> entry : screenResultHandlers.entrySet()) {
			if (currentScreen.getClass().equals(entry.getKey())) {
				PartialScreenFeedback nextScreen = entry.getValue().processValue(value);
				if (nextScreen == null) {
					return value;
				}

				if (nextScreen instanceof PartialScreenFeedback) {
					currentScreen.hide();
					currentScreen = (PartialScreenFeedback) nextScreen;
					currentScreen.resetState();
					currentScreen.show(stage, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

					if (currentScreen.hideTitleArea()) {
						titleText.remove();
					} else {
						stage.addActor(titleText);
					}
					return null;
				} else {
					return nextScreen;
				}
			}
		}

		return value;
	}

	@Override
	public void resetState() {
		currentScreen.resetState();
		signInScreen.resetState();
	}

	private interface ScreenResultHandler {
		public PartialScreenFeedback processValue(Object value);
	}

	public class SignInScreenResultHandler implements ScreenResultHandler {
		@Override
		public PartialScreenFeedback processValue(Object value) {
			return chooseHandleScreen;
		}
	}

	public class ChooseHandleScreenResultHandler implements ScreenResultHandler {
		@Override
		public PartialScreenFeedback processValue(Object value) {
			if (((String) value).equals("signIn")) {
				Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
				prefs.putString(Constants.Auth.LAST_SESSION_ID, "");
				prefs.flush();

				return signInScreen;
			} else if (((String) value).equals("hasHandle")) {
				return mainMenuScreen;
			}
			return null;
		}
	}

	public class MainMenuScreenResultHandler implements ScreenResultHandler {
		@Override
		public PartialScreenFeedback processValue(Object value) {
			String nextScreen = (String) value;

			if (nextScreen.equals(Strings.NEW)) {
				if (GameLoop.USER.coins == 0) {
					return noMoreCoinsScreen;
				}
				return levelSelectionScreen;
			} else if (nextScreen.equals(Strings.CONTINUE)) {
				return currentGameScreen;
			}

			return null;
		}
	}

	public class LevelSelectionScreenResultHandler implements ScreenResultHandler {
		@Override
		public PartialScreenFeedback processValue(Object value) {
			String action = (String) value;
			if (action.equals(Action.BACK)) {
				return mainMenuScreen;
			}

			return null;
		}
	}

	public class GameListScreenResultHandler implements ScreenResultHandler {

		@Override
		public PartialScreenFeedback processValue(Object value) {
			if (value instanceof String) {
				String action = (String) value;
				if (action.equals(Action.BACK)) {
					return mainMenuScreen;
				}
			}

			return null;
		}
	}

	public class NoMoreCoinsDialogResultHandler implements ScreenResultHandler {

		@Override
		public PartialScreenFeedback processValue(Object value) {
			String action = (String) value;
			if (action.endsWith(Action.DIALOG_CANCEL)) {
				return mainMenuScreen;
			}

			return null;
		}
	}
}
