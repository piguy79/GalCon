package com.railwaygames.solarsmash.screen.signin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.PartialScreenFeedback;
import com.railwaygames.solarsmash.Strings;
import com.railwaygames.solarsmash.UISkin;
import com.railwaygames.solarsmash.config.ConfigResolver;
import com.railwaygames.solarsmash.http.GameAction;
import com.railwaygames.solarsmash.http.SocialAction;
import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.model.GameCount;
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

	private ShaderLabel newLabel;
	private ShaderLabel continueLabel;
	private ShaderLabel inviteLabel;
	private ShaderLabel coinText;
	protected WaitImageButton waitImage;
	private XpProgressBar xpBar;

	public boolean hideTitleArea = false;

	public MainMenuScreen(Resources resources, GameAction gameAction, SocialAction socialAction) {
		this.gameAction = gameAction;
		this.socialAction = socialAction;
		this.resources = resources;
	}

	private void addElementsToStage(final GameCount gameCount) {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();

		final String noFreeSlots = "No game slots available. \nMax slots ["
				+ ConfigResolver.getByConfigKey(Constants.Config.MAX_NUM_OF_OPEN_GAMES) + "]"
				+ "\nComplete games to open up slots.";

		resources.assetManager.load("data/images/loading.pack", TextureAtlas.class);
		resources.assetManager.finishLoading();

		newLabel = new ShaderLabel(resources.fontShader, Strings.NEW, resources.skin, Constants.UI.DEFAULT_FONT);
		newLabel.setAlignment(Align.center);
		newLabel.setWidth(width);
		newLabel.setX(width / 2 - newLabel.getWidth() / 2);
		newLabel.setY(0.45f * height);
		newLabel.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				if (noFreeGameSlots(gameCount)) {
					final Overlay overlay = new DismissableOverlay(resources, new TextOverlay(noFreeSlots, resources));
					stage.addActor(overlay);
				} else {
					startHideSequence(Strings.NEW);
				}
			}

		});
		stage.addActor(newLabel);
		actors.add(newLabel);

		continueLabel = new ShaderLabel(resources.fontShader, Strings.CONTINUE, resources.skin,
				Constants.UI.DEFAULT_FONT);
		continueLabel.setAlignment(Align.center);
		continueLabel.setWidth(width);
		continueLabel.setX(width / 2 - continueLabel.getWidth() / 2);
		continueLabel.setY(0.33f * height);
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
		stage.addActor(continueLabel);
		actors.add(continueLabel);

		inviteLabel = new ShaderLabel(resources.fontShader, Strings.INVITES, resources.skin, Constants.UI.DEFAULT_FONT);
		inviteLabel.setAlignment(Align.center);
		inviteLabel.setWidth(width);
		inviteLabel.setX(width / 2 - continueLabel.getWidth() / 2);
		inviteLabel.setY(0.21f * height);
		inviteLabel.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				if (noFreeGameSlots(gameCount)) {
					final Overlay overlay = new DismissableOverlay(resources, new TextOverlay(noFreeSlots, resources));
					stage.addActor(overlay);
				} else {
					startHideSequence(Strings.INVITES);
				}
			}
		});
		stage.addActor(inviteLabel);
		actors.add(inviteLabel);

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

		coinText = new ShaderLabel(resources.fontShader, createCoinDisplay(), resources.skin, Constants.UI.DEFAULT_FONT);
		coinText.setAlignment(Align.left, Align.left);

		float yMidPoint = coinImage.getY() + coinImage.getHeight() / 2;
		float coinTextWidth = 0.4f * width;
		coinText.setBounds(10 + (coinImage.getWidth() * 1.1f), yMidPoint - coinText.getHeight() / 2, coinTextWidth,
				coinText.getHeight());
		stage.addActor(coinText);
		actors.add(coinText);

		addContinueCount(gameCount);
		addInviteCount(gameCount);
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

	private void addContinueCount(GameCount gameCount) {
		if (gameCount != null && gameCount.pendingGameCount > 0) {
			CountLabel countLabel = new CountLabel(gameCount.pendingGameCount, resources.fontShader,
					(UISkin) resources.skin);
			countLabel.setX((Gdx.graphics.getWidth() / 2) + (continueLabel.getTextBounds().width * 0.6f));
			countLabel.setY(continueLabel.getY() + (continueLabel.getHeight() * 0.2f));

			stage.addActor(countLabel);
			actors.add(countLabel);
		}
	}

	private void addInviteCount(GameCount gameCount) {
		if (gameCount != null && gameCount.inviteCount > 0) {
			CountLabel countLabel = new CountLabel(gameCount.inviteCount, resources.fontShader, (UISkin) resources.skin);
			countLabel.setX((Gdx.graphics.getWidth() / 2) + (inviteLabel.getTextBounds().width * 0.6f));
			countLabel.setY(inviteLabel.getY() + (inviteLabel.getHeight() * 0.2f));

			stage.addActor(countLabel);
			actors.add(countLabel);
		}
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
	public void show(Stage stage, float width, float height) {
		this.stage = stage;
		actors.clear();

		waitImage = new WaitImageButton(resources.skin);
		float buttonWidth = .25f * (float) width;
		waitImage.setWidth(buttonWidth);
		waitImage.setHeight(buttonWidth);
		waitImage.setX(width / 2 - buttonWidth / 2);
		waitImage.setY(height / 2 - buttonWidth / 2);
		stage.addActor(waitImage);

		loadUser();
	}

	private void loadUser() {
		waitImage.start();
		gameAction.addFreeCoins(new UIConnectionResultCallback<Player>() {

			@Override
			public void onConnectionResult(final Player result) {
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
						addElementsToStage(result);
						waitImage.stop();
					};

					@Override
					public void onConnectionError(String msg) {
						addElementsToStage(null);
						waitImage.stop();
					}
				}, GameLoop.USER.handle);
			}

			@Override
			public void onConnectionError(String msg) {
				final Overlay ovrlay = new DismissableOverlay(resources, new TextOverlay(
						"Could not retrieve user.\n\nPlease try again.", resources), new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						loadUser();
					}
				});
				stage.addActor(ovrlay);
			}
		}, GameLoop.USER.handle);
	}

	@Override
	public void hide() {
		for (Actor actor : actors) {
			actor.remove();
		}
	}

	@Override
	public void resetState() {
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
}
