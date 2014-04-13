package com.railwaygames.solarsmash.screen.tutorial;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;

public class Overview implements Tutorial {

	private Resources resources;
	private Group group;

	public Overview(Resources resources, Group group) {
		this.resources = resources;
		this.group = group;
	}

	@Override
	public int getPageCount() {
		return 1;
	}

	@Override
	public void showPage(int page) {
		{
			ShaderLabel lbl = new ShaderLabel(resources.fontShader, "Welcome Commander", resources.skin,
					Constants.UI.DEFAULT_FONT);
			lbl.setWidth(Gdx.graphics.getWidth());
			lbl.setX(0);
			lbl.setY(Gdx.graphics.getHeight() * 0.7f - lbl.getHeight() * 0.5f);
			lbl.setAlignment(Align.center, Align.center);
			lbl.setTouchable(Touchable.disabled);
			lbl.setColor(Color.CLEAR);
			lbl.addAction(color(Color.WHITE, 0.66f));
			group.addActor(lbl);
		}
		{
			ShaderLabel lbl = new ShaderLabel(resources.fontShader, "", resources.skin, Constants.UI.SMALL_FONT);
			lbl.setWidth(Gdx.graphics.getWidth());
			lbl.setX(0);
			lbl.setY(Gdx.graphics.getHeight() * 0.6f - lbl.getHeight() * 0.5f);
			lbl.setAlignment(Align.center, Align.center);
			lbl.setWrap(true);
			lbl.setTouchable(Touchable.disabled);
			lbl.setColor(Color.CLEAR);
			lbl.addAction(delay(0.66f, color(Color.WHITE, 0.66f)));
			group.addActor(lbl);
		}

	}
}
