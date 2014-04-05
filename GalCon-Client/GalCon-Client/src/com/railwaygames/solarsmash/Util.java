package com.railwaygames.solarsmash;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class Util {
	public static ShaderProgram createShader(String vertexShader, String fragmentShader) {
		ShaderProgram shader = new ShaderProgram(Gdx.files.internal(vertexShader), Gdx.files.internal(fragmentShader));
		if (!shader.isCompiled() && !shader.getLog().isEmpty()) {
			throw new IllegalStateException("Shader compilation fail: " + shader.getLog());
		}

		return shader;
	}
}
