package com.xxx.galcon.screen;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.screen.event.GameStartEvent;
import com.xxx.galcon.screen.event.SocialGameStartEvent;
import com.xxx.galcon.screen.widget.CommonTextButton;
import com.xxx.galcon.screen.widget.OKCancelDialog;

public class GameStartDialog extends OKCancelDialog {
	
	private UISkin skin;
	private ShaderProgram fontShader;
	private float height;
	private float width;
	private int selectedMapKey;

	public GameStartDialog(AssetManager assetManager, float width,
			float height, Stage stage, UISkin skin, ShaderProgram fontShader, int selectedMapKey) {
		super(assetManager, width, height, stage, skin, Type.CANCEL);
		this.skin = skin;
		this.fontShader = fontShader;
		this.height = height;
		this.width = width;
		this.selectedMapKey = selectedMapKey;
		
		create();
	}

	private void create() {
		CommonTextButton randomPlay = new CommonTextButton(skin, "Play a random opponent", height * 0.3f, width * 0.8f, fontShader);
		randomPlay.setX((getWidth() / 2) - (randomPlay.getWidth() / 2));
		randomPlay.setY(getHeight() * 0.5f);
		
		randomPlay.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				fire(new GameStartEvent(selectedMapKey));
			}
		});
		
		addActor(randomPlay);
		
		CommonTextButton socialPlay = new CommonTextButton(skin, "Play with a friend", height * 0.3f, width * 0.8f, fontShader);
		socialPlay.setX((getWidth() / 2) - (socialPlay.getWidth() / 2));
		socialPlay.setY(getHeight() * 0.15f);
		
		socialPlay.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				fire(new SocialGameStartEvent(selectedMapKey));
			}
		});
		
		addActor(socialPlay);
		
	}

}
