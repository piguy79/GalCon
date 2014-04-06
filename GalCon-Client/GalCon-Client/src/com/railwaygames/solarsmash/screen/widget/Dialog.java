package com.railwaygames.solarsmash.screen.widget;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.railwaygames.solarsmash.model.Point;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.overlay.DialogOverlay;

public class Dialog extends Group {

	protected Resources resources;
	private AtlasRegion dialogTextureBg;
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
		dialogTextureBg = resources.menuAtlas.findRegion("dialog_bg_no_shadow");
		TextureRegionDrawable tex = new TextureRegionDrawable(dialogTextureBg);
		background = new Image(tex);
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
}
