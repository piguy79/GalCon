#ifdef GL_ES
#define LOWP lowp
#define MEDP mediump
#define HIGP highp
#else
#define LOWP
#define MEDP
#define HIGP
#endif

attribute vec4 a_position;
attribute MEDP vec2 a_texCoord0;

uniform mat4 uMVMatrix;
uniform mat4 uPMatrix;

varying MEDP vec2 vTexCoords;

void main() {
	vTexCoords = a_texCoord0;	
	gl_Position = uPMatrix * uMVMatrix * a_position;
}

