package com.railwaygames.solarsmash.screen.widget;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.railwaygames.solarsmash.model.Point;
import com.railwaygames.solarsmash.screen.GraphicsUtils;

public class ActionButton extends Button {

	public ActionButton(Skin skin, String style, Point position) {
		super(skin, style);
		GraphicsUtils.setCommonButtonSize(this);
		setPosition(position.x, position.y);

	}
}
