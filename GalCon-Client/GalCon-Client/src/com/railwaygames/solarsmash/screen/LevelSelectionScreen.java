package com.railwaygames.solarsmash.screen;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.tablelayout.Cell;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.Fonts;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.PartialScreenFeedback;
import com.railwaygames.solarsmash.UIConnectionWrapper;
import com.railwaygames.solarsmash.config.ConfigResolver;
import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.model.Map;
import com.railwaygames.solarsmash.model.Maps;
import com.railwaygames.solarsmash.model.Point;
import com.railwaygames.solarsmash.screen.event.GameStartListener;
import com.railwaygames.solarsmash.screen.overlay.DismissableOverlay;
import com.railwaygames.solarsmash.screen.overlay.Overlay;
import com.railwaygames.solarsmash.screen.overlay.TextOverlay;
import com.railwaygames.solarsmash.screen.widget.ActorBar.Align;
import com.railwaygames.solarsmash.screen.widget.ScrollPaneHighlightReel;
import com.railwaygames.solarsmash.screen.widget.ScrollPaneHighlightReel.ScrollPaneHighlightReelBuilder;
import com.railwaygames.solarsmash.screen.widget.WaitImageButton;

public class LevelSelectionScreen implements PartialScreenFeedback, UIConnectionResultCallback<Maps> {

	private List<Map> allMaps;

	private AtlasRegion levelSelectionCard;
	private AtlasRegion levelSelectCardShadow;

	private Object returnValue;

	private Stage stage;
	private Table cardTable;
	private Actor choiceActor;
	private Button backButton;
	protected WaitImageButton waitImage;
	private ScrollPaneHighlightReel highlightReel;

	private Array<Actor> actors = new Array<Actor>();
	private Resources resources;

	public LevelSelectionScreen(Resources resources) {
		this.resources = resources;
	}

	@Override
	public void hide() {

	}

	private void startHideSequence(final String retVal) {
		waitImage.stop();
		GraphicsUtils.hideAnimated(actors, retVal.equals(Action.BACK), new Runnable() {
			@Override
			public void run() {
				returnValue = retVal;
			}
		});
	}

	@Override
	public Object getRenderResult() {
		return returnValue;
	}

	@Override
	public void resetState() {
		returnValue = null;
	}

	@Override
	public void onConnectionResult(Maps result) {
		waitImage.stop();
		this.allMaps = result.allMaps;
		Collections.sort(this.allMaps, new Comparator<Map>() {
			@Override
			public int compare(Map o1, Map o2) {
				return Integer.valueOf(o1.availableFromXp).compareTo(Integer.valueOf(o2.availableFromXp));
			}
		});

		createGameList();
	}

	private void createScrollhighlightReel() {
		float actorWidth = Gdx.graphics.getWidth() * 0.04f;
		float actorPadding = calculateActorPadding(actorWidth);

		ScrollPaneHighlightReelBuilder builder = new ScrollPaneHighlightReel.ScrollPaneHighlightReelBuilder(
				Gdx.graphics.getHeight() * 0.1f, Gdx.graphics.getWidth() * 0.4f).align(Align.LEFT)
				.actorSize(Gdx.graphics.getHeight() * 0.02f, actorWidth).actorPadding(actorPadding);

		float totalWidth = 0f;

		for (Map map : allMaps) {
			Image image = new Image(resources.skin.getDrawable(Constants.UI.SCROLL_HIGHLIGHT));
			image.setColor(Color.GRAY);
			builder.addActorWithKey(map.key, image);
			totalWidth = totalWidth + actorPadding + actorWidth;
		}

		// Account for the first one.
		totalWidth = totalWidth - actorPadding;

		highlightReel = builder.build();

		highlightReel.setX((Gdx.graphics.getWidth() - totalWidth) / 2);
		highlightReel.setY(Gdx.graphics.getHeight() * 0.05f);

		highlightReel.highlight(allMaps.get(0).key);
		actors.add(highlightReel);
		stage.addActor(highlightReel);

	}

	private void createGameList() {
		final Table table = new Table();
		final ScrollPane scrollPane = new ScrollPane(table);
		scrollPane.setScrollingDisabled(false, true);
		scrollPane.setFadeScrollBars(false);

		cardTable.add(scrollPane);

		float cardWidth = Gdx.graphics.getWidth() * .7f;
		float padSide = (Gdx.graphics.getWidth() - cardWidth) * 0.5f;

		table.pad(10).padLeft(padSide).padRight(padSide).defaults().expandX().space(30).width(cardWidth)
				.height(Gdx.graphics.getHeight() * .7f);

		for (int i = 0; i < allMaps.size(); ++i) {
			final Map map = allMaps.get(i);

			CardActor card = new CardActor(map, resources);
			if (card.mapAvailable) {
				card.addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						createGameStartDialog(map.key);
					}
				});
			}

			table.add(card).expandX().fillX();
		}

		choiceActor = new Actor() {
			public void draw(SpriteBatch batch, float parentAlpha) {
				float scrollX = scrollPane.getScrollX();
				for (int i = 0; i < table.getCells().size(); ++i) {
					Cell<CardActor> cell = table.getCells().get(i);
					float adjustX = cell.getWidgetX() - scrollX;

					float rightXBound = Math.min(getWidth(), adjustX + cell.getWidgetWidth());
					float leftXBound = Math.max(0, adjustX);

					if (rightXBound - leftXBound > getWidth() * .45f) {
						highlightReel.highlight(cell.getWidget().getMapKey());
					}
				}

			};
		};
		choiceActor.setWidth(Gdx.graphics.getWidth());
		choiceActor.setHeight(Gdx.graphics.getHeight());
		actors.add(choiceActor);
		stage.addActor(choiceActor);

		actors.add(cardTable);
		stage.addActor(cardTable);

		backButton.remove();
		stage.addActor(backButton);

		createScrollhighlightReel();
	}

	private float calculateActorPadding(float actorWidth) {
		float initialPadding = Gdx.graphics.getWidth() * 0.1f;
		float totalSize = 0f;
		for (Map map : allMaps) {
			totalSize = totalSize + initialPadding + actorWidth;
		}

		if (totalSize > (Gdx.graphics.getWidth() * 0.9f)) {
			return initialPadding * 0.6f;
		}
		return initialPadding;
	}

	private void createGameStartDialog(int selectedMapKey) {
		final GameStartDialog dialog = new GameStartDialog(resources, Gdx.graphics.getWidth() * 0.8f,
				Gdx.graphics.getHeight() * 0.32f, stage, selectedMapKey);
		float dialogY = Gdx.graphics.getHeight() * 0.45f;

		stage.addActor(dialog);
		dialog.setX(-dialog.getWidth());
		dialog.setY(dialogY);
		Point startPoint = new Point(Gdx.graphics.getWidth() * 0.1f, dialogY);
		dialog.show(startPoint);

		dialog.addListener(new GameStartListener() {

			@Override
			public void startGame(int selectedMapKey) {
				dialog.hide();
				startHideSequence(Action.PLAY + ":" + selectedMapKey);
			}

			@Override
			public void startSocialGame(int selectedMapKey) {
				dialog.hide();
				startHideSequence(Action.PLAY_WITH_FRIENDS + ":" + selectedMapKey);
			}
		});
	}

	@Override
	public void onConnectionError(String msg) {
		waitImage.stop();

		final Overlay ovrlay = new DismissableOverlay(resources, new TextOverlay(msg, resources), new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				UIConnectionWrapper.findAllMaps(LevelSelectionScreen.this);
			}
		});

		stage.addActor(ovrlay);
	}

	private class CardActor extends Actor {

		private Map map;
		private TextureRegion mapTex;
		public boolean mapAvailable;

		public CardActor(Map map, Resources resources) {
			this.map = map;
			if (GameLoop.USER.xp >= map.availableFromXp) {
				mapAvailable = true;
				this.mapTex = resources.levelAtlas.findRegion("" + map.key);
			} else {
				mapAvailable = false;
				this.mapTex = resources.levelSelectionAtlas.findRegion("lock_card_background");
			}
		}

		public String getMapTitle() {
			if (mapAvailable) {
				return map.title;
			}
			return "";
		}

		public String getMapDescription() {
			if (mapAvailable) {
				return map.description;
			}
			return "Reach level " + ConfigResolver.getRankForXp(map.availableFromXp).level + " to unlock.";
		}

		public int getMapKey() {
			return map.key;
		}

		@Override
		public void draw(SpriteBatch batch, float parentAlpha) {
			int height = (int) getHeight();
			int width = (int) getWidth();

			int x = (int) getX();
			int y = (int) getY();

			batch.draw(levelSelectCardShadow, x, y, width, height);

			int xOffset = (int) (width * 0.1f);
			int yOffset = (int) (height * 0.1f);
			x += xOffset;
			y += yOffset;
			width -= xOffset;
			height -= yOffset;

			BitmapFont smallFont = Fonts.getInstance(resources.assetManager).xSmallFont();
			BitmapFont mediumFont = Fonts.getInstance(resources.assetManager).mediumFont();

			batch.draw(levelSelectionCard, x, y, width, height);

			int mapHeight = (int) (getHeight() * .55f);
			int mapWidth = (int) (getWidth() * .9f);
			batch.draw(mapTex, x + width / 2 - mapWidth / 2, y + height / 2 - mapHeight / 2, mapWidth, mapHeight);

			String text = getMapTitle();
			mediumFont.setColor(Color.BLACK);
			float halfFontWidth = mediumFont.getBounds(text).width / 2;
			batch.setShader(resources.fontShader);
			mediumFont.draw(batch, text, x + width / 2 - halfFontWidth, y + height * .88f);

			text = getMapDescription();
			smallFont.setColor(Color.BLACK);
			halfFontWidth = smallFont.getBounds(text).width / 2;
			smallFont.draw(batch, text, x + width / 2 - halfFontWidth, y + height * .15f);
			batch.setShader(null);
		}
	}

	@Override
	public void show(Stage stage, float width, float height) {
		this.stage = stage;
		this.actors.clear();

		waitImage = new WaitImageButton(resources.skin);
		float buttonWidth = .25f * (float) width;
		waitImage.setWidth(buttonWidth);
		waitImage.setHeight(buttonWidth);
		waitImage.setX(width / 2 - buttonWidth / 2);
		waitImage.setY(height / 2 - buttonWidth / 2);
		stage.addActor(waitImage);

		int tableHeight = (int) (height * .7f);

		this.levelSelectionCard = resources.levelSelectionAtlas.findRegion("level_card_gray");
		this.levelSelectCardShadow = resources.levelSelectionAtlas.findRegion("level_select_card_shadow");

		cardTable = new Table();
		cardTable.setX(0);
		cardTable.setY(height * .4f - tableHeight * .42f);
		cardTable.setWidth(width);
		cardTable.setHeight(tableHeight);

		backButton = new Button(resources.skin, "backButton");
		GraphicsUtils.setCommonButtonSize(backButton);
		backButton.setX(10);
		backButton.setY(height - backButton.getHeight() - 5);
		backButton.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				startHideSequence(Action.BACK);
			}
		});

		Actor headerTextActor = new Actor() {
			public void draw(SpriteBatch batch, float parentAlpha) {
				BitmapFont font = Fonts.getInstance(resources.assetManager).mediumFont();
				font.setColor(Color.WHITE);
				String text = "Choose Your Galaxy";
				float halfFontWidth = font.getBounds(text).width / 2;
				batch.setShader(resources.fontShader);
				font.draw(batch, text, (getX() + getWidth() / 2 - halfFontWidth), getHeight() * .97f);
				batch.setShader(null);
				font.setColor(Color.WHITE);
			};
		};
		headerTextActor.setWidth(width);
		headerTextActor.setHeight(height);

		actors.add(headerTextActor);
		stage.addActor(headerTextActor);
		actors.add(backButton);
		stage.addActor(backButton);

		if (allMaps == null) {
			waitImage.start();
			UIConnectionWrapper.findAllMaps(this);
		} else {
			Maps maps = new Maps();
			maps.allMaps = allMaps;
			onConnectionResult(maps);
		}
	}

	@Override
	public void render(float delta) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hideTitleArea() {
		return true;
	}
}
