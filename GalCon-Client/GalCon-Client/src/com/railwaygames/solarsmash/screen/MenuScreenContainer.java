package com.railwaygames.solarsmash.screen;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.rotateBy;
import static com.railwaygames.solarsmash.Constants.APP_TITLE;

import java.util.HashMap;
import java.util.Map;

import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.PartialScreenFeedback;
import com.railwaygames.solarsmash.ScreenFeedback;
import com.railwaygames.solarsmash.Strings;
import com.railwaygames.solarsmash.http.GameAction;
import com.railwaygames.solarsmash.http.InAppBillingAction;
import com.railwaygames.solarsmash.http.SocialAction;
import com.railwaygames.solarsmash.screen.signin.ChooseHandleScreen;
import com.railwaygames.solarsmash.screen.signin.LoadingScreen;
import com.railwaygames.solarsmash.screen.signin.MainMenuScreen;
import com.railwaygames.solarsmash.screen.signin.SignInScreen;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;
import com.railwaygames.solarsmash.screen.widget.ShaderTextField.OnscreenKeyboard;

public class MenuScreenContainer implements ScreenFeedback {

	private Stage stage;
	private ShaderLabel titleText;

	private SignInScreen signInScreen;
	private MainMenuScreen mainMenuScreen;
	private ChooseHandleScreen chooseHandleScreen;
	private LevelSelectionScreen levelSelectionScreen;
	private LoadingScreen loadingScreen;
	private PartialScreenFeedback currentScreen;

	private GameListScreen currentGameScreen;
	private GameQueueScreen gameQueueScreen;
	private NoMoreCoinsDialog noMoreCoinsScreen;

	private Resources resources;

	Map<Class<?>, ScreenResultHandler> screenResultHandlers = new HashMap<Class<?>, ScreenResultHandler>();

	public MenuScreenContainer(Resources resources, SocialAction socialAction, GameAction gameAction,
			InAppBillingAction inAppBillingAction, TweenManager tweenManager, OnscreenKeyboard keyboard) {
		this.stage = new Stage();
		this.resources = resources;

		signInScreen = new SignInScreen(resources, socialAction, gameAction);
		mainMenuScreen = new MainMenuScreen(resources, gameAction, socialAction);
		chooseHandleScreen = new ChooseHandleScreen(resources, gameAction, keyboard);
		levelSelectionScreen = new LevelSelectionScreen(resources);
		currentGameScreen = new GameListScreen(resources);
		gameQueueScreen = new GameQueueScreen(resources);
		noMoreCoinsScreen = new NoMoreCoinsDialog(resources);
		loadingScreen = new LoadingScreen(resources, gameAction, inAppBillingAction);

		screenResultHandlers.put(SignInScreen.class, new SignInScreenResultHandler());
		screenResultHandlers.put(ChooseHandleScreen.class, new ChooseHandleScreenResultHandler());
		screenResultHandlers.put(MainMenuScreen.class, new MainMenuScreenResultHandler());
		screenResultHandlers.put(LevelSelectionScreen.class, new LevelSelectionScreenResultHandler());
		screenResultHandlers.put(GameListScreen.class, new GameListScreenResultHandler());
		screenResultHandlers.put(GameQueueScreen.class, new GameListScreenResultHandler());
		screenResultHandlers.put(NoMoreCoinsDialog.class, new NoMoreCoinsDialogResultHandler());
		screenResultHandlers.put(LoadingScreen.class, new LoadingScreenResultHandler());

		currentScreen = signInScreen;
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.act(delta);
		stage.draw();

		if (titleText != null) {
			if (currentScreen.hideTitleArea()) {
				titleText.remove();
			} else {
				stage.addActor(titleText);
			}
		}

		currentScreen.render(delta);
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
	}

	@Override
	public void show() {
		Color bg = resources.skin.get(Constants.UI.DEFAULT_BG_COLOR, Color.class);
		Gdx.gl.glClearColor(bg.r, bg.g, bg.b, bg.a);

		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();

		stage.clear();
		stage.setViewport(new ExtendViewport(width, height));

		Image bgImage = new Image(resources.menuAtlas.findRegion("bg"));
		bgImage.setX(-2 * width);
		bgImage.setWidth(width * 4);
		bgImage.setY(-0.5f * height);
		bgImage.setHeight(height * 2f);
		bgImage.setColor(0.0f, 0.7f, 0.7f, 0.6f);
		bgImage.setOrigin((float) width * 2.0f, (float) height * 1.0f);
		bgImage.addAction(forever(rotateBy(360, 150)));
		stage.addActor(bgImage);

		titleText = new ShaderLabel(resources.fontShader, APP_TITLE, resources.skin, Constants.UI.LARGE_FONT);
		titleText.setX(width / 2 - titleText.getWidth() / 2);
		titleText.setY(height * 0.7f);
		stage.addActor(titleText);

		Gdx.input.setInputProcessor(stage);

		if (currentScreen.hideTitleArea()) {
			titleText.remove();
		} else {
			stage.addActor(titleText);
		}
		currentScreen.show(stage, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {

	}

	@Override
	public void refresh() {
		if (currentScreen.canRefresh()) {
			showNextScreen(currentScreen);
		}
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
					showNextScreen(nextScreen);
					return null;
				} else {
					return nextScreen;
				}
			}
		}

		return value;
	}

	private void showNextScreen(PartialScreenFeedback nextScreen) {
		currentScreen.hide();
		currentScreen = (PartialScreenFeedback) nextScreen;
		currentScreen.resetState();
		if (currentScreen.hideTitleArea()) {
			titleText.remove();
		} else {
			stage.addActor(titleText);
		}
		currentScreen.show(stage, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
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
				return loadingScreen;
			}
			return null;
		}
	}

	public class LoadingScreenResultHandler implements ScreenResultHandler {
		@Override
		public PartialScreenFeedback processValue(Object value) {
			if (((String) value).equals(Action.DONE)) {
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
			} else if (nextScreen.equals(Action.COINS)) {
				return noMoreCoinsScreen;
			} else if (nextScreen.equals(Strings.INVITES)) {
				if (GameLoop.USER.coins == 0) {
					return noMoreCoinsScreen;
				}
				return gameQueueScreen;
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
			} else {
				// clear back to main menu, then proceed to the gameboard
				currentScreen.hide();
				currentScreen = mainMenuScreen;
				currentScreen.resetState();
				if (currentScreen.hideTitleArea()) {
					titleText.remove();
				} else {
					stage.addActor(titleText);
				}
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
			if (action.equals(Action.BACK)) {
				return mainMenuScreen;
			}

			return null;
		}
	}
}
