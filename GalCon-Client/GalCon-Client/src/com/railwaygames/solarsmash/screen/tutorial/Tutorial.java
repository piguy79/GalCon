package com.railwaygames.solarsmash.screen.tutorial;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;

public abstract class Tutorial {

	public abstract int getPageCount();
	
	public abstract int getPage(String continuePoint);

	public abstract void showPage(int page);
	
	public abstract String pauseEvent(int page);

	protected ShaderLabel createBasicLabel(Resources resources, float y, float delay, String text) {
		ShaderLabel lbl = new ShaderLabel(resources.fontShader, text, resources.skin, Constants.UI.SMALL_FONT,
				Color.WHITE);
		lbl.setWidth(Gdx.graphics.getWidth() * 0.7f);
		lbl.setX(Gdx.graphics.getWidth() * 0.15f);
		lbl.setAlignment(Align.center, Align.center);
		lbl.setWrap(true);
		lbl.setTouchable(Touchable.disabled);
		lbl.setColor(Color.CLEAR);
		lbl.setY(y);
		lbl.addAction(delay(delay, color(Color.WHITE, 0.66f)));

		return lbl;
	}
}
