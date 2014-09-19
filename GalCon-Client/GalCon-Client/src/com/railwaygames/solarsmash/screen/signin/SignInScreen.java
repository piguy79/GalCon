package com.railwaygames.solarsmash.screen.signin;

import static com.badlogic.gdx.math.Interpolation.pow3;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sizeTo;
import static com.railwaygames.solarsmash.Constants.GALCON_PREFS;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.railwaygames.solarsmash.Actions;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.PartialScreenFeedback;
import com.railwaygames.solarsmash.Strings;
import com.railwaygames.solarsmash.http.AuthenticationListener;
import com.railwaygames.solarsmash.http.GameAction;
import com.railwaygames.solarsmash.http.SocialAction;
import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.model.Session;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;
import com.railwaygames.solarsmash.screen.widget.WaitImageButton;

public class SignInScreen implements PartialScreenFeedback, AuthenticationListener, UIConnectionResultCallback<Session> {

	private SocialAction socialAction;
	private GameAction gameAction;

	private Stage stage;
	private ShaderLabel signInLabel;
	private WaitImageButton waitImage;
	private Button googlePlusButton;
	private Button facebookButton;
	private Button localProviderButton;
	private ShaderLabel localProviderText;
	private Button localProviderOKButton;
	private ShaderLabel localProviderOKText;

	private String returnValue = null;

	private Resources resources;

	public SignInScreen(Resources resources, SocialAction socialAction, GameAction gameAction) {
		this.socialAction = socialAction;
		this.gameAction = gameAction;
		this.resources = resources;
	}

	@Override
	public void render(float delta) {

	}

	private float width;
	private float height;

	@Override
	public void resize(int width, int height) {
		this.width = width;
		this.height = height;

		signInLabel.setWidth(width);
		signInLabel.setX(width / 2 - signInLabel.getWidth() / 2);
		signInLabel.setY(0.6f * height);

		float buttonWidth = .25f * (float) width;
		waitImage.setWidth(buttonWidth);
		waitImage.setHeight(buttonWidth);
		waitImage.setX(width / 2 - buttonWidth / 2);
		waitImage.setY(height * 0.6f - buttonWidth / 2);
	}

	@Override
	public void show(Stage stage) {
		this.stage = stage;

		signInLabel = new ShaderLabel(resources.fontShader, "", resources.skin, Constants.UI.DEFAULT_FONT, Color.WHITE);
		signInLabel.setAlignment(Align.center);
		stage.addActor(signInLabel);

		waitImage = new WaitImageButton(resources.skin);
		stage.addActor(waitImage);

		Preferences prefs = Gdx.app.getPreferences(GALCON_PREFS);
		String socialAuthProvider = prefs.getString(Constants.Auth.SOCIAL_AUTH_PROVIDER);

		if (socialAuthProvider != null && !socialAuthProvider.isEmpty()) {
			String id = prefs.getString(socialAuthProvider + Constants.Auth.SOCIAL_AUTH_PROVIDER);
			String lastSessionId = prefs.getString(Constants.Auth.LAST_SESSION_ID, "");
			if (lastSessionId.isEmpty() || id.isEmpty()) {
				waitImage.start();
				socialAction.signIn(this, socialAuthProvider);
			} else {
				gameAction.setSession(lastSessionId);
				returnValue = "done";
			}
		} else {
			addAuthenticationMethodsToStage();
		}
	}

	private void addAuthenticationMethodsToStage() {
		width = Gdx.graphics.getWidth();
		height = Gdx.graphics.getHeight();

		if (socialAction.enableGoogle()) {
			addGooglePlusButton(width, height);
		}
		addFacebookButton(width, height);
		addLocalProviderButton(width, height);
	}

	private InputListener createLocalProviderWarningListener() {
		return new InputListener() {

			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				signInLabel.setText("");

				if (googlePlusButton != null) {
					googlePlusButton.addAction(sequence(fadeOut(0.3f, pow3), run(new Runnable() {
						@Override
						public void run() {
							googlePlusButton.remove();
							googlePlusButton = null;
						}
					})));
				}

				if (facebookButton != null) {
					facebookButton.addAction(sequence(fadeOut(0.3f, pow3), run(new Runnable() {
						@Override
						public void run() {
							facebookButton.remove();
							facebookButton = null;
						}
					})));
				}
				localProviderButton.addAction(Actions.highlightButtonClick());
				localProviderText.addAction(Actions.highlightButtonClick());
				localProviderText.addAction(sequence(delay(0.2f), fadeOut(0.3f, pow3), run(new Runnable() {
					@Override
					public void run() {
						localProviderText.remove();

						float bWidth = width * 0.8f;
						float bHeight = bWidth * 1.0f;

						localProviderButton.addAction(parallel(
								moveTo(width * 0.5f - bWidth * 0.5f, localProviderButton.getY(), 0.6f, pow3),
								sizeTo(bWidth, bHeight, 0.6f, pow3)));

						localProviderText.setWrap(true);
						localProviderText.setBounds(width * 0.5f - bWidth * 0.4f, localProviderButton.getY(),
								bWidth * 0.8f, bHeight);
						localProviderText.setText("To keep this account permanently and play across multiple devices"
								+ ", you may associate it with G+ or Facebook at any later time.");
						localProviderText.addAction(sequence(delay(0.6f), fadeIn(0.3f, pow3)));
						stage.addActor(localProviderText);

						addLocalOKProviderButton(width, height);
						localProviderOKButton.addAction(sequence(delay(0.5f), color(Color.WHITE, 0.3f, pow3)));
						localProviderOKText.addAction(sequence(delay(0.5f), color(Color.WHITE, 0.3f, pow3)));
					}
				})));
			}
		};
	}

	private InputListener createButtonListener(final String authProvider) {
		return new InputListener() {

			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				signInLabel.setText("");
				waitImage.start();
				socialAction.signIn(SignInScreen.this, authProvider);
			}
		};
	}

	private void addGooglePlusButton(float width, float height) {
		googlePlusButton = new Button(resources.skin, Constants.UI.GOOGLE_PLUS_SIGN_IN_NORMAL);
		googlePlusButton.setWidth(0.3f * width);
		googlePlusButton.setX(width * 0.75f - googlePlusButton.getWidth() / 2);
		googlePlusButton.setY(0.38f * height);
		googlePlusButton.setHeight(0.07f * height);
		stage.addActor(googlePlusButton);

		googlePlusButton.addListener(createButtonListener(Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE));

		float currentX = googlePlusButton.getX();
		googlePlusButton.setX(width * 2);
		googlePlusButton.addAction(sequence(delay(0.1f),
				moveTo(currentX, googlePlusButton.getY(), 0.7f, Interpolation.pow3)));
	}

	private void addFacebookButton(float width, float height) {
		facebookButton = new Button(resources.skin, Constants.UI.FACEBOOK_SIGN_IN_BUTTON);
		facebookButton.setWidth(0.3f * width);
		facebookButton.setX(width * 0.25f - facebookButton.getWidth() / 2);
		facebookButton.setY(0.38f * height);
		facebookButton.setHeight(0.07f * height);
		stage.addActor(facebookButton);

		facebookButton.addListener(createButtonListener(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK));

		float currentX = facebookButton.getX();
		facebookButton.setX(width * 2);
		facebookButton.addAction(sequence(delay(0.1f),
				moveTo(currentX, facebookButton.getY(), 0.7f, Interpolation.pow3)));
	}

	private void addLocalOKProviderButton(float width, float height) {
		localProviderOKButton = new Button(resources.skin, Constants.UI.CLEAR_BUTTON);
		float bWidth = width * 0.3f;
		float bHeight = bWidth * 0.4f;
		localProviderOKButton.setBounds(width * 0.5f - bWidth * 0.5f, height * 0.26f, bWidth, bHeight);

		localProviderOKText = new ShaderLabel(resources.fontShader, "OK", resources.skin, Constants.UI.SMALL_FONT,
				Color.WHITE);
		localProviderOKText.setAlignment(Align.center);
		localProviderOKText.setY(localProviderOKButton.getY() + localProviderOKButton.getHeight() / 2
				- localProviderOKText.getHeight() * 0.5f);
		localProviderOKText.setWidth(width);
		localProviderOKText.setTouchable(Touchable.disabled);

		localProviderOKButton.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				localProviderOKButton.addAction(Actions.highlightButtonClick());
				localProviderOKText.addAction(Actions.highlightButtonClick());
			}
		});
		localProviderOKButton.addListener(createButtonListener(Constants.Auth.SOCIAL_AUTH_PROVIDER_LOCAL));

		stage.addActor(localProviderOKButton);
		stage.addActor(localProviderOKText);

		localProviderOKButton.setColor(Color.CLEAR);
		localProviderOKText.setColor(Color.CLEAR);
	}

	private void addLocalProviderButton(float width, float height) {
		localProviderButton = new Button(resources.skin, Constants.UI.CLEAR_BUTTON);
		float bWidth = width * 0.6f;
		float bHeight = bWidth * 0.23f;
		localProviderButton.setBounds(width * 0.5f - bWidth * 0.5f, height * 0.23f, bWidth, bHeight);

		localProviderText = new ShaderLabel(resources.fontShader, "Continue without login", resources.skin,
				Constants.UI.SMALL_FONT, Color.WHITE);
		localProviderText.setAlignment(Align.center);
		localProviderText.setY(localProviderButton.getY() + localProviderButton.getHeight() / 2
				- localProviderText.getHeight() * 0.5f);
		localProviderText.setWidth(width);
		localProviderText.setTouchable(Touchable.disabled);

		localProviderButton.addListener(createLocalProviderWarningListener());

		stage.addActor(localProviderButton);
		stage.addActor(localProviderText);

		float currentX = localProviderButton.getX();
		localProviderButton.setX(width * 2);
		localProviderButton.addAction(sequence(delay(0.1f),
				moveTo(currentX, localProviderButton.getY(), 0.7f, Interpolation.pow3)));

		currentX = localProviderText.getX();
		localProviderText.setX(width * 2);
		localProviderText.addAction(sequence(delay(0.1f),
				moveTo(currentX, localProviderText.getY(), 0.7f, Interpolation.pow3)));
	}

	@Override
	public void hide() {
		signInLabel.setText("");
	}

	private void startHideSequence() {
		if (googlePlusButton != null) {
			googlePlusButton.addAction(sequence(delay(0.1f), moveTo(-width, googlePlusButton.getY(), 0.7f, pow3)));
		}

		if (facebookButton != null) {
			facebookButton.addAction(sequence(delay(0.1f), moveTo(-width, facebookButton.getY(), 0.7f, pow3)));
		}

		if (localProviderButton != null) {
			localProviderButton
					.addAction(sequence(delay(0.1f), moveTo(-width, localProviderButton.getY(), 0.7f, pow3)));
		}

		if (localProviderText != null) {
			localProviderText.addAction(sequence(delay(0.1f), moveTo(-width, localProviderText.getY(), 0.7f, pow3)));
		}

		if (localProviderOKButton != null) {
			localProviderOKButton.addAction(sequence(delay(0.1f),
					moveTo(-width, localProviderOKButton.getY(), 0.7f, pow3)));
		}

		if (localProviderOKText != null) {
			localProviderOKText
					.addAction(sequence(delay(0.1f), moveTo(-width, localProviderOKText.getY(), 0.7f, pow3)));
		}

		signInLabel.addAction(sequence(delay(0.1f), moveTo(-width, signInLabel.getY(), 0.7f, pow3), run(new Runnable() {
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
		if (googlePlusButton == null && facebookButton == null) {
			addAuthenticationMethodsToStage();
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
		if (googlePlusButton == null && facebookButton == null) {
			addAuthenticationMethodsToStage();
		}
	}

	@Override
	public boolean hideTitleArea() {
		return false;
	}

	@Override
	public boolean canRefresh() {
		return false;
	}

}