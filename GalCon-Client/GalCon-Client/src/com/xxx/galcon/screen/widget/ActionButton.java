package com.xxx.galcon.screen.widget;

import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.xxx.galcon.model.Point;

public class ActionButton extends ImageButton {

	public ActionButton(Skin skin, String style, float width, float height, Point position) {
		super(skin, style);
		setWidth(width);
		setHeight(height);
		setPosition(position.x, position.y);
	}
	
	public ActionButton(Skin skin, String style, float width, float height) {
		super(skin, style);
		setWidth(width);
		setHeight(height);
	}
	

}
