package com.railwaygames.solarsmash.screen.signin;

import static com.badlogic.gdx.math.Interpolation.pow3;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.PartialScreenFeedback;
import com.railwaygames.solarsmash.Strings;
import com.railwaygames.solarsmash.http.GameAction;
import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.model.HandleResponse;
import com.railwaygames.solarsmash.model.Player;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;
import com.railwaygames.solarsmash.screen.widget.ShaderTextField;
import com.railwaygames.solarsmash.screen.widget.ShaderTextField.OnscreenKeyboard;
import com.railwaygames.solarsmash.screen.widget.WaitImageButton;

public class ChooseHandleScreen implements PartialScreenFeedback {
	private Stage stage;

	private ShaderLabel chooseHandleLabel;
	private WaitImageButton waitImage;
	private ImageButton okImageButton;
	private ShaderTextField handleTextField;

	private GameAction gameAction;
	private UserHandleResponseHandler userHandleResponseHandler = new UserHandleResponseHandler();
	private FindUserHandler findUserHandler = new FindUserHandler();

	private String result = null;
	private OnscreenKeyboard keyboard;

	private Resources resources;
	private float width;
	private float height;

	public ChooseHandleScreen(Resources resources, GameAction gameAction, OnscreenKeyboard keyboard) {
		this.resources = resources;
		this.gameAction = gameAction;

		this.keyboard = keyboard;
	}

	@Override
	public void resize(int width, int height) {
		float buttonWidth = .25f * (float) width;
		if (waitImage != null) {
			waitImage.setWidth(buttonWidth);
			waitImage.setHeight(buttonWidth);
			waitImage.setX(width / 2 - buttonWidth / 2);
			waitImage.setY(height / 2 - buttonWidth / 2);
		}

		this.width = width;
		this.height = height;
	}

	@Override
	public void show(Stage stage) {
		this.stage = stage;
		this.width = Gdx.graphics.getWidth();
		this.height = Gdx.graphics.getHeight();

		Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
		String authProvider = prefs.getString(Constants.Auth.SOCIAL_AUTH_PROVIDER);
		String id = prefs.getString(authProvider + Constants.ID);

		if (id.isEmpty()) {
			result = "signIn";
			return;
		}

		waitImage = new WaitImageButton(resources.skin);
		stage.addActor(waitImage);

		waitImage.start();
		gameAction.findUserInformation(findUserHandler, id, authProvider);
	}

	private void addHandleFields() {
		chooseHandleLabel = new ShaderLabel(resources.fontShader, "Galactic explorer,\nchoose a username",
				resources.skin, Constants.UI.DEFAULT_FONT, Color.WHITE);
		chooseHandleLabel.setAlignment(Align.center);
		chooseHandleLabel.setWidth(width);
		chooseHandleLabel.setX(width / 2 - chooseHandleLabel.getWidth() / 2);
		chooseHandleLabel.setY(0.55f * height);
		stage.addActor(chooseHandleLabel);

		handleTextField = new ShaderTextField("", resources);
		handleTextField.setWidth(width * 0.75f);
		handleTextField.setHeight(height * .08f);
		handleTextField.setX(width * 0.5f - handleTextField.getWidth() * 0.6f);
		handleTextField.setY(0.45f * height);
		handleTextField.setOnscreenKeyboard(keyboard);

		stage.addActor(handleTextField);

		okImageButton = new ImageButton(resources.skin, Constants.UI.OK_BUTTON);
		okImageButton.setWidth(height * .08f);
		okImageButton.setHeight(height * .08f);
		okImageButton.setX(width / 2 + handleTextField.getWidth() * 0.42f);
		okImageButton.setY(0.45f * height);
		stage.addActor(okImageButton);

		okImageButton.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				handleTextField.getOnscreenKeyboard().show(false);
				chooseHandleLabel.setText("");
				requestHandle();
			}
		});
	}

	private void requestHandle() {
		String text = handleTextField.getText();
		if (text.length() < 3) {
			chooseHandleLabel.setText(Strings.HANDLE_VALID_LENGTH);
		} else {
			Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
			String authProvider = prefs.getString(Constants.Auth.SOCIAL_AUTH_PROVIDER);
			waitImage.start();
			gameAction.requestHandleForId(userHandleResponseHandler, GameLoop.USER.auth.auth.get(authProvider),
					handleTextField.getText(), authProvider);
		}
	}

	@Override
	public void hide() {
		if (handleTextField != null) {
			handleTextField.getOnscreenKeyboard().show(false);
		}
	}

	@Override
	public void render(float delta) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getRenderResult() {
		return result;
	}

	@Override
	public void resetState() {
		result = null;
	}

	private void startHideSequence() {
		if (chooseHandleLabel != null) {
			chooseHandleLabel.addAction(sequence(delay(0.25f),
					moveTo(-Gdx.graphics.getWidth(), chooseHandleLabel.getY(), 0.9f, pow3)));
		}

		if (handleTextField != null) {
			handleTextField.addAction(sequence(delay(0.5f),
					moveTo(-Gdx.graphics.getWidth(), handleTextField.getY(), 0.9f, pow3)));
			okImageButton.addAction(sequence(delay(0.6f),
					moveTo(-Gdx.graphics.getWidth(), okImageButton.getY(), 0.9f, pow3), run(new Runnable() {
						@Override
						public void run() {
							result = "hasHandle";
						}
					})));
		} else {
			result = "hasHandle";
		}
	}

	private class FindUserHandler implements UIConnectionResultCallback<Player> {

		@Override
		public void onConnectionResult(Player player) {
			if (player.sessionExpired) {
				result = "signIn";
			} else {
				if (player.handle != null && !player.handle.isEmpty()) {
					GameLoop.USER = player;
					Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
					prefs.putString(Constants.HANDLE, player.handle);
					prefs.flush();

					startHideSequence();
				} else {
					addHandleFields();
				}
			}
			waitImage.stop();
		}

		@Override
		public void onConnectionError(String msg) {
			waitImage.stop();
		}
	}

	private class UserHandleResponseHandler implements UIConnectionResultCallback<HandleResponse> {

		@Override
		public void onConnectionResult(HandleResponse response) {
			if (response.handleCreated) {
				Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
				prefs.putString(Constants.HANDLE, response.player.handle);
				prefs.flush();

				GameLoop.USER = response.player;
				startHideSequence();
			}

			waitImage.stop();
		}

		@Override
		public void onConnectionError(String msg) {
			chooseHandleLabel.setText(msg);
			waitImage.stop();
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
