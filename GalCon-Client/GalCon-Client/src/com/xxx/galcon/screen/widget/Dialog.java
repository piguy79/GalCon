package com.xxx.galcon.screen.widget;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.model.Point;

public class Dialog extends Table {
	
	protected AssetManager assetManager;
	private AtlasRegion dialogTextureBg;
	
	
	
	public Dialog(AssetManager assetManager, float width, float height) {
		super();
		this.assetManager = assetManager;
		setWidth(width);
		setHeight(height);
		createTable();
	}

	private void createTable() {
		TextureAtlas menusAtlas = assetManager.get("data/images/menus.atlas", TextureAtlas.class);
		dialogTextureBg = menusAtlas.findRegion("dialog_bg");
		setBackground(new TextureRegionDrawable(dialogTextureBg));
	}
	
	public void show(Point point, float duration){
		MoveToAction moveDialogAction = new MoveToAction();
		moveDialogAction.setX(point.x);
		moveDialogAction.setY(point.y);
		moveDialogAction.setDuration(duration);
		
		addAction(moveDialogAction);
	}
	
	public void hide(float duration){
		MoveToAction moveDialogAction = new MoveToAction();
		moveDialogAction.setX(-getWidth());
		moveDialogAction.setY(getY());
		moveDialogAction.setDuration(duration);

		addAction(moveDialogAction);
	}
	
	

}
