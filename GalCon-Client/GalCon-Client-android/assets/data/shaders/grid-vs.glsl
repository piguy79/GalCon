#ifdef GL_ES
	#ifdef GL_FRAGMENT_PRECISION_HIGH 
		precision highp float;
	#else
		precision mediump float;
	#endif
#endif

attribute vec4 a_position;
uniform mat4 uMVMatrix;
uniform mat4 uPMatrix;

attribute vec2 a_texCoord0;
varying vec2 vTexCoords;

void main() {
	vTexCoords = a_texCoord0;	
	gl_Position = uPMatrix * uMVMatrix * a_position;
}

