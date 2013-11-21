/**
 * 
 */
package com.xxx.galcon.screen;



import static com.xxx.galcon.Util.createShader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.Constants;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.screen.BoardScreen.WorldPlane;
import com.xxx.galcon.screen.widget.ShaderLabel;

/**
 * @author conormullen
 *
 */
public class PlanetInformationDialog extends TouchRegion implements ScreenFeedback {
	
	private Planet planet;

	private AtlasRegion dialogTextureBg;
	private Texture planetNumbersTexture;
	private AtlasRegion planetTexture;
	private AtlasRegion planetTouchTexture;
	
	private Model planetModel;
	
	
	SpriteBatch spriteBatch;
	
	private boolean isBaseDialogReady = false;
	private boolean isReady = false;
	private boolean isShownAndClosed = false;
	
	private InputProcessor oldInputProcessor;
	
	TextureAtlas gameBoardAtlas;

	private String returnResult;
	private String pendingReturnResult;
		
	private Stage stage;
	private Table planetInfoTable;
	
	private ShaderProgram fontShader;
	private ShaderProgram planetShader;
	private UISkin skin;
	
	private Camera camera;
	private Matrix4 modelViewMatrix = new Matrix4();
	private ImageButton cancelButton;
	private ImageButton harvestButton;
	
	
	private BoardScreen screen;


	public PlanetInformationDialog(int x, int y, int width, int height,
			 Planet planet, UISkin skin, AssetManager assetManager, Camera camera, BoardScreen screen) {
		super(x, y, width, height, false);
		this.planet = planet;
		this.skin = skin;
		this.camera = camera;
		this.screen = screen;
		spriteBatch = new SpriteBatch();

		stage = new Stage();
		
		gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);
		dialogTextureBg = gameBoardAtlas.findRegion("ship_selection_dialog_bg");
		TextureAtlas planetAtlas = assetManager.get("data/images/planets.atlas", TextureAtlas.class);
		planetTexture = planetAtlas.findRegion("planet3");
		planetTouchTexture = planetAtlas.findRegion("planet3-touch");
		planetNumbersTexture = assetManager.get("data/fonts/planet_numbers.png", Texture.class);
		
		fontShader = createShader("data/shaders/font-vs.glsl", "data/shaders/font-fs.glsl");
		planetShader = createShader("data/shaders/planet-vs.glsl", "data/shaders/planet-fs.glsl");

		planetModel = generateModelFromObjectFile("data/models/planet.obj");
		planetModel.meshes.get(0).setAutoBind(false);
		
		createPlanetInfo();
	}


	private Model generateModelFromObjectFile(String objectFile) {
		ObjLoader loader = new ObjLoader();
		return loader.loadObj(Gdx.files.internal(objectFile));
	}

	private void createPlanetInfo() {
		planetInfoTable = new Table();
		planetInfoTable.setBackground(new TextureRegionDrawable(dialogTextureBg));
		planetInfoTable.setWidth(width);
		planetInfoTable.setHeight(height);
		planetInfoTable.setX(-width);
		planetInfoTable.setY(height * 0.2f);

		planetInfoTable.add(new ShaderLabel(fontShader, "Regen Rate: ", skin, Constants.UI.DEFAULT_FONT)).left().padTop(height * 0.2f);
		planetInfoTable.add(new ShaderLabel(fontShader, " " + planet.shipRegenRate, skin, Constants.UI.DEFAULT_FONT)).left().padTop(height * 0.2f);
		planetInfoTable.row();
		planetInfoTable.add(new ShaderLabel(fontShader, "Population: ", skin, Constants.UI.DEFAULT_FONT)).left();
		planetInfoTable.add(new ShaderLabel(fontShader, "10,000,000", skin, Constants.UI.DEFAULT_FONT)).left();
		if(planet.hasAbility()){
			planetInfoTable.row();
			planetInfoTable.add(new ShaderLabel(fontShader, "Ability: ", skin, Constants.UI.DEFAULT_FONT)).left();
			planetInfoTable.add(new ShaderLabel(fontShader, planet.ability, skin, Constants.UI.DEFAULT_FONT)).left();
			if(planet.isUnderHarvest()){
				planetInfoTable.row();
				planetInfoTable.add(new ShaderLabel(fontShader, "Rounds left: ", skin, Constants.UI.DEFAULT_FONT));
				planetInfoTable.add(new ShaderLabel(fontShader, " " +  planet.harvest.startingRound, skin, Constants.UI.DEFAULT_FONT));
			}
		}
		MoveToAction action = new MoveToAction();
		action.setDuration(0.2f);
		action.setPosition(width * 0.05f, height * 0.2f);
		
		planetInfoTable.addAction(Actions.sequence(action, Actions.run(new Runnable() {
			
			@Override
			public void run() {
				isBaseDialogReady = true;
			}
		})));
		
		
		stage.addActor(planetInfoTable);
		
		addCancelButton();
		addHarvestButton();

	}

	
	private void addHarvestButton() {
		if(planet.hasAbility() && !planet.isUnderHarvest()){
			float buttonSize = width * 0.15f;
			
			harvestButton = new ImageButton(skin, "okButton");
			harvestButton.setX(width - buttonSize);
			harvestButton.setY(height * 0.28f);
			harvestButton.setWidth(width * 0.15f);
			harvestButton.setHeight(width * 0.15f);
			harvestButton.setColor(0, 0, 0, 0);
			harvestButton.addListener(new InputListener(){
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					return true;
				}

				@Override
				public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
					pendingReturnResult = Action.HARVEST;
					hide();
				}
			});

			harvestButton.addAction(Actions.sequence(Actions.delay(0.2f), Actions.fadeIn(0.4f)));
			stage.addActor(harvestButton);
			
		}
		
	}

	private void addCancelButton() {
		cancelButton = new ImageButton(skin, "cancelButton");
		cancelButton.setWidth(width * 0.15f);
		cancelButton.setHeight(width * 0.15f);
		cancelButton.setY(height * 0.28f);
		cancelButton.setX(width * 0.08f);
		cancelButton.setColor(0,0,0,0);
		cancelButton.addListener(new InputListener(){
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				pendingReturnResult = Action.DIALOG_CANCEL;
				hide();
				
			}
		});
		cancelButton.addAction(Actions.sequence(Actions.delay(0.2f), Actions.fadeIn(0.4f)));
		stage.addActor(cancelButton);
		
	}
	

	public boolean isReady() {
		return isReady;
	}

	@Override
	public void render(float delta) {
		if (pendingReturnResult != null && isShownAndClosed) {
			returnResult = pendingReturnResult;
		}
		
		stage.act(delta);
		stage.draw();
		
		if(isBaseDialogReady){
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			renderPlanet();
			Gdx.gl.glDisable(GL20.GL_BLEND);
		}
	}
	
	private void renderPlanet() {
		planetTexture.getTexture().bind(1);
		planetTouchTexture.getTexture().bind(2);
		planetNumbersTexture.bind(3);

		planetShader.begin();
		planetShader.setUniformi("planetTex", 1);
		planetShader.setUniformi("planetTouchTex", 2);
		planetShader.setUniformi("numbersTex", 3);

		planetShader.setUniformMatrix("uPMatrix", camera.combined);

		Mesh mesh = planetModel.meshes.get(0);
		mesh.bind(planetShader);
		modelViewMatrix.idt();
		
		WorldPlane plane = screen.new WorldPlane();
		
				
		modelViewMatrix.trn(0, 0,
				-99f);
		modelViewMatrix.translate(0, plane.getWorldHeight(camera) * 0.2f
				, 0);

		modelViewMatrix.scale(plane.getWorldWidth(camera) * 0.032f, plane.getWorldHeight(camera) * 0.023f, 1.0f);

		planetShader.setUniformMatrix("uMVMatrix", modelViewMatrix);

		float [] planetBits = planet.getPlanetBits();
		planetShader.setUniform1fv("uPlanetBits", planetBits, 0, 4);
		planetShader.setUniformf("radius", plane.getWorldWidth(camera) * 0.2f);
		planetShader.setUniformi("shipCount",
				planet.numberOfShips);
		planetModel.meshes.get(0).render(planetShader, GL20.GL_TRIANGLES);
		
		mesh.unbind(planetShader);
		planetShader.end();

		Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);
		
	}

	@Override
	public void resize(int width, int height) {
		
	}

	@Override
	public void show() {
		oldInputProcessor = Gdx.input.getInputProcessor();
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void hide() {
		cancelButton.addAction(Actions.fadeOut(0.2f));
		if(harvestButton != null){
			harvestButton.addAction(Actions.fadeOut(0.2f));
		}
		
		MoveToAction moveTo = new MoveToAction();
		moveTo.setPosition(-width, planetInfoTable.getY());
		moveTo.setDuration(0.2f);
		planetInfoTable.addAction(Actions.sequence(Actions.delay(0.2f), Actions.run(new Runnable() {
			@Override
			public void run() {
				isBaseDialogReady = false;
			}
		}) ,moveTo, Actions.run(new Runnable() {
			
			@Override
			public void run() {
				isShownAndClosed = true;
			}
		})));
		Gdx.input.setInputProcessor(oldInputProcessor);
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
	public void dispose() {
		stage.dispose();
		hide();
	}

	@Override
	public Object getRenderResult() {
		return returnResult;
	}

	@Override
	public void resetState() {
		// TODO Auto-generated method stub
		
	}

	public Planet getPlanet() {
		return planet;
	}

}
