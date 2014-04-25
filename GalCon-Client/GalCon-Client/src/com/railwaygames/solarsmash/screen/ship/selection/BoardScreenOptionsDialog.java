package com.railwaygames.solarsmash.screen.ship.selection;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import org.joda.time.DateTime;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.UISkin;
import com.railwaygames.solarsmash.model.GameBoard;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.event.AboutEvent;
import com.railwaygames.solarsmash.screen.event.CancelGameEvent;
import com.railwaygames.solarsmash.screen.event.RefreshEvent;
import com.railwaygames.solarsmash.screen.event.ResignEvent;
import com.railwaygames.solarsmash.screen.widget.OKCancelDialog;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;

public class BoardScreenOptionsDialog extends OKCancelDialog {

	private ImageButton resignButton;
	private ShaderLabel resignText;
	private ImageButton cancelButton;
	private ShaderLabel cancelText;
	private ShaderLabel confirmText;
	private ImageButton refreshButton;
	private ShaderLabel refreshText;
	private ImageButton aboutButton;
	private ShaderLabel aboutText;
	private GameBoard gameBoard;

	public BoardScreenOptionsDialog(GameBoard gameBoard, Resources resources, float width, float height, Stage stage) {
		super(resources, width, height, stage, OKCancelDialog.Type.CANCEL);
		this.gameBoard = gameBoard;

		confirmText = new ShaderLabel(resources.fontShader, "Are you sure you want to", resources.skin,
				Constants.UI.BASIC_BUTTON_TEXT);
		confirmText.setAlignment(Align.center);
		confirmText.setY(getHeight() * 0.6f);
		confirmText.setWidth(getWidth());
		confirmText.setColor(Color.CLEAR);

		addActor(confirmText);

		if (gameBoard.players.size() == 1) {
			createCancelButton(resources.fontShader, resources.skin);
		} else {
			createResignButton(resources.fontShader, resources.skin);
		}
		createRefreshButton(resources.fontShader, resources.skin);
		createAboutButton(resources.fontShader, resources.skin);
	}

	private void createResignButton(ShaderProgram fontShader, UISkin skin) {
		resignButton = new ImageButton(skin, Constants.UI.BASIC_BUTTON);
		resignButton.setLayoutEnabled(false);
		float bWidth = getWidth() * 0.66f;
		float bHeight = bWidth * 0.30f;
		resignButton.setBounds(getWidth() * 0.5f - bWidth * 0.5f, getHeight() * 0.5f - bHeight * 0.5f, bWidth, bHeight);

		resignText = new ShaderLabel(fontShader, "Resign", skin, Constants.UI.BASIC_BUTTON_TEXT);
		resignText.setAlignment(Align.center);
		resignText.setY(resignButton.getY() + resignButton.getHeight() / 2 - resignText.getHeight() * 0.5f);
		resignText.setWidth(getWidth());

		addActor(resignButton);
		addActor(resignText);

		resignButton.addListener(resignListener);
		resignText.addListener(resignListener);
	}

	private void createCancelButton(ShaderProgram fontShader, UISkin skin) {
		cancelButton = new ImageButton(skin, Constants.UI.BASIC_BUTTON);
		cancelButton.setLayoutEnabled(false);
		float bWidth = getWidth() * 0.66f;
		float bHeight = bWidth * 0.30f;
		cancelButton
				.setBounds(getWidth() * 0.5f - bWidth * 0.5f, getHeight() * 0.50f - bHeight * 0.5f, bWidth, bHeight);

		cancelText = new ShaderLabel(fontShader, "Cancel Game", skin, Constants.UI.BASIC_BUTTON_TEXT);
		cancelText.setAlignment(Align.center);
		cancelText.setY(cancelButton.getY() + cancelButton.getHeight() / 2 - cancelText.getHeight() * 0.5f);
		cancelText.setWidth(getWidth());

		addActor(cancelButton);
		addActor(cancelText);

		cancelButton.addListener(cancelListener);
		cancelText.addListener(cancelListener);
	}

	private void createRefreshButton(ShaderProgram fontShader, UISkin skin) {
		refreshButton = new ImageButton(skin, Constants.UI.BASIC_BUTTON);
		refreshButton.setLayoutEnabled(false);
		float bWidth = getWidth() * 0.66f;
		float bHeight = bWidth * 0.30f;
		refreshButton
				.setBounds(getWidth() * 0.5f - bWidth * 0.5f, getHeight() * 0.8f - bHeight * 0.5f, bWidth, bHeight);

		refreshText = new ShaderLabel(fontShader, "Refresh", skin, Constants.UI.BASIC_BUTTON_TEXT);
		refreshText.setAlignment(Align.center);
		refreshText.setY(refreshButton.getY() + refreshButton.getHeight() / 2 - refreshText.getHeight() * 0.5f);
		refreshText.setWidth(getWidth());

		addActor(refreshButton);
		addActor(refreshText);

		refreshButton.addListener(refreshListener);
		refreshText.addListener(refreshListener);
	}

	private void createAboutButton(ShaderProgram fontShader, UISkin skin) {
		aboutButton = new ImageButton(skin, Constants.UI.BASIC_BUTTON);
		aboutButton.setLayoutEnabled(false);
		float bWidth = getWidth() * 0.66f;
		float bHeight = bWidth * 0.30f;
		aboutButton.setBounds(getWidth() * 0.5f - bWidth * 0.5f, getHeight() * 0.2f - bHeight * 0.5f, bWidth, bHeight);

		aboutText = new ShaderLabel(fontShader, "About", skin, Constants.UI.BASIC_BUTTON_TEXT);
		aboutText.setAlignment(Align.center);
		aboutText.setY(aboutButton.getY() + aboutButton.getHeight() / 2 - aboutText.getHeight() * 0.5f);
		aboutText.setWidth(getWidth());

		addActor(aboutButton);
		addActor(aboutText);

		aboutButton.addListener(aboutListener);
		aboutText.addListener(aboutListener);
	}

	private ClickListener resignListener = new ClickListener() {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			resignButton.addAction(sequence(alpha(0.0f, 0.3f), run(new Runnable() {
				public void run() {
					resignButton.remove();
					resignButton = null;
					resignText.removeListener(resignListener);
				};
			})));
			hideAndRemove(refreshButton);
			hideAndRemove(refreshText);
			hideAndRemove(aboutButton);
			hideAndRemove(aboutText);

			confirmText.addAction(sequence(color(Color.BLACK, 0.3f)));

			addOkButton();
			showButton(okButton, 0.3f);
			okButton.addListener(new ClickListener() {
				public void clicked(InputEvent event, float x, float y) {
					fire(new ResignEvent());
				};
			});
		}
	};

	private ClickListener refreshListener = new ClickListener() {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			hide();
			fire(new RefreshEvent());
		}
	};

	private ClickListener aboutListener = new ClickListener() {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			hide();
			fire(new AboutEvent());
		}
	};

	private void hideAndRemove(final Actor actor) {
		actor.addAction(sequence(alpha(0.0f, 0.3f), run(new Runnable() {
			public void run() {
				actor.remove();
			};
		})));
	}

	private ClickListener cancelListener = new ClickListener() {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			DateTime now = new DateTime();
			long timeLeft = (gameBoard.createdDate.getMillis() + 60 * 60 * 1000) - now.getMillis();

			hideAndRemove(refreshButton);
			hideAndRemove(refreshText);
			hideAndRemove(aboutButton);
			hideAndRemove(aboutText);

			if (timeLeft > 0) {
				cancelButton.addAction(sequence(alpha(0.0f, 0.3f), run(new Runnable() {
					public void run() {
						cancelButton.remove();
						cancelButton = null;
						cancelText.removeListener(cancelListener);
					};
				})));

				int minsLeft = (int) (timeLeft / 1000.0f / 60.0f);
				cancelText.setY(getHeight() * 0.5f);
				cancelText.setWrap(true);
				cancelText.setText("You will be able to cancel this game in " + minsLeft
						+ " minutes if no one has joined.");
			} else {
				cancelButton.addAction(sequence(alpha(0.0f, 0.3f), run(new Runnable() {
					public void run() {
						cancelButton.remove();
						cancelButton = null;
						cancelText.removeListener(cancelListener);
					};
				})));

				cancelText.addAction(Actions.moveBy(0, getHeight() * 0.2f, 0.3f));
				confirmText.addAction(sequence(color(Color.BLACK, 0.3f)));

				addOkButton();
				showButton(okButton, 0.3f);
				okButton.addListener(new ClickListener() {
					public void clicked(InputEvent event, float x, float y) {
						fire(new CancelGameEvent());
					};
				});
			}
		}
	};
}
