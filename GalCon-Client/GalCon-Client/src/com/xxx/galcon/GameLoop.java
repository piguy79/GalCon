package com.xxx.galcon;

import static com.xxx.galcon.Util.createShader;

import org.joda.time.DateTime;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.xxx.galcon.config.Configuration;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.InAppBillingAction;
import com.xxx.galcon.http.SetPlayerResultHandler;
import com.xxx.galcon.http.SocialAction;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.GameInviteRequest;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.screen.Action;
import com.xxx.galcon.screen.BoardScreen;
import com.xxx.galcon.screen.FriendScreen;
import com.xxx.galcon.screen.MenuScreenContainer;
import com.xxx.galcon.screen.Resources;
import com.xxx.galcon.screen.SetGameBoardResultHandler;
import com.xxx.galcon.screen.widget.ShaderTextField.OnscreenKeyboard;

public class GameLoop extends Game {
	public static Player USER;
	public static Configuration CONFIG;

	private InGameInputProcessor inputProcessor = new InGameInputProcessor();

	private GL20 gl;
	public AssetManager assetManager = new AssetManager();
	public UISkin skin = new UISkin();
	public TweenManager tweenManager;

	private MenuScreenContainer menuScreenContainer;
	private BoardScreen boardScreen;
	private FriendScreen friendScreen;

	private GameAction gameAction;
	private SocialAction socialAction;
	private InAppBillingAction inAppBillingAction;

	private boolean loadingNewCoins = false;

	private OnscreenKeyboard keyboard;

	public GameLoop(GameAction gameAction, SocialAction socialAction, InAppBillingAction inAppBillingAction,
			OnscreenKeyboard keyboard) {
		this.gameAction = gameAction;
		this.socialAction = socialAction;
		this.inAppBillingAction = inAppBillingAction;
		this.keyboard = keyboard;

		UIConnectionWrapper.setGameAction(gameAction);
		ExternalActionWrapper.setActions(gameAction, inAppBillingAction);
		tweenManager = new TweenManager();

		Player player = new Player();
		GameLoop.USER = player;

		gameAction.setGameLoop(this);
	}

	@Override
	public void pause() {
		super.pause();
	}

	@Override
	public void resume() {
		super.resume();
	}

	@Override
	public void create() {
		Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);

		/*
		 * Assume OpenGL ES 2.0 support has been validated by platform specific
		 * file.
		 */
		gl = Gdx.graphics.getGL20();
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glEnable(GL20.GL_DEPTH_BUFFER_BIT);

		Gdx.input.setCatchBackKey(true);

		Gdx.input.setInputProcessor(inputProcessor);

		assetManager.load("data/images/gameBoard.atlas", TextureAtlas.class);
		assetManager.load("data/images/levels.atlas", TextureAtlas.class);
		assetManager.load("data/images/levelSelection.atlas", TextureAtlas.class);
		assetManager.load("data/images/menus.atlas", TextureAtlas.class);
		assetManager.load("data/images/planets.atlas", TextureAtlas.class);
		assetManager.load("data/images/social.atlas", TextureAtlas.class);

		assetManager.load("data/fonts/planet_numbers.png", Texture.class);

		assetManager.finishLoading();

		skin.initialize(assetManager);

		Tween.setCombinedAttributesLimit(4);

		Resources resources = new Resources();
		resources.skin = skin;
		resources.assetManager = assetManager;
		resources.levelAtlas = assetManager.get("data/images/levels.atlas", TextureAtlas.class);
		resources.gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);
		resources.menuAtlas = assetManager.get("data/images/menus.atlas", TextureAtlas.class);
		resources.levelSelectionAtlas = assetManager.get("data/images/levelSelection.atlas", TextureAtlas.class);
		resources.fontShader = createShader("data/shaders/font-vs.glsl", "data/shaders/font-fs.glsl");

		boardScreen = new BoardScreen(resources, tweenManager);
		friendScreen = new FriendScreen(skin, assetManager, socialAction, gameAction);
		menuScreenContainer = new MenuScreenContainer(resources, socialAction, gameAction, inAppBillingAction,
				tweenManager, keyboard);
		setScreen(menuScreenContainer);
	}

	public void reset() {
		menuScreenContainer.resetState();
		setScreen(menuScreenContainer);
	}

	@Override
	public void render() {
		super.render();
		checkCoinStats();

		tweenManager.update(Gdx.graphics.getDeltaTime());

		Object result = ((ScreenFeedback) getScreen()).getRenderResult();
		if (result != null) {
			if (getScreen() instanceof MenuScreenContainer) {
				if (result instanceof String) {
					String action = (String) result;
					if (action.split(":")[0].equals(Action.PLAY)) {
						String level = action.split(":")[1];
						gameAction.matchPlayerToGame(new SetGameBoardResultHandler(boardScreen), GameLoop.USER.handle,
								Long.valueOf(level));
						openBoardScreen();
					} else if (action.split(":")[0].equals(Action.PLAY_WITH_FRIENDS)) {
						friendScreen.setPreviousScreen((MenuScreenContainer) getScreen());
						friendScreen.setMapType(action.split(":")[1]);
						setScreen(friendScreen);
					}
				} else if (result instanceof GameBoard) {
					boardScreen.resetState();
					boardScreen.setGameBoard((GameBoard) result);
					openBoardScreen();
				}
			} else if (getScreen() instanceof BoardScreen) {
				String action = (String) result;
				if (action.equals(Action.BACK)) {
					boardScreen.resetState();
					boardScreen.getPreviousScreen().resetState();
					setScreen(boardScreen.getPreviousScreen());
				}
			} else if (getScreen() instanceof FriendScreen) {
				String action = (String) result;
				if (action.equals(Action.BACK)) {
					friendScreen.resetState();
					friendScreen.getPreviousScreen().resetState();
					setScreen(friendScreen.getPreviousScreen());
				}
				if (action.equals(Action.INVITE_PLAYER)) {
					boardScreen.setPreviousScreen(friendScreen.getPreviousScreen());
					GameInviteRequest gameInviteRequest = friendScreen.getGameInviteRequest();
					friendScreen.resetState();
					gameAction.invitePlayerForGame(new SetGameBoardResultHandler(boardScreen),
							gameInviteRequest.requesterHandle, gameInviteRequest.inviteeHandle,
							gameInviteRequest.mapKey);
					setScreen(boardScreen);
				}
			}
		}
	}

	private void openBoardScreen() {
		boardScreen.setPreviousScreen((MenuScreenContainer) getScreen());
		setScreen(boardScreen);
	}

	private void checkCoinStats() {
		if (GameLoop.USER != null && GameLoop.USER.coins != null && GameLoop.CONFIG != null) {
			DateTime timeRemaining = GameLoop.USER.timeRemainingUntilCoinsAvailable();

			if (timeRemaining != null) {
				loadingNewCoins = false;
			} else if (timeRemaining == null && GameLoop.USER.coins == 0 && GameLoop.USER.usedCoins != -1) {
				if (!loadingNewCoins) {
					loadingNewCoins = true;
					gameAction.addFreeCoins(new SetPlayerResultHandler(GameLoop.USER), GameLoop.USER.handle);
				}
			} else {
				loadingNewCoins = false;
			}
		}
	}
}
