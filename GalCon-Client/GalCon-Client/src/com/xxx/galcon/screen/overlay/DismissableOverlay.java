package com.xxx.galcon.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.xxx.galcon.InGameInputProcessor;

public class DismissableOverlay extends Overlay {

	private Overlay delegate;
	private boolean dismissed = false;
	private PostDismissAction postDismissAction;

	public DismissableOverlay(AssetManager assetManger, Overlay delegate) {
		super(assetManger, delegate.isDisplayOverlayTexture());
		this.delegate = delegate;
		postDismissAction = new PostDismissAction() {
			
			@Override
			public void apply() {				
			}
		};
	}
	
	public DismissableOverlay(AssetManager assetManger, Overlay delegate, PostDismissAction postDismissAction){
		super(assetManger, delegate.isDisplayOverlayTexture());
		this.delegate = delegate;
		this.postDismissAction = postDismissAction;
	}

	@Override
	protected void doCustomRender(float delta, SpriteBatch spriteBatch) {
		delegate.doCustomRender(delta, spriteBatch);

		InGameInputProcessor ip = (InGameInputProcessor) Gdx.input.getInputProcessor();
		if (ip.didTouch()) {
			dismissed = true;
			postDismissAction.apply();
			ip.consumeTouch();
		}
	}
	

	public boolean isDismissed() {
		return dismissed;
	}
}
