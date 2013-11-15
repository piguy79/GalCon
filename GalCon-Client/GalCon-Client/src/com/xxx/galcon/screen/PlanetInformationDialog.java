/**
 * 
 */
package com.xxx.galcon.screen;



import static aurelienribon.tweenengine.TweenCallback.COMPLETE;
import static aurelienribon.tweenengine.TweenCallback.END;
import static com.xxx.galcon.Constants.OWNER_NO_ONE;
import static com.xxx.galcon.Util.createShader;

import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout.Alignment;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.InGameInputProcessor;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.InGameInputProcessor.TouchPoint;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.model.tween.ColorTween;
import com.xxx.galcon.model.tween.PlanetInformationTween;
import com.xxx.galcon.screen.hud.Button;
import com.xxx.galcon.screen.widget.ShaderLabel;

/**
 * @author conormullen
 *
 */
public class PlanetInformationDialog extends TouchRegion implements ScreenFeedback {
	
	private Planet planet;
	private TweenManager tweenManager;
	
	private AtlasRegion cancelButtonTex;
	private AtlasRegion okButtonTex;

	private AtlasRegion dialogTextureBg;
	private Texture planetNumbersTexture;
	private AtlasRegion planetTexture;
	private AtlasRegion planetTouchTexture;
	
	private Model planetModel;
	
	private List<Button> buttons = new ArrayList<Button>();
	
	SpriteBatch spriteBatch;
	
	private static final String CANCEL = "cancel";
	private static final String OK = "ok";
	private static final int INDEX_PLANET_OWNED_BY_USER = 0;
	private static final int INDEX_PLANET_OWNED_BY_ENEMY = 1;
	private static final int INDEX_PLANET_TOUCHED = 2;
	private static final int INDEX_PLANET_ABILITY = 3;
	
	private boolean isBaseDialogReady = false;
	private boolean isReady = false;
	private boolean isShownAndClosed = false;
	private boolean buttonsUpdated = false;
	
	private static final String TWEEN_ID_SHOW = "show";
	private static final String TWEEN_ID_BUTTON_SHOW = "button show";
	private static final String TWEEN_ID_HIDE = "hide";
	private static final String TWEEN_ID_BUTTON_HIDE = "button hide";
	
	private TextureAtlas menusAtlas;
	TextureAtlas gameBoardAtlas;

	private String returnResult;
	private String pendingReturnResult;
	
	private Color alphaAnimColor = new Color(1.0f, 1.0f, 1.0f, 0.0f);
	
	private Stage stage;
	private Table planetInfoTable;
	
	private ShaderProgram fontShader;
	private ShaderProgram planetShader;
	private UISkin skin;
	
	private Camera camera;
	private Matrix4 modelViewMatrix = new Matrix4();
	
	private float[] planetBits = new float[4];
	



	public PlanetInformationDialog(int x, int y, int width, int height,
			 Planet planet, UISkin skin, AssetManager assetManager, TweenManager tweenManager, Camera camera) {
		super(x, y, width, height, false);
		this.planet = planet;
		this.skin = skin;
		this.tweenManager = tweenManager;
		this.camera = camera;
		spriteBatch = new SpriteBatch();
		stage = new Stage();
		
		menusAtlas = assetManager.get("data/images/menus.atlas", TextureAtlas.class);
		gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);
		
		okButtonTex = menusAtlas.findRegion("ok_button");
		cancelButtonTex = menusAtlas.findRegion("cancel_button");
		dialogTextureBg = gameBoardAtlas.findRegion("ship_selection_dialog_bg");
		TextureAtlas planetAtlas = assetManager.get("data/images/planets.atlas", TextureAtlas.class);
		planetTexture = planetAtlas.findRegion("planet3");
		planetTouchTexture = planetAtlas.findRegion("planet3-touch");
		planetNumbersTexture = assetManager.get("data/fonts/planet_numbers.png", Texture.class);

		
		Tween.registerAccessor(Color.class, new ColorTween());
		
		fontShader = createShader("data/shaders/font-vs.glsl", "data/shaders/font-fs.glsl");
		planetShader = createShader("data/shaders/planet-vs.glsl", "data/shaders/planet-fs.glsl");

		planetModel = generateModelFromObjectFile("data/models/planet.obj");
		planetModel.meshes.get(0).setAutoBind(false);
		
		addCancelButton();
		addHarvestButton();
		createPlanetInfo();
	}
	
	private void addHarvestButton() {
		if(planet.hasAbility() && !planet.isUnderHarvest()){
			buttons.add(new Button(okButtonTex) {
				@Override
				public void updateLocationAndSize(int x, int y, int width, int height) {
					int buttonSize = (int) (width * 0.15f);
					int margin = 0;

					this.x = x + width - margin - buttonSize;
					this.y = y + margin;
					this.height = buttonSize;
					this.width = buttonSize;
				}

				@Override
				public String getActionOnClick() {
					return OK;
				}
			});
		}
	}

	private Model generateModelFromObjectFile(String objectFile) {
		ObjLoader loader = new ObjLoader();
		return loader.loadObj(Gdx.files.internal(objectFile));
	}

	private void createPlanetInfo() {
		planetInfoTable = new Table();
		planetInfoTable.setWidth(width);
		planetInfoTable.setHeight(height);
		planetInfoTable.setX(width * 0.2f);
		planetInfoTable.setY(y * 0.19f);
		planetInfoTable.align(Align.left);
		
		planetInfoTable.add(new ShaderLabel(fontShader, "Regen Rate: ", skin, Constants.UI.DEFAULT_FONT)).left();
		planetInfoTable.add(new ShaderLabel(fontShader, " " + planet.shipRegenRate, skin, Constants.UI.DEFAULT_FONT)).left();
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
		
		stage.addActor(planetInfoTable);
		
	}
	

	private void addCancelButton() {
		buttons.add(new Button(cancelButtonTex) {
			@Override
			public void updateLocationAndSize(int x, int y, int width, int height) {
				int buttonSize = (int) (width * 0.15f);
				int margin = 0;

				this.x = x + margin;
				this.y = y + margin;
				this.height = buttonSize;
				this.width = buttonSize;
			}

			@Override
			public String getActionOnClick() {
				return CANCEL;
			}
		});
	}
	
	public boolean isReady() {
		return isReady;
	}
	
	public float getX() {
		return super.x;
	}

	public void setX(int x) {
		super.x = x;
	}

	public float getY() {
		return super.y;
	}

	public void setY(int y) {
		super.y = y;
	}

	@Override
	public void render(float delta) {
		processTouch();
		
		
		
		spriteBatch.begin();
		spriteBatch.draw(dialogTextureBg, x, y, width, height);
		
		if(isBaseDialogReady){
			spriteBatch.setColor(alphaAnimColor);
			for(int i = 0; i < buttons.size(); i++){
				Button button = buttons.get(i);
				if(!buttonsUpdated){
					button.updateLocationAndSize(x, y, width, height);
				}
				button.render(spriteBatch);
			}
			buttonsUpdated = true;
			spriteBatch.setColor(Color.WHITE);
			
		}
		
		spriteBatch.end();
		
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
		
		int screenHeight = Gdx.graphics.getHeight();
		
		modelViewMatrix.trn(0, screenHeight * 0.04f,
				-99f);

		float radius = scaleRegenToRadius(planet, 0.25f, 0.48f);
		modelViewMatrix.scale(width * 0.01f, height * 0.006f, 1.0f);
		modelViewMatrix.scl(radius * 2.2f, radius * 2.2f, 1.0f);

		planetShader.setUniformMatrix("uMVMatrix", modelViewMatrix);

		setPlanetBits(planet, planetBits);
		planetShader.setUniform1fv("uPlanetBits", planetBits, 0, 4);
		planetShader.setUniformf("radius", radius);
		planetShader.setUniformi("shipCount",
				planet.numberOfShips);
		planetModel.meshes.get(0).render(planetShader, GL20.GL_TRIANGLES);
		
		mesh.unbind(planetShader);
		planetShader.end();

		Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);
		
	}
	
	private void setPlanetBits(Planet planet, float[] planetBits) {

		String planetOwner = planet.owner;
		planetBits[INDEX_PLANET_TOUCHED] = planet.touched ? 1.0f : 0.0f;
		planetBits[INDEX_PLANET_ABILITY] = planet.hasAbility() ? 1.0f : 0.0f;
		planetBits[INDEX_PLANET_OWNED_BY_USER] = planetOwner.equals(GameLoop.USER.handle) ? 1.0f : 0.0f;
		planetBits[INDEX_PLANET_OWNED_BY_ENEMY] = !planetOwner.equals(OWNER_NO_ONE)
				&& !planetOwner.equals(GameLoop.USER.handle) ? 1.0f : 0.0f;
	}
	
	private float scaleRegenToRadius(Planet planet, float minRadius, float maxRadius) {
		float regenRatio = planet.shipRegenRate / Constants.SHIP_REGEN_RATE_MAX;
		return minRadius + (maxRadius - minRadius) * regenRatio;
	}

	private void processTouch() {
		returnResult = null;

		InGameInputProcessor ip = (InGameInputProcessor) Gdx.input.getInputProcessor();
		if (ip.didTouch() && isReady) {
			TouchPoint touchPoint = ip.getTouch();
			int x = touchPoint.x;

			for (int i = 0; i < buttons.size(); ++i) {
				Button button = buttons.get(i);
				if (button.isTouched(x, y)) {
					ip.consumeTouch();
					if (button.getActionOnClick().equals(CANCEL)) {
						pendingReturnResult = Action.DIALOG_CANCEL;
						hide();
					}else if(button.getActionOnClick().equals(OK)){
						pendingReturnResult = Action.HARVEST;
						hide();
					}
				}
			}
		}

		if (pendingReturnResult != null && isShownAndClosed) {
			returnResult = pendingReturnResult;
		}
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void show() {
		int targetwidth = Gdx.graphics.getWidth();
		int targetheight = Gdx.graphics.getHeight();
		int xMargin = (int) (targetwidth * .1f);

		ShowDialogCallback cb = new ShowDialogCallback();

		Tween showAnimation = Tween.to(this, PlanetInformationTween.POSITION_XY, 0.3f)
				.target(xMargin, (int) (targetheight * .2f)).ease(TweenEquations.easeOutQuad)
				.setUserData(TWEEN_ID_SHOW).setCallback(cb).setCallbackTriggers(END);
		Tween showButtonAnimation = Tween.to(this.alphaAnimColor, ColorTween.ALPHA, 0.2f)
				.target(1.0f, 1.0f, 1.0f, 1.0f).setUserData(TWEEN_ID_BUTTON_SHOW).setCallback(cb)
				.setCallbackTriggers(END);

		Timeline.createSequence().push(showAnimation).push(showButtonAnimation).start(tweenManager);
		
	}

	@Override
	public void hide() {
		
		int targetwidth = Gdx.graphics.getWidth();
		int targetheight = Gdx.graphics.getHeight();

		Tween hideAlphaAnimation = Tween.to(this.alphaAnimColor, ColorTween.ALPHA, 0.2f).target(1.0f, 1.0f, 1.0f, 0.0f)
				.setUserData(TWEEN_ID_BUTTON_HIDE);

		Tween hideAnimation = Tween.to(this, PlanetInformationTween.POSITION_XY, 0.3f)
				.target(targetwidth * -1, (int) (targetheight * .6f)).ease(TweenEquations.easeInQuad)
				.setUserData(TWEEN_ID_HIDE);

		Timeline.createSequence().push(hideAlphaAnimation).push(hideAnimation).setCallback(new HideDialogCallback())
				.setCallbackTriggers(COMPLETE).start(tweenManager);
		
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
		if(returnResult != null){
			if(returnResult.equals(CANCEL)){
				returnResult = Action.DIALOG_DELETE;
			}
		}
		
		return returnResult;
	}

	@Override
	public void resetState() {
		// TODO Auto-generated method stub
		
	}
	
	private class ShowDialogCallback implements TweenCallback {

		@Override
		public void onEvent(int type, BaseTween<?> source) {
			switch (type) {
			case END:
				if (source.getUserData().equals(TWEEN_ID_SHOW)) {
					isBaseDialogReady = true;
				} else if (source.getUserData().equals(TWEEN_ID_BUTTON_SHOW)) {
					isReady = true;
				}
				break;
			default:
				break;
			}

		}
	}

	private class HideDialogCallback implements TweenCallback {

		@Override
		public void onEvent(int type, BaseTween<?> source) {
			switch (type) {
			case COMPLETE:
				isShownAndClosed = true;
				break;
			default:
				break;
			}

		}
	}

	public Planet getPlanet() {
		return planet;
	}

}
