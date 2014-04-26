package com.railwaygames.solarsmash.screen.overlay;

import static com.railwaygames.solarsmash.Constants.CONNECTION_ERROR_MESSAGE;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.UIConnectionWrapper;
import com.railwaygames.solarsmash.config.ConfigResolver;
import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.model.Map;
import com.railwaygames.solarsmash.model.Maps;
import com.railwaygames.solarsmash.model.Player;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;
import com.railwaygames.solarsmash.screen.widget.WaitImageButton;

public class LevelUpOverlay extends Overlay {
	
	private Player player;
	private Maps allMaps;
	private WaitImageButton waitImage;
	protected float height;
	protected float width;
	
	private ShaderLabel reachedLabel;


	public LevelUpOverlay(Resources resources, Player player) {
		super(resources, 0.95f);
		this.player = player;
		this.height = Gdx.graphics.getHeight();
		this.width = Gdx.graphics.getWidth();
		
		createWaitImage();
		UIConnectionWrapper.findAllMaps(mapResultCallback);
	}
	
	private void createOverlay(){
		createReachLabel();
		createLevelLabel();
		createMapUnlock();
	}
	
	

	private void createLevelLabel() {
		ShaderLabel levelLabel = new ShaderLabel(resources.fontShader, "Level " + ConfigResolver.getRankForXp(player.xp).level, resources.skin, Constants.UI.LARGE_FONT_YELLOW);
		levelLabel.setX(0);
		levelLabel.setWidth(width);
		levelLabel.setY(reachedLabel.getY() - (levelLabel.getTextBounds().height * 1.3f));
		levelLabel.setAlignment(Align.center);
		addActor(levelLabel);
	}

	private void createReachLabel() {
		reachedLabel = new ShaderLabel(resources.fontShader, "You've Reached", resources.skin, Constants.UI.DEFAULT_FONT);
		reachedLabel.setX(0);
		reachedLabel.setWidth(width);
		reachedLabel.setAlignment(Align.center);
		reachedLabel.setY(height * 0.8f);
		addActor(reachedLabel);
	}
	
	private void createMapUnlock() {
		
		Map unlockedMap = mapUnlocked();
		if(unlockedMap != null){
			
			Image levelCardBg = new Image(resources.skin.getDrawable(Constants.UI.LEVEL_CARD_BG));
			levelCardBg.setWidth(width * 0.4f);
			levelCardBg.setHeight(height * 0.4f);
			levelCardBg.setX((width / 2) - (levelCardBg.getWidth() / 2));
			levelCardBg.setY(height * 0.1f);
			addActor(levelCardBg);
			

			TextureRegionDrawable mapTex = new TextureRegionDrawable(resources.levelAtlas.findRegion("" + unlockedMap.key));
			Image mapImage = new Image(mapTex);
			
			mapImage.setWidth(levelCardBg.getWidth());
			mapImage.setHeight(levelCardBg.getHeight() / 2);
			mapImage.setX(levelCardBg.getX());
			mapImage.setY(levelCardBg.getY() + (mapImage.getHeight() / 2));
			addActor(mapImage);
			
			ShaderLabel unlockLabel = new ShaderLabel(resources.fontShader, "New Map Unlocked!", resources.skin, Constants.UI.DEFAULT_FONT);
			unlockLabel.setX(0);
			unlockLabel.setY(levelCardBg.getY() + levelCardBg.getHeight() + unlockLabel.getTextBounds().height);
			unlockLabel.setWidth(width);
			unlockLabel.setAlignment(Align.center);
			addActor(unlockLabel);
			
			ShaderLabel desc = new ShaderLabel(resources.fontShader, unlockedMap.description, resources.skin, Constants.UI.X_SMALL_FONT_BLACK);
			desc.setX(unlockLabel.getX());
			desc.setY(mapImage.getY() - (desc.getTextBounds().height * 2));
			desc.setWidth(unlockLabel.getWidth());
			desc.setAlignment(Align.center);
			addActor(desc);
			

			ShaderLabel title = new ShaderLabel(resources.fontShader, unlockedMap.title, resources.skin, Constants.UI.X_SMALL_FONT_BLACK);
			title.setX(unlockLabel.getX());
			title.setY(mapImage.getY() + mapImage.getHeight() + title.getTextBounds().height);
			title.setWidth(unlockLabel.getWidth());
			title.setAlignment(Align.center);
			addActor(title);
		}
	}
	
	private Map mapUnlocked(){
		for(Map map : allMaps.allMaps){
			if(ConfigResolver.getRankForXp(map.availableFromXp).level == ConfigResolver.getRankForXp(player.xp).level){
				return map;
			}
		}
		
		return null;
	}

	private void createWaitImage(){
		waitImage = new WaitImageButton(resources.skin);
		float buttonWidth = .25f * (float) width;
		waitImage.setWidth(buttonWidth);
		waitImage.setHeight(buttonWidth);
		waitImage.setX((width / 2) - (buttonWidth / 2));
		waitImage.setY(height * 0.3f);
		
		addActor(waitImage);
		
		waitImage.start();
	}
	
	private UIConnectionResultCallback<Maps> mapResultCallback = new UIConnectionResultCallback<Maps>() {

		@Override
		public void onConnectionResult(Maps result) {
			allMaps = result;
			waitImage.stop();
			createOverlay();
		}

		@Override
		public void onConnectionError(String msg) {
			waitImage.stop();
			final Overlay overlay = new DismissableOverlay(resources, new TextOverlay(CONNECTION_ERROR_MESSAGE, resources));
			addActor(overlay);
		}
	};

}
