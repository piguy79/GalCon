package com.railwaygames.solarsmash.screen.overlay;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.railwaygames.solarsmash.screen.Resources;

public class DismissableOverlay extends Overlay {

	private Overlay delegate;

	public DismissableOverlay(Resources resources, Overlay delegate, ClickListener clickListener) {
		super(resources);
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

	public DismissableOverlay(Resources resources, Overlay delegate) {
		this(resources, delegate, null);
	}

	public DismissableOverlay(Resources resources, ClickListener clickListener) {
		this(resources, null, clickListener);
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);

		if (delegate != null) {
			delegate.draw(batch, parentAlpha);
		}
	}
}
