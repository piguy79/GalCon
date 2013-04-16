package com.xxx.galcon.screen;

import static com.xxx.galcon.Constants.OWNER_NO_ONE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
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
import com.xxx.galcon.ConnectionWrapper;
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.http.ConnectionResultCallback;
import com.xxx.galcon.math.GalConMath;
import com.xxx.galcon.math.WorldMath;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Planet;

public class BoardScreen implements ScreenFeedback, ContactListener {
	public enum ReturnCode {
		BACK
	};

	private static final float BOARD_WIDTH_RATIO = .95f;
	private static final float BOARD_HEIGHT_RATIO = .7f;

	private static final float PLANET_Z_COORD = -99.5f;
	private static final float TILE_SIZE_IN_UNITS = 10.0f;

	private static final String TOUCH_OBJECT = "touch";

	private Camera camera;
	private GameBoard gameBoard;
	private World physicsWorld;

	private ShaderProgram planetShader;
	private ShaderProgram gridShader;
	private ShaderProgram shipShader;

	private BitmapFont font;
	private Texture planetNumbersTexture;

	private StillModel planetModel;
	private StillModel shipModel;
	private Matrix4 modelViewMatrix = new Matrix4();

	private BoardPlane boardPlane = new BoardPlane();
	private WorldPlane worldPlane = new WorldPlane();

	List<Planet> touchedPlanets = new ArrayList<Planet>(2);
	List<Move> moves = new ArrayList<Move>();

	private AssetManager assetManager;
	private BoardScreenHud boardScreenHud;
	private ShipSelectionDialog shipSelectionDialog;

	boolean intro = true;
	float introTimeBegin = 0.0f;
	float introElapsedTime = 2.8f;

	private ReturnCode returnCode = null;

	public BoardScreen(AssetManager assetManager) {
		this.assetManager = assetManager;

		planetShader = new ShaderProgram(Gdx.files.internal("data/shaders/planet-vs.glsl"),
				Gdx.files.internal("data/shaders/planet-fs.glsl"));
		if (!planetShader.isCompiled() && !planetShader.getLog().isEmpty()) {
			throw new IllegalStateException("Shader compilation fail: " + planetShader.getLog());
		}

		gridShader = new ShaderProgram(Gdx.files.internal("data/shaders/grid-vs.glsl"),
				Gdx.files.internal("data/shaders/grid-fs.glsl"));
		if (!gridShader.isCompiled() && !gridShader.getLog().isEmpty()) {
			throw new IllegalStateException("Shader compilation fail: " + gridShader.getLog());
		}

		shipShader = new ShaderProgram(Gdx.files.internal("data/shaders/ship-vs.glsl"),
				Gdx.files.internal("data/shaders/ship-fs.glsl"));
		if (!shipShader.isCompiled() && !shipShader.getLog().isEmpty()) {
			throw new IllegalStateException("Shader compilation fail: " + shipShader.getLog());
		}

		font = new BitmapFont(Gdx.files.internal("data/fonts/tahoma_16.fnt"),
				Gdx.files.internal("data/fonts/tahoma_16.png"), false);

		ObjLoader loader = new ObjLoader();
		planetModel = loader.loadObj(Gdx.files.internal("data/models/planet.obj"));

		loader = new ObjLoader();
		shipModel = loader.loadObj(Gdx.files.internal("data/models/ship.obj"));

		planetNumbersTexture = assetManager.get("data/fonts/planet_numbers.png", Texture.class);

		boardScreenHud = new BoardScreenHud(assetManager);

		physicsWorld = new World(new Vector2(0.0f, 0.0f), true);
		physicsWorld.setContactListener(this);
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
					- (0.02f * (worldPlane.topLeft.y - worldPlane.bottomRight.y));

			topInWorld = heightInWorld / 2.0f + boardPlane.yShift;
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

			float y = Math.abs((worldY - topInWorld) / heightInWorld);
			if (y < 0 || y > 1) {
				return null;
			}

			x *= gameBoard.widthInTiles;
			y *= gameBoard.heightInTiles;

			x -= 0.5f;
			y -= 0.5f;

			return new Vector2(x, y);
		}
	}

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
		associateHudInformation(gameBoard);
	}

	private void associateHudInformation(GameBoard gameBoard2) {
		boardScreenHud.associateCurrentRoundInformation(gameBoard.currentPlayerToMove, gameBoard.roundNumber);
	}

	private void processGameBoard() {
		List<Body> bodies = new ArrayList<Body>();
		Iterator<Body> bodyIter = physicsWorld.getBodies();
		while (bodyIter.hasNext()) {
			bodies.add(bodyIter.next());
		}

		for (Body body : bodies) {
			body.setUserData(null);
			physicsWorld.destroyBody(body);
		}

		for (Planet planet : gameBoard.planets) {
			addPhysicsToPlanet(planet);
		}
	}

	private Body handleTouch(Camera camera) {
		if (gameBoard == null) {
			return null;
		}

		if (!gameBoard.currentPlayerToMove.equals(GameLoop.USER)) {
			return null;
		}

		Body contactBody = null;
		if (Gdx.input.justTouched()) {
			if (shipSelectionDialog != null) {
				int x = Gdx.input.getX();
				int y = Gdx.graphics.getHeight() - Gdx.input.getY();
				if (shipSelectionDialog.contains(x, y)) {
					return null;
				} else {
					shipSelectionDialog.dispose();
					shipSelectionDialog = null;
				}
			} else {
				Vector2 worldXY = WorldMath.screenXYToWorldXY(camera, Gdx.input.getX(), Gdx.input.getY());
				Vector2 boardXY = boardPlane.worldXYToBoardXY(worldXY.x, worldXY.y, gameBoard);

				if (boardXY != null) {
					BodyDef bodyDef = new BodyDef();
					bodyDef.type = BodyDef.BodyType.DynamicBody;
					bodyDef.position.set(boardXY);

					contactBody = physicsWorld.createBody(bodyDef);
					contactBody.setUserData(TOUCH_OBJECT);

					CircleShape shape = new CircleShape();
					shape.setRadius(0.2f);

					FixtureDef fixtureDef = new FixtureDef();
					fixtureDef.shape = shape;
					contactBody.createFixture(fixtureDef);
				}
			}
		}

		return contactBody;
	}

	private void moveCameraForIntro(Camera camera) {
		if (intro) {
			if (introTimeBegin == 0.0f) {
				int width = Gdx.graphics.getWidth();
				int height = Gdx.graphics.getHeight();
				camera = new PerspectiveCamera(67f, width, height);
				camera.near = 1.0f;
				camera.far = 5000f;
				camera.translate(0.0f, -120.0f, 20.0f);
				camera.lookAt(0.0f, -100.0f, 0.0f);

				camera.update();
			}
			introTimeBegin += Gdx.graphics.getDeltaTime();

			camera.position.set(new Vector3(0, 0, 0));
			camera.translate(0.0f, -40.0f * (introElapsedTime - introTimeBegin),
					3.33f * (introElapsedTime - introTimeBegin) + 10.0f);
			camera.lookAt(0.0f, -33.3f * (introElapsedTime - introTimeBegin), 0.0f);
			camera.update();

			if (introTimeBegin > introElapsedTime) {
				intro = false;

				camera.position.set(new Vector3(0, 0, 0));
				camera.translate(0.0f, 0.0f, 10.0f);
				camera.lookAt(0.0f, 0.0f, 0.0f);
				camera.update();
			}
		}
	}

	private void renderGrid(Camera camera) {
		gridShader.begin();

		gridShader.setUniformMatrix("uPMatrix", camera.combined);
		gridShader.setUniformMatrix("uMVMatrix", boardPlane.modelViewMatrix);
		gridShader.setUniformf("uTilesWide", gameBoard.widthInTiles);
		gridShader.setUniformf("uTilesTall", gameBoard.heightInTiles);

		planetModel.render(planetShader);

		gridShader.end();
	}

	private void renderPlanets(List<Planet> planets, Camera camera) {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		planetNumbersTexture.bind(1);

		planetShader.begin();
		planetShader.setUniformi("numbersTex", 1);

		for (Planet planet : planets) {

			modelViewMatrix.idt();
			modelViewMatrix.trn(-boardPlane.widthInWorld / 2, (boardPlane.heightInWorld / 2) + boardPlane.yShift,
					PLANET_Z_COORD);

			float tileWidthInWorld = boardPlane.widthInWorld / gameBoard.widthInTiles;
			float tileHeightInWorld = boardPlane.heightInWorld / gameBoard.heightInTiles;
			modelViewMatrix.trn(tileWidthInWorld * planet.position.getX() + tileWidthInWorld / 2, -tileHeightInWorld
					* planet.position.getY() - tileHeightInWorld / 2, 0.0f);

			modelViewMatrix.scale(tileWidthInWorld / TILE_SIZE_IN_UNITS, tileHeightInWorld / TILE_SIZE_IN_UNITS, 1.0f);

			planetShader.setUniformMatrix("uPMatrix", camera.combined);
			planetShader.setUniformMatrix("uMVMatrix", modelViewMatrix);
			planetShader.setUniformi("shipCount", planet.numberOfShips);

			float r = 0.0f, g = 0.0f, b = 0.0f;
			if (planet.touched) {
				if (planet.owner.equals(GameLoop.USER)) {
					g = 1.0f;
				} else if (!planet.owner.equals(OWNER_NO_ONE)) {
					r = 1.0f;
				} else {
					r = 1.0f;
					g = 1.0f;
					b = 1.0f;
				}
			} else {
				if (planet.owner.equals(GameLoop.USER)) {
					g = 0.5f;
				} else if (!planet.owner.equals(OWNER_NO_ONE)) {
					r = 0.5f;
				} else if (!planet.touched) {
					r = 0.5f;
					g = 0.5f;
					b = 0.5f;
				}
			}
			planetShader.setUniformf("uColor", r, g, b, 1.0f);
			planetShader.setUniformf("uRadius", (float) 0.45f * (planet.shipRegenRate / Constants.SHIP_REGEN_RATE_MAX));

			planetModel.render(planetShader);
		}
		planetShader.end();

		Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);

		Gdx.gl.glDisable(GL20.GL_BLEND);
	}

	private void renderShips(List<Planet> planets, List<Move> moves, Camera camera) {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		float tileWidthInWorld = boardPlane.widthInWorld / gameBoard.widthInTiles;
		float tileHeightInWorld = boardPlane.heightInWorld / gameBoard.heightInTiles;

		shipShader.begin();

		for (Move move : moves) {
			if (!move.player.equals(GameLoop.USER)) {
				continue;
			}

			modelViewMatrix.idt();
			modelViewMatrix.trn(-boardPlane.widthInWorld / 2, (boardPlane.heightInWorld / 2) + boardPlane.yShift,
					PLANET_Z_COORD);

			int startX = 0, startY = 0, endX = 0, endY = 0;
			for (Planet planet : planets) {
				if (planet.name.equals(move.fromPlanet)) {
					startX = planet.position.getX();
					startY = planet.position.getY();
				} else if (planet.name.equals(move.toPlanet)) {
					endX = planet.position.getX();
					endY = planet.position.getY();
				}
			}

			int totalDuration = GalConMath.distance(startX, startY, endX, endY);
			float percentTraveled = 1.0f - ((float) move.duration / (float) totalDuration);

			float shipX = startX + (endX - startX) * percentTraveled;
			float shipY = startY + (endY - startY) * percentTraveled;

			float angle = new Vector2(endX - startX, endY - startY).angle();

			modelViewMatrix.trn(tileWidthInWorld * shipX + tileWidthInWorld / 2, -tileHeightInWorld * shipY
					- tileHeightInWorld / 2, 0.0f);

			modelViewMatrix.rotate(0, 0, 1, 180 - angle);

			modelViewMatrix.scale(tileWidthInWorld / 8.0f, tileHeightInWorld / 8.0f, 1.0f);

			shipShader.setUniformMatrix("uPMatrix", camera.combined);
			shipShader.setUniformMatrix("uMVMatrix", modelViewMatrix);

			float r = 1.0f, g = 0.0f, b = 0.0f;
			shipShader.setUniformf("uColor", r, g, b, 1.0f);

			shipModel.render(shipShader);
		}

		shipShader.end();

		Gdx.gl.glDisable(GL20.GL_BLEND);
	}

	private void addPhysicsToPlanet(Planet planet) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(new Vector2(planet.position.getX(), planet.position.getY()));

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
				if (planet.owner.equals(GameLoop.USER) && alreadySelectedPlanet.owner.equals(GameLoop.USER)) {
					planet.touched = false;
				} else if (!planet.owner.equals(GameLoop.USER) && !alreadySelectedPlanet.owner.equals(GameLoop.USER)) {
					planet.touched = false;
				} else {
					touchedPlanets.add(planet);
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

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		if (gameBoard == null) {
			float width = Gdx.graphics.getWidth() / 2;
			float height = Gdx.graphics.getHeight() / 2;

			SpriteBatch spriteBatch = new SpriteBatch();
			spriteBatch.begin();

			String text = "Loading...";
			float halfFontWidth = font.getBounds(text).width / 2;
			font.draw(spriteBatch, text, width / 2 - halfFontWidth, height * .4f);
			spriteBatch.end();
			return;
		}

		Body contactBody = handleTouch(camera);

		physicsWorld.step(delta, 8, 3);

		if (contactBody != null) {
			physicsWorld.destroyBody(contactBody);
		}

		moveCameraForIntro(camera);

		renderGrid(camera);
		renderPlanets(gameBoard.planets, camera);
		renderShips(gameBoard.planets, gameBoard.movesInProgress, camera);

		boardScreenHud.render(delta);

		renderDialogs(delta);

		Action hudResult = (Action) boardScreenHud.getRenderResult();
		if (hudResult != null) {
			processHudButtonTouch(hudResult);
		}
	}

	private void renderDialogs(float delta) {
		if (shipSelectionDialog != null) {
			shipSelectionDialog.render(delta);

			Action action = (Action) shipSelectionDialog.getRenderResult();
			if (action != null) {
				processShipSelectionTouch(action);
			}
		}
	}

	private void processShipSelectionTouch(Action action) {
		if (action == Action.DIALOG_OK) {
			Move move = new Move();

			int startX = 0, startY = 0, endX = 0, endY = 0;
			for (Planet planet : touchedPlanets) {
				if (planet.owner.equals(GameLoop.USER) && move.fromPlanet == null) {
					move.fromPlanet = planet.name;
					move.shipsToMove = shipSelectionDialog.getShipsToSend();
					planet.numberOfShips -= move.shipsToMove;
					startX = planet.position.getX();
					startY = planet.position.getY();
				} else {
					move.toPlanet = planet.name;
					endX = planet.position.getX();
					endY = planet.position.getY();
				}
			}

			move.duration = GalConMath.distance(startX, startY, endX, endY);

			moves.add(move);
			clearTouchedPlanets();
			shipSelectionDialog.dispose();
			shipSelectionDialog = null;
		} else if (action == Action.DIALOG_CANCEL) {
			shipSelectionDialog.dispose();
			shipSelectionDialog = null;
		}
	}

	private void processHudButtonTouch(Action action) {
		if (action == Action.SEND) {
			if (touchedPlanets.size() != 2) {
				return;
			}

			int shipsOnPlanet = 0;
			for (Planet planet : touchedPlanets) {
				if (planet.owner.equals(GameLoop.USER)) {
					shipsOnPlanet = planet.numberOfShips;
				}
			}

			int width = Gdx.graphics.getWidth();
			int height = Gdx.graphics.getHeight();
			int xMargin = (int) (width * .15f);
			int dialogWidth = width - 2 * xMargin;
			shipSelectionDialog = new ShipSelectionDialog(xMargin, (int) (height * .6f), dialogWidth,
					(int) (dialogWidth * .8f), assetManager, shipsOnPlanet);

		} else if (action == Action.END_TURN) {
			ConnectionWrapper.performMoves(new PerformMoveResultHandler(), gameBoard.id, moves);
		} else if (action == Action.REFRESH) {
			ConnectionWrapper.findGameById(new FindGameByIdResultHandler(), gameBoard.id);
		} else if (action == Action.BACK) {
			returnCode = ReturnCode.BACK;
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
		// TODO Auto-generated method stub

	}

	@Override
	public Object getRenderResult() {
		return returnCode;
	}

	public class PerformMoveResultHandler implements ConnectionResultCallback<GameBoard> {

		@Override
		public void result(GameBoard result) {
			setGameBoard(result);
			moves.clear();
			clearTouchedPlanets();
		}
	}

	public class FindGameByIdResultHandler implements ConnectionResultCallback<GameBoard> {

		@Override
		public void result(GameBoard result) {
			setGameBoard(result);
			moves.clear();
			clearTouchedPlanets();
		}
	}

	@Override
	public void resetState() {
		returnCode = null;
		gameBoard = null;
		moves.clear();
		clearTouchedPlanets();
	}
}
