package com.railwaygames.solarsmash.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;

public class TextOverlay extends Overlay {

	private ShaderLabel label;
	private String text;

	public TextOverlay(String text, Resources resources) {
		super(resources);
		label = new ShaderLabel(resources.fontShader, text, resources.skin, Constants.UI.DEFAULT_FONT, Color.WHITE);
		text = this.text;

		float y = Gdx.graphics.getHeight() / 2 - label.getHeight() / 2;
		float margin = Gdx.graphics.getWidth() * 0.03f;
		label.setBounds(margin, y, Gdx.graphics.getWidth() - 2 * margin, label.getHeight());
		label.setWrap(true);
		label.setAlignment(Align.center, Align.center);
		addActor(label);
	}

	protected ShaderLabel getLabel() {
		return label;
	}

	public String getOriginalText() {
		return text;
	}
}
