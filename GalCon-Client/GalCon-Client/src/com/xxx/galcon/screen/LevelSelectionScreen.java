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
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.Map;
import com.xxx.galcon.model.Maps;
import com.xxx.galcon.screen.hud.Hud;
import com.xxx.galcon.screen.hud.LevelSelectionHud;

public class LevelSelectionScreen implements ScreenFeedback, UIConnectionResultCallback<Maps> {

	private List<Map> allMaps;

	private AssetManager assetManager;

	private String loadingMessage = "Loading...";

	private Texture levelSelectionCard;
	private Texture rectButtonBlank;

	private Object returnValue;

	private Hud levelSelectionHud;

	private Skin skin;
	private Stage stage;
	private Table cardTable;
	private Actor loadingTextActor;

	private InputProcessor oldInputProcessor;

	public LevelSelectionScreen(AssetManager assetManager) {
		this.assetManager = assetManager;

		this.levelSelectionCard = assetManager.get("data/images/level_selection_card.png", Texture.class);
		this.rectButtonBlank = assetManager.get("data/images/rect_button_blank.png", Texture.class);

		levelSelectionHud = new LevelSelectionHud(assetManager);

		buttons.add(new Button(rectButtonBlank) {
			@Override
			public void updateLocationAndSize(int x, int y, int width, int height) {
				int buttonWidth = (int) (width * 0.3f);
				int buttonHeight = (int) (height * 0.08f);
				int margin = (int) (width * 0.12f);

				this.x = x + margin;
				this.y = y + margin;
				this.height = buttonHeight;
				this.width = buttonWidth;
			}

			@Override
			public String getActionOnClick() {
				return Action.PLAY;
			}

			@Override
			public void render(SpriteBatch spriteBatch) {
				super.render(spriteBatch);

				BitmapFont font = Fonts.getInstance().smallFont();
				font.setColor(Color.BLACK);
				String text = "Play";
				TextBounds bounds = font.getBounds(text);
				float halfFontWidth = bounds.width / 2;
				float halfFontHeight = bounds.height / 2;
				font.draw(spriteBatch, text, x + width / 2 - halfFontWidth, y + height / 2 + halfFontHeight);
				font.setColor(Color.WHITE);
			}
		});

		buttons.add(new Button(rectButtonBlank) {
			@Override
			public void updateLocationAndSize(int x, int y, int width, int height) {
				int buttonWidth = (int) (width * 0.3f);
				int buttonHeight = (int) (height * 0.08f);
				int margin = (int) (width * 0.12f);

				this.x = x + width - margin - buttonWidth;
				this.y = y + margin;
				this.height = buttonHeight;
				this.width = buttonWidth;
			}

			@Override
			public String getActionOnClick() {
				return Action.PLAY_WITH_FRIENDS;
			}

			@Override
			public void render(SpriteBatch spriteBatch) {
				super.render(spriteBatch);

				BitmapFont font = Fonts.getInstance().smallFont();
				font.setColor(Color.BLACK);
				String text = "Play with friends";
				TextBounds bounds = font.getBounds(text);
				float halfFontWidth = bounds.width / 2;
				float halfFontHeight = bounds.height / 2;
				font.draw(spriteBatch, text, x + width / 2 - halfFontWidth, y + height / 2 + halfFontHeight);
				font.setColor(Color.WHITE);
			}
		});

		skin = new Skin();
		skin.add("default", new LabelStyle(Fonts.getInstance().largeFont(), Color.RED));

		stage = new Stage();
		cardTable = new Table();
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
		stage.addActor(loadingTextActor);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(148.0f / 255.0f, 228.0f / 255.0f, 148.0f / 255.0f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		stage.act(delta);
		stage.draw();

		// float width = Gdx.graphics.getWidth();
		// float height = Gdx.graphics.getHeight();

		// levelSelectionHud.render(delta);
		// if (levelSelectionHud.getRenderResult() != null) {
		// String result = (String) levelSelectionHud.getRenderResult();
		// if (result == Action.BACK) {
		// returnValue = result;
		// }
		// }

		// InGameInputProcessor ip = (InGameInputProcessor)
		// Gdx.input.getInputProcessor();
		// if (ip.didTouch()) {
		// TouchPoint tp = ip.getTouch();
		// int x = tp.x / 2;
		// int y = tp.y / 2;
		// for (int i = 0; i < buttons.size(); ++i) {
		// Button button = buttons.get(i);
		// if (button.isTouched(x, y)) {
		// returnValue = button.getActionOnClick();
		// ip.consumeTouch();
		// }
		// }
		// }

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
		cardTable.setY(height / 2 - tableHeight / 2);
		cardTable.setWidth(width);
		cardTable.setHeight(tableHeight);
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

		Table table = new Table();
		ScrollPane scrollPane = new ScrollPane(table);
		scrollPane.setScrollingDisabled(false, true);

		cardTable.add(scrollPane);

		table.pad(10).defaults().expandX().space(8).width(Gdx.graphics.getWidth() * .7f)
				.height(Gdx.graphics.getHeight() * .6f);

		for (int i = 0; i < allMaps.size(); ++i) {
			final Map map = allMaps.get(i);

			table.add(new Actor() {
				@Override
				public void draw(SpriteBatch batch, float parentAlpha) {
					int height = (int) getHeight();
					int width = (int) getWidth();

					int x = (int) getX();
					int y = (int) getY();

					BitmapFont mediumFont = Fonts.getInstance().mediumFont();
					BitmapFont smallFont = Fonts.getInstance().smallFont();
					batch.draw(levelSelectionCard, x, y, width, height);

					String text = map.title;
					mediumFont.setColor(Color.BLACK);
					float halfFontWidth = mediumFont.getBounds(text).width / 2;
					mediumFont.draw(batch, text, x + width / 2 - halfFontWidth, y + height * .73f);
					mediumFont.setColor(Color.WHITE);

					text = map.description;
					smallFont.setColor(Color.BLACK);
					halfFontWidth = smallFont.getBounds(text).width / 2;
					smallFont.draw(batch, text, x + width / 2 - halfFontWidth, y + height * .3f);
					smallFont.setColor(Color.WHITE);

					int mapHeight = (int) (getHeight() * .35f);
					int mapWidth = (int) (getWidth() * .5f);
					batch.draw(assetManager.get("data/images/levels/" + map.key + ".png", Texture.class), x + width / 2
							- mapWidth / 2, y + height / 2 - mapHeight / 2, mapWidth, mapHeight);
				}
			}).expandX().fillX();
		}

		loadingTextActor.remove();
		stage.addActor(cardTable);
		ImageButton playButton = new ImageButton(skin);
		playButton.
	}

	@Override
	public void onConnectionError(String msg) {
		// TODO Auto-generated method stub

	}

}
