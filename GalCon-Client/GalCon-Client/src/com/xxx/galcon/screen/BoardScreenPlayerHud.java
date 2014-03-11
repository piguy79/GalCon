package com.xxx.galcon.screen;

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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.model.Bounds;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.screen.event.TransitionEvent;
import com.xxx.galcon.screen.widget.ActionButton;
import com.xxx.galcon.screen.widget.ShaderLabel;

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

		Player enemy = getEnemy(gameBoard);
		Player user = getUser(gameBoard);

		createUserLabels(enemy, user);
		showWhoNeedsToMove(enemy, user);
		createResourceBonusLabels(enemy, user);

		createOptionsButton();
	}

	private void createResourceBonusLabels(Player enemy, Player user) {
		Map<String, Integer> playerAbilities = Abilities.aggregate(gameBoard, enemy);

		playerAbilities.put("A", 25);
		playerAbilities.put("D", 50);
		playerAbilities.put("B", 50);

		createResourceBonusLabels(playerAbilities, false);

		playerAbilities = Abilities.aggregate(gameBoard, user);
		createResourceBonusLabels(playerAbilities, true);
	}

	private void createResourceBonusLabels(Map<String, Integer> playerAbilities, boolean invert) {
		float startStripe = 0.313f;
		float endStripe = 0.56f;

		float margin = playerHudBg.getWidth() * 0.01f;
		float height = playerHudBg.getHeight() * 0.25f - margin;
		float y = margin;
		float x = 0;

		if (invert) {
			x = playerHudBg.getWidth();
		}

		for (Map.Entry<String, Integer> abilities : playerAbilities.entrySet()) {
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

			ShaderLabel label = new ShaderLabel(resources.fontShader, "+" + abilities.getValue() + "%"
					+ abilities.getKey(), resources.skin, Constants.UI.X_SMALL_FONT);
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
		public void draw(SpriteBatch batch, float parentAlpha) {
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

	private void showWhoNeedsToMove(Player enemy, Player user) {
		float shipHeight = playerHudBg.getWidth() * 0.08f;

		if (!enemy.hasMoved(gameBoard)) {
			Image ship = new Image(resources.skin, "shipImage");
			ship.setColor(Constants.Colors.ENEMY_SHIP_FILL);
			ship.setX(playerHudBg.getWidth() * 0.53f);
			ship.setY(playerHudBg.getHeight() * 0.7f);

			Vector2 vec = Scaling.fillY.apply(ship.getWidth(), ship.getHeight(), 1, shipHeight);
			ship.setSize(vec.x, vec.y);

			ship.setOrigin(ship.getWidth() * 0.5f, ship.getHeight() * 0.5f);
			ship.rotate(180);

			addWhoNeedsToMoveAction(ship);
			playerHudBg.addActor(ship);
		}

		if (!user.hasMoved(gameBoard)) {
			Image ship = new Image(resources.skin, "shipImage");
			ship.setColor(Constants.Colors.USER_SHIP_FILL);
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

	private void createUserLabels(Player enemy, Player user) {
		float margin = playerHudBg.getWidth() * 0.01f;

		ShaderLabel enemyLabel = new ShaderLabel(resources.fontShader, enemy.handle, resources.skin,
				Constants.UI.X_SMALL_FONT);
		enemyLabel.setColor(new Color(1.0f, 0.8f, 0.8f, 1.0f));
		enemyLabel.setWidth(playerHudBg.getWidth() * 0.66f);
		enemyLabel.setY(playerHudBg.getHeight() - enemyLabel.getTextBounds().height - playerHudBg.getHeight() * 0.07f);
		enemyLabel.setAlignment(Align.left);
		enemyLabel.setX(margin);
		playerHudBg.addActor(enemyLabel);

		if (enemy.rank != null) {
			ShaderLabel enemyRank = new ShaderLabel(resources.fontShader, "" + enemy.rank.level, resources.skin,
					Constants.UI.LARGE_FONT);
			enemyRank.setColor(new Color(1.0f, 0.4f, 0.4f, 0.4f));
			enemyRank.setWidth(playerHudBg.getWidth() * 0.5f);
			enemyRank.setY(playerHudBg.getHeight() * 0.5f - enemyRank.getTextBounds().height * 0.8f);
			enemyRank.setAlignment(Align.left);
			enemyRank.setX(margin);
			playerHudBg.addActor(enemyRank);
		}

		ShaderLabel vs = new ShaderLabel(resources.fontShader, "vs", resources.skin, Constants.UI.X_SMALL_FONT);
		vs.setWidth(playerHudBg.getWidth());
		vs.setColor(new Color(1.0f, 1.0f, 1.0f, 0.7f));
		vs.setY(playerHudBg.getHeight() * 0.5f - vs.getTextBounds().height * 0.6f);
		vs.setAlignment(Align.center);
		playerHudBg.addActor(vs);

		ShaderLabel userLabel = new ShaderLabel(resources.fontShader, user.handle, resources.skin,
				Constants.UI.X_SMALL_FONT);
		userLabel.setColor(new Color(0.8f, 1.0f, 0.8f, 1.0f));
		userLabel.setWidth(playerHudBg.getWidth() * 0.66f);
		userLabel.setY(0);
		userLabel.setAlignment(Align.right);
		userLabel.setX(playerHudBg.getWidth() - userLabel.getWidth() - margin);
		playerHudBg.addActor(userLabel);

		ShaderLabel userRank = new ShaderLabel(resources.fontShader, "" + user.rank.level, resources.skin,
				Constants.UI.LARGE_FONT);
		userRank.setColor(new Color(0.4f, 1.0f, 0.4f, 0.4f));
		userRank.setWidth(playerHudBg.getWidth() * 0.5f);
		userRank.setY(playerHudBg.getHeight() * 0.5f - userRank.getTextBounds().height * 0.8f);
		userRank.setAlignment(Align.right);
		userRank.setX(playerHudBg.getWidth() * 0.5f);
		playerHudBg.addActor(userRank);
	}

	private Player getEnemy(GameBoard gameBoard) {
		List<Player> players = gameBoard.players;
		if (players.size() < 2) {
			Player waitingForOpponent = new Player();
			waitingForOpponent.rank = null;
			waitingForOpponent.handle = BoardScreen.Labels.waitingLabel(gameBoard.social);
			return waitingForOpponent;
		}

		if (players.get(0).handle.equals(GameLoop.USER.handle)) {
			return players.get(1);
		}

		return players.get(0);
	}

	private Player getUser(GameBoard gameBoard) {
		List<Player> players = gameBoard.players;
		if (players.get(0).handle.equals(GameLoop.USER.handle)) {
			return players.get(0);
		}

		return players.get(1);
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
			public void draw(SpriteBatch batch, float parentAlpha) {
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

		private static Map<String, Integer> bonuses = new HashMap<String, Integer>();

		public static Map<String, Integer> aggregate(GameBoard gameBoard, Player player) {
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
				Integer configBonus = Integer.valueOf(gameBoard.gameConfig.getValue(ability));
				Integer bonus = 0;
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
