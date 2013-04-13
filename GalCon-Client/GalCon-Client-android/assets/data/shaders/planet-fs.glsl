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

uniform int shipCount;
uniform vec4 uColor;
uniform float uRadius;
uniform sampler2D numbersTex;

varying vec2 vTexCoords;

void main() {
	float dist = distance(vec2(0.5, 0.5), vTexCoords);
	float xdist = 0.5 - vTexCoords.x;
	float ydist = 0.5 - vTexCoords.y;
	
	if(shipCount > -1) {
		if(xdist < 0.2 && xdist > -0.2 && ydist < 0.25 && ydist > -0.25) {
			xdist = (xdist + 0.2) * (1.0 / 0.6);
			ydist = (ydist + 0.25) * (1.0 / 0.5);
				
			vec4 numberTexColor = texture2D(numbersTex, vec2(xdist * 0.05, ydist * 0.05));
			gl_FragColor = numberTexColor;
		} else {
			gl_FragColor = mix(uColor, vec4(0, 0, 0, 0), smoothstep(uRadius, uRadius + 0.05, dist));
		}
	} else {
		gl_FragColor = vec4(1,1,0,0);
	}
	
}