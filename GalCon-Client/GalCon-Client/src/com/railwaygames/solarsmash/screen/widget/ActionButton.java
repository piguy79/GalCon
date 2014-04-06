package com.railwaygames.solarsmash.screen.widget;

import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.railwaygames.solarsmash.model.Point;
import com.railwaygames.solarsmash.screen.GraphicsUtils;

public class ActionButton extends ImageButton {

	public ActionButton(Skin skin, String style, Point position) {
		super(skin, style);
		GraphicsUtils.setCommonButtonSize(this);
		setPosition(position.x, position.y);

	}
}
