package com.xxx.galcon.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
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
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.math.WorldMath;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Planet;

public class BoardScreen implements ScreenFeedback, ContactListener {
	private static final float BOARD_WIDTH_RATIO = .95f;
	private static final float BOARD_HEIGHT_RATIO = .7f;

	private static final float PLANET_Z_COORD = -99.5f;
	private static final float TILE_SIZE_IN_UNITS = 10.0f;

	private Camera camera;
	private GameBoard gameBoard;
	private GameAction gameAction;
	private World physicsWorld;

	private ShaderProgram colorShader;
	private ShaderProgram gridShader;

	private StillModel planetModel;
	private Matrix4 modelViewMatrix = new Matrix4();

	boolean intro = true;
	float introTimeBegin = 0.0f;
	float introElapsedTime = 2.8f;

	public BoardScreen() {
		colorShader = new ShaderProgram(Gdx.files.internal("data/shaders/color-vs.glsl"),
				Gdx.files.internal("data/shaders/color-fs.glsl"));
		gridShader = new ShaderProgram(Gdx.files.internal("data/shaders/grid-vs.glsl"),
				Gdx.files.internal("data/shaders/grid-fs.glsl"));

		ObjLoader loader = new ObjLoader();
		planetModel = loader.loadObj(Gdx.files.internal("data/models/planet.obj"));
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

		// 0,0 is in the center of the tile, hence -0.5, -0.5 is the far left
		// corner. Same logic applies to bottom right
		public Vector2 worldXYToBoardXY(float worldX, float worldY, GameBoard gameBoard) {
			float x = (worldX - leftInWorld) / widthInWorld;
			if (x < 0 || x > 1) {
				return null;
			}

			float y = Math.abs((worldY - topInWorld) / heightInWorld);
			if (y < 0 || y > 1) {
				return null;
			}

			x *= gameBoard.width;
			y *= gameBoard.height;

			x -= 0.5f;
			y -= 0.5f;

			return new Vector2(x, y);
		}
	}

	private BoardPlane boardPlane = new BoardPlane();

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

	private WorldPlane worldPlane = new WorldPlane();

	public void setGameBoard(GameBoard gameBoard) {
		this.gameBoard = gameBoard;
		processGameBoard();
	}

	private void processGameBoard() {
		physicsWorld = new World(new Vector2(0.0f, 0.0f), true);
		physicsWorld.setContactListener(this);

		for (Planet planet : gameBoard.planets) {
			addPhysicsToPlanet(planet);
		}
	}

	private Body handleTouch(Camera camera) {
		Body contactBody = null;
		if (Gdx.input.justTouched()) {
			Vector2 worldXY = WorldMath.screenXYToWorldXY(camera, Gdx.input.getX(), Gdx.input.getY());
			Vector2 boardXY = boardPlane.worldXYToBoardXY(worldXY.x, worldXY.y, gameBoard);

			if (boardXY != null) {
				BodyDef bodyDef = new BodyDef();
				bodyDef.type = BodyDef.BodyType.DynamicBody;
				bodyDef.position.set(boardXY);

				contactBody = physicsWorld.createBody(bodyDef);
				contactBody.setUserData("touch");

				CircleShape shape = new CircleShape();
				shape.setRadius(0.1f);

				FixtureDef fixtureDef = new FixtureDef();
				fixtureDef.shape = shape;
				contactBody.createFixture(fixtureDef);
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
		gridShader.setUniformf("uTilesWide", gameBoard.width);
		gridShader.setUniformf("uTilesTall", gameBoard.height);

		planetModel.render(colorShader);

		gridShader.end();
	}

	private void renderPlanet(Planet planet, GL20 gl, Camera camera) {
		gl.glEnable(GL20.GL_BLEND);
		gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		colorShader.begin();
		modelViewMatrix.idt();
		modelViewMatrix.trn(-boardPlane.widthInWorld / 2, (boardPlane.heightInWorld / 2) + boardPlane.yShift,
				PLANET_Z_COORD);

		float tileWidthInWorld = boardPlane.widthInWorld / gameBoard.width;
		float tileHeightInWorld = boardPlane.heightInWorld / gameBoard.height;
		modelViewMatrix.trn(tileWidthInWorld * planet.position.getX() + tileWidthInWorld / 2, -tileHeightInWorld
				* planet.position.getY() - tileHeightInWorld / 2, 0.0f);

		colorShader.setUniformMatrix("uPMatrix", camera.combined);
		colorShader.setUniformMatrix("uMVMatrix", modelViewMatrix);
		if (planet.touched) {
			colorShader.setUniformf("uColor", 0.8f, 0.0f, 0.0f, 1.0f);
		} else {
			colorShader.setUniformf("uColor", 0.8f, 0.8f, 1.0f, 1.0f);
		}
		colorShader.setUniformf("uRadius", (float) 0.45f * (planet.shipRegenRate / Constants.SHIP_REGEN_RATE_MAX));

		planetModel.render(colorShader);
		colorShader.end();

		gl.glDisable(GL20.GL_BLEND);
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
		Object userDataOne = contact.getFixtureA().getBody().getUserData();
		Object userDataTwo = contact.getFixtureB().getBody().getUserData();

		if (userDataOne instanceof Planet) {
			((Planet) userDataOne).touched = true;
		}
		if (userDataTwo instanceof Planet) {
			((Planet) userDataTwo).touched = true;
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

		Body contactBody = handleTouch(camera);

		physicsWorld.step(delta, 8, 3);

		if (contactBody != null) {
			physicsWorld.destroyBody(contactBody);
		}

		moveCameraForIntro(camera);

		renderGrid(camera);

		for (Planet planet : gameBoard.planets) {
			renderPlanet(planet, Gdx.gl20, camera);
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
		// TODO Auto-generated method stub
		return null;
	}
}
