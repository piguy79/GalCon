package com.xxx.galcon.screen;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.rotateBy;
import static com.xxx.galcon.Util.createShader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.xxx.galcon.Constants;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.People;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.screen.widget.ActionButton;
import com.xxx.galcon.screen.widget.ShaderTextField;
import com.xxx.galcon.screen.widget.WaitImageButton;

public class FriendScreen implements ScreenFeedback {
	
	private MenuScreenContainer previousScreen;
	private InputProcessor oldInputProcessor;


	private AssetManager assetManager;
	private UISkin skin;
	private ShaderProgram fontShader;
	
	private Stage stage;
	private TextureAtlas menusAtlas;
	
	private WaitImageButton waitImage;
	private ActionButton backButton;
	private ShaderTextField searchBox;
	private ActionButton searchButton;
	
	private String returnCode = null;


	
	public FriendScreen(UISkin skin, AssetManager assetManager) {
		this.assetManager = assetManager;
		this.skin = skin;

		fontShader = createShader("data/shaders/font-vs.glsl", "data/shaders/font-fs.glsl");
		menusAtlas = assetManager.get("data/images/menus.atlas", TextureAtlas.class);

	}

	private void initialize() {
		createBg();
		createWaitImage();
		createBackButton();
		createSearchBox();
		createSearchButton();
		
		Gdx.input.setInputProcessor(stage);
	}

	private void createSearchButton() {
		Point position = new Point(searchBox.getX() + searchBox.getWidth() + (GraphicsUtils.actionButtonSize * 0.25f), searchBox.getY());
		searchButton = new ActionButton(skin, "okButton", position);
		
		searchButton.addListener(new ClickListener(){@Override
		public void clicked(InputEvent event, float x, float y) {
			UIConnectionWrapper.searchForPlayers(new UIConnectionResultCallback<People>() {
				
				@Override
				public void onConnectionResult(People result) {
					for(Player player: result.people){
						System.out.print(player.handle);
					}
					
				}
				
				@Override
				public void onConnectionError(String msg) {
					// TODO Auto-generated method stub
					
				}
			}, searchBox.getText());
		}});
		
		stage.addActor(searchButton);
		
	}

	private void createSearchBox() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		
		searchBox = new ShaderTextField(fontShader, "", skin, Constants.UI.TEXT_FIELD);
		searchBox.setWidth(width * 0.75f);
		searchBox.setHeight(height * .08f);
		searchBox.setX(width * 0.5f - searchBox.getWidth() * 0.6f);
		searchBox.setY(backButton.getY() - (height * 0.1f));
		searchBox.setOnscreenKeyboard(new ShaderTextField.DefaultOnscreenKeyboard());
		
		stage.addActor(searchBox);
		
	}

	private void createBackButton() {
		Point position = new Point(10, 0);
		backButton = new ActionButton(skin, "backButton", position);
		GraphicsUtils.setCommonButtonSize(backButton);
		backButton.setX(position.x);
		backButton.setY(Gdx.graphics.getHeight() - backButton.getHeight() - 5);
		backButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				System.out.println("Clicked");
				stage.dispose();
				returnCode = Action.BACK;
			}
		});
		stage.addActor(backButton);
	}

	private void createWaitImage() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();

		waitImage = new WaitImageButton(skin);
		float buttonWidth = .25f * (float) width;
		waitImage.setWidth(buttonWidth);
		waitImage.setHeight(buttonWidth);
		waitImage.setX(width / 2 - buttonWidth / 2);
		waitImage.setY(height / 2 - buttonWidth / 2);
		stage.addActor(waitImage);

		waitImage.start();
		
	}

	private void createBg() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		
		Image bgImage = new Image(menusAtlas.findRegion("bg"));
		bgImage.setX(-2 * width);
		bgImage.setWidth(width * 4);
		bgImage.setY(-0.5f * height);
		bgImage.setHeight(height * 2f);
		bgImage.setColor(0.0f, 0.7f, 0.7f, 0.6f);
		bgImage.setOrigin((float) width * 2.0f, (float) height * 1.0f);
		bgImage.addAction(forever(rotateBy(360, 150)));
		stage.addActor(bgImage);
		
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		
	}

	@Override
	public void show() {
		stage = new Stage();
		initialize();
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getRenderResult() {
		return returnCode;
	}

	@Override
	public void resetState() {
		returnCode = null;
		
	}
	
	public MenuScreenContainer getPreviousScreen() {
		return previousScreen;
	}

	public void setPreviousScreen(MenuScreenContainer previousScreen) {
		this.previousScreen = previousScreen;
	}

}
