#ifdef GL_ES
	#ifdef GL_FRAGMENT_PRECISION_HIGH 
		precision highp float;
	#else
		precision mediump float;
	#endif
#endif

uniform float uPlanetBits[4];
uniform sampler2D planetTex;
uniform sampler2D planetTouchTex;
uniform sampler2D planetGlowTex;

varying vec2 vTexCoords;

const int INDEX_PLANET_OWNED_BY_USER = 0;
const int INDEX_PLANET_OWNED_BY_ENEMY = 1;
const int INDEX_PLANET_TOUCHED = 2;
const int INDEX_PLANET_ABILITY = 3;

vec4 blendOneMinusSourceAlpha(vec4 src, vec4 dest) {
	return src*src.a + dest*(1.0 - src.a);
}

void main() {	
	vec4 planetTexColor = texture2D(planetTex, vTexCoords);
	vec4 planetTexTouchColor = texture2D(planetTouchTex, vTexCoords);
	
	vec4 pixel;
	if(uPlanetBits[INDEX_PLANET_TOUCHED] == 1.0) {
		vec4 planetTexGlowColor = texture2D(planetGlowTex, vTexCoords);
		vec4 blend = blendOneMinusSourceAlpha(planetTexColor, planetTexGlowColor);
		pixel = blendOneMinusSourceAlpha(planetTexTouchColor, blend);
	} else {
		pixel = blendOneMinusSourceAlpha(planetTexTouchColor, planetTexColor);
	}
	
	if(uPlanetBits[INDEX_PLANET_OWNED_BY_USER] == 1.0) {
		pixel += vec4(-0.5, 0.3, -0.5, 0.0);
	} else if(uPlanetBits[INDEX_PLANET_OWNED_BY_ENEMY] == 1.0) {
		pixel += vec4(0.3, -0.5, -0.5, 0.0);
	} else if (uPlanetBits[INDEX_PLANET_TOUCHED] != 1.0) {
		pixel += vec4(-0.4, -0.4, -0.4, 0.0);
	}
	
	if(uPlanetBits[INDEX_PLANET_TOUCHED] == 1.0) {
		pixel += vec4(0.1, 0.1, 0.1, 0.0);
	}
	
	if(uPlanetBits[INDEX_PLANET_ABILITY] == 1.0) {
		pixel += vec4(-0.1, -0.1, 0.6, 0.0);
	}
	
	gl_FragColor = pixel;
}