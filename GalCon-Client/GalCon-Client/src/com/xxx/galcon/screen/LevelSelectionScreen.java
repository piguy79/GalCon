package com.xxx.galcon.screen;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.esotericsoftware.tablelayout.Cell;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.Map;
import com.xxx.galcon.model.Maps;
import com.xxx.galcon.screen.hud.HeaderHud;

public class LevelSelectionScreen implements ScreenFeedback, UIConnectionResultCallback<Maps> {

	private List<Map> allMaps;
	private int selectedMapKey = 0;

	private AssetManager assetManager;

	private String loadingMessage = "Loading...";

	private Texture levelSelectBgBottom;
	private Texture levelSelectionCard;

	private Texture back;
	private Texture forward;
	private Texture regularPlay;
	private Texture socialPlay;
	private Texture backTexture;
	private Texture levelSelectCardShadow;

	private Object returnValue;

	private Skin skin;
	private Stage stage;
	private Table cardTable;
	private ImageButton backButton;
	private Actor loadingTextActor;
	private Actor headerTextActor;

	private InputProcessor oldInputProcessor;

	public LevelSelectionScreen(AssetManager assetManager) {
		this.assetManager = assetManager;

		this.levelSelectionCard = assetManager.get("data/images/level_selection_card.png", Texture.class);
		this.levelSelectBgBottom = assetManager.get("data/images/level_select_bg_bottom.png", Texture.class);
		this.levelSelectionCard = assetManager.get("data/images/level_card_black.png", Texture.class);
		this.forward = assetManager.get("data/images/arrow_right_small_black.png", Texture.class);
		this.back = assetManager.get("data/images/arrow_left_small_black.png", Texture.class);
		this.regularPlay = assetManager.get("data/images/reg_play.png", Texture.class);
		this.socialPlay = assetManager.get("data/images/social_play.png", Texture.class);
		this.backTexture = assetManager.get("data/images/back.png", Texture.class);
		this.levelSelectCardShadow = assetManager.get("data/images/level_select_card_shadow.png", Texture.class);

		skin = new Skin();
		skin.add("default", new LabelStyle(Fonts.getInstance().largeFont(), Color.RED));

		TextureRegionDrawable textureRegionDrawable = new TextureRegionDrawable(new TextureRegion(regularPlay));
		skin.add("regularPlay", new ImageButtonStyle(textureRegionDrawable, textureRegionDrawable,
				textureRegionDrawable, textureRegionDrawable, textureRegionDrawable, textureRegionDrawable));

		textureRegionDrawable = new TextureRegionDrawable(new TextureRegion(socialPlay));
		skin.add("socialPlay", new ImageButtonStyle(textureRegionDrawable, textureRegionDrawable,
				textureRegionDrawable, textureRegionDrawable, textureRegionDrawable, textureRegionDrawable));

		textureRegionDrawable = new TextureRegionDrawable(new TextureRegion(backTexture));
		skin.add("backButton", new ImageButtonStyle(textureRegionDrawable, textureRegionDrawable,
				textureRegionDrawable, textureRegionDrawable, textureRegionDrawable, textureRegionDrawable));

		stage = new Stage();
		cardTable = new Table();
		backButton = new ImageButton(skin, "backButton");
		backButton.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				returnValue = Action.BACK;
			}
		});

		headerTextActor = new Actor() {
			public void draw(SpriteBatch batch, float parentAlpha) {
				BitmapFont font = Fonts.getInstance().largeFont();
				font.setColor(Color.WHITE);
				String text = "Choose Your Galaxy";
				float halfFontWidth = font.getBounds(text).width / 2;
				font.draw(batch, text, (getWidth() / 2 - halfFontWidth), getHeight() * .97f);
				font.setColor(Color.WHITE);
			};
		};

		stage.addActor(headerTextActor);
		stage.addActor(backButton);

		loadingTextActor = new Actor() {
			@Override
			public void draw(SpriteBatch batch, float parentAlpha) {
				BitmapFont font = Fonts.getInstance().mediumFont();
				if (loadingMessage.length() > 15) {
					font = Fonts.getInstance().smallFont();
				}
				float halfFontWidth = font.getBounds(loadingMessage).width / 2;
				font.draw(batch, loadingMessage, getWidth() / 2 - halfFontWidth, getHeight() * .4f);
			}
		};
		loadingTextActor.setWidth(Gdx.graphics.getWidth());
		loadingTextActor.setHeight(Gdx.graphics.getHeight());
		stage.addActor(loadingTextActor);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(48.0f / 255.0f, 150.0f / 255.0f, 200.0f / 255.0f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		stage.act(delta);
		stage.draw();

		// if (((InGameInputProcessor)
		// Gdx.input.getInputProcessor()).isBackKeyPressed()) {
		// returnValue = Action.BACK;
		// }
	}

	@Override
	public void resize(int width, int height) {
		stage.setViewport(width, height, true);

		int tableHeight = (int) (height * .7f);

		cardTable.setX(0);
		cardTable.setY(height * .5f - tableHeight * .42f);
		cardTable.setWidth(width);
		cardTable.setHeight(tableHeight);

		int buttonHeight = (int) (Gdx.graphics.getHeight() * (HeaderHud.HEADER_HEIGHT_RATIO * 0.88f));
		backButton.setX(10);
		backButton.setY(height - buttonHeight - 5);
		backButton.setWidth(buttonHeight);
		backButton.setHeight(buttonHeight);

		headerTextActor.setWidth(width);
		headerTextActor.setHeight(height);
	}

	@Override
	public void show() {
		if (allMaps == null) {
			UIConnectionWrapper.findAllMaps(this);
		}
		oldInputProcessor = Gdx.input.getInputProcessor();
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(oldInputProcessor);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		stage.dispose();
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
		this.allMaps = result.allMaps;
		Collections.sort(this.allMaps, new Comparator<Map>() {
			@Override
			public int compare(Map o1, Map o2) {
				return Integer.valueOf(o1.availableFromLevel).compareTo(Integer.valueOf(o2.availableFromLevel));
			}
		});

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

		Actor choiceActor = new Actor() {
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

				BitmapFont font = Fonts.getInstance().largeFont();
				font.setColor(Color.WHITE);
				float halfFontWidth = font.getBounds(text).width / 2;
				font.draw(batch, text, (getWidth() / 2 - halfFontWidth), getHeight() * .92f);
				font.setColor(Color.WHITE);
			};
		};
		choiceActor.setWidth(Gdx.graphics.getWidth());
		choiceActor.setHeight(Gdx.graphics.getHeight());
		stage.addActor(choiceActor);

		loadingTextActor.remove();
		stage.addActor(cardTable);

		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();

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
				returnValue = Action.PLAY + ":" + selectedMapKey;
			}
		});
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
				returnValue = Action.PLAY_WITH_FRIENDS + ":" + selectedMapKey;
			}
		});
		stage.addActor(friendsPlayButton);
	}

	@Override
	public void onConnectionError(String msg) {
		// TODO Auto-generated method stub

	}

	private class CardActor extends Actor {

		private Map map;
		private Texture mapTex;

		public CardActor(Map map, AssetManager assetManager) {
			this.map = map;
			this.mapTex = assetManager.get("data/images/levels/" + map.key + ".png", Texture.class);
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

			BitmapFont mediumFont = Fonts.getInstance().mediumFont();
			BitmapFont largeFont = Fonts.getInstance().largeFont();
			batch.draw(levelSelectionCard, x, y, width, height);

			String text = map.title;
			largeFont.setColor(Color.WHITE);
			float halfFontWidth = largeFont.getBounds(text).width / 2;
			largeFont.draw(batch, text, x + width / 2 - halfFontWidth, y + height * .85f);

			text = map.description;
			mediumFont.setColor(Color.WHITE);
			halfFontWidth = mediumFont.getBounds(text).width / 2;
			mediumFont.draw(batch, text, x + width / 2 - halfFontWidth, y + height * .18f);

			int mapHeight = (int) (getHeight() * .55f);
			int mapWidth = (int) (getWidth() * .9f);
			batch.draw(mapTex, x + width / 2 - mapWidth / 2, y + height / 2 - mapHeight / 2, mapWidth, mapHeight);
		}
	}

}
