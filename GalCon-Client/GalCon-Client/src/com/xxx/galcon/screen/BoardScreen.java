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
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.math.GalConMath;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.HarvestMove;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.model.factory.MoveFactory;
import com.xxx.galcon.model.factory.PlanetButtonFactory;
import com.xxx.galcon.screen.event.DialogEventListener;
import com.xxx.galcon.screen.event.MoveListener;
import com.xxx.galcon.screen.event.TransitionEventListener;
import com.xxx.galcon.screen.overlay.DismissableOverlay;
import com.xxx.galcon.screen.overlay.TextOverlay;
import com.xxx.galcon.screen.ship.selection.ExistingMoveDialog;
import com.xxx.galcon.screen.ship.selection.MoveDialog;
import com.xxx.galcon.screen.ship.selection.PlanetInformationDialog;
import com.xxx.galcon.screen.widget.Line;
import com.xxx.galcon.screen.widget.Moon;
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

	private TextureAtlas levelAtlas;
	private TextureAtlas gameBoardAtlas;
	private TextureAtlas menuAtlas;
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
	
	private List<Moon> moons = new ArrayList<Moon>();

	private Map<String, PlanetButton> planetButtons = new HashMap<String, PlanetButton>();
	private Map<String, Integer> planetToMoveCount = new HashMap<String, Integer>();

	private boolean planetMoveChange = false;

	private MenuScreenContainer previousScreen;
	
	private TextOverlay moveOverlay;

	public BoardScreen(UISkin skin, AssetManager assetManager, TweenManager tweenManager) {
		this.assetManager = assetManager;
		this.skin = skin;

		levelAtlas = assetManager.get("data/images/levels.atlas", TextureAtlas.class);
		gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);
		menuAtlas = assetManager.get("data/images/menus.atlas", TextureAtlas.class);

		this.moves = new ArrayList<Move>();

		fontShader = createShader("data/shaders/font-vs.glsl", "data/shaders/font-fs.glsl");

		stage = new Stage();

		stage.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				stage.getRoot().fire(event);

			}
		});

	}

	public void setGameBoard(GameBoard gameBoard) {
		stage = new Stage();
		stage.setViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		this.gameBoard = gameBoard;
		moves = new ArrayList<Move>();
		for (Move move : gameBoard.movesInProgress) {
			if (move.belongsToPlayer(GameLoop.USER)) {
				moves.add(move);
			}
		}
		planetButtons.clear();
		planetToMoveCount.clear();
		if (moveHud != null) {
			moveHud.removeMoves();
		}
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
		playerHud = new BoardScreenPlayerHud(assetManager, skin, fontShader, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight() * 0.1f, position, gameBoard);
		playerHud.addListener(new TransitionEventListener() {
			@Override
			public void transition(String action) {
				if (action.equals(Action.BACK)) {
					stage.dispose();
					returnCode = action;
				} else if (action.equals(Action.REFRESH)) {
					UIConnectionWrapper.findGameById(new FindGameByIdResultHandler(), gameBoard.id,
							GameLoop.USER.handle);
				}
			}
		});
		stage.addActor(playerHud);
	}

	private void createMoves() {
		MoveFactory.setSkin(skin);
		for (Move move : moves) {
			if (move.belongsToPlayer(GameLoop.USER)) {
				float tileHeight = boardTable.getHeight() / gameBoard.heightInTiles;
				float tileWidth = boardTable.getWidth() / gameBoard.widthInTiles;
				Point initialPointInWorld = pointInWorld(move.previousPosition.x, move.previousPosition.y);

				Table movetoDisplay = MoveFactory
						.createShipForDisplay(move, tileHeight, tileWidth, initialPointInWorld);

				Point newPosition = pointInWorld(move.currentPosition.x, move.currentPosition.y);

				if (!roundHasAlreadyBeenAnimated()) {
					movetoDisplay.addAction(Actions.moveTo(newPosition.x + (tileWidth / 2), newPosition.y
							+ (tileHeight / 2), 1.2f));
				} else {
					movetoDisplay.setPosition(newPosition.x + (tileWidth / 2), newPosition.y + (tileHeight / 2));
				}

				if (move.executed && !roundHasAlreadyBeenAnimated()) {
					movetoDisplay.addAction(Actions.scaleTo(0, 0, 0.9f));
				}
				stage.addActor(movetoDisplay);
			}
		}

		roundAnimated = gameBoard.roundInformation.currentRound;

	}

	private Table setupRotationWrapper(ImageButton shipMoveButton, float tileHeight, float tileWidth, Point movePosition) {
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
		moveHud = new MoveHud(assetManager, skin, gameBoard, fontShader, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight() * 0.1f);
		moveHud.addMoves(moves);

		moveHud.addListener(new MoveListener() {

			@Override
			protected void performMove(Move move) {
				if (move.startingRound == gameBoard.roundInformation.currentRound && !GameLoop.USER.hasMoved(gameBoard)) {
					renderMoveDialog(move);
				} else {
					highlightMove(move);
				}
			}

			@Override
			public void sendMove() {
				List<Move> currentRoundMoves = new ArrayList<Move>();
				for (Move move : moves) {
					if (move.startingRound == gameBoard.roundInformation.currentRound) {
						currentRoundMoves.add(move);
					}
				}
				moveOverlay = new TextOverlay("Uploading ship movements", menuAtlas, skin, fontShader);
				stage.addActor(moveOverlay);
				UIConnectionWrapper.performMoves(new PerformMoveResultHandler(), gameBoard.id, currentRoundMoves,
						inProgressHarvest);
			}

		});

		stage.addActor(moveHud);

	}

	private void highlightMove(Move move) {
		float tileWidthInWorld = boardTable.getWidth() / gameBoard.widthInTiles;
		float tileHeightInWorld = boardTable.getHeight() / gameBoard.heightInTiles;

		PlanetButtonFactory.setup(assetManager, tileWidthInWorld, tileHeightInWorld);
		final PlanetButton fromPlanet = PlanetButtonFactory.createPlanetButtonWithExpansion(
				gameBoard.getPlanet(move.fromPlanet), gameBoard, roundHasAlreadyBeenAnimated());
		final PlanetButton toPlanet = PlanetButtonFactory.createPlanetButtonWithExpansion(
				gameBoard.getPlanet(move.toPlanet), gameBoard, roundHasAlreadyBeenAnimated());
		positionPlanet(fromPlanet);
		positionPlanet(toPlanet);

		Point position = pointInWorld(move.currentPosition.x, move.currentPosition.y);
		final Table moveToDisplay = MoveFactory.createShipForDisplay(move, PlanetButtonFactory.tileHeightInWorld,
				PlanetButtonFactory.tileWidthInWorld, position);
		
		final SingleMoveInfoHud sinlgeMoveHud = new SingleMoveInfoHud(move, assetManager, fontShader,skin,  Gdx.graphics.getWidth(), moveHud.getHeight());
		sinlgeMoveHud.setX(moveHud.getX());
		sinlgeMoveHud.setY(moveHud.getY());

		DismissableOverlay overlay = new DismissableOverlay(menuAtlas, 0.9f, new ClickListener() {

			@Override
			public void clicked(InputEvent event, float x, float y) {
				fromPlanet.remove();
				toPlanet.remove();
				moveToDisplay.remove();
				sinlgeMoveHud.remove();
			}
		});
		stage.addActor(overlay);
		stage.addActor(fromPlanet);
		stage.addActor(toPlanet);
		stage.addActor(moveToDisplay);
		stage.addActor(sinlgeMoveHud);

		fromPlanet.addAction(fadePlanetInAndOut(fromPlanet));
		toPlanet.addAction(fadePlanetInAndOut(toPlanet));
		moveToDisplay.addAction(Actions.forever(Actions.sequence(Actions.color(new Color(0, 0, 0, 0), 0.9f),
				Actions.color(Color.RED, 0.9f))));

	}

	private RepeatAction fadePlanetInAndOut(PlanetButton planetButton) {
		return Actions.forever(Actions.sequence(Actions.color(new Color(0, 0, 0, 0), 0.7f),
				Actions.color(planetButton.planet.getColor(), 0.5f)));
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
		float yOffset = moveHud.getHeight();

		return new Point(tileWidthInWorld * x, (tileHeightInWorld * y) + yOffset);
	}

	private void createPlanets() {
		float tileWidthInWorld = boardTable.getWidth() / gameBoard.widthInTiles;
		float tileHeightInWorld = boardTable.getHeight() / gameBoard.heightInTiles;

		PlanetButtonFactory.setup(assetManager, tileWidthInWorld, tileHeightInWorld);

		for (final Planet planet : gameBoard.planets) {
			final PlanetButton planetButton = PlanetButtonFactory.createPlanetButtonWithExpansion(planet, gameBoard,
					roundHasAlreadyBeenAnimated());
			positionPlanet(planetButton);

			planetButton.addListener(new ClickListener() {

				@Override
				public void clicked(InputEvent event, float x, float y) {
						planetButton.addAction(Actions.forever(Actions.sequence(
								Actions.color(new Color(0, 0, 0, 0), 0.7f), Actions.color(planet.getColor(), 0.5f))));
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
			
			if(planet.hasAbility()){
				createMoon(planet);
			}
		}
	}

	private void createMoon(Planet planet) {
		final Moon moon = PlanetButtonFactory.createMoon(assetManager, planet,
				 PlanetButtonFactory.tileHeightInWorld * 0.4f, PlanetButtonFactory.tileWidthInWorld * 0.4f);
		
		PlanetButton associatedAbilityPlanet = planetButtons.get(planet.name);
		float relativeX = associatedAbilityPlanet.centerPoint().x - (moon.getWidth() / 2);
		float relativeY = associatedAbilityPlanet.centerPoint().y - (moon.getHeight() / 2);
		
		moon.setX(relativeX - PlanetButtonFactory.tileWidthInWorld / 2);
		moon.setY(relativeY);
		
		moons.add(moon);
		stage.addActor(moon);	
	}

	private void positionPlanet(final PlanetButton planetButton) {
		float yOffset = boardTable.getY();

		planetButton.setX(PlanetButtonFactory.tileWidthInWorld * planetButton.planet.position.x);
		planetButton.setY((PlanetButtonFactory.tileHeightInWorld * planetButton.planet.position.y) + yOffset);
		planetButton.setColor(planetButton.planet.getColor());

		float xAdjust = (PlanetButtonFactory.tileWidthInWorld / 2) - (planetButton.getWidth() / 2);
		planetButton.setX(planetButton.getX() + xAdjust);

		float yAdjust = (PlanetButtonFactory.tileHeightInWorld / 2) - (planetButton.getHeight() / 2);
		planetButton.setY(planetButton.getY() + yAdjust);
	}

	private void renderDialog() {
		if (touchedPlanets.size() > 1) {
			if(GameLoop.USER.hasMoved(gameBoard)){
				if(planetsAreTheSame(touchedPlanets)){
					renderPlanetInformationDialog(touchedPlanets.get(0));
				}else{
					highlightNewPlanet();
				}
			}else{
				if(planetsAreTheSame(touchedPlanets)){
					renderPlanetInformationDialog(touchedPlanets.get(0));					
				}else if(!userPlanetInSelection()){
					highlightNewPlanet();
				}else{
					Planet userPlanet = touchedUserPlanet(touchedPlanets);
					Planet otherPlanet = otherPlanet(touchedPlanets, userPlanet);
					renderMoveDialog(userPlanet, otherPlanet);
				}
			}
		}
	}

	private boolean userPlanetInSelection() {
		for(Planet planet : touchedPlanets){
			if(planet.isOwnedBy(GameLoop.USER)){
				return true;
			}
		}
		return false;
	}

	private void highlightNewPlanet() {
		clearMoveActions(touchedPlanets.get(0));
		Planet toKeep = touchedPlanets.get(1);
		touchedPlanets.clear();
		touchedPlanets.add(toKeep);
		
	}

	private boolean planetsAreTheSame(List<Planet> touchedPlanets2) {
		return touchedPlanets2.get(0).name.equals(touchedPlanets2.get(1).name);
	}

	private void renderPlanetInformationDialog(final Planet planet) {
		int offset = planetToMoveCount.get(planet.name) != null ? planetToMoveCount.get(planet.name) : 0;
		;
		PlanetInformationDialog dialog = new PlanetInformationDialog(assetManager, Gdx.graphics.getWidth() * 0.8f,
				Gdx.graphics.getHeight() * 0.4f, stage, planet, gameBoard, roundHasAlreadyBeenAnimated(), fontShader,
				skin, offset);
		float dialogY = Gdx.graphics.getHeight() - (dialog.getHeight() + (dialog.getHeight() * 0.5f));
		dialog.setX(-dialog.getWidth());
		dialog.setY(dialogY);
		stage.addActor(dialog);
		dialog.show(new Point(Gdx.graphics.getWidth() * 0.1f, dialogY), 0.4f);

		dialog.addListener(new DialogEventListener() {
			@Override
			public void cancelDialog() {
				super.cancelDialog();
				clearMoveActions(planet);
				touchedPlanets.clear();
			}
		});

		dialog.addListener(new MoveListener() {
			@Override
			public void handleHarvest(Planet planet) {
				inProgressHarvest.add(MoveFactory.createHarvestMove(planet));
				clearTouchedPlanets();
				clearMoveActions(planet);
			}

		});
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
		moveDialog = new MoveDialog(one, two, offSetCount, assetManager,
				boardTable.getWidth() * 0.8f, boardTable.getHeight() * 0.25f, skin, gameBoard.roundInformation.currentRound, stage);
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
		float initialX = boardTable.getX() + boardTable.getWidth() * 0.1f;

		moveDialog.setPosition(-boardTable.getWidth(), initialY);

		moveDialog.show(new Point(initialX, initialY));
		stage.addActor(moveDialog);
	}

	private void renderMoveDialog(final Move move) {
		int offset = planetToMoveCount.get(move.fromPlanet(gameBoard.planets).name);
		moveDialog = new ExistingMoveDialog(move, move.fromPlanet(gameBoard.planets), move.toPlanet(gameBoard.planets),
				offset, assetManager, boardTable.getWidth() * 0.8f, boardTable.getHeight() * 0.25f, skin, gameBoard.roundInformation.currentRound, stage);
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
		if (newMove.startingRound == gameBoard.roundInformation.currentRound) {
			Integer count = planetToMoveCount.get(newMove.fromPlanet);
			planetToMoveCount.put(newMove.fromPlanet, count + newMove.shipsToMove);
		}
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
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		if (planetMoveChange) {
			applyMovesToPlanetButtons();
			planetMoveChange = false;
		}	
		renderMoons();

		stage.act(delta);
		stage.draw();
	}

	private void renderMoons() {
		for(Moon moon : moons){
			Point newPosition = findMoonPosition(moon);
			if(newPosition != null){
				moon.setX(newPosition.x - (moon.getWidth() /2));
				moon.setY(newPosition.y - (moon.getHeight() / 2));
			}
		}
		
	}

	private Point findMoonPosition(Moon moon) {
		PlanetButton associatedPlanet = planetButtons.get(moon.associatedPlanet.name);
		Point movePoint = null;
		if(associatedPlanet != null && gameBoard != null){
			if(moon.angle == 360){
				moon.angle = 0;
			}
			
			float tileWidthInWorld = boardTable.getWidth() / gameBoard.widthInTiles;
			float tileHeightInWorld = boardTable.getHeight() / gameBoard.heightInTiles;
			
			movePoint = GalConMath.nextPointInEllipse(associatedPlanet.centerPoint(), tileWidthInWorld * 0.6f, tileHeightInWorld / 2, moon.angle);
			moon.angle = (float) (moon.angle + moon.rateOfOrbit);
		}
		
		
		return movePoint;
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
			inProgressMoves.clear();
			inProgressHarvest.clear();
			clearTouchedPlanets();
			planetToMoveCount.clear();
			setGameBoard(result);
			moveOverlay.remove();
		}

		@Override
		public void onConnectionError(String msg) {
			moveOverlay.remove();
		}
	}

	public class FindGameByIdResultHandler implements UIConnectionResultCallback<GameBoard> {

		@Override
		public void onConnectionResult(GameBoard result) {
			inProgressMoves.clear();
			clearTouchedPlanets();
			planetToMoveCount.clear();
			inProgressHarvest.clear();
			moves.clear();

			setGameBoard(result);
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
