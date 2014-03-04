package com.xxx.galcon.screen;

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
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.tablelayout.Cell;
import com.xxx.galcon.Constants;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.PartialScreenFeedback;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.Map;
import com.xxx.galcon.model.Maps;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.screen.event.GameStartListener;
import com.xxx.galcon.screen.overlay.DismissableOverlay;
import com.xxx.galcon.screen.overlay.Overlay;
import com.xxx.galcon.screen.overlay.TextOverlay;
import com.xxx.galcon.screen.widget.ActorBar.Align;
import com.xxx.galcon.screen.widget.ScrollPaneHighlightReel.ScrollPaneHighlightReelBuilder;
import com.xxx.galcon.screen.widget.ScrollPaneHighlightReel;
import com.xxx.galcon.screen.widget.WaitImageButton;

public class LevelSelectionScreen implements PartialScreenFeedback, UIConnectionResultCallback<Maps> {

	private List<Map> allMaps;

	private AtlasRegion levelSelectionCard;
	private AtlasRegion levelSelectCardShadow;

	private Object returnValue;

	private Stage stage;
	private Table cardTable;
	private Actor choiceActor;
	private ImageButton backButton;
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
				.height(Gdx.graphics.getHeight() * .7f);

		for (int i = 0; i < allMaps.size(); ++i) {
			final Map map = allMaps.get(i);

			CardActor card = new CardActor(map, resources);
			card.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					createGameStartDialog(map.key);
				}

			});
			table.add(card).expandX().fillX();
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
						highlightReel.highlight(cell.getWidget().getMapKey());
					}
				}

				BitmapFont font = Fonts.getInstance(resources.assetManager).mediumFont();
				float halfFontWidth = font.getBounds(text).width / 2;
				batch.setShader(resources.fontShader);
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

		backButton.remove();
		stage.addActor(backButton);
		
		createScrollhighlightReel();
	}

	
	private void createScrollhighlightReel() {	
		ScrollPaneHighlightReelBuilder builder = new ScrollPaneHighlightReel.ScrollPaneHighlightReelBuilder(Gdx.graphics.getHeight() * 0.1f, Gdx.graphics.getWidth() * 0.4f)
									.align(Align.LEFT).actorSize(Gdx.graphics.getHeight() * 0.02f, Gdx.graphics.getWidth() * 0.04f)
									.actorPadding(Gdx.graphics.getWidth() * 0.1f);
		
		for(Map map : allMaps){
			Image image = new Image(resources.skin.getDrawable(Constants.UI.SCROLL_HIGHLIGHT));
			image.setColor(Color.GRAY);
			builder.addActorWithKey(map.key, image);
		}
		
		highlightReel = builder.build();
		
		highlightReel.setX(Gdx.graphics.getWidth() * 0.35f);
		highlightReel.setY(Gdx.graphics.getHeight() * 0.05f);
		
		highlightReel.highlight(allMaps.get(0).key);
		actors.add(highlightReel);
		stage.addActor(highlightReel);
		
	}

	private void createGameStartDialog(int selectedMapKey) {
		final GameStartDialog dialog = new GameStartDialog(resources, Gdx.graphics.getWidth() * 0.8f,
				Gdx.graphics.getHeight() * 0.5f, stage, selectedMapKey);
		float dialogY = Gdx.graphics.getHeight() * 0.3f;

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

		public CardActor(Map map, Resources resources) {
			this.map = map;
			this.mapTex = resources.levelAtlas.findRegion("" + map.key);
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

			BitmapFont smallFont = Fonts.getInstance(resources.assetManager).xSmallFont();
			BitmapFont mediumFont = Fonts.getInstance(resources.assetManager).mediumFont();

			batch.draw(levelSelectionCard, x, y, width, height);

			int mapHeight = (int) (getHeight() * .55f);
			int mapWidth = (int) (getWidth() * .9f);
			batch.draw(mapTex, x + width / 2 - mapWidth / 2, y + height / 2 - mapHeight / 2, mapWidth, mapHeight);

			String text = map.title;
			mediumFont.setColor(Color.BLACK);
			float halfFontWidth = mediumFont.getBounds(text).width / 2;
			batch.setShader(resources.fontShader);
			mediumFont.draw(batch, text, x + width / 2 - halfFontWidth, y + height * .88f);

			text = map.description;
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

		backButton = new ImageButton(resources.skin, "backButton");
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
