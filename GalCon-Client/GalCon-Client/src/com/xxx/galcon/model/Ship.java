package com.xxx.galcon.model;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

public class Ship {
	private Mesh shipMesh;
	private ShaderProgram colorShader;
	private Matrix4 location;

	public Ship(Mesh shipMesh, ShaderProgram shaderProgram) {
		this.shipMesh = shipMesh;
		this.colorShader = shaderProgram;
	}

	public void render() {
		
		shipMesh.render(colorShader, GL20.GL_TRIANGLES);
	}
}
