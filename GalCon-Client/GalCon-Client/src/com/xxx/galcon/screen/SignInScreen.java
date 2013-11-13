package com.xxx.galcon.screen;

import static com.xxx.galcon.Constants.APP_TITLE;
import static com.xxx.galcon.Constants.GALCON_PREFS;
import static com.xxx.galcon.Constants.SOCIAL_AUTH_PROVIDER;
import static com.xxx.galcon.Constants.SOCIAL_AUTH_PROVIDER_GOOGLE;
import static com.xxx.galcon.Util.createShader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.xxx.galcon.Constants;
import com.xxx.galcon.InGameInputProcessor;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.Strings;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.http.AuthenticationListener;
import com.xxx.galcon.http.SocialAction;
import com.xxx.galcon.screen.widget.ShaderLabel;
import com.xxx.galcon.screen.widget.WaitImageButton;

public class SignInScreen implements ScreenFeedback, AuthenticationListener {

	private UISkin skin;
	private Table mainLayoutTable;
	private ShaderProgram fontShader;

	private SocialAction socialAction;

	private Stage stage;
	private ShaderLabel signInLabel;
	private WaitImageButton waitImage;

	public SignInScreen(UISkin skin, SocialAction socialAction) {
		this.skin = skin;
		this.stage = new Stage();

		fontShader = createShader("data/shaders/font-vs.glsl", "data/shaders/font-fs.glsl");

		this.socialAction = socialAction;
		socialAction.registerSignInListener(this);

		Preferences prefs = Gdx.app.getPreferences(GALCON_PREFS);
		String socialAuthProvider = prefs.getString(SOCIAL_AUTH_PROVIDER);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		stage.clear();
		mainLayoutTable = new Table();

		stage.setViewport(width, height, true);
		stage.addActor(mainLayoutTable);

		mainLayoutTable.setFillParent(true);

		ShaderLabel titleText = new ShaderLabel(fontShader, APP_TITLE, skin, Constants.UI.LARGE_FONT);
		mainLayoutTable.add(titleText).height(.3f * (float) height);

		mainLayoutTable.row();
		signInLabel = new ShaderLabel(fontShader, "", skin, Constants.UI.DEFAULT_FONT);
		mainLayoutTable.add(signInLabel).expandX().height(.2f * (float) height);

		mainLayoutTable.row();

		Table buttonTable = new Table();
		mainLayoutTable.add(buttonTable).expand();

		Button googlePlusButton = new Button(skin, Constants.UI.GOOGLE_PLUS_SIGN_IN_BUTTON);
		buttonTable.add(googlePlusButton).width(0.4f * (float) width).height(0.1f * (float) height);

		googlePlusButton.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				waitImage.start();
				socialAction.signIn(SOCIAL_AUTH_PROVIDER_GOOGLE);
			}
		});

		waitImage = new WaitImageButton(skin);
		float buttonWidth = .25f * (float) width;
		waitImage.setWidth(buttonWidth);
		waitImage.setHeight(buttonWidth);
		waitImage.setX(width / 2 - buttonWidth / 2);
		waitImage.setY(height / 2 - buttonWidth / 2);
		stage.addActor(waitImage);

		Gdx.input.setInputProcessor(stage);

	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
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
		return "";
	}

	@Override
	public void resetState() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSignInFailed() {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				waitImage.end();
				signInLabel.setText(Strings.AUTH_FAIL);
			}
		});
	}

	@Override
	public void onSignInSucceeded(String token) {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				waitImage.end();
				signInLabel.setText(Strings.AUTH_SUCCESS);
			}
		});
	}

	@Override
	public void onSignOut() {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				waitImage.end();
				signInLabel.setText(Strings.AUTH_FAIL);
			}
		});
	}

}
