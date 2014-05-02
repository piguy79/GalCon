package com.railwaygames.solarsmash.screen.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.config.ConfigResolver;
import com.railwaygames.solarsmash.model.Rank;
import com.railwaygames.solarsmash.screen.Resources;

public class XpProgressBar extends Group {

	private Resources resources;
	private float height;
	private float width;

	private Image mainXp;
	private Image coverXp;

	private Rank usersCurrentRank;
	private Rank nextRank;

	public XpProgressBar(Resources resources, float height, float width) {
		this.resources = resources;
		this.height = height;
		this.width = width;
		usersCurrentRank = ConfigResolver.getRankForXp(GameLoop.USER.xp);
		nextRank = ConfigResolver.getNextRank(GameLoop.USER.xp);

		createMainImage();
		createCoverImage();
		createXpNeededText();
		createNextLevelCount();
		createXpArrow();
		createUserWelcome();
	}

	private void createMainImage() {
		mainXp = new Image(resources.skin, Constants.UI.XP_BAR_MAIN);
		mainXp.setWidth(width * 0.8f);
		mainXp.setHeight(height);
		mainXp.setX(getX() + (width * 0.05f));
		mainXp.setY(getY());

		addActor(mainXp);
	}

	private void createCoverImage() {
		coverXp = new Image(resources.skin, Constants.UI.XP_BAR_COVER);

		if (reachedMaxRank()) {
			coverXp.setWidth(0);
		} else {
			float total = nextRank.startFrom - usersCurrentRank.startFrom;
			float userInRange = GameLoop.USER.xp - usersCurrentRank.startFrom;
			float percentage = userInRange / total;
			float percentageNotEarned = 100 - (percentage * 100);

			coverXp.setWidth((mainXp.getWidth() * percentageNotEarned) / 100f);
		}

		coverXp.setHeight(height);
		coverXp.setX(mainXp.getX() + (mainXp.getWidth() - coverXp.getWidth()));

		addActor(coverXp);
	}

	private void createXpNeededText() {
		if (reachedMaxRank()) {
			ShaderLabel neededText = new ShaderLabel(resources.fontShader, "Max Rank achieved!", resources.skin,
					Constants.UI.DEFAULT_FONT, Color.YELLOW);
			neededText.setX(mainXp.getX() + 10);
			addActor(neededText);
		} else {
			int xpNeeded = nextRank.startFrom - GameLoop.USER.xp;
			ShaderLabel xpNeededLabel = new ShaderLabel(resources.fontShader, xpNeeded + "xp", resources.skin,
					Constants.UI.DEFAULT_FONT, Color.YELLOW);
			xpNeededLabel.setX(mainXp.getX() + 10);
			addActor(xpNeededLabel);

			ShaderLabel neededText = new ShaderLabel(resources.fontShader, "Needed", resources.skin,
					Constants.UI.DEFAULT_FONT, Color.WHITE);
			neededText.setX(xpNeededLabel.getX() + (xpNeededLabel.getTextBounds().width * 1.05f));
			addActor(neededText);
		}
	}

	private void createNextLevelCount() {
		if (!reachedMaxRank()) {
			ShaderLabel nextLevel = new ShaderLabel(resources.fontShader, "" + nextRank.level, resources.skin,
					Constants.UI.DEFAULT_FONT, Color.WHITE);
			nextLevel.setX(mainXp.getX() + mainXp.getWidth() + 5);
			addActor(nextLevel);
		}
	}

	private void createXpArrow() {
		Image arrow = new Image(resources.skin, Constants.UI.XP_BAR_ARROW);
		arrow.setWidth(width * 0.03f);
		arrow.setHeight(height * 0.3f);
		arrow.setX(coverXp.getX() - (arrow.getWidth() * 0.5f));
		arrow.setY(getY() - arrow.getHeight());

		addActor(arrow);
	}

	private void createUserWelcome() {
		String welcome = GameLoop.USER.handle + "[" + usersCurrentRank.level + "]";
		ShaderLabel userWelcome = new ShaderLabel(resources.fontShader, welcome, resources.skin,
				Constants.UI.DEFAULT_FONT, Color.WHITE);
		userWelcome.setX(mainXp.getX() + 2);
		userWelcome.setY(mainXp.getHeight());
		addActor(userWelcome);

	}

	private boolean reachedMaxRank() {
		return nextRank == null;
	}
}
