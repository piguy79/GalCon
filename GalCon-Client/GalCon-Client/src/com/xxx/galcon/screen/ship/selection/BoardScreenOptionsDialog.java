package com.xxx.galcon.screen.ship.selection;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.xxx.galcon.Constants;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.screen.Resources;
import com.xxx.galcon.screen.event.CancelGameEvent;
import com.xxx.galcon.screen.event.RefreshEvent;
import com.xxx.galcon.screen.event.ResignEvent;
import com.xxx.galcon.screen.widget.OKCancelDialog;
import com.xxx.galcon.screen.widget.ShaderLabel;

public class BoardScreenOptionsDialog extends OKCancelDialog {

	private ImageButton resignButton;
	private ShaderLabel resignText;
	private ImageButton cancelButton;
	private ShaderLabel cancelText;
	private ShaderLabel confirmText;
	private ImageButton refreshButton;
	private ShaderLabel refreshText;

	public BoardScreenOptionsDialog(GameBoard gameBoard, Resources resources, float width, float height, Stage stage) {
		super(resources, width, height, stage, OKCancelDialog.Type.CANCEL);

		confirmText = new ShaderLabel(resources.fontShader, "Are you sure you want to", resources.skin,
				Constants.UI.BASIC_BUTTON_TEXT);
		confirmText.setAlignment(Align.center);
		confirmText.setY(getHeight() * 0.6f);
		confirmText.setWidth(getWidth());
		confirmText.setColor(Color.CLEAR);

		addActor(confirmText);

		if(gameBoard.players.size() == 1){
			createCancelButton(resources.fontShader, resources.skin);
		}else{
			createResignButton(resources.fontShader, resources.skin);
		}
		createRefreshButton(resources.fontShader, resources.skin);
	}

	private void createResignButton(ShaderProgram fontShader, UISkin skin) {
		resignButton = new ImageButton(skin, Constants.UI.BASIC_BUTTON);
		resignButton.setLayoutEnabled(false);
		float bWidth = getWidth() * 0.66f;
		float bHeight = bWidth * 0.30f;
		resignButton
				.setBounds(getWidth() * 0.5f - bWidth * 0.5f, getHeight() * 0.30f - bHeight * 0.5f, bWidth, bHeight);

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
				.setBounds(getWidth() * 0.5f - bWidth * 0.5f, getHeight() * 0.30f - bHeight * 0.5f, bWidth, bHeight);

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
				.setBounds(getWidth() * 0.5f - bWidth * 0.5f, getHeight() * 0.7f - bHeight * 0.5f, bWidth, bHeight);

		refreshText = new ShaderLabel(fontShader, "Refresh", skin, Constants.UI.BASIC_BUTTON_TEXT);
		refreshText.setAlignment(Align.center);
		refreshText.setY(refreshButton.getY() + refreshButton.getHeight() / 2 - refreshText.getHeight() * 0.5f);
		refreshText.setWidth(getWidth());

		addActor(refreshButton);
		addActor(refreshText);

		refreshButton.addListener(refreshListener);
		refreshText.addListener(refreshListener);
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
			refreshButton.addAction(sequence(alpha(0.0f, 0.3f), run(new Runnable() {
				public void run() {
					refreshButton.remove();
					refreshButton = null;
				};
			})));
			refreshText.addAction(sequence(alpha(0.0f, 0.3f), run(new Runnable() {
				public void run() {
					refreshText.remove();
					refreshText = null;
				};
			})));
			resignText.addAction(moveBy(0, getHeight() * 0.2f, 0.3f));
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
	
	private ClickListener cancelListener = new ClickListener() {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			cancelButton.addAction(sequence(alpha(0.0f, 0.3f), run(new Runnable() {
				public void run() {
					cancelButton.remove();
					cancelButton = null;
					cancelText.removeListener(cancelListener);
				};
			})));
			refreshButton.addAction(sequence(alpha(0.0f, 0.3f), run(new Runnable() {
				public void run() {
					refreshButton.remove();
					refreshButton = null;
				};
			})));
			refreshText.addAction(sequence(alpha(0.0f, 0.3f), run(new Runnable() {
				public void run() {
					refreshText.remove();
					refreshText = null;
				};
			})));
			cancelText.addAction(moveBy(0, getHeight() * 0.2f, 0.3f));
			confirmText.addAction(sequence(color(Color.BLACK, 0.3f)));

			addOkButton();
			showButton(okButton, 0.3f);
			okButton.addListener(new ClickListener() {
				public void clicked(InputEvent event, float x, float y) {
					fire(new CancelGameEvent());
				};
			});
		}
	};
}
