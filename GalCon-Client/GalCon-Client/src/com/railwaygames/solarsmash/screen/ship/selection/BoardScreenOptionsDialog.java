package com.railwaygames.solarsmash.screen.ship.selection;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import org.joda.time.DateTime;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.UISkin;
import com.railwaygames.solarsmash.model.GameBoard;
import com.railwaygames.solarsmash.model.Player;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.event.AboutEvent;
import com.railwaygames.solarsmash.screen.event.CancelGameEvent;
import com.railwaygames.solarsmash.screen.event.RefreshEvent;
import com.railwaygames.solarsmash.screen.event.ResignEvent;
import com.railwaygames.solarsmash.screen.event.TutorialEvent;
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
	private ImageButton tutorialButton;
	private ShaderLabel tutorialText;
	private GameBoard gameBoard;
	private Group statsGroup;
	
	private static final float BUTTON_HEIGHT_RATIO = 0.22f;
	private static final float BUTTON_WIDTH_RATIO = 0.73f;

	public BoardScreenOptionsDialog(GameBoard gameBoard, Resources resources, float width, float height, Stage stage) {
		super(resources, width, height, stage, OKCancelDialog.Type.CANCEL);
		this.gameBoard = gameBoard;

		confirmText = new ShaderLabel(resources.fontShader, "Are you sure you want to", resources.skin,
				Constants.UI.DEFAULT_FONT, Color.BLACK);
		confirmText.setAlignment(Align.center);
		confirmText.setY(getHeight() * 0.5f);
		confirmText.setWidth(getWidth());
		confirmText.setColor(Color.CLEAR);

		addActor(confirmText);
		
		statsGroup = new Group();
		statsGroup.setWidth(width);
		statsGroup.setHeight(height);
		addActor(statsGroup);

		if (gameBoard.players.size() == 1) {
			createCancelButton(resources.fontShader, resources.skin);
		} else {
			createResignButton(resources.fontShader, resources.skin);
		}
		createRefreshButton(resources.fontShader, resources.skin);
		createAboutButton(resources.fontShader, resources.skin);
		createTutorialButton(resources.fontShader, resources.skin);
		createPlayerStats();
	}
	
	private void createPlayerStats() {
		Player player1 = gameBoard.getUser();
		createPlayerLabels(player1, 0.96f, Align.left);

		Player player2 = gameBoard.getEnemy();
		createPlayerLabels(player2, 0.76f, Align.right);

		Integer p1Wins = gameBoard.handleToVictoriesVsOpponent.get(player1.handle);
		p1Wins = p1Wins == null ? 0 : p1Wins;
		Integer p2Wins = gameBoard.handleToVictoriesVsOpponent.get(player2.handle);
		p2Wins = p2Wins == null ? 0 : p2Wins;
		{
			ShaderLabel lbl = new ShaderLabel(resources.fontShader, "Head to Head Record", resources.skin,
					Constants.UI.X_SMALL_FONT, Color.WHITE);
			lbl.setWidth(getWidth());
			lbl.setX(0);
			lbl.setY(getHeight() * 0.84f - lbl.getHeight() * 0.5f);
			lbl.setAlignment(Align.center, Align.center);
			lbl.setTouchable(Touchable.disabled);
			lbl.setColor(Color.CLEAR);
			lbl.addAction(color(Color.WHITE, 0.66f));
			statsGroup.addActor(lbl);
		}
		{
			ShaderLabel lbl = new ShaderLabel(resources.fontShader, winLossRecordString(p1Wins, p2Wins),
					resources.skin, Constants.UI.DEFAULT_FONT, Color.WHITE);
			lbl.setWidth(getWidth());
			lbl.setX(0);
			lbl.setY(getHeight() * 0.81f - lbl.getHeight() * 0.5f);
			lbl.setAlignment(Align.center, Align.center);
			lbl.setTouchable(Touchable.disabled);
			lbl.setColor(Color.CLEAR);
			lbl.addAction(color(Color.WHITE, 0.66f));
			statsGroup.addActor(lbl);
		}
	}
	
	private void createPlayerLabels(Player player, float y, int align) {
		float margin = getWidth() * 0.05f;
		float width = getWidth() - 2.0f * margin;
		{
			ShaderLabel lbl = new ShaderLabel(resources.fontShader, player.handle, resources.skin,
					Constants.UI.DEFAULT_FONT, Color.WHITE);
			lbl.setWidth(width);
			lbl.setX(margin);
			lbl.setY(getHeight() * y - lbl.getHeight() * 0.5f);
			lbl.setAlignment(align, align);
			lbl.setTouchable(Touchable.disabled);
			lbl.setColor(Color.CLEAR);
			if (player.handle.equals(GameLoop.USER.handle)) {
				lbl.addAction(color(Constants.Colors.USER_SHIP_FILL, 0.66f));
			} else {
				lbl.addAction(color(Constants.Colors.ENEMY_SHIP_FILL, 0.66f));
			}
			statsGroup.addActor(lbl);
		}
		{
			ShaderLabel lbl = new ShaderLabel(resources.fontShader, "Overall: " + extractWinLossRecord(player),
					resources.skin, Constants.UI.X_SMALL_FONT, Color.WHITE);
			lbl.setWidth(width);
			lbl.setX(margin);
			lbl.setY(getHeight() * (y - 0.04f) - lbl.getHeight() * 0.5f);
			lbl.setAlignment(align, align);
			lbl.setTouchable(Touchable.disabled);
			lbl.setColor(Color.CLEAR);
			lbl.addAction(color(Color.WHITE, 0.66f));
			statsGroup.addActor(lbl);
		}
	}
	
	private String extractWinLossRecord(Player player) {
		int losses = 0;
		int wins = 0;
		if (player != null) {
			losses = player.losses;
			wins = player.wins;
		}

		return winLossRecordString(wins, losses);
	}

	private String winLossRecordString(int wins, int losses) {
		return "(" + wins + " - " + losses + ")";
	}

	private void createTutorialButton(ShaderProgram fontShader, UISkin skin) {
		tutorialButton = new ImageButton(skin, Constants.UI.BASIC_BUTTON);
		tutorialButton.setLayoutEnabled(false);
		float bWidth = getWidth() * BUTTON_WIDTH_RATIO;
		float bHeight = bWidth * BUTTON_HEIGHT_RATIO;
		tutorialButton.setBounds(getWidth() * 0.5f - bWidth * 0.5f, getHeight() * 0.28f - bHeight * 0.5f, bWidth, bHeight);

		tutorialText = new ShaderLabel(fontShader, "Tutorial", skin, Constants.UI.DEFAULT_FONT, Color.BLACK);
		tutorialText.setAlignment(Align.center);
		tutorialText.setY(tutorialButton.getY() + tutorialButton.getHeight() / 2 - tutorialText.getHeight() * 0.5f);
		tutorialText.setWidth(getWidth());

		addActor(tutorialButton);
		addActor(tutorialText);

		tutorialButton.addListener(tutorialListener);
		tutorialText.addListener(tutorialListener);
	}

	private void createResignButton(ShaderProgram fontShader, UISkin skin) {
		resignButton = new ImageButton(skin, Constants.UI.BASIC_BUTTON);
		resignButton.setLayoutEnabled(false);
		float bWidth = getWidth() * BUTTON_WIDTH_RATIO;
		float bHeight = bWidth * BUTTON_HEIGHT_RATIO;
		resignButton.setBounds(getWidth() * 0.5f - bWidth * 0.5f, getHeight() * 0.44f - bHeight * 0.5f, bWidth, bHeight);

		resignText = new ShaderLabel(fontShader, "Resign", skin, Constants.UI.DEFAULT_FONT, Color.BLACK);
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
		float bWidth = getWidth() * BUTTON_WIDTH_RATIO;
		float bHeight = bWidth * BUTTON_HEIGHT_RATIO;
		cancelButton
				.setBounds(getWidth() * 0.5f - bWidth * 0.5f, getHeight() * 0.44f - bHeight * 0.5f, bWidth, bHeight);

		cancelText = new ShaderLabel(fontShader, "Cancel Game", skin, Constants.UI.DEFAULT_FONT, Color.BLACK);
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
		float bWidth = getWidth() * BUTTON_WIDTH_RATIO;
		float bHeight = bWidth * BUTTON_HEIGHT_RATIO;
		refreshButton
				.setBounds(getWidth() * 0.5f - bWidth * 0.5f, getHeight() * 0.6f - bHeight * 0.5f, bWidth, bHeight);

		refreshText = new ShaderLabel(fontShader, "Refresh", skin, Constants.UI.DEFAULT_FONT, Color.BLACK);
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
		float bWidth = getWidth() * BUTTON_WIDTH_RATIO;
		float bHeight = bWidth * BUTTON_HEIGHT_RATIO;
		aboutButton.setBounds(getWidth() * 0.5f - bWidth * 0.5f, getHeight() * 0.12f - bHeight * 0.5f, bWidth, bHeight);

		aboutText = new ShaderLabel(fontShader, "About", skin, Constants.UI.DEFAULT_FONT, Color.BLACK);
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
			hideAndRemove(tutorialButton);
			hideAndRemove(tutorialText);
			hideAndRemove(statsGroup);

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
	
	private ClickListener tutorialListener = new ClickListener() {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			hide();
			fire(new TutorialEvent());
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
			hideAndRemove(tutorialButton);
			hideAndRemove(tutorialText);
			hideAndRemove(statsGroup);

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
