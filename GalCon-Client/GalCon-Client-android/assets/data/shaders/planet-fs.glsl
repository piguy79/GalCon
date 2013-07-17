#ifdef GL_ES
	#ifdef GL_FRAGMENT_PRECISION_HIGH 
		precision highp float;
	#else
		precision mediump float;
	#endif
#endif

uniform float uPlanetBits[4];
uniform int shipCount;
uniform float radius;
uniform sampler2D numbersTex;
uniform sampler2D planetTex;
uniform sampler2D planetTouchTex;

varying vec2 vTexCoords;

const int INDEX_PLANET_OWNED_BY_USER = 0;
const int INDEX_PLANET_OWNED_BY_ENEMY = 1;
const int INDEX_PLANET_TOUCHED = 2;
const int INDEX_PLANET_ABILITY = 3;

const vec4 ADD_OWN_COLOR = vec4(-0.5, -0.1, -0.5, 0.0);
const vec4 ADD_ENEMY_COLOR = vec4(-0.1, -0.5, -0.5, 0.0);
const vec4 ADD_NOT_TOUCHED_COLOR = vec4(-0.4, -0.4, -0.4, 0.0);
const vec4 ADD_TOUCHED_COLOR = vec4(0.1, 0.1, 0.1, 0.0);
const vec4 ADD_ABILITY_COLOR = vec4(-0.1, -0.1, 0.6, 0.0);
const float IMAGE_WIDTH = 512.0; 

float numberOffset(int number);

void main() {	
	vec4 pixel;
	if(uPlanetBits[INDEX_PLANET_TOUCHED] == 1.0) {
		pixel = texture2D(planetTouchTex, vTexCoords);
	} else {
		pixel = texture2D(planetTex, vTexCoords);
	}
	
	if(uPlanetBits[INDEX_PLANET_OWNED_BY_USER] == 1.0) {
		pixel += ADD_OWN_COLOR;
	} else if(uPlanetBits[INDEX_PLANET_OWNED_BY_ENEMY] == 1.0) {
		pixel += ADD_ENEMY_COLOR;
	} else if (uPlanetBits[INDEX_PLANET_TOUCHED] != 1.0) {
		pixel += ADD_NOT_TOUCHED_COLOR;
	}
	
	if(uPlanetBits[INDEX_PLANET_TOUCHED] == 1.0) {
		pixel += ADD_TOUCHED_COLOR;
	}
	
	if(uPlanetBits[INDEX_PLANET_ABILITY] == 1.0) {
		pixel += ADD_ABILITY_COLOR;
	}
	
	// Number rendering //////////////
	// make the numbers larger to account for the smaller scale on small planets
	float modifier = (0.5 - radius) * 1.6;
	
	float xDistToCenter = 0.5 - vTexCoords.x;
	xDistToCenter += xDistToCenter * modifier;
	
	float yDistToCenter = vTexCoords.y - 0.5;
	yDistToCenter -= yDistToCenter * modifier;
	
	float halfWidth = 0.1 + 0.2 * modifier;
	float halfHeight = 0.12;
	
	float textAreaHalfWidth = halfWidth;
	if(shipCount > 9) {
		textAreaHalfWidth *= 2.0;
	}
	
	vec4 numberTexColor = vec4(0,0,0,0);
	if(xDistToCenter < textAreaHalfWidth && xDistToCenter > -textAreaHalfWidth 
		&& yDistToCenter < halfHeight && yDistToCenter > -halfHeight) {
		
		int numberToShow = shipCount;
		
		float xNumberCenter = 0.5;
		if(shipCount > 9) {
			if(xDistToCenter > 0.0) {
				xNumberCenter = 0.5 - halfWidth;
				numberToShow = numberToShow / 10;
			} else {
				xNumberCenter = 0.5 + halfWidth;
				numberToShow = numberToShow - (numberToShow / 10) * 10;
			}
		}
			
		float xDistToNumberCenter = vTexCoords.x - xNumberCenter;
		xDistToNumberCenter -= xDistToNumberCenter * modifier;
		float yDistToNumberCenter = yDistToCenter;
		xDistToNumberCenter = (xDistToNumberCenter + halfWidth) * (1.0 / (1.0-(halfWidth + halfWidth)));
		yDistToNumberCenter = (yDistToNumberCenter + halfHeight) * (1.0 / (1.0-(halfHeight + halfHeight)));
				
		float xMult = 0.42;
		if(numberToShow == 1) {
			xMult = 0.36;
		}
		xMult -= 0.55 * modifier;
		numberTexColor = texture2D(numbersTex, 
									vec2(numberOffset(numberToShow) + xDistToNumberCenter * xMult, yDistToNumberCenter * 0.4));
	}
	
	if(numberTexColor.a != 0.0) {
		gl_FragColor = numberTexColor;
	} else {
		gl_FragColor = pixel;
	}
}

float numberOffset(int number) {
	if(number == 0) {
		return 444.0/IMAGE_WIDTH;
	}
	if(number == 1) {
		return 402.0/IMAGE_WIDTH;
	}
	if(number == 2) {
		return 351.0/IMAGE_WIDTH;
	}
	if(number == 3) {
		return 302.0/IMAGE_WIDTH;
	}
	if(number == 4) {
		return 248.0/IMAGE_WIDTH;
	}
	if(number == 5) {
		return 200.0/IMAGE_WIDTH;
	}
	if(number == 6) {
		return 150.0/IMAGE_WIDTH;
	}
	if(number == 7) {
		return 100.0/IMAGE_WIDTH;
	}
	if(number == 8) {
		return 50.0/IMAGE_WIDTH;
	}
	
	return 0.0;
}