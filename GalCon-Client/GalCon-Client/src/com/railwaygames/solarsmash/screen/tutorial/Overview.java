package com.railwaygames.solarsmash.screen.tutorial;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;

public class Overview extends Tutorial {

	private Resources resources;
	private Group group;
	private boolean page5Good = false;
	private boolean isMyMove = true;

	public Overview(Resources resources, Group group, boolean isMyMove) {
		this.resources = resources;
		this.group = group;
		this.isMyMove = isMyMove;
	}

	@Override
	public int getPageCount() {
		return 6;
	}

	@Override
	public int getPage(String continuePoint) {
		if (Constants.Tutorial.BREAK_TOUCH_USER_PLANET.equals(continuePoint)) {
			return 3;
		} else if (Constants.Tutorial.BREAK_TOUCH_OTHER_PLANET.equals(continuePoint)) {
			return 4;
		} else if (Constants.Tutorial.BREAK_SEND_MOVES_OK.equals(continuePoint)) {
			page5Good = true;
			return 5;
		} else if (Constants.Tutorial.BREAK_SEND_MOVES_CANCEL.equals(continuePoint)) {
			page5Good = false;
			return 5;
		}

		return 1;
	}

	@Override
	public void showPage(int page) {
		List<Actor> toRemove = new ArrayList<Actor>();
		for (Actor actor : group.getChildren()) {
			if (actor instanceof Label) {
				toRemove.add(actor);
			}
		}
		for (Actor actor : toRemove) {
			actor.remove();
		}
		switch (page) {
		case 1:
			page1();
			break;
		case 2:
			page2();
			break;
		case 3:
			page3();
			break;
		case 4:
			page4();
			break;
		case 5:
			page5();
			break;
		case 6:
			page6();
			break;
		}
	}

	private void page1() {
		{
			ShaderLabel lbl = new ShaderLabel(resources.fontShader, "Welcome\nCommander", resources.skin,
					Constants.UI.LARGE_FONT, Color.WHITE);
			lbl.setWidth(Gdx.graphics.getWidth());
			lbl.setX(0);
			lbl.setY(Gdx.graphics.getHeight() * 0.7f - lbl.getHeight() * 0.5f);
			lbl.setAlignment(Align.center, Align.center);
			lbl.setTouchable(Touchable.disabled);
			lbl.setColor(Color.CLEAR);
			lbl.addAction(color(Color.WHITE, 0.66f));
			group.addActor(lbl);
		}

		group.addActor(createBasicLabel(resources, Gdx.graphics.getHeight() * 0.45f, 0.5f,
				"The objective: eliminate all of your enemy's planets by attacking them with ships, round after round"));

		group.addActor(createBasicLabel(resources, Gdx.graphics.getHeight() * 0.25f, 1.75f,
				"This tutorial will show you the basic gameplay mechanics"));
	}

	private void page2() {
		group.addActor(createBasicLabel(resources, Gdx.graphics.getHeight() * 0.45f, 0.0f,
				"Green lines show planets in your control. Red lines show a planet in the enemy's control"));

		if (isMyMove) {
			group.addActor(createBasicLabel(resources, Gdx.graphics.getHeight() * 0.25f, 1.25f,
					"Tap one of your planets to begin"));
		}
	}

	private void page3() {
		if (isMyMove) {
			group.addActor(createBasicLabel(resources, Gdx.graphics.getHeight() * 0.65f, 0.0f, "Excellent"));

			group.addActor(createBasicLabel(resources, Gdx.graphics.getHeight() * 0.45f, 1.25f,
					"Now tap a different planet to send ships for attack"));
		} else {
			group.addActor(createBasicLabel(resources, Gdx.graphics.getHeight() * 0.45f, 0.0f,
					"When it is your turn, you would tap a different planet in order to attack"));
		}

		group.addActor(createBasicLabel(resources, Gdx.graphics.getHeight() * 0.25f, 1.50f,
				"Hint: Larger planets will build more ships for you per round compared to smaller planets"));
	}

	private void page4() {
		group.addActor(createBasicLabel(resources, Gdx.graphics.getHeight() * 0.65f, 0.0f,
				"Next: sending a specific number of ships to attack"));

		group.addActor(createBasicLabel(resources, Gdx.graphics.getHeight() * 0.45f, 1.25f,
				"Drag the bottom slider until you send a number greater than or equal to the number on the defending planet"));

		group.addActor(createBasicLabel(resources, Gdx.graphics.getHeight() * 0.25f, 2.00f,
				"Hint: The top right display will show you how many rounds until the ships impact the planet"));
	}

	private void page5() {
		if (isMyMove) {
			if (page5Good) {
				group.addActor(createBasicLabel(resources, Gdx.graphics.getHeight() * 0.65f, 0.0f,
						"Great. You can see all moves in progress on the bottom of the screen and can tap each one to inspect it"));
			} else {
				group.addActor(createBasicLabel(
						resources,
						Gdx.graphics.getHeight() * 0.65f,
						0.0f,
						"It appears you canceled the move. You can try again soon and see all moves in progress on the bottom of the screen. Tap any move to inspect it"));
			}

			group.addActor(createBasicLabel(resources, Gdx.graphics.getHeight() * 0.45f, 1.25f,
					"You can queue up many moves at one time by following the same steps"));
		} else {
			group.addActor(createBasicLabel(resources, Gdx.graphics.getHeight() * 0.65f, 0.0f,
					"You can queue up many moves at one time by following the same steps"));
		}
	}

	private void page6() {
		group.addActor(createBasicLabel(resources, Gdx.graphics.getHeight() * 0.65f, 0.0f,
				"After issuing all of your moves for the round, "
						+ "click the green button in the bottom right to end your turn"));

		group.addActor(createBasicLabel(resources, Gdx.graphics.getHeight() * 0.45f, 1.25f,
				"That's all for now.\nGood luck!"));
	}

	@Override
	public String pauseEvent(int page) {
		if (!isMyMove) {
			return "";
		}
		switch (page) {
		case 2:
			return Constants.Tutorial.BREAK_TOUCH_USER_PLANET;
		case 3:
			return Constants.Tutorial.BREAK_TOUCH_OTHER_PLANET;
		case 4:
			return Constants.Tutorial.BREAK_SEND_MOVES;
		default:
			return "";
		}
	}
}
