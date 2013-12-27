package com.xxx.galcon.screen;

import static com.xxx.galcon.Util.createShader;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.tablelayout.Cell;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.PartialScreenFeedback;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.Map;
import com.xxx.galcon.model.Maps;
import com.xxx.galcon.screen.hud.HeaderHud;
import com.xxx.galcon.screen.overlay.DismissableOverlay;
import com.xxx.galcon.screen.overlay.Overlay;
import com.xxx.galcon.screen.overlay.TextOverlay;
import com.xxx.galcon.screen.widget.WaitImageButton;

public class LevelSelectionScreen implements PartialScreenFeedback, UIConnectionResultCallback<Maps> {

	private List<Map> allMaps;
	private int selectedMapKey = 0;

	private AssetManager assetManager;
	private ShaderProgram fontShader;

	private AtlasRegion levelSelectBgBottom;
	private AtlasRegion levelSelectionCard;
	private AtlasRegion levelSelectCardShadow;

	private Object returnValue;

	private Skin skin;
	private Stage stage;
	private Table cardTable;
	private Actor choiceActor;
	private ImageButton backButton;
	protected WaitImageButton waitImage;

	private Array<Actor> actors = new Array<Actor>();

	private TextureAtlas levelSelectionAtlas;
	private TextureAtlas levelsAtlas;
	private TextureAtlas menusAtlas;

	public LevelSelectionScreen(Skin skin, AssetManager assetManager) {
		this.assetManager = assetManager;
		this.skin = skin;

		fontShader = createShader("data/shaders/font-vs.glsl", "data/shaders/font-fs.glsl");

		levelSelectionAtlas = assetManager.get("data/images/levelSelection.atlas", TextureAtlas.class);
		levelsAtlas = assetManager.get("data/images/levels.atlas", TextureAtlas.class);
		menusAtlas = assetManager.get("data/images/menus.atlas", TextureAtlas.class);
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
				return Integer.valueOf(o1.availableFromLevel).compareTo(Integer.valueOf(o2.availableFromLevel));
			}
		});

		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();

		final Table table = new Table();
		final ScrollPane scrollPane = new ScrollPane(table);
		scrollPane.setScrollingDisabled(false, true);
		scrollPane.setFadeScrollBars(false);

		cardTable.add(scrollPane);

		float cardWidth = Gdx.graphics.getWidth() * .7f;
		float padSide = (Gdx.graphics.getWidth() - cardWidth) * 0.5f;

		table.pad(10).padLeft(padSide).padRight(padSide).defaults().expandX().space(30).width(cardWidth)
				.height(Gdx.graphics.getHeight() * .6f);

		for (int i = 0; i < allMaps.size(); ++i) {
			final Map map = allMaps.get(i);
			table.add(new CardActor(map, assetManager)).expandX().fillX();
		}

		choiceActor = new Actor() {
			public void draw(SpriteBatch batch, float parentAlpha) {
				String text = "";
				float scrollX = scrollPane.getScrollX();
				for (int i = 0; i < table.getCells().size(); ++i) {
					Cell<CardActor> cell = table.getCells().get(i);
					float adjustX = cell.getWidgetX() - scrollX;

					float rightXBound = Math.min(getWidth(), adjustX + cell.getWidgetWidth());
					float leftXBound = Math.max(0, adjustX);

					if (rightXBound - leftXBound > getWidth() * .45f) {
						text = cell.getWidget().getMapTitle();
						selectedMapKey = cell.getWidget().getMapKey();
					}
				}

				BitmapFont font = Fonts.getInstance(assetManager).mediumFont();
				float halfFontWidth = font.getBounds(text).width / 2;
				batch.setShader(fontShader);
				font.draw(batch, text, (getX() + getWidth() / 2 - halfFontWidth), getHeight() * .92f);
				batch.setShader(null);
			};
		};
		choiceActor.setWidth(Gdx.graphics.getWidth());
		choiceActor.setHeight(Gdx.graphics.getHeight());
		actors.add(choiceActor);
		stage.addActor(choiceActor);

		actors.add(cardTable);
		stage.addActor(cardTable);

		Actor bottomBar = new Actor() {
			@Override
			public void draw(SpriteBatch batch, float parentAlpha) {
				batch.draw(levelSelectBgBottom, getX(), getY(), getWidth(), getHeight());
			}
		};
		bottomBar.setX(0);
		bottomBar.setY(0);
		bottomBar.setWidth(width);
		bottomBar.setHeight(height * 0.18f);
		actors.add(bottomBar);
		stage.addActor(bottomBar);

		ImageButton randomPlayButton = new ImageButton(skin, "regularPlay");
		int buttonWidth = (int) (height * 0.15f);
		int buttonHeight = (int) (height * 0.15f);
		int margin = (int) (width * 0.1f);
		int ymargin = (int) (height * 0.02f);

		randomPlayButton.setX(margin);
		randomPlayButton.setY(ymargin);
		randomPlayButton.setWidth(buttonWidth);
		randomPlayButton.setHeight(buttonHeight);
		randomPlayButton.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				startHideSequence(Action.PLAY + ":" + selectedMapKey);
			}
		});
		actors.add(randomPlayButton);
		stage.addActor(randomPlayButton);

		ImageButton friendsPlayButton = new ImageButton(skin, "socialPlay");
		friendsPlayButton.setX(width - margin - buttonWidth);
		friendsPlayButton.setY(ymargin);
		friendsPlayButton.setWidth(buttonWidth);
		friendsPlayButton.setHeight(buttonHeight);
		friendsPlayButton.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				startHideSequence(Action.PLAY_WITH_FRIENDS + ":" + selectedMapKey);
			}
		});
		actors.add(friendsPlayButton);
		stage.addActor(friendsPlayButton);

		backButton.remove();
		stage.addActor(backButton);
	}

	@Override
	public void onConnectionError(String msg) {
		waitImage.stop();

		final Overlay ovrlay = new DismissableOverlay(menusAtlas, new TextOverlay(msg, menusAtlas, skin, fontShader),
				new ClickListener() {
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

		public CardActor(Map map, AssetManager assetManager) {
			this.map = map;
			this.mapTex = levelsAtlas.findRegion("" + map.key);
		}

		public String getMapTitle() {
			return map.title;
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

			BitmapFont smallFont = Fonts.getInstance(assetManager).smallFont();
			BitmapFont mediumFont = Fonts.getInstance(assetManager).mediumFont();

			batch.draw(levelSelectionCard, x, y, width, height);

			int mapHeight = (int) (getHeight() * .55f);
			int mapWidth = (int) (getWidth() * .9f);
			batch.draw(mapTex, x + width / 2 - mapWidth / 2, y + height / 2 - mapHeight / 2, mapWidth, mapHeight);

			String text = map.title;
			mediumFont.setColor(Color.WHITE);
			float halfFontWidth = mediumFont.getBounds(text).width / 2;
			batch.setShader(fontShader);
			mediumFont.draw(batch, text, x + width / 2 - halfFontWidth, y + height * .85f);

			text = map.description;
			smallFont.setColor(Color.WHITE);
			halfFontWidth = smallFont.getBounds(text).width / 2;
			smallFont.draw(batch, text, x + width / 2 - halfFontWidth, y + height * .18f);
			batch.setShader(null);
		}
	}

	@Override
	public void show(Stage stage, float width, float height) {
		this.stage = stage;
		this.actors.clear();

		waitImage = new WaitImageButton(skin);
		float buttonWidth = .25f * (float) width;
		waitImage.setWidth(buttonWidth);
		waitImage.setHeight(buttonWidth);
		waitImage.setX(width / 2 - buttonWidth / 2);
		waitImage.setY(height / 2 - buttonWidth / 2);
		stage.addActor(waitImage);

		int tableHeight = (int) (height * .7f);
		int buttonHeight = (int) (Gdx.graphics.getHeight() * (HeaderHud.HEADER_HEIGHT_RATIO * 0.88f));

		this.levelSelectBgBottom = levelSelectionAtlas.findRegion("level_select_bg_bottom");
		this.levelSelectionCard = levelSelectionAtlas.findRegion("level_card_black");
		this.levelSelectCardShadow = levelSelectionAtlas.findRegion("level_select_card_shadow");

		cardTable = new Table();
		cardTable.setX(0);
		cardTable.setY(height * .5f - tableHeight * .42f);
		cardTable.setWidth(width);
		cardTable.setHeight(tableHeight);

		backButton = new ImageButton(skin, "backButton");
		backButton.setX(10);
		backButton.setY(height - buttonHeight - 5);
		backButton.setWidth(buttonHeight);
		backButton.setHeight(buttonHeight);
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
				BitmapFont font = Fonts.getInstance(LevelSelectionScreen.this.assetManager).mediumFont();
				font.setColor(Color.WHITE);
				String text = "Choose Your Galaxy";
				float halfFontWidth = font.getBounds(text).width / 2;
				batch.setShader(fontShader);
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
