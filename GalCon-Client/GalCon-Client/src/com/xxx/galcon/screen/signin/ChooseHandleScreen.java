package com.xxx.galcon.screen.signin;

import static com.badlogic.gdx.math.Interpolation.pow3;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import static com.xxx.galcon.Util.createShader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.PartialScreenFeedback;
import com.xxx.galcon.Strings;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.HandleResponse;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.screen.widget.ShaderLabel;
import com.xxx.galcon.screen.widget.ShaderTextField;
import com.xxx.galcon.screen.widget.WaitImageButton;

public class ChooseHandleScreen implements PartialScreenFeedback {
	private Skin skin;
	private Stage stage;

	private ShaderProgram fontShader;
	private ShaderLabel chooseHandleLabel;
	private WaitImageButton waitImage;
	private ImageButton okImageButton;
	private ShaderTextField handleTextField;

	private GameAction gameAction;
	private UserHandleResponseHandler userHandleResponseHandler = new UserHandleResponseHandler();
	private FindUserHandler findUserHandler = new FindUserHandler();

	private String result = null;

	public ChooseHandleScreen(Skin skin, GameAction gameAction, AssetManager assetManager) {
		this.skin = skin;
		this.gameAction = gameAction;

		fontShader = createShader("data/shaders/font-vs.glsl", "data/shaders/font-fs.glsl");
	}

	@Override
	public void show(Stage stage, float width, float height) {
		this.stage = stage;

		Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
		String email = prefs.getString(Constants.EMAIL, "");
		if (email.isEmpty()) {
			result = "signIn";
			return;
		}

		waitImage = new WaitImageButton(skin);
		float buttonWidth = .25f * (float) width;
		waitImage.setWidth(buttonWidth);
		waitImage.setHeight(buttonWidth);
		waitImage.setX(width / 2 - buttonWidth / 2);
		waitImage.setY(height / 2 - buttonWidth / 2);
		stage.addActor(waitImage);

		waitImage.start();
		gameAction.findUserInformation(findUserHandler, email);
	}

	private void addHandleFields() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();

		chooseHandleLabel = new ShaderLabel(fontShader, "Galactic explorer,\nchoose a username", skin,
				Constants.UI.DEFAULT_FONT);
		chooseHandleLabel.setAlignment(Align.center);
		chooseHandleLabel.setWidth(width);
		chooseHandleLabel.setX(width / 2 - chooseHandleLabel.getWidth() / 2);
		chooseHandleLabel.setY(0.5f * height);
		stage.addActor(chooseHandleLabel);

		handleTextField = new ShaderTextField(fontShader, "", skin, Constants.UI.TEXT_FIELD);
		handleTextField.setWidth(width * 0.75f);
		handleTextField.setHeight(height * .08f);
		handleTextField.setX(width * 0.5f - handleTextField.getWidth() * 0.6f);
		handleTextField.setY(0.4f * height);
		handleTextField.setOnscreenKeyboard(new ShaderTextField.DefaultOnscreenKeyboard());

		stage.addActor(handleTextField);

		okImageButton = new ImageButton(skin, Constants.UI.OK_BUTTON);
		okImageButton.setWidth(height * .08f);
		okImageButton.setHeight(height * .08f);
		okImageButton.setX(width / 2 + handleTextField.getWidth() * 0.42f);
		okImageButton.setY(0.4f * height);
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
			waitImage.start();
			gameAction.requestHandleForEmail(userHandleResponseHandler, GameLoop.USER.email, handleTextField.getText());
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
			okImageButton.addAction(sequence(delay(0.5f),
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
			if (player.sessionExpired || player.sessionInvalid) {
				result = "signIn";
			} else {
				if (player.handle != null && !player.handle.isEmpty()) {
					GameLoop.USER = player;
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

			if (response.reason != null) {
				chooseHandleLabel.setText(response.reason);
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
}
