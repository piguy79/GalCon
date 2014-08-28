package com.railwaygames.solarsmash.screen;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.model.Bounds;
import com.railwaygames.solarsmash.model.GameBoard;
import com.railwaygames.solarsmash.model.Player;
import com.railwaygames.solarsmash.model.Point;
import com.railwaygames.solarsmash.screen.event.TransitionEvent;
import com.railwaygames.solarsmash.screen.widget.ActionButton;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;

public class BoardScreenPlayerHud extends Group {

	private AtlasRegion bgTexture;
	private Resources resources;
	private GameBoard gameBoard;

	private ActionButton backButton;
	private Group playerHudBg;

	public BoardScreenPlayerHud(Resources resources, Bounds bounds, GameBoard gameBoard) {
		this.resources = resources;
		this.gameBoard = gameBoard;
		bounds.applyBounds(this);

		createTable();
		createLayout();
	}

	private void createLayout() {
		createBackButton();
		createPlayerHudBg();

		Player enemy = gameBoard.getEnemy();
		Player user = gameBoard.getUser();

		createUserLabels(user, enemy);
		showWhoNeedsToMove(user, enemy);
		createResourceBonusLabels(user, enemy);

		createOptionsButton();
	}

	private void createResourceBonusLabels(Player user, Player enemy) {
		Map<String, Double> playerAbilities = Abilities.aggregate(gameBoard, enemy);
		createResourceBonusLabels(playerAbilities, true);

		playerAbilities = Abilities.aggregate(gameBoard, user);
		createResourceBonusLabels(playerAbilities, false);
	}

	private void createResourceBonusLabels(Map<String, Double> playerAbilities, boolean invert) {
		float startStripe = 0.313f;
		float endStripe = 0.56f;

		float margin = playerHudBg.getWidth() * 0.01f;
		float height = playerHudBg.getHeight() * 0.25f - margin;
		float y = margin;
		float x = 0;

		if (invert) {
			x = playerHudBg.getWidth();
		}

		for (Map.Entry<String, Double> abilities : playerAbilities.entrySet()) {
			float bottomWidth = playerHudBg.getWidth()
					* ((endStripe - startStripe) * (y / playerHudBg.getHeight()) + startStripe);
			float topWidth = playerHudBg.getWidth()
					* ((endStripe - startStripe) * ((y + height) / playerHudBg.getHeight()) + startStripe);

			if (invert) {
				bottomWidth *= -1;
				topWidth *= -1;
			}

			Actor abilityActor = new AbilityBox(invert ? margin * -1 : margin, x, invert ? playerHudBg.getHeight() - y
					: y, invert ? height * -1 : height, bottomWidth, topWidth);
			playerHudBg.addActor(abilityActor);

			ShaderLabel label = new ShaderLabel(resources.fontShader, "+"
					+ Integer.toString((int) Math.floor(abilities.getValue() * 100)) + "%" + abilities.getKey(),
					resources.skin, Constants.UI.X_SMALL_FONT, Color.WHITE);
			if (invert) {
				label.setBounds(x + bottomWidth, playerHudBg.getHeight() - y - height + margin, bottomWidth * -1,
						height);
				label.setAlignment(Align.left, Align.left);
			} else {
				label.setBounds(x, y + margin, bottomWidth, height);
				label.setAlignment(Align.right, Align.right);
			}

			playerHudBg.addActor(label);

			y += (height + margin);
		}
	}

	private static class AbilityBox extends Actor {
		private float y;
		private float x;
		private float margin;
		private float height;
		private float bottomWidth;
		private float topWidth;

		public AbilityBox(float margin, float x, float y, float height, float bottomWidth, float topWidth) {
			this.y = y;
			this.x = x;
			this.margin = margin;
			this.height = height;
			this.topWidth = topWidth;
			this.bottomWidth = bottomWidth;
		}

		private ShapeRenderer renderer = new ShapeRenderer();

		@Override
		public void draw(Batch batch, float parentAlpha) {
			batch.end();

			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			renderer.setProjectionMatrix(batch.getProjectionMatrix());
			renderer.setTransformMatrix(batch.getTransformMatrix());
			renderer.translate(getX(), getY(), 0);

			Color alphaWhite = new Color(1.0f, 1.0f, 1.0f, 0.3f);

			renderer.setColor(alphaWhite);
			renderer.begin(ShapeType.Filled);
			renderer.rect(x + margin, y, bottomWidth, height, Color.CLEAR, alphaWhite, alphaWhite, Color.CLEAR);
			renderer.triangle(x + margin + bottomWidth, y, x + margin + bottomWidth, y + height, x + margin + topWidth,
					y + height);
			renderer.end();
			Gdx.gl.glDisable(GL20.GL_BLEND);

			batch.begin();
		}
	}

	private void showWhoNeedsToMove(Player user, Player enemy) {
		float shipHeight = playerHudBg.getWidth() * 0.08f;

		if (!user.hasMoved(gameBoard)) {
			Image ship = new Image(resources.skin, "shipImage");
			ship.setColor(Constants.Colors.USER_SHIP_FILL);
			ship.setX(playerHudBg.getWidth() * 0.53f);
			ship.setY(playerHudBg.getHeight() * 0.7f);

			Vector2 vec = Scaling.fillY.apply(ship.getWidth(), ship.getHeight(), 1, shipHeight);
			ship.setSize(vec.x, vec.y);

			ship.setOrigin(ship.getWidth() * 0.5f, ship.getHeight() * 0.5f);
			ship.rotateBy(180);

			addWhoNeedsToMoveAction(ship);
			playerHudBg.addActor(ship);
		}

		if (!enemy.hasMoved(gameBoard)) {
			Image ship = new Image(resources.skin, "shipImage");
			ship.setColor(Constants.Colors.ENEMY_SHIP_FILL);
			ship.setX(playerHudBg.getWidth() * 0.37f);
			ship.setY(playerHudBg.getHeight() * 0.0f);

			Vector2 vec = Scaling.fillY.apply(ship.getWidth(), ship.getHeight(), 1, shipHeight);
			ship.setSize(vec.x, vec.y);

			addWhoNeedsToMoveAction(ship);
			playerHudBg.addActor(ship);
		}
	}

	private void addWhoNeedsToMoveAction(Image ship) {
		Color originalColor = ship.getColor();
		ship.addAction(forever(sequence(delay(1.0f), color(Color.WHITE, 0.8f), color(originalColor, 0.8f), delay(2.0f))));
	}

	private void createUserLabels(Player user, Player enemy) {
		float margin = playerHudBg.getWidth() * 0.01f;

		ShaderLabel userLabel = new ShaderLabel(resources.fontShader, user.handle, resources.skin,
				Constants.UI.X_SMALL_FONT, Color.WHITE);
		userLabel.setColor(new Color(0.8f, 1.0f, 0.8f, 1.0f));
		userLabel.setWidth(playerHudBg.getWidth() * 0.66f);
		userLabel.setY(playerHudBg.getHeight() - userLabel.getTextBounds().height - playerHudBg.getHeight() * 0.07f);
		userLabel.setAlignment(Align.left);
		userLabel.setX(margin);
		playerHudBg.addActor(userLabel);

		ShaderLabel userRegenLabel = new ShaderLabel(resources.fontShader, "" + gameBoard.ownedPlanetRegen(user),
				resources.skin, Constants.UI.LARGE_FONT, Color.WHITE);
		userRegenLabel.setColor(new Color(0.4f, 1.0f, 0.4f, 0.4f));
		userRegenLabel.setWidth(playerHudBg.getWidth() * 0.5f);
		userRegenLabel.setY(playerHudBg.getHeight() * 0.5f - userRegenLabel.getTextBounds().height * 0.8f);
		userRegenLabel.setAlignment(Align.left);
		userRegenLabel.setX(margin);
		playerHudBg.addActor(userRegenLabel);

		ShaderLabel vs = new ShaderLabel(resources.fontShader, "vs", resources.skin, Constants.UI.X_SMALL_FONT,
				Color.WHITE);
		vs.setWidth(playerHudBg.getWidth());
		vs.setColor(new Color(1.0f, 1.0f, 1.0f, 0.7f));
		vs.setY(playerHudBg.getHeight() * 0.5f - vs.getTextBounds().height * 0.6f);
		vs.setAlignment(Align.center);
		playerHudBg.addActor(vs);

		ShaderLabel enemyLabel = new ShaderLabel(resources.fontShader, enemy.handle, resources.skin,
				Constants.UI.X_SMALL_FONT, Color.WHITE);
		enemyLabel.setColor(new Color(1.0f, 0.8f, 0.8f, 1.0f));
		enemyLabel.setWidth(playerHudBg.getWidth() * 0.66f);
		enemyLabel.setY(0);
		enemyLabel.setAlignment(Align.right);
		enemyLabel.setX(playerHudBg.getWidth() - enemyLabel.getWidth() - margin);
		playerHudBg.addActor(enemyLabel);

		ShaderLabel enemyRegenLabel = new ShaderLabel(resources.fontShader, "" + gameBoard.ownedPlanetRegen(enemy),
				resources.skin, Constants.UI.LARGE_FONT, Color.WHITE);
		enemyRegenLabel.setColor(new Color(1.0f, 0.4f, 0.4f, 0.4f));
		enemyRegenLabel.setWidth(playerHudBg.getWidth() * 0.5f);
		enemyRegenLabel.setY(playerHudBg.getHeight() * 0.5f - enemyRegenLabel.getTextBounds().height * 0.8f);
		enemyRegenLabel.setAlignment(Align.right);
		enemyRegenLabel.setX(playerHudBg.getWidth() * 0.5f);
		playerHudBg.addActor(enemyRegenLabel);
	}

	private void createPlayerHudBg() {
		playerHudBg = new Group();
		playerHudBg.setWidth(getWidth() * 0.63f);
		playerHudBg.setHeight(getHeight());
		playerHudBg.setX(getWidth() * 0.5f - playerHudBg.getWidth() * 0.5f);
		addActor(playerHudBg);

		TextureRegionDrawable drawable = new TextureRegionDrawable(resources.gameBoardAtlas.findRegion("player_hud_bg"));
		Image bg = new Image(drawable);
		bg.setWidth(playerHudBg.getWidth());
		bg.setHeight(playerHudBg.getHeight());

		playerHudBg.addActor(bg);
	}

	private void createBackButton() {
		backButton = new ActionButton(resources.skin, "backButton", new Point(10, getHeight() * 0.1f));
		backButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent clickEvent, float x, float y) {
				TransitionEvent event = new TransitionEvent(Action.BACK);
				fire(event);
			}
		});
		addActor(backButton);
	}

	private void createOptionsButton() {
		ImageButton button = new ImageButton(resources.skin, "optionsButton") {
			@Override
			public void layout() {
				super.layout();
			}

			@Override
			public void draw(Batch batch, float parentAlpha) {
				super.draw(batch, parentAlpha);
			}
		};

		float height = getHeight() * 0.6f;
		float width = height;
		button.getImage().setScaling(Scaling.fillY);
		button.setBounds(getWidth() - width - 0.045f * getWidth(), getHeight() * 0.2f, width, height);
		button.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				TransitionEvent transitionEvent = new TransitionEvent(Action.OPTIONS);
				fire(transitionEvent);
			}
		});
		addActor(button);
	}

	private void createTable() {
		bgTexture = resources.gameBoardAtlas.findRegion("player_hud");
		Image backGround = new Image(new TextureRegionDrawable(bgTexture));
		backGround.setWidth(getWidth());
		backGround.setHeight(getHeight());
		addActor(backGround);
	}

	private static class Abilities {
		private static final Map<String, String> ABILITY_TO_ABBREVIATION = new HashMap<String, String>() {
			{
				put(Constants.ABILITY_ATTACK_INCREASE, "A");
				put(Constants.ABILITY_DEFENCE_INCREASE, "D");
				put(Constants.ABILITY_SPEED, "S");
				put(Constants.ABILITY_REGEN_BLOCK, "B");
			}
		};

		private static Map<String, Double> bonuses = new HashMap<String, Double>();

		public static Map<String, Double> aggregate(GameBoard gameBoard, Player player) {
			List<String> abilites = gameBoard.ownedPlanetAbilities(player);

			bonuses.clear();
			if (abilites.isEmpty()) {
				return bonuses;
			}

			for (int i = 0; i < abilites.size(); ++i) {
				String ability = abilites.get(i);
				String abbrev = ABILITY_TO_ABBREVIATION.get(ability);
				if (abbrev == null) {
					throw new IllegalArgumentException("BoardScreenPlayerHud does not understand: " + abilites.get(i));
				}
				Double configBonus = Double.parseDouble(gameBoard.gameConfig.getValue(ability));
				Double bonus = 0d;
				if (bonuses.containsKey(abbrev)) {
					bonus = bonuses.get(abbrev);
				}
				bonus += configBonus;
				bonuses.put(abbrev, bonus);
			}

			abilites = gameBoard.ownedPlanetsUnderHarvest(player);

			for (int i = 0; i < abilites.size(); ++i) {
				String ability = abilites.get(i);
				String abbrev = ABILITY_TO_ABBREVIATION.get(ability);
				if (abbrev == null) {
					throw new IllegalArgumentException("BoardScreenPlayerHud does not understand: " + abilites.get(i));
				}
				Double configBonus = Double.parseDouble(gameBoard.gameConfig.getValue("harvestEnhancement"));
				Double bonus = 0d;
				if (bonuses.containsKey(abbrev)) {
					bonus = bonuses.get(abbrev);
				}
				bonus += configBonus;
				bonuses.put(abbrev, bonus);
			}

			return bonuses;
		}
	}
}
