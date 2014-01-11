package com.xxx.galcon.screen.widget;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.screen.event.CancelDialogEvent;

public abstract class CancelEnabledDialog extends Dialog{
	
	protected ActionButton cancelButton;
	private UISkin skin;

	public CancelEnabledDialog(AssetManager assetManager, float width,
			float height, Stage stage, UISkin skin) {
		super(assetManager, width, height, stage);
		this.skin = skin;
		addCancelButton();
	}
	
	private void addCancelButton(){
		cancelButton = new ActionButton(skin,"cancelButton", new Point(getX(), getY()));
		cancelButton.setX(cancelButton.getX() - (cancelButton.getWidth() * 0.4f));
		cancelButton.setY(cancelButton.getY() - (cancelButton.getHeight() * 0.4f));
		cancelButton.setColor(0, 0, 0, 0);
		cancelButton.addListener(new ClickListener(){@Override
		public void clicked(InputEvent event, float x, float y) {
			fire(new CancelDialogEvent());
			cancelButton.addAction(Actions.fadeOut(0.4f));
			hide();
		}});
		
		addActor(cancelButton);
	}
	
	@Override
	public void show(Point point, float duration) {
		super.show(point, duration);
		cancelButton.addAction(Actions.sequence(Actions.delay(duration), Actions.color(new Color(0, 0, 0, 1), 0.4f)));
	}
	
	public void hide(){
		cancelButton.addAction(Actions.fadeOut(0.4f));
		doHide();
	}
	
	public abstract void doHide();

}
