package com.railwaygames.solarsmash.screen.widget;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.model.Point;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.overlay.DialogOverlay;

public class Dialog extends Group {

	protected Resources resources;
	private Image background;
	private DialogOverlay overlay;

	public Dialog(Resources resources, float width, float height, Stage stage) {
		super();
		this.resources = resources;
		setWidth(width);
		setHeight(height);

		overlay = new DialogOverlay(resources);
		stage.addActor(overlay);
		addBackground();
	}

	private void addBackground() {
		background = new Image(resources.skin, Constants.UI.GRAY_IMAGE_BG);
		background.setWidth(getWidth());
		background.setHeight(getHeight());
		addActor(background);
	}

	protected void show(Point point, float duration) {
		MoveToAction moveDialogAction = new MoveToAction();
		moveDialogAction.setX(point.x);
		moveDialogAction.setY(point.y);
		moveDialogAction.setDuration(duration);

		addAction(moveDialogAction);
	}

	protected void hide(float duration) {
		MoveToAction moveDialogAction = new MoveToAction();
		moveDialogAction.setX(-getWidth());
		moveDialogAction.setY(getY());
		moveDialogAction.setDuration(duration);

		addAction(Actions.sequence(moveDialogAction, new RunnableAction() {
			@Override
			public void run() {
				overlay.remove();
				remove();
			}
		}));
	}

	@Override
	public boolean remove() {
		overlay.remove();
		return super.remove();
	}

	public DialogOverlay getOverlay() {
		return overlay;
	}
}
