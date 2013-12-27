package com.xxx.galcon.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.xxx.galcon.Constants;
import com.xxx.galcon.screen.widget.ShaderLabel;

public class TextOverlay extends Overlay {

	private ShaderLabel shaderLabel;

	public TextOverlay(String text, TextureAtlas menusAtlas, Skin skin, ShaderProgram fontShader) {
		super(menusAtlas);
		shaderLabel = new ShaderLabel(fontShader, text, skin, Constants.UI.DEFAULT_FONT);

		float y = Gdx.graphics.getHeight() / 2 - shaderLabel.getHeight() / 2;
		shaderLabel.setBounds(0, y, Gdx.graphics.getWidth(), shaderLabel.getHeight());
		shaderLabel.setAlignment(Align.center, Align.center);
	}

	@Override
	protected void doCustomRender(SpriteBatch batch) {
		shaderLabel.draw(batch, 0.5f);
	}
}
