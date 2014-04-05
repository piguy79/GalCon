package com.railwaygames.solarsmash.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;

public class TextOverlay extends Overlay {

	private ShaderLabel shaderLabel;

	public TextOverlay(String text, Resources resources) {
		super(resources);
		shaderLabel = new ShaderLabel(resources.fontShader, text, resources.skin, Constants.UI.DEFAULT_FONT);

		float y = Gdx.graphics.getHeight() / 2 - shaderLabel.getHeight() / 2;
		shaderLabel.setBounds(0, y, Gdx.graphics.getWidth(), shaderLabel.getHeight());
		shaderLabel.setWrap(true);
		shaderLabel.setAlignment(Align.center, Align.center);
		addActor(shaderLabel);
	}
}
