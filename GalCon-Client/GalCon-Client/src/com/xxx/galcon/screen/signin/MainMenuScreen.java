package com.xxx.galcon.screen.signin;

import org.joda.time.DateTime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.PartialScreenFeedback;
import com.xxx.galcon.Strings;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.config.ConfigResolver;
import com.xxx.galcon.http.AuthenticationListener;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.SocialAction;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.GameCount;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.screen.Action;
import com.xxx.galcon.screen.GraphicsUtils;
import com.xxx.galcon.screen.Resources;
import com.xxx.galcon.screen.overlay.DismissableOverlay;
import com.xxx.galcon.screen.overlay.Overlay;
import com.xxx.galcon.screen.overlay.TextOverlay;
import com.xxx.galcon.screen.widget.ActorBar;
import com.xxx.galcon.screen.widget.CountLabel;
import com.xxx.galcon.screen.widget.ShaderLabel;
import com.xxx.galcon.screen.widget.WaitImageButton;

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
	private ImageButton fbButton;
	private ImageButton gpButton;

	public MainMenuScreen(Resources resources, GameAction gameAction, SocialAction socialAction) {
		this.gameAction = gameAction;
		this.socialAction = socialAction;
		this.resources = resources;
	}

	private void addElementsToStage(GameCount gameCount) {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();

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
				startHideSequence(Strings.NEW);
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
				startHideSequence(Strings.INVITES);
			}
		});
		stage.addActor(inviteLabel);
		actors.add(inviteLabel);

		ImageButton coinImage = new ImageButton(resources.skin, Constants.UI.COIN);
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

		addSocialButtonBar();

		addContinueCount(gameCount);
		addInviteCount(gameCount);
	}

	private void addSocialButtonBar() {
		createFbButton();
		createGpButton();

		float buttonWidth = Gdx.graphics.getWidth() * 0.2f;
		float buttonHeight = Gdx.graphics.getHeight() * 0.15f;

		ActorBar buttonBar = new ActorBar.ActorBarBuilder(Gdx.graphics.getHeight() * 0.1f, Gdx.graphics.getWidth() * 0.6f)
								.actorSize(buttonHeight, buttonWidth).actorPadding(buttonWidth * 0.1f)
								.align(ActorBar.Align.RIGHT).addActor(fbButton).addActor(gpButton).build();
		buttonBar.setX(Gdx.graphics.getWidth() * 0.4f);
		buttonBar.setY(Gdx.graphics.getHeight() - (buttonBar.getHeight() * 1.1f));

		stage.addActor(buttonBar);
		actors.add(buttonBar);

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

	private void createGpButton() {
		gpButton = new ImageButton(resources.skin, Constants.UI.GOOGLE_PLUS_SIGN_IN_NORMAL);
		gpButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				registerSocialProvider(Constants.Auth.SOCIAL_AUTH_PROVIDER_GOOGLE);
			}
		});
	}

	private void createFbButton() {
		fbButton = new ImageButton(resources.skin, Constants.UI.FACEBOOK_SIGN_IN_BUTTON);

		fbButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				registerSocialProvider(Constants.Auth.SOCIAL_AUTH_PROVIDER_FACEBOOK);
			}

		});
	}

	private void registerSocialProvider(String authProvider) {
		final TextOverlay overlay = new TextOverlay("Adding " + authProvider + " access.", resources);
		stage.addActor(overlay);
		socialAction.addAuthDetails(new AuthenticationListener() {

			@Override
			public void onSignOut() {
			}

			@Override
			public void onSignInSucceeded(String authProvider, String token) {
				overlay.addAction(Actions.sequence(Actions.delay(0.4f), Actions.run(new Runnable() {

					@Override
					public void run() {
						overlay.remove();

					}
				})));
				Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
				String id = prefs.getString(authProvider + Constants.ID);
				prefs.flush();

				gameAction.addProviderToUser(new UIConnectionResultCallback<Player>() {
					@Override
					public void onConnectionResult(Player result) {
						GameLoop.USER = result;
					}

					@Override
					public void onConnectionError(String msg) {

					}
				}, GameLoop.USER.handle, id, authProvider);

			}

			@Override
			public void onSignInFailed(String failureMessage) {
				overlay.addAction(Actions.sequence(Actions.delay(0.4f), Actions.run(new Runnable() {

					@Override
					public void run() {
						overlay.remove();

					}
				})));
			}
		}, authProvider);
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
		String coinsText = "";

		DateTime timeRemaining = GameLoop.USER.timeRemainingUntilCoinsAvailable();

		if (timeRemaining != null) {
			coinsText += timeRemaining.getMinuteOfHour() + ":" + timeRemaining.getSecondOfMinute();
		} else {
			coinsText += GameLoop.USER.coins;
		}

		return coinsText;
	}

	private boolean hasAppConfigInformation() {
		return GameLoop.CONFIG.configValues != null;
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
		gameAction.recoverUsedCoinCount(new UIConnectionResultCallback<Player>() {

			@Override
			public void onConnectionResult(Player result) {
				GameLoop.USER = result;
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
		return false;
	}
}
