package com.xxx.galcon.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class DismissableOverlay extends Overlay {

	private Overlay delegate;
	private boolean dismissed = false;

	public DismissableOverlay(AssetManager assetManger, Overlay delegate) {
		super(assetManger);
		this.delegate = delegate;
	}

	@Override
	protected void doCustomRender(float delta, SpriteBatch spriteBatch) {
		delegate.doCustomRender(delta, spriteBatch);

		if (Gdx.input.justTouched()) {
			dismissed = true;
		}
	}

	public boolean isDismissed() {
		return dismissed;
	}
}
