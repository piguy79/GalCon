package com.railwaygames.solarsmash.screen.widget;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.railwaygames.solarsmash.model.Point;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.event.CancelDialogEvent;
import com.railwaygames.solarsmash.screen.event.OKDialogEvent;

public abstract class OKCancelDialog extends Dialog {
	public enum Type {
		CANCEL, OK_CANCEL;
	}

	protected ActionButton cancelButton;
	protected ActionButton okButton;

	public OKCancelDialog(Resources resources, float width, float height, Stage stage, Type type) {
		super(resources, width, height, stage);
		addCancelButton();
		if (type == Type.OK_CANCEL) {
			addOkButton();
		}
	}

	private void addCancelButton() {
		cancelButton = new ActionButton(resources.skin, "cancelButton", new Point(getX(), getY()));
		cancelButton.setX(cancelButton.getX() - (cancelButton.getWidth() * 0.4f));
		cancelButton.setY(cancelButton.getY() - (cancelButton.getHeight() * 0.4f));
		cancelButton.setColor(0, 0, 0, 0);
		cancelButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				fire(new CancelDialogEvent());
				cancelButton.addAction(fadeOut(0.4f));
				hide();
			}
		});

		addActor(cancelButton);
	}

	protected void addOkButton() {
		okButton = new ActionButton(resources.skin, "okButton", new Point(getWidth(), 0));
		okButton.setX(okButton.getX() - (okButton.getWidth() * 0.6f));
		okButton.setY(okButton.getY() - (okButton.getHeight() * 0.4f));
		okButton.setColor(0, 0, 0, 0);
		okButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				fire(new OKDialogEvent());
				okButton.addAction(fadeOut(0.4f));
				hide();
			}
		});

		addActor(okButton);
	}

	public void show(Point point) {
		float duration = 0.4f;

		super.show(point, duration);
		showButton(cancelButton, duration);
		if (okButton != null) {
			showButton(okButton, duration);
		}
	}

	protected void showButton(ActionButton button, float duration) {
		button.addAction(sequence(delay(duration), color(new Color(0, 0, 0, 1), 0.4f)));
	}

	public void hide() {
		cancelButton.addAction(Actions.fadeOut(0.4f));
		if (okButton != null) {
			okButton.addAction(Actions.fadeOut(0.4f));
		}
		super.hide(0.3f);
	}
}
