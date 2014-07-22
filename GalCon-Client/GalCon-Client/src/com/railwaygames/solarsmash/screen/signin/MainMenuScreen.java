package com.railwaygames.solarsmash.screen.signin;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.ExternalActionWrapper;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.PartialScreenFeedback;
import com.railwaygames.solarsmash.Strings;
import com.railwaygames.solarsmash.UISkin;
import com.railwaygames.solarsmash.config.ConfigResolver;
import com.railwaygames.solarsmash.http.FriendsListener;
import com.railwaygames.solarsmash.http.GameAction;
import com.railwaygames.solarsmash.http.SocialAction;
import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.model.Friend;
import com.railwaygames.solarsmash.model.GameCount;
import com.railwaygames.solarsmash.model.Leaderboards;
import com.railwaygames.solarsmash.model.Leaderboards.LeaderboardEntry;
import com.railwaygames.solarsmash.model.Map;
import com.railwaygames.solarsmash.model.Maps;
import com.railwaygames.solarsmash.model.Player;
import com.railwaygames.solarsmash.screen.Action;
import com.railwaygames.solarsmash.screen.GraphicsUtils;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.level.LevelManager;
import com.railwaygames.solarsmash.screen.overlay.DismissableOverlay;
import com.railwaygames.solarsmash.screen.overlay.LevelUpOverlay;
import com.railwaygames.solarsmash.screen.overlay.Overlay;
import com.railwaygames.solarsmash.screen.overlay.TextOverlay;
import com.railwaygames.solarsmash.screen.widget.CountLabel;
import com.railwaygames.solarsmash.screen.widget.ScrollPaneHighlightReel;
import com.railwaygames.solarsmash.screen.widget.ScrollPaneHighlightReel.ScrollPaneHighlightReelBuilder;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;
import com.railwaygames.solarsmash.screen.widget.WaitImageButton;
import com.railwaygames.solarsmash.screen.widget.XpProgressBar;

public class MainMenuScreen implements PartialScreenFeedback {
	private String returnValue;
	private GameAction gameAction;
	private Stage stage;

	private Array<Actor> actors = new Array<Actor>();

	private Resources resources;
	private SocialAction socialAction;

	private AtlasRegion cardImage;
	private ScrollPaneHighlightReel highlightReel;
	private ShaderLabel coinText;
	protected WaitImageButton waitImage;
	private XpProgressBar xpBar;
	private Table cardTable;
	private Actor choiceActor;
	private ShaderLabel leaderboardText;

	private Overlay errorOverlay;

	private Leaderboards friendLeaderboards = null;
	private List<LeaderboardCardActor> leaderboardCards = new ArrayList<LeaderboardCardActor>();

	public boolean hideTitleArea = false;

	public MainMenuScreen(Resources resources, GameAction gameAction, SocialAction socialAction) {
		this.gameAction = gameAction;
		this.socialAction = socialAction;
		this.resources = resources;
	}

	private boolean loadingCards = false;

	private void loadCards(final GameCount gameCount) {
		if (loadingCards) {
			return;
		}
		Gdx.app.log("MMS", "Starting loadCards()");
		loadingCards = true;
		leaderboardCards.clear();

		final Table table = new Table();
		final ScrollPane scrollPane = new ScrollPane(table);
		scrollPane.setScrollingDisabled(false, true);
		scrollPane.setFadeScrollBars(false);

		cardTable.add(scrollPane);

		float cardWidth = Gdx.graphics.getWidth() * .62f;

		table.pad(0).padLeft((Gdx.graphics.getWidth() - cardWidth) * 0.3f).padRight(Gdx.graphics.getWidth() * 0.25f)
				.defaults().expandX().space(Gdx.graphics.getWidth() * 0.07f).width(cardWidth)
				.height(Gdx.graphics.getHeight() * .53f);

		gameAction.findAllMaps(new UIConnectionResultCallback<Maps>() {
			@Override
			public void onConnectionError(String msg) {
				// ignore error for now. if maps don't load, leaderboards just
				// won't show this time
				loadingCards = false;
			}

			public void onConnectionResult(Maps result) {
				Gdx.app.log("MMS", "Starting findAllMaps()->onConnectionResult()");
				CardGroup card = new MenuCardActor(gameCount, resources);
				table.add(card).expandX().fillX();

				LeaderboardCardActor lcard = new LeaderboardCardActor("all", "Overall", resources);
				table.add(lcard).expandX().fillX();
				leaderboardCards.add(lcard);

				for (Map map : result.allMaps) {
					lcard = new LeaderboardCardActor("" + map.key, map.title, resources);
					table.add(lcard).expandX().fillX();
					leaderboardCards.add(lcard);
				}

				AboutLeaderboardCardActor alcard = new AboutLeaderboardCardActor();
				table.add(alcard).expandX().fillX();

				createScrollhighlightReel(result.allMaps);
				loadingCards = false;
			};
		});

		choiceActor = new Actor() {
			public void draw(Batch batch, float parentAlpha) {
				if (highlightReel == null) {
					return;
				}
				float scrollX = scrollPane.getScrollX();
				for (int i = 0; i < table.getCells().size; ++i) {
					Cell<CardGroup> cell = table.getCells().get(i);
					float adjustX = cell.getActorX() - scrollX;

					float rightXBound = Math.min(getWidth(), adjustX + cell.getActorWidth());
					float leftXBound = Math.max(0, adjustX);

					if (rightXBound - leftXBound > getWidth() * .45f) {
						if (cell.getActor().getKey() >= 0) {
							leaderboardText.addAction(color(Color.WHITE, 0.3f));
						} else {
							leaderboardText.addAction(color(Color.CLEAR, 0.3f));
						}
						highlightReel.highlight(cell.getActor().getKey());
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
	}

	private void createScrollhighlightReel(List<Map> allMaps) {
		float actorWidth = Gdx.graphics.getWidth() * 0.04f;
		float actorPadding = Gdx.graphics.getWidth() * 0.05f;

		ScrollPaneHighlightReelBuilder builder = new ScrollPaneHighlightReel.ScrollPaneHighlightReelBuilder(
				Gdx.graphics.getHeight() * 0.08f, Gdx.graphics.getWidth() * 0.4f)
				.align(com.railwaygames.solarsmash.screen.widget.ActorBar.Align.LEFT)
				.actorSize(Gdx.graphics.getHeight() * 0.015f, actorWidth).actorPadding(actorPadding);

		float totalWidth = 0f;

		Image image = new Image(resources.skin.getDrawable(Constants.UI.SCROLL_HIGHLIGHT));
		image.setColor(Color.GRAY);
		builder.addActorWithKey(-1, image);
		totalWidth = totalWidth + actorPadding + actorWidth;

		image = new Image(resources.skin.getDrawable(Constants.UI.SCROLL_HIGHLIGHT));
		image.setColor(Color.GRAY);
		builder.addActorWithKey(0, image);
		totalWidth = totalWidth + actorPadding + actorWidth;

		for (Map map : allMaps) {
			image = new Image(resources.skin.getDrawable(Constants.UI.SCROLL_HIGHLIGHT));
			image.setColor(Color.GRAY);
			builder.addActorWithKey(map.key, image);
			totalWidth = totalWidth + actorPadding + actorWidth;
		}

		image = new Image(resources.skin.getDrawable(Constants.UI.SCROLL_HIGHLIGHT));
		image.setColor(Color.GRAY);
		builder.addActorWithKey(10000, image);
		totalWidth = totalWidth + actorPadding + actorWidth;

		// Account for the first one.
		totalWidth = totalWidth - actorPadding;

		highlightReel = builder.build();

		highlightReel.setX((Gdx.graphics.getWidth() - totalWidth) / 2);
		highlightReel.setY(Gdx.graphics.getHeight() * 0.17f);

		highlightReel.highlight(1);
		actors.add(highlightReel);
		stage.addActor(highlightReel);
	}

	private void addElementsToStage(final GameCount gameCount) {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();

		resources.assetManager.load("data/images/loading.pack", TextureAtlas.class);
		resources.assetManager.finishLoading();

		loadCards(gameCount);

		Button coinImage = new Button(resources.skin, Constants.UI.COIN);
		GraphicsUtils.setCommonButtonSize(coinImage);
		coinImage.setX(10);
		coinImage.setY(height * 0.99f - coinImage.getHeight());
		coinImage.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				startHideSequence(Action.COINS);
			}
		});
		stage.addActor(coinImage);
		actors.add(coinImage);

		coinText = new ShaderLabel(resources.fontShader, createCoinDisplay(), resources.skin,
				Constants.UI.DEFAULT_FONT, Color.WHITE);
		coinText.setAlignment(Align.left, Align.left);

		float yMidPoint = coinImage.getY() + coinImage.getHeight() / 2;
		float coinTextWidth = 0.4f * width;
		coinText.setBounds(10 + (coinImage.getWidth() * 1.1f), yMidPoint - coinText.getHeight() / 2, coinTextWidth,
				coinText.getHeight());
		stage.addActor(coinText);
		actors.add(coinText);

		addProgressBar();
	}

	private boolean noFreeGameSlots(final GameCount gameCount) {
		return gameCount.currentGameCount >= Integer.parseInt(ConfigResolver
				.getByConfigKey(Constants.Config.MAX_NUM_OF_OPEN_GAMES));
	}

	private void addProgressBar() {
		xpBar = new XpProgressBar(resources, Gdx.graphics.getHeight() * 0.05f, Gdx.graphics.getWidth());
		xpBar.setY(Gdx.graphics.getHeight() * 0.05f);
		stage.addActor(xpBar);
		actors.add(xpBar);
	}

	private void startHideSequence(final String retVal) {
		GraphicsUtils.hideAnimated(actors, retVal.equals(Action.BACK), new Runnable() {
			@Override
			public void run() {
				returnValue = retVal;
			}
		});
	}

	@Override
	public void render(float delta) {
		if (coinText != null) {
			coinText.setText(createCoinDisplay());
		}
	}

	private String createCoinDisplay() {
		return "" + GameLoop.USER.coins;
	}

	@Override
	public void resize(int width, int height) {
		float buttonWidth = .25f * (float) width;
		waitImage.setWidth(buttonWidth);
		waitImage.setHeight(buttonWidth);
		waitImage.setX(width / 2 - buttonWidth / 2);
		waitImage.setY(height / 2 - buttonWidth / 2);

		int tableHeight = (int) (height * .53f);
		cardTable.setX(0);
		cardTable.setY(height * .4f - tableHeight * .42f);
		cardTable.setWidth(width);
		cardTable.setHeight(tableHeight);

		leaderboardText.setX(width / 2 - leaderboardText.getWidth() / 2);
		leaderboardText.setY(height * 0.72f);
	}

	@Override
	public void show(Stage stage) {
		Gdx.app.log("MMS", "Starting show()");
		this.stage = stage;
		actors.clear();
		leaderboardCards.clear();

		waitImage = new WaitImageButton(resources.skin);
		stage.addActor(waitImage);

		this.cardImage = resources.levelSelectionAtlas.findRegion("level_card_gray");

		leaderboardText = new ShaderLabel(resources.fontShader, "Leaderboards", resources.skin,
				Constants.UI.DEFAULT_FONT, Color.WHITE);
		leaderboardText.setColor(Color.CLEAR);
		actors.add(leaderboardText);
		stage.addActor(leaderboardText);

		cardTable = new Table();

		loadFbFriends();
		loadUser();
	}

	private void loadFbFriends() {
		if (GameLoop.USER.auth.hasAuth(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK)) {
			socialAction.getFriends(new FriendsListener() {
				@Override
				public void onFriendsLoadedFail(String error) {
					Gdx.app.log("FRIENDS", "Could not load FB friends: " + error);
					showLeaderboardLoadError();
					loadGooglePlusFriends();
				}

				@Override
				public void onFriendsLoadedSuccess(List<Friend> friends, String authProviderUsed) {
					loadLeaderboardsForFriends(friends, authProviderUsed);
					loadGooglePlusFriends();
				}
			}, Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK);
		} else {
			loadGooglePlusFriends();
		}
	}

	private void loadGooglePlusFriends() {
		if (GameLoop.USER.auth.hasAuth(Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE) && socialAction.enableGoogle()) {
			socialAction.getFriends(new FriendsListener() {
				@Override
				public void onFriendsLoadedFail(String error) {
					Gdx.app.log("FRIENDS", "Could not load G+ friends: " + error);
					showLeaderboardLoadError();
				}

				@Override
				public void onFriendsLoadedSuccess(List<Friend> friends, String authProviderUsed) {
					loadLeaderboardsForFriends(friends, authProviderUsed);

				}
			}, Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE);
		}
	}

	private void showLeaderboardLoadError() {
		for (LeaderboardCardActor actor : leaderboardCards) {
			actor.showError();
		}
	}

	private void loadLeaderboardsForFriends(List<Friend> friends, String authProviderUsed) {
		List<String> authIds = new ArrayList<String>(friends.size());
		for (Friend friend : friends) {
			authIds.add(friend.id);
		}

		gameAction.findLeaderboardsForFriends(new UIConnectionResultCallback<Leaderboards>() {

			@Override
			public void onConnectionResult(Leaderboards result) {
				if (friendLeaderboards == null) {
					friendLeaderboards = result;
				} else {
					friendLeaderboards.merge(result);
				}

				for (LeaderboardCardActor actor : leaderboardCards) {
					actor.refreshFriends();
				}
			}

			@Override
			public void onConnectionError(String msg) {
				showLeaderboardLoadError();
			}

		}, authIds, GameLoop.USER.handle, authProviderUsed);
	}

	private boolean loadingUser = false;

	private void loadUser() {
		if (loadingUser) {
			return;
		}
		loadingUser = true;
		waitImage.start();
		gameAction.addFreeCoins(new UIConnectionResultCallback<Player>() {

			@Override
			public void onConnectionResult(final Player result) {
				if (errorOverlay != null) {
					errorOverlay.remove();
				}
				GameLoop.USER = result;

				if (LevelManager.shouldShowLevelUp(result)) {
					hideTitleArea = true;
					final LevelUpOverlay levelUp = new LevelUpOverlay(resources, result);
					levelUp.addListener(new ClickListener() {
						@Override
						public void clicked(InputEvent event, float x, float y) {
							hideTitleArea = false;
							LevelManager.storeLevel(result);
							levelUp.remove();
							finishLoadingUser();
						}
					});
					stage.addActor(levelUp);

				} else {
					finishLoadingUser();
				}
			}

			private void finishLoadingUser() {

				gameAction.findGamesWithPendingMove(new UIConnectionResultCallback<GameCount>() {
					public void onConnectionResult(GameCount result) {
						loadingUser = false;
						addElementsToStage(result);
						waitImage.stop();
					};

					@Override
					public void onConnectionError(String msg) {
						loadingUser = false;
						errorOverlay = new DismissableOverlay(resources, new TextOverlay(
								"Could not retrieve user.\n\nPlease try again.", resources), new ClickListener() {
							@Override
							public void clicked(InputEvent event, float x, float y) {
								loadUser();
							}
						});
						stage.addActor(errorOverlay);
					}
				}, GameLoop.USER.handle);
			}

			@Override
			public void onConnectionError(String msg) {
				loadingUser = false;
				if (errorOverlay != null) {
					errorOverlay.remove();
				}
				errorOverlay = new DismissableOverlay(resources, new TextOverlay(
						"Could not retrieve user.\n\nPlease try again.", resources), new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						loadUser();
					}
				});
				stage.addActor(errorOverlay);
			}
		}, GameLoop.USER.handle);
	}

	@Override
	public void hide() {
		for (Actor actor : actors) {
			actor.remove();
		}

		if (waitImage != null) {
			waitImage.stop();
		}
	}

	@Override
	public void resetState() {
		friendLeaderboards = null;
		returnValue = null;
	}

	@Override
	public Object getRenderResult() {
		return returnValue;
	}

	@Override
	public boolean hideTitleArea() {
		return hideTitleArea;
	}

	@Override
	public boolean canRefresh() {
		return true;
	}

	public class AboutLeaderboardCardActor extends CardGroup {

		private Table table;
		private boolean loaded = false;

		@Override
		protected void sizeChanged() {
			super.sizeChanged();
			table.setX(getWidth() * 0.13f);
			table.setWidth(getWidth() - getWidth() * 0.06f);
			table.setHeight(getHeight());
			table.setY(getY());
		}

		public AboutLeaderboardCardActor() {
			super(10000);

			table = new Table();
			table.defaults().pad(0);
			table.top();

			addActor(table);
		}

		@Override
		public void draw(Batch batch, float parentAlpha) {
			if (!loaded) {
				loaded = true;
				loadCard();
			}

			super.draw(batch, parentAlpha);
		}

		private void loadCard() {
			ShaderLabel label = new ShaderLabel(resources.fontShader, "About\nLeaderboards", resources.skin,
					Constants.UI.DEFAULT_FONT, Color.BLACK);
			label.setAlignment(Align.center);
			table.add(label).align(Align.center);

			table.row();
			label = new ShaderLabel(
					resources.fontShader,
					"The leaderboard scores are calculated hourly. "
							+ "It takes the following into account: your win/loss record, your opponent's win/loss record, and the number of games played. "
							+ "Games against the AI are not counted as well as games where people immediately resigned or claimed."
							+ "\n\nThe formula may be tweaked as time goes on.", resources.skin,
					Constants.UI.X_SMALL_FONT,

					Color.BLACK);
			label.setWrap(true);
			label.setAlignment(Align.top, Align.left);
			table.add(label).expand().top().left().fill();
		}
	}

	private class LeaderboardCardActor extends CardGroup implements UIConnectionResultCallback<Leaderboards> {
		private Leaderboards globalLeaderboards;
		private boolean loaded = false;
		private boolean headerLoaded = false;
		private final String id;
		private final String mapTitle;
		private Table table;

		private Button tabLeft;
		private Button tabRight;
		private ShaderLabel tabTextLeft;
		private ShaderLabel tabTextRight;

		@Override
		protected void sizeChanged() {
			super.sizeChanged();
			table.setX(getWidth() * 0.13f);
			table.setWidth(getWidth() - getWidth() * 0.06f);
			table.setHeight(getHeight());
			table.setY(getY());

			float x = getX();
			float y = getY();
			float width = getWidth();
			float height = getHeight();

			int xOffset = (int) (width * 0.1f);
			int yOffset = (int) (height * 0.1f);
			x += xOffset;
			y += yOffset;

			tabLeft.setBounds(xOffset, y, width * 0.5f, height * 0.13f);
			tabTextLeft.setBounds(xOffset, y, width * 0.5f, height * 0.13f);

			tabRight.setBounds(xOffset + width * 0.5f, y, width * 0.5f, height * 0.13f);
			tabTextRight.setBounds(xOffset + width * 0.5f, y, width * 0.5f, height * 0.13f);
		}

		public void refreshFriends() {
			if (!tabLeft.isVisible()) {
				table.clear();
				loadLeaderboards(friendLeaderboards.leaderboards.get(id));
			}
		}

		public void showError() {
			if (!tabLeft.isVisible()) {
				table.clear();
				loadHeader();
				ShaderLabel label = new ShaderLabel(resources.fontShader, "Could not load leaderboards at this time",
						resources.skin, Constants.UI.X_SMALL_FONT, Color.WHITE);
				label.setColor(Color.RED);
				label.setWrap(true);
				label.setAlignment(Align.top, Align.center);
				table.row();
				table.add(label).colspan(3).align(Align.center).expand().fill().top();
			}
		}

		public LeaderboardCardActor(final String id, String mapTitle, final Resources resources) {
			super(id.equals("all") ? 0 : Integer.valueOf(id));

			this.id = id;
			this.mapTitle = id.equals("all") ? mapTitle : "Map: " + mapTitle;
			table = new Table();
			table.defaults().pad(0);
			table.top();

			addActor(table);

			tabLeft = new Button(resources.skin, Constants.UI.TAB_LEFT);
			tabRight = new Button(resources.skin, Constants.UI.TAB_RIGHT);

			tabTextLeft = new ShaderLabel(resources.fontShader, "Friends", resources.skin, Constants.UI.X_SMALL_FONT,
					Color.WHITE);
			tabTextLeft.setColor(Color.BLACK);
			tabTextLeft.setAlignment(Align.center);
			tabTextLeft.setTouchable(Touchable.disabled);
			tabLeft.addListener(new ClickListener() {
				public void clicked(InputEvent event, float x, float y) {
					if (tabLeft.isVisible()) {
						table.clear();
						loadHeader();
						tabTextLeft.setColor(Color.BLACK);
						tabTextRight.setColor(Color.WHITE);
						tabRight.setVisible(true);
						tabLeft.setVisible(false);

						if (friendLeaderboards != null) {
							loadLeaderboards(friendLeaderboards.leaderboards.get(id));
						}
					}
				};
			});
			tabTextRight = new ShaderLabel(resources.fontShader, "Global", resources.skin, Constants.UI.X_SMALL_FONT,
					Color.WHITE);
			tabTextRight.setAlignment(Align.center);
			tabTextRight.setTouchable(Touchable.disabled);
			tabRight.addListener(new ClickListener() {
				public void clicked(InputEvent event, float x, float y) {
					if (tabRight.isVisible()) {
						table.clear();
						loadHeader();
						tabTextLeft.setColor(Color.WHITE);
						tabTextRight.setColor(Color.BLACK);
						tabLeft.setVisible(true);
						tabRight.setVisible(false);

						if (globalLeaderboards != null) {
							loadLeaderboards(globalLeaderboards.leaderboards.get(id));
						} else {
							WaitImageButton waitImage = new WaitImageButton(resources.skin);
							float buttonWidth = .4f * (float) getWidth();
							waitImage.start();
							table.row();
							table.add(waitImage).colspan(3).center().height(table.getHeight() * 0.4f)
									.prefWidth(buttonWidth).minWidth(buttonWidth);
							ExternalActionWrapper.findLeaderboardById(LeaderboardCardActor.this, id);
						}
					}
				};
			});

			tabLeft.setVisible(false);

			addActor(tabLeft);
			addActor(tabRight);
			addActor(tabTextLeft);
			addActor(tabTextRight);
		}

		@Override
		public void draw(Batch batch, float parentAlpha) {
			if (!headerLoaded) {
				headerLoaded = true;
				loadHeader();

				WaitImageButton waitImage = new WaitImageButton(resources.skin);
				float buttonWidth = .4f * (float) getWidth();
				waitImage.start();
				table.row();
				table.add(waitImage).colspan(3).center().height(table.getHeight() * 0.4f).prefWidth(buttonWidth)
						.minWidth(buttonWidth);
			}
			if (!loaded) {
				if (!tabLeft.isVisible() && friendLeaderboards != null) {
					loaded = true;
					loadLeaderboards(friendLeaderboards.leaderboards.get(id));
				}
			}

			super.draw(batch, parentAlpha);
		}

		private void loadHeader() {
			ShaderLabel label = new ShaderLabel(resources.fontShader, mapTitle, resources.skin,
					Constants.UI.DEFAULT_FONT, Color.BLACK);
			table.add(label).colspan(3).align(Align.center).height(getHeight() * 0.12f);

			table.row().height(getHeight() * 0.03f);
			table.add();
			label = new ShaderLabel(resources.fontShader, "Handle", resources.skin, Constants.UI.X_SMALL_FONT,
					Color.GRAY);
			table.add(label).left().padRight(getWidth() * 0.02f);

			label = new ShaderLabel(resources.fontShader, "Score", resources.skin, Constants.UI.X_SMALL_FONT,
					Color.GRAY);
			table.add(label).left().padRight(getWidth() * 0.02f);
		}

		private void loadLeaderboards(List<LeaderboardEntry> results) {
			table.clear();
			loadHeader();

			if (results == null) {
				table.row().height(getHeight() * 0.055f);
				ShaderLabel label = new ShaderLabel(resources.fontShader, "No results found", resources.skin,
						Constants.UI.X_SMALL_FONT, Color.BLACK);
				table.add(label).colspan(3).align(Align.center);
				return;
			}

			for (LeaderboardEntry entry : results) {
				table.row().height(getHeight() * 0.055f);
				ShaderLabel label = new ShaderLabel(resources.fontShader, "" + entry.rank, resources.skin,
						Constants.UI.X_SMALL_FONT, Color.BLACK);
				table.add(label).left().padRight(getWidth() * 0.02f);

				label = new ShaderLabel(resources.fontShader, entry.handle, resources.skin, Constants.UI.X_SMALL_FONT,
						Color.GRAY);
				if (entry.handle.equals(GameLoop.USER.handle)) {
					label.setColor(Constants.Colors.USER_SHIP_FILL);
				} else {
					label.setColor(Constants.Colors.ENEMY_SHIP_FILL);
				}
				table.add(label).expandX().left();

				BigDecimal score = new BigDecimal(entry.score);
				score = score.setScale(2, RoundingMode.HALF_UP);

				label = new ShaderLabel(resources.fontShader, score.toString(), resources.skin,
						Constants.UI.X_SMALL_FONT, Color.BLACK);
				table.add(label).right();
			}
		}

		@Override
		public void onConnectionError(String msg) {
			table.clear();
			loadHeader();
		}

		@Override
		public void onConnectionResult(Leaderboards result) {
			this.globalLeaderboards = result;

			if (!tabRight.isVisible()) {
				table.clear();
				loadHeader();
				loadLeaderboards(result.leaderboards.get(id));
			}
		}
	}

	private class MenuCardActor extends CardGroup {
		private ShaderLabel newLabel;
		private ShaderLabel continueLabel;
		private ShaderLabel inviteLabel;
		private CountLabel continueCountLabel;
		private CountLabel inviteCountLabel;

		@Override
		protected void sizeChanged() {
			super.sizeChanged();

			int height = (int) getHeight();
			int width = (int) getWidth();

			newLabel.setX(width * 0.6f - newLabel.getWidth() / 2);
			newLabel.setY(0.8f * height);

			continueLabel.setX(width * 0.6f - continueLabel.getWidth() / 2);
			continueLabel.setY(0.5f * height);

			if (continueCountLabel != null) {
				continueCountLabel.setX((Gdx.graphics.getWidth() / 2) + (continueLabel.getTextBounds().width * 0.4f));
				continueCountLabel.setY(continueLabel.getY() + (continueLabel.getHeight() * 0.2f));
			}

			inviteLabel.setX(width * 0.6f - inviteLabel.getWidth() / 2);
			inviteLabel.setY(0.2f * height);

			if (inviteCountLabel != null) {
				inviteCountLabel.setX((Gdx.graphics.getWidth() / 2) + (inviteLabel.getTextBounds().width * 0.4f));
				inviteCountLabel.setY(inviteLabel.getY() + (inviteLabel.getHeight() * 0.2f));
			}
		}

		public MenuCardActor(final GameCount gameCount, final Resources resources) {
			super(-1);

			final String noFreeSlots = "No game slots available. \nMax slots ["
					+ ConfigResolver.getByConfigKey(Constants.Config.MAX_NUM_OF_OPEN_GAMES) + "]"
					+ "\nComplete games to open up slots.";

			newLabel = new ShaderLabel(resources.fontShader, Strings.NEW, resources.skin, Constants.UI.DEFAULT_FONT,
					Color.WHITE);
			newLabel.setAlignment(Align.center);
			newLabel.addListener(new InputListener() {
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					return true;
				}

				@Override
				public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
					if (noFreeGameSlots(gameCount)) {
						final Overlay overlay = new DismissableOverlay(resources, new TextOverlay(noFreeSlots,
								resources));
						stage.addActor(overlay);
					} else {
						startHideSequence(Strings.NEW);
					}
				}
			});

			continueLabel = new ShaderLabel(resources.fontShader, Strings.CONTINUE, resources.skin,
					Constants.UI.DEFAULT_FONT, Color.WHITE);
			continueLabel.setAlignment(Align.center);
			continueLabel.addListener(new InputListener() {
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					return true;
				}

				@Override
				public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
					startHideSequence(Strings.CONTINUE);
				}
			});

			inviteLabel = new ShaderLabel(resources.fontShader, Strings.INVITES, resources.skin,
					Constants.UI.DEFAULT_FONT, Color.WHITE);
			inviteLabel.setAlignment(Align.center);
			inviteLabel.addListener(new InputListener() {
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					return true;
				}

				@Override
				public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
					if (noFreeGameSlots(gameCount)) {
						final Overlay overlay = new DismissableOverlay(resources, new TextOverlay(noFreeSlots,
								resources));
						stage.addActor(overlay);
					} else {
						startHideSequence(Strings.INVITES);
					}
				}
			});

			if (gameCount != null && gameCount.pendingGameCount > 0) {
				continueCountLabel = new CountLabel(gameCount.pendingGameCount, resources.fontShader,
						(UISkin) resources.skin);

				addActor(continueCountLabel);
			}

			if (gameCount != null && gameCount.inviteCount > 0) {
				inviteCountLabel = new CountLabel(gameCount.inviteCount, resources.fontShader, (UISkin) resources.skin);
				addActor(inviteCountLabel);
			}

			addActor(continueLabel);
			addActor(newLabel);
			addActor(inviteLabel);
		}
	}

	private class CardGroup extends Group {

		private int key;

		public CardGroup(int key) {
			this.key = key;
		}

		public int getKey() {
			return key;
		}

		@Override
		public void draw(Batch batch, float parentAlpha) {
			if (!getClass().equals(MenuCardActor.class)) {
				int height = (int) getHeight();
				int width = (int) getWidth();

				int x = (int) getX();
				int y = (int) getY();

				int xOffset = (int) (width * 0.1f);
				int yOffset = (int) (height * 0.1f);
				x += xOffset;
				y += yOffset;

				height -= yOffset;

				batch.draw(cardImage, x, y, width, height);
			}

			super.draw(batch, parentAlpha);
		}
	}
}
