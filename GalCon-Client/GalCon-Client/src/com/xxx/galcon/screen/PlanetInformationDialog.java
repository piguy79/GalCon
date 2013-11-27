/**
 * 
 */
package com.xxx.galcon.screen;



import static com.xxx.galcon.Util.createShader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.Constants;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.screen.BoardScreen.WorldPlane;
import com.xxx.galcon.screen.widget.ShaderLabel;
import com.xxx.galcon.screen.widget.ShaderTextButton;

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
	
	private boolean isReady = false;
	private boolean isShownAndClosed = false;
	
	private InputProcessor oldInputProcessor;
	
	TextureAtlas gameBoardAtlas;
	TextureAtlas menuAtlas;
	TextureAtlas planetAtlas;

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
	
	private AssetManager assetManager;


	public PlanetInformationDialog(int x, int y, int width, int height,
			 Planet planet, UISkin skin, AssetManager assetManager, Camera camera, BoardScreen screen) {
		super(x, y, width, height, false);
		this.planet = planet;
		this.skin = skin;
		this.camera = camera;
		this.screen = screen;
		this.assetManager = assetManager;
		spriteBatch = new SpriteBatch();

		stage = new Stage();
		
		gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);
		TextureAtlas menuAtlas = assetManager.get("data/images/menus.atlas", TextureAtlas.class);
		planetAtlas = assetManager.get("data/images/planets.atlas", TextureAtlas.class);

		dialogTextureBg = menuAtlas.findRegion("dialog_bg");
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
		Drawable bg = new TextureRegionDrawable(dialogTextureBg);
		bg.setMinHeight(height * 0.3f);
		planetInfoTable.setBackground(bg);
		planetInfoTable.setWidth(width);
		planetInfoTable.setHeight(height);
		planetInfoTable.setX(-width);
		planetInfoTable.setY(height * 0.4f);
		
		addPlanet();
		planetInfoTable.add(new ShaderLabel(fontShader, "Regen Rate: ", skin, Constants.UI.DEFAULT_FONT_BLACK)).right();
		planetInfoTable.add(new ShaderLabel(fontShader, " " + planet.shipRegenRate, skin, Constants.UI.DEFAULT_FONT_BLACK));
		planetInfoTable.row();
		planetInfoTable.add(new ShaderLabel(fontShader, "Population: ", skin, Constants.UI.DEFAULT_FONT_BLACK)).right();
		planetInfoTable.add(new ShaderLabel(fontShader, "10,000,000", skin, Constants.UI.DEFAULT_FONT_BLACK));
		if(planet.hasAbility()){
			planetInfoTable.row();
			planetInfoTable.add(new ShaderLabel(fontShader, "Ability: ", skin, Constants.UI.DEFAULT_FONT_BLACK)).right();
			planetInfoTable.add(new ShaderLabel(fontShader, planet.ability, skin, Constants.UI.DEFAULT_FONT_BLACK));
			if(planet.isUnderHarvest()){
				planetInfoTable.row();
				planetInfoTable.add(new ShaderLabel(fontShader, "Rounds left: ", skin, Constants.UI.DEFAULT_FONT_BLACK));
				planetInfoTable.add(new ShaderLabel(fontShader, " " +  planet.harvest.startingRound, skin, Constants.UI.DEFAULT_FONT_BLACK));
			}
		}
		MoveToAction action = new MoveToAction();
		action.setDuration(0.2f);
		action.setPosition(width * 0.02f, height * 0.2f);
		
		planetInfoTable.addAction(action);
		
		
		stage.addActor(planetInfoTable);
		
		addCancelButton();
		addHarvestButton();

	}

	
	private void addPlanet() {
		TextureRegionDrawable planetTexture = new TextureRegionDrawable(planetAtlas.findRegion("planet2"));
		planetTexture.setMinHeight(height * 0.35f);
		planetTexture.setMinWidth(width * 0.5f);
		TextButtonStyle style = new TextButtonStyle(planetTexture, planetTexture, planetTexture, Fonts.getInstance(assetManager).mediumFont());
		TextButton planetImage = new ShaderTextButton(fontShader,"" + planet.numberOfShips, style);
		planetImage.setColor(planet.getColor());
		planetInfoTable.add(planetImage).colspan(2).expandX().center();
		planetInfoTable.row();
	}


	private void addHarvestButton() {
		if(planet.hasAbility() && !planet.isUnderHarvest()){
			float buttonSize = width * 0.15f;
			
			harvestButton = new ImageButton(skin, "okButton");
			harvestButton.setX(width - buttonSize);
			harvestButton.setY(height * 0.27f);
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
		cancelButton.setY(height * 0.27f);
		cancelButton.setX(width * 0.03f);
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
		planetInfoTable.addAction(Actions.sequence(Actions.delay(0.2f),moveTo, Actions.run(new Runnable() {
			
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
