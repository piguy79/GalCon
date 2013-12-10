package com.xxx.galcon.screen.signin;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.rotateBy;
import static com.xxx.galcon.Constants.APP_TITLE;
import static com.xxx.galcon.Util.createShader;

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
import com.xxx.galcon.InGameInputProcessor;
import com.xxx.galcon.PartialScreenFeedback;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.SocialAction;
import com.xxx.galcon.screen.widget.ShaderLabel;

public class SignInProcessScreens implements ScreenFeedback {

	private UISkin skin;
	private ShaderProgram fontShader;

	private SocialAction socialAction;

	private Stage stage;

	private SignInScreen signInScreen;
	private MainMenuScreen mainMenuScreen;
	private ChooseHandleScreen chooseHandleScreen;
	private PartialScreenFeedback currentScreen;

	private TextureAtlas menusAtlas;

	public SignInProcessScreens(UISkin skin, SocialAction socialAction, GameAction gameAction, AssetManager assetManager) {
		this.skin = skin;
		this.stage = new Stage();

		fontShader = createShader("data/shaders/font-vs.glsl", "data/shaders/font-fs.glsl");
		menusAtlas = assetManager.get("data/images/menus.atlas", TextureAtlas.class);

		this.socialAction = socialAction;

		signInScreen = new SignInScreen(skin, socialAction, gameAction);
		mainMenuScreen = new MainMenuScreen(skin, gameAction, assetManager);
		chooseHandleScreen = new ChooseHandleScreen(skin, gameAction, assetManager);

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

		ShaderLabel titleText = new ShaderLabel(fontShader, APP_TITLE, skin, Constants.UI.LARGE_FONT);
		titleText.setX(width / 2 - titleText.getWidth() / 2);
		titleText.setY(height * 0.7f);
		stage.addActor(titleText);

		Gdx.input.setInputProcessor(stage);

		currentScreen.show(stage, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
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
		
		System.out.println(value);

		if (currentScreen instanceof SignInScreen) {
			currentScreen.hide();
			currentScreen = chooseHandleScreen;
			currentScreen.resetState();
			currentScreen.show(stage, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			return null;
		} else if (currentScreen instanceof ChooseHandleScreen) {
			if (((String) value).equals("signIn")) {
				currentScreen.hide();
				currentScreen = signInScreen;
				currentScreen.resetState();

				Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
				prefs.putString(Constants.Auth.LAST_SESSION_ID, "");
				prefs.flush();

				currentScreen.show(stage, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				return null;
			} else if (((String) value).equals("hasHandle")) {
				currentScreen.hide();
				currentScreen = mainMenuScreen;
				currentScreen.resetState();

				currentScreen.show(stage, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				return null;
			}
		} else if (currentScreen instanceof MainMenuScreen) {

		}

		return value;
	}

	@Override
	public void resetState() {
		currentScreen.resetState();
		signInScreen.resetState();
	}
}
