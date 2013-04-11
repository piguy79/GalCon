#ifdef GL_ES
#define LOWP lowp
#define MEDP mediump
#define HIGP highp
precision lowp float;
#else
#define LOWP
#define MEDP
#define HIGP
#endif

uniform vec4 uColor;
uniform float uRadius;

varying MEDP vec2 vTexCoords;

void main() {
	float dist = distance(vec2(0.5, 0.5), vTexCoords);

	gl_FragColor = mix(uColor, vec4(0, 0, 0, 0), smoothstep(uRadius, uRadius+0.05, dist));
}