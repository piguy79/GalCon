package com.xxx.galcon.screen;

import static com.xxx.galcon.Util.createShader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.HarvestMove;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.model.factory.MoveFactory;
import com.xxx.galcon.screen.event.MoveListener;
import com.xxx.galcon.screen.event.TransitionEventListener;
import com.xxx.galcon.screen.ship.selection.ExistingMoveDialog;
import com.xxx.galcon.screen.ship.selection.MoveDialog;
import com.xxx.galcon.screen.widget.Line;
import com.xxx.galcon.screen.widget.PlanetButton;

public class BoardScreen implements ScreenFeedback {

	private int roundAnimated = -2;
	private Camera camera;
	private GameBoard gameBoard;
	private ShaderProgram fontShader;
	private List<Move> moves;

	public List<Planet> touchedPlanets = new ArrayList<Planet>(2);
	List<Move> inProgressMoves = new ArrayList<Move>();
	List<HarvestMove> inProgressHarvest = new ArrayList<HarvestMove>();


	private TextureAtlas planetAtlas;
	private TextureAtlas levelAtlas;
	private TextureAtlas gameBoardAtlas;
	private AssetManager assetManager;

	boolean intro = true;
	float introTimeBegin = 0.0f;
	float introElapsedTime = 2.8f;

	private String returnCode = null;
	private UISkin skin;

	private Stage stage;

	private Table boardTable;
	private InputProcessor oldInputProcessor;
	private MoveDialog moveDialog;
	private MoveHud moveHud;
	private BoardScreenPlayerHud playerHud;

	private Map<String, PlanetButton> planetButtons = new HashMap<String, PlanetButton>();
	private Map<String, Integer> planetToMoveCount = new HashMap<String, Integer>();

	private boolean planetMoveChange = false;
	
	private MenuScreenContainer previousScreen;

	public BoardScreen(UISkin skin, AssetManager assetManager, TweenManager tweenManager) {
		this.assetManager = assetManager;
		this.skin = skin;

		planetAtlas = assetManager.get("data/images/planets.atlas", TextureAtlas.class);
		levelAtlas = assetManager.get("data/images/levels.atlas", TextureAtlas.class);
		gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);

		this.moves = new ArrayList<Move>();

		fontShader = createShader("data/shaders/font-vs.glsl", "data/shaders/font-fs.glsl");

		stage = new Stage();
		stage.setViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

	}

	public void setGameBoard(GameBoard gameBoard) {
		stage = new Stage();
		this.gameBoard = gameBoard;
		moves = gameBoard.movesInProgress;
		planetButtons.clear();
		planetToMoveCount.clear();
		planetMoveChange = true;
		createLayout();
	}

	private void createLayout() {
		boardTable = new Table();
		AtlasRegion bg = levelAtlas.findRegion("" + gameBoard.map);
		boardTable.setBackground(new TextureRegionDrawable(bg));
		boardTable.setWidth(Gdx.graphics.getWidth());
		boardTable.setHeight(Gdx.graphics.getHeight() * 0.8f);
		boardTable.setY(Gdx.graphics.getHeight() * 0.1f);

		stage.addActor(boardTable);
		createGrid();
		createMoveHud();
		createPlayerHud();
		createPlanets();
		createMoves();

		Gdx.input.setInputProcessor(stage);
	}

	private void createPlayerHud() {
		Point position = new Point(0, boardTable.getHeight() + moveHud.getHeight());
		playerHud = new BoardScreenPlayerHud(assetManager, skin, fontShader, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() * 0.1f, position);
		playerHud.addListener(new TransitionEventListener(){
			@Override
			public void transition(String action) {
				if(action.equals(Action.BACK)){
					stage.dispose();
					returnCode = action;
				}
			}
		});
		stage.addActor(playerHud);
	}

	private void createMoves() {
		for (Move move : moves) {
			ImageButton shipMoveButton = new ImageButton(skin.get("shipButton", ImageButtonStyle.class));
			float tileHeight = boardTable.getHeight() / gameBoard.heightInTiles;
			shipMoveButton.setHeight(tileHeight * 0.4f);
			float tileWidth = boardTable.getWidth() / gameBoard.widthInTiles;
			shipMoveButton.setWidth(tileWidth * 0.4f);
			shipMoveButton.setOrigin(shipMoveButton.getWidth()/2, shipMoveButton.getHeight()/2);
			
			
			Point movePosition = pointInWorld(move.previousPosition.x, move.previousPosition.y);
			
			Table wrapper = setupRotationWrapper(shipMoveButton, tileHeight,
					tileWidth, movePosition);
			
			wrapper.setRotation(move.angleOfMovement());
						
			Point newPosition = pointInWorld(move.currentPosition.x, move.currentPosition.y);
			wrapper.addAction(Actions.moveTo(newPosition.x+ (tileWidth / 2), newPosition.y + (tileHeight / 2), 1.2f));
			stage.addActor(wrapper);
			
		}

	}

	private Table setupRotationWrapper(ImageButton shipMoveButton,
			float tileHeight, float tileWidth, Point movePosition) {
		Table wrapper = new Table();
		
		wrapper.defaults().width(tileWidth * 0.25f).height(tileWidth * 0.25f).pad(0);
		wrapper.add(shipMoveButton);			
		wrapper.setX(movePosition.x + (tileWidth / 2));
		wrapper.setY(movePosition.y + (tileHeight / 2));
		wrapper.setTransform(true);
		wrapper.setOrigin(wrapper.getPrefWidth() / 2, wrapper.getPrefHeight() / 2);
		wrapper.setScaleX(1.5f);
		return wrapper;
	}

	private void createMoveHud() {
		moveHud = new MoveHud(assetManager, skin, gameBoard, fontShader, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() * 0.1f);
		moveHud.addMoves(moves);

		moveHud.addListener(new MoveListener() {

			@Override
			protected void performMove(Move move) {
				renderMoveDialog(move);
			}

			@Override
			public void sendMove() {
				List<Move> currentRoundMoves = new ArrayList<Move>();
				for (Move move : moves) {
					if (move.startingRound == gameBoard.roundInformation.currentRound) {
						currentRoundMoves.add(move);
					}
				}
				UIConnectionWrapper.performMoves(new PerformMoveResultHandler(), gameBoard.id, currentRoundMoves,
						new ArrayList<HarvestMove>());
			}

		});

		stage.addActor(moveHud);

	}

	private void createGrid() {

		float yOffset = boardTable.getHeight() / gameBoard.heightInTiles;
		float xOffset = boardTable.getWidth() / gameBoard.widthInTiles;
		Color grey = Color.GRAY;
		grey.a = 0.6f;
		TextureRegion lineRegion = gameBoardAtlas.findRegion("line");

		for (int i = 1; i < gameBoard.heightInTiles; i++) {
			Line line = new Line(grey, Gdx.graphics.getWidth(), lineRegion);
			line.setY((yOffset * i) + boardTable.getY());
			line.setHeight(Gdx.graphics.getHeight() * 0.005f);
			stage.addActor(line);

		}

		for (int i = 1; i < gameBoard.widthInTiles; i++) {
			Line horizontalLine = new Line(grey, Gdx.graphics.getWidth() * 0.005f, lineRegion);
			horizontalLine.setY(boardTable.getY());
			horizontalLine.setX(xOffset * i);
			horizontalLine.setHeight(boardTable.getHeight());
			stage.addActor(horizontalLine);
		}

	}

	private Point pointInWorld(float x, float y) {
		float tileWidthInWorld = boardTable.getWidth() / gameBoard.widthInTiles;
		float tileHeightInWorld = boardTable.getHeight() / gameBoard.heightInTiles;
		float yOffset = boardTable.getY();

		return new Point(tileWidthInWorld * x, (tileHeightInWorld * y) + yOffset);
	}

	private void createPlanets() {
		float tileWidthInWorld = boardTable.getWidth() / gameBoard.widthInTiles;
		float tileHeightInWorld = boardTable.getHeight() / gameBoard.heightInTiles;
		float yOffset = boardTable.getY();

		TextureRegionDrawable planetTexture = new TextureRegionDrawable(planetAtlas.findRegion("planet2"));

		float minPlanetWidth = tileWidthInWorld * 0.6f;
		float minPlanetHeight = tileHeightInWorld * 0.6f;
		planetTexture.setMinWidth(minPlanetWidth);
		planetTexture.setMinHeight(minPlanetHeight);

		TextButtonStyle style = new TextButtonStyle(planetTexture, planetTexture, planetTexture, Fonts.getInstance(
				assetManager).mediumFont());
		for (final Planet planet : gameBoard.planets) {
			float maxExpand = 5;
			float expand = planet.shipRegenRate > maxExpand ? maxExpand : planet.shipRegenRate;
			planetTexture.setMinWidth(minPlanetWidth + ((minPlanetWidth * 0.2f) * expand));
			planetTexture.setMinHeight(minPlanetHeight + ((minPlanetHeight * 0.2f) * expand));
			final PlanetButton planetButton = new PlanetButton(fontShader, ""
					+ planet.numberOfShipsToDisplay(gameBoard, roundHasAlreadyBeenAnimated()), style, planet);

			planetButton.setX(tileWidthInWorld * planet.position.x);
			planetButton.setY((tileHeightInWorld * planet.position.y) + yOffset);
			planetButton.setColor(planet.getColor());

			planetButton.addListener(new ClickListener() {

				@Override
				public void clicked(InputEvent event, float x, float y) {
					planetButton.addAction(Actions.forever(Actions.sequence(Actions.color(new Color(0, 0, 0, 0), 0.7f),
							Actions.color(planet.getColor(), 0.5f))));
					if (touchedPlanets.size() < 2) {
						touchedPlanets.add(planet);
						renderDialog();
					} else {
						clearTouchedPlanets();
					}
				}

			});
			stage.addActor(planetButton);
			planetButtons.put(planet.name, planetButton);
			planetToMoveCount.put(planet.name, 0);
		}
	}

	private void renderDialog() {
		if (touchedPlanets.size() > 1) {
			Planet userPlanet = touchedUserPlanet(touchedPlanets);
			if (userPlanet != null) {
				Planet otherPlanet = otherPlanet(touchedPlanets, userPlanet);
				if(!GameLoop.USER.hasMoved(gameBoard)){
					renderMoveDialog(userPlanet, otherPlanet);
				}else{
					clearMoveActions(userPlanet);
					clearMoveActions(otherPlanet);
				}
			} else {
				clearMoveActions(touchedPlanets.get(0));
				Planet toKeep = touchedPlanets.get(1);
				touchedPlanets.clear();
				touchedPlanets.add(toKeep);
			}
		}
	}

	private Planet otherPlanet(List<Planet> planets, Planet userPlanet) {
		for (Planet planet : planets) {
			if (!planet.name.equals(userPlanet.name)) {
				return planet;
			}
		}

		return null;
	}

	private Planet touchedUserPlanet(List<Planet> planets) {
		for (Planet planet : planets) {
			if (planet.owner != null && planet.owner.equals(GameLoop.USER.handle)) {
				return planet;
			}
		}
		return null;
	}

	private void renderMoveDialog(final Planet one, final Planet two) {
		Integer offSetCount = planetToMoveCount.get(one.name) == null ? 0 : planetToMoveCount.get(one.name);
		moveDialog = new MoveDialog(one, two, offSetCount, one.numberOfShips - offSetCount, assetManager,
				boardTable.getWidth() * 0.9f, boardTable.getHeight() * 0.3f, skin);
		setupPositionOFMoveDialog();

		moveDialog.addListener(new MoveListener() {

			@Override
			protected void performMove(Move move) {
				createNewMove(move);
			}

			@Override
			public void cancelDialog() {
				clearTouchedPlanets();
				clearMoveActions(one);
				clearMoveActions(two);
			}

		});
	}

	private void clearMoveActions(Planet planet) {
		planetButtons.get(planet.name).clearActions();
		planetButtons.get(planet.name).addAction(Actions.color(planet.getColor(), 0.4f));
	}

	private void setupPositionOFMoveDialog() {
		float initialY = boardTable.getY() + (boardTable.getHeight() * 0.6f);
		float initialX = boardTable.getX() + boardTable.getWidth() * 0.05f;

		moveDialog.setPosition(-boardTable.getWidth(), initialY);

		moveDialog.show(new Point(initialX, initialY));
		stage.addActor(moveDialog);
	}

	private void renderMoveDialog(final Move move) {
		int offset = planetToMoveCount.get(move.fromPlanet(gameBoard.planets).name);
		moveDialog = new ExistingMoveDialog(move, move.fromPlanet(gameBoard.planets), move.toPlanet(gameBoard.planets),
				offset, assetManager, boardTable.getWidth() * 0.9f, boardTable.getHeight() * 0.3f, skin);
		setupPositionOFMoveDialog();

		moveDialog.addListener(new MoveListener() {
			@Override
			public void cancelDialog() {
				deleteMove(move);
			}
		});

		moveDialog.addListener(new MoveListener() {
			@Override
			protected void performMove(Move newMove) {
				deleteMove(move);
				createNewMove(newMove);
			}
		});
	}

	private void createNewMove(Move newMove) {
		clearTouchedPlanets();
		Integer count = planetToMoveCount.get(newMove.fromPlanet);
		planetToMoveCount.put(newMove.fromPlanet, count + newMove.shipsToMove);
		moves.add(newMove);
		moveHud.addMove(newMove);
		moveDialog.hide();
		clearMoveActions(newMove.fromPlanet(gameBoard.planets));
		clearMoveActions(newMove.toPlanet(gameBoard.planets));
		planetMoveChange = true;
	}

	private void deleteMove(Move move) {
		moves.remove(move);
		int offset = planetToMoveCount.get(move.fromPlanet);
		offset -= move.shipsToMove;
		planetToMoveCount.put(move.fromPlanet, offset);
		planetMoveChange = true;
		moveHud.removeMove(move);

	}

	private boolean roundHasAlreadyBeenAnimated() {
		return roundAnimated == gameBoard.roundInformation.currentRound;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(float delta) {

		if (planetMoveChange) {
			applyMovesToPlanetButtons();
			planetMoveChange = false;
		}

		stage.act(delta);
		stage.draw();
	}

	private void applyMovesToPlanetButtons() {
		for (Planet planet : gameBoard.planets) {
			int offSet = planetToMoveCount.get(planet.name);
			planetButtons.get(planet.name)
					.setText(new StringBuilder().append(planet.numberOfShips - offSet).toString());
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

	}

	@Override
	public void show() {
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

	}

	@Override
	public Object getRenderResult() {
		return returnCode;
	}

	public void setConnectionError(String msg) {

	}

	public class PerformMoveResultHandler implements UIConnectionResultCallback<GameBoard> {

		@Override
		public void onConnectionResult(GameBoard result) {
			setGameBoard(result);
			inProgressMoves.clear();
			clearTouchedPlanets();
		}

		@Override
		public void onConnectionError(String msg) {

		}
	}

	public class FindGameByIdResultHandler implements UIConnectionResultCallback<GameBoard> {

		@Override
		public void onConnectionResult(GameBoard result) {
			setGameBoard(result);
			inProgressMoves.clear();
			clearTouchedPlanets();
		}

		@Override
		public void onConnectionError(String msg) {

		}
	}

	@Override
	public void resetState() {
		returnCode = null;
		roundAnimated = -2;
		inProgressMoves.clear();
		clearTouchedPlanets();

		gameBoard = null;
	}

	public List<Move> getPendingMoves() {
		return inProgressMoves;
	}
	
	public MenuScreenContainer getPreviousScreen() {
		return previousScreen;
	}
	
	public void setPreviousScreen(MenuScreenContainer previousScreen) {
		this.previousScreen = previousScreen;
	}
}
