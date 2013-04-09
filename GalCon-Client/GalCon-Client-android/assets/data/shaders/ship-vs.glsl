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
uniform mat4 uMVMatrix;
uniform mat4 uPMatrix;

void main() {
	gl_Position = uPMatrix * uMVMatrix * a_position;
}

