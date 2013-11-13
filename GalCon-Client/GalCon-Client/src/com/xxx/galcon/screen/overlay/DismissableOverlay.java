package com.xxx.galcon.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.xxx.galcon.InGameInputProcessor;

public class DismissableOverlay extends Overlay {

	private Overlay delegate;
	private boolean dismissed = false;
	private PostDismissAction postDismissAction;

	public DismissableOverlay(TextureAtlas menusAtlas, Overlay delegate) {
		super(menusAtlas, delegate.isDisplayOverlayTexture());
		this.delegate = delegate;
		postDismissAction = new PostDismissAction() {

			@Override
			public void apply() {
			}
		};
	}

	public DismissableOverlay(TextureAtlas menusAtlas, Overlay delegate, PostDismissAction postDismissAction) {
		super(menusAtlas, delegate.isDisplayOverlayTexture());
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
