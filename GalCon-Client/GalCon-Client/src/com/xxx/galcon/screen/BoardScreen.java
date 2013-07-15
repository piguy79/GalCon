package com.xxx.galcon.screen;

import static com.xxx.galcon.Constants.CONNECTION_ERROR_MESSAGE;
import static com.xxx.galcon.Constants.GALCON_PREFS;
import static com.xxx.galcon.Constants.OWNER_NO_ONE;
import static com.xxx.galcon.Constants.PLANET_ABILITIES;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.loaders.wavefront.ObjLoader;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.xxx.galcon.Constants;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.InGameInputProcessor;
import com.xxx.galcon.InGameInputProcessor.TouchPoint;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.math.GalConMath;
import com.xxx.galcon.math.WorldMath;
import com.xxx.galcon.model.EndGameInformation;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.model.factory.MoveFactory;
import com.xxx.galcon.model.tween.MoveTween;
import com.xxx.galcon.model.tween.ShipSelectionDialogTween;
import com.xxx.galcon.screen.hud.BoardScreenHud;
import com.xxx.galcon.screen.hud.HeaderHud;
import com.xxx.galcon.screen.overlay.DismissableOverlay;
import com.xxx.galcon.screen.overlay.Overlay;
import com.xxx.galcon.screen.overlay.PostDismissAction;
import com.xxx.galcon.screen.overlay.TextOverlay;

public class BoardScreen implements ScreenFeedback, ContactListener {
	private static final int INDEX_PLANET_OWNED_BY_USER = 0;
	private static final int INDEX_PLANET_OWNED_BY_ENEMY = 1;
	private static final int INDEX_PLANET_TOUCHED = 2;
	private static final int INDEX_PLANET_ABILITY = 3;

	private static final float BOARD_WIDTH_RATIO = .98f;
	private static final float BOARD_HEIGHT_RATIO = .76f;
	private static final float BOARD_TOP_OFFSET = 0.1f;

	private static final float PLANET_Z_COORD = -99.5f;
	private static final float TILE_SIZE_IN_UNITS = 10.0f;

	private static final String TOUCH_OBJECT = "touch";

	private int roundAnimated = -2;

	private Camera camera;
	private GameBoard gameBoard;
	private World physicsWorld;

	private ShaderProgram planetShader;
	private ShaderProgram gridShader;
	private ShaderProgram shipShader;
	private ShaderProgram overlayShader;

	private Texture planetNumbersTexture;
	private Texture planetTexture;
	private Texture planetTouchTexture;
	private Texture bg1Texture;

	private StillModel planetModel;
	private StillModel shipModel;
	private Matrix4 modelViewMatrix = new Matrix4();
	private final Matrix4 fontViewMatrix = new Matrix4();

	private BoardPlane boardPlane = new BoardPlane();
	private WorldPlane worldPlane = new WorldPlane();

	List<Planet> touchedPlanets = new ArrayList<Planet>(2);
	List<Planet> moveSelectedPlanets = new ArrayList<Planet>(2);
	List<Move> inProgressMoves = new ArrayList<Move>();

	private float[] moveSelectedPlanetsCoords = new float[4];

	private AssetManager assetManager;
	private TweenManager tweenManager;
	private BoardScreenHud boardScreenHud;
	private HeaderHud playerInfoHud;
	private ShipSelectionDialog shipSelectionDialog;
	private Overlay overlay;

	public ScreenFeedback previousScreen;

	boolean intro = true;
	float introTimeBegin = 0.0f;
	float introElapsedTime = 2.8f;

	private String connectionError;

	private String returnCode = null;
	private MoveFactory moveFactory;

	public BoardScreen(AssetManager assetManager, TweenManager tweenManager) {
		this.assetManager = assetManager;
		this.tweenManager = tweenManager;

		planetShader = createShaderUsingFiles("data/shaders/planet-vs.glsl", "data/shaders/planet-fs.glsl");
		gridShader = createShaderUsingFiles("data/shaders/grid-vs.glsl", "data/shaders/grid-fs.glsl");
		shipShader = createShaderUsingFiles("data/shaders/ship-vs.glsl", "data/shaders/ship-fs.glsl");
		overlayShader = createShaderUsingFiles("data/shaders/overlay-vs.glsl", "data/shaders/overlay-fs.glsl");

		planetModel = generateStillModelFromObjectFile("data/models/planet.obj");
		shipModel = generateStillModelFromObjectFile("data/models/ship.obj");

		planetNumbersTexture = assetManager.get("data/fonts/planet_numbers.png", Texture.class);
		planetTexture = assetManager.get("data/images/planets/planet3.png", Texture.class);
		planetTouchTexture = assetManager.get("data/images/planets/planet3-touch.png", Texture.class);
		bg1Texture = assetManager.get("data/images/bg1.jpg", Texture.class);

		boardScreenHud = new BoardScreenHud(this, assetManager);
		playerInfoHud = new HeaderHud(assetManager);

		physicsWorld = new World(new Vector2(0.0f, 0.0f), true);
		physicsWorld.setContactListener(this);

		this.moveFactory = new MoveFactory();

		Tween.registerAccessor(Move.class, new MoveTween());
		Tween.registerAccessor(ShipSelectionDialog.class, new ShipSelectionDialogTween());

	}

	private StillModel generateStillModelFromObjectFile(String objectFile) {
		ObjLoader loader = new ObjLoader();
		return loader.loadObj(Gdx.files.internal(objectFile));
	}

	private ShaderProgram createShaderUsingFiles(String vertexShader, String fragmentShader) {
		ShaderProgram shader = new ShaderProgram(Gdx.files.internal(vertexShader), Gdx.files.internal(fragmentShader));
		if (!shader.isCompiled() && !shader.getLog().isEmpty()) {
			throw new IllegalStateException("Shader compilation fail: " + shader.getLog());
		}

		return shader;
	}

	/*
	 * All coordinates are assumed to be on a plane at Z
	 */
	public class BoardPlane {
		public float topInWorld;
		public float leftInWorld;
		public float widthInWorld;
		public float heightInWorld;
		public float yShift;

		private Matrix4 modelViewMatrix = new Matrix4();

		public void resize() {
			widthInWorld = (worldPlane.bottomRight.x - worldPlane.topLeft.x) * BOARD_WIDTH_RATIO;
			heightInWorld = (worldPlane.topLeft.y - worldPlane.bottomRight.y) * BOARD_HEIGHT_RATIO;

			yShift = worldPlane.topLeft.y - boardPlane.heightInWorld / 2.0f
					- (BOARD_TOP_OFFSET * (worldPlane.topLeft.y - worldPlane.bottomRight.y));

			topInWorld = -heightInWorld / 2.0f + yShift;
			leftInWorld = -widthInWorld / 2.0f;

			modelViewMatrix.idt();
			modelViewMatrix.scale(boardPlane.widthInWorld / TILE_SIZE_IN_UNITS, boardPlane.heightInWorld
					/ TILE_SIZE_IN_UNITS, 1.0f);
			modelViewMatrix.trn(0.0f, boardPlane.yShift, WorldPlane.Z);
		}

		/**
		 * 0,0 is in the center of the top left tile, hence -0.5, -0.5 is the
		 * far left corner. Same logic applies to bottom right
		 */
		public Vector2 worldXYToBoardXY(float worldX, float worldY, GameBoard gameBoard) {
			float x = (worldX - leftInWorld) / widthInWorld;
			if (x < 0 || x > 1) {
				return null;
			}

			float y = (worldY - topInWorld) / heightInWorld;
			if (y < 0 || y > 1) {
				return null;
			}
			y = 1.0f - y;

			x *= gameBoard.widthInTiles;
			y *= gameBoard.heightInTiles;

			x -= 0.5f;
			y -= 0.5f;

			return new Vector2(x, y);
		}
	}

	/**
	 * (0,0) is in the middle of the screen... +y is UP, +x is right.
	 */
	public class WorldPlane {
		public static final float Z = -100.0f;
		public Vector3 topLeft;
		public Vector3 bottomRight;

		public void resize(Camera camera) {
			Vector2 worldXY = WorldMath.screenXYToWorldXY(camera, 0, 0);
			topLeft = new Vector3(worldXY.x, worldXY.y, WorldPlane.Z);

			worldXY = WorldMath.screenXYToWorldXY(camera, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			bottomRight = new Vector3(worldXY.x, worldXY.y, WorldPlane.Z);
		}
	}

	public void setGameBoard(GameBoard gameBoard) {
		this.gameBoard = gameBoard;
		processGameBoard();
		associateHudInformation();
	}

	private void associateHudInformation() {
		boardScreenHud.associateCurrentRoundInformation(gameBoard);
		playerInfoHud.associateCurrentRoundInformation(gameBoard);
	}

	private List<Body> bodyClearList = new ArrayList<Body>();

	private void clearPhysicsWorld() {
		bodyClearList.clear();
		Iterator<Body> bodyIter = physicsWorld.getBodies();
		while (bodyIter.hasNext()) {
			Body body = bodyIter.next();
			bodyClearList.add(body);
		}

		for (Body body : bodyClearList) {
			body.setUserData(null);
			physicsWorld.destroyBody(body);
		}
	}

	private void processGameBoard() {
		clearPhysicsWorld();

		for (Planet planet : gameBoard.planets) {
			addPhysicsToPlanet(planet);
		}

	}

	private Body handleTouch(Camera camera) {
		if (gameBoard == null || overlay != null) {
			return null;
		}

		if (GameLoop.USER.hasMoved(gameBoard)) {
			return null;
		}

		Body contactBody = null;
		InGameInputProcessor ip = (InGameInputProcessor) Gdx.input.getInputProcessor();
		if (ip.didTouch()) {
			TouchPoint touchPoint = ip.getTouch();
			if (shipSelectionDialog != null) {
				int x = touchPoint.x;
				int y = Gdx.graphics.getHeight() - touchPoint.y;
				if (shipSelectionDialog.contains(x, y)) {
					return null;
				} else {
					clearTouchedPlanets();
					shipSelectionDialog.dispose();
					shipSelectionDialog = null;
				}
			} else {
				Vector2 worldXY = WorldMath.screenXYToWorldXY(camera, touchPoint.x, touchPoint.y);
				Vector2 boardXY = boardPlane.worldXYToBoardXY(worldXY.x, worldXY.y, gameBoard);

				if (boardXY != null) {
					BodyDef bodyDef = new BodyDef();
					bodyDef.type = BodyDef.BodyType.DynamicBody;
					bodyDef.position.set(boardXY);

					contactBody = physicsWorld.createBody(bodyDef);
					contactBody.setUserData(TOUCH_OBJECT);

					CircleShape shape = new CircleShape();
					shape.setRadius(0.16f);

					FixtureDef fixtureDef = new FixtureDef();
					fixtureDef.shape = shape;
					contactBody.createFixture(fixtureDef);

					ip.consumeTouch();
				}
			}
		}

		return contactBody;
	}

	private float[] touchedPlanetsCoords = new float[4];

	private void renderGrid(Camera camera) {
		bg1Texture.bind(1);

		gridShader.begin();
		gridShader.setUniformi("bgTex", 1);

		gridShader.setUniformMatrix("uPMatrix", camera.combined);
		gridShader.setUniformMatrix("uMVMatrix", boardPlane.modelViewMatrix);
		gridShader.setUniformf("uTilesWide", gameBoard.widthInTiles);
		gridShader.setUniformf("uTilesTall", gameBoard.heightInTiles);

		touchedPlanetsCoords[0] = -1;
		touchedPlanetsCoords[1] = -1;
		touchedPlanetsCoords[2] = -1;
		touchedPlanetsCoords[3] = -1;

		int size = touchedPlanets.size();
		if (size > 0) {
			Planet planet = touchedPlanets.get(0);
			touchedPlanetsCoords[0] = planet.position.x;
			touchedPlanetsCoords[1] = planet.position.y;

			if (size > 1) {

				planet = touchedPlanets.get(1);
				touchedPlanetsCoords[2] = planet.position.x;
				touchedPlanetsCoords[3] = planet.position.y;
			}
		}
		gridShader.setUniform1fv("uTouchPlanetsCoords", touchedPlanetsCoords, 0, 4);

		planetModel.render(planetShader);

		gridShader.end();
	}

	private float[] planetBits = new float[4];

	private void renderPlanets(List<Planet> planets, Camera camera) {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		planetTexture.bind(1);
		planetTouchTexture.bind(2);
		planetNumbersTexture.bind(3);

		planetShader.begin();
		planetShader.setUniformi("planetTex", 1);
		planetShader.setUniformi("planetTouchTex", 2);
		planetShader.setUniformi("numbersTex", 3);

		planetShader.setUniformMatrix("uPMatrix", camera.combined);
		int size = planets.size();
		for (int i = 0; i < size; ++i) {
			Planet planet = planets.get(i);
			modelViewMatrix.idt();
			modelViewMatrix.trn(-boardPlane.widthInWorld / 2, (boardPlane.heightInWorld / 2) + boardPlane.yShift,
					PLANET_Z_COORD);

			float tileWidthInWorld = boardPlane.widthInWorld / gameBoard.widthInTiles;
			float tileHeightInWorld = boardPlane.heightInWorld / gameBoard.heightInTiles;
			modelViewMatrix.trn(tileWidthInWorld * planet.position.x + tileWidthInWorld / 2, -tileHeightInWorld
					* planet.position.y - tileHeightInWorld / 2, 0.0f);

			float radius = scaleRegenToRadius(planet, 0.25f, 0.48f);

			modelViewMatrix.scale(tileWidthInWorld / TILE_SIZE_IN_UNITS, tileHeightInWorld / TILE_SIZE_IN_UNITS, 1.0f);
			modelViewMatrix.scl(radius * 2.2f, radius * 2.2f, 1.0f);

			planetShader.setUniformMatrix("uMVMatrix", modelViewMatrix);

			setPlanetBits(planet, planetBits);
			planetShader.setUniform1fv("uPlanetBits", planetBits, 0, 4);
			planetShader.setUniformf("radius", radius);
			planetShader.setUniformi("shipCount", planet.numberOfShipsToDisplay(gameBoard));
			planetModel.render(planetShader);
		}
		planetShader.end();

		Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);

		Gdx.gl.glDisable(GL20.GL_BLEND);
	}

	private float scaleRegenToRadius(Planet planet, float minRadius, float maxRadius) {
		float regenRatio = planet.shipRegenRate / Constants.SHIP_REGEN_RATE_MAX;
		return minRadius + (maxRadius - minRadius) * regenRatio;
	}

	private void setPlanetBits(Planet planet, float[] planetBits) {
		
		String planetOwner = planet.owner;
		if(planet.isBeingAttacked(gameBoard)){
			planetOwner = planet.previousRoundOwner(gameBoard);
		}
		
		planetBits[INDEX_PLANET_TOUCHED] = planet.touched ? 1.0f : 0.0f;
		planetBits[INDEX_PLANET_ABILITY] = planet.hasAbility() ? 1.0f : 0.0f;
		planetBits[INDEX_PLANET_OWNED_BY_USER] = planetOwner.equals(GameLoop.USER.handle) ? 1.0f : 0.0f;
		planetBits[INDEX_PLANET_OWNED_BY_ENEMY] = !planetOwner.equals(OWNER_NO_ONE)
				&& !planetOwner.equals(GameLoop.USER.handle) ? 1.0f : 0.0f;
	}

	private void renderShips(List<Planet> planets, List<Move> moves, Camera camera) {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		float tileWidthInWorld = boardPlane.widthInWorld / gameBoard.widthInTiles;
		float tileHeightInWorld = boardPlane.heightInWorld / gameBoard.heightInTiles;

		shipShader.begin();

		for (int i = 0; i < moves.size(); i++) {
			Move move = moves.get(i);

			if (!move.belongsToPlayer(GameLoop.USER)) {
				continue;
			}

			modelViewMatrix.idt();
			modelViewMatrix.trn(-boardPlane.widthInWorld / 2, (boardPlane.heightInWorld / 2) + boardPlane.yShift,
					PLANET_Z_COORD);

			if (!move.animation.isStarted()) {
				move.animation.start(tweenManager);
			}

			float xToDraw = move.currentAnimation.x, yToDraw = move.currentAnimation.y;

			if (roundAnimated == gameBoard.roundInformation.currentRound) {
				xToDraw = move.currentPosition.x;
				yToDraw = move.currentPosition.y;
				if (move.executed) {
					continue;
				}
			} else if (move.animation.isFinished()) {
				roundAnimated = gameBoard.roundInformation.currentRound;

			}

			modelViewMatrix.trn(tileWidthInWorld * xToDraw + tileWidthInWorld / 2, -tileHeightInWorld * yToDraw
					- tileHeightInWorld / 2, 0.0f);

			modelViewMatrix.rotate(0, 0, 1, 180 - move.angleOfMovement());

			float r = 1.0f, g = 0.0f, b = 0.0f;
			float scalingFactor = 8.0f;
			if (move.executed) {
				float distance = distanceToPlanet(move, gameBoard.planets);

				scalingFactor = scalingFactor / distance;
			}
			modelViewMatrix.scale(tileWidthInWorld / scalingFactor, tileHeightInWorld / scalingFactor, 1.0f);

			Move selectedMove = gameBoard.selectedMove();

			shipShader.setUniformMatrix("uPMatrix", camera.combined);
			shipShader.setUniformMatrix("uMVMatrix", modelViewMatrix);
			if (selectedMove != null && selectedMove.hashCode() != move.hashCode()) {
				selectedMove.selected += Gdx.graphics.getDeltaTime();
				r -= 0.6f * selectedMove.selected;

				g -= 0.8f *  selectedMove.selected;
				b -= 0.8f *  selectedMove.selected;
				
				if(r < 0.4){
					r = 0.4f;
					g = 0f;
					b = 0f;
				}
				shipShader.setUniformf("uColor", r, g, b, 1.0f);
			} else {
				shipShader.setUniformf("uColor", r, g, b, 1.0f);

			}

			shipModel.render(shipShader);
		}

		shipShader.end();

		Gdx.gl.glDisable(GL20.GL_BLEND);
	}

	private float distanceToPlanet(Move move, List<Planet> planets) {
		for (Planet planet : planets) {
			if (planet.name.equals(move.toPlanet)) {
				return GalConMath.distance(move.currentAnimation.x, move.currentAnimation.y, planet.position.x,
						planet.position.y);
			}
		}
		return 0;
	}

	private void addPhysicsToPlanet(Planet planet) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		bodyDef.position.set(new Vector2(planet.position.x, planet.position.y));

		Body characterBody = physicsWorld.createBody(bodyDef);
		characterBody.setUserData(planet);

		CircleShape shape = new CircleShape();
		shape.setRadius((float) 0.45f * (planet.shipRegenRate / Constants.SHIP_REGEN_RATE_MAX));

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		characterBody.createFixture(fixtureDef);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void beginContact(Contact contact) {
		if (gameBoard == null) {
			return;
		}

		Object userDataOne = contact.getFixtureA().getBody().getUserData();
		Object userDataTwo = contact.getFixtureB().getBody().getUserData();

		Planet planet = null;
		if (userDataOne instanceof Planet) {
			planet = (Planet) userDataOne;
		}
		if (userDataTwo instanceof Planet) {
			planet = (Planet) userDataTwo;
		}

		if (planet != null) {
			planet.touched = !planet.touched;
		}

		if (planet.touched) {
			if (touchedPlanets.size() == 1) {
				Planet alreadySelectedPlanet = touchedPlanets.get(0);
				if (!planet.isOwnedBy(GameLoop.USER) && !alreadySelectedPlanet.owner.equals(GameLoop.USER.handle)) {
					planet.touched = false;
				} else {
					touchedPlanets.add(planet);
					showShipSelectionDialog(touchedPlanets);
				}
			} else if (touchedPlanets.size() == 2) {
				planet.touched = false;
			} else {
				touchedPlanets.add(planet);
			}
		}
		if (!planet.touched) {
			touchedPlanets.remove(planet);
		}
	}

	@Override
	public void endContact(Contact contact) {

	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {

	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {

	}

	private void showShipSelectionDialog(Move moveToEdit, List<Planet> touchedPlanets) {
		int shipsOnPlanet = 0;
		for (Planet planet : touchedPlanets) {
			if (planet.isOwnedBy(GameLoop.USER)) {
				shipsOnPlanet = planet.numberOfShips;
				break;
			}
		}

		if (moveToEdit != null) {
			shipsOnPlanet = moveToEdit.shipsToMove + gameBoard.getPlanet(moveToEdit.fromPlanet).numberOfShips;
		}

		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();
		int xMargin = (int) (width * .15f);
		int dialogWidth = width - 2 * xMargin;

		shipSelectionDialog = new ShipSelectionDialog(moveToEdit, (int) (width * -1), (int) (height * .6f),
				dialogWidth, (int) (dialogWidth * .8f), assetManager, shipsOnPlanet, tweenManager, touchedPlanets);
	}

	private void showShipSelectionDialog(List<Planet> touchedPlanets) {
		this.showShipSelectionDialog(null, touchedPlanets);
	}

	private BitmapFont fpsFont = new BitmapFont();
	private SpriteBatch fpsSpriteBatch = new SpriteBatch();

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		if (gameBoard == null) {
			renderLoadingText();
			return;
		}

		Body contactBody = handleTouch(camera);

		physicsWorld.step(1.0f / 60.0f, 6, 2);

		if (contactBody != null) {
			physicsWorld.destroyBody(contactBody);
		}

		renderGrid(camera);
		renderPlanets(gameBoard.planets, camera);
		renderOverlay(camera);
		renderShips(gameBoard.planets, gameBoard.movesInProgress, camera);

		if (gameBoard.hasWinner()) {
			displayWinner(gameBoard.endGameInformation);
		} else if (gameBoard.wasADraw()) {
			displayDraw();
		}

		if (overlay == null) {
			List<String> ownedPlanetAbilities = gameBoard.ownedPlanetAbilities(GameLoop.USER);
			for (int i = 0; i < ownedPlanetAbilities.size(); ++i) {
				String ability = ownedPlanetAbilities.get(i);

				Preferences prefs = Gdx.app.getPreferences(GALCON_PREFS);
				if (!prefs.getBoolean(ability + "_SHOWN", false)) {
					overlay = new DismissableOverlay(assetManager, new TextOverlay(
							"Congrats!\n \nWhile you hold this planet,\nyou will gain the following:\n"
									+ PLANET_ABILITIES.get(ability), assetManager, true));
					prefs.putBoolean(ability + "_SHOWN", true);
					prefs.flush();
					break;
				}
			}
		}

		boardScreenHud.setTouchEnabled(true);
		if (overlay != null) {
			boardScreenHud.setTouchEnabled(false);
		}

		boardScreenHud.render(delta);
		playerInfoHud.setGameBoard(gameBoard);
		playerInfoHud.render(delta);

		renderDialogs(delta);

		String hudResult = (String) boardScreenHud.getRenderResult();
		if (hudResult != null) {
			processHudButtonTouch(hudResult);
		}

		hudResult = (String) playerInfoHud.getRenderResult();
		if (hudResult != null) {
			processHudButtonTouch(hudResult);
		}

		if (overlay != null) {
			if (overlay instanceof DismissableOverlay && ((DismissableOverlay) overlay).isDismissed()) {
				overlay = null;
			} else {
				overlay.render(delta);
			}
		}

		fpsSpriteBatch.begin();
		fpsSpriteBatch.setColor(Color.WHITE);

		fpsFont.draw(fpsSpriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), (int) 10, (int) 50);
		fpsSpriteBatch.end();
	}

	private void renderOverlay(Camera camera) {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		if(gameBoard.selectedMove() == null){
			return;
		}

		overlayShader.begin();

		overlayShader.setUniformMatrix("uPMatrix", camera.combined);
		overlayShader.setUniformMatrix("uMVMatrix", boardPlane.modelViewMatrix);
		overlayShader.setUniformf("uTilesWide", gameBoard.widthInTiles);
		overlayShader.setUniformf("uTilesTall", gameBoard.heightInTiles);

		moveSelectedPlanetsCoords[0] = -1;
		moveSelectedPlanetsCoords[1] = -1;
		moveSelectedPlanetsCoords[2] = -1;
		moveSelectedPlanetsCoords[3] = -1;

		int size = moveSelectedPlanets.size();
		if (size > 0) {
			Planet planet = moveSelectedPlanets.get(0);
			moveSelectedPlanetsCoords[0] = planet.position.x;
			moveSelectedPlanetsCoords[1] = planet.position.y;

			if (size > 1) {

				planet = moveSelectedPlanets.get(1);
				moveSelectedPlanetsCoords[2] = planet.position.x;
				moveSelectedPlanetsCoords[3] = planet.position.y;
			}
		}
		overlayShader.setUniform1fv("uSelectedPlanetsCoords", moveSelectedPlanetsCoords, 0, 4);
		overlayShader.setUniformf("uDimmer", gameBoard.selectedMove() != null ? 1.0f : 0.0f);

		planetModel.render(overlayShader);

		overlayShader.end();

	}

	private SpriteBatch textSpriteBatch = new SpriteBatch();

	private void renderLoadingText() {
		float width = Gdx.graphics.getWidth() / 2;
		float height = Gdx.graphics.getHeight() / 2;

		textSpriteBatch.begin();
		fontViewMatrix.setToOrtho2D(0, 0, width, height);
		textSpriteBatch.setProjectionMatrix(fontViewMatrix);

		String text = connectionError != null ? connectionError : "Loading...";
		BitmapFont font = Fonts.getInstance().mediumFont();
		float halfFontWidth = font.getBounds(text).width / 2;
		font.draw(textSpriteBatch, text, width / 2 - halfFontWidth, height * .4f);
		textSpriteBatch.end();
	}

	private void displayDraw() {
		float width = Gdx.graphics.getWidth() / 2;
		float height = Gdx.graphics.getHeight() / 2;

		textSpriteBatch.begin();
		fontViewMatrix.setToOrtho2D(0, 0, width, height);
		textSpriteBatch.setProjectionMatrix(fontViewMatrix);

		String text = "Draw Game";

		BitmapFont font = Fonts.getInstance().mediumFont();
		float halfFontWidth = font.getBounds(text).width / 2;
		font.draw(textSpriteBatch, text, width / 2 - halfFontWidth, height * .25f);
		textSpriteBatch.end();
	}

	private void displayWinner(EndGameInformation endGameInfo) {
		float width = Gdx.graphics.getWidth() / 2;
		float height = Gdx.graphics.getHeight() / 2;

		textSpriteBatch.begin();
		fontViewMatrix.setToOrtho2D(0, 0, width, height);
		textSpriteBatch.setProjectionMatrix(fontViewMatrix);

		String text = "You Lost";
		if (GameLoop.USER.handle.equals(endGameInfo.winnerHandle)) {
			text = "Victory! Gained " + endGameInfo.xpAwardToWinner + "xp";
		}

		BitmapFont font = Fonts.getInstance().mediumFont();
		float halfFontWidth = font.getBounds(text).width / 2;
		font.draw(textSpriteBatch, text, width / 2 - halfFontWidth, height * .25f);
		textSpriteBatch.end();
	}

	private void renderDialogs(float delta) {
		if (shipSelectionDialog != null) {
			shipSelectionDialog.render(delta);

			if (!shipSelectionDialog.hideAnimation.isStarted() && touchedPlanets.size() == 1) {
				shipSelectionDialog.showAnimation.kill();
				TweenManager.setAutoStart(shipSelectionDialog.hideAnimation, true);
				shipSelectionDialog.hideAnimation.start();
			}

			String action = (String) shipSelectionDialog.getRenderResult();
			if (action != null) {
				processShipSelectionTouch(action);
			}

			if (shipSelectionDialog.hideAnimation.isFinished()) {
				shipSelectionDialog.dispose();
				shipSelectionDialog = null;
			}
		}
	}

	private void processShipSelectionTouch(String action) {
		if (action.equals(Action.DIALOG_OK)) {
			Move move = moveFactory.createMove(touchedPlanets, shipSelectionDialog.getShipsToSend());
			if (move != null) {
				inProgressMoves.add(move);
			}
		} else if (action.equals(Action.DIALOG_UPDATE)) {
			Move move = shipSelectionDialog.getCurrentMoveToEdit();

			int priorShipsToMove = move.shipsToMove;
			move.shipsToMove = shipSelectionDialog.getShipsToSend();

			Planet planet = gameBoard.getPlanet(move.fromPlanet);
			planet.numberOfShips += (priorShipsToMove - move.shipsToMove);
		} else if (action.equals(Action.DIALOG_DELETE)) {
			for (ListIterator<Move> iter = inProgressMoves.listIterator(); iter.hasNext();) {
				Move move = iter.next();
				if (move.equals(shipSelectionDialog.getCurrentMoveToEdit())) {
					Planet planet = gameBoard.getPlanet(move.fromPlanet);
					planet.numberOfShips += move.shipsToMove;
					iter.remove();
				}
			}
		}

		clearTouchedPlanets();
		TweenManager.setAutoStart(shipSelectionDialog.hideAnimation, true);
		shipSelectionDialog.hideAnimation.start(tweenManager);

		boardScreenHud.associateCurrentRoundInformation(gameBoard);
	}

	private void processHudButtonTouch(String action) {
		if (action.equals(Action.END_TURN)) {
			overlay = new TextOverlay("Sending ships to their doom", assetManager, true);
			UIConnectionWrapper.performMoves(new PerformMoveResultHandler(), gameBoard.id, inProgressMoves);
		} else if (action.equals(Action.REFRESH)) {
			overlay = new TextOverlay("Refreshing...", assetManager, true);
			UIConnectionWrapper.findGameById(new FindGameByIdResultHandler(), gameBoard.id, GameLoop.USER.handle);
		} else if (action.equals(Action.BACK)) {
			returnCode = Action.BACK;
		} else if (action.startsWith(Action.SHIP_MOVE)) {
			if (action.contains("-")) {
				Integer moveHashCode = Integer.valueOf(action.split("-")[1]);
				for (int i = 0; i < inProgressMoves.size(); ++i) {
					Move move = inProgressMoves.get(i);
					if (move.hashCode() == moveHashCode) {
						showShipSelectionDialog(move, touchedPlanets);
						break;
					}
				}

				for (Move move : gameBoard.movesInProgress) {
					if (moveHashCode == move.hashCode()) {
						move.selected = 0.0f;
						addPlanetsToMoveSelectedPlanets(move);
					} else {
						move.selected = -1f;
					}
				}
			}
		}
	}

	private void addPlanetsToMoveSelectedPlanets(Move move) {
		moveSelectedPlanets.clear();

		for (Planet planet : gameBoard.planets) {
			if (planet.name.equals(move.fromPlanet) || planet.name.equals(move.toPlanet)) {
				moveSelectedPlanets.add(planet);
			}
		}

		overlay = new DismissableOverlay(assetManager, new TextOverlay("", assetManager, false),
				new PostDismissAction() {

					@Override
					public void apply() {
						clearMoveSelectedPlanets();
					}
				});

	}

	private void clearMoveSelectedPlanets() {
		moveSelectedPlanets.clear();

		if (gameBoard == null) {
			return;
		}

		for (Move move : gameBoard.movesInProgress) {
			move.selected = -1f;
		}
	}

	private void clearTouchedPlanets() {
		touchedPlanets.clear();

		if (gameBoard == null || gameBoard.planets == null) {
			return;
		}
		for (Planet planet : gameBoard.planets) {
			planet.touched = false;
		}
	}

	@Override
	public void resize(int width, int height) {
		camera = new PerspectiveCamera(67f, width, height);
		camera.near = 1.0f;
		camera.far = 5000f;

		camera.position.set(new Vector3(0, 0, 0));
		camera.translate(0.0f, 0.0f, 10.0f);
		camera.lookAt(0.0f, 0.0f, 0.0f);
		camera.update();

		Gdx.gl.glViewport(0, 0, width, height);

		worldPlane.resize(camera);
		boardPlane.resize();
		boardScreenHud.resize(width, height);
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {

	}

	@Override
	public Object getRenderResult() {
		return returnCode;
	}

	public void setConnectionError(String msg) {
		connectionError = msg;
	}

	public class PerformMoveResultHandler implements UIConnectionResultCallback<GameBoard> {

		@Override
		public void onConnectionResult(GameBoard result) {
			setGameBoard(result);
			inProgressMoves.clear();
			clearTouchedPlanets();
			overlay = null;
		}

		@Override
		public void onConnectionError(String msg) {
			overlay = new DismissableOverlay(assetManager, new TextOverlay(CONNECTION_ERROR_MESSAGE, "medium",
					assetManager));
		}
	}

	public class FindGameByIdResultHandler implements UIConnectionResultCallback<GameBoard> {

		@Override
		public void onConnectionResult(GameBoard result) {
			setGameBoard(result);
			inProgressMoves.clear();
			clearTouchedPlanets();
			overlay = null;
		}

		@Override
		public void onConnectionError(String msg) {
			overlay = new DismissableOverlay(assetManager, new TextOverlay(CONNECTION_ERROR_MESSAGE, "medium",
					assetManager));
		}
	}

	@Override
	public void resetState() {
		returnCode = null;
		connectionError = null;
		roundAnimated = -2;
		inProgressMoves.clear();
		clearTouchedPlanets();

		gameBoard = null;
	}

	public List<Move> getPendingMoves() {
		return inProgressMoves;
	}
}
