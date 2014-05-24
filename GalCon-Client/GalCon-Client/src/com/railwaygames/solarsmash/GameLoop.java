package com.railwaygames.solarsmash;

import static com.railwaygames.solarsmash.Util.createShader;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.railwaygames.solarsmash.config.Configuration;
import com.railwaygames.solarsmash.http.GameAction;
import com.railwaygames.solarsmash.http.InAppBillingAction;
import com.railwaygames.solarsmash.http.SocialAction;
import com.railwaygames.solarsmash.model.GameBoard;
import com.railwaygames.solarsmash.model.GameInviteRequest;
import com.railwaygames.solarsmash.model.Player;
import com.railwaygames.solarsmash.screen.Action;
import com.railwaygames.solarsmash.screen.BoardScreen;
import com.railwaygames.solarsmash.screen.FriendScreen;
import com.railwaygames.solarsmash.screen.MenuScreenContainer;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.SetGameBoardResultHandler;
import com.railwaygames.solarsmash.screen.widget.ShaderTextField.OnscreenKeyboard;

public class GameLoop extends Game {
	public static Player USER;
	public static Configuration CONFIG;

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

	public void refresh() {
		final Screen screen = getScreen();
		if (screen instanceof ScreenFeedback) {
			Gdx.app.postRunnable(new Runnable() {
				public void run() {
					((ScreenFeedback) screen).refresh();
				};
			});
		}
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

		assetManager.load("data/images/gameBoard.atlas", TextureAtlas.class);
		assetManager.load("data/images/levels.atlas", TextureAtlas.class);
		assetManager.load("data/images/levelSelection.atlas", TextureAtlas.class);
		assetManager.load("data/images/menus.atlas", TextureAtlas.class);
		assetManager.load("data/images/planets.atlas", TextureAtlas.class);
		assetManager.load("data/images/social.atlas", TextureAtlas.class);

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
		resources.planetAtlas = assetManager.get("data/images/planets.atlas", TextureAtlas.class);
		resources.fontShader = createShader("data/shaders/font-vs.glsl", "data/shaders/font-fs.glsl");

		Fonts.dispose();
		
		boardScreen = new BoardScreen(resources);
		friendScreen = new FriendScreen(resources, socialAction, gameAction);
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
					} else if(action.split(":")[0].equals(Action.PRACTICE)){
						String level = action.split(":")[1];
						gameAction.practiceGame(new SetGameBoardResultHandler(boardScreen), GameLoop.USER.handle, Long.valueOf(level));
						openBoardScreen();
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
}
