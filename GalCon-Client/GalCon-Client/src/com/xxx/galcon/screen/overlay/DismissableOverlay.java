package com.xxx.galcon.screen.overlay;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class DismissableOverlay extends Overlay {

	private Overlay delegate;

	public DismissableOverlay(TextureAtlas menusAtlas, Overlay delegate, ClickListener clickListener) {
		super(menusAtlas, 0.8f);
		this.delegate = delegate;

		this.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				DismissableOverlay.this.remove();
			}
		});
		if (clickListener != null) {
			this.addListener(clickListener);
		}
	}

	public DismissableOverlay(TextureAtlas menusAtlas, ClickListener clickListener) {
		this(menusAtlas, null, clickListener);
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);

		if (delegate != null) {
			delegate.draw(batch, parentAlpha);
		}
	}
}
