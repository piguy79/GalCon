package com.railwaygames.solarsmash.screen.tutorial;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;

public class Overview extends Tutorial {

	private Resources resources;
	private Group group;

	public Overview(Resources resources, Group group) {
		this.resources = resources;
		this.group = group;
	}

	@Override
	public int getPageCount() {
		return 2;
	}

	@Override
	public void showPage(int page) {
		switch (page) {
		case 1:
			page1();
			break;
		case 2:
			page2();
			break;
		default:
			break;
		}
	}

	@Override
	public String getTopHudText(int page) {
		switch (page) {
		case 2:
			return "This is the map";
		default:
			return "";
		}
	}

	private void page1() {
		{
			ShaderLabel lbl = new ShaderLabel(resources.fontShader, "Welcome\nCommander", resources.skin,
					Constants.UI.LARGE_FONT);
			lbl.setWidth(Gdx.graphics.getWidth());
			lbl.setX(0);
			lbl.setY(Gdx.graphics.getHeight() * 0.7f - lbl.getHeight() * 0.5f);
			lbl.setAlignment(Align.center, Align.center);
			lbl.setTouchable(Touchable.disabled);
			lbl.setColor(Color.CLEAR);
			lbl.addAction(color(Color.WHITE, 0.66f));
			group.addActor(lbl);
		}

		group.addActor(createBasicLabel(resources, Gdx.graphics.getHeight() * 0.45f, 1.00f,
				"As we proceed in our unceasing hunt for resources, we continue to be engaged by enemy forces"));

		group.addActor(createBasicLabel(resources, Gdx.graphics.getHeight() * 0.3f, 2.25f,
				"Let me give you a basic introduction to the view of the battefield and of the controls at your disposal"));
	}

	private void page2() {
		Image image = new Image(new TextureRegionDrawable(resources.tutorialAtlas.findRegion("overview1")));

		float xMargin = group.getWidth() * 0.08f;
		float yMargin = group.getHeight() * 0.01f;

		image.setBounds(xMargin, yMargin, group.getWidth() - 2 * xMargin, group.getHeight() - 2 * yMargin);

		group.addActor(image);

	}
}
