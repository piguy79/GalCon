package com.xxx.galcon.screen;

import static com.xxx.galcon.Constants.GALCON_PREFS;
import static com.xxx.galcon.Constants.SOCIAL_AUTH_PROVIDER;
import static com.xxx.galcon.Util.createShader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.xxx.galcon.Constants;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.SocialAction;
import com.xxx.galcon.screen.widget.ShaderLabel;
import com.xxx.galcon.screen.widget.WaitImageButton;

public class ScreenContainer implements ScreenFeedback {

	private UISkin skin;
	private Table mainLayoutTable;
	private ShaderProgram shader;

	private SocialAction socialAction;

	private Stage stage;
	private ShaderLabel signInLabel;
	private WaitImageButton waitImage;

	private SignInScreen signInScreen;
	private MainMenuScreen mainMenuScreen;
	private ScreenFeedback currentScreen;

	public ScreenContainer(UISkin skin, SocialAction socialAction, GameAction gameAction, AssetManager assetManager) {
		this.skin = skin;
		this.stage = new Stage();

		shader = createShader("data/shaders/font-vs.glsl", "data/shaders/font-fs.glsl");

		this.socialAction = socialAction;

		Preferences prefs = Gdx.app.getPreferences(GALCON_PREFS);
		String socialAuthProvider = prefs.getString(SOCIAL_AUTH_PROVIDER);

		signInScreen = new SignInScreen(skin, socialAction);
		mainMenuScreen = new MainMenuScreen(skin, gameAction, assetManager);

		currentScreen = signInScreen;
	}

	@Override
	public void render(float delta) {
		currentScreen.render(delta);
	}

	@Override
	public void resize(int width, int height) {
		currentScreen.resize(width, height);
	}

	@Override
	public void show() {
		Color bg = skin.get(Constants.UI.DEFAULT_BG_COLOR, Color.class);
		Gdx.gl.glClearColor(bg.r, bg.g, bg.b, bg.a);

		currentScreen.show();
	}

	@Override
	public void hide() {
		currentScreen.hide();
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
			return value;
		}

		if (currentScreen instanceof SignInScreen) {
			currentScreen.hide();
			currentScreen = mainMenuScreen;
			currentScreen.show();
			currentScreen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			return null;
		} else if (currentScreen instanceof MainMenuScreen) {

		}

		return value;
	}

	@Override
	public void resetState() {
		currentScreen.resetState();
	}
}
