package com.railwaygames.solarsmash.screen.tutorial;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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
	private TextureAtlas atlas;

	public Overview(Resources resources, Group group) {
		this.resources = resources;
		this.group = group;

		resources.assetManager.load("data/images/tutorial.atlas", TextureAtlas.class);
		resources.assetManager.finishLoading();
		atlas = resources.assetManager.get("data/images/tutorial.atlas", TextureAtlas.class);
	}

	@Override
	public int getPageCount() {
		return 12;
	}

	@Override
	public void showPage(int page) {
//		group.clear();

		switch (page) {
		case 1:
			page1();
			break;
		case 2:
			addImage("overview1");
			break;
		case 3:
			addImage("overview2");
			break;
		case 4:
			addImage("overview2");
			break;
		case 5:
			addImage("overview1");
			break;
		case 6:
			addImage("overview3");
			break;
		case 7:
			addImage("overview4");
			break;
		case 8:
			addImage("overview4");
			break;
		case 9:
			addImage("overview4");
			break;
		case 10:
			addImage("overview5");
			break;
		case 11:
			addImage("overview4");
			break;
		case 12:
			addImage("overview4");
			break;
		default:
			break;
		}
	}

	@Override
	public String getTopHudText(int page) {
		switch (page) {
		case 2:
			return "This is your view of the battlefield";
		case 3:
			return "These planets are currently owned by a player";
		case 4:
			return "Green lines show planets in your control. Red lines show a planet in the enemy's control";
		case 5:
			return "The number on any planet represents the number of ships currently on that planet";
		case 6:
			return "To move, tap a planet you own then tap a planet to send ships to. Drag the slider on the bottom";
		case 7:
			return "All moves in progress show on the bottom. Tap any move to see it";
		case 8:
			return "All ships are cloaked while moving. Your enemy can't see your moves and you can't see their moves";
		case 9:
			return "Every round, all owned planets will build more ships";
		case 10:
			return "Double tap on any planet to see how many ships can be built per round";
		case 11:
			return "When you are done issuing moves for this round, tap the green button in the bottom right";
		case 12:
			return "Good luck commander!";
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
				"We continue to be engaged by enemy forces as we search for the resources necessary for our survival"));

		group.addActor(createBasicLabel(resources, Gdx.graphics.getHeight() * 0.3f, 2.25f,
				"Let me give you a basic introduction to the view of the battefield and of the controls at your disposal"));
	}

	private void addImage(String imageName) {
		Image image = new Image(new TextureRegionDrawable(atlas.findRegion(imageName)));

		float xMargin = group.getWidth() * 0.09f;
		float yMargin = group.getHeight() * 0.01f;

		image.setBounds(xMargin, yMargin, group.getWidth() - 2 * xMargin, group.getHeight() - 2 * yMargin);

		group.addActor(image);
	}
}
