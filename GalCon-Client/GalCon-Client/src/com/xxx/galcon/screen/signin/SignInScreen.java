package com.xxx.galcon.screen.signin;

import static com.badlogic.gdx.math.Interpolation.pow3;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import static com.xxx.galcon.Constants.GALCON_PREFS;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.xxx.galcon.Constants;
import com.xxx.galcon.PartialScreenFeedback;
import com.xxx.galcon.Strings;
import com.xxx.galcon.http.AuthenticationListener;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.SocialAction;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.Session;
import com.xxx.galcon.screen.Resources;
import com.xxx.galcon.screen.widget.ShaderLabel;
import com.xxx.galcon.screen.widget.WaitImageButton;

public class SignInScreen implements PartialScreenFeedback, AuthenticationListener, UIConnectionResultCallback<Session> {

	private SocialAction socialAction;
	private GameAction gameAction;

	private Stage stage;
	private ShaderLabel signInLabel;
	private WaitImageButton waitImage;
	private Button googlePlusButton;
	private Button facebookButton;

	private String returnValue = null;

	private Resources resources;

	public SignInScreen(Resources resources, SocialAction socialAction, GameAction gameAction) {
		this.socialAction = socialAction;
		this.gameAction = gameAction;
		this.resources = resources;
		socialAction.registerSignInListener(this);
	}

	@Override
	public void render(float delta) {

	}

	@Override
	public void show(Stage stage, float width, float height) {
		this.stage = stage;

		signInLabel = new ShaderLabel(resources.fontShader, "", resources.skin, Constants.UI.DEFAULT_FONT);
		signInLabel.setAlignment(Align.center);
		signInLabel.setWidth(width);
		signInLabel.setX(width / 2 - signInLabel.getWidth() / 2);
		signInLabel.setY(0.6f * height);
		stage.addActor(signInLabel);

		waitImage = new WaitImageButton(resources.skin);
		float buttonWidth = .25f * (float) width;
		waitImage.setWidth(buttonWidth);
		waitImage.setHeight(buttonWidth);
		waitImage.setX(width / 2 - buttonWidth / 2);
		waitImage.setY(height / 2 - buttonWidth / 2);
		stage.addActor(waitImage);

		Preferences prefs = Gdx.app.getPreferences(GALCON_PREFS);
		String socialAuthProvider = prefs.getString(Constants.Auth.SOCIAL_AUTH_PROVIDER);

		if (socialAuthProvider != null && !socialAuthProvider.isEmpty()) {
			String id = prefs.getString(socialAuthProvider + Constants.Auth.SOCIAL_AUTH_PROVIDER);
			String lastSessionId = prefs.getString(Constants.Auth.LAST_SESSION_ID, "");
			if (lastSessionId.isEmpty() || id.isEmpty()) {
				waitImage.start();
				socialAction.signIn(socialAuthProvider);
			} else {
				gameAction.setSession(lastSessionId);
				returnValue = "done";
			}
		} else {
			addAuthenticationMethodsToStage(width, height);
		}
	}

	private void addAuthenticationMethodsToStage(float width, float height) {
		addGooglePlusButton(width, height);
		addFacebookButton(width, height);
	}

	private void addGooglePlusButton(float width, float height) {
		googlePlusButton = new ImageButton(resources.skin, Constants.UI.GOOGLE_PLUS_SIGN_IN_NORMAL);
		googlePlusButton.setWidth(0.7f * width);
		googlePlusButton.setX(width / 2 - googlePlusButton.getWidth() / 2);
		googlePlusButton.setY(0.3f * height);
		stage.addActor(googlePlusButton);

		googlePlusButton.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				signInLabel.setText("");
				waitImage.start();
				socialAction.signIn(Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE);
			}
		});

		float currentX = googlePlusButton.getX();
		googlePlusButton.setX(width * 2);
		googlePlusButton.addAction(sequence(delay(0.1f),
				moveTo(currentX, googlePlusButton.getY(), 0.9f, Interpolation.pow3)));
	}

	private void addFacebookButton(float width, float height) {
		facebookButton = new ImageButton(resources.skin, Constants.UI.FACEBOOK_SIGN_IN_BUTTON);
		facebookButton.setWidth(0.7f * width);
		facebookButton.setX(width / 2 - googlePlusButton.getWidth() / 2);
		facebookButton.setY(0.2f * height);
		stage.addActor(facebookButton);

		facebookButton.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				signInLabel.setText("");
				waitImage.start();
				socialAction.signIn(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK);
			}
		});

		float currentX = facebookButton.getX();
		facebookButton.setX(width * 2);
		facebookButton.addAction(sequence(delay(0.1f),
				moveTo(currentX, facebookButton.getY(), 0.9f, Interpolation.pow3)));
	}

	@Override
	public void hide() {
		signInLabel.setText("");
	}

	private void startHideSequence() {
		if (googlePlusButton != null) {
			googlePlusButton.addAction(sequence(delay(0.25f),
					moveTo(-Gdx.graphics.getWidth(), googlePlusButton.getY(), 0.9f, pow3)));
		}

		if (facebookButton != null) {
			facebookButton.addAction(sequence(delay(0.25f),
					moveTo(-Gdx.graphics.getWidth(), facebookButton.getY(), 0.9f, pow3)));
		}

		signInLabel.addAction(sequence(delay(0.5f), moveTo(-Gdx.graphics.getWidth(), signInLabel.getY(), 0.9f, pow3),
				run(new Runnable() {
					@Override
					public void run() {
						returnValue = "done";
					}
				})));
	}

	@Override
	public Object getRenderResult() {
		return returnValue;
	}

	@Override
	public void resetState() {
		returnValue = null;
	}

	@Override
	public void onSignInFailed(final String failureMessage) {
		if (googlePlusButton == null) {
			addAuthenticationMethodsToStage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		}

		waitImage.stop();
		signInLabel.setText(failureMessage);
	}

	@Override
	public void onSignInSucceeded(final String authProvider, String token) {
		Preferences prefs = Gdx.app.getPreferences(GALCON_PREFS);
		prefs.putString(Constants.Auth.SOCIAL_AUTH_PROVIDER, authProvider);
		prefs.flush();

		gameAction.exchangeTokenForSession(this, authProvider, token);
	}

	@Override
	public void onSignOut() {
		waitImage.stop();
		signInLabel.setText(Strings.AUTH_FAIL);
	}

	@Override
	public void onConnectionResult(Session result) {
		if (!result.errorMessage.isEmpty()) {
			onConnectionError(result.errorMessage);
		} else {
			Preferences prefs = Gdx.app.getPreferences(GALCON_PREFS);
			prefs.putString(Constants.Auth.LAST_SESSION_ID, result.session);
			prefs.flush();

			gameAction.setSession(result.session);
			waitImage.stop();

			signInLabel.setText(Strings.AUTH_SUCCESS);
			startHideSequence();
		}
	}

	@Override
	public void onConnectionError(String msg) {
		waitImage.stop();

		signInLabel.setText(Strings.AUTH_FAIL);
		if (googlePlusButton == null) {
			addAuthenticationMethodsToStage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		}
	}

	@Override
	public boolean hideTitleArea() {
		return false;
	}
}